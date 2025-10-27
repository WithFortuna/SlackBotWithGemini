package com.zuzihe.slackbot.bolt.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SlackAppType {
    AICHATTER_CHAT("일반 채팅용 슬랙앱"),
    DOCSBOT("문서봇 용 슬랙 앱"),
    ;

    private final String description;
}
