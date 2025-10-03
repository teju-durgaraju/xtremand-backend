package com.xtremand.email.verification.service;

import com.xtremand.domain.entity.User;
import com.xtremand.email.verification.model.ChartDropdownRange;
import com.xtremand.email.verification.model.ChartDropdownResponse;
import com.xtremand.email.verification.model.dto.chart.ChartDataDto;
import com.xtremand.email.verification.model.dto.chart.ChartDataResponseDto;
import com.xtremand.email.verification.model.dto.chart.ChartRange;
import com.xtremand.email.verification.repository.EmailVerificationHistoryRepository;
import com.xtremand.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailVerificationChartService {

    private final EmailVerificationHistoryRepository historyRepository;
    private final UserRepository userRepository;

    public List<ChartDropdownResponse> getAvailableDropdownRanges() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Collections.emptyList();
        }
        String userEmail = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(userEmail);

        if (userOpt.isEmpty()) {
            return List.of(new ChartDropdownResponse(
                ChartDropdownRange.LAST_1_MONTH.getLabel(),
                ChartDropdownRange.LAST_1_MONTH.getValue()
            ));
        }

        Long userId = userOpt.get().getId();
        Optional<EmailVerificationHistoryRepository.DateRangeProjection> dateRangeOpt = historyRepository.findDateRangeByUserId(userId);

        if (dateRangeOpt.isEmpty() || dateRangeOpt.get().getEarliest() == null || dateRangeOpt.get().getLatest() == null) {
            return List.of(new ChartDropdownResponse(
                ChartDropdownRange.LAST_1_MONTH.getLabel(),
                ChartDropdownRange.LAST_1_MONTH.getValue()
            ));
        }

        Instant earliestInstant = dateRangeOpt.get().getEarliest();
        Instant latestInstant = dateRangeOpt.get().getLatest();

        ZonedDateTime earliestZoned = earliestInstant.atZone(ZoneId.systemDefault());
        ZonedDateTime latestZoned = latestInstant.atZone(ZoneId.systemDefault());

        long monthsOfData = ChronoUnit.MONTHS.between(earliestZoned, latestZoned);

        List<ChartDropdownResponse> ranges = new ArrayList<>();
        ranges.add(new ChartDropdownResponse(ChartDropdownRange.LAST_1_MONTH.getLabel(), ChartDropdownRange.LAST_1_MONTH.getValue()));

        if (monthsOfData >= 3) {
            ranges.add(new ChartDropdownResponse(ChartDropdownRange.LAST_3_MONTHS.getLabel(), ChartDropdownRange.LAST_3_MONTHS.getValue()));
        }
        if (monthsOfData >= 6) {
            ranges.add(new ChartDropdownResponse(ChartDropdownRange.LAST_6_MONTHS.getLabel(), ChartDropdownRange.LAST_6_MONTHS.getValue()));
        }
        if (monthsOfData >= 12) {
            ranges.add(new ChartDropdownResponse(ChartDropdownRange.LAST_1_YEAR.getLabel(), ChartDropdownRange.LAST_1_YEAR.getValue()));
        }
        if (monthsOfData >= 36) {
            ranges.add(new ChartDropdownResponse(ChartDropdownRange.LAST_3_YEARS.getLabel(), ChartDropdownRange.LAST_3_YEARS.getValue()));
        }
        if (monthsOfData >= 60) {
            ranges.add(new ChartDropdownResponse(ChartDropdownRange.LAST_5_YEARS.getLabel(), ChartDropdownRange.LAST_5_YEARS.getValue()));
        }

        return ranges;
    }

    public ChartDataResponseDto getChartData(ChartRange range) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        Instant startDate = calculateStartDate(range);
        String groupBy = getGroupByClause(range);

        List<EmailVerificationHistoryRepository.ChartDataProjection> projections =
                historyRepository.findChartDataByUserEmail(userEmail, startDate, groupBy);

        Map<String, AggregatedData> aggregatedDataMap = processProjections(projections, range);

        List<ChartDataDto> chartData = formatChartData(aggregatedDataMap, range);

        return new ChartDataResponseDto(chartData);
    }

    private Instant calculateStartDate(ChartRange range) {
        Instant now = Instant.now();
        return switch (range) {
            case M1 -> now.minus(30, ChronoUnit.DAYS);
            case M3 -> now.minus(90, ChronoUnit.DAYS);
            case M6 -> now.minus(180, ChronoUnit.DAYS);
            case Y1 -> now.minus(365, ChronoUnit.DAYS);
            case Y3 -> now.minus(3 * 365, ChronoUnit.DAYS);
            case Y5 -> now.minus(5 * 365, ChronoUnit.DAYS);
        };
    }

    private String getGroupByClause(ChartRange range) {
        return switch (range) {
            case M1, M3 -> "IYYY-IW"; // Group by week
            case M6, Y1, Y3, Y5 -> "YYYY-MM"; // Group by month
        };
    }

    private Map<String, AggregatedData> processProjections(List<EmailVerificationHistoryRepository.ChartDataProjection> projections, ChartRange range) {
        Map<String, AggregatedData> aggregatedDataMap = new LinkedHashMap<>();
        for (EmailVerificationHistoryRepository.ChartDataProjection p : projections) {
            AggregatedData data = aggregatedDataMap.computeIfAbsent(p.getPeriod(), k -> new AggregatedData());
            long count = p.getCount();
            data.verified += count;
            switch (p.getStatus()) {
                case "VALID" -> data.deliverable += count;
                case "RISKY" -> data.risky += count;
                case "INVALID" -> data.invalid += count;
                case "UNKNOWN" -> data.unknown += count;
            }
        }
        return aggregatedDataMap;
    }

    private List<ChartDataDto> formatChartData(Map<String, AggregatedData> aggregatedDataMap, ChartRange range) {
        List<ChartDataDto> chartData = new ArrayList<>();
        int weekCounter = 1;
        for (Map.Entry<String, AggregatedData> entry : aggregatedDataMap.entrySet()) {
            String periodLabel;
            if (range == ChartRange.M1 || range == ChartRange.M3) {
                periodLabel = "Week " + weekCounter++;
            } else {
                String[] parts = entry.getKey().split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                java.time.YearMonth ym = java.time.YearMonth.of(year, month);
                periodLabel = ym.format(java.time.format.DateTimeFormatter.ofPattern("MMM yy"));
            }

            AggregatedData data = entry.getValue();
            chartData.add(new ChartDataDto(
                    periodLabel,
                    data.verified,
                    data.deliverable,
                    data.risky,
                    data.invalid,
                    data.unknown,
                    data.verified // total is same as verified
            ));
        }
        return chartData;
    }


    private static class AggregatedData {
        long verified = 0;
        long deliverable = 0;
        long risky = 0;
        long invalid = 0;
        long unknown = 0;
    }
}