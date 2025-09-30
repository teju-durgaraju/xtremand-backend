package com.xtremand.email.verification.service;

import com.xtremand.domain.entity.EmailValidationRule;
import com.xtremand.email.verification.config.CacheConfig;
import com.xtremand.email.verification.repository.EmailValidationRuleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmailValidationRuleService {

    private final EmailValidationRuleRepository ruleRepository;

    @Cacheable(CacheConfig.RULES_CACHE)
    public Set<String> getDisposableDomains() {
        return ruleRepository.findValuesByRuleType(EmailValidationRule.RuleType.DISPOSABLE_DOMAIN);
    }

    @Cacheable(CacheConfig.RULES_CACHE)
    public Set<String> getRoleBasedPrefixes() {
        return ruleRepository.findValuesByRuleType(EmailValidationRule.RuleType.ROLE_BASED_PREFIX);
    }

    @Cacheable(CacheConfig.RULES_CACHE)
    public Set<String> getBlacklistedDomains() {
        return ruleRepository.findValuesByRuleType(EmailValidationRule.RuleType.BLACKLIST_DOMAIN);
    }

    @Cacheable(CacheConfig.RULES_CACHE)
    public Set<String> getBlacklistedEmails() {
        return ruleRepository.findValuesByRuleType(EmailValidationRule.RuleType.BLACKLIST_EMAIL);
    }

    @Cacheable(CacheConfig.RULES_CACHE)
    public Set<String> getCatchAllDomains() {
        return ruleRepository.findValuesByRuleType(EmailValidationRule.RuleType.CATCH_ALL_DOMAIN);
    }

    public boolean isBlacklisted(String email) {
        if (getBlacklistedEmails().contains(email.toLowerCase())) {
            return true;
        }
        String domain = email.substring(email.indexOf("@") + 1);
        return getBlacklistedDomains().contains(domain.toLowerCase());
    }

    public boolean isDisposable(String domain) {
        return getDisposableDomains().contains(domain.toLowerCase());
    }

    public boolean isRoleBased(String prefix) {
        return getRoleBasedPrefixes().contains(prefix.toLowerCase());
    }
}