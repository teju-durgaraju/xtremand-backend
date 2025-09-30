package com.xtremand.email.verification.repository;

import com.xtremand.domain.entity.EmailVerificationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

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
            syntax_check,
            mx_check,
            disposable_check,
            role_based_check,
            catch_all_check,
            blacklist_check,
            (case when smtp_check_status = 'DELIVERABLE' then true else false end) AS smtp_check,
            (case when smtp_ping_status = 'SUCCESS' then true else false end) AS smtp_ping
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
        ZonedDateTime getLastVerifiedAt();
        boolean getSyntax_check();
        boolean getMx_check();
        boolean getDisposable_check();
        boolean getRole_based_check();
        boolean getCatch_all_check();
        boolean getBlacklist_check();
        boolean getSmtp_check();
        boolean getSmtp_ping();
    }
}