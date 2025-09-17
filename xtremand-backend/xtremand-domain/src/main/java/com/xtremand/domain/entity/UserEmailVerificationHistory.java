package com.xtremand.domain.entity;

import com.xtremand.domain.enums.Confidence;
import com.xtremand.domain.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "xt_user_email_verification_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEmailVerificationHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Confidence confidence;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status;

    @Column(name = "syntax_check", nullable = false)
    private boolean syntaxCheck;

    @Column(name = "mx_check", nullable = false)
    private boolean mxCheck;

    @Column(name = "disposable_check", nullable = false)
    private boolean disposableCheck;

    @Column(name = "role_based_check", nullable = false)
    private boolean roleBasedCheck;

    @Column(name = "catch_all_check", nullable = false)
    private boolean catchAllCheck;

    @Column(name = "blacklist_check", nullable = false)
    private boolean blacklistCheck;

    @Column(name = "smtp_check", nullable = false)
    private boolean smtpCheck;
}
