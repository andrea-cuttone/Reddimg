package net.acuttone.reddimg.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


public class RedditLink {
	public String title;
	private String url;
	private String id;
	private String commentUrl;
	private int score;
	private String subreddit;
	private String author;
	private String voteStatus;
	private String thumbUrl;
	private Bitmap thumb;
	
	public RedditLink(String id, String url, String commentUrl, String title, String author, String subreddit, int score, Boolean voteStatus, String thumbUrl) {
		this.id = id;
		this.title = title;
		this.url = url;
		this.commentUrl = commentUrl;
		this.author = author;
		this.subreddit = subreddit;
		this.score = score;
		if(voteStatus == null) {
			this.voteStatus = RedditClient.NO_VOTE;
		} else if(voteStatus == true) {
			this.voteStatus = RedditClient.UPVOTE;
		} else if(voteStatus == false) {
			this.voteStatus = RedditClient.DOWNVOTE;
		}
		this.thumbUrl = thumbUrl;
	}
	
	public String getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getCommentUrl() {
		return commentUrl;
	}
	
	public int getScore() {
		return score;
	}

	public String getSubreddit() {
		return subreddit;
	}

	public String getAuthor() {
		return author;
	}
	
	public String getVoteStatus() {
		return voteStatus;
	}
	
	public void setVoteStatus(String voteStatus) {
		this.voteStatus = voteStatus;
	}

	public Bitmap getThumb() {
		return thumb;
	}
	
	public void setThumb(Bitmap thumb) {
		if(this.thumb != null && this.thumb.isRecycled() == false) {
			this.thumb.recycle();
		}
		this.thumb = thumb;
	}
	
	public void prepareThumb(int size) {
		if (thumb == null || thumb.isRecycled()) {
			HttpURLConnection connection = null;
			InputStream is = null;

			try {
				connection = (HttpURLConnection) new URL(url).openConnection();
				connection.setConnectTimeout(5000);
				is = connection.getInputStream();
				thumb = BitmapFactory.decodeStream(is);
			} catch (MalformedURLException e) {
				Log.e(ReddimgApp.APP_NAME, e.toString());
				return;
			} catch (IOException e) {
				Log.e(ReddimgApp.APP_NAME, e.toString());
				return;
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
				try {
					if (is != null) {
						is.close();
					}
				} catch (IOException e) {
					Log.e(ReddimgApp.APP_NAME, e.toString());
				}
			}
		}
		if (thumb != null && thumb.getWidth() != size) {
			Bitmap bmpSquared = thumb;
			if (thumb.getWidth() != thumb.getHeight()) {
				int minDim = Math.min(thumb.getWidth(), thumb.getHeight());
				int x0 = (thumb.getWidth() - minDim) / 2;
				int x1 = (thumb.getWidth() + minDim) / 2;
				int y0 = (thumb.getHeight() - minDim) / 2;
				int y1 = (thumb.getHeight() + minDim) / 2;
				try {
					bmpSquared = Bitmap.createBitmap(thumb, x0, y0, x1, y1);
					thumb.recycle();
				} catch (Throwable e) {
					Log.w(ReddimgApp.APP_NAME, e.toString());
				}
			}
			try {
				setThumb(Bitmap.createScaledBitmap(bmpSquared, size, size, true));
				bmpSquared.recycle();
			} catch (Throwable e) {
				Log.w(ReddimgApp.APP_NAME, e.toString());
			}
		}
	}
	
	public String getThumbUrl() {
		return thumbUrl;
	}

	public void setThumbUrl(String thumbUrl) {
		this.thumbUrl = thumbUrl;
	}
	
	@Override
	public String toString() {
		return getTitle() + " - " + getUrl();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((commentUrl == null) ? 0 : commentUrl.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RedditLink other = (RedditLink) obj;
		if (commentUrl == null) {
			if (other.commentUrl != null)
				return false;
		} else if (!commentUrl.equals(other.commentUrl))
			return false;
		return true;
	}

}
