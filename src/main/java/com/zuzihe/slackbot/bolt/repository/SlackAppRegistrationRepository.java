package com.zuzihe.slackbot.bolt.repository;

import aichatter.bolt.domain.SlackAppRegistration;
import aichatter.bolt.domain.SlackAppType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlackAppRegistrationRepository extends JpaRepository<SlackAppRegistration, Long> {
    Optional<SlackAppRegistration> findBySlackAppType(SlackAppType slackAppType);

    Optional<SlackAppRegistration> findByAppId(String appId);

    void deleteBySlackAppType(SlackAppType slackAppType);
}
