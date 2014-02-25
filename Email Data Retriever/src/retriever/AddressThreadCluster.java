package retriever;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.MessagingException;

public class AddressThreadCluster implements ThreadCluster {
	private final String regex;
	private final String ownerEmailRegex;
	
	public AddressThreadCluster(String regex, String ownerEmail) {
		this.regex = regex;
		this.ownerEmailRegex = ".*"+ownerEmail.replace(".", "[.]") + "(>)?\\s*";
	}
	
	
	@Override
	public int compareTo(ThreadCluster o) {
		if (o instanceof AddressThreadCluster) {
			return regex.compareTo(((AddressThreadCluster) o).regex);
		} else if (o instanceof ResponseTimeThreadCluster) {
			return -1;
		} else {
			return -1 * o.compareTo(this);
		}
	}

	@Override
	public boolean matchesCluster(OfflineThread thread) {
		
		try {
			OfflineMessage[] originalAndResponse = thread.getOriginalAndResponse();
			OfflineMessage original = originalAndResponse[0];
			if (original == null) {
				return false;
			}
			Set<Address> addresses = new HashSet<Address>();
			if (original.getAllRecipients() != null) {
				addresses.addAll(Arrays.asList(original.getAllRecipients()));
			}
			if (original.getFrom() != null) {
				addresses.addAll(Arrays.asList(original.getFrom()));
			}
			for (Address address : addresses) {
				String addressStr = address.toString().toLowerCase();
				if (!Pattern.matches(ownerEmailRegex, addressStr) &&
						Pattern.matches(regex, addressStr)) {
					return true;
				}
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return false;
	}

}
