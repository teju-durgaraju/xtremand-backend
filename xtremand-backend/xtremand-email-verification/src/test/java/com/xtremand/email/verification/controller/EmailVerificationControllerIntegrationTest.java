package com.xtremand.email.verification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtremand.domain.entity.EmailVerificationBatch;
import com.xtremand.domain.entity.EmailVerificationHistory;
import com.xtremand.email.verification.model.dto.VerifyBatchRequest;
import com.xtremand.email.verification.repository.EmailVerificationBatchRepository;
import com.xtremand.email.verification.repository.EmailVerificationHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.xtremand.common.email.notification.NotificationService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import com.xtremand.common.error.ErrorCodeRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser
class EmailVerificationControllerIntegrationTest {

    @SpringBootApplication
    @EnableJpaRepositories(basePackages = {
            "com.xtremand.email.verification.repository",
            "com.xtremand.domain.repository",
            "com.xtremand.user.repository"
    })
    @EntityScan(basePackages = {"com.xtremand.domain.entity", "com.xtremand.user.entity"})
    @ComponentScan(basePackages = {"com.xtremand.email.verification", "com.xtremand.auth", "com.xtremand.user", "com.xtremand.common.email", "com.xtremand.common.error"})
    static class TestConfiguration {
        @Bean
        public ErrorCodeRegistry errorCodeRegistry(@Value("classpath*:error-codes.yaml") Resource[] yamls) {
            return new ErrorCodeRegistry(yamls);
        }
    }

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmailVerificationBatchRepository batchRepository;

    @Autowired
    private EmailVerificationHistoryRepository historyRepository;

    @BeforeEach
    void setUp() {
        historyRepository.deleteAll();
        batchRepository.deleteAll();
    }

    @Test
    void verifyBatch_shouldCreateBatchAndReturnAccepted() throws Exception {
        VerifyBatchRequest request = new VerifyBatchRequest();
        request.setEmails(Arrays.asList("test1@example.com", "test2@example.com"));
        request.setUserId(1L);

        mockMvc.perform(post("/api/v1/email/verify/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.batchId", is(notNullValue())))
                .andExpect(jsonPath("$.totalEmails", is(2)));
    }

    @Test
    void getBatchSummaries_shouldReturnPagedBatchInfo() throws Exception {
        EmailVerificationBatch batch = new EmailVerificationBatch();
        batch.setTotalEmails(10);
        batchRepository.save(batch);

        mockMvc.perform(get("/api/v1/email/verify/batch")
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].totalEmails", is(10)));
    }

    @Test
    void getBatchEmails_shouldReturnPagedEmailHistoryForBatch() throws Exception {
        EmailVerificationBatch batch = new EmailVerificationBatch();
        batch.setTotalEmails(1);
        batch = batchRepository.save(batch);

        EmailVerificationHistory history = new EmailVerificationHistory();
        history.setEmail("test@example.com");
        history.setDomain("example.com");
        history.setBatch(batch);
        history.setStatus(EmailVerificationHistory.VerificationStatus.VALID);
        history.setScore(90);
        history.setSmtpCheckStatus(EmailVerificationHistory.SmtpCheckStatus.DELIVERABLE);
        history.setSmtpPingStatus(EmailVerificationHistory.SmtpPingStatus.SUCCESS);
        historyRepository.save(history);

        mockMvc.perform(get("/api/v1/email/verify/batch/{batchId}/emails", batch.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].email", is("test@example.com")));
    }

    @Test
    void getDistinctLatestResults_shouldReturnLatestVerificationForEachEmail() throws Exception {
        EmailVerificationHistory oldHistory = new EmailVerificationHistory();
        oldHistory.setEmail("test@example.com");
        oldHistory.setDomain("example.com");
        oldHistory.setStatus(EmailVerificationHistory.VerificationStatus.INVALID);
        oldHistory.setScore(10);
        oldHistory.setSmtpCheckStatus(EmailVerificationHistory.SmtpCheckStatus.INVALID);
        oldHistory.setSmtpPingStatus(EmailVerificationHistory.SmtpPingStatus.FAIL);
        historyRepository.saveAndFlush(oldHistory);

        // Ensure a time difference
        Thread.sleep(10);

        EmailVerificationHistory newHistory = new EmailVerificationHistory();
        newHistory.setEmail("test@example.com");
        newHistory.setDomain("example.com");
        newHistory.setStatus(EmailVerificationHistory.VerificationStatus.VALID);
        newHistory.setScore(95);
        newHistory.setConfidence(EmailVerificationHistory.Confidence.HIGH);
        newHistory.setSyntaxCheck(true);
        newHistory.setSmtpCheckStatus(EmailVerificationHistory.SmtpCheckStatus.DELIVERABLE);
        newHistory.setSmtpPingStatus(EmailVerificationHistory.SmtpPingStatus.SUCCESS);
        historyRepository.saveAndFlush(newHistory);

        EmailVerificationHistory otherHistory = new EmailVerificationHistory();
        otherHistory.setEmail("another@example.com");
        otherHistory.setDomain("example.com");
        otherHistory.setStatus(EmailVerificationHistory.VerificationStatus.VALID);
        otherHistory.setScore(90);
        otherHistory.setConfidence(EmailVerificationHistory.Confidence.HIGH);
        otherHistory.setSyntaxCheck(true);
        otherHistory.setSmtpCheckStatus(EmailVerificationHistory.SmtpCheckStatus.DELIVERABLE);
        otherHistory.setSmtpPingStatus(EmailVerificationHistory.SmtpPingStatus.SUCCESS);
        historyRepository.saveAndFlush(otherHistory);


        mockMvc.perform(get("/api/v1/email/verify/distinct-latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.email == 'test@example.com')].score", contains(95)))
                .andExpect(jsonPath("$[?(@.email == 'test@example.com')].checks.smtp_check", contains(true)));
    }
}