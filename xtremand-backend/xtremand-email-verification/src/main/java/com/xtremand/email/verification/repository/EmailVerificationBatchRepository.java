package com.xtremand.email.verification.repository;

import com.xtremand.domain.entity.EmailVerificationBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationBatchRepository extends JpaRepository<EmailVerificationBatch, UUID> {
    Page<EmailVerificationBatch> findByUser_Id(Long userId, Pageable pageable);
    Optional<EmailVerificationBatch> findByIdAndUser_Id(UUID id, Long userId);
}