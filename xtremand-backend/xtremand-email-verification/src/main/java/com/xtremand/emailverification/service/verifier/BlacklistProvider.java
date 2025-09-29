package com.xtremand.emailverification.service.verifier;

import com.xtremand.emailverification.service.EmailValidationRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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