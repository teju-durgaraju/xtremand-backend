package com.xtremand.email.service;

import com.xtremand.domain.dto.CampaignAnalyticsDto;
import com.xtremand.domain.dto.CampaignCreateRequest;
import com.xtremand.domain.dto.CampaignDto;
import com.xtremand.domain.dto.Overview;
import com.xtremand.common.exception.RecordNotFoundException;
import com.xtremand.domain.entity.*;
import com.xtremand.domain.util.XtremandUtil;
import com.xtremand.email.repository.*;
import com.xtremand.user.repository.UserRepository;
import com.xtremand.contact.repository.ContactListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;

@Service
@Transactional
public class CampaignService {

	private final CampaignRepository campaignRepo;
	private final ContactListRepository contactListRepo;
	private final EmailService emailService;
	private final EmailTemplateRepository emailTemplateRepo;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private XtremandUtil xtremandUtil;
	
	@Autowired
	private EmailAnalyticsRepository  emailAnalyticsRepository;
	
	public CampaignService(CampaignRepository campaignRepo, ContactListRepository contactListRepo,
			EmailService emailService, EmailTemplateRepository emailTemplateRepo) {
		this.campaignRepo = campaignRepo;
		this.contactListRepo = contactListRepo;
		this.emailService = emailService;
		this.emailTemplateRepo = emailTemplateRepo;
	}

	public Campaign createCampaign(CampaignCreateRequest req, Authentication authentication) {
	    ContactList contactList = contactListRepo.findById(req.getContactListId()).orElseThrow(
	            () -> new RecordNotFoundException("Contact list not found with id: " + req.getContactListId()));
	    EmailTemplate emailTemplate = emailTemplateRepo.findById(req.getTemplateId()).orElseThrow(
	            () -> new RecordNotFoundException("Email template not found with id: " + req.getTemplateId()));

	    User user = userRepository.fetchByUsername(authentication.getName());

	    Campaign campaign = new Campaign();
	    campaign.setName(req.getName());
	    campaign.setType(req.getType());
	    campaign.setContactList(contactList);
	    campaign.setEmailTemplate(emailTemplate);
	    campaign.setScheduledAt(req.getScheduledAt());
	    campaign.setCreatedAt(LocalDate.now());
	    campaign.setCreatedBy(user);
	    campaign.setContentStrategy(req.getContentStrategy());
	    campaign.setAiPersonalization(req.isAiPersonalization());
	    campaign = campaignRepo.save(campaign);
	    return campaign;
	}

	public Campaign updateCampaign(Long id, CampaignCreateRequest req, Authentication authentication) {
	    Campaign existingCampaign = getCampaignById(id);
	    ContactList contactList = contactListRepo.findById(req.getContactListId()).orElseThrow(
	            () -> new RecordNotFoundException("Contact list not found with id: " + req.getContactListId()));
	    EmailTemplate emailTemplate = emailTemplateRepo.findById(req.getTemplateId()).orElseThrow(
	            () -> new RecordNotFoundException("Email template not found with id: " + req.getTemplateId()));

	    User user = userRepository.fetchByUsername(authentication.getName());

	    existingCampaign.setName(req.getName());
	    existingCampaign.setType(req.getType());
	    existingCampaign.setContactList(contactList);
	    existingCampaign.setEmailTemplate(emailTemplate);
	    existingCampaign.setScheduledAt(req.getScheduledAt());
	    existingCampaign.setContentStrategy(req.getContentStrategy());
	    existingCampaign.setAiPersonalization(req.isAiPersonalization());
	    existingCampaign.setUpdatedAt(LocalDate.now());
	    existingCampaign.setUpdatedBy(user);

	    return campaignRepo.save(existingCampaign);
	}

	public void deleteCampaign(Long id) {
	    if (!campaignRepo.existsById(id)) {
	        throw new RecordNotFoundException("Campaign not found with id: " + id);
	    }
	    campaignRepo.deleteById(id);
	}

	public Campaign getCampaignById(Long id) {
		return campaignRepo.findById(id)
				.orElseThrow(() -> new RecordNotFoundException("Campaign not found with id: " + id));
	}
	
	
	
	@Scheduled(fixedRate = 60000)
	public void sendScheduledCampaigns() {
		if (xtremandUtil.isProfileActive()) {
			List<Campaign> campaignsToSend = campaignRepo.findByScheduledAtBeforeAndSentFalse(LocalDateTime.now());
			for (Campaign campaign : campaignsToSend) {
				launchCampaign(campaign);
			}
		}
	}

	public String launchCampaign(Campaign campaign) {
		ContactList contactList = campaign.getContactList();
		EmailTemplate template = campaign.getEmailTemplate();
		emailService.sendBulkEmailToList(contactList, template.getSubjectLine(), template.getContent(), campaign);
		campaign.setSent(true);
		campaignRepo.save(campaign);
		return "Campaign Launched Successfully";
	}
	
	public CampaignAnalyticsDto getCampaignAnalytics() {
		List<Campaign> campaigns = campaignRepo.findAll();
		int totalCampaigns = campaigns.size();
		int activeCampaigns = (int) campaigns.stream().filter(c -> !c.isSent()).count();
		int totalSent = (int) campaigns.stream().filter(Campaign::isSent).count();
		List<EmailAnalytics> emails = emailAnalyticsRepository.findAll();
		Overview overview = buildOverview(emails);
		double responseRate = overview.getReplyRate();
		List<CampaignDto> campaignDtos = campaigns.stream().map(this::toDto).collect(Collectors.toList());
		return new CampaignAnalyticsDto(totalCampaigns, activeCampaigns, totalSent, responseRate,
				campaignDtos);
	}

	private Overview buildOverview(List<EmailAnalytics> emails) {
		long totalSent = emails.size();
		long totalBounced = emails.stream().filter(e -> e.getBouncedAt() != null).count();
		long totalDelivered = totalSent - totalBounced;
		long totalOpened = emails.stream().filter(e -> e.getOpenedAt() != null).count();
		long totalClicked = emails.stream().filter(e -> e.getClickedAt() != null).count();
		long totalReplied = emails.stream().filter(e -> e.getRepliedAt() != null).count();
		Overview o = new Overview();
		o.setTotalSent(totalSent);
		o.setTotalBounced(totalBounced);
		o.setTotalDelivered(totalDelivered);
		o.setTotalOpened(totalOpened);
		o.setTotalClicked(totalClicked);
		o.setTotalReplied(totalReplied);
		o.setOpenRate(calculateRate(totalOpened, totalDelivered));
		o.setClickRate(calculateRate(totalClicked, totalDelivered));
		o.setReplyRate(calculateRate(totalReplied, totalDelivered));
		o.setDeliveryRate(calculateRate(totalDelivered, totalSent));
		o.setBounceRate(calculateRate(totalBounced, totalSent));
		return o;
	}

	private double calculateRate(long numerator, long denominator) {
		if (denominator == 0)
			return 0.0;
		double rate = ((double) numerator * 100) / denominator;
		return Math.round(rate * 100.0) / 100.0;
	}

	private CampaignDto toDto(Campaign campaign) {
		return new CampaignDto(campaign.getId(), campaign.getName(),
				campaign.getType() != null ? campaign.getType().name() : null, campaign.getContentStrategy(),
				campaign.getScheduledAt(), campaign.isAiPersonalization(), campaign.getCreatedAt(), campaign.isSent(),
				campaign.getContactList() != null ? campaign.getContactList().getId() : null,
				campaign.getEmailTemplate() != null ? campaign.getEmailTemplate().getId() : null);
	}




}
