package com.xtremand.email.verification.repository;

import com.xtremand.domain.entity.EmailVerificationBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmailVerificationBatchRepository extends JpaRepository<EmailVerificationBatch, UUID> {
}