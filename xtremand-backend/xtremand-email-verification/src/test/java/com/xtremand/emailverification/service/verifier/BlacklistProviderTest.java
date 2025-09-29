package com.xtremand.emailverification.service.verifier;

import com.xtremand.emailverification.service.EmailValidationRuleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlacklistProviderTest {

    @Mock
    private EmailValidationRuleService ruleService;

    @InjectMocks
    private BlacklistProvider blacklistProvider;

    @Test
    void testIsBlacklisted_True() {
        String email = "spammer@evil.com";
        when(ruleService.isBlacklisted(email)).thenReturn(true);
        assertTrue(blacklistProvider.isBlacklisted(email));
    }

    @Test
    void testIsBlacklisted_False() {
        String email = "gooduser@example.com";
        when(ruleService.isBlacklisted(email)).thenReturn(false);
        assertFalse(blacklistProvider.isBlacklisted(email));
    }

    @Test
    void testIsBlacklisted_NullAndEmpty() {
        assertFalse(blacklistProvider.isBlacklisted(null));
        assertFalse(blacklistProvider.isBlacklisted(""));
    }
}