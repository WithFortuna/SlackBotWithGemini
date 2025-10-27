package com.zuzihe.slackbot.bolt.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public class SlackWorkspaceUserRequestDTO {
    private String slackInfoToken;
    private Map<String, String> loginResultTokens;  // /api/login호출 후 응답으로 받은 토큰
}
