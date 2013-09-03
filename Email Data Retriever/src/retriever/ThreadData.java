package retriever;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.mail.Address;
import javax.mail.MessagingException;

public class ThreadData {

	int totalMessages;
	ArrayList<Set<OfflineMessage>> threads;
	Set<String> seenMessages;
	Set<String> unseenMessages;

	Map<String, Integer> addressIDs;

	public ThreadData(int totalMessages, ArrayList<Set<OfflineMessage>> threads,
			Set<String> seenMessages, Set<String> unseenMessages) {
		this.totalMessages = totalMessages;
		this.threads = threads;
		this.seenMessages = seenMessages;
		this.unseenMessages = unseenMessages;
	}

	private void buildAddressIDs() throws MessagingException {

		addressIDs = new HashMap<String, Integer>();

		for (Set<OfflineMessage> thread : threads) {

			for (OfflineMessage message : thread) {
				message.getSubject();
				message.getReceivedDate();
				Address[] from = message.getFrom();
				if (from != null) {
					for (Address address : from) {
						assignAddressID(address);
					}
				}
				Address[] recipients = message.getAllRecipients();
				if (recipients != null) {
					for (Address address : recipients) {
						assignAddressID(address);
					}
				}
			}
		}
	}

	private void assignAddressID(Address address) {
		String cleanedAddress = address.toString().toLowerCase();
		if (cleanedAddress.contains("<") && cleanedAddress.contains(">")) {
			cleanedAddress = cleanedAddress.substring(cleanedAddress.lastIndexOf('<') + 1,
					cleanedAddress.lastIndexOf('>'));
		}
		if (!addressIDs.containsKey(cleanedAddress)) {
			addressIDs.put(cleanedAddress, addressIDs.size() + 1);
		}
	}

	public String getDataString(String sourceEmail, boolean includeSubjects,
			boolean includeFullEmailAddresses, boolean includeAttachments,
			boolean includeAttachedFileNames) throws MessagingException {

		if (addressIDs == null) {
			buildAddressIDs();
		}

		int totalMessagesInThreads = 0;
		String messageVectors = "";
		int threadID = 1;
		int messageID = 1;
		for (Set<OfflineMessage> thread : threads) {
			totalMessagesInThreads += thread.size();

			for (OfflineMessage message : thread) {

				String vector = "Message:" + messageID;
				vector += " Thread:" + threadID;
				vector += " Received-Date:" + message.getReceivedDate();
				String fromStr = "";
				Address[] from = message.getFrom();
				if (from != null)
					for (Address addressObj : from) {
						if (fromStr.length() > 1) {
							fromStr += ",";
						}
						fromStr += getPublicAddressRepresentation(addressObj,
								includeFullEmailAddresses);
					}
				vector += " From:[" + fromStr + "]";

				if (includeAttachments) {
					vector += " Num_Attachments:" + message.getAttachedFiles().size();
					if (includeAttachedFileNames) {
						vector += " AttachedFiles:" + message.getAttachedFiles();
					}
				}

				message.getFrom();
				String recipientStr = "";
				Address[] recipients = message.getAllRecipients();
				if (recipients != null)
					for (Address addressObj : recipients) {
						if (recipientStr.length() > 1) {
							recipientStr += ",";
						}
						recipientStr += getPublicAddressRepresentation(addressObj,
								includeFullEmailAddresses);
					}
				vector += " Recipients:[" + recipientStr + "]";

				if (includeSubjects) {
					vector += " Subject:" + message.getSubject();
				}

				messageVectors += vector + "\n";
				messageID++;
			}

			threadID++;
		}

		String toShare = "Source email:" + sourceEmail + "\n";
		toShare += "Total messages in account:" + totalMessages + "\n";
		toShare += "Total threads retrieved:" + threads.size() + "\n";
		toShare += "Total messages checked:" + seenMessages.size() + "\n";
		toShare += "Total messages in retrieved threads:" + totalMessagesInThreads + "\n";
		toShare += "Total unseen messages for retrieved threads:" + unseenMessages.size() + "\n";
		toShare += "Includes subjects:" + includeSubjects + ", Includes email addresses:"
				+ includeFullEmailAddresses + "\n";

		toShare += "\n";
		toShare += messageVectors;

		return toShare;

	}

	protected static String getCleanedAddress(Address addressObj) {
		String address = addressObj.toString();
		String cleanedAddress = address.toLowerCase();
		if (cleanedAddress.contains("<") && cleanedAddress.contains(">")) {
			cleanedAddress = cleanedAddress.substring(cleanedAddress.lastIndexOf('<') + 1,
					cleanedAddress.lastIndexOf('>'));
		}
		return cleanedAddress;
	}

	private Integer getCodedAddressValue(Address addressObj) {
		String cleanedAddress = getCleanedAddress(addressObj);
		return addressIDs.get(cleanedAddress);
	}

	private String getPublicAddressRepresentation(Address addressObj,
			boolean includeFullEmailAddresses) {
		String address = addressObj.toString();
		if (includeFullEmailAddresses) {
			return address;
		} else {
			return "" + getCodedAddressValue(addressObj);
		}
	}
}
