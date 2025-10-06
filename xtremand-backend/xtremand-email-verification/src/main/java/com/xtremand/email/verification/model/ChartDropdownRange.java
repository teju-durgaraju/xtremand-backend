package com.xtremand.email.verification.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChartDropdownRange {
    LAST_1_MONTH("Last 1 Month", "1M", 1),
    LAST_3_MONTHS("Last 3 Months", "3M", 3),
    LAST_6_MONTHS("Last 6 Months", "6M", 6),
    LAST_1_YEAR("Last 1 Year", "1Y", 12),
    LAST_3_YEARS("Last 3 Years", "3Y", 36),
    LAST_5_YEARS("Last 5 Years", "5Y", 60);

    private final String label;
    private final String value;
    private final int months;
}