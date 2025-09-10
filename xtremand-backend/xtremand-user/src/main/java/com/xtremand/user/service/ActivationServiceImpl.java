package com.xtremand.user.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.domain.entity.User;
import com.xtremand.domain.entity.UserActivationHistory;
import com.xtremand.domain.enums.ActivationStatus;
import com.xtremand.domain.enums.UserStatus;
import com.xtremand.user.repository.UserActivationHistoryRepository;
import com.xtremand.user.repository.UserRepository;

import com.xtremand.email.notification.NotificationService;

@Service
public class ActivationServiceImpl implements ActivationService {

    private final UserActivationHistoryRepository userActivationHistoryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Value("${xtremand.security.token.activation.expiry-in-hours:24}")
    private long activationTokenExpiryInHours;

    public ActivationServiceImpl(UserActivationHistoryRepository userActivationHistoryRepository,
                                 UserRepository userRepository, NotificationService notificationService) {
        this.userActivationHistoryRepository = userActivationHistoryRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void createActivationTokenAndSendEmail(User user) {
        String token = UUID.randomUUID().toString();
        UserActivationHistory history = UserActivationHistory.builder()
                .user(user)
                .activationToken(token)
                .requestedAt(Instant.now())
                .status(ActivationStatus.PENDING)
                .build();
        userActivationHistoryRepository.save(history);

        String activationLink = "http://localhost:8080/api/auth/activate?token=" + token;
        notificationService.sendActivationEmail(user.getEmail(), activationLink);
    }

    @Override
    @Transactional
    public void activateUser(String token) {
        UserActivationHistory history = userActivationHistoryRepository.findByActivationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid activation token"));

        if (history.getStatus() == ActivationStatus.COMPLETED) {
            throw new IllegalStateException("Account already activated");
        }

        if (history.getRequestedAt().plus(activationTokenExpiryInHours, ChronoUnit.HOURS).isBefore(Instant.now())) {
            history.setStatus(ActivationStatus.EXPIRED);
            userActivationHistoryRepository.save(history);
            throw new IllegalStateException("Activation token expired");
        }

        User user = history.getUser();
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        history.setActivatedAt(Instant.now());
        history.setStatus(ActivationStatus.COMPLETED);
        userActivationHistoryRepository.save(history);
    }
}
