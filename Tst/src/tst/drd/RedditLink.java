package tst.drd;

import android.graphics.Bitmap;

public class RedditLink {

	public String title;
	public Bitmap image;

	public RedditLink(Bitmap image, String title) {
		super();
		this.title = title;
		this.image = image;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Bitmap getImage() {
		return image;
	}
	public void setImage(Bitmap image) {
		this.image = image;
	}
	
}
