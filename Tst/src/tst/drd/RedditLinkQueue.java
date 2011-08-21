package tst.drd;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class RedditLinkQueue {

	List<RedditLink> links;
	
	public RedditLinkQueue(Resources res, Context context) {
		links = new ArrayList<RedditLink>();
		
		ImageResizer resizer = new ImageResizer(context);
		
		Bitmap img = resizer.resize(BitmapFactory.decodeResource(res, R.drawable.img1));
		links.add(new RedditLink(img, "A lot of paintings"));
		img = resizer.resize(BitmapFactory.decodeResource(res, R.drawable.img2));
		links.add(new RedditLink(img, "A nice castleeeeeeeeeeeeeeeeeeeeeeeee"));		
	}
	
	public RedditLink at(int index) {
		return links.get(Math.abs(index % 2));
	}
	
}
