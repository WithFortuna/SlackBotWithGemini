package com.zuzihe.slackbot.bolt.application.common.dto;

import jakarta.annotation.Nullable;
import lombok.Getter;

@Getter
public abstract class SlackSendMessageContext {
    private final String channel;
    private final String threadTs;
    @Nullable
    private final String originalMessageTs;
    private final String botToken;

    protected SlackSendMessageContext(String channel,
                                      String threadTs,
                                      @Nullable
                                      String originalMessageTs,
                                      String botToken) {
        this.channel = channel;
        this.threadTs = threadTs;
        this.originalMessageTs = originalMessageTs;
        this.botToken = botToken;
    }
}
