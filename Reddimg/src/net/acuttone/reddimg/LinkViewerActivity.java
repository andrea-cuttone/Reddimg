package net.acuttone.reddimg;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
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
		WebView webView = (WebView) findViewById(R.id.webViewLink);
		// TODO: fix the width
		//webView.getSettings().setLoadWithOverviewMode(true);
		//webView.getSettings().setUseWideViewPort(true);
		ReddimgApp.instance().getImageCache().prepareImage(redditLink.getUrl());
		String diskPath = ReddimgApp.instance().getImageCache().getDiskPath(redditLink.getUrl());
		webView.loadUrl(diskPath);
	}
}