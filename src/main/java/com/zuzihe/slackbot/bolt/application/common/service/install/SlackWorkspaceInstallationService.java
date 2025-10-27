package com.zuzihe.slackbot.bolt.application.common.service.install;

import aichatter.bolt.domain.SlackWorkspaceInstallation;
import aichatter.bolt.repository.SlackWorkspaceInstallationRepository;
import aichatter.bolt.web.dto.SlackWorkspaceInstallationCreateDTO;
import aichatter.global.ErrorCode;
import aichatter.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

import static aichatter.config.SecurityConfig.LoggerUtil.logger;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlackWorkspaceInstallationService {
    private final SlackWorkspaceInstallationRepository repository;

    @Transactional
    public SlackWorkspaceInstallation saveInstallation(SlackWorkspaceInstallationCreateDTO dto) {
        Optional<SlackWorkspaceInstallation> existing = repository.findByTeamIdAndSlackAppRegistrationId(
                dto.teamId(),
                dto.appRegistrationId()
        );

        SlackWorkspaceInstallation installation;
        if (existing.isPresent()) {
            logger.warn("중복 설치 요청된 워크스페이스 정보: teamId={}", dto.teamId());
            throw new CustomException(ErrorCode.SLACK_WORKSPACE_ALREADY_EXISTS);
        } else {
            installation = new SlackWorkspaceInstallation(
                    dto.appRegistrationId(),
                    dto.teamId(),
                    dto.teamName(),
                    dto.botToken(),
                    dto.botUserId(),
                    dto.botScopes(),
                    dto.installerUserId(),
                    dto.isEnterpriseInstall(),
                    OffsetDateTime.now()
            );
            logger.info("새 워크스페이스 설치 정보 생성: teamId={}", dto.teamId());
        }

        return repository.save(installation);
    }

    public Optional<SlackWorkspaceInstallation> findActiveInstallationByTeamId(String teamId) {
        return repository.findByTeamIdAndIsActiveTrue(teamId);
    }

    public Optional<String> getBotToken(String teamId, Long slackAppRegistrationId) {
        return repository.findByTeamIdAndSlackAppRegistrationId(teamId, slackAppRegistrationId)
                .map(SlackWorkspaceInstallation::getBotToken);
    }

    @Transactional
    public void deactivateInstallation(String teamId, Long slackAppRegistrationId) {
        repository.findByTeamIdAndSlackAppRegistrationId(teamId, slackAppRegistrationId).ifPresent(installation -> {
            installation.deactivate();
            repository.save(installation);
            logger.info("워크스페이스 설치 비활성화: teamId={}", teamId);
        });
    }

    @Transactional
    public void deleteInstallation(String teamId) {
        repository.deleteByTeamId(teamId);
        logger.info("워크스페이스 설치 정보 삭제: teamId={}", teamId);
    }
}
