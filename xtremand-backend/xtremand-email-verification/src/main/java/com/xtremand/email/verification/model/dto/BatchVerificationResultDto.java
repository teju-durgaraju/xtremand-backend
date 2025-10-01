package com.xtremand.email.verification.model.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class BatchVerificationResultDto {
    private UUID batchId;
    private int totalEmails;
    private int validEmails;
    private int invalidEmails;
    private int deliverableEmails;
    private int disposableEmails;
    private BigDecimal validRate;
}