package com.zuzihe.slackbot.slack.http.install.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuzihe.slackbot.slack.http.install.app.web.SlackOAuthResponse;
import com.zuzihe.slackbot.slack.workspace.service.WorkspaceInstallationService;
import com.zuzihe.slackbot.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackOAuthService {

    private final WorkspaceInstallationService workspaceInstallationService;

    @Value("${slack.client-id}")
    private String slackClientId;

    @Value("${slack.client-secret}")
    private String slackClientSecret;

    @Value("${slack.redirect-uri}")
    private String redirectUri;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient;

    public String getInstallRedirect(String state) {
        return "https://slack.com/oauth/v2/authorize"
                + "?client_id=" + slackClientId
                + "&scope=commands,chat:write"
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&state=" + state;
    }

    public String handleCallback(String code, String state) throws JsonProcessingException {
        log.info("Slack callback 도착! code = {}, state = {}", code, state);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", slackClientId);
        form.add("client_secret", slackClientSecret);
        form.add("redirect_uri", redirectUri);

        String rawJson = webClient.post()
                .uri("https://slack.com/api/oauth.v2.access")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("Slack OAuth 응답 원문:\n{}", rawJson);

        SlackOAuthResponse response = objectMapper.readValue(rawJson, SlackOAuthResponse.class);

        if (!response.isOk()) {
            throw new CustomException("Slack OAuth 실패: " + response.getError());
        }

        saveInstalledWorkspace(response);
        return "Slack 앱 설치 완료!";
    }

    private void saveInstalledWorkspace(SlackOAuthResponse response) {
        if (response.getTeam() == null) {
            log.error("team 정보가 Slack 응답에 없습니다.");
            throw new IllegalStateException("Slack 응답에 team 정보 없음");
        }

        String teamId = response.getTeam().getId();
        String teamName = response.getTeam().getName();
        String botToken = response.getAccess_token();
        String botUserId = response.getBot_user_id();
        String scope = response.getScope();

        // 기존 OAuth 방식으로 받은 토큰을 데이터베이스에 저장
        try {
            workspaceInstallationService.saveOrUpdateInstallation(
                    teamId,
                    teamName,
                    botToken,
                    botUserId,
                    scope,
                    null, // userToken - OAuth v2에서는 별도 처리
                    null, // userId
                    null, // userScopes
                    null  // installerUserId - 기존 방식에서는 추적하지 않음
            );

            log.info("워크스페이스 설치 정보 저장 완료: teamId={}, teamName={}", teamId, teamName);
        } catch (Exception e) {
            log.error("워크스페이스 설치 정보 저장 실패: teamId={}", teamId, e);
            throw new CustomException("설치 정보 저장 실패: " + e.getMessage());
        }
    }
}
