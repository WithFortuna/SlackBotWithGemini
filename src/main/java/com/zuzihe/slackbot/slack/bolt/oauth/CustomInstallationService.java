package com.zuzihe.slackbot.slack.bolt.oauth;

import com.slack.api.bolt.model.Bot;
import com.slack.api.bolt.model.Installer;
import com.slack.api.bolt.service.InstallationService;
import com.zuzihe.slackbot.slack.workspace.entity.WorkspaceInstallation;
import com.zuzihe.slackbot.slack.workspace.service.WorkspaceInstallationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomInstallationService implements InstallationService {

    private final WorkspaceInstallationService workspaceInstallationService;

    @Override
    public boolean isHistoricalDataEnabled() {
        return false;
    }

    @Override
    public void setHistoricalDataEnabled(boolean isHistoricalDataEnabled) {
        // 현재는 히스토리컬 데이터 관리를 지원하지 않음
    }

    @Override
    public void saveInstallerAndBot(Installer installer) throws Exception {
        log.info("Slack 앱 설치 정보 저장 시작: teamId={}",
                installer.getTeamId());

        try {
            workspaceInstallationService.saveOrUpdateInstallation(
                    installer.getTeamId(),
                    null, // teamName - Installer 인터페이스에는 없음
                    installer.getBotAccessToken(),
                    installer.getBotUserId(),
                    installer.getBotScope(),
                    installer.getInstallerUserAccessToken(),
                    installer.getInstallerUserId(),
                    installer.getInstallerUserScope(),
                    installer.getInstallerUserId()
            );

            log.info("Slack 앱 설치 정보 저장 완료: teamId={}", installer.getTeamId());
        } catch (Exception e) {
            log.error("Slack 앱 설치 정보 저장 실패: teamId={}", installer.getTeamId(), e);
            throw new RuntimeException("설치 정보 저장 실패", e);
        }
    }

    @Override
    public void deleteBot(Bot bot) throws Exception {
        log.info("Slack 봇 정보 삭제 시작: teamId={}", bot.getTeamId());

        try {
            workspaceInstallationService.deleteInstallation(bot.getTeamId());
            log.info("Slack 봇 정보 삭제 완료: teamId={}", bot.getTeamId());
        } catch (Exception e) {
            log.error("Slack 봇 정보 삭제 실패: teamId={}", bot.getTeamId(), e);
            throw e;
        }
    }

    @Override
    public void deleteInstaller(Installer installer) throws Exception {
        log.info("Slack 설치자 정보 삭제 시작: teamId={}, userId={}", installer.getTeamId(), installer.getInstallerUserId());

        try {
            // 사용자별 권한만 삭제하는 로직이 필요하면 여기에 구현
            // 현재는 전체 설치 삭제로 처리
            workspaceInstallationService.deleteInstallation(installer.getTeamId());
            log.info("Slack 설치자 정보 삭제 완료: teamId={}", installer.getTeamId());
        } catch (Exception e) {
            log.error("Slack 설치자 정보 삭제 실패: teamId={}", installer.getTeamId(), e);
            throw e;
        }
    }

    @Override
    public void deleteAll(String enterpriseId, String teamId) {
        log.info("Slack 앱 전체 설치 정보 삭제 시작: enterpriseId={}, teamId={}", enterpriseId, teamId);

        try {
            workspaceInstallationService.deleteInstallation(teamId);
            log.info("Slack 앱 전체 설치 정보 삭제 완료: teamId={}", teamId);
        } catch (Exception e) {
            log.error("Slack 앱 전체 설치 정보 삭제 실패: teamId={}", teamId, e);
            throw e;
        }
    }

    @Override
    public Bot findBot(String enterpriseId, String teamId) {
        log.debug("Slack 봇 정보 조회: enterpriseId={}, teamId={}", enterpriseId, teamId);

        try {
            return workspaceInstallationService.findActiveInstallationByTeamId(teamId)
                    .map(this::convertToBot)
                    .orElse(null);
        } catch (Exception e) {
            log.error("Slack 봇 정보 조회 실패: teamId={}", teamId, e);
            return null;
        }
    }

    @Override
    public Installer findInstaller(String enterpriseId, String teamId, String userId) {
        log.debug("Slack 설치자 정보 조회: enterpriseId={}, teamId={}, userId={}",
                enterpriseId, teamId, userId);

        try {
            return workspaceInstallationService.findActiveInstallationByTeamId(teamId)
                    .map(this::convertToInstaller)
                    .orElse(null);
        } catch (Exception e) {
            log.error("Slack 설치자 정보 조회 실패: teamId={}", teamId, e);
            return null;
        }
    }

    private Bot convertToBot(WorkspaceInstallation workspace) {
        // Bot도 인터페이스이므로 익명 구현체로 생성
        return new Bot() {
            @Override
            public String getAppId() { return null; }
            @Override
            public void setAppId(String appId) {}

            @Override
            public String getEnterpriseId() { return workspace.getEnterpriseId(); }
            @Override
            public void setEnterpriseId(String enterpriseId) {}

            @Override
            public String getTeamId() { return workspace.getTeamId(); }
            @Override
            public void setTeamId(String teamId) {}

            @Override
            public Boolean getIsEnterpriseInstall() { return workspace.getIsEnterpriseInstall(); }
            @Override
            public void setIsEnterpriseInstall(Boolean isEnterpriseInstall) {}

            @Override
            public String getEnterpriseUrl() { return null; }
            @Override
            public void setEnterpriseUrl(String enterpriseUrl) {}

            @Override
            public String getTokenType() { return "bot"; }
            @Override
            public void setTokenType(String tokenType) {}

            @Override
            public String getBotId() { return null; }
            @Override
            public void setBotId(String botId) {}

            @Override
            public String getBotUserId() { return workspace.getBotUserId(); }
            @Override
            public void setBotUserId(String botUserId) {}

            @Override
            public String getBotScope() { return workspace.getBotScopes(); }
            @Override
            public void setBotScope(String scope) {}

            @Override
            public String getBotAccessToken() { return workspace.getBotToken(); }
            @Override
            public void setBotAccessToken(String botAccessToken) {}

            @Override
            public String getBotRefreshToken() { return null; }
            @Override
            public void setBotRefreshToken(String botRefreshToken) {}

            @Override
            public Long getBotTokenExpiresAt() { return null; }
            @Override
            public void setBotTokenExpiresAt(Long botTokenExpiresAt) {}

            @Override
            public Long getInstalledAt() {
                return workspace.getInstalledAt() != null ?
                    workspace.getInstalledAt().atZone(java.time.ZoneId.systemDefault()).toEpochSecond() : null;
            }
            @Override
            public void setInstalledAt(Long installedAt) {}
        };
    }

    private Installer convertToInstaller(WorkspaceInstallation workspace) {
        // Installer는 인터페이스이므로 구현체가 필요합니다.
        // Bolt SDK에서 제공하는 기본 구현체를 사용하거나 직접 구현해야 합니다.
        // 여기서는 간단한 구현체를 만들어 사용합니다.
        return new Installer() {
            @Override
            public String getAppId() { return null; }
            @Override
            public void setAppId(String appId) {}

            @Override
            public String getEnterpriseId() { return workspace.getEnterpriseId(); }
            @Override
            public void setEnterpriseId(String enterpriseId) {}

            @Override
            public String getTeamId() { return workspace.getTeamId(); }
            @Override
            public void setTeamId(String teamId) {}

            @Override
            public Boolean getIsEnterpriseInstall() { return workspace.getIsEnterpriseInstall(); }
            @Override
            public void setIsEnterpriseInstall(Boolean isEnterpriseInstall) {}

            @Override
            public String getEnterpriseUrl() { return null; }
            @Override
            public void setEnterpriseUrl(String enterpriseUrl) {}

            @Override
            public String getTokenType() { return "bot"; }
            @Override
            public void setTokenType(String tokenType) {}

            @Override
            public String getInstallerUserId() { return workspace.getInstallerUserId(); }
            @Override
            public void setInstallerUserId(String userId) {}

            @Override
            public String getInstallerUserScope() { return workspace.getUserScopes(); }
            @Override
            public void setInstallerUserScope(String scope) {}

            @Override
            public String getInstallerUserAccessToken() { return workspace.getUserToken(); }
            @Override
            public void setInstallerUserAccessToken(String userAccessToken) {}

            @Override
            public String getInstallerUserRefreshToken() { return null; }
            @Override
            public void setInstallerUserRefreshToken(String installerUserRefreshToken) {}

            @Override
            public Long getInstallerUserTokenExpiresAt() { return null; }
            @Override
            public void setInstallerUserTokenExpiresAt(Long installerUserTokenExpiresAt) {}

            @Override
            public String getBotId() { return null; }
            @Override
            public void setBotId(String botId) {}

            @Override
            public String getBotUserId() { return workspace.getBotUserId(); }
            @Override
            public void setBotUserId(String botUserId) {}

            @Override
            public String getBotScope() { return workspace.getBotScopes(); }
            @Override
            public void setBotScope(String scope) {}

            @Override
            public String getBotAccessToken() { return workspace.getBotToken(); }
            @Override
            public void setBotAccessToken(String botAccessToken) {}

            @Override
            public String getBotRefreshToken() { return null; }
            @Override
            public void setBotRefreshToken(String botRefreshToken) {}

            @Override
            public Long getBotTokenExpiresAt() { return null; }
            @Override
            public void setBotTokenExpiresAt(Long botTokenExpiresAt) {}

            @Override
            public Bot toBot() { return convertToBot(workspace); }

            @Override
            public String getIncomingWebhookUrl() { return null; }
            @Override
            public void setIncomingWebhookUrl(String incomingWebhookUrl) {}

            @Override
            public String getIncomingWebhookChannelId() { return null; }
            @Override
            public void setIncomingWebhookChannelId(String incomingWebhookChannelId) {}

            @Override
            public String getIncomingWebhookConfigurationUrl() { return null; }
            @Override
            public void setIncomingWebhookConfigurationUrl(String incomingWebhookConfigurationUrl) {}

            @Override
            public Long getInstalledAt() {
                return workspace.getInstalledAt() != null ?
                    workspace.getInstalledAt().atZone(java.time.ZoneId.systemDefault()).toEpochSecond() : null;
            }
            @Override
            public void setInstalledAt(Long installedAt) {}
        };
    }
}