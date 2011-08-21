package tst.drd;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ImageResizer {

	private DisplayMetrics displaymetrics;

	public ImageResizer(Context context) {
		displaymetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(displaymetrics);
	}
	
	public Bitmap resize(Bitmap src) {
		return Bitmap.createScaledBitmap(src, displaymetrics.widthPixels, displaymetrics.heightPixels, false);
	}
	
}
