package net.acuttone.reddimg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class TextWrapper {

	private static final int LINES_INTERSPACE = 3;

	public static void drawTextLines(Canvas canvas, List<String> lines, int x, int y, Paint textPaint) {		
		int totLineHeight = 0;
		for(int i = 0; i < lines.size(); i++) {
			canvas.drawText(lines.get(i), x, y + totLineHeight, textPaint);
			Rect bounds = new Rect();
			textPaint.getTextBounds(lines.get(i), 0, lines.get(i).length(), bounds);
			totLineHeight += bounds.height() + LINES_INTERSPACE;
		}
	}
	
	public static Rect getMultilineBounds(List<String> lines, int x, int y, Paint textPaint) {		
		if(lines == null || lines.isEmpty()) {
			return null;
		}
		
		Rect totalBounds = new Rect();
		Rect currentBounds = new Rect();
		textPaint.getTextBounds(lines.get(0), 0, lines.get(0).length(), currentBounds);
		totalBounds.top = y + currentBounds.top;
		totalBounds.bottom = y + currentBounds.bottom;
		totalBounds.left = x + currentBounds.left;
		totalBounds.right = x + currentBounds.right;
		
		for(int i = 1; i < lines.size(); i++) {
			currentBounds = new Rect();
			textPaint.getTextBounds(lines.get(i), 0, lines.get(i).length(), currentBounds);
			totalBounds.right = Math.max(totalBounds.right, x + currentBounds.right);
			totalBounds.bottom += currentBounds.height() + LINES_INTERSPACE; 
		}

		return totalBounds;
	}
	
	public static List<String> getWrappedLines(String text, int width, Paint textPaint) {
		List<String> tokens = new ArrayList<String>(Arrays.asList(text.split(" ")));
		int tokenCount = 0;
		List<String> lines = new ArrayList<String>();
		while (tokenCount < tokens.size()) {
			StringBuilder sb = new StringBuilder();
			Rect rect = new Rect(0, 0, 0, 0);
			while (rect.right < width && tokenCount < tokens.size()) {
				sb.append(tokens.get(tokenCount) + " ");
				tokenCount++;
				textPaint.getTextBounds(sb.toString(), 0, sb.length(), rect);
			}

			String lineText = sb.toString().trim();
			if(rect.right >= width) {
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
				while (rect.right < width) {
					textPaint.getTextBounds(tooLongToken, 0, tooLongTokenCounter, rect);
					tooLongTokenCounter++;
				}
				tokens.remove(tokenCount);
				tokens.add(tokenCount, tooLongToken.substring(0, tooLongTokenCounter - 2));
				tokens.add(tokenCount + 1, tooLongToken.substring(tooLongTokenCounter - 2, tooLongToken.length()));
			}
		}
		return lines;
	}
	
}
