package retriever.imap.outlook;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import retriever.MessageListener;
import retriever.OfflineMessage;
import retriever.ThreadData;
import retriever.ThreadRetriever;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class OutlookThreadRetriever extends ThreadRetriever {

	public OutlookThreadRetriever(Store store, MessageListener listener) {
		this.store = store;
		super.addMessageListener(listener);
	}

	@Override
	public ThreadData retrieveThreads(int numMessages, int numThreads) throws MessagingException {
		Folder inbox = store.getFolder("INBOX");
		inbox.open(Folder.READ_ONLY);
        Folder sent = store.getFolder("Sent Items");
        sent.open(Folder.READ_ONLY);

    /*    Folder merged = store.getDefaultFolder().getFolder("merged");
        if(merged.exists()) merged.delete(true);
        boolean isCreated = merged.create(Folder.HOLDS_MESSAGES);
        System.out.println(isCreated);
        merged.open(Folder.READ_WRITE);     */

        ArrayList<Message> messages = new ArrayList<Message>();

        int inboxCount = inbox.getMessageCount();
        int sentCount = sent.getMessageCount();

        for (int i=0;i<numMessages;i++) {
            if((inboxCount<1)||(sentCount<1)) break;
            Message currInbox = inbox.getMessage(inboxCount);
            Message currSent = sent.getMessage(sentCount);
            if(currInbox.getReceivedDate().after(currSent.getReceivedDate())) {
                messages.add(0,currInbox);
                inboxCount--;
                System.out.println("Inbox message with date: " +currInbox.getReceivedDate());
            } else {
                messages.add(0,currSent);
                sentCount--;
                System.out.println("Sent message with date: " +currSent.getReceivedDate());
            }
            System.out.println((i+1)+"\n");
        }
		return super.retrieveThreads(messages.toArray(), numMessages, numThreads);
	}

}
