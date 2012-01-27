package net.acuttone.reddimg;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
		AsyncTask<Integer, RedditLink, Object[]> loadTask = new AsyncTask<Integer, RedditLink, Object[]>() {
			private ProgressDialog progressDialog;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				if(fadeTask != null) {
					fadeTask.cancel(true);
				}
				
				viewLeftArrow.setAlpha(0);
				viewRightArrow.setAlpha(0);
				
				recycleBitmap();
				
				progressDialog = ProgressDialog.show(LinkViewerActivity.this, "Reddimg", "Loading links...");
			}

			@Override
			protected Object[] doInBackground(Integer... params) {
				RedditLink redditLink = ReddimgApp.instance().getLinksQueue().get(params[0]);
				publishProgress(redditLink);
				Bitmap bitmap = ReddimgApp.instance().getImageCache().getImage(redditLink.getUrl());
				Object [] result = new Object[2];
				result[0] = bitmap;
				result[1] = redditLink;
				return result;
			}
			
			@Override
			protected void onProgressUpdate(RedditLink... values) {
				super.onProgressUpdate(values);
				updateTitle(values[0]);
				progressDialog.setMessage("Loading image...");
			}

			@Override
			protected void onPostExecute(Object[] result) {
				super.onPostExecute(result);
				progressDialog.dismiss();
				Bitmap bitmap = (Bitmap) ((Object []) result)[0];
				RedditLink redditLink = (RedditLink) ((Object []) result)[1];
				
				applyImage(bitmap, redditLink);
			}
		};
		loadTask.execute(currentLinkIndex);
	}

	private void applyImage(Bitmap bitmap, RedditLink redditLink) {
		viewBitmap.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		viewBitmap.setAdjustViewBounds(true);
		viewBitmap.setImageBitmap(bitmap);
		
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
		    viewBitmap.setImageBitmap(null);
		    if(bitmap != null && bitmap.isRecycled() == false) {
		    	bitmap.recycle();
		    }
		}
	}

	public void updateTitle(RedditLink link) {
		StringBuilder sb = new StringBuilder();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ReddimgApp.instance());
		if(sp.getBoolean("showScore", false)) {
			sb.append("[" + link.getScore() + "] ");
		}
		sb.append(link.getTitle());
		if(sp.getBoolean("showAuthor", false)) {
			sb.append(" | by " + link.getAuthor());
		}		
		if(sp.getBoolean("showSubreddit", false)) {
			sb.append(" in " + link.getSubreddit());
		}
		textViewTitle.setText(sb.toString());
		int size = Integer.parseInt(sp.getString(PrefsActivity.TITLE_SIZE_KEY, "24"));
		textViewTitle.setTextSize(size);
	}
}