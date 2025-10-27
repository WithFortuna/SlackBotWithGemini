package com.zuzihe.slackbot.bolt.config;

import aichatter.bolt.application.common.service.user.SlackWorkspaceUserService;
import aichatter.bolt.config.dto.SlackUserInfo;
import aichatter.bolt.web.dto.SlackWorkspaceUserDTO;
import aichatter.config.JwtTokenProvider;
import aichatter.dto.account.request.AppPrincipalDTO;
import aichatter.global.ErrorCode;
import aichatter.global.exception.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static aichatter.bolt.util.SlackRequestParser.extractSlackUserInfo;
import static aichatter.bolt.util.SlackRequestParser.isEventRequestByBotMyself;


/**
 * 슬랙 요청에 대한 인증 필터
 * /api/slack/** 경로로 들어오는 요청을 Map<String, Object>로 파싱하여
 * 이벤트와 인터랙티브 요청에 따라 사용자 정보를 추출하고 계정 연동을 확인합니다.
 */
@Slf4j
@RequiredArgsConstructor
public class SlackAuthenticationFilter extends OncePerRequestFilter {

    private final SlackWorkspaceUserService slackWorkspaceUserService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();

        return !(requestURI.startsWith("/api/slack"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Request body를 여러 번 읽을 수 있도록 래핑
        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);

        try {
            SlackUserInfo slackUserInfo = extractSlackUserInfo(wrappedRequest);
            if (isEventRequestByBotMyself(wrappedRequest)) {
                filterChain.doFilter(wrappedRequest, response);
            }

            if (requestFromUser(slackUserInfo)) {
                checkConnectedToAccountAndSetAuthentication(slackUserInfo.appId(), slackUserInfo.teamId(), slackUserInfo.slackUserId());
            } else {
                log.warn("[SlackAuth] 슬랙 정보 추출 실패 - TeamId: {}, SlackUserId: {}", slackUserInfo.teamId(), slackUserInfo.slackUserId());
            }
        } catch (Exception e) {
            log.error("[SlackAuth] 슬랙 인증 처리 중 오류 {}", e.getMessage(), e);
            e.printStackTrace();
        }

        filterChain.doFilter(wrappedRequest, response);
    }

    private static boolean requestFromUser(SlackUserInfo slackUserInfo) {
        return slackUserInfo.teamId() != null && slackUserInfo.slackUserId() != null;
    }    /**
     * 계정 연동 검증 및 SecurityContext에 인증 정보 설정
     */
    private void checkConnectedToAccountAndSetAuthentication(String appId, String teamId, String slackUserId) {
        try {
            checkSlackConnectedToAccount(appId, teamId, slackUserId);

            SlackWorkspaceUserDTO workspaceUser = slackWorkspaceUserService.getSlackWorkspaceUser(appId, teamId, slackUserId);

            // SecurityContext에 인증 정보 설정
            SecurityContextHolder.getContext().setAuthentication(makeAuthentication(workspaceUser.id(), workspaceUser.domainId(), workspaceUser.userType()));
        } catch (CustomException e) {
            if (e.getErrorCode() == ErrorCode.SLACK_USER_NOT_CONNECTED_TO_ACCOUNT) {   // Slack유저가 계정과 연동되어 있지 않는 것은 정상 흐름이다
                log.info("[SlackAuth] 미연동 유저 오류 정상흐름 처리");
                return;
            }
            throw e;
        } catch (Exception e) {
            log.error("[SlackAuth] 인증 처리 중 오류 - TeamId: {}, SlackUserId: {}", teamId, slackUserId, e);
            throw e;
        }
    }

    @NotNull
    private UsernamePasswordAuthenticationToken makeAuthentication(Long accountId, Long domainId, String userType) {
        // AppPrincipalDTO 생성
        AppPrincipalDTO appPrincipalDTO = new AppPrincipalDTO(
                accountId,
                domainId,
                userType
        );

        // Authentication 객체 생성
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                appPrincipalDTO,
                null,
                List.of(new SimpleGrantedAuthority(jwtTokenProvider.getRole(userType)))
        );
        return authentication;
    }

    private void checkSlackConnectedToAccount(String appId, String teamId, String slackUserId) {
        Optional<Long> workspaceUserIdOpt = slackWorkspaceUserService.getWorkspaceUserIddBySlackUser(appId, teamId, slackUserId);
        if (workspaceUserIdOpt.isEmpty()) {
            log.warn("[SlackAuth] 연동되지 않은 슬랙 사용자부터 요청 발생 - TeamId: {}, SlackUserId: {}", teamId, slackUserId);
            throw new CustomException(ErrorCode.SLACK_USER_NOT_CONNECTED_TO_ACCOUNT);
        } else {
            log.debug("[SlackAuth] 슬랙 사용자 인증 성공 - TeamId: {}, SlackUserId: {}, AccountId: {}",
                    teamId, slackUserId, workspaceUserIdOpt.get());
        }
    }
}
