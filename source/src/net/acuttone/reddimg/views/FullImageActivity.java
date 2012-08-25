package net.acuttone.reddimg.views;

import net.acuttone.reddimg.R;
import net.acuttone.reddimg.core.ReddimgApp;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

public class FullImageActivity extends Activity {

	public static final String IMAGE_NAME = "IMAGE_NAME";
	private static final String FULLIMAGE_TUTORIAL_SHOWN = "FULLIMAGE_TUTORIAL_SHOWN";

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
		if(ReddimgApp.instance().getPrefs().getBoolean(FULLIMAGE_TUTORIAL_SHOWN, false) == false) {
			showTutorial();
		}
	}
	
	private void showTutorial() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Tutorial");
		builder.setMessage("Pinch to zoom\n\nPress back to return to the previous screen")
				.setCancelable(false).setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Editor editor = ReddimgApp.instance().getPrefs().edit();
						editor.putBoolean(FULLIMAGE_TUTORIAL_SHOWN, true);
						editor.commit();
						dialog.dismiss();
					}
				});
		AlertDialog dlg = builder.create();
		dlg.show();
	}

}