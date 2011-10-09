package net.acuttone.reddimg;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

// TODO: this should be a dlg! http://developer.android.com/guide/topics/ui/dialogs.html
public class LoadingActivity extends Activity {

	private static final int DIALOG_CONNECTION_PROBLEM = 1;

	private static final String CONNECTION_PROBLEM_TEXT = "Oops! There seem to be a problem with the connection";

	private int currentIndex;
	private Bitmap oldBmp;
	private boolean waitForConnection;

	private LoadingView view;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentIndex = getIntent().getExtras().getInt(MainActivity.CURRENT_INDEX);
		RedditLink currentLink = RedditApplication.instance().getLinksQueue().get(currentIndex);
		if (currentLink != null) {
			Bitmap image = RedditApplication.instance().getImageCache().getFromMem(currentLink.getUrl());
			if (image != null) {
				setResult(RESULT_OK, getIntent());
				finish();
				return;
			}
		}
		waitForConnection = false;
		oldBmp = getIntent().getExtras().getParcelable(MainActivity.CURRENT_BMP);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		view = new LoadingView(getApplicationContext());
		setContentView(view);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (DIALOG_CONNECTION_PROBLEM == id) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(CONNECTION_PROBLEM_TEXT).setCancelable(false).setNeutralButton("Retry", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					RedditApplication.instance().getImagePrefetcher().setPaused(false);
					waitForConnection = false;
					view.invalidate();
				}
			});
			return builder.create();
		}
		return super.onCreateDialog(id);
	}

	class LoadingView extends View {
		private static final int STATUS_TEXT_BOUNDS_MARGIN = 15;
		private Paint paint;
		private Rect statusRect;

		public LoadingView(Context context) {
			super(context);
			paint = new Paint();
			paint.setTextSize(14.0f);
			paint.setAntiAlias(true);
			statusRect = new Rect(20, RedditApplication.instance().getScreenH() / 3, RedditApplication.instance().getScreenW() - 20, -1);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			if (oldBmp != null) {
				canvas.drawBitmap(oldBmp, 0, 0, paint);
			}

			if (!waitForConnection) {

				RedditLink currentLink = RedditApplication.instance().getLinksQueue().get(currentIndex);
				if (currentLink != null) {
					Bitmap image = RedditApplication.instance().getImageCache().getFromMem(currentLink.getUrl());
					if (image != null) {
						setResult(RESULT_OK, getIntent());
						finish();
						return;
					} else {
						if (isConnectionActive()) {
							drawStatus(canvas, "Loading " + currentLink.getUrl());
						} else {
							showWaitingDlg();
						}
					}
				} else {
					if (isConnectionActive()) {
						drawStatus(canvas, "Fetching new links...");
					} else {
						showWaitingDlg();
					}
				}

				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {

				}

				invalidate();
			}
		}

		private void showWaitingDlg() {
			RedditApplication.instance().getImagePrefetcher().setPaused(true);
			waitForConnection = true;
			showDialog(DIALOG_CONNECTION_PROBLEM);
		}

		private boolean isConnectionActive() {
			ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = conMgr.getActiveNetworkInfo();
			return networkInfo != null && networkInfo.isConnected() && networkInfo.isAvailable();
		}

		private void drawStatus(Canvas canvas, String text) {
			List<String> wrappedLines = TextWrapper.getWrappedLines(text, statusRect.width(), paint);
			Rect bounds = TextWrapper.getMultilineBounds(wrappedLines, statusRect.left, statusRect.top, paint);
			bounds.left = 0;
			bounds.right = RedditApplication.instance().getScreenW();
			bounds.top -= STATUS_TEXT_BOUNDS_MARGIN;
			bounds.bottom += STATUS_TEXT_BOUNDS_MARGIN;
			paint.setColor(Color.BLACK);
			canvas.drawRect(bounds, paint);
			paint.setColor(Color.WHITE);
			TextWrapper.drawTextLines(canvas, wrappedLines, statusRect.left, statusRect.top, paint);
		}
	}

}