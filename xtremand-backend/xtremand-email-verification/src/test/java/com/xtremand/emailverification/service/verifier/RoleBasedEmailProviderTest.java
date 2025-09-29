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
class RoleBasedEmailProviderTest {

    @Mock
    private EmailValidationRuleService ruleService;

    @InjectMocks
    private RoleBasedEmailProvider roleBasedEmailProvider;

    @Test
    void testIsRoleBased_True() {
        when(ruleService.isRoleBased("admin")).thenReturn(true);
        assertTrue(roleBasedEmailProvider.isRoleBased("admin@example.com"));
    }

    @Test
    void testIsRoleBased_False() {
        when(ruleService.isRoleBased("john.doe")).thenReturn(false);
        assertFalse(roleBasedEmailProvider.isRoleBased("john.doe@example.com"));
    }

    @Test
    void testIsRoleBased_InvalidEmailFormat() {
        assertFalse(roleBasedEmailProvider.isRoleBased("invalid-email"));
    }

    @Test
    void testIsRoleBased_NullEmail() {
        assertFalse(roleBasedEmailProvider.isRoleBased(null));
    }
}