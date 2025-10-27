package com.zuzihe.slackbot.bolt.application.docsbot.service;

import aichatter.bolt.application.common.service.install.SlackWorkspaceInstallationService;
import aichatter.bolt.infra.SlackBoltClient;
import aichatter.bolt.util.SlackUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static aichatter.config.SecurityConfig.LoggerUtil.logger;

@Service
@RequiredArgsConstructor
public class DocsbotSlackInteractiveService {
    private final SlackBoltClient slackBoltClient;
    private final SlackWorkspaceInstallationService slackWorkspaceInstallationService;
    private final SlackUtils slackUtils;

    @Async
    public void openNewChatWithUser(String teamId, String userId, String chatRoomId) {
        logger.info("[Bolt] 새로운 채팅 시작 - TeamId: {}, UserId: {}", teamId, userId);
        Long appRegistrationId = slackUtils.getDocsbotAppRegistrationId();

        slackWorkspaceInstallationService.getBotToken(teamId, appRegistrationId)
                .ifPresent(token -> slackBoltClient.openConversationAndSendWelcomeMessageWithChatRoomId(token, userId, chatRoomId));
    }
}
