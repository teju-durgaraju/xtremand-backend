package com.xtremand.email.verification.model.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AccountKpi {
    private long validEmails;
    private long invalidEmails;
    private long riskyEmails;
    private long unknownEmails;
    private long totalProcessed;
    private BigDecimal qualityScore;
    private BigDecimal deliverabilityRate;
    private BigDecimal bounceRate;
}