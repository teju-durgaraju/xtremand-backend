package com.xtremand.email.verification.controller;

import com.xtremand.domain.entity.EmailVerificationBatch;
import com.xtremand.email.verification.model.VerificationResult;
import com.xtremand.email.verification.model.dto.AccountKpiDto;
import com.xtremand.email.verification.model.dto.BatchVerificationResultDto;
import com.xtremand.email.verification.model.dto.DistinctEmailVerificationResultDto;
import com.xtremand.email.verification.model.dto.EmailVerificationBatchDto;
import com.xtremand.email.verification.model.dto.VerifyBatchRequest;
import com.xtremand.email.verification.model.dto.VerifyEmailRequest;
import com.xtremand.email.verification.model.dto.VerifyEmailResponse;
import com.xtremand.email.verification.model.mapper.BatchResultMapper;
import com.xtremand.email.verification.model.mapper.EmailVerificationMapper;
import com.xtremand.email.verification.service.BatchVerificationService;
import com.xtremand.email.verification.service.EmailVerificationService;
import com.xtremand.email.verification.service.KpiAggregationService;
import com.xtremand.email.verification.service.ReportingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/email/verify")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;
    private final BatchVerificationService batchVerificationService;
    private final ReportingService reportingService;
    private final KpiAggregationService kpiAggregationService;
    private final EmailVerificationMapper emailVerificationMapper;
    private final BatchResultMapper batchResultMapper;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VerifyEmailResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        VerificationResult result = emailVerificationService.verifyEmail(request.getEmail());
        return ResponseEntity.ok(VerifyEmailResponse.fromVerificationResult(result));
    }

    @PostMapping("/batch")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BatchVerificationResultDto> verifyBatch(@Valid @RequestBody VerifyBatchRequest request) {
        EmailVerificationBatch batch = batchVerificationService.startBatchVerification(request.getEmails());
        BatchVerificationResultDto responseDto = batchResultMapper.toBatchResultDto(batch);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseDto);
    }

    @GetMapping("/batch")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<EmailVerificationBatchDto>> getBatchSummaries(Pageable pageable) {
        Page<EmailVerificationBatchDto> summaries = reportingService.getBatchSummaries(pageable);
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/batch/{batchId}/emails")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<VerifyEmailResponse>> getBatchEmails(@PathVariable UUID batchId, Pageable pageable) {
        Page<VerifyEmailResponse> emails = reportingService.getBatchEmails(batchId, pageable);
        return ResponseEntity.ok(emails);
    }

    @GetMapping("/distinct-latest")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DistinctEmailVerificationResultDto>> getDistinctLatestResults() {
        List<DistinctEmailVerificationResultDto> results = reportingService.getDistinctLatestResults();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/kpi")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AccountKpiDto> getAccountKpi() {
        AccountKpiDto kpis = kpiAggregationService.getAccountKpis();
        return ResponseEntity.ok(kpis);
    }
}