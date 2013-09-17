package retriever.view.windowbuilder;

import java.awt.Button;
import java.awt.Color;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import retriever.MessageListener;
import retriever.ThreadRetriever;
import retriever.imap.ImapAuthenticator;

public class AuthenticationPane extends JPanel implements MessageListener {
	EmailDataRetriever parent;
	private final JComboBox imapServer;
	private final JTextField smtpServer;
	private final JTextField emailAddress;
	private final JPasswordField password;
	Button submit;
	JTextArea textArea;
	JProgressBar progressBar;
	JLabel progressMessage;

	AuthenticationListener listener;

	String getIMAPServer() {
		return imapServer.getSelectedItem().toString();
	}

	String getSMTPServer() {
		return smtpServer.getText();
	}

	String getEmailAddress() {
		return emailAddress.getText();
	}

	String getPassword() {
		char[] passwordChars = password.getPassword();
		return new String(passwordChars);
	}

	public void credentialEditingEnabled(boolean enabled) {
		imapServer.setEnabled(false);
		smtpServer.setEditable(enabled);
		emailAddress.setEditable(enabled);
		password.setEditable(enabled);
		submit.setEnabled(enabled);

	}

	public void defaultFocus() {
		emailAddress.requestFocus();
	}

	/**
	 * Create the panel.
	 */
	public AuthenticationPane(EmailDataRetriever parent, ImapAuthenticator authenticator) {
		this.parent = parent;
		listener = new AuthenticationListener(this, authenticator);

		JPanel credentials = new JPanel();

		JLabel imapServerLabel = new JLabel("IMAP Server");

		JLabel smtpServerLabel = new JLabel("SMTP Server");

		smtpServer = new JTextField();
		smtpServer.setEditable(false);
		smtpServer.setText("smtp.gmail.com");
		smtpServer.addActionListener(listener);

		JLabel emailAddressLabel = new JLabel("Email or Username");

		emailAddress = new JTextField();
		emailAddress.addActionListener(listener);

		JLabel passwordLabel = new JLabel("Password");

		password = new JPasswordField();
		password.addActionListener(listener);

		String[] possibleImapServers = { "imap.gmail.com" };// ,
															// "outlook.unc.edu"};
		imapServer = new JComboBox();
		for (String possibleImapServer : possibleImapServers) {
			imapServer.addItem(possibleImapServer);
		}
		imapServer.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (getIMAPServer().equals("imap.gmail.com")) {
					smtpServer.setText("smtp.gmail.com");
				} else if (getIMAPServer().equals("outlook.unc.edu")) {
					smtpServer.setText("smtp.unc.edu");
				} else {
					smtpServer.setText("");
				}
			}

		});

		GroupLayout gl_credentials = new GroupLayout(credentials);
		gl_credentials
				.setHorizontalGroup(gl_credentials
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								gl_credentials
										.createSequentialGroup()
										.addGroup(
												gl_credentials
														.createParallelGroup(Alignment.LEADING)
														.addComponent(imapServerLabel)
														.addComponent(smtpServerLabel)
														.addComponent(emailAddressLabel)
														.addComponent(passwordLabel))
										.addGap(3)
										.addGroup(
												gl_credentials
														.createParallelGroup(Alignment.LEADING)
														.addComponent(emailAddress,
																GroupLayout.DEFAULT_SIZE, 205,
																Short.MAX_VALUE)
														.addComponent(password,
																GroupLayout.DEFAULT_SIZE, 205,
																Short.MAX_VALUE)
														.addGroup(
																gl_credentials
																		.createSequentialGroup()
																		.addPreferredGap(
																				ComponentPlacement.UNRELATED)
																		.addGroup(
																				gl_credentials
																						.createParallelGroup(
																								Alignment.LEADING)
																						.addComponent(
																								smtpServer,
																								Alignment.TRAILING,
																								GroupLayout.DEFAULT_SIZE,
																								205,
																								Short.MAX_VALUE)
																						.addComponent(
																								imapServer,
																								0,
																								204,
																								Short.MAX_VALUE))))));
		gl_credentials.setVerticalGroup(gl_credentials.createParallelGroup(Alignment.LEADING)
				.addGroup(
						gl_credentials
								.createSequentialGroup()
								.addGroup(
										gl_credentials
												.createParallelGroup(Alignment.LEADING)
												.addComponent(imapServerLabel)
												.addComponent(imapServer,
														GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE,
														GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(
										gl_credentials
												.createParallelGroup(Alignment.LEADING)
												.addComponent(smtpServerLabel)
												.addComponent(smtpServer,
														GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE,
														GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(
										gl_credentials
												.createParallelGroup(Alignment.LEADING)
												.addComponent(emailAddressLabel)
												.addComponent(emailAddress,
														GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE,
														GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(
										gl_credentials
												.createParallelGroup(Alignment.LEADING)
												.addComponent(passwordLabel)
												.addComponent(password, GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE,
														GroupLayout.PREFERRED_SIZE))
								.addContainerGap()));
		credentials.setLayout(gl_credentials);

		JPanel submitPanel = new JPanel();

		submit = new Button("Start collecting data");
		submit.setForeground(Color.BLACK);
		submitPanel.add(submit);
		submit.addActionListener(listener);

		JPanel logPanel = new JPanel();

		Label label = new Label("Log");

		textArea = new JTextArea();
		textArea.setLineWrap(false);
		JScrollPane textAreaPane = new JScrollPane(textArea);
		textArea.setEditable(false);

		JLabel lblMessagesRetrieved = new JLabel("Data Collection Progress");
		progressBar = new JProgressBar();
		progressBar.setMaximum(ThreadRetriever.MAX_MESSAGES);
		progressMessage = new JLabel("<html>" + "Message 0 of a maximum of "
				+ ThreadRetriever.MAX_MESSAGES + "\r\n" + "<br>"
				+ "Collected data on 0 of a maximum of " + ThreadRetriever.NUM_THREADS_RETRIEVED
				+ " threads" + "<br>" + "Missing data from 0 messages" + "</html>");

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout
				.setHorizontalGroup(groupLayout
						.createParallelGroup(Alignment.TRAILING)
						.addGroup(
								groupLayout
										.createSequentialGroup()
										.addGroup(
												groupLayout
														.createParallelGroup(Alignment.LEADING)
														.addGroup(
																Alignment.TRAILING,
																groupLayout
																		.createParallelGroup(
																				Alignment.LEADING)
																		.addComponent(
																				lblMessagesRetrieved,
																				GroupLayout.DEFAULT_SIZE,
																				274,
																				Short.MAX_VALUE)
																		.addComponent(
																				logPanel,
																				GroupLayout.DEFAULT_SIZE,
																				274,
																				Short.MAX_VALUE)
																		.addGroup(
																				groupLayout
																						.createSequentialGroup()
																						.addContainerGap()
																						.addComponent(
																								progressBar,
																								GroupLayout.DEFAULT_SIZE,
																								264,
																								Short.MAX_VALUE))
																		.addComponent(
																				credentials,
																				GroupLayout.DEFAULT_SIZE,
																				274,
																				Short.MAX_VALUE))
														.addComponent(submitPanel,
																Alignment.TRAILING,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addContainerGap()
																		.addComponent(
																				progressMessage)))
										.addContainerGap()));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
				groupLayout
						.createSequentialGroup()
						.addComponent(credentials, GroupLayout.PREFERRED_SIZE,
								GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(submitPanel, GroupLayout.PREFERRED_SIZE, 32,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(lblMessagesRetrieved, GroupLayout.PREFERRED_SIZE, 14,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(progressBar, GroupLayout.PREFERRED_SIZE,
								GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(progressMessage)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(logPanel, GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE)));
		GroupLayout gl_logPanel = new GroupLayout(logPanel);
		gl_logPanel.setHorizontalGroup(gl_logPanel
				.createParallelGroup(Alignment.LEADING)
				.addGroup(
						gl_logPanel
								.createSequentialGroup()
								.addGap(6)
								.addComponent(textAreaPane, GroupLayout.DEFAULT_SIZE, 268,
										Short.MAX_VALUE))
				.addGroup(
						Alignment.TRAILING,
						gl_logPanel
								.createSequentialGroup()
								.addComponent(label, GroupLayout.PREFERRED_SIZE, 54,
										GroupLayout.PREFERRED_SIZE)
								.addContainerGap(220, Short.MAX_VALUE)));
		gl_logPanel
				.setVerticalGroup(gl_logPanel.createParallelGroup(Alignment.LEADING).addGroup(
						gl_logPanel
								.createSequentialGroup()
								.addComponent(label, GroupLayout.PREFERRED_SIZE, 19,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(textAreaPane, GroupLayout.DEFAULT_SIZE, 77,
										Short.MAX_VALUE)));
		logPanel.setLayout(gl_logPanel);
		setLayout(groupLayout);

	}

	@Override
	public void logMessage(String message) {
		textArea.setText(textArea.getText() + message + "\n");
	}

	@Override
	public void updateRetrievedMessageCounts(int latestRetrieved, int seenThreads,
			int missingMessages) {
		progressBar.setValue(latestRetrieved);
		progressBar.setMaximum(latestRetrieved
				+ (ThreadRetriever.NUM_THREADS_RETRIEVED - seenThreads) + missingMessages);
		progressMessage.setText("<html>" + "Message " + latestRetrieved + " of a maximum of "
				+ ThreadRetriever.MAX_MESSAGES + "\r\n" + "<br>" + "Collected data on "
				+ seenThreads + " of a maximum of " + ThreadRetriever.NUM_THREADS_RETRIEVED
				+ " threads" + "<br>" + "Missing data from " + missingMessages + " messages"
				+ "</html>");
	}

	public boolean login() {
		return parent.login();
	}

	public boolean retrieve() {
		return parent.retrieve();
	}
}
