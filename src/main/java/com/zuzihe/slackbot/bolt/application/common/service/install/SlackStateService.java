package com.zuzihe.slackbot.bolt.application.common.service.install;


import aichatter.bolt.domain.SlackAppType;
import aichatter.global.ErrorCode;
import aichatter.global.exception.CustomException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SlackStateService {
    private final Map<String, StateDetail> cache = new ConcurrentHashMap<>(); // 데모 TODO: DB에 넣기
    private final long TTL_MS = 10 * 60 * 1000;

    public String issue(SlackAppType appType) {
        byte[] r = new byte[32];
        new SecureRandom().nextBytes(r);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(r);
        cache.put(state, new StateDetail(System.currentTimeMillis() + TTL_MS, appType));
        log.info("state 발급했음. appType: {}", appType);
        return state;
    }

    public SlackAppType verifyAndConsume(String state) {
        StateDetail stateDetail = cache.remove(state);
        Long exp = stateDetail.getExpiration();
        if (exp == null || exp < System.currentTimeMillis()) {
            throw new CustomException(ErrorCode.SLACK_STATE_EXPIRED);
        }

        return stateDetail.getSlackAppType();
    }

    @Getter
    static class StateDetail {
        Long expiration; // ms
        SlackAppType  slackAppType;

        public StateDetail(Long expiration, SlackAppType slackAppType) {
            this.expiration = expiration;
            this.slackAppType = slackAppType;
        }
    }
}
