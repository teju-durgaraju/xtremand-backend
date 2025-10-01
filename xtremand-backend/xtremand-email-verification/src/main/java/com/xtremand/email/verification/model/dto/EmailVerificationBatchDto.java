package com.xtremand.email.verification.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class EmailVerificationBatchDto {
    private UUID batchId;
    private Long userId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private ZonedDateTime createdAt;

    private int totalEmails;
    private int validEmails;
    private int invalidEmails;
    private int deliverableEmails;
    private int disposableEmails;
    private BigDecimal validRate;
}