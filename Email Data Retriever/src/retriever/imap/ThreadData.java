package retriever.imap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;

public class ThreadData {

	ArrayList<Set<Message>> threads;
	Set<String> seenMessages;
	
	Map<String,Integer> addressIDs;
	
	public ThreadData(ArrayList<Set<Message>> threads, Set<String> seenMessages){
		
		this.threads = threads;
		this.seenMessages = seenMessages;
	}
	
	private void buildAddressIDs() throws MessagingException{
		
		addressIDs = new HashMap<String, Integer>();
		
		for(Set<Message> thread: threads){
			
			for(Message message: thread){
				message.getSubject();
				message.getReceivedDate();
				Address[] from = message.getFrom();
				if(from != null) {
					for(Address address: from){
						assignAddressID(address);
					}				
				}
				Address[] recipients = message.getAllRecipients();
				if(recipients != null){
					for(Address address: recipients){
						assignAddressID(address);
					}
				}
			}
		}
	}
	
	private void assignAddressID(Address address){
		String cleanedAddress = address.toString().toLowerCase();
		if(cleanedAddress.contains("<") && cleanedAddress.contains(">")){
			cleanedAddress = cleanedAddress.substring(cleanedAddress.lastIndexOf('<')+1, cleanedAddress.lastIndexOf('>'));
		}
		if(!addressIDs.containsKey(cleanedAddress)){
			addressIDs.put(cleanedAddress, addressIDs.size()+1);
		}
	}
	
	public String getDataString(String sourceEmail, boolean includeSubjects, boolean includeFullEmailAddresses) throws MessagingException{
		
		if(addressIDs == null){
			buildAddressIDs();
		}
		
		int totalMessagesInThreads = 0;
		String messageVectors = "";
		int threadID = 1;
		int messageID = 1;
		for(Set<Message> thread: threads){
			totalMessagesInThreads += thread.size();
			
			for(Message message: thread){
				
				String vector = "Message:"+messageID;
				vector += " Thread:"+threadID;
				vector += " Received-Date:"+message.getReceivedDate();				
				String fromStr = "";
				Address[] from = message.getFrom();
				if(from != null) for(Address addressObj: from){
					if(fromStr.length() > 1){
						fromStr += ",";
					}
					fromStr += getPublicAddressRepresentation(addressObj, includeFullEmailAddresses);
				}
				vector += " From:["+fromStr+"]";
				
				message.getFrom();
				String recipientStr = "";				
				Address[] recipients = message.getAllRecipients();
				if(recipients != null) for(Address addressObj: recipients){
					if(recipientStr.length() > 1){
						recipientStr += ",";
					}
					recipientStr += getPublicAddressRepresentation(addressObj, includeFullEmailAddresses);
				}
				vector += " Recipients:["+recipientStr+"]";
				
				if(includeSubjects){
					vector += " Subject:"+message.getSubject();
				}
				
				messageVectors += vector + "\n";
				messageID++;
			}
						
			threadID++;
		}
		
		
		String toShare = "Source email:"+sourceEmail+"\n";
		toShare += "Total threads retrieved:"+threads.size()+"\n";
		toShare += "Total messages checked:"+seenMessages.size()+"\n";
		toShare += "Total messages in retrieved threads:" + totalMessagesInThreads + "\n";
		toShare += "Includes subjects:"+includeSubjects +", Includes email addresses:"+includeFullEmailAddresses + "\n";
		
		toShare += "\n";
		toShare += messageVectors;
		
		return toShare;
		
	}
	
	private String getPublicAddressRepresentation(Address addressObj, boolean includeFullEmailAddresses){
		String address = addressObj.toString();
		if(includeFullEmailAddresses){
			return address;
		}else{
			String cleanedAddress = address.toLowerCase();
			if(cleanedAddress.contains("<") && cleanedAddress.contains(">")){
				cleanedAddress = cleanedAddress.substring(cleanedAddress.lastIndexOf('<')+1, cleanedAddress.lastIndexOf('>'));
			}
			return ""+addressIDs.get(cleanedAddress);
		}
	}
}
