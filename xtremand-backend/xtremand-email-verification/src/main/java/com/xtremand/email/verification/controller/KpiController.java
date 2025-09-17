package com.xtremand.email.verification.controller;

import com.xtremand.domain.entity.EmailVerificationKpi;
import com.xtremand.email.verification.dto.ChartData;
import com.xtremand.email.verification.dto.KpiResponse;
import com.xtremand.email.verification.dto.MonthlyKpi;
import com.xtremand.email.verification.service.ChartService;
import com.xtremand.email.verification.service.KpiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/email/kpi")
@Tag(name = "Email Verification KPI", description = "Operations related to email verification KPIs.")
@RequiredArgsConstructor
public class KpiController {

    private final KpiService kpiService;
    private final ChartService chartService;

    @GetMapping
    @Operation(summary = "Get consolidated KPI", responses = {
            @ApiResponse(responseCode = "200", description = "Consolidated KPI data")})
    public KpiResponse getConsolidatedKpi() {
        MonthlyKpi currentMonthKpi = kpiService.getCurrentMonthKpi();
        MonthlyKpi previousMonthKpi = kpiService.getPreviousMonthKpi();
        return new KpiResponse(currentMonthKpi, previousMonthKpi, kpiService.calculateTrends(currentMonthKpi, previousMonthKpi));
    }

    @GetMapping("/history")
    @Operation(summary = "Get KPI history", responses = {
            @ApiResponse(responseCode = "200", description = "KPI history data")})
    public List<EmailVerificationKpi> getKpiHistory(@RequestParam(defaultValue = "6") int months) {
        return kpiService.getKpiHistory(months);
    }

    @GetMapping("/quality-score")
    public List<ChartData> getQualityScore(@RequestParam(defaultValue = "12") int months) {
        return chartService.getMonthlyPerformance();
    }

    @GetMapping("/deliverability")
    public List<ChartData> getDeliverability(@RequestParam(defaultValue = "12") int months) {
        return chartService.getMonthlyPerformance();
    }

    @GetMapping("/bounce-rate")
    public List<ChartData> getBounceRate(@RequestParam(defaultValue = "12") int months) {
        return chartService.getMonthlyPerformance();
    }

    @GetMapping("/verification-counts")
    public List<ChartData> getVerificationCounts(@RequestParam(defaultValue = "12") int months) {
        return chartService.getMonthlyPerformance();
    }

    @GetMapping("/performance/weekly")
    public List<ChartData> getWeeklyPerformance() {
        return chartService.getWeeklyPerformance();
    }
}
