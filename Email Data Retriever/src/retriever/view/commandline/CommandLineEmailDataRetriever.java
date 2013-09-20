package retriever.view.commandline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import javax.mail.MessagingException;

import retriever.imap.ImapAuthenticator;

/**
 * Tool to collect data from messages from the command line
 * 
 * @author bartel
 * 
 */
public class CommandLineEmailDataRetriever {

	public static void main(String[] args) {

		ArrayList<String> argsList = new ArrayList<String>(Arrays.asList(args));
		Map<String, String> flags = extractFlags(argsList);

		String imap = flags.get("imap");
		String email = flags.get("email");
		String password = flags.get("password");
		ImapAuthenticator authenticator = new ImapAuthenticator();
		try {
			authenticator.login(imap, email, password);
		} catch (MessagingException e) {
			System.out.print("Login Failed: " + e.getMessage());
			System.out.print("<br>");
			System.out.print("<a href='javascript:hideLoading()'>Try Again</a>");
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
