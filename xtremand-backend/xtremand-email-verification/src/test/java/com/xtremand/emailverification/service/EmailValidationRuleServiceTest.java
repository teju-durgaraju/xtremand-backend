package com.xtremand.emailverification.service;

import com.xtremand.emailverification.domain.EmailValidationRule;
import com.xtremand.emailverification.repository.EmailValidationRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleCacheManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailValidationRuleServiceTest {

    @Mock
    private EmailValidationRuleRepository ruleRepository;

    @InjectMocks
    private EmailValidationRuleService ruleService;

    @BeforeEach
    void setUp() {
        // Mocking the repository calls
        when(ruleRepository.findValuesByRuleType(EmailValidationRule.RuleType.DISPOSABLE_DOMAIN))
                .thenReturn(new HashSet<>(Arrays.asList("mailinator.com", "yopmail.com")));

        when(ruleRepository.findValuesByRuleType(EmailValidationRule.RuleType.ROLE_BASED_PREFIX))
                .thenReturn(new HashSet<>(Arrays.asList("admin", "support")));

        when(ruleRepository.findValuesByRuleType(EmailValidationRule.RuleType.BLACKLIST_DOMAIN))
                .thenReturn(new HashSet<>(Arrays.asList("spam.com")));

        when(ruleRepository.findValuesByRuleType(EmailValidationRule.RuleType.BLACKLIST_EMAIL))
                .thenReturn(new HashSet<>(Arrays.asList("spammer@evil.com")));
    }

    @Test
    void testGetDisposableDomains() {
        Set<String> domains = ruleService.getDisposableDomains();
        assertEquals(2, domains.size());
        assertTrue(domains.contains("mailinator.com"));
    }

    @Test
    void testIsDisposable() {
        assertTrue(ruleService.isDisposable("mailinator.com"));
        assertFalse(ruleService.isDisposable("gmail.com"));
    }

    @Test
    void testGetRoleBasedPrefixes() {
        Set<String> prefixes = ruleService.getRoleBasedPrefixes();
        assertEquals(2, prefixes.size());
        assertTrue(prefixes.contains("admin"));
    }

    @Test
    void testIsRoleBased() {
        assertTrue(ruleService.isRoleBased("admin"));
        assertFalse(ruleService.isRoleBased("john"));
    }

    @Test
    void testIsBlacklisted() {
        assertTrue(ruleService.isBlacklisted("spammer@evil.com")); // Email blacklist
        assertTrue(ruleService.isBlacklisted("user@spam.com")); // Domain blacklist
        assertFalse(ruleService.isBlacklisted("user@good.com"));
    }
}