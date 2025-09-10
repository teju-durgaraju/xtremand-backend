package com.xtremand.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xtremand.domain.entity.UserForgotPasswordHistory;

@Repository
public interface UserForgotPasswordHistoryRepository extends JpaRepository<UserForgotPasswordHistory, Long> {

    Optional<UserForgotPasswordHistory> findByResetToken(String resetToken);
}
