package net.acuttone.reddimg;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class RedditApplication extends Application {

    private static RedditApplication instance;
	
	private RedditLinkQueue linksQueue;
	private ImageCache imageCache;
	private int screenW;
	private int screenH;
	private ImagePrefetcher imagePrefetcher;

    public static RedditApplication instance() {
      return instance;
    }

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		linksQueue = new RedditLinkQueue();
		loadScreenSize();
		ImageResizer imgResizer = new ImageResizer();
		imageCache = new ImageCache(imgResizer, this);
		imagePrefetcher = new ImagePrefetcher(imageCache, linksQueue);
		imagePrefetcher.start();
	}

	public void loadScreenSize() {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(displaymetrics);
		screenW = displaymetrics.widthPixels;
		screenH = displaymetrics.heightPixels;
	}

	public RedditLinkQueue getLinksQueue() {
		return linksQueue;
	}

	public ImageCache getImageCache() {
		return imageCache;
	}

	public int getScreenW() {
		return screenW;
	}

	public int getScreenH() {
		return screenH;
	}

	public ImagePrefetcher getImagePrefetcher() {
		return imagePrefetcher;
	}
    
}