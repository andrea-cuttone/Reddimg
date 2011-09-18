package net.acuttone.reddimg;

import android.graphics.Bitmap;

enum ResizeMode { FIT_ALL, FIT_WIDTH };

public class ImageResizer {

	private double screenW;
	private double screenH;
	private ResizeMode resizeMode;	

	public ImageResizer(int w, int h) {
		this.screenW = w;
		this.screenH = h;
		this.resizeMode = ResizeMode.FIT_WIDTH;
	}
	
	public Bitmap resize(Bitmap src) {
		
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
