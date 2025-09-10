package com.xtremand.domain.entity;

import java.time.Instant;

import com.xtremand.domain.enums.ActivationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "xt_user_activation_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivationHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_user_activation_history_id_seq")
    @SequenceGenerator(name = "xt_user_activation_history_id_seq", sequenceName = "xt_user_activation_history_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "activation_token", nullable = false, unique = true)
    private String activationToken;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivationStatus status;
}
