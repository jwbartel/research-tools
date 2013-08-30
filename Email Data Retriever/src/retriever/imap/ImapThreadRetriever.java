package retriever.imap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;

import retriever.MessageListener;

public class ImapThreadRetriever {
	String imapServer;
	Store store;

	public final static int MAX_MESSAGES = 10000;
	public final static int NUM_THREADS_RETRIEVED = 200;
	public final static int BUFFER_SIZE = 100;

	ArrayList<MessageListener> messageListeners = new ArrayList<MessageListener>();

	public ImapThreadRetriever(String imapServer, Store store) {
		this.imapServer = imapServer;
		this.store = store;
	}

	public void logMessage(String message) {
		for (MessageListener listener : messageListeners) {
			listener.logMessage(message);
		}
	}

	public void updateRetrievedMessageCounts(int latestRetrieved) {
		for (MessageListener listener : messageListeners) {
			listener.updateRetrievedMessageCounts(latestRetrieved);
		}
	}

	private ArrayList<String> getReferences(Message message) throws MessagingException {
		ArrayList<String> references = new ArrayList<String>();
		if (message.getHeader("References") != null) {
			String[] refHeader = message.getHeader("References");
			for (int i = 0; i < refHeader.length; i++) {
				String[] entries = refHeader[i].split("\\s*((,\\s*\n)|(\n\\s*,)|(,)|(\n))\\s*");
				for (int j = 0; j < entries.length; j++) {
					references.add(entries[j]);
				}
			}
		}
		return references;
	}

	private String getInReplyTo(Message message) throws MessagingException {
		String inReplyTo = null;
		if (message.getHeader("In-Reply-To") != null) {
			inReplyTo = message.getHeader("In-Reply-To")[0];
		}
		return inReplyTo;
	}

	private int getIntersectionSize(ArrayList<String> group1, ArrayList<String> group2) {
		ArrayList<String> intersection = new ArrayList<String>(group1);
		intersection.retainAll(group2);
		return intersection.size();
	}

	private int getIntersectionSize(Set<String> group1, ArrayList<String> group2) {
		Set<String> intersection = new TreeSet<String>(group1);
		intersection.retainAll(group2);
		return intersection.size();
	}

	public ThreadData retrieveThreads() throws MessagingException {
		Folder folder = store.getFolder("[Gmail]/All Mail");
		folder.open(Folder.READ_ONLY);

		int totalMessages = folder.getMessageCount();

		int maxMessage = totalMessages;
		int minMessage = Math.max(0, maxMessage - BUFFER_SIZE + 1);
		Message[] messages = folder.getMessages(minMessage, maxMessage);

		ArrayList<ArrayList<String>> idsForThreads = new ArrayList<ArrayList<String>>();
		ArrayList<Set<Message>> threads = new ArrayList<Set<Message>>();
		Set<String> seenMessages = new TreeSet<String>();
		Set<String> unseenMessages = new TreeSet<String>();

		logMessage("Retrieving data from the latest " + NUM_THREADS_RETRIEVED
				+ " threads using at most the latest " + MAX_MESSAGES + " messages.\n");
		try {
			while (true) {
				int startMessage = Math.min(-1 * (maxMessage - totalMessages) + 1, MAX_MESSAGES);
				// int endMessage = Math.min(-1*(minMessage - totalMessages)+1,
				// MAX_MESSAGES);
				//
				// logMessage("Retrieving messages "+(startMessage)+" to "+(endMessage)
				// + " (newest first)");
				updateRetrievedMessageCounts(Math.max(0, startMessage - 1));

				for (int msgPos = messages.length - 1; msgPos >= 0; msgPos--) {
					Message message = messages[msgPos];

					String messageID = message.getHeader("Message-ID")[0];
					if (seenMessages.contains(messageID)) {
						continue;
					} else {
						seenMessages.add(messageID);
					}

					ArrayList<String> references = getReferences(message);
					String inReplyTo = getInReplyTo(message);
					sortIntoThreads(message, messageID, references, inReplyTo, idsForThreads,
							threads, unseenMessages, seenMessages);
					message.getSubject();
					message.getReceivedDate();
					message.getFrom();

					if ((threads.size() >= NUM_THREADS_RETRIEVED && unseenMessages.size() == 0)
							|| seenMessages.size() >= MAX_MESSAGES) {
						break;
					}

					updateRetrievedMessageCounts(startMessage + (messages.length - msgPos));
				}

				if (minMessage == 0) {
					break;
				}
				if ((threads.size() >= NUM_THREADS_RETRIEVED && unseenMessages.size() == 0)
						|| seenMessages.size() >= MAX_MESSAGES) {
					break;
				}

				maxMessage = minMessage - 1;
				minMessage = Math.max(0, maxMessage - BUFFER_SIZE + 1);
				messages = folder.getMessages(minMessage, maxMessage);

			}
		} catch (MessagingException e) {
			logMessage("ERROR: " + e.getMessage());
		}

		updateRetrievedMessageCounts(MAX_MESSAGES);
		return new ThreadData(threads, seenMessages);
	}

	@SuppressWarnings("unchecked")
	public void sortIntoThreads(Message message, String messageID, ArrayList<String> references,
			String inReplyTo, ArrayList<ArrayList<String>> idsForThreads,
			ArrayList<Set<Message>> threads, Set<String> unseenMessages, Set<String> seenMessages) {

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
			Set<Message> thread = threads.get(i);

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

					threadIDs.remove(i);
					threads.remove(i);
					i--;

				}
			}
		}

		boolean added = false;
		if (prevThread == null && threads.size() < NUM_THREADS_RETRIEVED) {
			idsForThreads.add(new ArrayList<String>(references));
			Set<Message> thread = new HashSet<Message>();
			thread.add(message);
			threads.add(thread);
			added = true;
		}

		if (prevThread != null || added) {
			unseenMessages.remove(messageID);
			references.removeAll(seenMessages);
			for (String id : references) {
				if (id.startsWith("Message Email Reply"))
					continue;
				if (id.endsWith("groups.facebook.com>"))
					continue;

				unseenMessages.add(id);
			}
		}
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

	public void addMessageListener(MessageListener l) {
		messageListeners.add(l);
	}

}
