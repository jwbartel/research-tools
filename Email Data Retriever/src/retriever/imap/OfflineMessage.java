package retriever.imap;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public class OfflineMessage {

	private static final String[] headers = {"Message-ID", "References", "In-Reply-To"};
	
	MimeMessage parent;
	private final Map<String, String[]> seenHeaders = new TreeMap<String, String[]>();
	private String subject = null;
	private Date receivedDate;
	private Address[] from;
	private Address[] allRecipients;

	public OfflineMessage(MimeMessage parent) throws MessagingException {
		this.parent = parent;
		preloadData();
	}
	
	private void preloadData() throws MessagingException {
		subject = parent.getSubject();
		receivedDate = parent.getReceivedDate();
		from = parent.getFrom();
		allRecipients = parent.getAllRecipients();
		for (String header : headers) {
			seenHeaders.put(header, parent.getHeader(header));
		}
	}

	public String[] getHeader(String header) throws MessagingException {
		if (!seenHeaders.containsKey(header)) {
			throw new MessagingException("Header value "+header+" was not preloaded");
//			String[] parentHeader = parent.getHeader(header);
//			if (parentHeader != null) {
//				seenHeaders.put(header, parentHeader);
//			}
//			return parentHeader;
		} else {
			return seenHeaders.get(header);
		}
	}

	public String getSubject() throws MessagingException {
		if (subject == null) {
			subject = parent.getSubject();
		}
		return subject;
	}

	public Date getReceivedDate() throws MessagingException {
		if (receivedDate == null) {
			receivedDate = parent.getReceivedDate();
		}
		return receivedDate;
	}

	public Address[] getFrom() throws MessagingException {
		if (from == null) {
			from = parent.getFrom();
		}
		return from;
	}

	public Address[] getAllRecipients() throws MessagingException {
		if (allRecipients == null) {
			allRecipients = parent.getAllRecipients();
		}
		return allRecipients;
	}
}
