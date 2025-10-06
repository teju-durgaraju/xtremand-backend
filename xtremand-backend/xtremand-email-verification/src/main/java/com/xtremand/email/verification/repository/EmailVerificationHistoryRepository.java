package com.xtremand.email.verification.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xtremand.domain.entity.EmailVerificationHistory;

@Repository
public interface EmailVerificationHistoryRepository extends JpaRepository<EmailVerificationHistory, Long> {
    String CHART_DATA_QUERY = """
        SELECT
            TO_CHAR(h.checked_at, :groupBy) AS period,
            h.status AS status,
            COUNT(h.id) AS count
        FROM
            xtremand_production.xt_user_email_verification_history h
        JOIN xtremand_production.xt_users u ON h.user_id = u.id
        WHERE
            u.email = :userEmail AND h.checked_at >= :startDate
        GROUP BY
            period, h.status
        ORDER BY
            period
    """;

    @Query(nativeQuery = true, value = CHART_DATA_QUERY)
    List<ChartDataProjection> findChartDataByUserEmail(
        @Param("userEmail") String userEmail,
        @Param("startDate") Instant startDate,
        @Param("groupBy") String groupBy
    );

    interface ChartDataProjection {
        String getPeriod();
        String getStatus();
        long getCount();
    }

    String ACCOUNT_KPI_QUERY = """
            SELECT
                COALESCE(COUNT(CASE WHEN h.status = 'VALID' THEN 1 END), 0) AS validEmails,
                COALESCE(COUNT(CASE WHEN h.status = 'INVALID' THEN 1 END), 0) AS invalidEmails,
                COALESCE(COUNT(CASE WHEN h.status = 'RISKY' THEN 1 END), 0) AS riskyEmails,
                COALESCE(COUNT(CASE WHEN h.status = 'UNKNOWN' THEN 1 END), 0) AS unknownEmails,
                COALESCE(COUNT(h.id), 0) AS totalProcessed,
                COALESCE(AVG(h.score), 0) AS qualityScore
            FROM
                xtremand_production.xt_user_email_verification_history h
            JOIN xtremand_production.xt_users u ON h.user_id = u.id
            WHERE u.email = :userEmail
            """;

    @Query(nativeQuery = true, value = ACCOUNT_KPI_QUERY)
    Optional<KpiQueryResult> getAccountKpis(@Param("userEmail") String userEmail);

    Page<EmailVerificationHistory> findByBatchIdAndUser_Email(UUID batchId, String email, Pageable pageable);

    String FIND_DISTINCT_LATEST_QUERY = """
            WITH RankedHistory AS (
                SELECT
                    h.*,
                    ROW_NUMBER() OVER (PARTITION BY h.email ORDER BY h.checked_at DESC) as rn
                FROM
                    xtremand_production.xt_user_email_verification_history h
                JOIN xtremand_production.xt_users u ON h.user_id = u.id
                WHERE u.email = :userEmail
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
    List<DistinctLatestVerificationProjection> findDistinctLatest(@Param("userEmail") String userEmail);

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

    @Query("SELECT MIN(h.checkedAt) AS earliest, MAX(h.checkedAt) AS latest FROM EmailVerificationHistory h WHERE h.user.id = :userId")
    Optional<DateRangeProjection> findDateRangeByUserId(@Param("userId") Long userId);

    interface DateRangeProjection {
        Instant getEarliest();
        Instant getLatest();
    }
}