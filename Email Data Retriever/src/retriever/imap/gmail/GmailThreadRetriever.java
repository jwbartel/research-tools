package retriever.imap.gmail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import retriever.MessageListener;
import retriever.OfflineMessage;
import retriever.ThreadData;
import retriever.ThreadRetriever;

public class GmailThreadRetriever extends ThreadRetriever {

	public GmailThreadRetriever(Store store, MessageListener listener) {
		this.store = store;
		super.addMessageListener(listener);
	}

	@Override
	public ThreadData retrieveThreads(int numMessages, int numThreads) throws MessagingException {
		Folder promotionsFolder = store.getFolder("Promotions");
		if (promotionsFolder.exists()) {
			promotionsFolder.open(Folder.READ_ONLY);
			return retrieveGmailThreadsWithPromotions(promotionsFolder, numMessages, numThreads);
		}

		Folder folder = store.getFolder("[Gmail]/All Mail");
		folder.open(Folder.READ_ONLY);
		return super.retrieveThreads(folder, numMessages, numThreads);
	}

	public ThreadData retrieveGmailThreadsWithPromotions(Folder promotionsFolder, int numMessages,
			int numThreads) throws MessagingException {
		Folder allMailFolder = store.getFolder("[Gmail]/All Mail");
		allMailFolder.open(Folder.READ_ONLY);

		int totalAllMailMessages = allMailFolder.getMessageCount();
		int totalPromotionsMessages = promotionsFolder.getMessageCount();

		int maxAllMailMessages = totalAllMailMessages;
		int maxPromotionsMessages = totalPromotionsMessages;

		int minAllMailMessage = Math.max(0, maxAllMailMessages - BUFFER_SIZE + 1);
		int minPromotionsMessage = Math.max(0, maxPromotionsMessages - BUFFER_SIZE + 1);

		Message[] allMailMessages = allMailFolder
				.getMessages(minAllMailMessage, maxAllMailMessages);
		Message[] promotionsMessages = promotionsFolder.getMessages(minPromotionsMessage,
				maxPromotionsMessages);

		ArrayList<ArrayList<String>> idsForThreads = new ArrayList<ArrayList<String>>();
		ArrayList<Set<OfflineMessage>> threads = new ArrayList<Set<OfflineMessage>>();
		Set<String> seenMessages = new TreeSet<String>();
		Set<String> unseenMessages = new TreeSet<String>();

		logMessage("Retrieving data from the latest " + numThreads
				+ " threads using at most the latest " + numMessages + " messages.\n");

		int promotionsPos = promotionsMessages.length - 1;
		String topPromotionsId = null;
		if (promotionsMessages.length > 0) {
			topPromotionsId = promotionsMessages[promotionsPos].getHeader("Message-ID")[0];
		}
		try {
			while (true) {
				int startMessage = Math.min(-1 * (maxAllMailMessages - totalAllMailMessages) + 1,
						numMessages);
				// int endMessage = Math.min(-1*(minMessage - totalMessages)+1,
				// MAX_MESSAGES);
				//
				// logMessage("Retrieving messages "+(startMessage)+" to "+(endMessage)
				// + " (newest first)");
				updateRetrievedMessageCounts(Math.max(0, startMessage - 1), threads.size(),
						unseenMessages.size());

				for (int msgPos = allMailMessages.length - 1; msgPos >= 0; msgPos--) {

					String messageID = allMailMessages[msgPos].getHeader("Message-ID")[0];
					if (seenMessages.contains(messageID)) {
						continue;
					} else {
						seenMessages.add(messageID);
					}

					if (topPromotionsId != null && topPromotionsId.equals(messageID)) {
						promotionsPos--;
						if (promotionsPos == -1 && promotionsMessages.length > 0) {
							maxPromotionsMessages = minPromotionsMessage - 1;
							minPromotionsMessage = Math.max(1, maxPromotionsMessages - BUFFER_SIZE
									+ 1);
							if (maxPromotionsMessages < 1) {
								promotionsMessages = new Message[0];
							} else {
								promotionsMessages = promotionsFolder.getMessages(
										minPromotionsMessage, maxPromotionsMessages);
							}
							promotionsPos = promotionsMessages.length - 1;
						}
						if (promotionsMessages.length > 0) {
							topPromotionsId = promotionsMessages[promotionsPos]
									.getHeader("Message-ID")[0];
						} else {
							topPromotionsId = null;
						}
					} else if (threads.size() != numThreads || unseenMessages.contains(messageID)) {

						String[] prefetchedHeaders = { "Message-ID", "References", "In-Reply-To",
								"References" };
						OfflineMessage message = new OfflineMessage(
								(MimeMessage) allMailMessages[msgPos], prefetchedHeaders);

						ArrayList<String> references = message.getReferences();
						String inReplyTo = message.getInReplyTo();
						sortIntoThreads(message, messageID, numThreads, references, inReplyTo,
								idsForThreads, threads, unseenMessages, seenMessages);

					}

					if ((threads.size() >= numThreads && unseenMessages.size() == 0)
							|| seenMessages.size() >= numMessages) {
						break;
					}

					updateRetrievedMessageCounts(startMessage + (allMailMessages.length - msgPos),
							threads.size(), unseenMessages.size());
				}

				if (minAllMailMessage == 0) {
					break;
				}
				if ((threads.size() >= numThreads && unseenMessages.size() == 0)
						|| seenMessages.size() >= numMessages) {
					break;
				}

				maxAllMailMessages = minAllMailMessage - 1;
				minAllMailMessage = Math.max(0, maxAllMailMessages - BUFFER_SIZE + 1);
				allMailMessages = allMailFolder.getMessages(minAllMailMessage, maxAllMailMessages);

			}
		} catch (MessagingException e) {
			e.printStackTrace();
			logMessage("ERROR: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			logMessage("ERROR: " + e.getMessage());
		}

		updateRetrievedMessageCounts(numMessages, threads.size(), unseenMessages.size());
		return new ThreadData(totalAllMailMessages, threads, seenMessages, unseenMessages);
	}
}
