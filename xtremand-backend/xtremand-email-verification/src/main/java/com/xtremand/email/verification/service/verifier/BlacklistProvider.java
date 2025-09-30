package com.xtremand.email.verification.service.verifier;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtremand.email.verification.service.EmailValidationRuleService;

@Component
@RequiredArgsConstructor
public class BlacklistProvider {

    private final EmailValidationRuleService ruleService;

    /**
     * Checks if an email address or its domain is on the blacklist.
     *
     * @param email The email address to check.
     * @return true if the email or domain is blacklisted, false otherwise.
     */
    public boolean isBlacklisted(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return ruleService.isBlacklisted(email);
    }
}