package com.xtremand.email.verification.repository;

import com.xtremand.domain.entity.EmailValidationRule;
import com.xtremand.domain.enums.RuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailValidationRuleRepository extends JpaRepository<EmailValidationRule, Long> {
    List<EmailValidationRule> findByRuleType(RuleType ruleType);
}
