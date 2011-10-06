package net.acuttone.reddimg;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class LoadingActivity extends Activity {

	private int currentIndex;
	private Bitmap oldBmp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentIndex = getIntent().getExtras().getInt(MainActivity.CURRENT_INDEX);
		RedditLink currentLink = RedditApplication.instance().getLinksQueue().get(currentIndex);
		if(currentLink != null) {
			Bitmap image = RedditApplication.instance().getImageCache().getFromMem(currentLink.getUrl());
			if (image != null) {
				setResult(RESULT_OK, getIntent());
				finish();
				return;
			}
		}
		
		oldBmp = getIntent().getExtras().getParcelable(MainActivity.CURRENT_BMP);		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		LoadingView view = new LoadingView(getApplicationContext());
		setContentView(view);
	}
	
	class LoadingView extends View {

		private static final int STATUS_TEXT_BOUNDS_MARGIN = 15;
		private Paint paint;
		private Rect statusRect;

		public LoadingView(Context context) {
			super(context);
			paint = new Paint();
			paint.setTextSize(14.0f);
			paint.setAntiAlias(true);
			statusRect = new Rect(20, RedditApplication.instance().getScreenH() / 3,
					RedditApplication.instance().getScreenW() - 20, -1); 
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			if(oldBmp != null) {
				canvas.drawBitmap(oldBmp, 0, 0, paint); 
			}
			RedditLink currentLink = RedditApplication.instance().getLinksQueue().get(currentIndex);
			if(currentLink != null) {
				String text = "loading " + currentLink.getUrl();
				drawStatus(canvas, text);				
				Bitmap image = RedditApplication.instance().getImageCache().getFromMem(currentLink.getUrl());
				if (image != null) {
					setResult(RESULT_OK, getIntent());
					finish();
				}
			} else {
				String text = "fetching new links...";
				drawStatus(canvas, text);				
			}

			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				
			}
			invalidate();
		}

		private void drawStatus(Canvas canvas, String text) {
			List<String> wrappedLines = TextWrapper.getWrappedLines(text, statusRect.width(), paint);
			Rect bounds = TextWrapper.getMultilineBounds(wrappedLines, statusRect.left, statusRect.top, paint);
			bounds.left = 0;
			bounds.right = RedditApplication.instance().getScreenW();
			bounds.top -= STATUS_TEXT_BOUNDS_MARGIN;
			bounds.bottom += STATUS_TEXT_BOUNDS_MARGIN;
			paint.setColor(Color.BLACK);
			canvas.drawRect(bounds, paint);
			paint.setColor(Color.WHITE);
			TextWrapper.drawTextLines(canvas, wrappedLines, statusRect.left, statusRect.top, paint);
		}
	}
	
}