package com.xtremand.emailverification.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(name = "xt_email_validation_rules", schema = "xtremand_production")
@Getter
@Setter
@ToString
public class EmailValidationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private RuleType ruleType;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    public enum RuleType {
        DISPOSABLE_DOMAIN,
        ROLE_BASED_PREFIX,
        BLACKLIST_DOMAIN,
        BLACKLIST_EMAIL,
        CATCH_ALL_DOMAIN
    }
}