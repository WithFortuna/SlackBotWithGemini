package com.zuzihe.slackbot.bolt.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * WorkspaceSlackUser를 aichatter 계정 연동에 사용되는 토큰 생성자
 */
@Component
public class SlackInfoTokenProvider {

    @Value("${jwt.secret.key}")
    private String SECRET_KEY;
    public static String APP_ID = "appId";
    public static String TEAM_ID = "teamId";
    public static String SLACK_USER_ID = "slackUserId";

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public String generate(String appId, String teamId, String slackUserId) {
        long now = System.currentTimeMillis();
        long expirationMs = 1000 * 60 * 30; // 30분 유효

        return Jwts.builder()
                .setSubject("slack_state")
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .addClaims(Map.of(APP_ID, appId, TEAM_ID, teamId, SLACK_USER_ID, slackUserId))
                .signWith(getSigningKey())
                .compact();
    }

    // state 풀기
    public Map<String, Object> parseState(String state) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(state)
                .getBody();
    }
}

