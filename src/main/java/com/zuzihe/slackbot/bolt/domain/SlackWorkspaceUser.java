package com.zuzihe.slackbot.bolt.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;


@Table(name = "SlackWorkspaceUser",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"appId", "teamId", "slackUserId"})})
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SlackWorkspaceUser {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "appId")
    private String appId;           // slack app 식별자

    @NotNull
    @Column(name = "slackAppRegistration_id")
    private Long slackAppRegistrationId;

    @NotNull
    @Column(name = "teamId")
    private String teamId;          // slack workspace 식별자

    @NotNull
    @Column(name = "slackWorkspaceInstallation_id")
    private Long slackWorkspaceInstallationId;

    @NotNull
    @Column(name = "slackUserId")
    private String slackUserId;     // workspace 내부 사용자 식별자

    @NotNull
    @Column(name = "domain_id")
    private Long domainId;

    @NotNull
    @Column(name = "account_id")
    private Long accountId;         // aichatter 계정 아이디

    @NotNull(message = "유효한 사용자 유형을 제공해 주세요")
    @Column(name = "userType", length = 20)
    private String userType;        // aichatter 계정 유저타입

    @NotNull(message = "유효한 regDate 값을 제공해 주세요")
    @Column(name = "regDate")
    private OffsetDateTime regDate;

    @Column(name = "modDate")
    private OffsetDateTime modDate;
}

