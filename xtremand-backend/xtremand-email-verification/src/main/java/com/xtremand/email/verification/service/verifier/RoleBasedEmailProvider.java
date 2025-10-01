package com.xtremand.email.verification.service.verifier;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtremand.email.verification.service.EmailValidationRuleService;

@Component
@RequiredArgsConstructor
public class RoleBasedEmailProvider {

    private final EmailValidationRuleService ruleService;

    /**
     * Checks if the local part of an email address is a role-based prefix.
     *
     * @param email The email address to check.
     * @return true if the prefix is role-based, false otherwise.
     */
    public boolean isRoleBased(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }
        String prefix = email.substring(0, email.indexOf("@"));
        return ruleService.isRoleBased(prefix);
    }
}