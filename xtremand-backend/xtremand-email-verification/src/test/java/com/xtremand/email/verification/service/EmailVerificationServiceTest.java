package com.xtremand.email.verification.service;

import com.xtremand.domain.entity.UserEmailVerificationHistory;
import com.xtremand.email.verification.repository.UserEmailVerificationHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private UserEmailVerificationHistoryRepository historyRepository;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailVerificationService, "syntaxScore", 20);
        ReflectionTestUtils.setField(emailVerificationService, "mxScore", 20);
        ReflectionTestUtils.setField(emailVerificationService, "disposableScore", 15);
        ReflectionTestUtils.setField(emailVerificationService, "roleBasedScore", 10);
        ReflectionTestUtils.setField(emailVerificationService, "catchAllScore", 10);
        ReflectionTestUtils.setField(emailVerificationService, "blacklistScore", 15);
        ReflectionTestUtils.setField(emailVerificationService, "smtpScore", 10);
        ReflectionTestUtils.setField(emailVerificationService, "disposableDomains", Collections.singleton("mailinator.com"));
        ReflectionTestUtils.setField(emailVerificationService, "roleBasedPrefixes", Collections.singleton("admin"));
        ReflectionTestUtils.setField(emailVerificationService, "blacklistedDomains", Collections.emptySet());
    }

    @Test
    void verify_validEmail_shouldReturnValidStatus() {
        String email = "test@example.com";
        var result = emailVerificationService.verify(email);
        assertEquals(email, result.getEmail());
        verify(historyRepository).save(any(UserEmailVerificationHistory.class));
    }

    @Test
    void verify_invalidEmail_shouldReturnInvalidStatus() {
        String email = "invalid-email";
        var result = emailVerificationService.verify(email);
        assertEquals(email, result.getEmail());
        assertEquals(0, result.getScore());
        verify(historyRepository).save(any(UserEmailVerificationHistory.class));
    }

    @Test
    void verify_disposableEmail_shouldReturnDisposableStatus() {
        String email = "test@mailinator.com";
        var result = emailVerificationService.verify(email);
        assertEquals(email, result.getEmail());
        assertEquals(true, result.getChecks().isDisposableCheck());
        verify(historyRepository).save(any(UserEmailVerificationHistory.class));
    }
}
