package com.xtremand.email.verification.service;

import com.xtremand.email.verification.model.dto.AccountKpi;
import com.xtremand.email.verification.model.dto.KpiResponse;
import com.xtremand.email.verification.repository.EmailVerificationHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KpiAggregationServiceTest {

    @Mock
    private EmailVerificationHistoryRepository repository;

    @InjectMocks
    private KpiAggregationService kpiAggregationService;

    private EmailVerificationHistoryRepository.KpiQueryResult mockQueryResult;

    @BeforeEach
    void setUp() {
        mockQueryResult = new EmailVerificationHistoryRepository.KpiQueryResult() {
            @Override
            public long getValidEmails() {
                return 1247;
            }
            @Override
            public long getInvalidEmails() {
                return 234;
            }
            @Override
            public long getRiskyEmails() {
                return 89;
            }
            @Override
            public long getUnknownEmails() {
                return 156;
            }
            @Override
            public long getTotalProcessed() {
                return 1726;
            }
            @Override
            public BigDecimal getQualityScore() {
                return new BigDecimal("92.413");
            }
        };
    }

    @Test
    void getAccountKpis_shouldReturnCalculatedKpis_whenDataExists() {
        when(repository.getAccountKpis()).thenReturn(Optional.of(mockQueryResult));

        KpiResponse response = kpiAggregationService.getAccountKpis();
        AccountKpi accountKpi = response.getAccount();

        assertEquals(1247, accountKpi.getValidEmails());
        assertEquals(234, accountKpi.getInvalidEmails());
        assertEquals(89, accountKpi.getRiskyEmails());
        assertEquals(156, accountKpi.getUnknownEmails());
        assertEquals(1726, accountKpi.getTotalProcessed());
        assertEquals(new BigDecimal("92.41"), accountKpi.getQualityScore());
        assertEquals(new BigDecimal("72.25"), accountKpi.getDeliverabilityRate());
        assertEquals(new BigDecimal("13.56"), accountKpi.getBounceRate());
    }

    @Test
    void getAccountKpis_shouldReturnDefaultKpis_whenNoDataExists() {
        when(repository.getAccountKpis()).thenReturn(Optional.empty());

        KpiResponse response = kpiAggregationService.getAccountKpis();
        AccountKpi accountKpi = response.getAccount();

        assertEquals(0, accountKpi.getValidEmails());
        assertEquals(0, accountKpi.getInvalidEmails());
        assertEquals(0, accountKpi.getRiskyEmails());
        assertEquals(0, accountKpi.getUnknownEmails());
        assertEquals(0, accountKpi.getTotalProcessed());
        assertEquals(new BigDecimal("0.00"), accountKpi.getQualityScore());
        assertEquals(new BigDecimal("0.00"), accountKpi.getDeliverabilityRate());
        assertEquals(new BigDecimal("0.00"), accountKpi.getBounceRate());
    }
}