package net.acuttone.reddimg;

import android.graphics.Bitmap;
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
	private Bitmap thumb;

	public RedditLink(String id, String url, String commentUrl, String title, String author, String subreddit, int score, Boolean voteStatus) {
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
		this.thumb = null;
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
		if (thumb == null || thumb.isRecycled() || thumb.getWidth() != size) {
			setThumb(null);
			Bitmap bmpFullSize = ReddimgApp.instance().getImageCache().getImage(getUrl());
			if (bmpFullSize != null) {
				Bitmap bmpSquared = bmpFullSize;
				if (bmpFullSize.getWidth() != bmpFullSize.getHeight()) {
					int minDim = Math.min(bmpFullSize.getWidth(), bmpFullSize.getHeight());
					int x0 = (bmpFullSize.getWidth() - minDim) / 2;
					int x1 = (bmpFullSize.getWidth() + minDim) / 2;
					int y0 = (bmpFullSize.getHeight() - minDim) / 2;
					int y1 = (bmpFullSize.getHeight() + minDim) / 2;
					try {
						bmpSquared = Bitmap.createBitmap(bmpFullSize, x0, y0, x1, y1);
						bmpFullSize.recycle();
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
