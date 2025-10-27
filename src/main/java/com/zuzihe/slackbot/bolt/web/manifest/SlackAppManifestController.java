package com.zuzihe.slackbot.bolt.web.manifest;

import aichatter.bolt.application.common.service.manifest.SlackAppManifestService;
import aichatter.bolt.domain.SlackAppType;
import aichatter.config.SecurityConfig;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static aichatter.utils.RequestUtils.getBaseUrl;

@RestController
@RequestMapping("/api/slack")
@RequiredArgsConstructor
public class SlackAppManifestController {
    private final SlackAppManifestService manifestService;

    @GetMapping("/config-template/{slackAppType}")
    public ResponseEntity<JsonNode> createManifest(HttpServletRequest request,
                                                   @PathVariable SlackAppType slackAppType) throws IOException {
        // 요청 URL 기반 baseUrl 추출
        String baseUrl = getBaseUrl(request);
        SecurityConfig.LoggerUtil.logger.info("[SlackManifestController] 요청자 baseUrl:{}", baseUrl);
        JsonNode json = manifestService.createManifestByAppType(baseUrl, slackAppType);
        return ResponseEntity.ok(json);
    }


}

