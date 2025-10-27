package com.zuzihe.slackbot.bolt.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "SlackWorkspaceInstallation",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"teamId", "slackAppRegistration_id"})}
)
public class SlackWorkspaceInstallation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "slackAppRegistration_id")
    private Long slackAppRegistrationId;

    @NotNull
    @Column(name = "teamId", nullable = false)
    private String teamId;  //slack workspace 식별자

    @NotNull
    @Column(name = "teamName")
    private String teamName;

    @Column(name = "enterpriseId")
    private String enterpriseId;

    @Column(name = "enterpriseName")
    private String enterpriseName;

    @NotNull
    @Column(name = "botToken", nullable = false, length = 512)
    private String botToken;    // slack workspace에 설치된 slack app 토큰(workspace에 종속적이므로 workspace  식별 가능)

    @NotNull
    @Column(name = "botUserId")
    private String botUserId;

    @NotNull
    @Column(name = "botScopes", length = 255)
    private String botScopes;

    @Column(name = "installerUserId")
    private String installerUserId;

    @Column(name = "isEnterpriseInstall")
    private Boolean isEnterpriseInstall;

    @Column(name = "isActive")
    private Boolean isActive = true;

    @NotNull(message = "유효한 regDate 값을 제공해 주세요")
    @Column(name = "regDate")
    private OffsetDateTime regDate;

    @Column(name = "modDate")
    private OffsetDateTime modDate;

    public SlackWorkspaceInstallation(Long slackAppRegistrationId,
                                      String teamId,
                                      String teamName,
                                      String botToken,
                                      String botUserId,
                                      String botScopes,
                                      String installerUserId,
                                      Boolean isEnterpriseInstall,
                                      OffsetDateTime regDate) {
        this.slackAppRegistrationId = slackAppRegistrationId;
        this.teamId = teamId;
        this.teamName = teamName;
        this.botToken = botToken;
        this.botUserId = botUserId;
        this.botScopes = botScopes;
        this.installerUserId = installerUserId;
        this.isEnterpriseInstall = isEnterpriseInstall;
        this.isActive = true;
        this.regDate = regDate;
        this.modDate = regDate;
    }


    // 업데이트를 위한 메서드
    public void updateInstallation(String botToken, String botUserId, String botScopes,
                                   String installerUserId, Boolean isEnterpriseInstall) {
        this.botToken = botToken;
        this.botUserId = botUserId;
        this.botScopes = botScopes;
        this.installerUserId = installerUserId;
        this.isEnterpriseInstall = isEnterpriseInstall;
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}