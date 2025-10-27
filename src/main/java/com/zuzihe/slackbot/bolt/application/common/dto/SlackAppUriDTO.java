package com.zuzihe.slackbot.bolt.application.common.dto;

public record SlackAppUriDTO(
        String eventUri,
        String interactiveUri,
        String redirectUri
) {
}
