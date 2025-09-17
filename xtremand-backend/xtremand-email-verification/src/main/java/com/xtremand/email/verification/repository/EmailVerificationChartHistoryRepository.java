package com.xtremand.email.verification.repository;

import com.xtremand.domain.entity.EmailVerificationChartHistory;
import com.xtremand.domain.enums.AggregationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailVerificationChartHistoryRepository extends JpaRepository<EmailVerificationChartHistory, Long> {
    List<EmailVerificationChartHistory> findByAggregationType(AggregationType aggregationType);
}
