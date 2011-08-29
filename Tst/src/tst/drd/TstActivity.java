package tst.drd;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

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
		private int currentImgIndex = 0;
		private RedditLinkQueue linksQueue;

		private float startX;
		private float currentX;
		private ImageCache imageCache;

		public MyView(Context context) {
			super(context);
			linksQueue = new RedditLinkQueue();
			imageCache = new ImageCache();
			setOnTouchListener(this);
		}

		public boolean onTouch(View v, MotionEvent event) {
			/*if (event.getAction() == MotionEvent.ACTION_DOWN) {
				startX = event.getX();
			}
			if (event.getAction() == MotionEvent.ACTION_MOVE) {
				currentX = event.getX();
				postInvalidate();
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {
				float endX = event.getX();
				if (endX < startX) {
					currentImgIndex++;
				} else {
					currentImgIndex--;
				}
				currentX = startX;
				postInvalidate();
			}*/
			
			if (event.getAction() == MotionEvent.ACTION_UP) {
				currentImgIndex++;
				postInvalidate();
			}
			
			return true;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			Log.d(APP_NAME, "Displaying " + currentImgIndex);
			// Rect clipBounds = canvas.getClipBounds();
			RedditLink center = linksQueue.at(currentImgIndex);
			Bitmap centerImg = imageCache.getImage(center.getUrl());
			Paint textPaint = new Paint();
			textPaint.setColor(Color.RED);
			int titleHeight = 50;
			float delta = currentX - startX;
			if (delta < 0) {
				RedditLink right = linksQueue.at(currentImgIndex + 1);
				Bitmap rightImg = imageCache.getImage(right.getUrl());
				canvas.drawBitmap(centerImg, delta, 0, null);
				canvas.drawText(center.getTitle(), delta, titleHeight, textPaint);
				canvas.drawBitmap(rightImg, centerImg.getWidth() + delta, 0, null);
				canvas.drawText(right.getTitle(), centerImg.getWidth() + delta, titleHeight, textPaint);
			} else {
				RedditLink left = linksQueue.at(currentImgIndex - 1);
				Bitmap leftImg = imageCache.getImage(left.getUrl());
				canvas.drawBitmap(centerImg, delta, 0, null);
				canvas.drawText(center.getTitle(), delta, titleHeight, textPaint);
				canvas.drawBitmap(leftImg, -leftImg.getWidth() + delta, 0, null);
				canvas.drawText(left.getTitle(), -leftImg.getWidth() + delta, titleHeight, textPaint);
			}
		}

	}
}