package com.xtremand.email.verification.model.mapper;

import com.xtremand.domain.entity.EmailVerificationHistory;
import com.xtremand.email.verification.model.dto.VerifyEmailResponse;

import org.springframework.stereotype.Component;

@Component
public class EmailVerificationMapper {

    public VerifyEmailResponse toDto(EmailVerificationHistory history) {
        if (history == null) {
            return null;
        }

        VerifyEmailResponse response = new VerifyEmailResponse();
        response.setEmail(history.getEmail());
        response.setStatus(history.getStatus());
        response.setScore(history.getScore());
        response.setConfidence(history.getConfidence());
        response.setRecommendation(history.getRecommendation());
        response.setDetails(history.getDetails());

        VerifyEmailResponse.Checks checks = new VerifyEmailResponse.Checks();
        checks.setSyntax_check(history.isSyntaxCheck());
        checks.setMx_check(history.isMxCheck());
        checks.setDisposable_check(!history.isDisposableCheck()); // Response shows if it's "good"
        checks.setRole_based_check(!history.isRoleBasedCheck());
        checks.setBlacklist_check(!history.isBlacklistCheck());
        checks.setCatch_all_check(!history.isCatchAllCheck());
        checks.setSmtp_check(history.getSmtpCheckStatus() != EmailVerificationHistory.SmtpCheckStatus.NOT_PERFORMED);
        checks.setSmtp_ping(history.getSmtpPingStatus() == EmailVerificationHistory.SmtpPingStatus.SUCCESS);
        response.setChecks(checks);

        VerifyEmailResponse.Smtp smtp = new VerifyEmailResponse.Smtp();
        smtp.setSmtp_check_status(history.getSmtpCheckStatus());
        smtp.setSmtp_ping_status(history.getSmtpPingStatus());
        smtp.set_catch_all(history.isCatchAll());
        smtp.set_greylisted(history.isGreylisted());
        smtp.setSmtp_logs(history.getSmtpLogs());
        response.setSmtp(smtp);

        return response;
    }
}