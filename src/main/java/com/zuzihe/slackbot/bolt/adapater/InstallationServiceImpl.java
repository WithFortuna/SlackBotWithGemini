package com.zuzihe.slackbot.bolt.adapater;

import aichatter.bolt.application.common.service.install.SlackWorkspaceInstallationService;
import com.slack.api.bolt.model.Bot;
import com.slack.api.bolt.model.Installer;
import com.slack.api.bolt.service.InstallationService;
import com.slack.api.model.block.LayoutBlock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class InstallationServiceImpl implements InstallationService {
    private final SlackWorkspaceInstallationService slackWorkspaceInstallationService;

    @Override
    public void saveBot(Bot bot) throws Exception {
//        new SlackWorkspaceInstallationCreateDTO();
    }

    @Override
    public String getInstallationGuideText(String enterpriseId, String teamId, String userId) {
        return InstallationService.super.getInstallationGuideText(enterpriseId, teamId, userId);
    }

    @Override
    public List<LayoutBlock> getInstallationGuideBlocks(String enterpriseId, String teamId, String userId) {
        return InstallationService.super.getInstallationGuideBlocks(enterpriseId, teamId, userId);
    }

    @Override
    public void deleteAll(String enterpriseId, String teamId) {
        InstallationService.super.deleteAll(enterpriseId, teamId);
    }

    @Override
    public boolean isHistoricalDataEnabled() {
        return false;
    }

    @Override
    public void setHistoricalDataEnabled(boolean isHistoricalDataEnabled) {

    }

    @Override
    public void saveInstallerAndBot(Installer installer) throws Exception {

    }

    @Override
    public void deleteBot(Bot bot) throws Exception {

    }

    @Override
    public void deleteInstaller(Installer installer) throws Exception {

    }

    @Override
    public Bot findBot(String enterpriseId, String teamId) {
        return null;
    }

    @Override
    public Installer findInstaller(String enterpriseId, String teamId, String userId) {
        return null;
    }
}
