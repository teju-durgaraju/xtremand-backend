package com.xtremand.email.verification.service.verifier;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtremand.email.verification.service.EmailValidationRuleService;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class CatchAllProvider {

    private final EmailValidationRuleService ruleService;

    /**
     * Checks if the domain is a known catch-all domain from the seeded list.
     *
     * @param domain The domain to check.
     * @return true if the domain is in the predefined catch-all list, false otherwise.
     */
    public boolean isKnownCatchAll(String domain) {
        if (domain == null || domain.trim().isEmpty()) {
            return false;
        }
        // This method will be expanded later to include SMTP-based detection.
        // For now, it only checks the predefined list.
        Set<String> catchAllDomains = ruleService.getCatchAllDomains();
        return catchAllDomains.contains(domain.toLowerCase());
    }
}