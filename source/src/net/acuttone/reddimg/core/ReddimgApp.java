package net.acuttone.reddimg.core;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

public class ReddimgApp extends Application {

    private static ReddimgApp instance;
    
    public static String APP_NAME = "REDDIMG";
    public static final int MEGABYTE = 1000000;
	
	private RedditLinkQueue linksQueue;
	private ImageCache imageCache;
	private int screenW;
	private int screenH;

	private RedditClient redditClient;

	private Timer linksQueueTimer;

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
		linksQueueTimer = null;
	}
	
	public void startLinksQueueTimer() {
		if (linksQueueTimer == null) {
			linksQueueTimer = new Timer();
			linksQueueTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					try {
						linksQueue.getNewLinks();
					} catch (IOException e) {
						Log.e(ReddimgApp.APP_NAME, e.toString());
					}
				}
			}, 0, 2000);
		}
	}
	
	public void stopLinksQueueTimer() {
		if (linksQueueTimer != null) {
			linksQueueTimer.cancel();
			linksQueueTimer = null;
		}
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
	
	public SharedPreferences getPrefs() {
		return PreferenceManager.getDefaultSharedPreferences(this);
	}
    
}