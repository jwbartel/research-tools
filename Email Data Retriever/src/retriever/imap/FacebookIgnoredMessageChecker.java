package retriever.imap;

import java.util.regex.Pattern;

import javax.mail.MessagingException;

import retriever.OfflineMessage;

public class FacebookIgnoredMessageChecker implements IgnoredMessageChecker {

	Pattern fromPattern = Pattern.compile("(.*<)?\\s*update[+].+@facebookmail.com(\\s*>\\s*)?");

	@Override
	public boolean shouldIgnore(OfflineMessage message) throws MessagingException {

		String[] messageIdArray = message.getHeader("Message-ID");
		if (messageIdArray == null || messageIdArray.length < 1) {
			return true;
		}
		String id = message.getHeader("Message-ID")[0];
		if (id.startsWith("Message Email Reply") || id.endsWith("groups.facebook.com>")) {
			return true;
		}
		// Address[] from = message.getFrom();
		// if (from.length > 0 &&
		// fromPattern.matcher(from[0].toString()).matches()) {
		// return true;
		// }
		return false;
	}

	@Override
	public boolean shouldIgnoreReference(String referenceId) {
		return referenceId.startsWith("Message Email Reply")
				|| referenceId.endsWith("groups.facebook.com>");
	}

}
