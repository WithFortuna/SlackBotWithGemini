package com.zuzihe.slackbot.bolt.web.registration;

import aichatter.bolt.application.common.service.registration.SlackAppRegistrationService;
import aichatter.bolt.domain.SlackAppType;
import aichatter.bolt.web.dto.SlackAppRegistrationCreateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/slack/app-registration")
@RestController
public class SlackAppRegistrationController {

    private final SlackAppRegistrationService slackAppRegistrationService;

    /**
     * SlackAppType을 기준으로 앱 등록 여부를 확인하는 API
     */
    @GetMapping("/exists/{slackAppType}")
    public ResponseEntity<Map<String, Object>> checkAppRegistrationExists(@PathVariable SlackAppType slackAppType) {
        try {
            boolean exists = slackAppRegistrationService.existsBySlackAppType(slackAppType);

            return ResponseEntity.ok(Map.of(
                    "exists", exists,
                    "appType", slackAppType.name()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Invalid app type",
                            "message", "유효하지 않은 앱 타입입니다. AICHATTER 또는 DOCSBOT을 입력해주세요."
                    ));
        }
    }

    /**
     * Slack 앱 등록 API
     * 요청 바디로 받은 DTO를 기반으로 앱을 등록하고 등록된 엔티티 ID를 반환합니다.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerSlackApp(@RequestBody SlackAppRegistrationCreateDTO dto) {
        Long id = slackAppRegistrationService.registerSlackApp(dto);
        return ResponseEntity.ok(Map.of(
                "id", id,
                "status", "registered"
        ));
    }

    @DeleteMapping("/unregister/{slackAppType}")
    public ResponseEntity<Map<String, Object>> unregisterSlackApp(@PathVariable SlackAppType slackAppType) {
        slackAppRegistrationService.unregisterSlackApp(slackAppType);
        return ResponseEntity.ok(Map.of(
                "status", "deleted"
        ));
    }

}
