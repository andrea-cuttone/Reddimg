package net.acuttone.reddimg;

import java.util.HashMap;

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
	private LinkRenderer linkRenderer;
	private ImagePrefetcher imagePrefetcher;
	

    public static RedditApplication getInstance() {
      return instance;
    }

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;

		linksQueue = new RedditLinkQueue();

		DisplayMetrics displaymetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(displaymetrics);
		screenW = displaymetrics.widthPixels;
		screenH = displaymetrics.heightPixels;
		ImageResizer imgResizer = new ImageResizer(screenW, screenH);
		imageCache = new ImageCache(imgResizer);

		linkRenderer = new LinkRenderer(screenW, screenH);

		imagePrefetcher = new ImagePrefetcher(imageCache, linksQueue);
		imagePrefetcher.start();
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

	public LinkRenderer getLinkRenderer() {
		return linkRenderer;
	}

	public ImagePrefetcher getImagePrefetcher() {
		return imagePrefetcher;
	}
    
}