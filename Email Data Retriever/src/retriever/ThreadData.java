package retriever;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.mail.Address;
import javax.mail.MessagingException;

public class ThreadData {

	int totalMessages;
	ArrayList<Set<OfflineMessage>> threads;
	Set<String> seenMessages;
	Set<String> unseenMessages;

	Map<String, Integer> addressIDs;
	
	//Cluster for threads that do not fit any others
	final static ThreadCluster MISFITS_CLUSTER = new ThreadCluster() {

		@Override
		public boolean matchesCluster(OfflineThread thread) {
			return true;
		}

		@Override
		public int compareTo(ThreadCluster o) {
			if (o != this) {
				return 1;
			} else {
				return 0;
			}
		}
	};

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

	public String getAddressMappings() throws MessagingException {
		if (addressIDs == null) {
			buildAddressIDs();
		}

		String mappingsStr = "";
		if (addressIDs != null) {
			for (Entry<String, Integer> entry : addressIDs.entrySet()) {
				String address = entry.getKey();
				Integer id = entry.getValue();
				mappingsStr += id + ":" + address + "\n";
			}
		}
		return mappingsStr;
	}

	private void addThread(ThreadCluster cluster, OfflineThread thread,
			Map<ThreadCluster, ArrayList<OfflineThread>> sortedThreads) {
		ArrayList<OfflineThread> thresholdThreads = sortedThreads.get(cluster);
		if (thresholdThreads == null) {
			thresholdThreads = new ArrayList<OfflineThread>();
			sortedThreads.put(cluster, thresholdThreads);
		}

		thresholdThreads.add(thread);
	}

	private Map<ThreadCluster, ArrayList<OfflineThread>> getSortedThreads(
			Collection<ThreadCluster> clusters) throws MessagingException {

		TreeSet<ThreadCluster> sortedClusters = new TreeSet<ThreadCluster>(clusters);
		
		
		
		Map<ThreadCluster, ArrayList<OfflineThread>> sortedThreads =
				new TreeMap<ThreadCluster, ArrayList<OfflineThread>>();
		for (Set<OfflineMessage> messages : threads) {

			OfflineThread thread = new OfflineThread(messages);
			if (thread.getResponseTime() == null) {
				continue;
			}

			boolean sorted = false;
			for (ThreadCluster cluster : sortedClusters) {
				if (cluster.matchesCluster(thread)) {
					addThread(cluster, thread, sortedThreads);
					sorted = true;
					break;
				}
			}

			if (!sorted) {
				addThread(MISFITS_CLUSTER, thread, sortedThreads);
			}
		}

		return sortedThreads;
	}

	private String getAllThreadStrings(Collection<ThreadCluster> cluster) throws MessagingException {
		Map<ThreadCluster, ArrayList<OfflineThread>> sortedThreads = getSortedThreads(cluster);

		String retVal = "";
		for (ArrayList<OfflineThread> threads : sortedThreads.values()) {
			for (OfflineThread thread : threads) {
				retVal += getSurveyThreadString(thread) + "\n";
			}
		}

		return retVal;
	}

	private Map<ThreadCluster, OfflineThread> getSelectedThreadsForSurvey(
			Collection<ThreadCluster> clusters) throws MessagingException {

		Map<ThreadCluster, OfflineThread> surveyThreads = new TreeMap<ThreadCluster, OfflineThread>();

		Random rand = new Random();
		Map<ThreadCluster, ArrayList<OfflineThread>> sortedThreads = getSortedThreads(clusters);
		for (Entry<ThreadCluster, ArrayList<OfflineThread>> entry : sortedThreads.entrySet()) {
			ThreadCluster cluster = entry.getKey();
			ArrayList<OfflineThread> threads = entry.getValue();

			int pos = rand.nextInt(threads.size());
			surveyThreads.put(cluster, threads.get(pos));
		}
		return surveyThreads;
	}

	private final static long SECOND = 1000;
	private final static long MINUTE = SECOND * 60;
	private final static long HOUR = MINUTE * 60;
	private final static long DAY = HOUR * 24;
	private final static long WEEK = DAY * 7;
	private final static long YEAR = DAY * 365;

	private static String getTimeString(long time) {

		String timeStr = "";

		long year = time / YEAR;
		if (year > 0) {
			timeStr += year + " years, ";
			time = time - (year * YEAR);
		}

		long week = time / WEEK;
		;
		if (week > 0) {
			timeStr += week + " weeks, ";
			time = time - (week * WEEK);
		}

		long day = time / DAY;
		if (day > 0) {
			timeStr += day + " days, ";
			time = time - (day * DAY);
		}

		long hour = time / HOUR;
		if (hour > 0) {
			timeStr += hour + " hours, ";
			time = time - (hour * HOUR);
		}

		long minute = time / MINUTE;
		if (minute > 0) {
			timeStr += minute + " minutes, ";
			time = time - (minute * MINUTE);
		}

		long second = time / SECOND;
		timeStr += second + " seconds";
		return timeStr;
	}

	private String getSurveyThreadString(OfflineThread thread) throws MessagingException {
		Long responseTime = thread.getResponseTime();
		Date[] originalAndResponseDate = thread.getFirstAndResponseDates();

		String timeString = getTimeString(responseTime);
		OfflineMessage[] originalAndResponse = thread.getOriginalAndResponse();
		String from = null;
		if (originalAndResponse[0].getFrom() != null && originalAndResponse[0].getFrom().length > 0) {
			from = getPublicAddressRepresentation(originalAndResponse[0].getFrom()[0], true);
		}
		Collection<String> recipients = new ArrayList<String>();
		if (originalAndResponse[0].getAllRecipients() != null) {
			for (Address recipient : originalAndResponse[0].getAllRecipients()) {
				recipients.add(getPublicAddressRepresentation(recipient, true));
			}
		}
		String subject = originalAndResponse[0].getSubject();
		String responder = null;
		if (originalAndResponse[1].getFrom() != null && originalAndResponse[1].getFrom().length > 0) {
			responder = getPublicAddressRepresentation(originalAndResponse[1].getFrom()[0], true);
		}

		String threadString = "Subject:" + subject + "\n";
		threadString += "From:" + from + "\n";
		threadString += "Original Date:" + originalAndResponseDate[0] + "\n";
		threadString += "Recipients:" + recipients + "\n";
		threadString += "Time to Response:" + timeString + "\n";
		threadString += "Response Date:" + originalAndResponseDate[1] + "\n";
		threadString += "Responder:" + responder;

		return threadString;
	}

	private String getAllSurveyThreadStrings(Collection<ThreadCluster> clusters)
			throws MessagingException {

		String surveyString = "";

		Map<ThreadCluster, OfflineThread> surveyThreads = getSelectedThreadsForSurvey(clusters);
		for (ThreadCluster cluster : clusters) {
			OfflineThread thread = surveyThreads.get(cluster);
			if (thread != null) {
				surveyString += getSurveyThreadString(thread) + "\n";
			}
		}

		OfflineThread thread = surveyThreads.get(MISFITS_CLUSTER);
		if (thread != null) {
			surveyString += getSurveyThreadString(thread) + "\n";
		}
		return surveyString;
	}

	public Map<String, String> getCompartmentalizedData(String sourceEmail,
			boolean includeSubjects, boolean includeFullEmailAddresses, boolean includeAttachments,
			boolean includeAttachedFileNames, Collection<ThreadCluster> clusters)
			throws MessagingException {

		Map<String, String> compartments = new HashMap<String, String>();
		if (addressIDs == null) {
			buildAddressIDs();
		}

		String summaryStr = "";
		String messagesStr = "";
		String subjectsStr = "";
		String attachmentsStr = "";
		String addressesStr = (includeFullEmailAddresses) ? getAddressMappings() : "";

		int totalMessagesInThreads = 0;
		int threadID = 1;
		int messageID = 1;
		for (Set<OfflineMessage> thread : threads) {
			totalMessagesInThreads += thread.size();

			for (OfflineMessage message : thread) {

				String messagePrefix = "Message:" + messageID;

				String messageVector = messagePrefix;
				messageVector += " Thread:" + threadID;

				// Get from addresses
				String fromStr = "";
				Address[] from = message.getFrom();
				if (from != null)
					for (Address addressObj : from) {
						if (fromStr.length() > 1) {
							fromStr += ",";
						}
						fromStr += getPublicAddressRepresentation(addressObj, false);
					}
				messageVector += " From:[" + fromStr + "]";

				// Get recipient addresses
				String recipientStr = "";
				Address[] recipients = message.getAllRecipients();
				if (recipients != null)
					for (Address addressObj : recipients) {
						if (recipientStr.length() > 0) {
							recipientStr += ",";
						}
						recipientStr += getPublicAddressRepresentation(addressObj, false);
					}
				messageVector += " Recipients:[" + recipientStr + "]";

				// Get received date
				messageVector += " Received-Date:" + message.getReceivedDate();
				messagesStr += messageVector + "\n";

				// Get subjects
				if (includeSubjects) {
					subjectsStr += messagePrefix + " Subject:" + message.getSubject() + "\n";
				}

				// Get Attachments
				if (includeAttachments) {
					attachmentsStr += "Message: " + messageID + " Num_Attachments:"
							+ message.getAttachedFiles().size();
					if (includeAttachedFileNames) {
						attachmentsStr += " AttachedFiles:" + message.getAttachedFiles();
					}
					attachmentsStr += "\n";
				}

				messageID++;
			}

			threadID++;
		}

		summaryStr += "Source email:" + sourceEmail + "\n";
		summaryStr += "Total messages in account:" + totalMessages + "\n";
		summaryStr += "Total threads retrieved:" + threads.size() + "\n";
		summaryStr += "Total messages checked:" + seenMessages.size() + "\n";
		summaryStr += "Total messages in retrieved threads:" + totalMessagesInThreads + "\n";
		summaryStr += "Total unseen messages for retrieved threads:" + unseenMessages.size() + "\n";
		summaryStr += "Includes subjects:" + includeSubjects + "\n";

		compartments.put("summary", summaryStr);
		if (includeFullEmailAddresses) {
			compartments.put("addresses", addressesStr);
		}
		compartments.put("messages", messagesStr);
		if (includeSubjects) {
			compartments.put("subjects", subjectsStr);
		}
		if (includeAttachments) {
			compartments.put("attachments", attachmentsStr);
		}
		if (includeSubjects && includeFullEmailAddresses) {
			compartments.put("threads with responses", getAllThreadStrings(clusters));
			compartments.put("survey", getAllSurveyThreadStrings(clusters));
		}
		return compartments;
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
		if (addressObj == null) {
			return null;
		}
		String address = addressObj.toString();
		if (includeFullEmailAddresses) {
			return address;
		} else {
			return "" + getCodedAddressValue(addressObj);
		}
	}
}
