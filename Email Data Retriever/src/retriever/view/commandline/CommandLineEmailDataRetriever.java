package retriever.view.commandline;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.mail.MessagingException;

import retriever.MessageListener;
import retriever.imap.ImapAuthenticator;

/**
 * Tool to collect data from messages from the command line
 * 
 * @author bartel
 * 
 */
public class CommandLineEmailDataRetriever implements MessageListener {

	BufferedWriter log;

	public void run(Map<String, String> flags) throws IOException {

		String id = flags.get("id");
		String imap = flags.get("imap");
		String email = flags.get("email");
		String password = flags.get("password");

		File logFile = new File("/afs/cs.unc.edu/home/bartel/public_html/email threads/logs/"
				+ email + "_" + id + ".txt");
		System.out.print(" " + logFile + " ");
		System.out.print("<br>");
		System.out.print(" " + logFile.getAbsolutePath() + " ");
		log = new BufferedWriter(new FileWriter(logFile, true));

		ImapAuthenticator authenticator = new ImapAuthenticator();
		authenticator.addMessageListener(this);
		try {
			authenticator.login(imap, email, password);
		} catch (MessagingException e) {
			logMessage("Failed logged in");
			System.out.print("Login Failed: " + e.getMessage());
			System.out.print("<br>");
			System.out.print("<a href='javascript:hideLoading()'>Try Again</a>");
			log.flush();
			log.close();
			return;
		}
		logMessage("Login successful");

		log.flush();
		log.close();
	}

	@Override
	public void logMessage(String message) {
		try {
			System.out.print("<br>Logging message:" + message);
			log.write("[" + new Date().toString() + "]" + message);
			log.newLine();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.print("<br>Exception: " + e.getMessage());
		}
	}

	@Override
	public void updateRetrievedMessageCounts(int latestRetrieved, int seenThreads,
			int missingMessages) {
		try {
			log.write("[" + new Date().toString() + "] Retrieved message " + latestRetrieved
					+ ", seen " + seenThreads + " threads" + ", missing " + missingMessages
					+ " messages");
			log.newLine();
		} catch (IOException e) {
		}
	}

	public static void main(String[] args) {

		ArrayList<String> argsList = new ArrayList<String>(Arrays.asList(args));
		Map<String, String> flags = extractFlags(argsList);

		try {
			new CommandLineEmailDataRetriever().run(flags);
		} catch (IOException e) {
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
				} else {
					args.remove(i);
				}
				i--;
			}
		}
		return flags;
	}
}
