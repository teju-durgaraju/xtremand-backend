package com.xtremand.email.verification.service;

import com.xtremand.domain.entity.EmailVerificationKpi;
import com.xtremand.domain.enums.VerificationStatus;
import com.xtremand.email.verification.dto.MonthlyKpi;
import com.xtremand.email.verification.dto.Trends;
import com.xtremand.email.verification.repository.EmailVerificationKpiRepository;
import com.xtremand.email.verification.repository.UserEmailVerificationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KpiService {

    private final EmailVerificationKpiRepository kpiRepository;
    private final UserEmailVerificationHistoryRepository historyRepository;

    public MonthlyKpi getCurrentMonthKpi() {
        YearMonth currentMonth = YearMonth.now();
        return getKpiForMonth(currentMonth);
    }

    public MonthlyKpi getPreviousMonthKpi() {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        return getKpiForMonth(previousMonth);
    }

    public Trends calculateTrends(MonthlyKpi current, MonthlyKpi previous) {
        if (current == null || previous == null) {
            return new Trends();
        }
        return Trends.builder()
                .qualityScoreChange(current.getQualityScore().subtract(previous.getQualityScore()))
                .deliverabilityRateChange(current.getDeliverabilityRate().subtract(previous.getDeliverabilityRate()))
                .bounceRateChange(current.getBounceRate().subtract(previous.getBounceRate()))
                .build();
    }

    public List<EmailVerificationKpi> getKpiHistory(int months) {
        return kpiRepository.findTopByOrderByMonthDesc(months);
    }

    private MonthlyKpi getKpiForMonth(YearMonth month) {
        String monthStr = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Optional<EmailVerificationKpi> kpiOpt = kpiRepository.findByMonth(monthStr);
        if (kpiOpt.isPresent()) {
            return toMonthlyKpi(kpiOpt.get());
        } else {
            LocalDateTime start = month.atDay(1).atStartOfDay();
            LocalDateTime end = month.atEndOfMonth().atTime(23, 59, 59);

            long valid = historyRepository.countByStatusAndCheckedAtBetween(VerificationStatus.VALID, start, end);
            long invalid = historyRepository.countByStatusAndCheckedAtBetween(VerificationStatus.INVALID, start, end);
            long risky = historyRepository.countByStatusAndCheckedAtBetween(VerificationStatus.RISKY, start, end);
            long disposable = historyRepository.countByStatusAndCheckedAtBetween(VerificationStatus.DISPOSABLE, start, end);
            long unknown = historyRepository.countByStatusAndCheckedAtBetween(VerificationStatus.UNKNOWN, start, end);
            long total = historyRepository.countByCheckedAtBetween(start, end);
            Double averageScore = historyRepository.getAverageScore(start, end);

            BigDecimal qualityScore = averageScore != null ? BigDecimal.valueOf(averageScore) : BigDecimal.ZERO;
            BigDecimal deliverabilityRate = total > 0 ? BigDecimal.valueOf(valid + risky).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
            BigDecimal bounceRate = total > 0 ? BigDecimal.valueOf(invalid).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

            EmailVerificationKpi kpi = EmailVerificationKpi.builder()
                    .month(monthStr)
                    .validEmails((int) valid)
                    .invalidEmails((int) invalid)
                    .riskyEmails((int) risky)
                    .unknownEmails((int) unknown)
                    .totalProcessed((int) total)
                    .qualityScore(qualityScore)
                    .deliverabilityRate(deliverabilityRate)
                    .bounceRate(bounceRate)
                    .build();
            kpiRepository.save(kpi);
            return toMonthlyKpi(kpi);
        }
    }

    private MonthlyKpi toMonthlyKpi(EmailVerificationKpi kpi) {
        return MonthlyKpi.builder()
                .month(kpi.getMonth())
                .validEmails(kpi.getValidEmails())
                .invalidEmails(kpi.getInvalidEmails())
                .riskyEmails(kpi.getRiskyEmails())
                .unknownEmails(kpi.getUnknownEmails())
                .totalProcessed(kpi.getTotalProcessed())
                .qualityScore(kpi.getQualityScore())
                .deliverabilityRate(kpi.getDeliverabilityRate())
                .bounceRate(kpi.getBounceRate())
                .build();
    }
}
