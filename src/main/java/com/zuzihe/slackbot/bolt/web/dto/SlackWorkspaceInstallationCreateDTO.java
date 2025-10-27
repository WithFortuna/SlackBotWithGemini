package com.zuzihe.slackbot.bolt.web.dto;

public record SlackWorkspaceInstallationCreateDTO(
        Long appRegistrationId,
        String teamId,
        String teamName,
        String botToken,
        String botUserId,
        String botScopes,
        String installerUserId,
        boolean isEnterpriseInstall
) {
}
