package com.zuzihe.slackbot.bolt.application.common.service.chatroom;

import aichatter.bolt.application.common.dto.SlackChatRoomCreateDTO;
import aichatter.bolt.application.common.dto.SlackChatRoomSearchDTO;
import aichatter.bolt.domain.SlackChatRoom;
import aichatter.bolt.repository.SlackChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

import static aichatter.config.SecurityConfig.LoggerUtil.logger;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SlackChatRoomService {
    private final SlackChatRoomRepository slackChatRoomRepository;

    public Optional<String> findChatRoomIdByThreadInfo(String teamId, String channel, String threadTs, Long slackWorkspaceUserId) {
        logger.info("[SlackChatRoom] 매핑 조회 - TeamId: {}, Channel: {}, ThreadTs: {}, slackWorkspaceUserId: {}", teamId, channel, threadTs, slackWorkspaceUserId);
        Optional<SlackChatRoom> slackChatRoom = slackChatRoomRepository
                .findByTeamIdAndChannelAndThreadTsAndSlackWorkspaceUserId(
                        teamId,
                        channel,
                        threadTs,
                        slackWorkspaceUserId);

        if (slackChatRoom.isPresent()) {
            String chatRoomId = slackChatRoom.get().getChatRoomId();
            logger.info("[SlackChatRoom] 매핑 조회 성공 - ChatRoomId: {}", chatRoomId);
            return Optional.of(chatRoomId);
        } else {
            logger.info("[SlackChatRoom] 매핑 조회 실패 - 매핑되지 않은 스레드");
            return Optional.empty();
        }
    }

    public Optional<SlackChatRoom> findSlackChatRoom(SlackChatRoomSearchDTO dto) {
        logger.info("[SlackChatRoom] SlackChatRoom 조회 - TeamId: {}, Channel: {}, ThreadTs: {}, slackWorkspaceUserId: {}",
                dto.teamId(), dto.channel(), dto.threadTs(), dto.slackWorkspaceUserId());

        Optional<SlackChatRoom> slackChatRoom = slackChatRoomRepository
                .findByTeamIdAndChannelAndThreadTsAndSlackWorkspaceUserId(
                        dto.teamId(),
                        dto.channel(),
                        dto.threadTs(),
                        dto.slackWorkspaceUserId()
                );

        if (slackChatRoom.isPresent()) {
            logger.info("[SlackChatRoom] SlackChatRoom 조회 성공 - Id: {}, ChatRoomId: {}",
                    slackChatRoom.get().getId(), slackChatRoom.get().getChatRoomId());
        } else {
            logger.info("[SlackChatRoom] SlackChatRoom 조회 실패 - 매핑되지 않은 스레드");
        }

        return slackChatRoom;
    }

    @Transactional
    public Long createSlackChatRoom(SlackChatRoomCreateDTO dto) {
        logger.info("[SlackChatRoom] 매핑 생성 시작 - TeamId: {}, Channel: {}, ThreadTs: {}, ChatRoomId: {}",
                dto.teamId(), dto.channel(), dto.threadTs(), dto.chatRoomId());

        SlackChatRoom slackChatRoom = SlackChatRoom.builder()
                .slackAppRegistrationId(dto.slackAppRegistrationId())
                .teamId(dto.teamId())
                .channel(dto.channel())
                .threadTs(dto.threadTs())
                .slackWorkspaceUserId(dto.slackWorkspaceUserId())
                .chatRoomId(dto.chatRoomId())
                .chatRoomTitle(dto.chatRoomTitle())
                .domainId(dto.domainId())
                .accountId(dto.accountId())
                .regDate(OffsetDateTime.now())
                .modDate(OffsetDateTime.now())
                .build();

        SlackChatRoom savedMapping = slackChatRoomRepository.save(slackChatRoom);
        logger.info("[SlackChatRoom] 매핑 생성 완료 - Id: {}", savedMapping.getId());

        return savedMapping.getId();
    }
}
