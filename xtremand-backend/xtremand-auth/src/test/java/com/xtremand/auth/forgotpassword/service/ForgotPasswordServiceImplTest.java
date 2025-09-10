package com.xtremand.auth.forgotpassword.service;

import com.xtremand.auth.forgotpassword.dto.ForgotPasswordRequest;
import com.xtremand.auth.forgotpassword.dto.ResetPasswordRequest;
import com.xtremand.auth.forgotpassword.exception.InvalidResetTokenException;
import com.xtremand.domain.entity.User;
import com.xtremand.domain.entity.UserForgotPasswordHistory;
import com.xtremand.domain.enums.ForgotPasswordStatus;
import com.xtremand.user.repository.UserForgotPasswordHistoryRepository;
import com.xtremand.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ForgotPasswordServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserForgotPasswordHistoryRepository forgotPasswordHistoryRepository;

    @Mock
    private ForgotPasswordEmailService emailService;

    @InjectMocks
    private ForgotPasswordServiceImpl forgotPasswordService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(forgotPasswordService, "frontendUrl", "http://localhost:3000");
        ReflectionTestUtils.setField(forgotPasswordService, "tokenValidityHours", 1);
    }

    @Test
    void forgotPassword_shouldSendEmailAndCreateHistory_whenUserExists() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        User user = new User();
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        forgotPasswordService.forgotPassword(request);

        ArgumentCaptor<UserForgotPasswordHistory> historyCaptor = ArgumentCaptor.forClass(UserForgotPasswordHistory.class);
        verify(forgotPasswordHistoryRepository).save(historyCaptor.capture());

        UserForgotPasswordHistory savedHistory = historyCaptor.getValue();
        assertEquals(user, savedHistory.getUser());
        assertEquals(ForgotPasswordStatus.PENDING, savedHistory.getStatus());
        assertNotNull(savedHistory.getResetToken());

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendForgotPasswordEmail(emailCaptor.capture(), linkCaptor.capture());

        assertEquals("test@example.com", emailCaptor.getValue());
        assertTrue(linkCaptor.getValue().contains(savedHistory.getResetToken()));
    }

    @Test
    void forgotPassword_shouldDoNothing_whenUserDoesNotExist() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        forgotPasswordService.forgotPassword(request);

        verify(forgotPasswordHistoryRepository, never()).save(any());
        verify(emailService, never()).sendForgotPasswordEmail(any(), any());
    }

    @Test
    void resetPassword_shouldResetPassword_whenTokenIsValid() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setResetToken("valid-token");
        request.setNewPassword("new-password");

        User user = new User();
        UserForgotPasswordHistory history = UserForgotPasswordHistory.builder()
                .user(user)
                .resetToken("valid-token")
                .requestedAt(LocalDateTime.now())
                .status(ForgotPasswordStatus.PENDING)
                .build();

        when(forgotPasswordHistoryRepository.findByResetToken("valid-token")).thenReturn(Optional.of(history));

        forgotPasswordService.resetPassword(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertNotNull(userCaptor.getValue().getPassword());

        assertEquals(ForgotPasswordStatus.COMPLETED, history.getStatus());
        assertNotNull(history.getResetAt());
        verify(forgotPasswordHistoryRepository).save(history);
    }

    @Test
    void resetPassword_shouldThrowException_whenTokenIsInvalid() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setResetToken("invalid-token");
        request.setNewPassword("new-password");

        when(forgotPasswordHistoryRepository.findByResetToken("invalid-token")).thenReturn(Optional.empty());

        assertThrows(InvalidResetTokenException.class, () -> forgotPasswordService.resetPassword(request));
    }

    @Test
    void resetPassword_shouldThrowException_whenTokenIsAlreadyUsed() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setResetToken("used-token");
        request.setNewPassword("new-password");

        UserForgotPasswordHistory history = UserForgotPasswordHistory.builder()
                .status(ForgotPasswordStatus.COMPLETED)
                .build();

        when(forgotPasswordHistoryRepository.findByResetToken("used-token")).thenReturn(Optional.of(history));

        assertThrows(InvalidResetTokenException.class, () -> forgotPasswordService.resetPassword(request));
    }

    @Test
    void resetPassword_shouldThrowException_whenTokenIsExpired() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setResetToken("expired-token");
        request.setNewPassword("new-password");

        UserForgotPasswordHistory history = UserForgotPasswordHistory.builder()
                .requestedAt(LocalDateTime.now().minusHours(2))
                .status(ForgotPasswordStatus.PENDING)
                .build();

        when(forgotPasswordHistoryRepository.findByResetToken("expired-token")).thenReturn(Optional.of(history));

        assertThrows(InvalidResetTokenException.class, () -> forgotPasswordService.resetPassword(request));
        assertEquals(ForgotPasswordStatus.EXPIRED, history.getStatus());
        verify(forgotPasswordHistoryRepository).save(history);
    }
}
