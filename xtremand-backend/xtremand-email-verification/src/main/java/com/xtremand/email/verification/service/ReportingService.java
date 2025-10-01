package com.xtremand.email.verification.service;

import com.xtremand.email.verification.model.dto.DistinctEmailVerificationResultDto;
import com.xtremand.email.verification.model.dto.EmailVerificationBatchDto;
import com.xtremand.email.verification.model.dto.VerifyEmailResponse;
import com.xtremand.email.verification.model.mapper.BatchResultMapper;
import com.xtremand.email.verification.model.mapper.EmailVerificationMapper;
import com.xtremand.email.verification.repository.EmailVerificationBatchRepository;
import com.xtremand.email.verification.repository.EmailVerificationHistoryRepository;
import com.xtremand.email.verification.repository.EmailVerificationHistoryRepository.DistinctLatestVerificationProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportingService {

    private final EmailVerificationBatchRepository batchRepository;
    private final EmailVerificationHistoryRepository historyRepository;
    private final BatchResultMapper batchResultMapper;
    private final EmailVerificationMapper emailVerificationMapper;

    public Page<EmailVerificationBatchDto> getBatchSummaries(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return batchRepository.findByUser_Email(userEmail, pageable)
                .map(batchResultMapper::toBatchDto);
    }

    public Page<VerifyEmailResponse> getBatchEmails(UUID batchId, Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        // First, verify that the batch belongs to the user to prevent data leakage
        batchRepository.findByIdAndUser_Email(batchId, userEmail)
                .orElseThrow(() -> new AccessDeniedException("Access denied to batch " + batchId));

        return historyRepository.findByBatchIdAndUser_Email(batchId, userEmail, pageable)
                .map(emailVerificationMapper::toDto);
    }

    public List<DistinctEmailVerificationResultDto> getDistinctLatestResults() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        List<DistinctLatestVerificationProjection> projections = historyRepository.findDistinctLatest(userEmail);
        return projections.stream()
                .map(batchResultMapper::toDistinctDto)
                .collect(Collectors.toList());
    }
}