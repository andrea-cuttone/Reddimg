package net.acuttone.reddimg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class LoadingActivity extends Activity {

	private Paint paint;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		LoadingView view = new LoadingView(getApplicationContext());
		setContentView(view);

		paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setTextSize(16.0f);
	}

	class LoadingView extends View {

		public LoadingView(Context context) {
			super(context);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			// TODO: move to periodic action
			super.onDraw(canvas);

			RedditLink currentLink = null;
			synchronized (RedditApplication.getInstance().getLinksQueue()) {
				currentLink = RedditApplication.getInstance().getLinksQueue().get(getIntent().getExtras().getInt(MainActivity.CURRENT_INDEX));
			}

			canvas.drawText("loading " + currentLink.getUrl(), 10, 10, paint);

			Bitmap image = RedditApplication.getInstance().getImageCache().getFromMem(currentLink.getUrl());
			if (image != null) {
				setResult(RESULT_OK, getIntent());
				finish();
			} else {
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {

				}
				invalidate();
			}
		}

	}
}