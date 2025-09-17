package com.xtremand.email.verification.service;

import com.xtremand.domain.entity.EmailVerificationChartHistory;
import com.xtremand.domain.enums.AggregationType;
import com.xtremand.email.verification.repository.EmailVerificationChartHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChartServiceTest {

    @Mock
    private EmailVerificationChartHistoryRepository chartHistoryRepository;

    @InjectMocks
    private ChartService chartService;

    @Test
    void getWeeklyPerformance_shouldReturnWeeklyChartData() {
        EmailVerificationChartHistory history = EmailVerificationChartHistory.builder().period("Week 1").build();
        when(chartHistoryRepository.findByAggregationType(AggregationType.WEEKLY)).thenReturn(List.of(history));

        var result = chartService.getWeeklyPerformance();

        assertEquals(1, result.size());
        assertEquals("Week 1", result.get(0).getPeriod());
    }
}
