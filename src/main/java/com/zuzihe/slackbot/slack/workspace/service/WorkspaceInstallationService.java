package com.zuzihe.slackbot.slack.workspace.service;

import com.zuzihe.slackbot.slack.workspace.entity.WorkspaceInstallation;
import com.zuzihe.slackbot.slack.workspace.repository.WorkspaceInstallationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceInstallationService {

    private final WorkspaceInstallationRepository repository;

    @Transactional
    public WorkspaceInstallation saveInstallation(WorkspaceInstallation installation) {
        log.info("워크스페이스 설치 정보 저장: teamId={}, teamName={}",
                installation.getTeamId(), installation.getTeamName());
        return repository.save(installation);
    }

    @Transactional
    public WorkspaceInstallation saveOrUpdateInstallation(String teamId, String teamName, String botToken,
                                                          String botUserId, String botScopes, String userToken,
                                                          String userId, String userScopes, String installerUserId) {
        Optional<WorkspaceInstallation> existing = repository.findByTeamId(teamId);

        WorkspaceInstallation installation;
        if (existing.isPresent()) {
            installation = existing.get();
            installation.setBotToken(botToken);
            installation.setBotUserId(botUserId);
            installation.setBotScopes(botScopes);
            installation.setUserToken(userToken);
            installation.setUserId(userId);
            installation.setUserScopes(userScopes);
            installation.setInstallerUserId(installerUserId);
            installation.setIsActive(true);
            log.info("기존 워크스페이스 설치 정보 업데이트: teamId={}", teamId);
        } else {
            installation = new WorkspaceInstallation();
            installation.setTeamId(teamId);
            installation.setTeamName(teamName);
            installation.setBotToken(botToken);
            installation.setBotUserId(botUserId);
            installation.setBotScopes(botScopes);
            installation.setUserToken(userToken);
            installation.setUserId(userId);
            installation.setUserScopes(userScopes);
            installation.setInstallerUserId(installerUserId);
            installation.setIsActive(true);
            log.info("새 워크스페이스 설치 정보 생성: teamId={}", teamId);
        }

        return repository.save(installation);
    }

    public Optional<WorkspaceInstallation> findByTeamId(String teamId) {
        return repository.findByTeamId(teamId);
    }

    public Optional<WorkspaceInstallation> findActiveInstallationByTeamId(String teamId) {
        return repository.findByTeamIdAndIsActiveTrue(teamId);
    }

    public Optional<String> getBotTokenByTeamId(String teamId) {
        return repository.findByTeamIdAndIsActiveTrue(teamId)
                .map(WorkspaceInstallation::getBotToken);
    }

    @Transactional
    public void deactivateInstallation(String teamId) {
        repository.findByTeamId(teamId).ifPresent(installation -> {
            installation.setIsActive(false);
            repository.save(installation);
            log.info("워크스페이스 설치 비활성화: teamId={}", teamId);
        });
    }

    @Transactional
    public void deleteInstallation(String teamId) {
        repository.deleteByTeamId(teamId);
        log.info("워크스페이스 설치 정보 삭제: teamId={}", teamId);
    }
}