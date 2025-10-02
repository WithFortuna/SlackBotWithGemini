package com.zuzihe.slackbot.slack.bolt.config;

import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.zuzihe.slackbot.slack.bolt.oauth.CustomInstallationService;
import com.zuzihe.slackbot.slack.bolt.oauth.CustomOAuthStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SlackBoltAppConfig {

    private final CustomInstallationService customInstallationService;
    private final CustomOAuthStateService customOAuthStateService;

    @Value("${slack.signing-secret:}")
    private String signingSecret;

    @Value("${slack.botToken:}")
    private String botToken;    // workspace에 종속적이므로 workspace 구분가능

    @Value("${slack.client-id:}")
    private String clientId;

    @Value("${slack.client-secret:}")
    private String clientSecret;

    @Value("${slack.redirect-uri:}")
    private String redirectUri;

    @Value("${slack.scopes:commands,chat:write}")
    private String scopes;

    @Bean
    public App slackApp() {
        AppConfig config = AppConfig.builder()
                .signingSecret(signingSecret)
                .singleTeamBotToken(botToken)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(redirectUri)
                .scope(scopes)
                // OAuth 경로 설정
                .oauthInstallPath("/install")  // OAuth 시작 경로: /slack/oauth/install
                .oauthRedirectUriPath("/callback")  // OAuth 콜백 경로: /slack/oauth/callback
                .build();

        // Only build and expose App; handlers are registered by separate registrars
        App app = new App(config);

        // OAuth 기능 활성화 및 커스텀 서비스 설정
        app.asOAuthApp(true);
        app.service(customInstallationService);
        app.service(customOAuthStateService);

        return app;
    }
}
