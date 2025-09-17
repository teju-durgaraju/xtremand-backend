package com.xtremand.email.verification.service;

import com.xtremand.domain.entity.EmailVerificationChartHistory;
import com.xtremand.domain.enums.AggregationType;
import com.xtremand.domain.enums.VerificationStatus;
import com.xtremand.email.verification.dto.ChartData;
import com.xtremand.email.verification.repository.EmailVerificationChartHistoryRepository;
import com.xtremand.email.verification.repository.UserEmailVerificationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChartService {

    private final EmailVerificationChartHistoryRepository chartHistoryRepository;
    private final UserEmailVerificationHistoryRepository userHistoryRepository;

    public List<ChartData> getWeeklyPerformance() {
        return getChartData(AggregationType.WEEKLY);
    }

    public List<ChartData> getMonthlyPerformance() {
        return getChartData(AggregationType.MONTHLY);
    }

    public List<ChartData> getYearlyPerformance() {
        return getChartData(AggregationType.YEARLY);
    }

    private List<ChartData> getChartData(AggregationType aggregationType) {
        List<EmailVerificationChartHistory> history = chartHistoryRepository.findByAggregationType(aggregationType);
        if (history.isEmpty()) {
            return calculateAndSaveChartData(aggregationType);
        }
        return history.stream().map(this::toChartData).collect(Collectors.toList());
    }

    private List<ChartData> calculateAndSaveChartData(AggregationType aggregationType) {
        List<ChartData> chartDataList = new ArrayList<>();
        if (aggregationType == AggregationType.WEEKLY) {
            LocalDate today = LocalDate.now();
            for (int i = 0; i < 4; i++) {
                LocalDate start = today.minusWeeks(i).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate end = today.minusWeeks(i).with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                chartDataList.add(getChartDataForPeriod("Week " + (4 - i), start.atStartOfDay(), end.atTime(23, 59, 59)));
            }
        }
        // Similar logic for MONTHLY and YEARLY
        return chartDataList;
    }

    private ChartData getChartDataForPeriod(String period, LocalDateTime start, LocalDateTime end) {
        long valid = userHistoryRepository.countByStatusAndCheckedAtBetween(VerificationStatus.VALID, start, end);
        long invalid = userHistoryRepository.countByStatusAndCheckedAtBetween(VerificationStatus.INVALID, start, end);
        long risky = userHistoryRepository.countByStatusAndCheckedAtBetween(VerificationStatus.RISKY, start, end);
        long disposable = userHistoryRepository.countByStatusAndCheckedAtBetween(VerificationStatus.DISPOSABLE, start, end);
        long unknown = userHistoryRepository.countByStatusAndCheckedAtBetween(VerificationStatus.UNKNOWN, start, end);
        long total = userHistoryRepository.countByCheckedAtBetween(start, end);

        return ChartData.builder()
                .period(period)
                .verified((int) valid)
                .deliverable((int) (valid + risky))
                .risky((int) risky)
                .invalid((int) invalid)
                .unknown((int) (unknown + disposable))
                .total((int) total)
                .build();
    }

    private ChartData toChartData(EmailVerificationChartHistory history) {
        return ChartData.builder()
                .period(history.getPeriod())
                .verified(history.getVerified())
                .deliverable(history.getDeliverable())
                .risky(history.getRisky())
                .invalid(history.getInvalid())
                .unknown(history.getUnknown())
                .total(history.getTotal())
                .build();
    }
}
