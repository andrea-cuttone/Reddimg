package tst.drd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
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
		private Paint textPaint;
		private int screenW;
		private int screenH;

		private int currentLinkIndex = 0;
		private float startY;
		private float currentY;
		private float yPos;		
		private ScrollingState scrollingState;
		private Bitmap currentImg;


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
					
			textPaint = new Paint();
			textPaint.setColor(Color.WHITE);
			textPaint.setTextSize(14.0f);
			textPaint.setAntiAlias(true);
			
			setOnTouchListener(this);
			
			loadImage();
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
			final int TITLE_SIDE_MARGIN = 5;
			final int TITLE_TOP = 15;
			final int TEXT_HEIGHT = 15;

			Log.d(APP_NAME, "Preparing image #" + currentLinkIndex);
			
			RedditLink link = linksQueue.at(currentLinkIndex);
			Bitmap image = imageCache.getImage(link.getUrl());
			if(currentImg != null) {
				currentImg.recycle();
			}
			currentImg = Bitmap.createBitmap(image.getWidth(), image.getHeight() + 5 * TEXT_HEIGHT, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(currentImg);
			
			String title = link.getTitle();
			List<String> tokens = new ArrayList<String>(Arrays.asList(title.split(" ")));
			int tokenCount = 0;
			int lineCount = 0;
			while (tokenCount < tokens.size()) {
				StringBuilder sb = new StringBuilder();
				Rect rect = new Rect(0, 0, 0, 0);
				while (rect.right < screenW - 2 * TITLE_SIDE_MARGIN && tokenCount < tokens.size()) {
					sb.append(tokens.get(tokenCount) + " ");
					tokenCount++;
					textPaint.getTextBounds(sb.toString(), 0, sb.length(), rect);
				}

				String lineText = sb.toString().trim();
				if(rect.right >= screenW - 2 * TITLE_SIDE_MARGIN) {
					tokenCount--;
					lineText = lineText.substring(0, lineText.length() - tokens.get(tokenCount).length());
				}
				if(lineText.length() > 0) {
					canvas.drawText(lineText, TITLE_SIDE_MARGIN, TITLE_TOP + lineCount * TEXT_HEIGHT, textPaint);
					lineCount++;
				} else {
					// token is too long, split it and retry
					String tooLongToken = tokens.get(tokenCount);
					rect = new Rect(0, 0, 0, 0);
					int tooLongTokenCounter = 0;
					while (rect.right < screenW - 2 * TITLE_SIDE_MARGIN) {
						textPaint.getTextBounds(tooLongToken, 0, tooLongTokenCounter, rect);
						tooLongTokenCounter++;
					}
					tokens.remove(tokenCount);
					tokens.add(tokenCount, tooLongToken.substring(0, tooLongTokenCounter - 2));
					tokens.add(tokenCount + 1, tooLongToken.substring(tooLongTokenCounter - 2, tooLongToken.length()));
				}
			}
			
			canvas.drawBitmap(image, 0, TITLE_TOP + lineCount * TEXT_HEIGHT, null);
			yPos = 0;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			canvas.drawBitmap(currentImg, 0, yPos + currentY - startY, null);
		}

	}
}