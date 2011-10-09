package net.acuttone.reddimg;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

enum ScrollingState { NO_SCROLL, SCROLL_LEFT, SCROLL_RIGHT };

// TODO: NEED TO CLEANUP BITMAPS AND THREADS ON STATE CHANGE!!!
public class MainActivity extends Activity implements OnTouchListener {
	
	public static String APP_NAME = "REDDIMG";
	
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		yPos = 0;
		scrollingState = ScrollingState.NO_SCROLL;
		currentLinkIndex = 0;
		
		linkRenderer = new LinkRenderer(RedditApplication.instance().getScreenW(), RedditApplication.instance().getScreenH());

		view = new SlideshowView(getApplicationContext());
		setContentView(view);
		view.setOnTouchListener(this);
		loadImage();			
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
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		view.postInvalidate();
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	private void loadImage() {
		final ProgressDialog dialog = ProgressDialog.show(this, "", "Loading...");
		new AsyncTask<Integer, String, Bitmap>() {

			@Override
			protected Bitmap doInBackground(Integer... params) {
				int index = params[0];
				Bitmap image = null;
				RedditLink currentLink = null;
				while (image == null) {
					if (isConnectionActive() == false) {
						return null;
					}

					currentLink = RedditApplication.instance().getLinksQueue().get(index);
					if (currentLink != null) {
						publishProgress("Loading " + currentLink.getUrl());
						image = RedditApplication.instance().getImageCache().getFromMem(currentLink.getUrl());
					} else {
						publishProgress("Fetching links...");
					}

					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {

					}
				}

				return linkRenderer.render(currentLink, image);
			}

			protected void onProgressUpdate(String... values) {
				dialog.setMessage(values[0]);
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				super.onPostExecute(result);
				dialog.dismiss();
				if (result != null) {
					yPos = 0;
					if (viewBitmap != null) {
						viewBitmap.recycle();
					}
					viewBitmap = result;
					view.invalidate();
				} else {
					RedditApplication.instance().getImagePrefetcher().setPaused(true);
					showDialog(DIALOG_CONNECTION_PROBLEM);
				}
			}
		}.execute(currentLinkIndex);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if (DIALOG_CONNECTION_PROBLEM == id) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(CONNECTION_PROBLEM_TEXT).setCancelable(false).setNeutralButton("Retry", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					RedditApplication.instance().getImagePrefetcher().setPaused(false);
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