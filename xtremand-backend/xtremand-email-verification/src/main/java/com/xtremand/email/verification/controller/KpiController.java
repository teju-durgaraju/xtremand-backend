package com.xtremand.email.verification.controller;

import com.xtremand.email.verification.model.dto.KpiResponse;
import com.xtremand.email.verification.service.KpiAggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/email/kpi")
@RequiredArgsConstructor
public class KpiController {

    private final KpiAggregationService kpiAggregationService;

    @GetMapping("/account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<KpiResponse> getAccountKpis() {
        KpiResponse response = kpiAggregationService.getAccountKpis();
        return ResponseEntity.ok(response);
    }
}