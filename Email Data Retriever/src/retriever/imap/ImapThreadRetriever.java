package retriever.imap;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.mail.MessagingException;
import javax.mail.Store;

import retriever.MessageListener;
import retriever.OfflineMessage;
import retriever.ThreadData;
import retriever.ThreadRetriever;
import retriever.imap.gmail.GmailThreadRetriever;
import retriever.imap.outlook.OutlookThreadRetriever;

public class ImapThreadRetriever extends ThreadRetriever implements MessageListener {
	String imapServer;
	Store store;

	private final static String GMAIL = "gmail";
    private final static String OUTLOOK = "outlook";

	final Map<String, ThreadRetriever> retrievers = new TreeMap<String, ThreadRetriever>();

	public ImapThreadRetriever(String imapServer, Store store) {
        this.imapServer = imapServer;
		this.store = store;
		if(imapServer.equals("imap.gmail.com"))
            retrievers.put(GMAIL, new GmailThreadRetriever(store, this));
        if(imapServer.equals("outlook.office365.com"))
            retrievers.put(OUTLOOK, new OutlookThreadRetriever(store, this));
	}

	@Override
	public ThreadData retrieveThreads(int numMessages, int numThreads) throws MessagingException {
		if (imapServer.equals("imap.gmail.com")) {
			return retrievers.get(GMAIL).retrieveThreads(numMessages, numThreads);
		}
        else if (imapServer.equals("outlook.office365.com")) {
            return retrievers.get(OUTLOOK).retrieveThreads(numMessages,numThreads);
        }
		return new ThreadData(0, new ArrayList<Set<OfflineMessage>>(), new TreeSet<String>(),
				new TreeSet<String>());
	}

}
