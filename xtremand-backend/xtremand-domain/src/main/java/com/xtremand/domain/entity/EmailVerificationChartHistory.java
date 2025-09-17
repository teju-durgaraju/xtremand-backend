package com.xtremand.domain.entity;

import com.xtremand.domain.enums.AggregationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "xt_email_verification_chart_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationChartHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String period;

    @Column(nullable = false)
    private int verified;

    @Column(nullable = false)
    private int deliverable;

    @Column(nullable = false)
    private int risky;

    @Column(nullable = false)
    private int invalid;

    @Column(nullable = false)
    private int unknown;

    @Column(nullable = false)
    private int total;

    @Enumerated(EnumType.STRING)
    @Column(name = "aggregation_type", nullable = false)
    private AggregationType aggregationType;
}
