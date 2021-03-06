package retriever;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import retriever.imap.IgnoredMessageChecker;
import retriever.imap.MasterIgnoredMessageChecker;
import retriever.view.commandline.CommandLineEmailDataRetriever;

public abstract class ThreadRetriever {

	public final static int DEFAULT_MAX_MESSAGES = 2000;
	public final static int DEFAULT_NUM_THREADS_RETRIEVED = 400;
	public final static int BUFFER_SIZE = 100;

	protected Long timeout = null;

	protected static final IgnoredMessageChecker messageChecker = new MasterIgnoredMessageChecker();

	ArrayList<MessageListener> messageListeners = new ArrayList<MessageListener>();
	protected Store store;

	public void updateRetrievedMessageCounts(int latestRetrieved, int seenThreads,
			int missingMessages) {
		for (MessageListener listener : messageListeners) {
			listener.updateRetrievedMessageCounts(latestRetrieved, seenThreads, missingMessages);
		}
	}

	public void logMessage(String message) {
		for (MessageListener listener : messageListeners) {
			listener.logMessage(message);
		}
	}

	public void addMessageListener(MessageListener l) {
		if (!messageListeners.contains(l)) {
			messageListeners.add(l);
		}
	}

	private ArrayList<String> getStringAddresses(Set<OfflineMessage> thread)
			throws MessagingException {
		Set<String> addressSet = new TreeSet<String>();
		for (OfflineMessage msg : thread) {
			if (msg.getAllRecipients() != null) {
				for (Address recipient : msg.getAllRecipients()) {
					addressSet.add(ThreadData.getCleanedAddress(recipient));
				}
			}
			if (msg.getFrom() != null && msg.getFrom().length > 0) {
				addressSet.add(ThreadData.getCleanedAddress(msg.getFrom()[0]));
			}
		}
		return new ArrayList<String>(addressSet);
	}

	protected int getIntersectionSize(ArrayList<String> group1, ArrayList<String> group2) {
		ArrayList<String> intersection = new ArrayList<String>(group1);
		intersection.retainAll(group2);
		return intersection.size();
	}

	private void pruneThreads(ArrayList<ArrayList<String>> idsForThreads,
			ArrayList<Set<OfflineMessage>> threads) {
		if (threads.size() > 1) {
			for (int i = 0; i < threads.size(); i++) {
				Set<OfflineMessage> thread = threads.get(i);
				ArrayList<String> idsForThread = idsForThreads.get(i);
				try {
					String baseSubject = thread.iterator().next().getBaseSubject();
					if (baseSubject == null || baseSubject.length() == 0) {
						continue;
					}
					ArrayList<String> addresses = null;
					try {
						addresses = getStringAddresses(thread);
					} catch (NullPointerException e) {
						addresses = getStringAddresses(thread);
					}

					for (int j = i + 1; j < threads.size(); j++) {
						try {
							Set<OfflineMessage> comparatorThread = threads.get(j);
							ArrayList<String> comparatorIdsForThread = idsForThreads.get(j);
							String comparatorBaseSubject = comparatorThread.iterator().next()
									.getBaseSubject();
							if (baseSubject.equals(comparatorBaseSubject)) {
								ArrayList<String> comparatorAddresses = getStringAddresses(comparatorThread);
								comparatorAddresses.retainAll(addresses);
								if (comparatorAddresses.size() > 0) {
									threads.remove(j);
									idsForThreads.remove(j);
									thread.addAll(comparatorThread);
									idsForThread.addAll(comparatorIdsForThread);
									addresses = getStringAddresses(thread);
								}
							}
						} catch (MessagingException e) {
							logMessage("Error: " + e.getMessage());
							e.printStackTrace();
						}
					}
				} catch (MessagingException e) {
					logMessage("Error: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void sortIntoThreads(OfflineMessage message, String messageID, int maxThreads,
			ArrayList<String> references, String inReplyTo,
			ArrayList<ArrayList<String>> idsForThreads, ArrayList<Set<OfflineMessage>> threads,
			Set<String> unseenMessages, Set<String> seenMessages) {

		if (references.size() == 0) {
			references = new ArrayList<String>();
			if (inReplyTo != null) {
				references.add(inReplyTo);
			}
		} else {
			references = (ArrayList<String>) references.clone();
		}
		references.add(messageID);

		Integer prevThread = null;
		for (int i = 0; i < idsForThreads.size(); i++) {
			ArrayList<String> threadIDs = idsForThreads.get(i);
			Set<OfflineMessage> thread = threads.get(i);

			if (getIntersectionSize(references, threadIDs) > 0) {
				if (prevThread == null) {
					prevThread = i;
					for (int j = 0; j < references.size(); j++) {
						if (!threadIDs.contains(references.get(j))) {
							threadIDs.add(references.get(j));
						}
					}
					thread.add(message);
				} else {
					ArrayList<String> threadToMergeTo = idsForThreads.get(prevThread);
					for (int j = 0; j < threadIDs.size(); j++) {
						if (!threadToMergeTo.contains(threadIDs.get(j))) {
							threadToMergeTo.add(threadIDs.get(j));
						}
					}
					threads.get(prevThread).addAll(threads.get(i));

					idsForThreads.remove(i);
					threads.remove(i);
					i--;

				}
			}
		}

		boolean added = false;
		if (prevThread == null && threads.size() < maxThreads) {
			idsForThreads.add(new ArrayList<String>(references));
			Set<OfflineMessage> thread = new HashSet<OfflineMessage>();
			thread.add(message);
			threads.add(thread);
			added = true;
		}

		if (prevThread != null || added) {
			unseenMessages.remove(messageID);
			references.removeAll(seenMessages);
			for (String id : references) {
				if (messageChecker.shouldIgnoreReference(id))
					continue;

				unseenMessages.add(id);
			}
		}
//		pruneThreads(idsForThreads, threads);
	}

	public void logFolderSizes(Folder folder, int depth) throws MessagingException {
		String prefix = "";
		for (int i = 0; i < depth - 1; i++) {
			prefix += "\t";
		}
		logMessage(prefix + folder.getFullName());

		prefix += "\t";
		Folder[] contents = folder.list();
		for (Folder content : contents) {
			try {
				logMessage(prefix + content.getFullName() + "(" + content.getMessageCount() + ")");
			} catch (MessagingException e) {
				logFolderSizes(content, depth + 1);
			}
		}
	}

	protected ThreadData retrieveThreads(Folder folder, boolean fetchAttachments)
			throws MessagingException {
		return retrieveThreads(folder, DEFAULT_MAX_MESSAGES, DEFAULT_NUM_THREADS_RETRIEVED,
				fetchAttachments);
	}

	protected ThreadData retrieveThreads(Object[] messages, int numMessages, int numThreads,
			boolean fetchAttachments) {
		ArrayList<ArrayList<String>> idsForThreads = new ArrayList<ArrayList<String>>();
		ArrayList<Set<OfflineMessage>> threads = new ArrayList<Set<OfflineMessage>>();
		Set<String> seenMessages = new TreeSet<String>();
		Set<String> unseenMessages = new TreeSet<String>();

		logMessage("Retrieving data from the latest " + numThreads
				+ " threads using at most the latest " + numMessages + " messages.\n");
		try {

			for (int msgPos = messages.length - 1; msgPos >= 0; msgPos--) {
				if (timeout != null) {
					try {
						logMessage("Sleeping for " + timeout + " ms");
						Thread.sleep(timeout);
					} catch (InterruptedException e) {
					}
				}

				String[] prefetchedHeaders = { "Message-ID", "References", "In-Reply-To",
						"References" };
				PrefetchOptions prefetchOptions = new PrefetchOptions(prefetchedHeaders,
						fetchAttachments);
				OfflineMessage message;
				try {
					message = new OfflineMessage((MimeMessage) messages[msgPos], prefetchOptions);
				} catch (Exception e) {
					logMessage("Error processing message: "
							+ CommandLineEmailDataRetriever.getStackTrace(e));
					continue;
				}

				if (message.getHeader("Message-ID") == null
						|| message.getHeader("Message-ID").length < 1) {
					continue;
				}

				String messageID = message.getHeader("Message-ID")[0];
				if (seenMessages.contains(messageID)) {
					continue;
				} else {
					seenMessages.add(messageID);
				}

				if (!messageChecker.shouldIgnore(message)
						|| (threads.size() == numThreads && unseenMessages.contains(messageID))) {
					ArrayList<String> references = message.getReferences();
					String inReplyTo = message.getInReplyTo();
					sortIntoThreads(message, messageID, numThreads, references, inReplyTo,
							idsForThreads, threads, unseenMessages, seenMessages);
				}

				if ((threads.size() >= numThreads && unseenMessages.size() == 0)
						|| seenMessages.size() >= numMessages) {
					break;
				}
				updateRetrievedMessageCounts(seenMessages.size(), threads.size(),
						unseenMessages.size());
			}

		} catch (MessagingException e) {
			e.printStackTrace();
			logMessage("ERROR: " + CommandLineEmailDataRetriever.getStackTrace(e));
		}

		updateRetrievedMessageCounts(seenMessages.size(), threads.size(), unseenMessages.size());
		return new ThreadData(messages.length, threads, seenMessages, unseenMessages);
	}

	protected ThreadData retrieveThreads(Folder folder, int numMessages, int numThreads,
			boolean fetchAttachments) throws MessagingException {

		int totalMessages = folder.getMessageCount();

		int maxMessage = totalMessages;
		int minMessage = Math.max(1, maxMessage - BUFFER_SIZE + 1);
		Message[] messages = folder.getMessages(minMessage, maxMessage);

		ArrayList<ArrayList<String>> idsForThreads = new ArrayList<ArrayList<String>>();
		ArrayList<Set<OfflineMessage>> threads = new ArrayList<Set<OfflineMessage>>();
		Set<String> seenMessages = new TreeSet<String>();
		Set<String> unseenMessages = new TreeSet<String>();

		logMessage("Retrieving data from the latest " + numThreads
				+ " threads using at most the latest " + numMessages + " messages.\n");
		try {
			while (true) {
				int startMessage = Math.min(-1 * (maxMessage - totalMessages) + 1, numMessages);
				// int endMessage = Math.min(-1*(minMessage - totalMessages)+1,
				// MAX_MESSAGES);
				//
				// logMessage("Retrieving messages "+(startMessage)+" to "+(endMessage)
				// + " (newest first)");
				updateRetrievedMessageCounts(Math.max(0, startMessage - 1), threads.size(),
						unseenMessages.size());

				for (int msgPos = messages.length - 1; msgPos >= 0; msgPos--) {
					String[] prefetchedHeaders = { "Message-ID", "References", "In-Reply-To",
							"References" };
					PrefetchOptions prefetchOptions = new PrefetchOptions(prefetchedHeaders,
							fetchAttachments);
					OfflineMessage message = new OfflineMessage((MimeMessage) messages[msgPos],
							prefetchOptions);

					if (message.getHeader("Message-ID") == null
							|| message.getHeader("Message-ID").length < 1) {
						continue;
					}

					String messageID = message.getHeader("Message-ID")[0];
					if (seenMessages.contains(messageID)) {
						continue;
					} else {
						seenMessages.add(messageID);
					}

					if (!messageChecker.shouldIgnore(message)
							|| (threads.size() == numThreads && unseenMessages.contains(messageID))) {
						ArrayList<String> references = message.getReferences();
						String inReplyTo = message.getInReplyTo();
						sortIntoThreads(message, messageID, numThreads, references, inReplyTo,
								idsForThreads, threads, unseenMessages, seenMessages);
					}

					if ((threads.size() >= numThreads && unseenMessages.size() == 0)
							|| seenMessages.size() >= numMessages) {
						break;
					}

					updateRetrievedMessageCounts((startMessage - 1) + (messages.length - msgPos),
							threads.size(), unseenMessages.size());
				}

				if (minMessage == 0) {
					break;
				}
				if ((threads.size() >= numThreads && unseenMessages.size() == 0)
						|| seenMessages.size() >= numMessages) {
					break;
				}

				maxMessage = Math.max(0, minMessage - 1);
				minMessage = Math.max(1, maxMessage - BUFFER_SIZE + 1);
				if (maxMessage == 0) {
					break;
				}
				messages = folder.getMessages(minMessage, maxMessage);

			}
		} catch (MessagingException e) {
			e.printStackTrace();
			logMessage("ERROR: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			logMessage("ERROR: " + e.getMessage());
		}

		updateRetrievedMessageCounts(seenMessages.size(), threads.size(), unseenMessages.size());
		folder.close(true);
		// folder.delete(true);
		return new ThreadData(totalMessages, threads, seenMessages, unseenMessages);
	}

	public abstract ThreadData retrieveThreads(int numMessages, int numThreads,
			boolean fetchAttachments) throws MessagingException;
}