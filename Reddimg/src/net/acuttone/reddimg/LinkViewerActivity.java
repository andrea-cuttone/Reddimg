package net.acuttone.reddimg;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

public class LinkViewerActivity extends Activity {

	public static final String LINK_INDEX = "LINK_INDEX";
	private int currentLinkIndex;
	private ImageView viewBitmap;
	private ImageView viewLeftArrow;
	private ImageView viewRightArrow;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.linkviewer);
		currentLinkIndex = getIntent().getExtras().getInt(LINK_INDEX);
		RedditLink redditLink = ReddimgApp.instance().getLinksQueue().get(currentLinkIndex);
		TextView titleTextView = (TextView) findViewById(R.id.textViewTitle);
		titleTextView.setText(redditLink.getTitle());
		Bitmap bitmap = ReddimgApp.instance().getImageCache().getImage(redditLink.getUrl());
		viewBitmap = (ImageView) findViewById(R.id.scrollViewLink).findViewById(R.id.imageViewLink);
		viewBitmap.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		viewBitmap.setAdjustViewBounds(true);
		viewBitmap.setImageBitmap(bitmap);
		
		viewLeftArrow = (ImageView) findViewById(R.id.imageleftarrow);
		viewRightArrow = (ImageView) findViewById(R.id.imagerightarrow);
		
		AsyncTask<Void,Integer,Void> fadeTask = new AsyncTask<Void, Integer, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				for(int alpha = 255; alpha >= 0; alpha -= 5) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) { }
					publishProgress(alpha);
				}
				return null;
			}
			
			@Override
			protected void onProgressUpdate(Integer... values) {
				viewLeftArrow.setAlpha(values[0]);
				viewRightArrow.setAlpha(values[0]);
				super.onProgressUpdate(values);
			}
			
		};
		fadeTask.execute(null);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Drawable drawable = viewBitmap.getDrawable();
		if (drawable instanceof BitmapDrawable) {
		    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
		    Bitmap bitmap = bitmapDrawable.getBitmap();
		    bitmap.recycle();
		}
	}
}