package com.zuzihe.slackbot.bolt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class SlackAsyncConfig {
    @Bean(name = "slackAsyncExecutor")
    public Executor slackAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("slack-async-");
        executor.initialize();

        // SecurityContext를 자동으로 전파
        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }

}
