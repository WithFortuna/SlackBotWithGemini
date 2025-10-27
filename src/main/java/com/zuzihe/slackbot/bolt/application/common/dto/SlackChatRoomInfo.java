package com.zuzihe.slackbot.bolt.application.common.dto;

public record SlackChatRoomInfo(
        String channel,
        String threadTs
) { }
