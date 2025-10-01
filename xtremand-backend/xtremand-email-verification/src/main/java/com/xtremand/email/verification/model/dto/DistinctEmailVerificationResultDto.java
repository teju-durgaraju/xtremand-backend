package com.xtremand.email.verification.model.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xtremand.domain.entity.EmailVerificationHistory;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DistinctEmailVerificationResultDto {

	private String email;
	private String domain;
	private EmailVerificationHistory.VerificationStatus status;
	private int score;
	private EmailVerificationHistory.Confidence confidence;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
	private Instant lastVerifiedAt;

	private VerificationChecksDto checks;

	@Data
	@Builder
	public static class VerificationChecksDto {
		private boolean syntaxCheck;
		private boolean mxCheck;
		private boolean disposableCheck;
		private boolean roleBasedCheck;
		private boolean catchAllCheck;
		private boolean blacklistCheck;
		private boolean smtpCheck;
		private boolean smtpPing;
	}
}