package com.xtremand.email.verification.model;

import com.xtremand.domain.entity.EmailVerificationHistory;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SmtpProbeResult {
    private final EmailVerificationHistory.SmtpCheckStatus status;
    private final EmailVerificationHistory.SmtpPingStatus pingStatus;
    private final boolean isCatchAll;
    private final boolean isGreylisted;
    private final String lastServerResponse;
    private final String logs;

    public static SmtpProbeResult notPerformed() {
        return SmtpProbeResult.builder()
                .status(EmailVerificationHistory.SmtpCheckStatus.NOT_PERFORMED)
                .pingStatus(EmailVerificationHistory.SmtpPingStatus.NOT_PERFORMED)
                .build();
    }
}