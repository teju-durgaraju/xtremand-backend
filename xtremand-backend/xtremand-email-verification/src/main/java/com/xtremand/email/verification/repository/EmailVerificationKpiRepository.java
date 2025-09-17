package com.xtremand.email.verification.repository;

import com.xtremand.domain.entity.EmailVerificationKpi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationKpiRepository extends JpaRepository<EmailVerificationKpi, Long> {
    Optional<EmailVerificationKpi> findByMonth(String month);
    List<EmailVerificationKpi> findTopByOrderByMonthDesc(int months);
}
