package com.xtremand.email.verification.service;

import com.xtremand.email.verification.model.dto.DistinctEmailVerificationResultDto;
import com.xtremand.email.verification.model.dto.EmailVerificationBatchDto;
import com.xtremand.email.verification.model.mapper.BatchResultMapper;
import com.xtremand.email.verification.model.mapper.EmailVerificationMapper;
import com.xtremand.email.verification.repository.EmailVerificationBatchRepository;
import com.xtremand.email.verification.repository.EmailVerificationHistoryRepository;
import com.xtremand.email.verification.repository.EmailVerificationHistoryRepository.DistinctLatestVerificationProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        return batchRepository.findAll(pageable)
                .map(batchResultMapper::toBatchDto);
    }

    public Page<com.xtremand.email.verification.model.dto.VerifyEmailResponse> getBatchEmails(UUID batchId, Pageable pageable) {
        return historyRepository.findByBatch_Id(batchId, pageable)
                .map(emailVerificationMapper::toDto);
    }

    public List<DistinctEmailVerificationResultDto> getDistinctLatestResults() {
        List<DistinctLatestVerificationProjection> projections = historyRepository.findDistinctLatest();
        return projections.stream()
                .map(batchResultMapper::toDistinctDto)
                .collect(Collectors.toList());
    }
}