package tst.drd;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

enum ScrollingState { NO_SCROLL, SCROLL_X, SCROLL_Y };

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
		private static final int MIN_DELTAX = 20;
		private static final float MIN_DELTAY = 20;
		private int currentImgIndex = 0;
		private RedditLinkQueue linksQueue;

		private float startX;
		private float currentX;
		private float startY;
		private float currentY;
		private float yPos;		

		private ScrollingState scrollingState;

		private ImageCache imageCache;
		private Paint textPaint;

		public MyView(Context context) {
			super(context);
			linksQueue = new RedditLinkQueue();
			
			DisplayMetrics displaymetrics = new DisplayMetrics();
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			wm.getDefaultDisplay().getMetrics(displaymetrics);
			ImageResizer imgResizer = new ImageResizer(displaymetrics.widthPixels, displaymetrics.heightPixels);
			
			imageCache = new ImageCache(imgResizer);
			
			setOnTouchListener(this);
			
			textPaint = new Paint();
			textPaint.setColor(Color.RED);
			
			scrollingState = ScrollingState.NO_SCROLL;
			
			yPos = 0;
		}

		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				startX = event.getX();
				startY = event.getY();
				currentX = startX;
				currentY = startY;
				postInvalidate();
				return true;
			}
			
			if (event.getAction() == MotionEvent.ACTION_MOVE) {
				currentX = event.getX();
				currentY = event.getY();
				if (scrollingState == ScrollingState.NO_SCROLL) {
					if (Math.abs(currentX - startX) > MIN_DELTAX) {
						scrollingState = ScrollingState.SCROLL_X;
					} else if (Math.abs(currentY - startY) > MIN_DELTAY) {
						scrollingState = ScrollingState.SCROLL_Y;
					}
				} 
				
				postInvalidate();
				return true;
			}
			
			if (event.getAction() == MotionEvent.ACTION_UP) {
				if (scrollingState == ScrollingState.SCROLL_X) {
					float deltaX = event.getX() - startX;
					if (deltaX < -MIN_DELTAX) {
						currentImgIndex++;
						yPos = 0;
						postInvalidate();
					} else if (deltaX > MIN_DELTAX && currentImgIndex > 0) {
						currentImgIndex--;
						yPos = 0;
					}
				} else if(scrollingState == ScrollingState.SCROLL_Y) {
					yPos = yPos + currentY - startY;
				}
					
				currentX = startX;
				currentY = startY;
				scrollingState = ScrollingState.NO_SCROLL;
				postInvalidate();
				return true;
			}
			
			return true;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			
			Log.d(APP_NAME, "Displaying " + currentImgIndex);
			RedditLink center = linksQueue.at(currentImgIndex);
			Bitmap centerImg = imageCache.getImage(center.getUrl());
			
			final int titleHeight = 15;
			
			if (scrollingState == ScrollingState.SCROLL_Y ||
					scrollingState == ScrollingState.NO_SCROLL) {
				canvas.drawBitmap(centerImg, 0, yPos + currentY - startY, null);
				canvas.drawText(center.getTitle(), 5, titleHeight, textPaint);
				return;
			}
			
			if (scrollingState == ScrollingState.SCROLL_X) {
				float deltaX = currentX - startX;
				if (deltaX <= 0) {
					RedditLink right = linksQueue.at(currentImgIndex + 1);
					Bitmap rightImg = imageCache.getImage(right.getUrl());
					canvas.drawBitmap(centerImg, deltaX, 0, null);
					canvas.drawText(center.getTitle(), deltaX, titleHeight, textPaint);
					canvas.drawBitmap(rightImg, centerImg.getWidth() + deltaX, 0, null);
					canvas.drawText(right.getTitle(), centerImg.getWidth() + deltaX, titleHeight, textPaint);
				} else if (deltaX > 0) {
					RedditLink left = linksQueue.at(currentImgIndex - 1);
					Bitmap leftImg = imageCache.getImage(left.getUrl());
					canvas.drawBitmap(centerImg, deltaX, 0, null);
					canvas.drawText(center.getTitle(), deltaX, titleHeight, textPaint);
					canvas.drawBitmap(leftImg, -leftImg.getWidth() + deltaX, 0, null);
					canvas.drawText(left.getTitle(), -leftImg.getWidth() + deltaX, titleHeight, textPaint);
				}
				return;
			}			
			
		}

	}
}