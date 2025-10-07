package com.xtremand.email.verification.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.email.verification.model.ChartDropdownResponse;
import com.xtremand.email.verification.model.dto.chart.ChartDataResponseDto;
import com.xtremand.email.verification.model.dto.chart.ChartRange;
import com.xtremand.email.verification.service.EmailVerificationChartService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/email/verify/chart")
@RequiredArgsConstructor
public class EmailVerificationChartController {

	private final EmailVerificationChartService chartService;

	@GetMapping("/dropdowns")
	public ResponseEntity<List<ChartDropdownResponse>> getDropdowns() {
		return ResponseEntity.ok(chartService.getAvailableDropdownRanges());
	}

	@GetMapping("/data")
	public ResponseEntity<ChartDataResponseDto> getChartData(@RequestParam("range") ChartRange range) {
		return ResponseEntity.ok(chartService.getChartData(range));
	}
}