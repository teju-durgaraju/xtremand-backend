package com.xtremand.email.verification.service;

import com.xtremand.domain.entity.User;
import com.xtremand.email.verification.model.ChartDropdownRange;
import com.xtremand.email.verification.model.ChartDropdownResponse;
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
import java.util.List;
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
            // This case should ideally not happen for an authenticated user.
            // Return a default or empty list.
            return List.of(new ChartDropdownResponse(
                ChartDropdownRange.LAST_1_MONTH.getLabel(),
                ChartDropdownRange.LAST_1_MONTH.getValue()
            ));
        }

        Long userId = userOpt.get().getId();
        Optional<EmailVerificationHistoryRepository.DateRangeProjection> dateRangeOpt = historyRepository.findDateRangeByUserId(userId);

        // If no history exists or only one entry, default to "Last 1 Month".
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

        // Calculate the total months between the earliest and latest records using ChronoUnit for better accuracy.
        long monthsOfData = ChronoUnit.MONTHS.between(earliestZoned, latestZoned);

        List<ChartDropdownResponse> ranges = new ArrayList<>();

        // Always include "Last 1 Month" if there's any data.
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
}