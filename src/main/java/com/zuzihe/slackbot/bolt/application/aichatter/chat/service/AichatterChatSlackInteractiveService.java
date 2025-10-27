package com.zuzihe.slackbot.bolt.application.aichatter.chat.service;

import aichatter.bolt.application.common.service.install.SlackWorkspaceInstallationService;
import aichatter.bolt.domain.SlackAppType;
import aichatter.bolt.infra.SlackBoltClient;
import aichatter.bolt.repository.SlackAppRegistrationRepository;
import aichatter.global.ErrorCode;
import aichatter.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static aichatter.config.SecurityConfig.LoggerUtil.logger;

@Service
@RequiredArgsConstructor
public class AichatterChatSlackInteractiveService {
    private final SlackBoltClient slackBoltClient;
    private final SlackWorkspaceInstallationService slackWorkspaceInstallationService;
    private final SlackAppRegistrationRepository appRegistrationRepository;

    @Async
    public void openNewChatWithUser(String teamId, String userId, String chatRoomId) {
        logger.info("[Bolt] 새로운 채팅 시작 - TeamId: {}, UserId: {}", teamId, userId);
        Long appRegistrationId = appRegistrationRepository.findBySlackAppType(SlackAppType.AICHATTER_CHAT)
                .orElseThrow(() -> new CustomException(ErrorCode.SLACK_APP_NOT_REGISTERED))
                .getId();
        slackWorkspaceInstallationService.getBotToken(teamId, appRegistrationId)
                .ifPresent(token -> slackBoltClient.openConversationAndSendWelcomeMessageWithChatRoomId(token, userId, chatRoomId));
    }
}
