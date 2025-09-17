package com.xtremand.email.verification.repository;

import com.xtremand.domain.entity.UserEmailVerificationHistory;
import com.xtremand.domain.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserEmailVerificationHistoryRepository extends JpaRepository<UserEmailVerificationHistory, Long> {
    long countByStatus(VerificationStatus status);

    @Query("SELECT AVG(e.score) FROM UserEmailVerificationHistory e")
    Double getAverageScore();

    long countByStatusAndCheckedAtBetween(VerificationStatus status, LocalDateTime start, LocalDateTime end);

    long countByCheckedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT AVG(e.score) FROM UserEmailVerificationHistory e WHERE e.checkedAt BETWEEN :start AND :end")
    Double getAverageScore(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
