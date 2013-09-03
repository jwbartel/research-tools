package retriever.view.windowbuilder;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import retriever.MessageListener;
import retriever.ThreadData;
import retriever.imap.ImapAuthenticator;
import retriever.imap.ImapThreadRetriever;

public class EmailDataRetriever extends JFrame implements MessageListener {
	
	public static final String TEMP_DATA_FILE = "email_thread_data.txt";
	public static final String TO_ADDRESS = "bartel+emaildata@cs.unc.edu";
	
	public static final String INTRODUCTION_MESSAGE =
		"<html>\r\n" +
		"This tool collects anonymized email data for research purposes. " +
		"We are working to study different how people collaborate and communicate through email " +
		"and how we can assist in that process.  " +
		"Thank you for participating!\r\n" +
		"<br><br>\r\n" +
		"After you retrieve your data, " +
		"you will be able to modify and confirm what you send to us to ensure it is properly anonymized.\r\n" +
		"</html>";
	
	ImapAuthenticator authenticator = new ImapAuthenticator();
	ImapThreadRetriever retriever = null;
	
	AuthenticationPane authenticationPane = new AuthenticationPane(this, authenticator);
	SendPane sendPane = new SendPane(this, TO_ADDRESS);
	
	public static final int FIELD_WIDTH = 200;

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					EmailDataRetriever frame = new EmailDataRetriever();
					frame.setVisible(true);
					frame.setAuthenticationFocus();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	protected boolean login(){
		String imapServer = authenticationPane.getIMAPServer();
		String emailAddress = authenticationPane.getEmailAddress();
		String password = authenticationPane.getPassword();
		try {
			authenticator = new ImapAuthenticator();
			authenticator.addMessageListener(this);
			authenticator.login(imapServer, emailAddress, password);
			sendPane.setEmailAddress(emailAddress);
			return true;
		} catch (MessagingException e) {
			logMessage("ERROR:"+e.getMessage());
			return false;
		}
	}
	
	protected boolean retrieve(){

		String imapServer = authenticationPane.getIMAPServer();
		try{
			retriever = new ImapThreadRetriever(imapServer, authenticator.getStore());
			retriever.addMessageListener(this);
			ThreadData data = retriever.retrieveThreads();
			logMessage("Generating data points");
			sendPane.setThreadData(data);
			contentPane.remove(authenticationPane);
			contentPane.add(sendPane, BorderLayout.CENTER);
			contentPane.validate();
			return true;
		} catch (MessagingException e) {
			e.printStackTrace();
			logMessage("ERROR:"+e.getMessage());
			return false;
		}
	}

	/**
	 * Create the frame.
	 */
	public EmailDataRetriever() {
		setTitle("Email Data Point Retriever");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 406, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 5));
		
		JLabel lblNewLabel = new JLabel(INTRODUCTION_MESSAGE);
		contentPane.add(lblNewLabel, BorderLayout.NORTH);
		
		JPanel dataPane = authenticationPane;
		contentPane.add(dataPane, BorderLayout.CENTER);
	}
	
	public void setAuthenticationFocus(){
		authenticationPane.defaultFocus();
	}
	
	public synchronized void packageAndSendData(String data){
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(TEMP_DATA_FILE));
			out.write(data);
			out.flush();
			out.close();
			
			if(! authenticator.isLoggedIn()) {
				login();
			}
			
			sendData(authenticationPane.getSMTPServer(), authenticationPane.getEmailAddress(), authenticationPane.getPassword(), TO_ADDRESS);
			
			File tempFile = new File(TEMP_DATA_FILE);
			tempFile.delete();
			
			JOptionPane.showMessageDialog(null, "Thank you for contributing.  Your data has now been sent to our researchers.");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendData(String host, String from, String password, String to){
		try {
			// Get system properties
			Properties props = System.getProperties();

			// Setup mail server
			props.put("mail.smtp.host", host);
	        props.put("mail.from", from);
	        props.put("mail.smtp.starttls.enable", "true");
	        props.put("mail.smtp.port", 587);

			// Get session
			Session session = Session.getInstance(props, null);
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject("Email Thread Data");

			// create the message part 
			MimeBodyPart messageBodyPart = new MimeBodyPart();
		    
			//fill message
			messageBodyPart.setText("Attached is the data collected on my past email threads");
			
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			
			// Part two is attachment
			messageBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource(TEMP_DATA_FILE);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(TEMP_DATA_FILE);
			multipart.addBodyPart(messageBodyPart);

			// Put parts in message
		    message.setContent(multipart);

		    // Send the message
		    Transport transport = session.getTransport("smtp");
		    transport.connect(from, password);
		    transport.sendMessage(message, message.getAllRecipients());
		    transport.close();
		    
			
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void logMessage(String message) {
		authenticationPane.logMessage(message);
	}
	
	public void updateRetrievedMessageCounts(int latestRetrieved, int seenThreads, int missingMessages){
		authenticationPane.updateRetrievedMessageCounts(latestRetrieved, seenThreads, missingMessages);
	}
}
