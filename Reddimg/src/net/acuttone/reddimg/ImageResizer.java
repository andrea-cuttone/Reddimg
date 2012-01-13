package net.acuttone.reddimg;

import android.graphics.Bitmap;

enum ResizeMode { FIT_ALL, FIT_WIDTH };

public class ImageResizer {

	private ResizeMode resizeMode;	

	public ImageResizer() {
		this.resizeMode = ResizeMode.FIT_WIDTH;
	}
	
	public Bitmap resize(Bitmap src) {
		double screenW = ReddimgApp.instance().getScreenW();
		double screenH = ReddimgApp.instance().getScreenH();
		
		double targetW, targetH;
		if(resizeMode == ResizeMode.FIT_WIDTH || 
		   src.getWidth() / screenW > src.getHeight() / screenH) {
			targetW = screenW;
			targetH = src.getHeight() * (screenW / src.getWidth()); 
		} else {
			targetH = screenH;
			targetW = src.getWidth() * (screenH / src.getHeight());
		}
		return Bitmap.createScaledBitmap(src, (int) targetW, (int) targetH, false);
	}
	
}
