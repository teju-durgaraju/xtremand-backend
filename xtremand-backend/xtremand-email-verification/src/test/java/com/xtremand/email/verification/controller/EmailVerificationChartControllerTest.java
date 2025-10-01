package com.xtremand.email.verification.controller;

import com.xtremand.domain.entity.EmailVerificationHistory;
import com.xtremand.domain.entity.User;
import com.xtremand.email.verification.TestEmailVerificationApplication;
import com.xtremand.email.verification.repository.EmailVerificationHistoryRepository;
import com.xtremand.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestEmailVerificationApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "testuser@example.com")
class EmailVerificationChartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationHistoryRepository historyRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User testUser;

    @BeforeEach
    void setUp() {
        historyRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .email("testuser@example.com")
                .password("password")
                .username("testuser")
                .build();
        testUser = userRepository.saveAndFlush(testUser);
    }

    @AfterEach
    void tearDown() {
        historyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getDropdowns_shouldReturnDefaultOptions() throws Exception {
        mockMvc.perform(get("/api/v1/email/verify/chart/dropdowns"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].label").value("Last 1 Month"))
                .andExpect(jsonPath("$[0].value").value("1M"));
    }

    @Test
    void getChartData_M1_shouldReturnWeeklyAggregatedData() throws Exception {
        // Data for this week
        Instant twoDaysAgo = Instant.now().minus(2, ChronoUnit.DAYS);
        createHistory(EmailVerificationHistory.VerificationStatus.VALID, twoDaysAgo);

        // Data for last week
        Instant eightDaysAgo = Instant.now().minus(8, ChronoUnit.DAYS);
        createHistory(EmailVerificationHistory.VerificationStatus.INVALID, eightDaysAgo);
        Instant nineDaysAgo = Instant.now().minus(9, ChronoUnit.DAYS);
        createHistory(EmailVerificationHistory.VerificationStatus.RISKY, nineDaysAgo);

        mockMvc.perform(get("/api/v1/email/verify/chart/data?range=1M"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chartPerformanceData", hasSize(2)));
    }

    @Test
    void getChartData_Y1_shouldReturnMonthlyAggregatedData() throws Exception {
        // Data for current month
        createHistory(EmailVerificationHistory.VerificationStatus.VALID, Instant.now().minus(5, ChronoUnit.DAYS));
        createHistory(EmailVerificationHistory.VerificationStatus.VALID, Instant.now().minus(10, ChronoUnit.DAYS));

        // Data for last month
        createHistory(EmailVerificationHistory.VerificationStatus.INVALID, Instant.now().minus(35, ChronoUnit.DAYS));
        createHistory(EmailVerificationHistory.VerificationStatus.UNKNOWN, Instant.now().minus(40, ChronoUnit.DAYS));

        mockMvc.perform(get("/api/v1/email/verify/chart/data?range=1Y"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chartPerformanceData", hasSize(2)));
    }

    private void createHistory(EmailVerificationHistory.VerificationStatus status, Instant checkedAt) {
        jdbcTemplate.update(
                "INSERT INTO xt_user_email_verification_history (user_id, email, status, score, smtp_check_status, smtp_ping_status, checked_at, syntax_check, mx_check, disposable_check, role_based_check, blacklist_check, catch_all_check, is_catch_all, is_greylisted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                testUser.getId(),
                UUID.randomUUID() + "@example.com",
                status.name(),
                80,
                EmailVerificationHistory.SmtpCheckStatus.DELIVERABLE.name(),
                EmailVerificationHistory.SmtpPingStatus.SUCCESS.name(),
                Timestamp.from(checkedAt),
                true, true, false, false, false, false, false, false
        );
    }
}