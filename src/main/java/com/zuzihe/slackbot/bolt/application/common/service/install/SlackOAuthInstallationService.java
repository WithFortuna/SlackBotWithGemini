package com.zuzihe.slackbot.bolt.application.common.service.install;

import aichatter.bolt.domain.SlackAppRegistration;
import aichatter.bolt.domain.SlackAppType;
import aichatter.bolt.repository.SlackAppRegistrationRepository;
import aichatter.bolt.web.dto.SlackOAuthResponse;
import aichatter.bolt.web.dto.SlackWorkspaceInstallationCreateDTO;
import aichatter.global.ErrorCode;
import aichatter.global.exception.CustomException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static aichatter.config.SecurityConfig.LoggerUtil.logger;

@Service
@RequiredArgsConstructor
public class SlackOAuthInstallationService {

    private final SlackWorkspaceInstallationService slackWorkspaceInstallationService;

    private final SlackAppRegistrationRepository appRegistrationRepository;

    private final SlackStateService stateService;

    @Value("${slack.redirect-uri}")
    private String redirectUri;

    @Value("${slack.slack-app-scope}")
    private String slackAppScope;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient.Builder webClientBuilder;

    public String getInstallUrl(String state, String baseUrl, SlackAppType appType) {
        SlackAppRegistration appRegistration = getAppRegistrationByAppType(appType);

        String redirectUrl = baseUrl + redirectUri;
        return "https://slack.com/oauth/v2/authorize"
                + "?client_id=" + appRegistration.getClientId()
                + "&scope=" + slackAppScope
                + "&redirect_uri=" + URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8)
                + "&state=" + state;
    }

    private SlackAppRegistration getAppRegistrationByAppType(SlackAppType appType) {
        return appRegistrationRepository.findBySlackAppType(appType)
                .orElseThrow(() -> new CustomException(ErrorCode.SLACK_WORKSPACE_USER_NOT_FOUND));
    }

    public String handleCallback(String code, String state, String baseUrl) throws JsonProcessingException {
        SlackAppType appType = stateService.verifyAndConsume(state);
        SlackAppRegistration appRegistration = getAppRegistrationByAppType(appType);

        logger.info("Slack callback 도착! code = {}, state = {}", code, state);
        String redirectUrl = baseUrl + redirectUri;
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", appRegistration.getClientId());
        form.add("client_secret", appRegistration.getClientSecret());
        form.add("redirect_uri", redirectUrl);

        WebClient webClient = webClientBuilder.build();
        String rawJson = webClient.post()
                .uri("https://slack.com/api/oauth.v2.access")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        logger.info("Slack OAuth 응답 원문:\n{}", rawJson);

        SlackOAuthResponse response = objectMapper.readValue(rawJson, SlackOAuthResponse.class);

        if (!response.isOk()) {
            throw new IllegalArgumentException("Slack OAuth 실패: " + response.getError());
        }

        saveInstalledWorkspace(response);
        return "Slack 앱 설치 완료!";
    }

    private void saveInstalledWorkspace(SlackOAuthResponse response) {
        if (response.getTeam() == null) {
            logger.error("team 정보가 Slack 응답에 없습니다.");
            throw new IllegalStateException("Slack 응답에 team 정보 없음");
        }

        String appId = response.getAppId();
        logger.info("워크스페이스 설치중 .. 시도중인 appId:{}", appId);
        Long appRegistrationId = appRegistrationRepository.findByAppId(appId)
                .orElseThrow(() -> new CustomException(ErrorCode.SLACK_APP_NOT_REGISTERED))
                .getId();

        SlackWorkspaceInstallationCreateDTO dto = new SlackWorkspaceInstallationCreateDTO(
                appRegistrationId,
                response.getTeam().getId(),
                response.getTeam().getName(),
                response.getAccessToken(),
                response.getBotUserId(),
                response.getScope(),
                response.getAuthedUser().getId(),
                response.isEnterpriseInstall()
        );

        try {
            slackWorkspaceInstallationService.saveInstallation(dto);
            logger.info("워크스페이스 설치 정보 저장 완료: teamId={}, teamName={}", dto.teamId(), dto.teamName());
        } catch (Exception e) {
            logger.error("워크스페이스 설치 정보 저장 실패: teamId={}", dto.teamId(), e);
            throw new IllegalArgumentException("설치 정보 저장 실패: " + e.getMessage());
        }
    }
}
