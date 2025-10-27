package com.zuzihe.slackbot.bolt.application.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SlackChatRoomSearchDTO(
        @NotBlank(message = "slackAppRegistrationId는 필수입니다")
        Long slackAppRegistrationId,

        @NotBlank(message = "teamId는 필수입니다")
        String teamId,

        @NotBlank(message = "channel은 필수입니다")
        String channel,

        @NotBlank(message = "threadTs는 필수입니다")
        String threadTs,

        @NotNull(message = "slackWorkspaceUserId는 필수입니다")
        Long slackWorkspaceUserId
) {
}
