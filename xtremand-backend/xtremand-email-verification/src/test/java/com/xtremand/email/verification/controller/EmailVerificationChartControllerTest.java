package com.xtremand.email.verification.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.xtremand.common.exception.BadRequestException;
import com.xtremand.common.exception.GlobalExceptionHandler;
import com.xtremand.email.verification.converter.ChartRangeConverter;
import com.xtremand.email.verification.model.dto.chart.ChartDataResponseDto;
import com.xtremand.email.verification.model.dto.chart.ChartRange;
import com.xtremand.email.verification.service.EmailVerificationChartService;

@ExtendWith(MockitoExtension.class)
public class EmailVerificationChartControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EmailVerificationChartService chartService;

    @InjectMocks
    private EmailVerificationChartController chartController;

    @BeforeEach
    void setUp() {
        FormattingConversionService conversionService = new FormattingConversionService();
        conversionService.addConverter(new ChartRangeConverter());

        mockMvc = MockMvcBuilders.standaloneSetup(chartController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setConversionService(conversionService)
                .build();
    }

    @Test
    @WithMockUser
    void getChartData_withValidRange_shouldReturnOk() throws Exception {
        when(chartService.getChartData(ChartRange.M1)).thenReturn(new ChartDataResponseDto(java.util.Collections.emptyList()));

        mockMvc.perform(get("/api/v1/email/verify/chart/data")
                        .param("range", "M1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getChartData_withInvalidRange_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/email/verify/chart/data")
                        .param("range", "INVALID_RANGE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getChartData_withInvalidRange_shouldReturnBadRequest_whenServiceThrowsException() throws Exception {
        when(chartService.getChartData(ChartRange.M3)).thenThrow(new BadRequestException("Invalid range"));

        mockMvc.perform(get("/api/v1/email/verify/chart/data")
                        .param("range", "M3"))
                .andExpect(status().isBadRequest());
    }
}