package com.xtremand.email.verification.service;

import com.xtremand.email.verification.model.dto.AccountKpiDto;
import com.xtremand.email.verification.repository.EmailVerificationHistoryRepository;
import com.xtremand.email.verification.security.UserIdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class KpiAggregationService {

    private final EmailVerificationHistoryRepository repository;
    private final UserIdentityService userIdentityService;

    @Transactional(readOnly = true)
    public AccountKpiDto getAccountKpis() {
        Long userId = userIdentityService.getRequiredUserId();
        return repository.getAccountKpis(userId)
                .map(this::buildSuccessResponse)
                .orElse(buildDefaultResponse());
    }

    private AccountKpiDto buildSuccessResponse(EmailVerificationHistoryRepository.KpiQueryResult result) {
        long totalProcessed = result.getTotalProcessed();
        long validEmails = result.getValidEmails();
        long invalidEmails = result.getInvalidEmails();

        double deliverabilityRate = 0.0;
        double bounceRate = 0.0;
        double qualityScore = 0.0;

        if (totalProcessed > 0) {
            deliverabilityRate = BigDecimal.valueOf(validEmails)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalProcessed), 2, RoundingMode.HALF_UP).doubleValue();
            bounceRate = BigDecimal.valueOf(invalidEmails)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalProcessed), 2, RoundingMode.HALF_UP).doubleValue();
            qualityScore = BigDecimal.valueOf(validEmails)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalProcessed), 2, RoundingMode.HALF_UP).doubleValue();
        }

        AccountKpiDto.AccountKpi accountKpi = AccountKpiDto.AccountKpi.builder()
                .validEmails(validEmails)
                .invalidEmails(invalidEmails)
                .riskyEmails(result.getRiskyEmails())
                .unknownEmails(result.getUnknownEmails())
                .totalProcessed(totalProcessed)
                .qualityScore(qualityScore)
                .deliverabilityRate(deliverabilityRate)
                .bounceRate(bounceRate)
                .build();

        return AccountKpiDto.builder().accountKpi(accountKpi).build();
    }

    private AccountKpiDto buildDefaultResponse() {
        AccountKpiDto.AccountKpi defaultKpi = AccountKpiDto.AccountKpi.builder()
                .validEmails(0)
                .invalidEmails(0)
                .riskyEmails(0)
                .unknownEmails(0)
                .totalProcessed(0)
                .qualityScore(0.0)
                .deliverabilityRate(0.0)
                .bounceRate(0.0)
                .build();
        return AccountKpiDto.builder().accountKpi(defaultKpi).build();
    }
}