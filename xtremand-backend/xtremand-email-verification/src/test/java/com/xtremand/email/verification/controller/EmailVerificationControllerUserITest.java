package com.xtremand.email.verification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtremand.domain.entity.EmailVerificationBatch;
import com.xtremand.domain.entity.EmailVerificationHistory;
import com.xtremand.domain.entity.User;
import com.xtremand.email.verification.model.dto.VerifyBatchRequest;
import com.xtremand.email.verification.model.dto.VerifyEmailRequest;
import com.xtremand.email.verification.repository.EmailVerificationBatchRepository;
import com.xtremand.email.verification.repository.EmailVerificationHistoryRepository;
import com.xtremand.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser
class EmailVerificationControllerUserITest {

    @SpringBootApplication
    @EnableJpaRepositories(basePackages = {"com.xtremand.email.verification.repository", "com.xtremand.user.repository"})
    @EntityScan(basePackages = {"com.xtremand.domain.entity"})
    @ComponentScan(basePackages = "com.xtremand.email.verification")
    static class TestConfiguration {}

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationBatchRepository batchRepository;

    @Autowired
    private EmailVerificationHistoryRepository historyRepository;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        historyRepository.deleteAll();
        batchRepository.deleteAll();
        userRepository.deleteAll();

        userA = userRepository.save(User.builder().username("userA").email("userA@test.com").password("password").build());
        userB = userRepository.save(User.builder().username("userB").email("userB@test.com").password("password").build());
    }

    private void createTestData() {
        // User A's data
        EmailVerificationBatch batchA = new EmailVerificationBatch();
        batchA.setUser(userA);
        batchA.setTotalEmails(1);
        batchRepository.save(batchA);

        EmailVerificationHistory historyA = new EmailVerificationHistory();
        historyA.setUser(userA);
        historyA.setEmail("testA@example.com");
        historyA.setStatus(EmailVerificationHistory.VerificationStatus.VALID);
        historyA.setScore(90);
        historyA.setBatch(batchA);
        historyA.setSmtpCheckStatus(EmailVerificationHistory.SmtpCheckStatus.DELIVERABLE);
        historyA.setSmtpPingStatus(EmailVerificationHistory.SmtpPingStatus.SUCCESS);
        historyRepository.save(historyA);

        // User B's data
        EmailVerificationBatch batchB = new EmailVerificationBatch();
        batchB.setUser(userB);
        batchB.setTotalEmails(1);
        batchRepository.save(batchB);

        EmailVerificationHistory historyB = new EmailVerificationHistory();
        historyB.setUser(userB);
        historyB.setEmail("testB@example.com");
        historyB.setStatus(EmailVerificationHistory.VerificationStatus.INVALID);
        historyB.setScore(10);
        historyB.setBatch(batchB);
        historyB.setSmtpCheckStatus(EmailVerificationHistory.SmtpCheckStatus.INVALID);
        historyB.setSmtpPingStatus(EmailVerificationHistory.SmtpPingStatus.FAIL);
        historyRepository.save(historyB);
    }

    @Test
    void getBatches_asUserA_shouldOnlyReturnUserABatches() throws Exception {
        createTestData();

        mockMvc.perform(get("/api/v1/email/verify/batch")
                        .with(jwt().jwt(j -> j.claim("user_id", userA.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].userId", is(userA.getId().intValue())));
    }

    @Test
    void getBatchEmails_asUserAForUserBBatch_shouldReturnAccessDenied() throws Exception {
        createTestData();
        EmailVerificationBatch batchB = batchRepository.findByUser_Id(userB.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent().get(0);

        mockMvc.perform(get("/api/v1/email/verify/batch/{batchId}/emails", batchB.getId())
                        .with(jwt().jwt(j -> j.claim("user_id", userA.getId()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDistinctLatest_asUserB_shouldOnlyReturnUserBResults() throws Exception {
        createTestData();

        mockMvc.perform(get("/api/v1/email/verify/distinct-latest")
                        .with(jwt().jwt(j -> j.claim("user_id", userB.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email", is("testB@example.com")));
    }

    @Test
    void getAccountKpi_asUserA_shouldReturnCorrectKpis() throws Exception {
        createTestData();

        mockMvc.perform(get("/api/v1/email/verify/kpi")
                        .with(jwt().jwt(j -> j.claim("user_id", userA.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountKpi.totalProcessed", is(1)))
                .andExpect(jsonPath("$.accountKpi.validEmails", is(1)))
                .andExpect(jsonPath("$.accountKpi.invalidEmails", is(0)));
    }

    @Test
    void verifyEmail_authenticated_shouldSucceed() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("test@example.com");

        mockMvc.perform(post("/api/v1/email/verify")
                        .with(jwt().jwt(j -> j.claim("user_id", userA.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("test@example.com")));
    }

    @Test
    void verifyBatch_authenticated_shouldSucceed() throws Exception {
        VerifyBatchRequest request = new VerifyBatchRequest();
        request.setEmails(List.of("batch1@example.com", "batch2@example.com"));

        mockMvc.perform(post("/api/v1/email/verify/batch")
                        .with(jwt().jwt(j -> j.claim("user_id", userA.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.totalEmails", is(2)));
    }
}