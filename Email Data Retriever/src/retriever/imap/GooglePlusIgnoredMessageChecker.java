package retriever.imap;

import javax.mail.MessagingException;

import retriever.OfflineMessage;

public class GooglePlusIgnoredMessageChecker implements IgnoredMessageChecker {

	@Override
	public boolean shouldIgnoreReference(String referenceId) {
		return referenceId.endsWith("plus.google.com");
	}

	@Override
	public boolean shouldIgnore(OfflineMessage message) throws MessagingException {
		return false;
	}

}
