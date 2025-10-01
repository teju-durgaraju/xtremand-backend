package com.xtremand.domain.entity;

import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "xt_user_email_verification_history")
@Getter
@Setter
@ToString
public class EmailVerificationHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_user_email_verification_history_id_seq")
	@SequenceGenerator(name = "xt_user_email_verification_history_id_seq", sequenceName = "xt_user_email_verification_history_id_seq", allocationSize = 1)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	@ToString.Exclude
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "batch_id")
	@ToString.Exclude
	private EmailVerificationBatch batch;

	@Column(name = "email", nullable = false)
	private String email;

	@Column(name = "domain")
	private String domain;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private VerificationStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "recommendation")
	private Recommendation recommendation;

	@Column(name = "score", nullable = false)
	private int score;

	@Enumerated(EnumType.STRING)
	@Column(name = "confidence", nullable = true)
	private Confidence confidence = Confidence.LOW;

	@Column(name = "syntax_check")
	private boolean syntaxCheck;

	@Column(name = "mx_check")
	private boolean mxCheck;

	@Column(name = "disposable_check")
	private boolean disposableCheck;

	@Column(name = "role_based_check")
	private boolean roleBasedCheck;

	@Column(name = "blacklist_check")
	private boolean blacklistCheck;

	@Column(name = "catch_all_check")
	private boolean catchAllCheck;

	@Enumerated(EnumType.STRING)
	@Column(name = "smtp_check_status", nullable = false)
	private SmtpCheckStatus smtpCheckStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "smtp_ping_status", nullable = false)
	private SmtpPingStatus smtpPingStatus;

	@Column(name = "is_catch_all")
	private boolean isCatchAll;

	@Column(name = "is_greylisted")
	private boolean isGreylisted;

	@Type(JsonBinaryType.class)
	@Column(name = "details", columnDefinition = "jsonb")
	private Map<String, Object> details;

	@Column(name = "smtp_logs")
	private String smtpLogs;

	@CreationTimestamp
	@Column(name = "checked_at", updatable = false)
	private Instant checkedAt;

	public enum VerificationStatus {
		VALID, INVALID, RISKY, UNKNOWN, DISPOSABLE, BLACKLISTED
	}

	public enum Recommendation {
		ACCEPT, REVIEW, REJECT
	}

	public enum Confidence {
		HIGH, MEDIUM, LOW
	}

	public enum SmtpCheckStatus {
		DELIVERABLE, INVALID, CATCH_ALL, UNKNOWN, NOT_PERFORMED
	}

	public enum SmtpPingStatus {
		SUCCESS, FAIL, NOT_PERFORMED
	}
}