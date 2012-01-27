package net.acuttone.reddimg;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class LinkViewerActivity extends Activity {

	public static final String LINK_INDEX = "LINK_INDEX";
	private int currentLinkIndex;
	private ImageView viewBitmap;
	private ImageView viewLeftArrow;
	private ImageView viewRightArrow;
	private TextView textViewTitle;
	private AsyncTask<Void,Integer,Void> fadeTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.linkviewer);
		textViewTitle = (TextView) findViewById(R.id.textViewTitle);
		viewBitmap = (ImageView) findViewById(R.id.scrollViewLink).findViewById(R.id.imageViewLink);
		viewLeftArrow = (ImageView) findViewById(R.id.imageleftarrow);
		viewRightArrow = (ImageView) findViewById(R.id.imagerightarrow);
		viewLeftArrow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(currentLinkIndex > 0) {
					currentLinkIndex--;
					loadImage();
				}
			}

		});
		viewRightArrow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				currentLinkIndex++;
				loadImage();
			}
		});
		currentLinkIndex = getIntent().getExtras().getInt(LINK_INDEX);
		loadImage();
	}
	
	private void loadImage() {
		recycleBitmap();
		
		RedditLink redditLink = ReddimgApp.instance().getLinksQueue().get(currentLinkIndex);
		textViewTitle.setText(redditLink.getTitle());
		Bitmap bitmap = ReddimgApp.instance().getImageCache().getImage(redditLink.getUrl());
		viewBitmap.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		viewBitmap.setAdjustViewBounds(true);
		viewBitmap.setImageBitmap(bitmap);
		
		if(fadeTask != null) {
			fadeTask.cancel(true);
		}
		
		fadeTask = new AsyncTask<Void, Integer, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				for(int alpha = 256; alpha >= 0 && isCancelled() == false; alpha -= 4) {
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
		recycleBitmap();
		
		if(fadeTask != null) {
			fadeTask.cancel(true);
		}
	}
	
	private void recycleBitmap() {
		Drawable drawable = viewBitmap.getDrawable();
		if (drawable instanceof BitmapDrawable) {
		    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
		    Bitmap bitmap = bitmapDrawable.getBitmap();
		    if(bitmap != null && bitmap.isRecycled() == false) {
		    	bitmap.recycle();
		    }
		}
	}
}