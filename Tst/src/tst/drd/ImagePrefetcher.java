package tst.drd;


public class ImagePrefetcher extends Thread {

	private ImageCache imageCache;
	private RedditLinkQueue linkQueue;

	public ImagePrefetcher(ImageCache imageCache, RedditLinkQueue linkQueue) {
		this.imageCache = imageCache;
		this.linkQueue = linkQueue;
	}

	@Override
	public void run() {
		while (true) {
			String targetUrl = "";
			synchronized (linkQueue) {
				int lastRequestedIndex = linkQueue.getLastRequestedIndex();
				for(int i = lastRequestedIndex; i < lastRequestedIndex + ImageCache.IN_MEM_CACHE_SIZE; i++) {
					RedditLink link = linkQueue.getForPrefetch(i);
					if(imageCache.getFromMem(link.getUrl()) == null) {
						targetUrl = link.getUrl();
						break;
					}
				}
			}
			if(targetUrl.length() > 0) {
				boolean success = imageCache.prepareImage(targetUrl);
				if(success == false) {
					synchronized (linkQueue) {
						linkQueue.removeUrl(targetUrl);
					}
				}
			}
			
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {

			}
		}
	}

}
