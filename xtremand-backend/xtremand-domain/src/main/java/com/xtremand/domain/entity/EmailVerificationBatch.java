package com.xtremand.domain.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "xt_email_verification_batch")
@Getter
@Setter
@ToString
public class EmailVerificationBatch {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private ZonedDateTime createdAt;

	@Column(name = "total_emails", nullable = false)
	private int totalEmails;

	@Column(name = "valid_emails")
	private int validEmails = 0;

	@Column(name = "invalid_emails")
	private int invalidEmails = 0;

	@Column(name = "deliverable_emails")
	private int deliverableEmails = 0;

	@Column(name = "disposable_emails")
	private int disposableEmails = 0;

	@Column(name = "valid_rate", precision = 5, scale = 2)
	private BigDecimal validRate = BigDecimal.ZERO;

	public void calculateValidRate() {
		if (this.totalEmails > 0) {
			this.validRate = BigDecimal.valueOf((double) this.validEmails * 100 / this.totalEmails).setScale(2,
					RoundingMode.HALF_UP);
		} else {
			this.validRate = BigDecimal.ZERO;
		}
	}
}