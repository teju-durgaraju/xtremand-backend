package com.xtremand.emailverification.model;

import com.xtremand.emailverification.domain.EmailVerificationHistory;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class VerificationResult {
    private String email;
    private EmailVerificationHistory.VerificationStatus status;
    private EmailVerificationHistory.Recommendation recommendation;
    private int score;
    private EmailVerificationHistory.Confidence confidence;

    // Individual check results
    private boolean syntaxCheck;
    private boolean mxCheck;
    private boolean disposableCheck;
    private boolean roleBasedCheck;
    private boolean blacklistCheck;
    private boolean catchAllCheck;

    // SMTP details
    private SmtpProbeResult smtpProbeResult;

    // Metadata
    private Map<String, Object> details;
}