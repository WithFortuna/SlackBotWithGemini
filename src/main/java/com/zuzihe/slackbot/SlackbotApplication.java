package com.zuzihe.slackbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.zuzihe.slackbot.slack.workspace.repository")
public class SlackbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(SlackbotApplication.class, args);
    }

}
