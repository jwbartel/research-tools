package retriever.view.commandline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import retriever.MessageListener;
import retriever.ThreadData;
import retriever.imap.ImapAuthenticator;
import retriever.imap.ImapThreadRetriever;

/**
 * Tool to collect data from messages from the command line
 * 
 * @author bartel
 * 
 */
public class CommandLineEmailDataRetriever implements MessageListener {

	final static File OUT_FOLDER = new File(
			"/afs/cs.unc.edu/home/bartel/public_html/email_threads/");

	BufferedWriter log;

	private static File getSubOutFolder(String folderName) {
		File retVal = new File(OUT_FOLDER, folderName);
		if (!retVal.exists()) {
			retVal.mkdirs();
		}
		return retVal;
	}

	public void run(ArrayList<String> args, Map<String, String> flags) throws IOException {

		String id = flags.get("id");
		String imap = flags.get("imap");
		String email = URLDecoder.decode(flags.get("email"), "UTF-8");
		String password = URLDecoder.decode(flags.get("password"), "UTF-8");

		if (!email.contains("@") && imap.equals("imap.gmail.com")) {
			email += "@gmail.com";
		}

		File logFile = new File(getSubOutFolder("logs"), id + ".txt");
		log = new BufferedWriter(new FileWriter(logFile, true));

		ImapAuthenticator authenticator = new ImapAuthenticator();
		authenticator.addMessageListener(this);
		try {
			authenticator.login(imap, email, password);
		} catch (MessagingException e) {
			logMessage("Failed logged in");
			logMessage(getStackTrace(e));
			System.out.print("Login Failed: " + e.getMessage());
			System.out.print("<br>");
			System.out.print("<a href='javascript:hideLoading()'>Try Again</a>");
			log.flush();
			log.close();
			return;
		}
		logMessage("Login successful");
		if (args.contains("-onlyCheckLogin")) {
			System.out.println("Login successful");
			log.flush();
			log.close();
			return;
		}

		int messages = Integer.parseInt(flags.get("messages"));
		int threads = Integer.parseInt(flags.get("threads"));
		ImapThreadRetriever retriever = new ImapThreadRetriever(imap, authenticator.getStore());
		retriever.addMessageListener(this);
		try {
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			ThreadData data = retriever.retrieveThreads(messages, threads);
			boolean includeSubjects = Boolean.parseBoolean(flags.get("subjects"));
			boolean includeFullEmailAddresses = Boolean.parseBoolean(flags.get("addresses"));
			boolean includeAttachments = Boolean.parseBoolean(flags.get("numAttach"));
			boolean includeAttachedFileNames = Boolean.parseBoolean(flags.get("fileNames"));
			Map<String, String> compartmentalizedData = data.getCompartmentalizedData(email,
					includeSubjects, includeFullEmailAddresses, includeAttachments,
					includeAttachedFileNames);

			File privateFolder = new File(getSubOutFolder("private_data"), id);
			if (!privateFolder.exists()) {
				privateFolder.mkdirs();
			}

			File anonymousFolder = new File(getSubOutFolder("anonymous_data"), id);
			if (!anonymousFolder.exists()) {
				anonymousFolder.mkdirs();
			}
			writeIfNotNull(compartmentalizedData.get("summary"), new File(privateFolder,
					"summary.txt"));
			writeIfNotNull(compartmentalizedData.get("addresses"), new File(privateFolder,
					"addresses.txt"));
			writeIfNotNull(compartmentalizedData.get("messages"), new File(anonymousFolder,
					"messages.txt"));
			writeIfNotNull(compartmentalizedData.get("subjects"), new File(anonymousFolder,
					"subjects.txt"));
			writeIfNotNull(compartmentalizedData.get("attachments"), new File(anonymousFolder,
					"attachments.txt"));

		} catch (Exception e) {
			logMessage("Failure retrieving and saving threads: " + e.getMessage());
			e.printStackTrace();
		}

		try {
			logMessage("Sending notification of completion");
			sendResponse(email, id);
		} catch (MessagingException e) {
			logMessage(getStackTrace(e));
		}

		log.flush();
		log.close();
	}

	private synchronized void sendResponse(String email, String id) throws MessagingException,
			IOException {

		Map<String, String> credentials = loadSenderCredentials(new File("credentials"));

		// Get system properties
		Properties props = System.getProperties();

		// Setup mail server
		props.put("mail.smtp.host", credentials.get("host"));
		props.put("mail.from", credentials.get("from email"));
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.port", 587);

		// Get session
		Session session = Session.getInstance(props, null);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(credentials.get("from email")));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
		message.setSubject("Email Thread Data");
		// create the message part
		String reviewAddress = "https://wwwx.cs.unc.edu/~bartel/cgi-bin/emailsampler/php/review.php?r="
				+ id;

		// fill message
		String bodyHtml = "Your email data has been collected.  Thank you for your contribution.  <a href='"
				+ reviewAddress + "'>You can review the collected data here.</a>";
		message.setDataHandler(new DataHandler(new ByteArrayDataSource(bodyHtml, "text/html")));

		// Send the message
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		Transport transport = session.getTransport("smtp");
		transport.connect(credentials.get("username"), credentials.get("password"));
		transport.sendMessage(message, message.getAllRecipients());
		transport.close();
	}

	private void writeIfNotNull(String content, File dest) throws IOException {
		if (content != null) {
			BufferedWriter out = new BufferedWriter(new FileWriter(dest));
			out.write(content);
			out.flush();
			out.close();
		}
	}

	private Map<String, String> loadSenderCredentials(File credentialsFile) throws IOException {
		Map<String, String> loadedCredentials = new HashMap<String, String>();
		BufferedReader in = new BufferedReader(new FileReader(credentialsFile));
		String line = in.readLine();
		while (line != null) {

			if (line.startsWith("host:")) {
				loadedCredentials.put("host", line.substring(line.indexOf(':') + 1));
			} else if (line.startsWith("username:")) {
				loadedCredentials.put("username", line.substring(line.indexOf(':') + 1));
			} else if (line.startsWith("from email:")) {
				loadedCredentials.put("from email", line.substring(line.indexOf(':') + 1));
			} else if (line.startsWith("password:")) {
				loadedCredentials.put("password", line.substring(line.indexOf(':') + 1));
			}

			line = in.readLine();
		}
		return loadedCredentials;
	}

	@Override
	public void logMessage(String message) {
		System.out.println(message);
		try {
			log.write("[" + new Date().toString() + "]" + message);
			log.newLine();
			log.flush();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.print("<br>Exception: " + getStackTrace(e));
		}
	}

	@Override
	public void updateRetrievedMessageCounts(int latestRetrieved, int seenThreads,
			int missingMessages) {
		String message = "Retrieved message " + latestRetrieved + ", seen " + seenThreads
				+ " threads" + ", missing " + missingMessages + " messages";
		logMessage(message);
	}

	public static void main(String[] args) {

		ArrayList<String> argsList = new ArrayList<String>(Arrays.asList(args));
		Map<String, String> flags = extractFlags(argsList);

		try {
			new CommandLineEmailDataRetriever().run(argsList, flags);
		} catch (IOException e) {
			System.out.print("Error: " + e.getMessage());
		}

	}

	public static Map<String, String> extractFlags(ArrayList<String> args) {
		Map<String, String> flags = new TreeMap<String, String>();
		for (int i = 0; i < args.size(); i++) {
			String arg = args.get(i);
			if (arg.startsWith("-")) {
				if (i < args.size() - 1 && !args.get(i + 1).startsWith("-")) {
					flags.put(args.get(i).substring(1), args.get(i + 1));
					args.remove(i);
					args.remove(i);
					i--;
				}
			}
		}
		return flags;
	}

	private String getStackTrace(Throwable e) {
		StackTraceElement[] stack = e.getStackTrace();
		String stacktrace = e.getClass().toString();
		if (e.getMessage() != null) {
			stacktrace += ": " + e.getMessage();
		}
		for (StackTraceElement s : stack) {
			stacktrace += "\n\tat " + s.toString();
		}

		if (e.getCause() != null && e.getCause() != e) {
			stacktrace += "\n\ncaused by ";
			stacktrace += getStackTrace(e.getCause());
		}
		return stacktrace;
	}
}
