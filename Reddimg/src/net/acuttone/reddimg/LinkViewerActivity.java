package net.acuttone.reddimg;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
	private ImageView view;

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
		view = (ImageView) findViewById(R.id.scrollViewLink).findViewById(R.id.imageViewLink);
		view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		view.setAdjustViewBounds(true);
		view.setImageBitmap(bitmap);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Drawable drawable = view.getDrawable();
		if (drawable instanceof BitmapDrawable) {
		    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
		    Bitmap bitmap = bitmapDrawable.getBitmap();
		    bitmap.recycle();
		}
	}
}