package com.xtremand.email.verification.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MonthlyKpi {
    String month;
    int validEmails;
    int invalidEmails;
    int riskyEmails;
    int unknownEmails;
    int totalProcessed;
    BigDecimal qualityScore;
    BigDecimal deliverabilityRate;
    BigDecimal bounceRate;
}
