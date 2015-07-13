package retriever.imap.outlook;

import java.util.ArrayList;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;

import retriever.MessageListener;
import retriever.ThreadData;
import retriever.ThreadRetriever;

public class OutlookThreadRetriever extends ThreadRetriever {

	public OutlookThreadRetriever(Store store, MessageListener listener) {
		this.store = store;
		super.addMessageListener(listener);
		super.timeout = 500L;
	}

	@Override
	public ThreadData retrieveThreads(int numMessages, int numThreads, boolean fetchAttachments)
			throws MessagingException {
		Folder inbox = store.getFolder("INBOX");
		inbox.open(Folder.READ_ONLY);
		Folder sent = store.getFolder("Sent Items");
		sent.open(Folder.READ_ONLY);

		/*
		 * Folder merged = store.getDefaultFolder().getFolder("merged");
		 * if(merged.exists()) merged.delete(true); boolean isCreated =
		 * merged.create(Folder.HOLDS_MESSAGES); System.out.println(isCreated);
		 * merged.open(Folder.READ_WRITE);
		 */

		ArrayList<Message> messages = new ArrayList<Message>();

		int inboxCount = inbox.getMessageCount();
		int sentCount = sent.getMessageCount();

		for (int i = 0; i < numMessages; i++) {
			if ((inboxCount < 1) || (sentCount < 1))
				break;

			if (timeout != null) {
				try {
					Thread.sleep(timeout);
				} catch (InterruptedException e) {
				}
			}
			Message currInbox = inbox.getMessage(inboxCount);
			Message currSent = sent.getMessage(sentCount);
			if (currInbox.getReceivedDate().after(currSent.getReceivedDate())) {
				messages.add(0, currInbox);
				inboxCount--;
				System.out.println("Inbox message with date: " + currInbox.getReceivedDate());
			} else {
				messages.add(0, currSent);
				sentCount--;
				System.out.println("Sent message with date: " + currSent.getReceivedDate());
			}
		}
		return super.retrieveThreads(messages.toArray(), numMessages, numThreads, fetchAttachments);
	}

}
