package net.acuttone.reddimg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

enum ScrollingState { NO_SCROLL, SCROLL_LEFT, SCROLL_RIGHT };

// TODO: NEED TO CLEANUP BITMAPS AND THREADS ON STATE CHANGE!!!
public class MainActivity extends Activity implements OnTouchListener {
	
	public static String APP_NAME = "REDDIMG";

	private static final double SCROLL_MARGIN = 5.;

	private RedditLinkQueue linksQueue;
	private ImageCache imageCache;
	private int screenW;
	private int screenH;

	private int currentLinkIndex = 0;
	private float startY;
	private float currentY;
	private float yPos;		
	private ScrollingState scrollingState;
	private Bitmap viewBitmap;

	private LinkRenderer linkRenderer;

	private ImagePrefetcher imagePrefetcher;

	private SlideshowView view;
	
	//private Bitmap brokenImg;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		linksQueue = new RedditLinkQueue();
		
		DisplayMetrics displaymetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(displaymetrics);
		screenW = displaymetrics.widthPixels;
		screenH = displaymetrics.heightPixels;
		ImageResizer imgResizer = new ImageResizer(screenW, screenH);
		imageCache = new ImageCache(imgResizer);
		
		yPos = 0;
		scrollingState = ScrollingState.NO_SCROLL;
		
		linkRenderer = new LinkRenderer(screenW, screenH);
		
		imagePrefetcher = new ImagePrefetcher(imageCache, linksQueue);
		imagePrefetcher.start();

		loadImage();			

		//brokenImg = Bitmap.createBitmap( 100, 100, Bitmap.Config.ARGB_8888);
		
		view = new SlideshowView(getApplicationContext());
		setContentView(view);
		view.setOnTouchListener(this);
	}
	
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (event.getX() < screenW * (1. / SCROLL_MARGIN)) {
				scrollingState = ScrollingState.SCROLL_LEFT;
			} else if (event.getX() > screenW * (1 - 1. / SCROLL_MARGIN)) {
				scrollingState = ScrollingState.SCROLL_RIGHT;
			} else {
				startY = event.getY();
			}
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if(scrollingState == ScrollingState.NO_SCROLL) {
				currentY = event.getY();
				v.postInvalidate();
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (scrollingState == ScrollingState.SCROLL_LEFT && currentLinkIndex > 0) {
				currentLinkIndex--;
				loadImage();
			} else if (scrollingState == ScrollingState.SCROLL_RIGHT) {
				currentLinkIndex++;
				loadImage();
			} else {
				yPos = yPos + currentY - startY;
			}
			currentY = startY = 0;
			scrollingState = ScrollingState.NO_SCROLL;
			v.postInvalidate();
		}
		return true;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		view.postInvalidate();
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	private void loadImage() {
		if (viewBitmap != null) {
			viewBitmap.recycle();
		}
		
		viewBitmap = null;
		yPos = 0;
		
		Intent i = new Intent(this, LoadingActivity.class);
		RedditApplication.getInstance().getMap().put("LINK_QUEUE", linksQueue);
		RedditApplication.getInstance().getMap().put("IMAGE_CACHE", imageCache);
		i.putExtra("CURRENT_INDEX", currentLinkIndex);
		startActivity(i);
		/*RedditLink currentLink = null;
		Bitmap image = null;
		while (image == null) {
			synchronized (linksQueue) {
				currentLink = linksQueue.get(currentLinkIndex);
			}
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {

			}
			image = imageCache.getFromMem(currentLink.getUrl());
		}

		Log.d(MainActivity.APP_NAME, currentLink.getUrl() + " found in mem cache");

		if (viewBitmap != null) {
			viewBitmap.recycle();
		}
		
		viewBitmap = linkRenderer.render(currentLink, image);
		yPos = 0;*/
	}

	class SlideshowView extends View {

		public SlideshowView(Context context) {
			super(context);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			viewBitmap = (Bitmap) RedditApplication.getInstance().getMap().get("IMAGE");
			if(viewBitmap == null || viewBitmap.isRecycled()) {
				return;
			}
			float actualY = yPos + currentY - startY;
			if(actualY < -viewBitmap.getHeight() + screenH) {
				actualY = -viewBitmap.getHeight() + screenH;
			} 
			if(actualY > 0) {
				actualY = 0;
			}
			canvas.drawBitmap(viewBitmap, 0, actualY, null);
		}

	}
}