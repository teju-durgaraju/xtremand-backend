package com.xtremand.emailverification.controller;

import com.xtremand.emailverification.model.VerificationResult;
import com.xtremand.emailverification.model.dto.VerifyBatchRequest;
import com.xtremand.emailverification.model.dto.VerifyEmailRequest;
import com.xtremand.emailverification.model.dto.VerifyEmailResponse;
import com.xtremand.emailverification.model.mapper.EmailVerificationMapper;
import com.xtremand.emailverification.repository.EmailVerificationHistoryRepository;
import com.xtremand.emailverification.service.BatchVerificationService;
import com.xtremand.emailverification.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/email/verify")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;
    private final BatchVerificationService batchVerificationService;
    private final EmailVerificationHistoryRepository historyRepository;
    private final EmailVerificationMapper mapper;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VerifyEmailResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        VerificationResult result = emailVerificationService.verifyEmail(request.getEmail(), request.getUserId());
        VerifyEmailResponse response = VerifyEmailResponse.fromVerificationResult(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/results/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VerifyEmailResponse> getVerificationResult(@PathVariable Long id) {
        return historyRepository.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/batch")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> verifyBatch(@Valid @RequestBody VerifyBatchRequest request) {
        batchVerificationService.startBatchVerification(request.getEmails(), request.getUserId());
        return ResponseEntity.accepted().build();
    }
}