package com.zuzihe.slackbot.bolt.web.dto;

import aichatter.bolt.domain.SlackWorkspaceUser;

import java.time.OffsetDateTime;

public record SlackWorkspaceUserDTO(
        Long id,
        OffsetDateTime regDate,
        OffsetDateTime modDate,
        Long domainId,
        Long accountId,         // aichatter 계정 아이디
        String userType,        // aichatter 계정 유저타입
        String teamId,          // slack workspace 식별자
        String slackUserId     // workspace 내 식별자
){
    public SlackWorkspaceUserDTO(SlackWorkspaceUser slackWorkspaceUser) {
        this(
                slackWorkspaceUser.getId(),
                slackWorkspaceUser.getRegDate(),
                slackWorkspaceUser.getModDate(),
                slackWorkspaceUser.getDomainId(),
                slackWorkspaceUser.getAccountId(),
                slackWorkspaceUser.getUserType(),
                slackWorkspaceUser.getTeamId(),
                slackWorkspaceUser.getSlackUserId()
        );
    }
}
