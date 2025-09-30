package com.xtremand.email.verification.service;

import com.xtremand.domain.entity.EmailVerificationBatch;
import com.xtremand.domain.entity.EmailVerificationHistory;
import com.xtremand.email.verification.model.dto.DistinctEmailVerificationResultDto;
import com.xtremand.email.verification.model.dto.EmailVerificationBatchDto;
import com.xtremand.email.verification.model.dto.VerifyEmailResponse;
import com.xtremand.email.verification.model.mapper.BatchResultMapper;
import com.xtremand.email.verification.model.mapper.EmailVerificationMapper;
import com.xtremand.email.verification.repository.EmailVerificationBatchRepository;
import com.xtremand.email.verification.repository.EmailVerificationHistoryRepository;
import com.xtremand.email.verification.repository.EmailVerificationHistoryRepository.DistinctLatestVerificationProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

    @Mock
    private EmailVerificationBatchRepository batchRepository;
    @Mock
    private EmailVerificationHistoryRepository historyRepository;
    @Mock
    private BatchResultMapper batchResultMapper;
    @Mock
    private EmailVerificationMapper emailVerificationMapper;

    @InjectMocks
    private ReportingService reportingService;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getBatchSummaries_shouldReturnPageOfBatchDtos() {
        // Given
        EmailVerificationBatch batch = new EmailVerificationBatch();
        Page<EmailVerificationBatch> page = new PageImpl<>(Collections.singletonList(batch));
        EmailVerificationBatchDto dto = EmailVerificationBatchDto.builder().build();

        when(batchRepository.findAll(pageable)).thenReturn(page);
        when(batchResultMapper.toBatchDto(any(EmailVerificationBatch.class))).thenReturn(dto);

        // When
        Page<EmailVerificationBatchDto> result = reportingService.getBatchSummaries(pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().get(0));
    }

    @Test
    void getBatchEmails_shouldReturnPageOfEmailResponseDtos() {
        // Given
        UUID batchId = UUID.randomUUID();
        EmailVerificationHistory history = new EmailVerificationHistory();
        Page<EmailVerificationHistory> page = new PageImpl<>(Collections.singletonList(history));
        VerifyEmailResponse dto = new VerifyEmailResponse();

        when(historyRepository.findByBatch_Id(batchId, pageable)).thenReturn(page);
        when(emailVerificationMapper.toDto(any(EmailVerificationHistory.class))).thenReturn(dto);

        // When
        Page<VerifyEmailResponse> result = reportingService.getBatchEmails(batchId, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().get(0));
    }

    @Test
    void getDistinctLatestResults_shouldReturnListOfDistinctDtos() {
        // Given
        DistinctLatestVerificationProjection projection = mock(DistinctLatestVerificationProjection.class);
        List<DistinctLatestVerificationProjection> projections = Collections.singletonList(projection);
        DistinctEmailVerificationResultDto dto = DistinctEmailVerificationResultDto.builder().build();

        when(historyRepository.findDistinctLatest()).thenReturn(projections);
        when(batchResultMapper.toDistinctDto(any(DistinctLatestVerificationProjection.class))).thenReturn(dto);

        // When
        List<DistinctEmailVerificationResultDto> result = reportingService.getDistinctLatestResults();

        // Then
        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
    }
}