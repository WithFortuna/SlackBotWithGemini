package com.zuzihe.slackbot.bolt.application.common.service.registration;

import aichatter.bolt.domain.SlackAppRegistration;
import aichatter.bolt.domain.SlackAppType;
import aichatter.bolt.repository.SlackAppRegistrationRepository;
import aichatter.bolt.web.dto.SlackAppRegistrationCreateDTO;
import aichatter.global.ErrorCode;
import aichatter.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class SlackAppRegistrationService {
    private final SlackAppRegistrationRepository appRegistrationRepository;

    @Transactional
    public Long registerSlackApp(SlackAppRegistrationCreateDTO dto) {
        if (appRegistrationRepository.findBySlackAppType(dto.slackAppType()).isPresent()) {
            throw new CustomException(ErrorCode.SLACK_APP_ALREADY_REGISTERED);
        }

        OffsetDateTime now = OffsetDateTime.now();
        SlackAppRegistration entity = new SlackAppRegistration(
                null,
                dto.appId(),
                dto.clientId(),
                dto.clientSecret(),
                dto.signingSecret(),
                dto.slackAppType(),
                now,
                now
        );
        return appRegistrationRepository.save(entity)
                .getId();
    }

    /**
     * SlackAppType을 기준으로 SlackAppRegistration이 존재하는지 확인
     * @param slackAppType 확인할 Slack 앱 타입
     * @return 존재하면 true, 존재하지 않으면 false
     */
    public boolean existsBySlackAppType(SlackAppType slackAppType) {
        return appRegistrationRepository.findBySlackAppType(slackAppType).isPresent();
    }

    @Transactional
    public void unregisterSlackApp(SlackAppType slackAppType) {
        appRegistrationRepository.deleteBySlackAppType(slackAppType);
        // TODO: App 삭제 시 workspaceInstallation도 삭제해야한다
    }

}
