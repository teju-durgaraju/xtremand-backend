package com.xtremand.email.verification.controller;

import com.xtremand.email.verification.model.ChartDropdownResponse;
import com.xtremand.email.verification.service.EmailVerificationChartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/email/verify/chart")
@RequiredArgsConstructor
public class EmailVerificationChartController {

    private final EmailVerificationChartService chartService;

    @GetMapping("/dropdowns")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChartDropdownResponse>> getDropdowns() {
        return ResponseEntity.ok(chartService.getAvailableDropdownRanges());
    }
}