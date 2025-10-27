package com.zuzihe.slackbot.bolt.repository;

import aichatter.bolt.domain.SlackWorkspaceUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlackWorkspaceUserRepository extends JpaRepository<SlackWorkspaceUser, Long> {
    Optional<SlackWorkspaceUser> findByTeamIdAndSlackUserId(String teamId, String slackUserId);

    @Query("select s from SlackWorkspaceUser s where s.teamId = :teamId and s.slackUserId = :slackUserId and s.appId = :appId")
    Optional<SlackWorkspaceUser> findWorkspaceUserOnMultipleSlackApps(
            @Param("appId") String appId,
            @Param("teamId") String teamId,
            @Param("slackUserId") String slackUserId
    );
}
