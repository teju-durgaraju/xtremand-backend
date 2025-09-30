package com.xtremand.email.verification.service;

import com.xtremand.domain.entity.EmailVerificationHistory.SmtpCheckStatus;
import com.xtremand.domain.entity.EmailVerificationHistory.SmtpPingStatus;
import com.xtremand.email.verification.config.SmtpVerificationProperties;
import com.xtremand.email.verification.model.SmtpProbeResult;
import com.xtremand.email.verification.service.verifier.MxCheckProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.smtp.SMTPClient;
import org.apache.commons.net.smtp.SMTPReply;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SmtpProbeService {

    private final SmtpVerificationProperties config;
    private final MxCheckProvider mxCheckProvider;

    public SmtpProbeResult probe(String email) {
        if (!config.isEnabled()) {
            return SmtpProbeResult.notPerformed();
        }

        String domain = email.substring(email.indexOf("@") + 1);
        List<String> mxHosts = mxCheckProvider.getMxHosts(domain);
        if (mxHosts.isEmpty()) {
            return SmtpProbeResult.builder()
                    .status(SmtpCheckStatus.UNKNOWN)
                    .pingStatus(SmtpPingStatus.FAIL)
                    .logs("No MX records found for domain.")
                    .build();
        }

        StringBuilder sessionLogs = new StringBuilder();
        SmtpProbeResult finalResult = null;

        for (String mxHost : mxHosts) {
            try {
                finalResult = performSmtpCheck(email, mxHost, sessionLogs);
                // If we get a definitive result (DELIVERABLE or INVALID), we can stop.
            if (finalResult.getStatus() == SmtpCheckStatus.DELIVERABLE) {
                // Check for catch-all before breaking
                boolean isCatchAll = isCatchAllDomain(domain, mxHost, sessionLogs);
                if (isCatchAll) {
                    finalResult = SmtpProbeResult.builder()
                            .status(SmtpCheckStatus.CATCH_ALL)
                            .pingStatus(finalResult.getPingStatus())
                            .isCatchAll(true)
                            .logs(sessionLogs.toString())
                            .build();
                }
                break; // Stop after a definitive result
            }
            if (finalResult.getStatus() == SmtpCheckStatus.INVALID) {
                break; // Stop after a definitive result
                }
            } catch (Exception e) {
                log.warn("Exception during SMTP probe to {} for email {}: {}", mxHost, email, e.getMessage());
                sessionLogs.append("Exception connecting to ").append(mxHost).append(": ").append(e.getMessage()).append("\n");
                finalResult = SmtpProbeResult.builder()
                        .status(SmtpCheckStatus.UNKNOWN)
                        .pingStatus(SmtpPingStatus.FAIL)
                        .logs(sessionLogs.toString())
                        .build();
            }
        }
        return finalResult;
    }

    private SmtpProbeResult performSmtpCheck(String email, String mxHost, StringBuilder sessionLogs) throws IOException {
        SMTPClient client = new SMTPClient();
        client.setConnectTimeout(config.getConnectionTimeout());
        client.setDefaultTimeout(config.getReadTimeout());

        try {
            client.connect(mxHost);
            sessionLogs.append("Connected to ").append(mxHost).append(". Response: ").append(client.getReplyString());

            if (!SMTPReply.isPositiveCompletion(client.getReplyCode())) {
                return SmtpProbeResult.builder().status(SmtpCheckStatus.UNKNOWN).pingStatus(SmtpPingStatus.FAIL).logs(sessionLogs.toString()).build();
            }

            client.helo(config.getHeloHost());
            sessionLogs.append("HELO ").append(config.getHeloHost()).append(". Response: ").append(client.getReplyString());

            client.setSender(config.getMailFrom());
            sessionLogs.append("MAIL FROM:<").append(config.getMailFrom()).append(">. Response: ").append(client.getReplyString());

            // Fallback: SMTP Ping successful if we got this far
            SmtpPingStatus pingStatus = SmtpPingStatus.SUCCESS;

            // Now, the critical check
            int rcptToReply = client.rcpt(email);
            sessionLogs.append("RCPT TO:<").append(email).append(">. Response: ").append(client.getReplyString());

            if (SMTPReply.isPositiveCompletion(rcptToReply)) {
                return SmtpProbeResult.builder().status(SmtpCheckStatus.DELIVERABLE).pingStatus(pingStatus).logs(sessionLogs.toString()).build();
            } else if (SMTPReply.isNegativePermanent(rcptToReply)) {
                // 550: User not found - a definitive INVALID
                return SmtpProbeResult.builder().status(SmtpCheckStatus.INVALID).pingStatus(pingStatus).logs(sessionLogs.toString()).build();
            } else if (SMTPReply.isNegativeTransient(rcptToReply)) {
                // 4xx: Greylisting - could be temporary
                // Implement retry logic here if needed
                log.info("Greylisting detected for {} on {}. Reply: {}", email, mxHost, client.getReplyString());
                return SmtpProbeResult.builder().status(SmtpCheckStatus.UNKNOWN).pingStatus(pingStatus).isGreylisted(true).logs(sessionLogs.toString()).build();
            } else {
                // Any other case is treated as UNKNOWN
                return SmtpProbeResult.builder().status(SmtpCheckStatus.UNKNOWN).pingStatus(pingStatus).logs(sessionLogs.toString()).build();
            }

        } finally {
            if (client.isConnected()) {
                client.disconnect();
            }
        }
    }

    // A simple catch-all check can be done by sending a probe to a likely non-existent email
    private boolean isCatchAllDomain(String domain, String mxHost, StringBuilder sessionLogs) {
        // Use a likely non-existent email to test for catch-all
        String randomEmail = "random-probe-" + UUID.randomUUID().toString().substring(0, 12) + "@" + domain;
        sessionLogs.append("\n--- Performing catch-all check with email: ").append(randomEmail).append(" ---\n");
        try {
            // We use a temporary log for this check to not pollute the main log unless we find something.
            SmtpProbeResult catchAllResult = performSmtpCheck(randomEmail, mxHost, new StringBuilder());
            boolean isCatchAll = catchAllResult.getStatus() == SmtpCheckStatus.DELIVERABLE;
            sessionLogs.append("Catch-all check result: ").append(isCatchAll).append(". Response: ").append(catchAllResult.getLogs()).append("\n");
            return isCatchAll;
        } catch (IOException e) {
            sessionLogs.append("Catch-all check failed with exception: ").append(e.getMessage()).append("\n");
            return false;
        }
    }
}