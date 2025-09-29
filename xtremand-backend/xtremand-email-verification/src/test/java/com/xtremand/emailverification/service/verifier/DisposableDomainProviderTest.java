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
class DisposableDomainProviderTest {

    @Mock
    private EmailValidationRuleService ruleService;

    @InjectMocks
    private DisposableDomainProvider disposableDomainProvider;

    @Test
    void testIsDisposable_True() {
        when(ruleService.isDisposable("mailinator.com")).thenReturn(true);
        assertTrue(disposableDomainProvider.isDisposable("user@mailinator.com"));
    }

    @Test
    void testIsDisposable_False() {
        when(ruleService.isDisposable("gmail.com")).thenReturn(false);
        assertFalse(disposableDomainProvider.isDisposable("user@gmail.com"));
    }

    @Test
    void testIsDisposable_InvalidEmailFormat() {
        assertFalse(disposableDomainProvider.isDisposable("invalid-email"));
    }

    @Test
    void testIsDisposable_NullEmail() {
        assertFalse(disposableDomainProvider.isDisposable(null));
    }
}