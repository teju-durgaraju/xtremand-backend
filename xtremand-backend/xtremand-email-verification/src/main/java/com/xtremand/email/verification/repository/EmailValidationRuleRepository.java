package com.xtremand.email.verification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.xtremand.domain.entity.EmailValidationRule;

import java.util.Set;

@Repository
public interface EmailValidationRuleRepository extends JpaRepository<EmailValidationRule, Long> {

    @Query("SELECT r.value FROM EmailValidationRule r WHERE r.ruleType = :ruleType")
    Set<String> findValuesByRuleType(EmailValidationRule.RuleType ruleType);

    boolean existsByRuleTypeAndValue(EmailValidationRule.RuleType ruleType, String value);
}