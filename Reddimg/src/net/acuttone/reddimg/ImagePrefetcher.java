package net.acuttone.reddimg;

enum ImagePrefetcherStatus { RUNNING, PAUSED, TERMINATED };

public class ImagePrefetcher extends Thread {

	private ImageCache imageCache;
	private RedditLinkQueue linkQueue;
	private ImagePrefetcherStatus status;

	public ImagePrefetcher(ImageCache imageCache, RedditLinkQueue linkQueue) {
		this.imageCache = imageCache;
		this.linkQueue = linkQueue;
		status = ImagePrefetcherStatus.PAUSED;
	}

	@Override
	public void run() {
		boolean terminationRequested = false;
		while (terminationRequested == false) {
			if (ImagePrefetcherStatus.PAUSED != getStatus()) {
				String targetUrl = "";
				//int lastRequestedIndex = linkQueue.getLastRequestedIndex();
				/*for (int i = lastRequestedIndex; i < lastRequestedIndex + ImageCache.IN_MEM_CACHE_SIZE; i++) {
					RedditLink link = linkQueue.getForPrefetch(i);					
					if (link != null && imageCache.getFromMem(link.getUrl()) == null) {
						targetUrl = link.getUrl();
						break;
					}
				}*/

				if (targetUrl.length() > 0) {
					/*boolean success = imageCache.prepareImage(targetUrl);
					if (success == false) {
						//linkQueue.removeUrl(targetUrl);
					}*/
				}
			}
			
			if(ImagePrefetcherStatus.TERMINATED == getStatus()) {
				terminationRequested = true;
			}

			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {

			}
		}
	}

	public synchronized ImagePrefetcherStatus getStatus() {
		return status;
	}

	public synchronized void setStatus(ImagePrefetcherStatus status) {
		this.status = status;
	}

}
