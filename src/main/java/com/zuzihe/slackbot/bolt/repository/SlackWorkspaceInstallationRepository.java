package com.zuzihe.slackbot.bolt.repository;

import aichatter.bolt.domain.SlackWorkspaceInstallation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlackWorkspaceInstallationRepository extends JpaRepository<SlackWorkspaceInstallation, Long> {

    Optional<SlackWorkspaceInstallation> findByTeamIdAndSlackAppRegistrationId(String teamId, Long slackAppRegistrationId);

    Optional<SlackWorkspaceInstallation> findByTeamIdAndIsActiveTrue(String teamId);

    Optional<SlackWorkspaceInstallation> findByTeamIdAndSlackAppRegistrationIdAndIsActiveTrue(String teamId, Long slackAppRegistrationId);

    boolean existsByTeamId(String teamId);

    void deleteByTeamId(String teamId);
}
