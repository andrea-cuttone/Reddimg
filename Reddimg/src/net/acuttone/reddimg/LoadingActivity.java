package net.acuttone.reddimg;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class LoadingActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		LoadingView view = new LoadingView(getApplicationContext());
		setContentView(view);
	}
	
	class LoadingView extends View {

		private Paint paint;

		public LoadingView(Context context) {
			super(context);
			paint = new Paint();
			paint.setColor(Color.WHITE);
			paint.setTextSize(14.0f);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			
			int currentIndex = getIntent().getExtras().getInt(MainActivity.CURRENT_INDEX);
			RedditLink currentLink = RedditApplication.getInstance().getLinksQueue().get(currentIndex);
			if(currentLink != null) {
				canvas.drawText("loading " + currentLink.getUrl() , 20, 20, paint);				
				Bitmap image = RedditApplication.getInstance().getImageCache().getFromMem(currentLink.getUrl());
				if (image != null) {
					setResult(RESULT_OK, getIntent());
					finish();
				}
			} else {
				canvas.drawText("fetching new links...", 20, 20, paint);				
			}

			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				
			}
			postInvalidate();
		}
	}
}