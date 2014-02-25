package retriever;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.mail.Address;
import javax.mail.MessagingException;

public class OfflineThread {

	private final Set<OfflineMessage> messages;
	
	private Date[] firstAndResponseDates = null;
	
	private OfflineMessage[] originalAndResponseMessages = null;
	private boolean searchedForOriginalAndResponse = false;
	
	private Long responseTime = null;
	private boolean searchedForResponseTime = false;
	
	private Set<Address> addresses = null;
	
	private String subject = null;
	private boolean searchedForSubject = false;
	
	public OfflineThread(Set<OfflineMessage> messages) {
		this.messages = messages;
	}
	
	public Set<OfflineMessage> getMessages() {
		return messages;
	}
	
	public Date[] getFirstAndResponseDates()
			throws MessagingException {
		if (messages.size() < 2) {
			return null;
		}
		
		if (firstAndResponseDates == null) {

			OfflineMessage firstMessage = null;
			OfflineMessage response = null;
			for (OfflineMessage message : messages) {
				Date msgDate = message.getReceivedDate();
				if (msgDate != null) {
					if (firstMessage == null || firstMessage.getReceivedDate().after(msgDate)) {
						firstMessage = message;
					} else if (response == null || response.getReceivedDate().after(msgDate)) {
						response = message;

					}
				}
			}

			firstAndResponseDates = new Date[2];
			if (firstMessage != null) {
				firstAndResponseDates[0] = firstMessage.getReceivedDate();
			}
			if (response != null) {
				firstAndResponseDates[1] = response.getReceivedDate();
			}
		}

		return firstAndResponseDates;

	}
	
	public String getSubject() {
		if (!searchedForSubject) {
			try {
				
				subject = messages.iterator().next().getBaseSubject();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			searchedForSubject = true;
		}
		return subject;
	}
	
	public OfflineMessage[] getOriginalAndResponse()
			throws MessagingException {
		if (messages.size() < 2) {
			return null;
		}

		if (!searchedForOriginalAndResponse) {
			OfflineMessage firstMessage = null;
			OfflineMessage response = null;
			for (OfflineMessage message : messages) {
				Date msgDate = message.getReceivedDate();
				if (msgDate != null) {
					if (firstMessage == null || firstMessage.getReceivedDate().after(msgDate)) {
						firstMessage = message;
					} else if (response == null || response.getReceivedDate().after(msgDate)) {
						response = message;

					}
				}
			}

			if (firstMessage != null && response != null) {
				originalAndResponseMessages = new OfflineMessage[2];
				originalAndResponseMessages[0] = firstMessage;
				originalAndResponseMessages[1] = response;
			} 
			searchedForOriginalAndResponse = true;
		}
		return originalAndResponseMessages;
	}
	
	public Long getResponseTime() throws MessagingException {
		if (!searchedForResponseTime) {
			if (messages.size() < 2) {
				return null;
			}

			Date[] orignalAndResponseTime = getFirstAndResponseDates();

			if (orignalAndResponseTime[0] != null && orignalAndResponseTime[1] != null) {
				responseTime = orignalAndResponseTime[1].getTime() - orignalAndResponseTime[0].getTime();
			}
			searchedForResponseTime = true;
		}
		return responseTime;
	}
	
	public Set<Address> getAddresses() throws MessagingException {
		if (addresses == null) {
			Address address = null;
			addresses = new HashSet<Address>();
			for (OfflineMessage message : messages) {
				if (message.getAllRecipients() != null) {
					addresses.addAll(Arrays.asList(message.getAllRecipients()));
				}
			}
		}
		return addresses;
	}
	
}
