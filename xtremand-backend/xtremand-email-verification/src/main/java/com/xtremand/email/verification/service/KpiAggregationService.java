package com.xtremand.email.verification.service;

import com.xtremand.email.verification.model.dto.AccountKpi;
import com.xtremand.email.verification.model.dto.KpiResponse;
import com.xtremand.email.verification.repository.EmailVerificationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class KpiAggregationService {

    private final EmailVerificationHistoryRepository repository;

    @Transactional(readOnly = true)
    public KpiResponse getAccountKpis() {
        return repository.getAccountKpis()
                .map(this::buildSuccessResponse)
                .orElse(buildDefaultResponse());
    }

    private KpiResponse buildSuccessResponse(EmailVerificationHistoryRepository.KpiQueryResult result) {
        long totalProcessed = result.getTotalProcessed();
        long validEmails = result.getValidEmails();
        long invalidEmails = result.getInvalidEmails();

        BigDecimal deliverabilityRate = BigDecimal.ZERO;
        BigDecimal bounceRate = BigDecimal.ZERO;

        if (totalProcessed > 0) {
            deliverabilityRate = BigDecimal.valueOf(validEmails)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalProcessed), 2, RoundingMode.HALF_UP);
            bounceRate = BigDecimal.valueOf(invalidEmails)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalProcessed), 2, RoundingMode.HALF_UP);
        }

        AccountKpi accountKpi = AccountKpi.builder()
                .validEmails(validEmails)
                .invalidEmails(invalidEmails)
                .riskyEmails(result.getRiskyEmails())
                .unknownEmails(result.getUnknownEmails())
                .totalProcessed(totalProcessed)
                .qualityScore(result.getQualityScore().setScale(2, RoundingMode.HALF_UP))
                .deliverabilityRate(deliverabilityRate)
                .bounceRate(bounceRate)
                .build();

        return KpiResponse.builder().account(accountKpi).build();
    }

    private KpiResponse buildDefaultResponse() {
        AccountKpi defaultKpi = AccountKpi.builder()
                .validEmails(0)
                .invalidEmails(0)
                .riskyEmails(0)
                .unknownEmails(0)
                .totalProcessed(0)
                .qualityScore(BigDecimal.ZERO.setScale(2))
                .deliverabilityRate(BigDecimal.ZERO.setScale(2))
                .bounceRate(BigDecimal.ZERO.setScale(2))
                .build();
        return KpiResponse.builder().account(defaultKpi).build();
    }
}