package com.zuzihe.slackbot.bolt.application.aichatter.chat;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum AichatterChatSlackEventType {
    MESSAGE("message"),
    APP_HOME_OPENED("app_home_opened"),
    APP_MENTION("app_mention"),
    ASSISTANT_THREAD_STARTED("assistant_thread_started"),
    ;
    private final String value;

    public static boolean isRegisteredEventType(String eventType) {
        return Arrays.stream(AichatterChatSlackEventType.values())
                .anyMatch(t -> t.value.equals(eventType));

    }
}
