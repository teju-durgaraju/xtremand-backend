package com.xtremand.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "xt_email_verification_kpi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationKpi extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 7)
    private String month;

    @Column(name = "valid_emails", nullable = false)
    private int validEmails;

    @Column(name = "invalid_emails", nullable = false)
    private int invalidEmails;

    @Column(name = "risky_emails", nullable = false)
    private int riskyEmails;

    @Column(name = "unknown_emails", nullable = false)
    private int unknownEmails;

    @Column(name = "total_processed", nullable = false)
    private int totalProcessed;

    @Column(name = "quality_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal qualityScore;

    @Column(name = "deliverability_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal deliverabilityRate;

    @Column(name = "bounce_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal bounceRate;
}
