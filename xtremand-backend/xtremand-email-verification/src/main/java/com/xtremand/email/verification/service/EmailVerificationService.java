package com.xtremand.email.verification.service;

import com.xtremand.domain.entity.EmailVerificationBatch;
import com.xtremand.domain.entity.EmailVerificationHistory;
import com.xtremand.email.verification.config.ScoringProperties;
import com.xtremand.email.verification.model.SmtpProbeResult;
import com.xtremand.email.verification.model.VerificationResult;
import com.xtremand.email.verification.repository.EmailVerificationHistoryRepository;
import com.xtremand.email.verification.service.verifier.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final SyntaxCheckProvider syntaxCheckProvider;
    private final MxCheckProvider mxCheckProvider;
    private final DisposableDomainProvider disposableDomainProvider;
    private final RoleBasedEmailProvider roleBasedEmailProvider;
    private final BlacklistProvider blacklistProvider;
    private final CatchAllProvider catchAllProvider;
    private final SmtpProbeService smtpProbeService;
    private final ScoringProperties scoringProperties;
    private final EmailVerificationHistoryRepository historyRepository;

    @Transactional
    public VerificationResult verifyEmail(String email, Long userId) {
        return this.verifyEmail(email, userId, null);
    }

    @Transactional
    public VerificationResult verifyEmail(String email, Long userId, EmailVerificationBatch batch) {
        // 1. Initial Syntax Check
        boolean isSyntaxValid = syntaxCheckProvider.isValid(email);
        String domain = null;
        if (email != null && email.contains("@")) {
            domain = email.substring(email.indexOf("@") + 1);
        }

        if (!isSyntaxValid) {
            VerificationResult result = VerificationResult.builder().email(email).syntaxCheck(false).score(0).status(EmailVerificationHistory.VerificationStatus.INVALID).build();
            saveHistory(result, userId, domain, batch);
            return result;
        }

        // 2. Perform all checks
        boolean isDisposable = disposableDomainProvider.isDisposable(email);
        boolean isBlacklisted = blacklistProvider.isBlacklisted(email);
        boolean isRoleBased = roleBasedEmailProvider.isRoleBased(email);
        boolean hasMxRecords = mxCheckProvider.hasMxRecords(domain);
        boolean isKnownCatchAll = catchAllProvider.isKnownCatchAll(domain);

        // 3. SMTP Probe (if applicable)
        SmtpProbeResult smtpResult = (hasMxRecords && !isDisposable && !isBlacklisted)
                ? smtpProbeService.probe(email)
                : SmtpProbeResult.notPerformed();

        // 4. Build the result object
        VerificationResult.VerificationResultBuilder resultBuilder = VerificationResult.builder()
                .email(email)
                .syntaxCheck(isSyntaxValid)
                .disposableCheck(!isDisposable)
                .blacklistCheck(!isBlacklisted)
                .roleBasedCheck(!isRoleBased)
                .mxCheck(hasMxRecords)
                .catchAllCheck(!isKnownCatchAll && !smtpResult.isCatchAll())
                .smtpProbeResult(smtpResult);

        // 5. Calculate Score and Determine Status
        calculateScoreAndStatus(resultBuilder, domain, hasMxRecords);
        VerificationResult finalResult = resultBuilder.build();

        // 6. Save to history and return
        saveHistory(finalResult, userId, domain, batch);
        return finalResult;
    }

    private void calculateScoreAndStatus(VerificationResult.VerificationResultBuilder builder, String domain, boolean hasMxRecords) {
        VerificationResult temp = builder.build(); // Build to access fields
        int score = 0;
        ScoringProperties.Weights weights = scoringProperties.getWeights();

        if (temp.isSyntaxCheck()) score += weights.getSyntaxValid();
        if (temp.isMxCheck()) score += weights.getMxValid();
        if (temp.isDisposableCheck()) score += weights.getNotDisposable();
        if (temp.isRoleBasedCheck()) score += weights.getNotRoleBased();
        if (temp.isBlacklistCheck()) score += weights.getNotBlacklisted();
        if (temp.isCatchAllCheck()) score += weights.getNotCatchAll();

        if (temp.getSmtpProbeResult().getStatus() == EmailVerificationHistory.SmtpCheckStatus.DELIVERABLE) {
            score += weights.getSmtpDeliverable();
        } else if (temp.getSmtpProbeResult().getPingStatus() == EmailVerificationHistory.SmtpPingStatus.SUCCESS) {
            score += weights.getSmtpPingSuccess();
        }

        builder.score(Math.min(score, 100));

        // Determine Status, Confidence, and Recommendation
        if (!temp.isSyntaxCheck() || !temp.isBlacklistCheck() || temp.getSmtpProbeResult().getStatus() == EmailVerificationHistory.SmtpCheckStatus.INVALID) {
            builder.status(EmailVerificationHistory.VerificationStatus.INVALID).recommendation(EmailVerificationHistory.Recommendation.REJECT);
        } else if (!temp.isDisposableCheck()) {
            builder.status(EmailVerificationHistory.VerificationStatus.DISPOSABLE).recommendation(EmailVerificationHistory.Recommendation.REJECT);
        } else if (temp.getSmtpProbeResult().getStatus() == EmailVerificationHistory.SmtpCheckStatus.DELIVERABLE) {
            builder.status(EmailVerificationHistory.VerificationStatus.VALID).recommendation(EmailVerificationHistory.Recommendation.ACCEPT);
        } else if (temp.getSmtpProbeResult().getStatus() == EmailVerificationHistory.SmtpCheckStatus.CATCH_ALL || !temp.isCatchAllCheck()) {
            builder.status(EmailVerificationHistory.VerificationStatus.RISKY).recommendation(EmailVerificationHistory.Recommendation.REVIEW);
        } else if (hasMxRecords) {
            builder.status(EmailVerificationHistory.VerificationStatus.UNKNOWN).recommendation(EmailVerificationHistory.Recommendation.REVIEW);
        } else {
            builder.status(EmailVerificationHistory.VerificationStatus.INVALID).recommendation(EmailVerificationHistory.Recommendation.REJECT);
        }

        ScoringProperties.Thresholds thresholds = scoringProperties.getThresholds();
        if (score >= thresholds.getConfidenceHigh()) {
            builder.confidence(EmailVerificationHistory.Confidence.HIGH);
        } else if (score >= thresholds.getConfidenceMedium()) {
            builder.confidence(EmailVerificationHistory.Confidence.MEDIUM);
        } else {
            builder.confidence(EmailVerificationHistory.Confidence.LOW);
        }

        Map<String, Object> details = new HashMap<>();
        details.put("mx_hosts", mxCheckProvider.getMxHosts(domain));
        builder.details(details);
    }

    private void saveHistory(VerificationResult result, Long userId, String domain, EmailVerificationBatch batch) {
        EmailVerificationHistory history = new EmailVerificationHistory();
        history.setUserId(userId);
        history.setEmail(result.getEmail());
        history.setDomain(domain);
        history.setBatch(batch);
        history.setStatus(result.getStatus());
        history.setRecommendation(result.getRecommendation());
        history.setScore(result.getScore());
        history.setConfidence(result.getConfidence());
        history.setSyntaxCheck(result.isSyntaxCheck());
        history.setMxCheck(result.isMxCheck());
        history.setDisposableCheck(!result.isDisposableCheck()); // DB stores if it *is* disposable
        history.setRoleBasedCheck(!result.isRoleBasedCheck()); // DB stores if it *is* role-based
        history.setBlacklistCheck(!result.isBlacklistCheck()); // DB stores if it *is* blacklisted
        history.setCatchAllCheck(!result.isCatchAllCheck()); // DB stores if it *is* catch-all

        if (result.getSmtpProbeResult() != null) {
            SmtpProbeResult smtp = result.getSmtpProbeResult();
            history.setSmtpCheckStatus(smtp.getStatus());
            history.setSmtpPingStatus(smtp.getPingStatus());
            history.setCatchAll(smtp.isCatchAll());
            history.setGreylisted(smtp.isGreylisted());
            history.setSmtpLogs(smtp.getLogs());
        } else {
            history.setSmtpCheckStatus(EmailVerificationHistory.SmtpCheckStatus.NOT_PERFORMED);
            history.setSmtpPingStatus(EmailVerificationHistory.SmtpPingStatus.NOT_PERFORMED);
        }
        history.setDetails(result.getDetails());
        historyRepository.save(history);
    }
}