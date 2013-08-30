package retriever;

public interface MessageListener {

	public void logMessage(String message);
	public void updateRetrievedMessageCounts(int latestRetrieved);
}
