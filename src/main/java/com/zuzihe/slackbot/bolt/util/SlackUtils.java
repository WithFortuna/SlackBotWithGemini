package com.zuzihe.slackbot.bolt.util;

import aichatter.bolt.domain.SlackAppType;
import aichatter.bolt.repository.SlackAppRegistrationRepository;
import aichatter.global.ErrorCode;
import aichatter.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SlackUtils {
    private final SlackAppRegistrationRepository appRegistrationRepository;

    public Long getAichatterChatAppRegistrationId() {
        return appRegistrationRepository.findBySlackAppType(SlackAppType.AICHATTER_CHAT)
                .orElseThrow(() -> new CustomException(ErrorCode.SLACK_APP_NOT_REGISTERED))
                .getId();
    }

    public Long getDocsbotAppRegistrationId() {
        return appRegistrationRepository.findBySlackAppType(SlackAppType.DOCSBOT)
                .orElseThrow(() -> new CustomException(ErrorCode.SLACK_APP_NOT_REGISTERED))
                .getId();
    }

}
