package net.acuttone.reddimg;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
		ReddimgApp.instance().getImageCache().prepareImage(redditLink.getUrl());
		String diskPath = ReddimgApp.instance().getImageCache().getDiskPath(redditLink.getUrl());
		Bitmap bitmap = ReddimgApp.instance().getImageCache().getFromDisk(redditLink.getUrl());
		ImageView view = (ImageView) findViewById(R.id.scrollViewLink).findViewById(R.id.imageViewLink);
		view.setImageBitmap(bitmap);
	}
}