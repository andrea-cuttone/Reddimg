package net.acuttone.reddimg.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.acuttone.reddimg.prefs.SubredditsPickerActivity;
import android.util.Log;

public class RedditLinkQueue {

	private List<RedditLink> links;
	private String lastT3;
	private String subreddits;
	private Object lock = new Object();
	
	public RedditLinkQueue() {
		initSubreddits();
	}
	
	public void initSubreddits() {
		synchronized (lock) {
			links = new ArrayList<RedditLink>();
			lastT3 = "";
			List<String> list = SubredditsPickerActivity
					.getSubredditsFromPref();
			if (list.isEmpty()) {
				list = SubredditsPickerActivity.getDefaultSubreddits();
			}
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < list.size(); i++) {
				sb.append(list.get(i));
				if (i != list.size() - 1) {
					sb.append("+");
				}
			}
			subreddits = sb.toString();
		}
	}

	public RedditLink get(int index) throws IOException {
		synchronized(lock) {
			if(index >= links.size()) {
				return null;
			} else if(index >= 0) {
				return links.get(index);
			} else {
				return null;
			}
		}
	}	
	
	public void getNewLinks() throws IOException {
		Log.d(ReddimgApp.APP_NAME, "Fetching links from " + subreddits);
		List<RedditLink> newLinks = new ArrayList<RedditLink>();
		String result = ReddimgApp.instance().getRedditClient().getLinks(newLinks, subreddits, lastT3);
		synchronized (lock) {
			if (result != null) {
				lastT3 = result;
			}

			for (RedditLink l : newLinks) {
				if (links.contains(l) == false) {
					links.add(l);
				}
			}
		}
	}

}
