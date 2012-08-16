package net.acuttone.reddimg.views;

import net.acuttone.reddimg.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;

public class FullImageActivity extends Activity {

	public static final String IMAGE_NAME = "IMAGE_NAME";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.fullimage);
		String imageName = "file:/" + getIntent().getExtras().getString(IMAGE_NAME);
		WebView webview = (WebView) findViewById(R.id.webview_fullimage);
		webview.getSettings().setAllowFileAccess(true);
		webview.getSettings().setBuiltInZoomControls(true);
		webview.getSettings().setUseWideViewPort(true);
		webview.getSettings().setLoadWithOverviewMode(true);
		String html = ("<html><head></head><body style=\"background-color: black;\"><img src=\""+ imageName + "\"></body></html>"); 
		webview.loadDataWithBaseURL("", html, "text/html", "utf-8", "");
	}

}