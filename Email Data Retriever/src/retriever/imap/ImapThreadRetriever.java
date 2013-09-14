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

public class ImapThreadRetriever extends ThreadRetriever implements MessageListener {
	String imapServer;
	Store store;

	private final static String GMAIL = "gmail";

	final Map<String, ThreadRetriever> retrievers = new TreeMap<String, ThreadRetriever>();

	public ImapThreadRetriever(String imapServer, Store store) {
		this.imapServer = imapServer;
		this.store = store;
		retrievers.put(GMAIL, new GmailThreadRetriever(store, this));
	}

	@Override
	public ThreadData retrieveThreads() throws MessagingException {
		if (imapServer.equals("imap.gmail.com")) {
			return retrievers.get(GMAIL).retrieveThreads();
		}

		return new ThreadData(0, new ArrayList<Set<OfflineMessage>>(), new TreeSet<String>(),
				new TreeSet<String>());
	}

}
