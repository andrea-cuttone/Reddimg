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

	private RedditLinkQueue linksQueue;
	private ImageCache imgCache;
	private Paint paint;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		LoadingView view = new LoadingView(getApplicationContext());
		setContentView(view);
		
		linksQueue = (RedditLinkQueue) RedditApplication.getInstance().getMap().get("LINK_QUEUE");
		imgCache = (ImageCache) RedditApplication.getInstance().getMap().get("IMAGE_CACHE");
		paint = new Paint();
		paint.setColor(Color.BLUE);
	}
	
	class LoadingView extends View {

		private LinkRenderer linkRenderer;

		public LoadingView(Context context) {
			super(context);
			linkRenderer = new LinkRenderer(240, 400);
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			// TODO: move to periodic action
			super.onDraw(canvas);
			canvas.drawColor(Color.RED);
			
			
			RedditLink currentLink = null;	
			synchronized (linksQueue) {
				currentLink = linksQueue.get(getIntent().getExtras().getInt("CURRENT_INDEX"));
			}
			
			canvas.drawText("loading " + currentLink.getUrl(), 10, 10, paint);
			
			Bitmap image = imgCache.getFromMem(currentLink.getUrl());
			if(image != null) {
				Bitmap bmp = linkRenderer.render(currentLink, image);
				RedditApplication.getInstance().getMap().put("IMAGE", bmp);
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