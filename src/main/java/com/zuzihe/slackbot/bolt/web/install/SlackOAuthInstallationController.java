package com.zuzihe.slackbot.bolt.web.install;
import aichatter.bolt.application.common.service.install.SlackOAuthInstallationService;
import aichatter.bolt.application.common.service.install.SlackStateService;
import aichatter.bolt.domain.SlackAppType;
import aichatter.global.exception.CustomException;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static aichatter.utils.RequestUtils.getBaseUrl;

/**
 * Slack App 설치에 사용
 */
@Slf4j
@RestController
@RequestMapping("/api/slack")
@RequiredArgsConstructor
public class SlackOAuthInstallationController {
    private final SlackOAuthInstallationService slackOAuthInstallationService;
    private final SlackStateService slackStateService;

    // 사용자가 앱 설치를 시작하면 Slack 인증 페이지로 리다이렉트
    @GetMapping("/oauth/install/{slackAppType}")
    public ResponseEntity<Void> start(HttpServletRequest request, @PathVariable SlackAppType slackAppType) {
        String installRedirectUrl = slackOAuthInstallationService.getInstallUrl(slackStateService.issue(slackAppType), getBaseUrl(request), slackAppType);

        return ResponseEntity
                .status(302)
                .location(URI.create(installRedirectUrl))
                .build();
    }

    /* Slack OAuth 인증 완료 후 콜백을 받는 엔드포인트
    Slack에서 code, state 파라미터를 담아 호출함
    */
    @GetMapping("/oauth/callback")
    public ResponseEntity<String> handleSlackOAuthCallback(
            HttpServletRequest request,
            @RequestParam String code,
            @RequestParam(required = false) String state) throws JsonProcessingException {
        try {
            String body = slackOAuthInstallationService.handleCallback(code, state, getBaseUrl(request));
            return ResponseEntity.ok(body);
        } catch (CustomException e) {
            String body = e.getMessage();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(body);
        }
    }

}
