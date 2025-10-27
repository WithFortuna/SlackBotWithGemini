package com.zuzihe.slackbot.bolt.application.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SlackChatRoomCreateDTO(
        @NotBlank(message = "slackAppRegistraionId는 필수입니다")
        Long slackAppRegistrationId,

        @NotBlank(message = "teamId는 필수입니다")
        String teamId,

        @NotBlank(message = "channel은 필수입니다")
        String channel,

        @NotBlank(message = "threadTs는 필수입니다")
        String threadTs,

        @NotNull(message = "slackAccountId는 필수입니다")
        Long slackWorkspaceUserId,

        @NotBlank(message = "chatRoomId는 필수입니다")
        String chatRoomId,

        @NotBlank(message = "chatRoomTitle은 필수입니다")
        String chatRoomTitle,

        @NotNull(message = "domainId는 필수입니다")
        Long domainId,

        @NotNull(message = "accountId는 필수입니다")
        Long accountId
) {
}
