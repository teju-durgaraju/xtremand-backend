package com.xtremand.email.verification.service;

import com.xtremand.domain.entity.EmailValidationRule;
import com.xtremand.domain.entity.UserEmailVerificationHistory;
import com.xtremand.domain.enums.Confidence;
import com.xtremand.domain.enums.RuleType;
import com.xtremand.domain.enums.VerificationStatus;
import com.xtremand.email.verification.dto.EmailVerificationChecks;
import com.xtremand.email.verification.dto.EmailVerifierOutput;
import com.xtremand.email.verification.dto.KpiDto;
import com.xtremand.email.verification.repository.EmailValidationRuleRepository;
import com.xtremand.email.verification.repository.UserEmailVerificationHistoryRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xbill.DNS.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailVerificationService {

    private final UserEmailVerificationHistoryRepository historyRepository;
    private final EmailValidationRuleRepository ruleRepository;

    @Value("${email.verification.score.syntax:20}")
    private int syntaxScore;
    @Value("${email.verification.score.mx:20}")
    private int mxScore;
    @Value("${email.verification.score.disposable:15}")
    private int disposableScore;
    @Value("${email.verification.score.role-based:10}")
    private int roleBasedScore;
    @Value("${email.verification.score.catch-all:10}")
    private int catchAllScore;
    @Value("${email.verification.score.blacklist:15}")
    private int blacklistScore;
    @Value("${email.verification.score.smtp:10}")
    private int smtpScore;

    private final Set<String> disposableDomains = new HashSet<>();
    private final Set<String> roleBasedPrefixes = new HashSet<>();
    private final Set<String> catchAllDomains = new HashSet<>();
    private final Set<String> blacklistedEmails = new HashSet<>();
    private final Set<String> blacklistedDomains = new HashSet<>();

    private static final Pattern EMAIL_SYNTAX_PATTERN = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

    @PostConstruct
    public void loadValidationRules() {
        disposableDomains.addAll(getValuesForRuleType(RuleType.DISPOSABLE));
        roleBasedPrefixes.addAll(getValuesForRuleType(RuleType.ROLE_BASED));
        catchAllDomains.addAll(getValuesForRuleType(RuleType.CATCH_ALL));
        blacklistedEmails.addAll(getValuesForRuleType(RuleType.BLACKLIST_EMAIL));
        blacklistedDomains.addAll(getValuesForRuleType(RuleType.BLACKLIST_DOMAIN));
    }

    private List<String> getValuesForRuleType(RuleType ruleType) {
        return ruleRepository.findByRuleType(ruleType).stream()
                .map(EmailValidationRule::getValue)
                .collect(Collectors.toList());
    }

    public EmailVerifierOutput verify(String email) {
        boolean syntaxCheck = EMAIL_SYNTAX_PATTERN.matcher(email).matches();
        if (!syntaxCheck) {
            EmailVerificationChecks checks = EmailVerificationChecks.builder().build();
            UserEmailVerificationHistory history = UserEmailVerificationHistory.builder()
                    .email(email)
                    .score(0)
                    .confidence(Confidence.LOW)
                    .checkedAt(LocalDateTime.now())
                    .status(VerificationStatus.INVALID)
                    .syntaxCheck(false)
                    .build();
            historyRepository.save(history);
            return EmailVerifierOutput.builder()
                    .email(email)
                    .status(VerificationStatus.INVALID)
                    .score(0)
                    .confidence(Confidence.LOW)
                    .checks(checks)
                    .message("Invalid email syntax.")
                    .build();
        }

        String domain = getDomainFromEmail(email);
        String prefix = getPrefixFromEmail(email);

        boolean mxCheck = hasMxRecord(domain);
        boolean disposableCheck = disposableDomains.contains(domain);
        boolean roleBasedCheck = prefix != null && roleBasedPrefixes.contains(prefix + "@");
        boolean catchAllCheck = catchAllDomains.contains(domain);
        boolean blacklistCheck = blacklistedEmails.contains(email) || blacklistedDomains.contains(domain);
        boolean smtpCheck = false; // Placeholder for now

        int score = 0;
        if (syntaxCheck) score += syntaxScore;
        if (mxCheck) score += mxScore;
        if (!disposableCheck) score += disposableScore;
        if (!roleBasedCheck) score += roleBasedScore;
        if (!catchAllCheck) score += catchAllScore;
        if (!blacklistCheck) score += blacklistScore;
        if (smtpCheck) score += smtpScore;

        Confidence confidence = Confidence.LOW;
        if (score >= 90) {
            confidence = Confidence.HIGH;
        } else if (score >= 70) {
            confidence = Confidence.MEDIUM;
        }

        VerificationStatus status = VerificationStatus.VALID;
        if (score < 70) {
            status = VerificationStatus.RISKY;
        }
        if (!syntaxCheck || !mxCheck) {
            status = VerificationStatus.INVALID;
        }
        if (disposableCheck) {
            status = VerificationStatus.DISPOSABLE;
        }
        if (blacklistCheck) {
            status = VerificationStatus.INVALID;
        }


        UserEmailVerificationHistory history = UserEmailVerificationHistory.builder()
                .email(email)
                .score(score)
                .confidence(confidence)
                .checkedAt(LocalDateTime.now())
                .status(status)
                .syntaxCheck(syntaxCheck)
                .mxCheck(mxCheck)
                .disposableCheck(disposableCheck)
                .roleBasedCheck(roleBasedCheck)
                .catchAllCheck(catchAllCheck)
                .blacklistCheck(blacklistCheck)
                .smtpCheck(smtpCheck)
                .build();
        historyRepository.save(history);

        EmailVerificationChecks checks = EmailVerificationChecks.builder()
                .syntaxCheck(syntaxCheck)
                .mxCheck(mxCheck)
                .disposableCheck(disposableCheck)
                .roleBasedCheck(roleBasedCheck)
                .catchAllCheck(catchAllCheck)
                .blacklistCheck(blacklistCheck)
                .smtpCheck(smtpCheck)
                .build();

        return EmailVerifierOutput.builder()
                .email(email)
                .status(status)
                .score(score)
                .confidence(confidence)
                .checks(checks)
                .message("Email verification completed.")
                .build();
    }

    public KpiDto getKpis() {
        long totalVerifications = historyRepository.count();
        long validEmails = historyRepository.countByStatus(VerificationStatus.VALID);
        long invalidEmails = historyRepository.countByStatus(VerificationStatus.INVALID);
        long riskyEmails = historyRepository.countByStatus(VerificationStatus.RISKY);
        long disposableEmails = historyRepository.countByStatus(VerificationStatus.DISPOSABLE);
        Double averageScore = historyRepository.getAverageScore();

        return KpiDto.builder()
                .totalVerifications(totalVerifications)
                .validEmails(validEmails)
                .invalidEmails(invalidEmails)
                .riskyEmails(riskyEmails)
                .disposableEmails(disposableEmails)
                .averageScore(averageScore != null ? averageScore : 0.0)
                .build();
    }

    private boolean hasMxRecord(String domain) {
        try {
            Lookup lookup = new Lookup(domain, Type.MX);
            org.xbill.DNS.Record[] records = lookup.run();
            return records != null && records.length > 0;
        } catch (TextParseException e) {
            return false;
        }
    }

    private String getDomainFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "";
        }
        return email.substring(email.indexOf("@") + 1);
    }

    private String getPrefixFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return null;
        }
        return email.substring(0, email.indexOf("@"));
    }
}
