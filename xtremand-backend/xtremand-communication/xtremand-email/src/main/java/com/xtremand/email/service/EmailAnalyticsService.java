package com.xtremand.email.service;

import com.xtremand.common.exception.RecordNotFoundException;
import com.xtremand.contact.service.ContactService;
import com.xtremand.domain.dto.EmailAnalyticsSummaryDto;
import com.xtremand.domain.dto.EmailHistoryDto;
import com.xtremand.domain.entity.Campaign;
import com.xtremand.domain.entity.ContactList;
import com.xtremand.domain.entity.ContactListEmailAnalytics;
import com.xtremand.domain.entity.EmailAnalytics;
import com.xtremand.domain.enums.EmailStatus;
import com.xtremand.email.repository.ContactListEmailAnalyticsRepository;
import com.xtremand.email.repository.EmailAnalyticsRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class EmailAnalyticsService {

	@Value("${email.tracking.url}")
	private String trackingUrl;

	private final EmailAnalyticsRepository repo;
	
	private final ContactService contactService;
	
	private final ContactListEmailAnalyticsRepository mappingRepo;


	public EmailAnalyticsService(EmailAnalyticsRepository repo, ContactService contactService,ContactListEmailAnalyticsRepository mappingRepo) {
		this.repo = repo;
		this.contactService = contactService;
		this.mappingRepo = mappingRepo;
	}

	public EmailAnalytics logEmailSent(Long contactId, String email, String subject,String body, UUID trackingId, Campaign campaign,String prompt) {
		EmailAnalytics analytics = new EmailAnalytics();
		analytics.setContactId(contactId);
		analytics.setEmail(email);
		analytics.setSubject(subject);
		analytics.setBody(body);
		analytics.setPrompt(prompt);
		analytics.setSentAt(LocalDateTime.now());
		analytics.setStatus(EmailStatus.OPENED);
		analytics.setOpenedAt(LocalDateTime.now());
		trackingId = UUID.randomUUID();
		analytics.setTrackingId(trackingId);
		String trackedLink = trackingUrl.replace("{trackingId}", trackingId.toString());
		analytics.setTrackingUrl(trackedLink);
		analytics.setCampaign(campaign);
		EmailAnalytics a = new EmailAnalytics();
		a = repo.save(analytics);
		repo.flush(); 
		return a;
	}

	public void logEmailNotSent(Long contactId, String email, String subject,String body, UUID trackingId, Campaign campaign,String prompt) {
		EmailAnalytics analytics = new EmailAnalytics();
		analytics.setContactId(contactId);
		analytics.setEmail(email);
		analytics.setSubject(subject);
		analytics.setBody(body);
		analytics.setPrompt(prompt);
		analytics.setStatus(EmailStatus.NOT_SENT);
		analytics.setCampaign(campaign);
		repo.save(analytics);
	}

	
	public void logClicked(UUID trackingId) {
		repo.findByTrackingId(trackingId).ifPresent(a -> {
			a.setStatus(EmailStatus.CLICKED);
			a.setClickedAt(LocalDateTime.now());
			repo.save(a);
		});
	}

	public void logBounced(UUID trackingId) {
		repo.findByTrackingId(trackingId).ifPresent(a -> {
			a.setStatus(EmailStatus.BOUNCED);
			a.setBouncedAt(LocalDateTime.now());
			repo.save(a);
		});
	}

	public void logReplied(UUID trackingId) {
		repo.findByTrackingId(trackingId).ifPresent(a -> {
			a.setStatus(EmailStatus.REPLIED);
			a.setRepliedAt(LocalDateTime.now());
			repo.save(a);
		});
	}
	
	public EmailAnalyticsSummaryDto getEmailAnalyticsSummary() {
		List<EmailAnalytics> allRecords = repo.findAll();
		long totalEmails = allRecords.size();
		long sentCount = allRecords.stream().filter(r -> r.getSentAt() != null).count();
		long openedCount = allRecords.stream().filter(r -> r.getOpenedAt() != null).count();
		long clickedCount = allRecords.stream().filter(r -> r.getClickedAt() != null).count();
		long bouncedCount = allRecords.stream().filter(r -> r.getBouncedAt() != null).count();
		long notSentCount = totalEmails - sentCount;
		long repliedCount = allRecords.stream().filter(r -> r.getRepliedAt() != null).count();
		double openRate = sentCount == 0 ? 0 : (double) openedCount / sentCount * 100;
		double clickRate = sentCount == 0 ? 0 : (double) clickedCount / sentCount * 100;
		double bounceRate = sentCount == 0 ? 0 : (double) bouncedCount / sentCount * 100;
		double notSentRate = totalEmails == 0 ? 0 : (double) notSentCount / totalEmails * 100;
		 double replyRate = sentCount == 0 ? 0 : (double) repliedCount / sentCount * 100;
		return new EmailAnalyticsSummaryDto(totalEmails, sentCount, notSentCount, openedCount, clickedCount,
				bouncedCount,repliedCount, openRate, bounceRate, notSentRate, clickRate,replyRate);
	}
	
	
	public List<EmailHistoryDto> getAllEmailHistory() {
		List<EmailAnalytics> allRecords = repo.findAll();
		List<EmailHistoryDto> history = new ArrayList<>();
		for (EmailAnalytics record : allRecords) {
			if (record.getSentAt() != null) {
				history.add(new EmailHistoryDto(record.getId(),record.getEmail(), EmailStatus.SENT, record.getSubject(),null,
						record.getSentAt()));
			}else {
				history.add(new EmailHistoryDto(record.getId(),record.getEmail(), EmailStatus.NOT_SENT, record.getSubject(),null, null));
			}
		}
		history.sort(
				Comparator.comparing(EmailHistoryDto::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder())));
		return history;
	}

	
	public EmailHistoryDto getEmailHistoryById(Long id) {
		Optional<EmailAnalytics> optionalRecord = repo.findById(id);
		if (optionalRecord.isPresent()) {
			EmailAnalytics record = optionalRecord.get();
			if (record.getSentAt() != null) {
				return new EmailHistoryDto(record.getId(), record.getEmail(), EmailStatus.SENT, record.getSubject(),
						record.getBody(), record.getSentAt());
			} else {
				return new EmailHistoryDto(record.getId(), record.getEmail(), EmailStatus.NOT_SENT, record.getSubject(),
						record.getBody(), null);
			}
		} else {

			throw new RecordNotFoundException("No email analytics record with id: " + id);
		}
	}
	
	
	public void saveContactAnalyticsMapping(Long contactListId, EmailAnalytics emailAnalytics) {
	    ContactListEmailAnalytics mapping = new ContactListEmailAnalytics();
	    // Fetch and validate ContactList
	    ContactList list = contactService.getListById(contactListId);
	    if (list == null) {
	        throw new RecordNotFoundException("No contact list found with id: " + contactListId);
	    }
	    mapping.setContactList(list);

	    // Use the managed EmailAnalytics entity or getReferenceById which does not hit DB but returns proxy
	    EmailAnalytics analytics = repo.getReferenceById(emailAnalytics.getId());
	    mapping.setEmailAnalytics(analytics);

	    // Save and flush to immediately detect FK issues
	    mappingRepo.save(mapping);
	    mappingRepo.flush();
	}


	
}
