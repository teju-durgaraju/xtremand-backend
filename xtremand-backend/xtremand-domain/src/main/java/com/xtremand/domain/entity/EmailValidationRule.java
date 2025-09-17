package com.xtremand.domain.entity;

import com.xtremand.domain.enums.RuleType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "xt_email_validation_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailValidationRule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private RuleType ruleType;

    @Column(nullable = false)
    private String value;
}
