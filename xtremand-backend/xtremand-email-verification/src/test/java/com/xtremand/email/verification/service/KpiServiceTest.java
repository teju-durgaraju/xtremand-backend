package com.xtremand.email.verification.service;

import com.xtremand.domain.entity.EmailVerificationKpi;
import com.xtremand.email.verification.repository.EmailVerificationKpiRepository;
import com.xtremand.email.verification.repository.UserEmailVerificationHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KpiServiceTest {

    @Mock
    private EmailVerificationKpiRepository kpiRepository;

    @Mock
    private UserEmailVerificationHistoryRepository historyRepository;

    @InjectMocks
    private KpiService kpiService;

    @Test
    void getCurrentMonthKpi_shouldReturnKpiForCurrentMonth() {
        YearMonth currentMonth = YearMonth.now();
        String monthStr = currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        EmailVerificationKpi kpi = EmailVerificationKpi.builder().month(monthStr).qualityScore(BigDecimal.TEN).build();
        when(kpiRepository.findByMonth(monthStr)).thenReturn(Optional.of(kpi));

        var result = kpiService.getCurrentMonthKpi();

        assertEquals(monthStr, result.getMonth());
    }
}
