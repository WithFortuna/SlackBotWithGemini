package com.zuzihe.slackbot.bolt.application.common.service.user;

import aichatter.bolt.domain.SlackWorkspaceUser;
import aichatter.bolt.repository.SlackAppRegistrationRepository;
import aichatter.bolt.repository.SlackWorkspaceInstallationRepository;
import aichatter.bolt.repository.SlackWorkspaceUserRepository;
import aichatter.bolt.util.SlackInfoTokenProvider;
import aichatter.bolt.web.dto.SlackWorkspaceUserDTO;
import aichatter.bolt.web.dto.SlackWorkspaceUserRequestDTO;
import aichatter.config.JwtTokenProvider;
import aichatter.dto.account.request.AppPrincipalDTO;
import aichatter.entity.domain.Domain;
import aichatter.global.ErrorCode;
import aichatter.global.exception.CustomException;
import aichatter.repository.account.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SlackWorkspaceUserService {
    private final SlackWorkspaceUserRepository workspaceUserRepository;
    private final SlackAppRegistrationRepository appRegistrationRepository;
    private final SlackWorkspaceInstallationRepository workspaceInstallationRepository;
    private final AccountRepository accountRepository;
    private final SlackInfoTokenProvider slackInfoTokenProvider;
    private final JwtTokenProvider jwtTokenProvider;

    public SlackWorkspaceUserDTO getSlackWorkspaceUser(Long id) {
        SlackWorkspaceUser slackWorkspaceUser = workspaceUserRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SLACK_WORKSPACE_USER_NOT_FOUND));

        return new SlackWorkspaceUserDTO(slackWorkspaceUser);
    }
    public boolean isConnectedToAichatterAccount(String appId, String teamId, String slackUserId) {
        // 이벤트를 날린 슬랙 사용자_id + aichatter계정을 합친 정보가 테이블에 있는지 체크
        Optional<SlackWorkspaceUser> slackAccountOpt = workspaceUserRepository.findWorkspaceUserOnMultipleSlackApps(appId, teamId, slackUserId);

        if (slackAccountOpt.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public Optional<Long> getWorkspaceUserIddBySlackUser(String appId, String teamId, String slackUserId) {
        Optional<SlackWorkspaceUser> slackAccountOpt = workspaceUserRepository.findWorkspaceUserOnMultipleSlackApps(appId, teamId, slackUserId);
        return slackAccountOpt.map(SlackWorkspaceUser::getAccountId);
    }

    @Transactional
    public void connectWorkspaceUserToAichatter(SlackWorkspaceUserRequestDTO dto, Domain domain) {
//        AppPrincipalDTO principal = authenticateUserAndGetPrincipal(slackAccountRequestDTO, domain);
        AppPrincipalDTO principal = extractPrincipalFromAccessToken(dto.getLoginResultTokens());
        Map<String, Object> slackAccountInfo = getWorkspaceUserInfo(dto.getSlackInfoToken());

        workspaceUserRepository.save(createConnection(domain, slackAccountInfo, principal));
    }

    private SlackWorkspaceUser createConnection(Domain domain, Map<String, Object> slackAccountInfo, AppPrincipalDTO principal) {
        String appId = (String) (slackAccountInfo.get(SlackInfoTokenProvider.APP_ID));
        String teamId = (String) slackAccountInfo.get(SlackInfoTokenProvider.TEAM_ID);
        String slackUserId = (String) slackAccountInfo.get(SlackInfoTokenProvider.SLACK_USER_ID);
        Long slackAppRegistrationId = appRegistrationRepository.findByAppId(appId)
                .orElseThrow(() -> new CustomException(ErrorCode.SLACK_APP_NOT_REGISTERED))
                .getId();
        Long slackWorkspaceInstallationId = workspaceInstallationRepository.findByTeamIdAndSlackAppRegistrationId(teamId, slackAppRegistrationId)
                .orElseThrow(() -> new CustomException(ErrorCode.SLACK_WORKSPACE_NOT_INSTALLED))
                .getId();

        return SlackWorkspaceUser.builder()
                .appId(appId)
                .slackAppRegistrationId(slackAppRegistrationId)
                .teamId(teamId)
                .slackWorkspaceInstallationId(slackWorkspaceInstallationId)
                .slackUserId(slackUserId)
                .accountId(principal.accountId())
                .domainId(domain.getId())
                .userType(getAichatterUserType(principal))
                .regDate(OffsetDateTime.now())
                .modDate(OffsetDateTime.now())
                .build();
    }

    private Map<String, Object> getWorkspaceUserInfo(String slackInfoToken) {
        Map<String, Object> slackAccountInfo;
        try {
            slackAccountInfo = slackInfoTokenProvider.parseState(slackInfoToken);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.err.println("=== JWT 만료 디버깅 정보 ===");
            System.err.println("현재 시간: " + new java.util.Date());
            System.err.println("발급 시간: " + e.getClaims().getIssuedAt());
            System.err.println("만료 시간: " + e.getClaims().getExpiration());
            System.err.println("서버 현재 밀리초: " + System.currentTimeMillis());
            throw new CustomException(ErrorCode.SLACK_INFO_TOKEN_EXPIRED);
        }
        return slackAccountInfo;
    }

    private String getAichatterUserType(AppPrincipalDTO principal) {
        return accountRepository.findById(principal.accountId())
                .orElseThrow()
                .getUserType();
    }

/*    private AppPrincipalDTO authenticateUserAndGetPrincipal(SlackAccountRequestDTO slackAccountRequestDTO, Domain domain) {
        Map<String, String> tokens = new HashMap<>();
        tokens = authService.authenticateUser(slackAccountRequestDTO.getLoginRequest(), domain.getDomainAddress()) ;

        if (!tokens.get("status").equals("success")) {
            throw new CustomException(ErrorCode.FAILED_TO_CONNECT_SLACK_USER);
        }
        AppPrincipalDTO principal = (AppPrincipalDTO) extractPrincipalFromAccessToken(tokens);
        return principal;
    }*/

    private AppPrincipalDTO extractPrincipalFromAccessToken(Map<String, String> tokens) {
        return (AppPrincipalDTO)jwtTokenProvider.getAuthentication(tokens.get("accessToken"))
                .getPrincipal();
    }

    public SlackWorkspaceUserDTO getSlackWorkspaceUser(String appId, String teamId, String slackUserId) {
        SlackWorkspaceUser slackWorkspaceUser = workspaceUserRepository
                .findWorkspaceUserOnMultipleSlackApps(appId, teamId, slackUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.SLACK_WORKSPACE_USER_NOT_FOUND));
        return new SlackWorkspaceUserDTO(slackWorkspaceUser);
    }
}
