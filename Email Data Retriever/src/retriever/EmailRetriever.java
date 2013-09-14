package retriever;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;

import javax.mail.Folder;
import javax.mail.MessagingException;

import retriever.imap.ImapAuthenticator;
import retriever.imap.ImapThreadRetriever;
public class EmailRetriever implements Serializable, MessageListener{
	String imapServer = "imap.gmail.com";
	String emailAddress = "";
	String password = "";
	
	ArrayList<String> messages = new ArrayList<String>();
	ArrayList<MessageListener> messageListeners = new ArrayList<MessageListener>();
	
	boolean loginSubmitted = false;
	boolean credentialEditingEnabled = true;
	PropertyChangeSupport propertyChange = new PropertyChangeSupport(this);
	
	ImapAuthenticator authenticator = null;
	ImapThreadRetriever retriever = null;
	
	protected void disableCredentialEditing(){
		credentialEditingEnabled = false;
	}
	
	protected void enableCredentialEditing(){
		credentialEditingEnabled = true;
	}
	
	private synchronized void maybeStartRetrieval(){
		if(!loginSubmitted && imapServer.length() > 0 && emailAddress.length() > 0 && password.length() > 0){
			disableCredentialEditing();
			Runnable r = new Runnable(){
				public void run(){
					login();
					retrieve();
					enableCredentialEditing();
					
				}
			};
			Thread t = new Thread(r);
			t.start();
		}
	}
	
	protected boolean login(){
		try {
			authenticator = new ImapAuthenticator();
			authenticator.addMessageListener(this);
			authenticator.login(imapServer, emailAddress, password);
			return true;
		} catch (MessagingException e) {
			logMessage("ERROR:"+e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	protected boolean retrieve(){
		try{
			retriever = new ImapThreadRetriever(imapServer, authenticator.getStore());
			retriever.addMessageListener(this);
			retriever.retrieveThreads();
			return true;
		} catch (MessagingException e) {
			logMessage("ERROR:"+e.getMessage());
			return false;
		}
	}
	
	public String getImapServer() {
		return imapServer;
	}
	
	public void setImapServer(String imapServer) {
		if(!credentialEditingEnabled){
			propertyChange.firePropertyChange("ImapServer", imapServer, this.imapServer);
			messages.add("Cannot edit imap server during processing");
			return;
		}
		String oldImapServer = this.imapServer;
		if(oldImapServer.equals(imapServer)) return;
		this.imapServer = imapServer;
		propertyChange.firePropertyChange("ImapServer", oldImapServer, imapServer);
		maybeStartRetrieval();
	}
	
	public String getEmailAddress() {
		return emailAddress;
	}
	
	public void setEmailAddress(String emailAddress) {
		if(!credentialEditingEnabled){
			propertyChange.firePropertyChange("EmailAddress", emailAddress, this.emailAddress);
			messages.add("Cannot edit email address during processing");
			return;
		}
		String oldAddress = this.emailAddress;
		if(oldAddress.equals(emailAddress)){return; }
		this.emailAddress = emailAddress;
		propertyChange.firePropertyChange("EmailAddress", oldAddress, emailAddress);
		maybeStartRetrieval();
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		if(!credentialEditingEnabled){
			propertyChange.firePropertyChange("Password", password, this.password);
			messages.add("Cannot edit password during processing");
			return;
		}
		String oldPassword = this.password;
		if(oldPassword.equals(password)) return;
		this.password = password;
		propertyChange.firePropertyChange("Password", oldPassword, password);
		maybeStartRetrieval();
	}
	
	public ArrayList<String> getMessages(){
		return messages;
	}
	
	public void logMessage(String message){
		@SuppressWarnings("unchecked")
		ArrayList<String> oldMessages = (ArrayList<String>) messages.clone();
		messages.add(message);
		propertyChange.firePropertyChange("Messages", oldMessages, messages);
		
		for(MessageListener listener: messageListeners){
			listener.logMessage(message);
		}
	}
	
	
	
	public void addPropertyChangeListener(PropertyChangeListener l) {
		propertyChange.addPropertyChangeListener(l);
	}
	
	public void addMessageListener(MessageListener l){
		messageListeners.add(l);
	}

	@Override
	public void updateRetrievedMessageCounts(int latestRetrieved, int seenThreads, int missingMessages) {
		// TODO Auto-generated method stub
		
	}
}
