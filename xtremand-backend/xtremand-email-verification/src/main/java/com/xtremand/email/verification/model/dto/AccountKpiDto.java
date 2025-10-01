package com.xtremand.email.verification.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountKpiDto {
    private AccountKpi accountKpi;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountKpi {
        private long validEmails;
        private long invalidEmails;
        private long riskyEmails;
        private long unknownEmails;
        private long totalProcessed;
        private double qualityScore;
        private double deliverabilityRate;
        private double bounceRate;
    }
}