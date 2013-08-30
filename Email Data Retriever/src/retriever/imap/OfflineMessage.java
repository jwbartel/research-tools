package retriever.imap;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public class OfflineMessage extends MimeMessage {

	MimeMessage parent;
	private final Map<String, String[]> seenHeaders = new TreeMap<>();
	private String subject = null;
	private Date receivedDate;
	private Address[] from;
	private Address[] allRecipients;

	public OfflineMessage(MimeMessage parent) throws MessagingException {
		super(parent);
		this.parent = parent;
	}

	@Override
	public String[] getHeader(String header) throws MessagingException {
		if (!seenHeaders.containsKey(header)) {
			String[] parentHeader = parent.getHeader(header);
			if (parentHeader != null) {
				seenHeaders.put(header, parentHeader);
			}
			return parentHeader;
		} else {
			return seenHeaders.get(header);
		}
	}

	@Override
	public String getSubject() throws MessagingException {
		if (subject == null) {
			subject = parent.getSubject();
		}
		return subject;
	}

	@Override
	public Date getReceivedDate() throws MessagingException {
		if (receivedDate == null) {
			receivedDate = parent.getReceivedDate();
		}
		return receivedDate;
	}

	@Override
	public Address[] getFrom() throws MessagingException {
		if (from == null) {
			from = parent.getFrom();
		}
		return from;
	}

	@Override
	public Address[] getAllRecipients() throws MessagingException {
		if (allRecipients == null) {
			allRecipients = parent.getAllRecipients();
		}
		return allRecipients;
	}
}
