package com.zuzihe.slackbot.bolt.application.common.dto;

import aichatter.bolt.application.docsbot.DocsbotSlackEventType;
import jakarta.annotation.Nullable;
import lombok.Getter;

@Getter
public class DocsbotSlackSendMessageContext extends SlackSendMessageContext {
    private final DocsbotSlackEventType slackEventType;

    private DocsbotSlackSendMessageContext(String channel,
                                           String threadTs,
                                           @Nullable
                                           String originalMessageTs,
                                           DocsbotSlackEventType slackEventType,
                                           String botToken) {
        super(channel, threadTs, originalMessageTs, botToken);
        this.slackEventType = slackEventType;
    }

    public static DocsbotSlackSendMessageContext forMessage(
            String channel,
            String threadTs,
            DocsbotSlackEventType eventType,
            String botToken) {
        return new DocsbotSlackSendMessageContext(channel, threadTs, null, eventType, botToken);
    }

    public static DocsbotSlackSendMessageContext forMessageUpdate(
            String channel,
            String threadTs,
            String originalMessageTs,
            DocsbotSlackEventType eventType,
            String botToken) {
        return new DocsbotSlackSendMessageContext(channel, threadTs, originalMessageTs, eventType, botToken);
    }
}
