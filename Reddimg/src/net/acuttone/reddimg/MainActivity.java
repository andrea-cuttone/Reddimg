package net.acuttone.reddimg;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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

	public static final String CURRENT_INDEX = "CURRENT_INDEX";
	public static final String CURRENT_BMP = "CURRENT_BMP";

	private static final double SCROLL_MARGIN = 5.;

	private static final int LOAD_IMAGE_CODE = 1;

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
				yPos = 0;
				if (viewBitmap != null) {
					viewBitmap.recycle();
				}
				viewBitmap = result;
				view.invalidate();
				dialog.dismiss();
			}
		}.execute(currentLinkIndex);
	}

	/*private void startLoadingActivity() {
		Intent i = new Intent(this, LoadingActivity.class);
		i.putExtra(CURRENT_INDEX, currentLinkIndex);
		if(viewBitmap != null) {
			Bitmap oldBmp = Bitmap.createBitmap(viewBitmap, 0, (int)(-yPos), 
					Math.min(viewBitmap.getWidth(), RedditApplication.instance().getScreenW()),
					Math.min(viewBitmap.getHeight(), RedditApplication.instance().getScreenH()));
			i.putExtra(CURRENT_BMP, oldBmp);
		}
		startActivityForResult(i, LOAD_IMAGE_CODE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {		
		super.onActivityResult(requestCode, resultCode, data);
		if(LOAD_IMAGE_CODE == requestCode && RESULT_OK == resultCode) {
			if (viewBitmap != null) {
				viewBitmap.recycle();
			}
			yPos = 0;
			RedditLink currentLink = RedditApplication.instance().getLinksQueue().get(data.getExtras().getInt(CURRENT_INDEX));
			Bitmap image = RedditApplication.instance().getImageCache().getFromMem(currentLink.getUrl());			
			viewBitmap = linkRenderer.render(currentLink, image);
			view.invalidate();
		} else {
			currentLinkIndex--;
			if(currentLinkIndex < 0) {
				currentLinkIndex = 0;
			}
		}
	}*/

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