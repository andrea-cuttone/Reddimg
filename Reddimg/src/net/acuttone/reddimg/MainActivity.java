package net.acuttone.reddimg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
		
		linkRenderer = new LinkRenderer(RedditApplication.getInstance().getScreenW(), RedditApplication.getInstance().getScreenH());

		startLoadingActivity();			

		view = new SlideshowView(getApplicationContext());
		setContentView(view);
		view.setOnTouchListener(this);
	}
	
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (event.getX() < RedditApplication.getInstance().getScreenW() * (1. / SCROLL_MARGIN)) {
				scrollingState = ScrollingState.SCROLL_LEFT;
			} else if (event.getX() > RedditApplication.getInstance().getScreenW() * (1 - 1. / SCROLL_MARGIN)) {
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
				startLoadingActivity();
			} else if (scrollingState == ScrollingState.SCROLL_RIGHT) {
				currentLinkIndex++;
				startLoadingActivity();
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

	private void startLoadingActivity() {
		Intent i = new Intent(this, LoadingActivity.class);
		i.putExtra(CURRENT_INDEX, currentLinkIndex);
		startActivityForResult(i, LOAD_IMAGE_CODE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {		
		super.onActivityResult(requestCode, resultCode, data);
		if(LOAD_IMAGE_CODE == requestCode) {
			if (viewBitmap != null) {
				viewBitmap.recycle();
			}
			yPos = 0;
			RedditLink currentLink = RedditApplication.getInstance().getLinksQueue().get(data.getExtras().getInt(CURRENT_INDEX));
			Bitmap image = RedditApplication.getInstance().getImageCache().getFromMem(currentLink.getUrl());			
			viewBitmap = linkRenderer.render(currentLink, image);
			view.invalidate();
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
			float actualY = yPos + currentY - startY;
			if(actualY < -viewBitmap.getHeight() + RedditApplication.getInstance().getScreenH()) {
				actualY = -viewBitmap.getHeight() + RedditApplication.getInstance().getScreenH();
			} 
			if(actualY > 0) {
				actualY = 0;
			}
			canvas.drawBitmap(viewBitmap, 0, actualY, null);
		}

	}
}