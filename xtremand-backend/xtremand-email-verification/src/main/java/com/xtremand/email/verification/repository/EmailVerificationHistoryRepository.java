package com.xtremand.email.verification.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.xtremand.domain.entity.EmailVerificationHistory;

@Repository
public interface EmailVerificationHistoryRepository extends JpaRepository<EmailVerificationHistory, Long> {

    Page<EmailVerificationHistory> findByBatch_Id(UUID batchId, Pageable pageable);

    String FIND_DISTINCT_LATEST_QUERY = """
        WITH RankedHistory AS (
            SELECT
                h.*,
                ROW_NUMBER() OVER (PARTITION BY h.email ORDER BY h.checked_at DESC) as rn
            FROM
                xtremand_production.xt_user_email_verification_history h
        )
        SELECT
            id,
            email,
            domain,
            status,
            score,
            confidence,
            checked_at AS "lastVerifiedAt",
            syntax_check AS syntaxCheck,
            mx_check AS mxCheck,
            disposable_check AS disposableCheck,
            role_based_check AS roleBasedCheck,
            catch_all_check AS catchAllCheck,
            blacklist_check AS blacklistCheck,
            (case when smtp_check_status = 'DELIVERABLE' then true else false end) AS smtpCheck,
            (case when smtp_ping_status = 'SUCCESS' then true else false end) AS smtpPing
        FROM
            RankedHistory
        WHERE
            rn = 1
        """;

    @Query(nativeQuery = true, value = FIND_DISTINCT_LATEST_QUERY)
    List<DistinctLatestVerificationProjection> findDistinctLatest();

    interface DistinctLatestVerificationProjection {
        Long getId();
        String getEmail();
        String getDomain();
        String getStatus();
        int getScore();
        String getConfidence();
        Instant getLastVerifiedAt();
        boolean getSyntaxCheck();
        boolean getMxCheck();
        boolean getDisposableCheck();
        boolean getRoleBasedCheck();
        boolean getCatchAllCheck();
        boolean getBlacklistCheck();
        boolean getSmtpCheck();
        boolean getSmtpPing();
    }
}