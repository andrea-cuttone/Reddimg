package net.acuttone.reddimg;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ReddimgApp extends Application {

    private static ReddimgApp instance;
    
    public static String APP_NAME = "REDDIMG";
	
	private RedditLinkQueue linksQueue;
	private ImageCache imageCache;
	private int screenW;
	private int screenH;

	private RedditClient redditClient;

    public static ReddimgApp instance() {
      return instance;
    }

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		redditClient = new RedditClient(getCacheDir());
		linksQueue = new RedditLinkQueue();
		loadScreenSize();
		imageCache = new ImageCache(this);	
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
		loadScreenSize();
		return screenW;
	}

	public int getScreenH() {
		loadScreenSize();
		return screenH;
	}

	public RedditClient getRedditClient() {
		return redditClient;
	}
    
}