package com.zuzihe.slackbot.slack.bolt.oauth;

import com.slack.api.bolt.service.OAuthStateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class CustomOAuthStateService implements OAuthStateService {

    private final ConcurrentHashMap<String, Long> stateStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // state 만료 시간 (10분)
    private static final long STATE_EXPIRY_MINUTES = 10;

    public CustomOAuthStateService() {
        // 주기적으로 만료된 state 정리 (1분마다 실행)
        scheduler.scheduleAtFixedRate(this::cleanupExpiredStates, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void addNewStateToDatastore(String state) throws Exception {
        long expiryTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(STATE_EXPIRY_MINUTES);
        stateStore.put(state, expiryTime);
        log.debug("새 OAuth state 데이터스토어에 추가: {}", state.substring(0, 8) + "...");
    }

    @Override
    public boolean isAvailableInDatabase(String state) {
        if (state == null || state.trim().isEmpty()) {
            log.warn("유효하지 않은 state: null 또는 빈 문자열");
            return false;
        }

        Long expiryTime = stateStore.get(state);
        if (expiryTime == null) {
            log.warn("존재하지 않는 OAuth state: {}", state.substring(0, 8) + "...");
            return false;
        }

        if (System.currentTimeMillis() > expiryTime) {
            log.warn("만료된 OAuth state: {}", state.substring(0, 8) + "...");
            stateStore.remove(state);
            return false;
        }

        log.debug("유효한 OAuth state 확인: {}", state.substring(0, 8) + "...");
        return true;
    }

    @Override
    public void deleteStateFromDatastore(String state) throws Exception {
        if (state != null) {
            stateStore.remove(state);
            log.debug("OAuth state 데이터스토어에서 삭제: {}", state.substring(0, 8) + "...");
        }
    }

    private String generateSecureRandomString(int length) {
        StringBuilder sb = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }

        return sb.toString();
    }

    private void cleanupExpiredStates() {
        long currentTime = System.currentTimeMillis();
        AtomicInteger removedCount = new AtomicInteger(0);

        stateStore.entrySet().removeIf(entry -> {
            if (currentTime > entry.getValue()) {
                removedCount.incrementAndGet();
                return true;
            }
            return false;
        });

        if (removedCount.get() > 0) {
            log.debug("만료된 OAuth state {} 개 정리 완료", removedCount.get());
        }
    }

    public int getActiveStateCount() {
        return stateStore.size();
    }
}