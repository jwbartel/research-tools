package retriever;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;

public class OfflineMessage {

	static String NON_WSP = "([^\\s])"; // any CHAR other than WSP
	static String WSP = "([\\s])";
	static String BLOBCHAR = "([^\\[\\]])"; // any CHAR except '[' and ']

	static String SUBJ_BLOB = "(" + "\\[" + BLOBCHAR + "*" + "\\]" + WSP + "*" + ")";
	static String SUBJ_REFWD = "(" + "((re)|(fw[d]?))" + WSP + "*" + SUBJ_BLOB + "?" + ":" + ")";

	static String SUBJ_FWD_HDR = "[fwd:";
	static String SUBJ_FWD_TRL = "]";

	static String SUBJ_LEADER = "(" + "(" + SUBJ_BLOB + "*" + SUBJ_REFWD + ")" + "|" + WSP + ")";
	static String SUBJ_TRAILER = "(" + "([(]fwd[)])" + "|" + WSP + ")";

	static Pattern SUBJ_BLOB_PATTERN = Pattern.compile(SUBJ_BLOB);
	static Pattern SUBJ_LEADER_PATTERN = Pattern.compile(SUBJ_LEADER);

	MimeMessage parent;
	private final Map<String, String[]> seenHeaders = new TreeMap<String, String[]>();
	private String subject = null;
	private String baseSubject = null;
	private Date receivedDate;
	private Address[] from;
	private Address[] allRecipients;
	private final ArrayList<String> attachedFiles = new ArrayList<String>();

	public OfflineMessage(MimeMessage parent, String[] prefetchedHeaders)
			throws MessagingException, IOException {
		this.parent = parent;
		preloadData(prefetchedHeaders);
	}

	private void preloadData(String[] prefetchedHeaders) throws MessagingException, IOException {
		subject = parent.getSubject();
		receivedDate = parent.getReceivedDate();
		from = parent.getFrom();
		allRecipients = parent.getAllRecipients();
		for (String header : prefetchedHeaders) {
			seenHeaders.put(header, parent.getHeader(header));
		}
		loadAttachments();
	}

	public String[] getHeader(String header) throws MessagingException {
		if (!seenHeaders.containsKey(header)) {
			throw new MessagingException("Header value " + header + " was not preloaded");
			// String[] parentHeader = parent.getHeader(header);
			// if (parentHeader != null) {
			// seenHeaders.put(header, parentHeader);
			// }
			// return parentHeader;
		} else {
			return seenHeaders.get(header);
		}
	}

	public String getSubject() throws MessagingException {
		if (subject == null) {
			subject = parent.getSubject();
		}
		return subject;
	}

	public Date getReceivedDate() throws MessagingException {
		if (receivedDate == null) {
			receivedDate = parent.getReceivedDate();
		}
		return receivedDate;
	}

	public Address[] getFrom() throws MessagingException {
		if (from == null) {
			from = parent.getFrom();
		}
		return from;
	}

	public Address[] getAllRecipients() throws MessagingException {
		if (allRecipients == null) {
			allRecipients = parent.getAllRecipients();
		}
		return allRecipients;
	}

	public ArrayList<String> getReferences() throws MessagingException {
		ArrayList<String> references = new ArrayList<String>();
		if (getHeader("References") != null) {
			String[] refHeader = getHeader("References");
			for (int i = 0; i < refHeader.length; i++) {
				String[] entries = refHeader[i].split("\\s*((,\\s*\n)|(\n\\s*,)|(,)|(\n))\\s*");
				for (int j = 0; j < entries.length; j++) {
					references.add(entries[j]);
				}
			}
		}
		return references;
	}

	public String getInReplyTo() throws MessagingException {
		String inReplyTo = null;
		if (getHeader("In-Reply-To") != null && getHeader("In-Reply-To").length > 0) {
			inReplyTo = getHeader("In-Reply-To")[0];
		}
		return inReplyTo;
	}

	public String getBaseSubject() throws MessagingException {
		if (baseSubject == null && subject != null) {
			try {
				baseSubject = extractBaseSubject();
			} catch (UnsupportedEncodingException e) {
				throw new MessagingException("Error generating base subject", e);
			}
		}
		return baseSubject;
	}

	public ArrayList<String> getAttachedFiles() {
		return attachedFiles;
	}

	private String extractBaseSubject() throws UnsupportedEncodingException {

		String baseSubject = new String(subject.getBytes("UTF-8"), "UTF-8").toLowerCase();
		baseSubject = baseSubject.replaceAll("\t", " ");
		baseSubject = baseSubject.replaceAll("[ ]+", " ");

		while (true) {
			while (baseSubject.matches(".*" + SUBJ_TRAILER)) {
				if (baseSubject.endsWith("(fwd)")) {
					baseSubject = baseSubject.substring(0, baseSubject.length() - 5);
				} else {
					baseSubject = baseSubject.substring(0, baseSubject.length() - 1);
				}
			}

			boolean shouldCheckAgain = true;
			while (shouldCheckAgain) {
				Matcher matcher = SUBJ_LEADER_PATTERN.matcher(baseSubject);
				if (matcher.find() && matcher.start() == 0) {
					baseSubject = baseSubject.substring(matcher.group().length());
					shouldCheckAgain = true;
				} else {
					shouldCheckAgain = false;
				}

				matcher = SUBJ_BLOB_PATTERN.matcher(baseSubject);
				if (matcher.find() && matcher.start() == 0 && matcher.end() != baseSubject.length()) {
					baseSubject = baseSubject.substring(matcher.group().length());
					shouldCheckAgain = true;
				}

			}

			if (baseSubject.startsWith(SUBJ_FWD_HDR) && baseSubject.endsWith(SUBJ_FWD_TRL)) {
				baseSubject = baseSubject.substring(SUBJ_FWD_HDR.length(), baseSubject.length()
						- SUBJ_FWD_TRL.length());
			} else {
				break;
			}
		}
		return baseSubject;
	}

	private void loadAttachments() throws IOException, MessagingException {
		if (parent.getContent() instanceof Multipart) {
			Multipart multipart = (Multipart) parent.getContent();
			for (int i = 0; i < multipart.getCount(); i++) {
				BodyPart bodypart = multipart.getBodyPart(i);
				if (Part.ATTACHMENT.equalsIgnoreCase(bodypart.getDisposition())) {
					attachedFiles.add(bodypart.getFileName());
				}
			}
		}
	}
}
