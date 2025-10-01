package com.xtremand.email.verification.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.domain.entity.EmailVerificationBatch;
import com.xtremand.domain.entity.EmailVerificationHistory;
import com.xtremand.domain.entity.User;
import com.xtremand.email.verification.config.AsyncConfig;
import com.xtremand.email.verification.model.VerificationResult;
import com.xtremand.email.verification.repository.EmailVerificationBatchRepository;
import com.xtremand.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchVerificationService {

	private final EmailVerificationService emailVerificationService;
	private final EmailVerificationBatchRepository batchRepository;
	private final UserRepository userRepository;

	@Transactional
	public EmailVerificationBatch startBatchVerification(List<String> emails) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String userEmail = authentication.getName();
		log.info("Starting batch verification for {} emails. User Email: {}", emails.size(), userEmail);

		// 1. Create and save the initial batch record
		EmailVerificationBatch batch = new EmailVerificationBatch();
		batch.setTotalEmails(emails.size());
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));
		batch.setUser(user);
		EmailVerificationBatch savedBatch = batchRepository.save(batch);

		// 2. Start the async processing
		processBatch(emails, savedBatch);

		// 3. Return the initial batch object with its ID
		return savedBatch;
	}

	@Async(AsyncConfig.BATCH_VERIFICATION_EXECUTOR)
	@Transactional
	public CompletableFuture<Void> processBatch(List<String> emails, EmailVerificationBatch batch) {
		log.info("Executing async batch verification for {} emails. Batch ID: {}", emails.size(), batch.getId());
		List<VerificationResult> results = new ArrayList<>();

		for (String email : emails) {
            try {
                // Pass the batch object to the verification service
                VerificationResult result = emailVerificationService.verifyEmail(email, batch);
                results.add(result);
            } catch (Exception e) {
                log.error("Error verifying email '{}' during batch process. Batch ID: {}. Error: {}", email, batch.getId(), e.getMessage());
                // Create a failed result to ensure it's counted as invalid
                results.add(VerificationResult.builder().email(email).status(EmailVerificationHistory.VerificationStatus.INVALID).build());
            }
        }

        updateBatchStatistics(batch, results);

        log.info("Async batch verification complete for Batch ID: {}", batch.getId());
        return CompletableFuture.completedFuture(null);
    }

    private void updateBatchStatistics(EmailVerificationBatch batch, List<VerificationResult> results) {
        int validCount = 0;
        int invalidCount = 0;
        int deliverableCount = 0;
        int disposableCount = 0;

        for (VerificationResult result : results) {
            if (result.getStatus() == EmailVerificationHistory.VerificationStatus.VALID) {
                validCount++;
            }
            if (result.getStatus() == EmailVerificationHistory.VerificationStatus.INVALID) {
                invalidCount++;
            }
            if (result.getStatus() == EmailVerificationHistory.VerificationStatus.DISPOSABLE) {
                disposableCount++;
            }
            if (result.getSmtpProbeResult() != null && result.getSmtpProbeResult().getStatus() == EmailVerificationHistory.SmtpCheckStatus.DELIVERABLE) {
                deliverableCount++;
            }
        }

        // Re-fetch the batch entity to ensure it's managed in the current transaction
        EmailVerificationBatch managedBatch = batchRepository.findById(batch.getId()).orElseThrow(() -> {
            log.error("Batch with ID {} not found for update after processing.", batch.getId());
            return new IllegalStateException("Batch not found for update");
        });

        managedBatch.setValidEmails(validCount);
        managedBatch.setInvalidEmails(invalidCount);
        managedBatch.setDisposableEmails(disposableCount);
        managedBatch.setDeliverableEmails(deliverableCount);
        managedBatch.calculateValidRate();

        batchRepository.save(managedBatch);
        log.info("Successfully updated statistics for Batch ID: {}", managedBatch.getId());
    }
}