package com.zuzihe.slackbot.bolt.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Slack OAuth 2.0 인증 응답 DTO
 *
 * Slack의 oauth.v2.access API 응답을 매핑하는 클래스입니다.
 * 사용자가 Slack 앱 설치를 승인한 후, 인증 코드를 access token으로 교환할 때 받는 응답입니다.
 *
 * @see <a href="https://api.slack.com/methods/oauth.v2.access">Slack API: oauth.v2.access</a>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)  // Slack API 응답의 알려지지 않은 필드 무시
public class SlackOAuthResponse {

    /**
     * API 호출 성공 여부
     * true: 인증 성공, false: 인증 실패
     */
    private boolean ok;

    /**
     * Slack 앱 ID
     * 등록된 slack app 식별자(설치된 app이 여러개여도 모두 동일)
     */
    @JsonProperty("app_id")
    private String appId;

    /**
     * 인증된 사용자 정보
     * 앱을 설치한 Slack 사용자의 ID를 포함
     */
    @JsonProperty("authed_user")
    private AuthedUser authedUser;

    /**
     * 승인된 권한 범위 (scopes)
     * 쉼표로 구분된 권한 목록 (예: "chat:write,channels:read")
     */
    private String scope;

    /**
     * 토큰 타입
     * 일반적으로 "bot"
     */
    @JsonProperty("token_type")
    private String tokenType;

    /**
     * 봇 토큰(설치된 workspace마다 다르므로 worksapce 식별 가능)
     * Bot User OAuth Access Token
     * Slack API 호출 시 사용하는 인증 토큰 (예: "xoxb-...")
     * 이 토큰으로 메시지 전송, 채널 조회 등의 API를 호출할 수 있습니다.
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * Bot 사용자 ID
     * 설치된 봇의 Slack 사용자 ID (예: "U1234567890")
     */
    @JsonProperty("bot_user_id")
    private String botUserId;

    /**
     * 앱이 설치된 Slack 팀(워크스페이스) 정보
     */
    private Team team;

    /**
     * Enterprise Grid 조직 정보 (해당하는 경우)
     * Enterprise Grid를 사용하는 조직에서만 존재
     */
    private Enterprise enterprise;

    /**
     * Enterprise 전체 설치 여부
     * true: Enterprise Grid 전체에 설치됨, false: 개별 워크스페이스에만 설치됨
     */
    @JsonProperty("is_enterprise_install")
    private boolean isEnterpriseInstall;

    private String error;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AuthedUser {
        /**
         * 사용자 ID
         * 앱을 설치한 Slack 사용자의 고유 ID (예: "U1234567890")
         */
        private String id;
    }

    /**
     * Slack 팀(워크스페이스) 정보
     * 앱이 설치된 Slack 워크스페이스의 정보를 담는 내부 클래스
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Team {
        /**
         * 팀(워크스페이스) ID
         * Slack 워크스페이스의 고유 ID (예: "T1234567890")
         */
        private String id;

        /**
         * 팀(워크스페이스) 이름
         * 사용자가 지정한 워크스페이스 이름 (예: "My Company")
         */
        private String name;
    }

    /**
     * Enterprise Grid 조직 정보
     * Enterprise Grid 환경에서 앱이 설치된 조직의 정보를 담는 내부 클래스
     * Enterprise Grid를 사용하지 않는 경우 null
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Enterprise {
        /**
         * Enterprise 조직 ID
         * Enterprise Grid 조직의 고유 ID (예: "E1234567890")
         */
        private String id;

        /**
         * Enterprise 조직 이름
         * Enterprise Grid 조직의 이름
         */
        private String name;
    }
}