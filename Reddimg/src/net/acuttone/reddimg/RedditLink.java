package net.acuttone.reddimg;


public class RedditLink {

	public String title;
	private String url;
	private String id;
	private String commentUrl;
	private int score;
	private String subreddit;
	private String author;

	public RedditLink(String id, String url, String commentUrl, String title, String author, String subreddit, int score) {
		this.id = id;
		this.title = title;
		this.url = url;
		this.commentUrl = commentUrl;
		this.author = author;
		this.subreddit = subreddit;
		this.score = score;
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
