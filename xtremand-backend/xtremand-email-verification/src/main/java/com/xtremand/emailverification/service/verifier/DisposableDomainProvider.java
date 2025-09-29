package com.xtremand.emailverification.service.verifier;

import com.xtremand.emailverification.service.EmailValidationRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DisposableDomainProvider {

    private final EmailValidationRuleService ruleService;

    /**
     * Checks if the domain of an email address is a known disposable email provider.
     *
     * @param email The email address to check.
     * @return true if the domain is disposable, false otherwise.
     */
    public boolean isDisposable(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }
        String domain = email.substring(email.indexOf("@") + 1);
        return ruleService.isDisposable(domain);
    }
}