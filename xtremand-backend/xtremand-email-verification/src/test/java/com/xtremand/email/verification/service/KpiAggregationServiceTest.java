package com.xtremand.email.verification.service;

import com.xtremand.email.verification.model.dto.AccountKpiDto;
import com.xtremand.email.verification.repository.EmailVerificationHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KpiAggregationServiceTest {

    @Mock
    private EmailVerificationHistoryRepository repository;

    @InjectMocks
    private KpiAggregationService kpiAggregationService;

    private EmailVerificationHistoryRepository.KpiQueryResult mockQueryResult;

    @Mock
    private Authentication authentication;

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
                return BigDecimal.ZERO;
            }
        };

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getAccountKpis_shouldReturnCalculatedKpis_whenDataExists() {
        when(authentication.getName()).thenReturn("test@example.com");
        when(repository.getAccountKpis(anyString())).thenReturn(Optional.of(mockQueryResult));

        AccountKpiDto response = kpiAggregationService.getAccountKpis();
        AccountKpiDto.AccountKpi accountKpi = response.getAccountKpi();

        assertEquals(1247, accountKpi.getValidEmails());
        assertEquals(234, accountKpi.getInvalidEmails());
        assertEquals(89, accountKpi.getRiskyEmails());
        assertEquals(156, accountKpi.getUnknownEmails());
        assertEquals(1726, accountKpi.getTotalProcessed());
        assertEquals(72.25, accountKpi.getQualityScore());
        assertEquals(72.25, accountKpi.getDeliverabilityRate());
        assertEquals(13.56, accountKpi.getBounceRate());
    }

    @Test
    void getAccountKpis_shouldReturnDefaultKpis_whenNoDataExists() {
        when(authentication.getName()).thenReturn("test@example.com");
        when(repository.getAccountKpis(anyString())).thenReturn(Optional.empty());

        AccountKpiDto response = kpiAggregationService.getAccountKpis();
        AccountKpiDto.AccountKpi accountKpi = response.getAccountKpi();

        assertEquals(0, accountKpi.getValidEmails());
        assertEquals(0, accountKpi.getInvalidEmails());
        assertEquals(0, accountKpi.getRiskyEmails());
        assertEquals(0, accountKpi.getUnknownEmails());
        assertEquals(0, accountKpi.getTotalProcessed());
        assertEquals(0.0, accountKpi.getQualityScore());
        assertEquals(0.0, accountKpi.getDeliverabilityRate());
        assertEquals(0.0, accountKpi.getBounceRate());
    }
}