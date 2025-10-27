package com.zuzihe.slackbot.bolt.repository;

import aichatter.bolt.domain.SlackChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlackChatRoomRepository extends JpaRepository<SlackChatRoom, Long> {
    Optional<SlackChatRoom> findByTeamIdAndChannelAndThreadTsAndSlackWorkspaceUserId(String teamId, String channel, String threadTs, Long slackWorkspaceUserId);
}
