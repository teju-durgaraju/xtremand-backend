package com.xtremand.email.verification.repository;

import com.xtremand.domain.entity.EmailValidationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * Repository for {@link EmailValidationRule}.
 */
@Repository
public interface EmailValidationRuleRepository extends JpaRepository<EmailValidationRule, Long> {

    @Query("SELECT r.value FROM EmailValidationRule r WHERE r.ruleType = :ruleType")
    Set<String> findValuesByRuleType(@Param("ruleType") EmailValidationRule.RuleType ruleType);
}