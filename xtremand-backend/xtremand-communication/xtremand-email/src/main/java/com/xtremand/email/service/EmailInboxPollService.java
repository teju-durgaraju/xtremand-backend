package com.xtremand.email.service;

import java.util.Properties;
import java.util.Arrays;
import java.util.Comparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.mail.*;
import jakarta.mail.Flags.Flag;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.FlagTerm;
import java.time.ZoneId;	
import com.xtremand.domain.entity.EmailAnalytics;
import com.xtremand.email.repository.EmailAnalyticsRepository;

@Service
public class EmailInboxPollService {

	@Autowired
	private EmailAnalyticsRepository repo;

	/**
	 * Poll inbox using either app password or OAuth2 token.
	 *
	 * @param emailUser       Email address
	 * @param passwordOrToken Either Gmail App Password or OAuth2 access token
	 * @param useOAuth        true if OAuth2 token, false if app password
	 * @throws Exception
	 */
	public void pollAndStoreInbox(String emailUser, String passwordOrToken, boolean useOAuth) throws Exception {
		Properties props = new Properties();
		props.put("mail.store.protocol", "imaps");
		props.put("mail.imaps.host", "imap.gmail.com");
		props.put("mail.imaps.port", "993");
		props.put("mail.imaps.ssl.enable", "true");
		props.put("mail.imaps.starttls.enable", "true");
		props.put("mail.imaps.ssl.trust", "*");
		if (useOAuth) {
			props.put("mail.imaps.auth.mechanisms", "XOAUTH2");
		}
		Session session = Session.getInstance(props);
		Store store = session.getStore("imaps");
		store.connect("imap.gmail.com", emailUser, passwordOrToken);
		Folder inbox = store.getFolder("INBOX");
		inbox.open(Folder.READ_ONLY);
		// Search only UNSEEN (unread) messages
		FlagTerm unseenFlagTerm = new FlagTerm(new Flags(Flag.SEEN), false);
		Message[] unreadMessages = inbox.search(unseenFlagTerm);
		// Sort by sent date descending (latest first)
		Arrays.sort(unreadMessages, Comparator.comparing((Message m) -> {
			try {
				return m.getSentDate();
			} catch (MessagingException e) {
				return null;
			}
		}).reversed());

		// Process all unread messages sorted by latest sent date
		for (Message m : unreadMessages) {
			Address[] fromAddresses = m.getFrom();
			String from = "";
			if (fromAddresses != null && fromAddresses.length > 0) {
				if (fromAddresses[0] instanceof InternetAddress internetAddress) {
					from = internetAddress.getAddress();
				} else {
					from = fromAddresses[0].toString();
				}
			}
			String to = InternetAddress.toString(m.getRecipients(Message.RecipientType.TO));
			String subject = m.getSubject();
			String bodyText = extractBody(m);
			String msgId = ((MimeMessage) m).getMessageID();
			String[] inReplyToHeaders = m.getHeader("In-Reply-To");
			String inReplyTo = (inReplyToHeaders != null && inReplyToHeaders.length > 0) ? inReplyToHeaders[0] : null;

			// Skip if this incoming message already saved
			if (repo.findByMessageId(msgId).isPresent())
				continue;

			// Only save if this email is a reply to a messageId that exists in sent emails
			if (inReplyTo == null || repo.findByMessageId(inReplyTo).isEmpty()) {
				// No matching sent message found for this reply, skip saving
				continue;
			}

			EmailAnalytics ea = new EmailAnalytics();
			ea.setFromEmail(from);
			ea.setSubject(subject);
			ea.setBody(bodyText);
			ea.setMessageId(msgId);
			ea.setInReplyTo(inReplyTo);
			ea.setStarred(false);
			ea.setDeleted(false);
			ea.setIncoming(true);
			ea.setSentAt(m.getSentDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
			repo.save(ea);
		}

		inbox.close(false);
		store.close();
	}

	private String extractBody(Message message) throws Exception {
		Object content = message.getContent();
		if (content instanceof String string) {
			return string;
		} else if (content instanceof Multipart multipart) {
			for (int i = 0; i < multipart.getCount(); i++) {
				BodyPart part = multipart.getBodyPart(i);
				String disposition = part.getDisposition();
				if (disposition == null || disposition.equalsIgnoreCase(Part.INLINE)) {
					Object partContent = part.getContent();
					if (partContent instanceof String stringPart) {
						return stringPart;
					}
				}
			}
			BodyPart part = multipart.getBodyPart(0);
			Object partContent = part.getContent();
			if (partContent instanceof String stringPart) {
				return stringPart;
			}
		}
		return "";
	}
}
