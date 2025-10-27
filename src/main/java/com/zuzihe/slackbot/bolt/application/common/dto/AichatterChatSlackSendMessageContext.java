package com.zuzihe.slackbot.bolt.application.common.dto;

import aichatter.bolt.application.aichatter.chat.AichatterChatSlackEventType;
import jakarta.annotation.Nullable;
import lombok.Getter;

@Getter
public class AichatterChatSlackSendMessageContext extends SlackSendMessageContext {
    private final AichatterChatSlackEventType slackEventType;

    private AichatterChatSlackSendMessageContext(String channel,
                                                 String threadTs,
                                                 @Nullable
                                                 String originalMessageTs,
                                                 AichatterChatSlackEventType slackEventType,
                                                 String botToken) {
        super(channel, threadTs, originalMessageTs, botToken);
        this.slackEventType = slackEventType;
    }

    public static AichatterChatSlackSendMessageContext forMessage(
            String channel,
            String threadTs,
            AichatterChatSlackEventType eventType,
            String botToken) {
        return new AichatterChatSlackSendMessageContext(channel, threadTs, null, eventType, botToken);
    }

    public static AichatterChatSlackSendMessageContext forMessageUpdate(
            String channel,
            String threadTs,
            String originalMessageTs,
            AichatterChatSlackEventType eventType,
            String botToken) {
        return new AichatterChatSlackSendMessageContext(channel, threadTs, originalMessageTs, eventType, botToken);
    }
}
