package com.xtremand.email.verification.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xtremand.domain.entity.EmailVerificationHistory;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class DistinctEmailVerificationResultDto {

    private String email;
    private String domain;
    private EmailVerificationHistory.VerificationStatus status;
    private int score;
    private EmailVerificationHistory.Confidence confidence;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private ZonedDateTime lastVerifiedAt;

    private VerificationChecksDto checks;

    @Data
    @Builder
    public static class VerificationChecksDto {
        private boolean syntax_check;
        private boolean mx_check;
        private boolean disposable_check;
        private boolean role_based_check;
        private boolean catch_all_check;
        private boolean blacklist_check;
        private boolean smtp_check;
        private boolean smtp_ping;
    }
}