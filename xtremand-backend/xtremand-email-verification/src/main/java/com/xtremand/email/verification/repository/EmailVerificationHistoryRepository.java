package com.xtremand.email.verification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xtremand.domain.entity.EmailVerificationHistory;

@Repository
public interface EmailVerificationHistoryRepository extends JpaRepository<EmailVerificationHistory, Long> {
}