package retriever.imap;

import javax.mail.MessagingException;

public interface IgnoredMessageChecker {
	
	public boolean shouldIgnoreReference(String referenceId);
	public boolean shouldIgnore(OfflineMessage message) throws MessagingException;
}
