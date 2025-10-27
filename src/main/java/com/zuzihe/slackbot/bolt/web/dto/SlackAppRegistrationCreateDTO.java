package com.zuzihe.slackbot.bolt.web.dto;

import aichatter.bolt.domain.SlackAppType;

public record SlackAppRegistrationCreateDTO(
        String clientId,
        String clientSecret,
        String signingSecret,
        SlackAppType slackAppType,
        String appId
) {
}
