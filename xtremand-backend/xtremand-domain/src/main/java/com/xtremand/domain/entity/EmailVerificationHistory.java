package com.xtremand.domain.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.ZonedDateTime;
import java.util.Map;

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

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "email", nullable = false)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private VerificationStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "recommendation")
	private Recommendation recommendation;

	@Column(name = "score", nullable = false)
	private int score;

	@Enumerated(EnumType.STRING)
	@Column(name = "confidence")
	private Confidence confidence;

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
	private ZonedDateTime checkedAt;

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