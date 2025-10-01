package com.xtremand.email.verification.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.xtremand.domain.entity.EmailVerificationHistory;

@Repository
public interface EmailVerificationHistoryRepository extends JpaRepository<EmailVerificationHistory, Long> {

    String ACCOUNT_KPI_QUERY = """
        SELECT
            COALESCE(COUNT(CASE WHEN status = 'VALID' THEN 1 END), 0) AS validEmails,
            COALESCE(COUNT(CASE WHEN status = 'INVALID' THEN 1 END), 0) AS invalidEmails,
            COALESCE(COUNT(CASE WHEN status = 'RISKY' THEN 1 END), 0) AS riskyEmails,
            COALESCE(COUNT(CASE WHEN status = 'UNKNOWN' THEN 1 END), 0) AS unknownEmails,
            COALESCE(COUNT(*), 0) AS totalProcessed,
            COALESCE(AVG(score), 0) AS qualityScore
        FROM
            xt_user_email_verification_history
        """;

    @Query(nativeQuery = true, value = ACCOUNT_KPI_QUERY)
    Optional<KpiQueryResult> getAccountKpis();

    Page<EmailVerificationHistory> findByBatch_Id(UUID batchId, Pageable pageable);

    String FIND_DISTINCT_LATEST_QUERY = """
        WITH RankedHistory AS (
            SELECT
                h.*,
                ROW_NUMBER() OVER (PARTITION BY h.email ORDER BY h.checked_at DESC) as rn
            FROM
                xt_user_email_verification_history h
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

    interface KpiQueryResult {
        long getValidEmails();
        long getInvalidEmails();
        long getRiskyEmails();
        long getUnknownEmails();
        long getTotalProcessed();
        BigDecimal getQualityScore();
    }

    interface DistinctLatestVerificationProjection {
        Long getId();
        String getEmail();
        String getDomain();
        String getStatus();
        int getScore();
        String getConfidence();
        OffsetDateTime getLastVerifiedAt();
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