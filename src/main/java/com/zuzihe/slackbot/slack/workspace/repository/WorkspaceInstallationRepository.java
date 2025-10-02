package com.zuzihe.slackbot.slack.workspace.repository;

import com.zuzihe.slackbot.slack.workspace.entity.WorkspaceInstallation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkspaceInstallationRepository extends JpaRepository<WorkspaceInstallation, Long> {

    Optional<WorkspaceInstallation> findByTeamId(String teamId);

    Optional<WorkspaceInstallation> findByTeamIdAndIsActiveTrue(String teamId);

    boolean existsByTeamId(String teamId);

    void deleteByTeamId(String teamId);
}