package tst.drd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class LinkRenderer {

	private int screenW;
	private int screenH;
	private Paint textPaint;

	private static final int TITLE_SIDE_MARGIN = 5;
	private static final int TITLE_TOP = 15;
	private static final int TEXT_HEIGHT = 15;
	
	public LinkRenderer(int screenW, int screenH) {
		this.screenW = screenW;
		this.screenH = screenH;
		
		textPaint = new Paint();
		textPaint.setColor(Color.WHITE);
		textPaint.setTextSize(14.0f);
		textPaint.setAntiAlias(true);
	}
	
	public Bitmap render(RedditLink link, Bitmap image) {
		String title = getTitle(link);
		List<String> tokens = new ArrayList<String>(Arrays.asList(title.split(" ")));
		int tokenCount = 0;
		List<String> lines = new ArrayList<String>();
		while (tokenCount < tokens.size()) {
			StringBuilder sb = new StringBuilder();
			Rect rect = new Rect(0, 0, 0, 0);
			while (rect.right < screenW - 2 * TITLE_SIDE_MARGIN && tokenCount < tokens.size()) {
				sb.append(tokens.get(tokenCount) + " ");
				tokenCount++;
				textPaint.getTextBounds(sb.toString(), 0, sb.length(), rect);
			}

			String lineText = sb.toString().trim();
			if(rect.right >= screenW - 2 * TITLE_SIDE_MARGIN) {
				tokenCount--;
				lineText = lineText.substring(0, lineText.length() - tokens.get(tokenCount).length());
			}
			if(lineText.length() > 0) {
				lines.add(lineText);
			} else {
				// token is too long, split it and retry
				String tooLongToken = tokens.get(tokenCount);
				rect = new Rect(0, 0, 0, 0);
				int tooLongTokenCounter = 0;
				while (rect.right < screenW - 2 * TITLE_SIDE_MARGIN) {
					textPaint.getTextBounds(tooLongToken, 0, tooLongTokenCounter, rect);
					tooLongTokenCounter++;
				}
				tokens.remove(tokenCount);
				tokens.add(tokenCount, tooLongToken.substring(0, tooLongTokenCounter - 2));
				tokens.add(tokenCount + 1, tooLongToken.substring(tooLongTokenCounter - 2, tooLongToken.length()));
			}
		}
		
		Bitmap currentImg = Bitmap.createBitmap(image.getWidth(), image.getHeight() + TITLE_TOP + lines.size() * TEXT_HEIGHT, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(currentImg);
		for(int i = 0; i < lines.size(); i++) {
			canvas.drawText(lines.get(i), TITLE_SIDE_MARGIN, TITLE_TOP + i * TEXT_HEIGHT, textPaint);
		}
		canvas.drawBitmap(image, 0, TITLE_TOP + lines.size() * TEXT_HEIGHT, null);
		return currentImg;
	}

	private String getTitle(RedditLink link) {
		return link.getTitle();
	}
}
