package com.xtremand.email.verification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.xtremand.email.verification.config.AsyncConfig;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchVerificationService {

    private final EmailVerificationService emailVerificationService;

    public void startBatchVerification(List<String> emails, Long userId) {
        log.info("Starting batch verification for {} emails. User ID: {}", emails.size(), userId);
        processBatch(emails, userId);
    }

    @Async(AsyncConfig.BATCH_VERIFICATION_EXECUTOR)
    public CompletableFuture<Void> processBatch(List<String> emails, Long userId) {
        log.info("Executing async batch verification for {} emails.", emails.size());
        for (String email : emails) {
            try {
                // We call the main service which handles its own transaction for each email.
                emailVerificationService.verifyEmail(email, userId);
            } catch (Exception e) {
                // Log and continue with the next email. Don't let one failure stop the whole batch.
                log.error("Error verifying email '{}' during batch process. User ID: {}. Error: {}", email, userId, e.getMessage());
            }
        }
        log.info("Async batch verification complete for user ID: {}", userId);
        return CompletableFuture.completedFuture(null);
    }
}