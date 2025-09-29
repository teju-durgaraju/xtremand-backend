package com.xtremand.emailverification.service;

import com.xtremand.emailverification.config.ScoringProperties;
import com.xtremand.emailverification.domain.EmailVerificationHistory;
import com.xtremand.emailverification.model.SmtpProbeResult;
import com.xtremand.emailverification.model.VerificationResult;
import com.xtremand.emailverification.repository.EmailVerificationHistoryRepository;
import com.xtremand.emailverification.service.verifier.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock private SyntaxCheckProvider syntaxCheckProvider;
    @Mock private MxCheckProvider mxCheckProvider;
    @Mock private DisposableDomainProvider disposableDomainProvider;
    @Mock private RoleBasedEmailProvider roleBasedEmailProvider;
    @Mock private BlacklistProvider blacklistProvider;
    @Mock private CatchAllProvider catchAllProvider;
    @Mock private SmtpProbeService smtpProbeService;
    @Mock private EmailVerificationHistoryRepository historyRepository;

    @Spy
    private ScoringProperties scoringProperties = new ScoringProperties();

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    @BeforeEach
    void setUp() {
        // Configure default success mocks
        when(syntaxCheckProvider.isValid(anyString())).thenReturn(true);
        when(mxCheckProvider.hasMxRecords(anyString())).thenReturn(true);
        when(disposableDomainProvider.isDisposable(anyString())).thenReturn(false);
        when(blacklistProvider.isBlacklisted(anyString())).thenReturn(false);
        when(roleBasedEmailProvider.isRoleBased(anyString())).thenReturn(false);
        when(catchAllProvider.isKnownCatchAll(anyString())).thenReturn(false);
    }

    @Test
    void testVerifyEmail_ValidEmail_FullSuccess() {
        // Arrange
        String email = "test@gmail.com";
        SmtpProbeResult smtpResult = SmtpProbeResult.builder()
                .status(EmailVerificationHistory.SmtpCheckStatus.DELIVERABLE)
                .pingStatus(EmailVerificationHistory.SmtpPingStatus.SUCCESS)
                .build();
        when(smtpProbeService.probe(email)).thenReturn(smtpResult);

        // Act
        VerificationResult result = emailVerificationService.verifyEmail(email, 1L);

        // Assert
        assertEquals(EmailVerificationHistory.VerificationStatus.VALID, result.getStatus());
        assertEquals(100, result.getScore());

        ArgumentCaptor<EmailVerificationHistory> historyCaptor = ArgumentCaptor.forClass(EmailVerificationHistory.class);
        verify(historyRepository).save(historyCaptor.capture());
        assertEquals(email, historyCaptor.getValue().getEmail());
        assertEquals(EmailVerificationHistory.VerificationStatus.VALID, historyCaptor.getValue().getStatus());
    }

    @Test
    void testVerifyEmail_InvalidSyntax() {
        // Arrange
        String email = "invalid-email";
        when(syntaxCheckProvider.isValid(email)).thenReturn(false);

        // Act
        VerificationResult result = emailVerificationService.verifyEmail(email, 1L);

        // Assert
        assertEquals(EmailVerificationHistory.VerificationStatus.INVALID, result.getStatus());
        assertEquals(0, result.getScore());
        verify(smtpProbeService, never()).probe(anyString());
    }

    @Test
    void testVerifyEmail_DisposableEmail() {
        // Arrange
        String email = "user@mailinator.com";
        when(disposableDomainProvider.isDisposable(email)).thenReturn(true);

        // Act
        VerificationResult result = emailVerificationService.verifyEmail(email, 1L);

        // Assert
        assertEquals(EmailVerificationHistory.VerificationStatus.DISPOSABLE, result.getStatus());
        verify(smtpProbeService, never()).probe(anyString()); // SMTP check should be skipped
    }

    @Test
    void testVerifyEmail_BlacklistedEmail() {
        // Arrange
        String email = "spammer@evil.com";
        when(blacklistProvider.isBlacklisted(email)).thenReturn(true);

        // Act
        VerificationResult result = emailVerificationService.verifyEmail(email, 1L);

        // Assert
        assertEquals(EmailVerificationHistory.VerificationStatus.INVALID, result.getStatus());
        verify(smtpProbeService, never()).probe(anyString()); // SMTP check should be skipped
    }
}