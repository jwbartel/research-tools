package retriever.imap;

import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;

import retriever.MessageListener;

public class ImapAuthenticator {

	Store store;

	ArrayList<MessageListener> messageListeners = new ArrayList<MessageListener>();

	private class JavaMailAuthenticator extends Authenticator {
		String user;
		String pw;

		public JavaMailAuthenticator(String username, String password) {
			super();
			this.user = username;
			this.pw = password;
		}

		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(user, pw);
		}
	}

	public ImapAuthenticator() {
	}

	public void login(String imapServer, String email, String password) throws MessagingException {
		logMessage("Logging in");

		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		props.setProperty("mail.imap.starttls.enable", "true");
		Session session = Session.getDefaultInstance(props, null);
		store = session.getStore("imaps");
		logMessage(email);
		store.connect(imapServer, email, password);

		logMessage("Logged into: " + store.getURLName().toString() + "\n");
	}

	public Store getStore() {
		return store;
	}

	public void logMessage(String message) {
		for (MessageListener listener : messageListeners) {
			listener.logMessage(message);
		}
	}

	public void addMessageListener(MessageListener l) {
		messageListeners.add(l);
	}

	public boolean isLoggedIn() {
		return store.isConnected();
	}
}
