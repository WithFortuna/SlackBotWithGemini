package com.zuzihe.slackbot.slack.workspace.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "workspace_installations")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class WorkspaceInstallation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_id", unique = true, nullable = false)
    private String teamId;

    @Column(name = "team_name")
    private String teamName;

    @Column(name = "enterprise_id")
    private String enterpriseId;

    @Column(name = "enterprise_name")
    private String enterpriseName;

    @Column(name = "bot_token", nullable = false, length = 512)
    private String botToken;

    @Column(name = "bot_user_id")
    private String botUserId;

    @Column(name = "bot_scopes", length = 1000)
    private String botScopes;

    @Column(name = "user_token", length = 512)
    private String userToken;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_scopes", length = 1000)
    private String userScopes;

    @Column(name = "installer_user_id")
    private String installerUserId;

    @Column(name = "is_enterprise_install")
    private Boolean isEnterpriseInstall = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "installed_at")
    private LocalDateTime installedAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public WorkspaceInstallation(String teamId, String teamName, String botToken) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.botToken = botToken;
    }
}