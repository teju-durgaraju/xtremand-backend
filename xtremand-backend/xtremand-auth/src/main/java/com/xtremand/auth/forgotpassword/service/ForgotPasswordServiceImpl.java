package com.xtremand.auth.forgotpassword.service;

import com.xtremand.auth.forgotpassword.dto.ForgotPasswordRequest;
import com.xtremand.auth.forgotpassword.dto.ResetPasswordRequest;
import com.xtremand.auth.forgotpassword.exception.InvalidResetTokenException;
import com.xtremand.common.util.AESUtil;
import com.xtremand.domain.entity.User;
import com.xtremand.domain.entity.UserForgotPasswordHistory;
import com.xtremand.domain.enums.ForgotPasswordStatus;
import com.xtremand.user.repository.UserForgotPasswordHistoryRepository;
import com.xtremand.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private final UserRepository userRepository;
    private final UserForgotPasswordHistoryRepository forgotPasswordHistoryRepository;
    private final ForgotPasswordEmailService emailService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${xtremand.security.reset-password.token-validity-hours}")
    private int tokenValidityHours;

    public ForgotPasswordServiceImpl(UserRepository userRepository,
                                     UserForgotPasswordHistoryRepository forgotPasswordHistoryRepository,
                                     ForgotPasswordEmailService emailService) {
        this.userRepository = userRepository;
        this.forgotPasswordHistoryRepository = forgotPasswordHistoryRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            UserForgotPasswordHistory history = UserForgotPasswordHistory.builder()
                    .user(user)
                    .resetToken(token)
                    .requestedAt(LocalDateTime.now())
                    .status(ForgotPasswordStatus.PENDING)
                    .build();
            forgotPasswordHistoryRepository.save(history);

            String resetLink = frontendUrl + "/reset-password?token=" + token;
            emailService.sendForgotPasswordEmail(user.getEmail(), resetLink);
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        UserForgotPasswordHistory history = forgotPasswordHistoryRepository.findByResetToken(request.getResetToken())
                .orElseThrow(() -> new InvalidResetTokenException("Invalid reset token"));

        if (history.getStatus() != ForgotPasswordStatus.PENDING) {
            throw new InvalidResetTokenException("Reset token has already been used");
        }

        if (history.getRequestedAt().plusHours(tokenValidityHours).isBefore(LocalDateTime.now())) {
            history.setStatus(ForgotPasswordStatus.EXPIRED);
            forgotPasswordHistoryRepository.save(history);
            throw new InvalidResetTokenException("Reset token has expired");
        }

        User user = history.getUser();
        user.setPassword(AESUtil.encrypt(request.getNewPassword()));
        userRepository.save(user);

        history.setStatus(ForgotPasswordStatus.COMPLETED);
        history.setResetAt(LocalDateTime.now());
        forgotPasswordHistoryRepository.save(history);
    }
}
