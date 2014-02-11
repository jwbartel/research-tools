package retriever;

/*
 * ThreadCluster class that determines whether a thread fits into a cluster for surveying
 */
public interface ThreadCluster extends Comparable<ThreadCluster>{

	public boolean matchesCluster(OfflineThread thread);
}
