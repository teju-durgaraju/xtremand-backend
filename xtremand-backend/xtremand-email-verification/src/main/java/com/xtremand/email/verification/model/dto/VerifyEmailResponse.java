package com.xtremand.email.verification.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.xtremand.domain.entity.EmailVerificationHistory;
import com.xtremand.email.verification.model.VerificationResult;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerifyEmailResponse {

    private String email;
    private EmailVerificationHistory.VerificationStatus status;
    private int score;
    private EmailVerificationHistory.Confidence confidence;
    private EmailVerificationHistory.Recommendation recommendation;
    private Checks checks;
    private Smtp smtp;
    private Map<String, Object> details;

    @Getter
    @Setter
    public static class Checks {
        private boolean syntax_check;
        private boolean mx_check;
        private boolean disposable_check;
        private boolean role_based_check;
        private boolean blacklist_check;
        private boolean catch_all_check;
        private boolean smtp_check;
        private boolean smtp_ping;
    }

    @Getter
    @Setter
    public static class Smtp {
        private EmailVerificationHistory.SmtpCheckStatus smtp_check_status;
        private EmailVerificationHistory.SmtpPingStatus smtp_ping_status;
        private boolean is_catch_all;
        private boolean is_greylisted;
        private String smtp_logs;
    }

    public static VerifyEmailResponse fromVerificationResult(VerificationResult result) {
        VerifyEmailResponse response = new VerifyEmailResponse();
        response.setEmail(result.getEmail());
        response.setStatus(result.getStatus());
        response.setScore(result.getScore());
        response.setConfidence(result.getConfidence());
        response.setRecommendation(result.getRecommendation());

        Checks checks = new Checks();
        checks.setSyntax_check(result.isSyntaxCheck());
        checks.setMx_check(result.isMxCheck());
        checks.setDisposable_check(result.isDisposableCheck());
        checks.setRole_based_check(result.isRoleBasedCheck());
        checks.setBlacklist_check(result.isBlacklistCheck());
        checks.setCatch_all_check(result.isCatchAllCheck());

        Smtp smtp = new Smtp();
        if (result.getSmtpProbeResult() != null) {
            checks.setSmtp_check(result.getSmtpProbeResult().getStatus() != EmailVerificationHistory.SmtpCheckStatus.NOT_PERFORMED);
            checks.setSmtp_ping(result.getSmtpProbeResult().getPingStatus() == EmailVerificationHistory.SmtpPingStatus.SUCCESS);
            smtp.setSmtp_check_status(result.getSmtpProbeResult().getStatus());
            smtp.setSmtp_ping_status(result.getSmtpProbeResult().getPingStatus());
            smtp.set_catch_all(result.getSmtpProbeResult().isCatchAll());
            smtp.set_greylisted(result.getSmtpProbeResult().isGreylisted());
            smtp.setSmtp_logs(result.getSmtpProbeResult().getLogs());
            response.setSmtp(smtp);
        }

        response.setChecks(checks);
        response.setDetails(result.getDetails());

        return response;
    }
}