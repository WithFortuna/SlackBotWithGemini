package com.zuzihe.slackbot.bolt.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Table(name = "SlackAppRegistration",
        uniqueConstraints = {@UniqueConstraint(columnNames = "slackAppType")})
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SlackAppRegistration {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appId")
    private String appId;

    @NotNull
    @Column(name = "clientId")
    private String clientId;

    @NotNull
    @Column(name = "clientSecret")
    private String clientSecret;

    @NotNull
    @Column(name = "signingSecret")
    private String signingSecret;

    @NotNull
    @Column(
            name = "slackAppType",
            columnDefinition = "VARCHAR(50)"
    )
    @Enumerated(EnumType.STRING)
    private SlackAppType slackAppType;

    @NotNull(message = "유효한 regDate 값을 제공해 주세요")
    @Column(name = "regDate")
    private OffsetDateTime regDate;

    @Column(name = "modDate")
    private OffsetDateTime modDate;
}
