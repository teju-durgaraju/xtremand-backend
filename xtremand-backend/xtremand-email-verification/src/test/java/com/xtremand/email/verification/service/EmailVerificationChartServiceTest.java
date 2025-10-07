package com.xtremand.email.verification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xtremand.common.exception.BadRequestException;
import com.xtremand.domain.entity.User;
import com.xtremand.email.verification.model.ChartDropdownResponse;
import com.xtremand.email.verification.model.dto.chart.ChartRange;
import com.xtremand.email.verification.repository.EmailVerificationHistoryRepository;
import com.xtremand.user.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class EmailVerificationChartServiceTest {

    @Mock
    private EmailVerificationHistoryRepository historyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmailVerificationChartService chartService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("test@example.com", "password")
        );
    }

    private EmailVerificationHistoryRepository.DateRangeProjection createDateRange(Instant earliest, Instant latest) {
        return new EmailVerificationHistoryRepository.DateRangeProjection() {
            @Override
            public Instant getEarliest() {
                return earliest;
            }

            @Override
            public Instant getLatest() {
                return latest;
            }
        };
    }

    @Test
    void getAvailableDropdownRanges_shouldReturnOnly1Month_whenDataIsLessThan1Month() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        Instant now = Instant.now();
        Instant earliest = now.minus(15, ChronoUnit.DAYS);
        when(historyRepository.findDateRangeByUserId(1L)).thenReturn(Optional.of(createDateRange(earliest, now)));

        List<ChartDropdownResponse> ranges = chartService.getAvailableDropdownRanges();

        assertThat(ranges).hasSize(1);
        assertThat(ranges.get(0).getValue()).isEqualTo("1M");
    }

    @Test
    void getAvailableDropdownRanges_shouldReturnUpTo6Months_whenDataIs8Months() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        Instant now = Instant.now();
        Instant earliest = now.minus(8 * 30, ChronoUnit.DAYS); // Approx 8 months
        when(historyRepository.findDateRangeByUserId(1L)).thenReturn(Optional.of(createDateRange(earliest, now)));

        List<ChartDropdownResponse> ranges = chartService.getAvailableDropdownRanges();

        assertThat(ranges).hasSize(3);
        assertThat(ranges.stream().map(ChartDropdownResponse::getValue)).containsExactly("1M", "3M", "6M");
    }

    @Test
    void getAvailableDropdownRanges_shouldReturnUpTo3Years_whenDataIs4Years() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        Instant now = Instant.now();
        Instant earliest = now.minus(4 * 365, ChronoUnit.DAYS); // Approx 4 years
        when(historyRepository.findDateRangeByUserId(1L)).thenReturn(Optional.of(createDateRange(earliest, now)));

        List<ChartDropdownResponse> ranges = chartService.getAvailableDropdownRanges();

        assertThat(ranges).hasSize(5);
        assertThat(ranges.stream().map(ChartDropdownResponse::getValue)).containsExactly("1M", "3M", "6M", "1Y", "3Y");
    }

    @Test
    void getAvailableDropdownRanges_shouldReturnUpTo5Years_whenDataIs7Years() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        Instant now = Instant.now();
        Instant earliest = now.minus(7 * 365, ChronoUnit.DAYS); // Approx 7 years
        when(historyRepository.findDateRangeByUserId(1L)).thenReturn(Optional.of(createDateRange(earliest, now)));

        List<ChartDropdownResponse> ranges = chartService.getAvailableDropdownRanges();

        assertThat(ranges).hasSize(6);
        assertThat(ranges.stream().map(ChartDropdownResponse::getValue)).containsExactly("1M", "3M", "6M", "1Y", "3Y", "5Y");
    }

    @Test
    void getAvailableDropdownRanges_shouldReturnOnly1Month_whenNoHistoryExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(historyRepository.findDateRangeByUserId(1L)).thenReturn(Optional.empty());

        List<ChartDropdownResponse> ranges = chartService.getAvailableDropdownRanges();

        assertThat(ranges).hasSize(1);
        assertThat(ranges.get(0).getValue()).isEqualTo("1M");
    }

    @Test
    void getAvailableDropdownRanges_shouldReturnOnly1Month_whenOnlyOneRecordExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        Instant now = Instant.now();
        when(historyRepository.findDateRangeByUserId(1L)).thenReturn(Optional.of(createDateRange(now, null)));

        List<ChartDropdownResponse> ranges = chartService.getAvailableDropdownRanges();

        assertThat(ranges).hasSize(1);
        assertThat(ranges.get(0).getValue()).isEqualTo("1M");
    }

    @Test
    void validateChartRange_shouldPass_whenRangeIsValid() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        Instant now = Instant.now();
        Instant earliest = now.minus(40, ChronoUnit.DAYS);
        when(historyRepository.findDateRangeByUserId(1L)).thenReturn(Optional.of(createDateRange(earliest, now)));

        chartService.validateChartRange(ChartRange.M1);
    }

    @Test
    void validateChartRange_shouldThrowBadRequest_whenRangeIsInvalid() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        Instant now = Instant.now();
        Instant earliest = now.minus(20, ChronoUnit.DAYS);
        when(historyRepository.findDateRangeByUserId(1L)).thenReturn(Optional.of(createDateRange(earliest, now)));

        assertThatThrownBy(() -> chartService.validateChartRange(ChartRange.M3))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("You do not have enough data to view this range. Please select a shorter time frame.");
    }

    @Test
    void validateChartRange_shouldPass_for1MonthRange_whenNoHistoryExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(historyRepository.findDateRangeByUserId(1L)).thenReturn(Optional.empty());

        chartService.validateChartRange(ChartRange.M1);
    }

    @Test
    void validateChartRange_shouldThrowBadRequest_for3MonthRange_whenNoHistoryExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(historyRepository.findDateRangeByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chartService.validateChartRange(ChartRange.M3))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid range selected. No data available for this range.");
    }

    @Test
    void validateChartRange_shouldThrowBadRequest_whenUserNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chartService.validateChartRange(ChartRange.M1))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User not found.");
    }

    @Test
    void getChartData_shouldCallValidateChartRange() {
        Instant fixedInstant = Instant.parse("2025-10-07T11:33:01.821423477Z");
        try (MockedStatic<Instant> mockedInstant = Mockito.mockStatic(Instant.class)) {
            mockedInstant.when(Instant::now).thenReturn(fixedInstant);

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            Instant earliest = fixedInstant.minus(40, ChronoUnit.DAYS);
            when(historyRepository.findDateRangeByUserId(1L)).thenReturn(Optional.of(createDateRange(earliest, fixedInstant)));

            chartService.getChartData(ChartRange.M1);

            verify(historyRepository).findChartDataByUserEmail("test@example.com", fixedInstant.minus(30, ChronoUnit.DAYS), "IYYY-IW");
        }
    }
}