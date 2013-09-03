package retriever.imap;

import javax.mail.MessagingException;

import retriever.OfflineMessage;

public class MasterIgnoredMessageChecker implements IgnoredMessageChecker {

	IgnoredMessageChecker[] checkers = {
			new FacebookIgnoredMessageChecker(),
			new TwitterIgnoredMessageChecker(),
	};
	
	@Override
	public boolean shouldIgnore(OfflineMessage message)
			throws MessagingException {
		for (IgnoredMessageChecker checker: checkers) {
			if (checker.shouldIgnore(message)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean shouldIgnoreReference(String referenceId) {
		for (IgnoredMessageChecker checker: checkers) {
			if (checker.shouldIgnoreReference(referenceId)) {
				return true;
			}
		}
		return false;
	}
}
