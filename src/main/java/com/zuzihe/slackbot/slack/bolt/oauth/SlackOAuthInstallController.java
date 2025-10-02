package com.zuzihe.slackbot.slack.bolt.oauth;

import com.slack.api.bolt.App;
import com.slack.api.bolt.jakarta_servlet.SlackOAuthAppServlet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Slack OAuth 설치 시작 및 콜백 처리를 위한 Servlet 설정
 * Bolt SDK의 SlackOAuthAppServlet을 사용하여 OAuth 플로우 처리
 *
 * 엔드포인트:
 * - GET /slack/oauth/install: OAuth 설치 시작 (Slack 인증 페이지로 리다이렉트)
 * - GET /slack/oauth/callback: OAuth 콜백 처리 (인가 코드를 액세스 토큰으로 교환하고 저장)
 */
@Slf4j
@Configuration
public class SlackOAuthInstallController {

    @Bean
    public ServletRegistrationBean<SlackOAuthAppServlet> slackOAuthServlet(@Autowired App app) {
        log.info("Slack OAuth Servlet 등록 중: /slack/oauth/*");

        SlackOAuthAppServlet servlet = new SlackOAuthAppServlet(app);
        ServletRegistrationBean<SlackOAuthAppServlet> registrationBean =
                new ServletRegistrationBean<>(servlet, "/slack/oauth/*");

        registrationBean.setName("SlackOAuthAppServlet");
        registrationBean.setLoadOnStartup(1);

        log.info("Slack OAuth Servlet 등록 완료");
        return registrationBean;
    }
}
