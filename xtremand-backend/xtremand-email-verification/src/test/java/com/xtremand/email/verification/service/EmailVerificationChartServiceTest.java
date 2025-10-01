package com.xtremand.email.verification.service;

import com.xtremand.email.verification.model.dto.chart.ChartDataDto;
import com.xtremand.email.verification.model.dto.chart.ChartDataResponseDto;
import com.xtremand.email.verification.model.dto.chart.ChartRange;
import com.xtremand.email.verification.repository.EmailVerificationHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationChartServiceTest {

    @Mock
    private EmailVerificationHistoryRepository historyRepository;

    @InjectMocks
    private EmailVerificationChartService chartService;

    @Test
    void testGetChartData_M1_WeeklyAggregation() {
        // Given
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");

        try (MockedStatic<SecurityContextHolder> mockedContext = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            ChartRange range = ChartRange.M1;
            EmailVerificationHistoryRepository.ChartDataProjection p1 = createProjection("2023-W01", "VALID", 10);
            EmailVerificationHistoryRepository.ChartDataProjection p2 = createProjection("2023-W01", "INVALID", 5);
            when(historyRepository.findChartDataByUserEmail(anyString(), any(Instant.class), anyString())).thenReturn(List.of(p1, p2));

            // When
            ChartDataResponseDto response = chartService.getChartData(range);

            // Then
            assertEquals(1, response.chartPerformanceData().size());
            ChartDataDto chartData = response.chartPerformanceData().get(0);
            assertEquals("Week 1", chartData.period());
            assertEquals(15, chartData.verified());
            assertEquals(10, chartData.deliverable());
            assertEquals(5, chartData.invalid());
        }
    }

    @Test
    void testGetChartData_Y1_MonthlyAggregation() {
        // Given
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");

        try (MockedStatic<SecurityContextHolder> mockedContext = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            ChartRange range = ChartRange.Y1;
            EmailVerificationHistoryRepository.ChartDataProjection p1 = createProjection("2023-10", "VALID", 100);
            EmailVerificationHistoryRepository.ChartDataProjection p2 = createProjection("2023-10", "RISKY", 20);
            EmailVerificationHistoryRepository.ChartDataProjection p3 = createProjection("2023-11", "VALID", 120);
            when(historyRepository.findChartDataByUserEmail(anyString(), any(Instant.class), anyString())).thenReturn(List.of(p1, p2, p3));

            // When
            ChartDataResponseDto response = chartService.getChartData(range);

            // Then
            assertEquals(2, response.chartPerformanceData().size());
            ChartDataDto month1 = response.chartPerformanceData().get(0);
            assertEquals("Oct 23", month1.period());
            assertEquals(120, month1.verified());
            assertEquals(100, month1.deliverable());
            assertEquals(20, month1.risky());

            ChartDataDto month2 = response.chartPerformanceData().get(1);
            assertEquals("Nov 23", month2.period());
            assertEquals(120, month2.verified());
            assertEquals(120, month2.deliverable());
        }
    }

    private EmailVerificationHistoryRepository.ChartDataProjection createProjection(String period, String status, long count) {
        EmailVerificationHistoryRepository.ChartDataProjection projection = mock(EmailVerificationHistoryRepository.ChartDataProjection.class);
        when(projection.getPeriod()).thenReturn(period);
        when(projection.getStatus()).thenReturn(status);
        when(projection.getCount()).thenReturn(count);
        return projection;
    }
}