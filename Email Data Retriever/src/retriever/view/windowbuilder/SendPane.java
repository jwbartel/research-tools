package retriever.view.windowbuilder;

import java.awt.BorderLayout;

import javax.mail.MessagingException;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;

import retriever.ThreadData;

public class SendPane extends JPanel {

	EmailDataRetriever parent;

	String addressToSendTo;

	JTextField emailAddress;
	JCheckBox includeSubjects;
	JCheckBox includeFullEmailAddresses;
	JTextArea textToSend;
	JButton sendButton;

	ThreadData data;

	ChangeSendDataListener changeData = new ChangeSendDataListener(this);

	/**
	 * Create the panel.
	 */
	public SendPane(EmailDataRetriever parent, String addressToSendTo) {
		this.parent = parent;
		this.addressToSendTo = addressToSendTo;

		setLayout(new BorderLayout(0, 10));

		JLabel sentDescription = new JLabel("<html>The following data will be sent to "
				+ addressToSendTo + " through the email address that you provided<html>");
		sentDescription.setHorizontalAlignment(SwingConstants.LEFT);
		add(sentDescription, BorderLayout.NORTH);

		JPanel dataPanel = new JPanel();
		add(dataPanel, BorderLayout.CENTER);

		JLabel emailAddressLabel = new JLabel("From");

		emailAddress = new JTextField("test@example.com");
		emailAddress.setEditable(false);

		includeSubjects = new JCheckBox("Include subjects");
		includeSubjects.addActionListener(changeData);
		includeFullEmailAddresses = new JCheckBox("Include full email addresses");
		includeFullEmailAddresses.addActionListener(changeData);

		JLabel lblSentData = new JLabel("Data that will be sent");

		textToSend = new JTextArea();
		textToSend.setEditable(true);
		JScrollPane textAreaPane = new JScrollPane(textToSend);

		sendButton = new JButton("Send via the provided email address");
		sendButton.addActionListener(new SubmitListener(this));

		GroupLayout gl_dataPanel = new GroupLayout(dataPanel);
		gl_dataPanel
				.setHorizontalGroup(gl_dataPanel
						.createParallelGroup(Alignment.TRAILING)
						.addGroup(
								gl_dataPanel
										.createSequentialGroup()
										.addGroup(
												gl_dataPanel
														.createParallelGroup(Alignment.TRAILING)
														.addGroup(
																Alignment.LEADING,
																gl_dataPanel
																		.createSequentialGroup()
																		.addContainerGap()
																		.addComponent(
																				textAreaPane,
																				GroupLayout.DEFAULT_SIZE,
																				255,
																				Short.MAX_VALUE))
														.addComponent(includeFullEmailAddresses,
																Alignment.LEADING,
																GroupLayout.DEFAULT_SIZE, 265,
																Short.MAX_VALUE)
														.addComponent(includeSubjects,
																Alignment.LEADING,
																GroupLayout.DEFAULT_SIZE, 265,
																Short.MAX_VALUE)
														.addGroup(
																gl_dataPanel
																		.createSequentialGroup()
																		.addComponent(
																				emailAddressLabel)
																		.addPreferredGap(
																				ComponentPlacement.UNRELATED)
																		.addComponent(
																				emailAddress,
																				GroupLayout.DEFAULT_SIZE,
																				231,
																				Short.MAX_VALUE))
														.addComponent(lblSentData,
																Alignment.LEADING)
														.addGroup(
																gl_dataPanel
																		.createSequentialGroup()
																		.addContainerGap(176,
																				Short.MAX_VALUE)
																		.addComponent(sendButton)))
										.addContainerGap()));
		gl_dataPanel.setVerticalGroup(gl_dataPanel.createParallelGroup(Alignment.LEADING).addGroup(
				gl_dataPanel
						.createSequentialGroup()
						.addGap(1)
						.addGroup(
								gl_dataPanel
										.createParallelGroup(Alignment.LEADING)
										.addComponent(emailAddressLabel)
										.addComponent(emailAddress, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(includeSubjects)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(includeFullEmailAddresses)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(lblSentData)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(textAreaPane, GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(sendButton)));
		dataPanel.setLayout(gl_dataPanel);

	}

	public void setEmailAddress(String emailAddress) throws MessagingException {
		this.emailAddress.setText(emailAddress);
		sendButton.setText("Send via the provided email address (" + emailAddress + ")");
		updateDataToSend();
	}

	public void setThreadData(ThreadData data) throws MessagingException {
		this.data = data;
		updateDataToSend();
	}

	public void updateDataToSend() throws MessagingException {
		if (data != null) {
			textToSend.setText(data.getDataString(emailAddress.getText(),
					includeSubjects.isSelected(), includeFullEmailAddresses.isSelected()));
		} else {
			textToSend.setText("");
		}
	}

	public String getDataToSend() throws MessagingException {
		return textToSend.getText();
	}

	public void sendData() {
		try {
			sendButton.setEnabled(false);
			parent.packageAndSendData(getDataToSend());
			System.exit(0);
		} catch (MessagingException e) {
			e.printStackTrace();
			sendButton.setEnabled(false);
		}
	}
}
