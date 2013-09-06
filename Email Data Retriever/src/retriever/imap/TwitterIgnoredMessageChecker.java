package retriever.imap;

import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.MessagingException;

import retriever.OfflineMessage;

public class TwitterIgnoredMessageChecker implements IgnoredMessageChecker {

	Pattern fromPattern = Pattern.compile("(.*<)?\\s*info@twitter.com(\\s*>\\s*)?");

	@Override
	public boolean shouldIgnore(OfflineMessage message) throws MessagingException {
		Address[] fromArray = message.getFrom();
		if (fromArray != null && fromArray.length > 0) {
			String from = fromArray[0].toString();
			if (fromPattern.matcher(from).matches()) {
				return true;
			}
		}
		String[] messageIdArray = message.getHeader("Message-ID");
		if (messageIdArray == null || messageIdArray.length < 1) {
			return true;
		}
		String messageID = message.getHeader("Message-ID")[0];
		if (messageID.endsWith("@spruce-goose.twitter.com")) {
			return true;
		}
		return false;
	}

	@Override
	public boolean shouldIgnoreReference(String referenceId) {
		return fromPattern.matcher(referenceId).matches()
				|| referenceId.endsWith("@spruce-goose.twitter.com");
	}

}
