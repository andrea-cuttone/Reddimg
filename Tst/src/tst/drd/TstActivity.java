package tst.drd;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

enum ScrollingState { NO_SCROLL, SCROLL_LEFT, SCROLL_RIGHT };

public class TstActivity extends Activity {
	
	public static final String APP_NAME = "REDDIMG";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		MyView myView = new MyView(getApplicationContext());
		setContentView(myView);
	}

	public class MyView extends View implements OnTouchListener {
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
		private Bitmap currentImg;

		private LinkRenderer linkRenderer;


		public MyView(Context context) {
			super(context);
			linksQueue = new RedditLinkQueue();
			
			DisplayMetrics displaymetrics = new DisplayMetrics();
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			wm.getDefaultDisplay().getMetrics(displaymetrics);
			screenW = displaymetrics.widthPixels;
			screenH = displaymetrics.heightPixels;
			ImageResizer imgResizer = new ImageResizer(screenW, screenH);
			imageCache = new ImageCache(imgResizer);
			
			yPos = 0;
			scrollingState = ScrollingState.NO_SCROLL;
			
			linkRenderer = new LinkRenderer(screenW, screenH);
			loadImage();			

			setOnTouchListener(this);
		}

		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (event.getX() < screenW * (1. / SCROLL_MARGIN)) {
					scrollingState = ScrollingState.SCROLL_LEFT;
				} else if (event.getX() > screenW * (1 - 1. / SCROLL_MARGIN)) {
					scrollingState = ScrollingState.SCROLL_RIGHT;
				} else {
					startY = event.getY();
					currentY = startY;
				}
			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				if(scrollingState == ScrollingState.NO_SCROLL) {
					currentY = event.getY();
					postInvalidate();
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
				currentY = startY;
				scrollingState = ScrollingState.NO_SCROLL;
				postInvalidate();
			}
			return true;
		}

		private void loadImage() {
			RedditLink link = linksQueue.at(currentLinkIndex);
			Bitmap image = imageCache.getImage(link.getUrl());
			if(currentImg != null) {
				currentImg.recycle();
			}
			currentImg = linkRenderer.render(link, image);
			yPos = 0;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			canvas.drawBitmap(currentImg, 0, yPos + currentY - startY, null);
		}

	}
}