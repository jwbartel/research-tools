package retriever;

public class PrefetchOptions {

	final String[] prefetchedHeaders;
	final boolean prefetchAttachments;

	public PrefetchOptions(String[] headers, boolean prefetchAttachments) {
		this.prefetchedHeaders = headers;
		this.prefetchAttachments = prefetchAttachments;
	}
}
