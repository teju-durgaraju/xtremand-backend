package com.xtremand.email.verification.service;

import com.xtremand.domain.entity.EmailValidationRule;
import com.xtremand.domain.entity.UserEmailVerificationHistory;
import com.xtremand.domain.enums.RuleType;
import com.xtremand.email.verification.repository.EmailValidationRuleRepository;
import com.xtremand.email.verification.repository.UserEmailVerificationHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private UserEmailVerificationHistoryRepository historyRepository;

    @Mock
    private EmailValidationRuleRepository ruleRepository;

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

        when(ruleRepository.findByRuleType(RuleType.DISPOSABLE)).thenReturn(List.of(EmailValidationRule.builder().value("mailinator.com").build()));
        when(ruleRepository.findByRuleType(RuleType.ROLE_BASED)).thenReturn(List.of(EmailValidationRule.builder().value("admin@").build()));
        when(ruleRepository.findByRuleType(RuleType.BLACKLIST_EMAIL)).thenReturn(List.of(EmailValidationRule.builder().value("testspam@gmail.com").build()));
        when(ruleRepository.findByRuleType(RuleType.BLACKLIST_DOMAIN)).thenReturn(List.of(EmailValidationRule.builder().value("spammail.com").build()));
        when(ruleRepository.findByRuleType(RuleType.CATCH_ALL)).thenReturn(Collections.emptyList());

        emailVerificationService.loadValidationRules();
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
        assertTrue(result.getChecks().isDisposableCheck());
        verify(historyRepository).save(any(UserEmailVerificationHistory.class));
    }

    @Test
    void verify_roleBasedEmail_shouldFlagRoleBasedCheck() {
        String email = "admin@example.com";
        var result = emailVerificationService.verify(email);
        assertEquals(email, result.getEmail());
        assertTrue(result.getChecks().isRoleBasedCheck());
        verify(historyRepository).save(any(UserEmailVerificationHistory.class));
    }

    @Test
    void verify_blacklistedEmail_shouldFlagBlacklistCheck() {
        String email = "testspam@gmail.com";
        var result = emailVerificationService.verify(email);
        assertEquals(email, result.getEmail());
        assertTrue(result.getChecks().isBlacklistCheck());
        verify(historyRepository).save(any(UserEmailVerificationHistory.class));
    }

    @Test
    void verify_blacklistedDomain_shouldFlagBlacklistCheck() {
        String email = "test@spammail.com";
        var result = emailVerificationService.verify(email);
        assertEquals(email, result.getEmail());
        assertTrue(result.getChecks().isBlacklistCheck());
        verify(historyRepository).save(any(UserEmailVerificationHistory.class));
    }
}
