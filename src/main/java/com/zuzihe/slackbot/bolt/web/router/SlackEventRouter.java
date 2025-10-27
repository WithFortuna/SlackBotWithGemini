package com.zuzihe.slackbot.bolt.web.router;

import aichatter.bolt.application.aichatter.chat.AichatterChatSlackHandlersInstaller;
import aichatter.bolt.application.docsbot.DocsbotSlackHandlersInstaller;
import aichatter.bolt.config.CachedBodyHttpServletRequest;
import aichatter.bolt.config.dto.SlackUserInfo;
import aichatter.bolt.domain.SlackAppRegistration;
import aichatter.bolt.domain.SlackAppType;
import aichatter.bolt.repository.SlackAppRegistrationRepository;
import aichatter.bolt.util.SlackAppFactory;
import aichatter.bolt.util.SlackRequestParser;
import aichatter.global.ErrorCode;
import aichatter.global.exception.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.api.bolt.App;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static aichatter.config.SecurityConfig.LoggerUtil.logger;

@RestController
@RequestMapping("/api/slack/events")
@RequiredArgsConstructor
public class SlackEventRouter {
    private final SlackAppRegistrationRepository appRegistrationRepository;
    private final AichatterChatSlackHandlersInstaller aichatterChatSlackHandlersInstaller;
    private final DocsbotSlackHandlersInstaller docsbotSlackHandlersInstaller;
    private final SlackAppFactory slackAppFactory;
    private final ObjectMapper objectMapper;

    @PostMapping("/AICHATTER_CHAT")
    public void routeAichatterChatEvents(HttpServletRequest request,
                                         HttpServletResponse response) throws IOException {
        SlackUserInfo slackUserInfo = SlackRequestParser.extractSlackUserInfo(new CachedBodyHttpServletRequest(request));
        SlackAppRegistration appRegistration = appRegistrationRepository.findBySlackAppType(SlackAppType.AICHATTER_CHAT)
                .orElseThrow(() -> new CustomException(ErrorCode.SLACK_APP_NOT_REGISTERED));

        App app = slackAppFactory.createApp(appRegistration.getSigningSecret(), slackUserInfo.teamId(), appRegistration.getId());
        aichatterChatSlackHandlersInstaller.installEventAndInteractiveHandlers(app);

        SlackBoltDelegator.delegateSlackRequest(request, response, app, appRegistration);
    }

    @PostMapping("/DOCSBOT")
    public void routeDocsbotEvents(HttpServletRequest request,
                                   HttpServletResponse response) throws IOException {
        SlackUserInfo slackUserInfo = SlackRequestParser.extractSlackUserInfo(new CachedBodyHttpServletRequest(request));
        SlackAppRegistration appRegistration = appRegistrationRepository.findBySlackAppType(SlackAppType.DOCSBOT)
                .orElseThrow(() -> new CustomException(ErrorCode.SLACK_APP_NOT_REGISTERED));

        App app = slackAppFactory.createApp(appRegistration.getSigningSecret(), slackUserInfo.teamId(), appRegistration.getId());
        docsbotSlackHandlersInstaller.installEventAndInteractiveHandlers(app);

        SlackBoltDelegator.delegateSlackRequest(request, response, app, appRegistration);
    }

    private String extractTeamIdFrom(HttpServletRequest request) throws IOException {
        String bodyString = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        Map bodyMap = objectMapper.readValue(bodyString, Map.class);
        Object teamId = bodyMap.get("team_id");
        logger.info("이벤트 요청 원문: {}", bodyString);
        return String.valueOf(teamId);
    }
}

