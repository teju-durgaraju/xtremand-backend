package com.xtremand.email.verification.model.dto.chart;

public record ChartDataDto(
        String period,
        long verified,
        long deliverable,
        long risky,
        long invalid,
        long unknown,
        long total
) {
}