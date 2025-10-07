package com.xtremand.api.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerifierService {

    private final com.xtremand.email.verification.service.EmailVerificationService emailVerificationService;

    @Async
    public void verifyEmails(Set<String> emails) {
        log.info("Starting asynchronous verification for {} emails.", emails.size());
        for (String email : emails) {
            try {
                // Since the existing service handles user context via SecurityContextHolder,
                // and this is an async thread, the context might not be available.
                // The original service has a fallback to get user from the batch, but here we pass null.
                // The service seems to handle the case where the user is not found gracefully.
                // We will call the simple verifyEmail method.
                emailVerificationService.verifyEmail(email);
                log.debug("Verification triggered for email: {}", email);
            } catch (Exception e) {
                // Log and continue, as per requirements, do not block or fail the main request.
                log.error("Error during async email verification for email: {}", email, e);
            }
        }
        log.info("Completed asynchronous verification for {} emails.", emails.size());
    }
}