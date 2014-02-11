package retriever;

import javax.mail.MessagingException;

public class ResponseTimeThreadCluster implements ThreadCluster{
	private final Long threshold;
	
	public ResponseTimeThreadCluster(long threshold) {
		this.threshold= threshold;
	}

	@Override
	public boolean matchesCluster(OfflineThread thread) {
		try {
			Long responseTime = thread.getResponseTime();
			if (responseTime == null) {
				return false;
			} else {
				return responseTime <= threshold;
			}
		} catch (MessagingException e) {
			e.printStackTrace();
			return false;
		}
		
	}

	@Override
	public int compareTo(ThreadCluster o) {
		if (o instanceof ResponseTimeThreadCluster) {
			return this.threshold.compareTo(((ResponseTimeThreadCluster) o).threshold);
		} else {
			return -1 * o.compareTo(this);
		}
	}
	
	@Override
	public String toString() {
		return "ResponsetimeThreadCluster: " + threshold + "ms";
	}
	
}
