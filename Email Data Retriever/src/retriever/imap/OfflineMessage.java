package retriever.imap;

import java.util.Map;
import java.util.TreeMap;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public class OfflineMessage extends MimeMessage {

	MimeMessage parent;
	private final Map<String, String[]> seenHeaders = new TreeMap<>();

	public OfflineMessage(MimeMessage parent) throws MessagingException {
		super(parent);
		this.parent = parent;
	}

	@Override
	public String[] getHeader(String header) throws MessagingException {
		if (!seenHeaders.containsKey(header)) {
			String[] parentHeader = parent.getHeader(header);
			if (parentHeader != null) {
				seenHeaders.put(header, parentHeader);
			}
			return parentHeader;
		} else {
			return seenHeaders.get(header);
		}
	}
}
