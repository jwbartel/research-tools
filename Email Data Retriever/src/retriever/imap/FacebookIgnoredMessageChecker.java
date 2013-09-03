package retriever.imap;

import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.mail.MessagingException;

import retriever.OfflineMessage;

public class FacebookIgnoredMessageChecker implements IgnoredMessageChecker {

	Pattern fromPattern = Pattern.compile("(.*<)?\\s*update[+].+@facebookmail.com(\\s*>\\s*)?");
	
	@Override
	public boolean shouldIgnore(OfflineMessage message) throws MessagingException {
		ArrayList<String> references = message.getReferences();
		String id = message.getHeader("Message-ID")[0];
		if (id.startsWith("Message Email Reply") || id.endsWith("groups.facebook.com>")) {
			return true;
		}
		if (fromPattern.matcher(message.getFrom()[0].toString()).matches()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean shouldIgnoreReference(String referenceId) {
		return referenceId.startsWith("Message Email Reply")
			|| referenceId.endsWith("groups.facebook.com>");
	}

}
