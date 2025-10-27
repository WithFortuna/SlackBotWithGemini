package com.zuzihe.slackbot.bolt.web.router;

import aichatter.bolt.application.aichatter.chat.AichatterChatSlackHandlersInstaller;
import aichatter.bolt.application.docsbot.DocsbotSlackHandlersInstaller;
import aichatter.bolt.config.CachedBodyHttpServletRequest;
import aichatter.bolt.domain.SlackAppRegistration;
import aichatter.bolt.domain.SlackAppType;
import aichatter.bolt.repository.SlackAppRegistrationRepository;
import aichatter.bolt.util.SlackAppFactory;
import aichatter.bolt.util.SlackRequestParser;
import aichatter.global.ErrorCode;
import aichatter.global.exception.CustomException;
import com.slack.api.bolt.App;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static aichatter.bolt.web.router.SlackBoltDelegator.delegateSlackRequest;
import static aichatter.config.SecurityConfig.LoggerUtil.logger;

@RestController
@RequestMapping("/api/slack/interactive")
@RequiredArgsConstructor
public class SlackInteractiveRouter {
    private final SlackAppRegistrationRepository appRegistrationRepository;
    private final AichatterChatSlackHandlersInstaller aichatterChatSlackHandlersInstaller;
    private final DocsbotSlackHandlersInstaller docsbotSlackHandlersInstaller;
    private final SlackAppFactory slackAppFactory;

    @PostMapping("/AICHATTER_CHAT")
    public void routeAichatterChatEvents(HttpServletRequest request,
                                         HttpServletResponse response) throws IOException {
        logging(request);

        SlackAppRegistration appRegistration = appRegistrationRepository.findBySlackAppType(SlackAppType.AICHATTER_CHAT)
                .orElseThrow(() -> new CustomException(ErrorCode.SLACK_APP_NOT_REGISTERED));
        String teamId = SlackRequestParser.extractTeamId(new CachedBodyHttpServletRequest(request));

        App app = slackAppFactory.createApp(appRegistration.getSigningSecret(), teamId, appRegistration.getId());
        aichatterChatSlackHandlersInstaller.installEventAndInteractiveHandlers(app);

        delegateSlackRequest(request, response, app, appRegistration);
    }

    private static void logging(HttpServletRequest request) throws IOException {
        String bodyString = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        logger.info("이벤트 요청 원문: {}", bodyString);
    }

    @PostMapping("/DOCSBOT")
    public void routeDocsbotEvents(HttpServletRequest request,
                                   HttpServletResponse response) throws IOException {
        logging(request);

        SlackAppRegistration appRegistration = appRegistrationRepository.findBySlackAppType(SlackAppType.DOCSBOT)
                .orElseThrow(() -> new CustomException(ErrorCode.SLACK_APP_NOT_REGISTERED));
        String teamId = SlackRequestParser.extractTeamId(new CachedBodyHttpServletRequest(request));

        App app = slackAppFactory.createApp(appRegistration.getSigningSecret(), teamId, appRegistration.getId());
        docsbotSlackHandlersInstaller.installEventAndInteractiveHandlers(app);

        delegateSlackRequest(request, response, app, appRegistration);
    }
}

