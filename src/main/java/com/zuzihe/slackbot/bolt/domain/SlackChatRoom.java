package com.zuzihe.slackbot.bolt.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Table(name = "SlackChatRoom",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"slackAppRegistration_id","teamId", "channel", "threadTs", "slackWorkspaceUser_id"})})
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SlackChatRoom {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @NotNull(message = "유효한 regDate 값을 제공해 주세요")
    @Column(name = "regDate")
    private OffsetDateTime regDate;

    @Column(name = "modDate")
    private OffsetDateTime modDate;

    @NotNull
    @Column(name = "slackAppRegistration_id")
    private Long slackAppRegistrationId; // 연결된 슬랙 앱

    @NotNull
    @Column(name = "teamId")
    private String teamId;          // slack workspace 식별자

    @NotNull
    @Column(name = "channel")
    private String channel;         // slack channel 식별자

    @NotNull
    @Column(name = "threadTs")
    private String threadTs;        // slack thread timestamp (스레드 식별자)

    @NotNull
    @Column(name = "slackWorkspaceUser_id")
    private Long slackWorkspaceUserId; // 방을 생성한 슬랙 사용자

    @NotNull
    @Column(name = "chatRoom_id")
    private String chatRoomId;      // aichatter ChatRoom ID (Elasticsearch)

    @NotNull
    @Column(name = "chatRoomTitle")
    private String chatRoomTitle;

    @NotNull
    @Column(name = "domain_id")
    private Long domainId;

    @NotNull
    @Column(name = "account_id")
    private Long accountId;         // aichatter 계정 아이디

    public void updateChatRoomTitle(String chatRoomTitle) {
        this.chatRoomId = chatRoomTitle;
    }
}
