package com.xtremand.email.service;

import com.xtremand.contact.repository.ContactListRepository;
import com.xtremand.contact.service.ContactService;
import com.xtremand.domain.dto.ContactDto;
import com.xtremand.domain.dto.RecipientGroup;
import com.xtremand.domain.dto.SendEmailRequest;
import com.xtremand.domain.entity.Campaign;
import com.xtremand.domain.entity.Contact;
import com.xtremand.domain.entity.ContactList;
import com.xtremand.domain.entity.EmailAnalytics;
import com.xtremand.email.repository.ContactListEmailAnalyticsRepository;
import com.xtremand.email.repository.EmailAnalyticsRepository;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmailService {

	@Value("${email.tracking.url}")
	private String trackingUrl;

	@Value("${email.username}")
	private String fromEmail;

	private final ProducerTemplate producerTemplate;
	private final ContactService contactService;
	private final EmailAnalyticsService analyticsService;
	private final ContactListEmailAnalyticsRepository mappingRepo;
	private final EmailAnalyticsRepository repo;
	private final ContactListRepository contactListRepository;

	public EmailService(ProducerTemplate producerTemplate, ContactService contactService, EmailAnalyticsService analyticsService,
			ContactListEmailAnalyticsRepository mappingRepo, EmailAnalyticsRepository repo, ContactListRepository contactListRepository) {
		this.producerTemplate = producerTemplate;
		this.contactService = contactService;
		this.analyticsService = analyticsService;
		this.mappingRepo = mappingRepo;
		this.repo = repo;
		this.contactListRepository = contactListRepository;
	}

	private List<ContactDto> expandRecipients(RecipientGroup group) {
		if (group == null)
			return Collections.emptyList();
		List<ContactDto> contacts = new ArrayList<>();
		if (group.getContactIds() != null) {
			for (Long cid : group.getContactIds()) {
				ContactDto c = contactService.getContactById(cid);
				if (c != null)
					contacts.add(c);
			}
		}
		if (group.getEmails() != null) {
			for (String email : group.getEmails()) {
				ContactDto c = contactService.getContactByEmail(email);
				if (c == null) {
					Contact newContact = new Contact();
					newContact.setEmail(email);
					newContact.setActive(true);
					String localPart = email.split("@")[0];
					newContact.setFirstName(localPart);
					c = contactService.saveAndConvertToDto(newContact);
				}
				contacts.add(c);
			}
		}
		if (group.getContactListIdToContactIds() != null) {
			for (Map.Entry<Long, List<Long>> entry : group.getContactListIdToContactIds().entrySet()) {
				Long listId = entry.getKey();
				List<Long> contactIds = entry.getValue();
				ContactList contactList = contactListRepository.findById(listId)
						.orElseThrow(() -> new RuntimeException("Contact list not found: " + listId));
				if (contactIds == null || contactIds.isEmpty()) {
					contacts.addAll(contactList.getContacts().stream().map(contactService::mapToDto).toList());
				} else {
					List<ContactDto> filteredContacts = contactList.getContacts().stream()
							.filter(c -> contactIds.contains(c.getId())).map(contactService::mapToDto).toList();
					contacts.addAll(filteredContacts);
				}
			}
		}

		return contacts;
	}

	public void sendBulkEmail(SendEmailRequest request, Campaign campaign) {
		List<ContactDto> toContacts = expandRecipients(request.getTo());
		List<ContactDto> ccContacts = expandRecipients(request.getCc());
		List<ContactDto> bccContacts = expandRecipients(request.getBcc());
		Set<String> processedEmails = new HashSet<>();
		List<ContactDto> allRecipients = new ArrayList<>();
		allRecipients.addAll(toContacts);
		allRecipients.addAll(ccContacts);
		allRecipients.addAll(bccContacts);

		List<ContactDto> distinctRecipients = allRecipients.stream()
				.filter(c -> c.getEmail() != null && !c.getEmail().isEmpty())
				.filter(c -> processedEmails.add(c.getEmail())).collect(Collectors.toList());
		for (ContactDto contact : distinctRecipients) {
			UUID trackingId = null;
			try {
				EmailAnalytics emailAnalytics = analyticsService.logEmailSent(contact.getId(), contact.getEmail(),
						request.getSubject(), request.getBody(), trackingId, campaign, request.getPrompt());
				saveAnalyticsMappingsForContact(contact, emailAnalytics, request);
			} catch (Exception e) {
				analyticsService.logEmailNotSent(contact.getId(), contact.getEmail(), request.getSubject(),
						request.getBody(), trackingId, campaign, request.getPrompt());
			}
		}
		String toEmails = toContacts.stream().map(ContactDto::getEmail).distinct().collect(Collectors.joining(","));
		String ccEmails = ccContacts.stream().map(ContactDto::getEmail).distinct().collect(Collectors.joining(","));
		String bccEmails = bccContacts.stream().map(ContactDto::getEmail).distinct().collect(Collectors.joining(","));
		String genericBody = personalizeBodyGeneric(request.getBody());
		sendEmail(request.getSubject(), genericBody, toEmails, ccEmails, bccEmails);
	}

	private void saveAnalyticsMappingsForContact(ContactDto contact, EmailAnalytics emailAnalytics,
			SendEmailRequest request) {
		saveMappingsFromRecipientGroup(contact, emailAnalytics, request.getTo());
		saveMappingsFromRecipientGroup(contact, emailAnalytics, request.getCc());
		saveMappingsFromRecipientGroup(contact, emailAnalytics, request.getBcc());
	}

	private void saveMappingsFromRecipientGroup(ContactDto contact, EmailAnalytics emailAnalytics, RecipientGroup group) {
		if (group == null || group.getContactListIdToContactIds() == null)
			return;
		for (Map.Entry<Long, List<Long>> entry : group.getContactListIdToContactIds().entrySet()) {
			Long contactListId = entry.getKey();
			List<Long> contactIds = entry.getValue();
			if (contactIds != null && contactIds.contains(contact.getId())) {
				analyticsService.saveContactAnalyticsMapping(contactListId, emailAnalytics);
			}
		}
	}

	private String personalizeBodyGeneric(String body) {
		String personalized = body.replace("[Recipient's Name]", "Valued Customer")
				.replace("{{email}}", "").replace("[Name]", "Valued Customer").replace("[firstName]", "")
				.replace("[lastName]", "").replace("[Email]", "").replace("[company]", "").replace("[phoneNumber]", "")
				.replace("[jobtitle]", "").replace("[Location]", "").replace("[SenderEmail]", fromEmail);

		String htmlBody = convertPlainTextToHtml(personalized);
		String genericTrackingId = "generic-tracking-id";
		String trackedPixel = "<img src=\"https://yourserver.com/track/open/" + genericTrackingId
				+ "\" width=\"1\" height=\"1\" />";
		String trackedLink = trackingUrl.replace("{trackingId}", genericTrackingId);
		String trackingHtml = "<br><a href=\"" + trackedLink + "\">Click Here</a>" + trackedPixel;
		return htmlBody.replace("</body></html>", trackingHtml + "</body></html>");
	}

	private void sendEmail(String subject, String body, String to, String cc, String bcc) {
		Map<String, Object> headers = new HashMap<>();
		headers.put("To", to);
		if (cc != null && !cc.isEmpty())
			headers.put("Cc", cc);
		if (bcc != null && !bcc.isEmpty())
			headers.put("Bcc", bcc);
		headers.put("From", fromEmail);
		headers.put("Subject", subject);
		headers.put("Content-Type", "text/html; charset=UTF-8");
		producerTemplate.sendBodyAndHeaders("direct:sendEmail", body, headers);
	}

	private String convertPlainTextToHtml(String plainText) {
		if (plainText == null || plainText.isEmpty()) {
			return "";
		}
		String escapedText = plainText.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\"", "&quot;").replace("'", "&#39;");
		String htmlContent = escapedText.replaceAll("(\\r\\n|\\r|\\n){2,}", "</p><p>")
				.replaceAll("(\\r\\n|\\r|\\n)", "<br/>");
		htmlContent = "<p>" + htmlContent + "</p>";
		return "<html><body style=\"font-family: Arial, sans-serif; color: #333;\">" + htmlContent + "</body></html>";
	}

	public void sendBulkEmailToList(ContactList contactList, String subject, String body, Campaign campaign) {
		SendEmailRequest req = new SendEmailRequest();
		req.setSubject(subject);
		req.setBody(body);
		req.setContactIds(contactList.getContacts().stream().map(Contact::getId).toList());
		sendBulkEmail(req, campaign);
	}

}
