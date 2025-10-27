package com.zuzihe.slackbot.bolt.util;

import aichatter.bolt.config.CachedBodyHttpServletRequest;
import aichatter.bolt.config.dto.SlackUserInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static aichatter.config.SecurityConfig.LoggerUtil.logger;

public class SlackRequestParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String extractTeamId(CachedBodyHttpServletRequest wrappedRequest) throws IOException {
        String requestURI = wrappedRequest.getRequestURI();
        if (eventRequest(requestURI)) {
            return extractTeamIdFromEvent(wrappedRequest);
        } else if (interactiveRequest(requestURI)) {
            return extractTeamIdFromInteractive(wrappedRequest);
        } else {
            return null;
        }
    }

    @Nullable
    private static String extractTeamIdFromInteractive(CachedBodyHttpServletRequest wrappedRequest) throws IOException {
        // Request body를 읽어서 Map으로 파싱
        String requestBody = getRequestBody(wrappedRequest);
        Map<String, Object> requestMap = parseInteractivePayload(requestBody);

        String teamId = null;
        Map<String, Object> team = (Map<String, Object>) requestMap.get("team");
        if (team != null) {
            teamId = (String) team.get("id");
        }

        return teamId;
    }

    private static String extractTeamIdFromEvent(CachedBodyHttpServletRequest wrappedRequest) throws IOException {
        // Request body를 읽어서 Map으로 파싱
        String requestBody = getRequestBody(wrappedRequest);
        Map<String, Object> requestMap = parseEventPayload(requestBody);

        return (String) requestMap.get("team_id");
    }

    public static SlackUserInfo extractSlackUserInfo(CachedBodyHttpServletRequest wrappedRequest) throws IOException {
        // Request body를 읽어서 Map으로 파싱
        String requestURI = wrappedRequest.getRequestURI();
        if (eventRequest(requestURI)) {
            return extractUserInfoFromEvent(wrappedRequest);
        }
        else if (interactiveRequest(requestURI)) {
            return extractUserInfoFromInteractive(wrappedRequest);
        }
        else {
            return new SlackUserInfo(null, null, null);
        }
    }

    private static SlackUserInfo extractUserInfoFromEvent(CachedBodyHttpServletRequest wrappedRequest) throws IOException {
        // Request body를 읽어서 Map으로 파싱
        String requestURI = wrappedRequest.getRequestURI();
        String requestBody = getRequestBody(wrappedRequest);
        Map<String, Object> requestMap = parseEventPayload(requestBody);
        logger.info("[Slack Auth Filter]requestURI: {}, requestBody: {}", requestURI, requestBody);

        // .event.user 필드에서 사용자 ID 추출
        Map<String, Object> event = (Map<String, Object>) requestMap.get("event");

        String teamId = (String) requestMap.get("team_id");
        String slackUserId = (String) event.get("user");
        String appId = (String) requestMap.get("api_app_id");

        return new SlackUserInfo(teamId, slackUserId, appId);
    }

    /**
     * 요청 body를 읽어서 문자열로 반환
     */
    private static String getRequestBody(HttpServletRequest request) throws IOException {
        return new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    public static boolean isEventRequestByBotMyself(CachedBodyHttpServletRequest wrappedRequest) throws IOException {
        String requestURI = wrappedRequest.getRequestURI();
        if (eventRequest(requestURI)) {
            // Request body를 읽어서 Map으로 파싱
            String requestBody = getRequestBody(wrappedRequest);
            Map<String, Object> requestMap = parseEventPayload(requestBody);

            // .event.user 필드에서 사용자 ID 추출
            Map<String, Object> event = (Map<String, Object>) requestMap.get("event");
            return event.get("bot_id") != null && event.get("type").equals("message");
        } else {
            return false;
        }
    }

    private static SlackUserInfo extractUserInfoFromInteractive(CachedBodyHttpServletRequest wrappedRequest) throws IOException {
        // Request body를 읽어서 Map으로 파싱
        String requestURI = wrappedRequest.getRequestURI();
        String requestBody = getRequestBody(wrappedRequest);
        Map<String, Object> requestMap = parseInteractivePayload(requestBody);
        logger.info("[Slack Auth Filter]requestURI: {}, requestBody: {}", requestURI, requestBody);

        String teamId = null;
        Map<String, Object> team = (Map<String, Object>) requestMap.get("team");
        if (team != null) {
            teamId = (String) team.get("id");
        }

        String slackUserId = null;
        Map<String, Object> user = (Map<String, Object>) requestMap.get("user");
        if (user != null) {
            slackUserId = (String) user.get("id");
        }

        String appId = (String) requestMap.get("api_app_id");

        return new SlackUserInfo(teamId, slackUserId, appId);
    }


    private static boolean eventRequest(String requestURI) {
        return requestURI.startsWith("/api/slack/events");
    }

    private static boolean interactiveRequest(String requestURI) {
        return requestURI.startsWith("/api/slack/interactive");
    }

    private static Map<String, Object> parseEventPayload(String requestBody) throws JsonProcessingException {
        return objectMapper.readValue(requestBody, Map.class);
    }
    /**
     * 인터랙티브 요청의 payload 파라미터를 파싱
     * Form data 구조: payload={"team":{"id":"T123"},"user":{"id":"U123"},...}
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseInteractivePayload(String requestBody) throws JsonProcessingException {
        // URL encoded form data에서 payload 파라미터 추출
        String decodedBody = URLDecoder.decode(requestBody, StandardCharsets.UTF_8);

        // payload= 다음의 JSON 추출
        String payloadPrefix = "payload=";
        int payloadStart = decodedBody.indexOf(payloadPrefix);
        if (payloadStart == -1) {
            logger.warn("[SlackAuth] 인터랙티브 요청에 payload 파라미터가 없음");
            return null;
        }

        String jsonPayload = decodedBody.substring(payloadStart + payloadPrefix.length());

        // 다른 파라미터가 있는 경우 & 기준으로 분리
        int nextParamIndex = jsonPayload.indexOf("&");
        if (nextParamIndex != -1) {
            jsonPayload = jsonPayload.substring(0, nextParamIndex);
        }

        // JSON을 Map으로 파싱
        return objectMapper.readValue(jsonPayload, Map.class);
    }

}
