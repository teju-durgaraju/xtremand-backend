package com.xtremand.emailverification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    public static final String BATCH_VERIFICATION_EXECUTOR = "batchVerificationExecutor";

    @Bean(name = BATCH_VERIFICATION_EXECUTOR)
    public Executor batchVerificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Start with a small number of threads
        executor.setMaxPoolSize(5); // Allow scaling up for bursts
        executor.setQueueCapacity(500); // Buffer for pending jobs
        executor.setThreadNamePrefix("EmailVerify-");
        executor.initialize();
        return executor;
    }
}