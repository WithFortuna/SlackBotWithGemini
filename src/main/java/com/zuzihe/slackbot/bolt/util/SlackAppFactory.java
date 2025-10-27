package com.zuzihe.slackbot.bolt.util;

import aichatter.bolt.repository.SlackWorkspaceInstallationRepository;
import aichatter.global.ErrorCode;
import aichatter.global.exception.CustomException;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static aichatter.config.SecurityConfig.LoggerUtil.logger;

@RequiredArgsConstructor
@Component
public class SlackAppFactory {
    private final SlackWorkspaceInstallationRepository workspaceInstallationRepository;

    private App createApp(String signingSecret, String botToken) {
        AppConfig config = AppConfig.builder()
                .signingSecret(signingSecret)
                .singleTeamBotToken(botToken)
                .build();
        return new App(config);
    }

    public App createApp(String signingSecret, String teamId, Long slackAppRegistrationId) {
        String botToken = workspaceInstallationRepository.findByTeamIdAndSlackAppRegistrationId(teamId, slackAppRegistrationId)
                .orElseThrow(() -> new CustomException(ErrorCode.SLACK_WORKSPACE_NOT_INSTALLED))
                .getBotToken();
        logger.info("bot Token 가져오기 성공: {}", botToken);

        return createApp(signingSecret, botToken);
    }
}
