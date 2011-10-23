package net.acuttone.reddimg;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

enum ScrollingState { NO_SCROLL, SCROLL_LEFT, SCROLL_RIGHT };

// TODO: check http://saigeethamn.blogspot.com/2010/05/image-switcher-view-android-developer.html'
// TODO: http://developer.android.com/reference/android/webkit/WebView.html
public class MainActivity extends Activity implements OnTouchListener {
	
	private static final int DIALOG_CONNECTION_PROBLEM = 1;
	private static final String CONNECTION_PROBLEM_TEXT = "Oops! There seem to be a problem with the connection";
	private static final double SCROLL_MARGIN = 5.;

	private int currentLinkIndex;
	private float startY;
	private float currentY;
	private float yPos;		
	private ScrollingState scrollingState;
	private Bitmap viewBitmap;
	private SlideshowView view;
	private LinkRenderer linkRenderer;
	private ProgressDialog progressDlg;
	private AsyncTask<Integer, String, Bitmap> loadImgTask;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		yPos = 0;
		scrollingState = ScrollingState.NO_SCROLL;
		currentLinkIndex = 0;
		RedditApplication.instance().loadScreenSize();
		linkRenderer = new LinkRenderer();
		view = new SlideshowView(getApplicationContext());
		setContentView(view);
		view.setOnTouchListener(this);
		loadImage();			
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(viewBitmap != null && viewBitmap.isRecycled() == false) {
			viewBitmap.recycle();
		}
		RedditApplication.instance().getImagePrefetcher().setStatus(ImagePrefetcherStatus.TERMINATED);
		RedditApplication.instance().getImageCache().clearMemCache();
	}
	
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (event.getX() < RedditApplication.instance().getScreenW() * (1. / SCROLL_MARGIN)) {
				scrollingState = ScrollingState.SCROLL_LEFT;
			} else if (event.getX() > RedditApplication.instance().getScreenW() * (1 - 1. / SCROLL_MARGIN)) {
				scrollingState = ScrollingState.SCROLL_RIGHT;
			} else {
				startY = event.getY();
			}
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if(scrollingState == ScrollingState.NO_SCROLL) {
				currentY = event.getY();
				v.postInvalidate();
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
			currentY = startY = 0;
			scrollingState = ScrollingState.NO_SCROLL;
			v.postInvalidate();
		}
		return true;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		RedditApplication.instance().getImagePrefetcher().setStatus(ImagePrefetcherStatus.PAUSED);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		RedditApplication.instance().getImagePrefetcher().setStatus(ImagePrefetcherStatus.RUNNING);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if(viewBitmap != null) {
			viewBitmap.recycle();
			viewBitmap = null;
			view.invalidate();
		}
		RedditApplication.instance().loadScreenSize();
		RedditApplication.instance().getImageCache().clearMemCache();
		loadImage();
	}
	
	private void loadImage() {
		if(progressDlg != null && progressDlg.isShowing()) {
			progressDlg.dismiss();
		}
		progressDlg = ProgressDialog.show(this, "", "Loading...");
		if(loadImgTask != null) {
			loadImgTask.cancel(false);
		}
		loadImgTask = new AsyncTask<Integer, String, Bitmap>() {

			private Bitmap image;

			@Override
			protected Bitmap doInBackground(Integer... params) {
				int index = params[0];
				image = null;
				RedditLink currentLink = null;
				while (image == null) {
					if (isConnectionActive() == false) {
						return null;
					}

					currentLink = RedditApplication.instance().getLinksQueue().get(index);
					if (currentLink != null) {
						publishProgress("Loading " + currentLink.getUrl());
						image = RedditApplication.instance().getImageCache().getFromMem(currentLink.getUrl());
						// make a copy to avoid that the cached instance is cleared
						if(image != null) {
							image = Bitmap.createBitmap(image);
						}
					} else {
						publishProgress("Fetching links...");
					}

					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {

					}
				}

				Bitmap result = linkRenderer.render(currentLink, image);
				image.recycle();
				return result;
			}

			protected void onProgressUpdate(String... values) {
				progressDlg.setMessage(values[0]);
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				super.onPostExecute(result);
				progressDlg.dismiss();
				if (result != null) {
					yPos = 0;
					if (viewBitmap != null) {
						viewBitmap.recycle();
					}
					viewBitmap = result;
					view.invalidate();
				} else {
					RedditApplication.instance().getImagePrefetcher().setStatus(ImagePrefetcherStatus.PAUSED);
					showDialog(DIALOG_CONNECTION_PROBLEM);
				}
			}
			
			@Override
			protected void onCancelled() {				
				super.onCancelled();
				if(image != null) {
					image.recycle();
				}
			}
		};
		loadImgTask.execute(currentLinkIndex);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if (DIALOG_CONNECTION_PROBLEM == id) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(CONNECTION_PROBLEM_TEXT).setCancelable(false).setNeutralButton("Retry", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					RedditApplication.instance().getImagePrefetcher().setStatus(ImagePrefetcherStatus.RUNNING);
					loadImage();
				}
			});
			return builder.create();
		}
		return super.onCreateDialog(id);
	}
	
	private boolean isConnectionActive() {
		ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = conMgr.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected() && networkInfo.isAvailable();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		RedditLink currentLink = null;
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.loginmenuitem:
			intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			return true;
		case R.id.settingsmenuitem:
			intent = new Intent(this, PrefsActivity.class);
			startActivity(intent);
			return true;
		case R.id.upvotemenuitem:
			currentLink = RedditApplication.instance().getLinksQueue().get(currentLinkIndex);
			RedditApplication.instance().getRedditClient().vote(currentLink.getId(), RedditClient.UPVOTE);
			return true;
		case R.id.downvotemenuitem:
			currentLink = RedditApplication.instance().getLinksQueue().get(currentLinkIndex);
			RedditApplication.instance().getRedditClient().vote(currentLink.getId(), RedditClient.DOWNVOTE);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	class SlideshowView extends View {

		public SlideshowView(Context context) {
			super(context);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			if(viewBitmap == null || viewBitmap.isRecycled()) {
				return;
			}
			if(yPos + currentY - startY < -viewBitmap.getHeight() + RedditApplication.instance().getScreenH()) {
				yPos = -viewBitmap.getHeight() + RedditApplication.instance().getScreenH() - currentY + startY;
			} 
			if(yPos + currentY - startY > 0) {
				yPos = - currentY + startY;
			}
			canvas.drawBitmap(viewBitmap, 0, yPos + currentY - startY, null);
		}

	}
}