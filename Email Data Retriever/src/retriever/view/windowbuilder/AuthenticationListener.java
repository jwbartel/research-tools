package retriever.view.windowbuilder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import retriever.imap.ImapAuthenticator;

public class AuthenticationListener implements ActionListener {
	AuthenticationPane pane;
	ImapAuthenticator authenticator;
	
	public AuthenticationListener(AuthenticationPane pane, ImapAuthenticator authenticator){
		this.pane = pane;
		this.authenticator = authenticator;
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String imapServer = pane.getIMAPServer();
		String emailAddress = pane.getEmailAddress();
		String password = pane.getPassword();
		if (imapServer.length() > 0 && emailAddress.length() > 0 && password.length() > 0){
			pane.credentialEditingEnabled(false);
			Runnable run = new Runnable(){
				public void run(){
					if(!pane.login() || !pane.retrieve()){
						pane.credentialEditingEnabled(true);
						return;
					}
				}
			};
			Thread t = new Thread(run);
			t.start();
		}
	}

	
}
