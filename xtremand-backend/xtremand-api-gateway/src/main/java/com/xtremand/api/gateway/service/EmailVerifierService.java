package com.xtremand.api.gateway.service;

import com.xtremand.domain.entity.User;
import com.xtremand.email.verification.service.EmailVerificationService;
import com.xtremand.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerifierService {

    private final EmailVerificationService emailVerificationService;
    private final UserRepository userRepository;

    @Value("${xtremand.system.user.email:system@xtremand.com}")
    private String systemUserEmail;

    private User systemUser;

    @PostConstruct
    public void init() {
        this.systemUser = userRepository.findByEmail(systemUserEmail)
                .orElseThrow(() -> new IllegalStateException("System user not found: " + systemUserEmail));
        log.info("EmailVerifierService initialized with system user: {}", systemUser.getEmail());
    }

    @Async("emailVerifierExecutor")
    public void verifyEmails(Set<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return;
        }
        log.info("Starting asynchronous verification for {} emails.", emails.size());
        for (String email : emails) {
            try {
                log.debug("Verifying email [{}] for system user.", email);
                emailVerificationService.verifyEmailAsynchronously(email, systemUser);
            } catch (Exception e) {
                log.error("Error verifying email [{}]: {}", email, e.getMessage(), e);
            }
        }
    }
}