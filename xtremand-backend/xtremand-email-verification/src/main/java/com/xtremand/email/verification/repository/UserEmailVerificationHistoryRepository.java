package com.xtremand.email.verification.repository;

import com.xtremand.domain.entity.UserEmailVerificationHistory;
import com.xtremand.domain.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEmailVerificationHistoryRepository extends JpaRepository<UserEmailVerificationHistory, Long> {
    long countByStatus(VerificationStatus status);

    @Query("SELECT AVG(e.score) FROM UserEmailVerificationHistory e")
    Double getAverageScore();
}
