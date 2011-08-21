package tst.drd;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

public class TstActivity extends Activity {
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		MyView myView = new MyView(getApplicationContext());
       	setContentView(myView);
	}

	public class MyView extends View implements OnTouchListener {
		private int currentImgIndex = 0;
		private RedditLinkQueue linksQueue;
		
		private float startX;
		private float currentX;

		public MyView(Context context) {
			super(context);
			linksQueue = new RedditLinkQueue(getResources(), getContext());
			setOnTouchListener(this);
		}

		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				startX = event.getX();
			} 
			if (event.getAction() == MotionEvent.ACTION_MOVE) {
				currentX = event.getX();
				postInvalidate();
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {
				float endX = event.getX();
				if (endX < startX) {
					currentImgIndex++;
				} else {
					currentImgIndex--;
				}
				currentX = startX;
				postInvalidate();
			}
			return true;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			RedditLink center = linksQueue.at(currentImgIndex);
			Paint textPaint = new Paint();
			textPaint.setColor(Color.RED);
			int titleHeight = 50;
			float delta = currentX - startX;
			if(delta < 0) {
				RedditLink right = linksQueue.at(currentImgIndex + 1);				
				canvas.drawBitmap(center.getImage(), delta, 0, null);
				canvas.drawText(center.getTitle(), delta, titleHeight, textPaint);				
				canvas.drawBitmap(right.getImage(), center.getImage().getWidth() + delta, 0, null);
				canvas.drawText(right.getTitle(), center.getImage().getWidth() + delta, titleHeight, textPaint);								
			} else {
				RedditLink left = linksQueue.at(currentImgIndex - 1);				
				canvas.drawBitmap(center.getImage(), delta, 0, null);
				canvas.drawText(center.getTitle(), delta, titleHeight, textPaint);								
				canvas.drawBitmap(left.getImage(), -left.getImage().getWidth() + delta, 0, null);
				canvas.drawText(left.getTitle(), -left.getImage().getWidth() + delta, titleHeight, textPaint);												
			}
		}

	}
}