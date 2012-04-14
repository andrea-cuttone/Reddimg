package net.acuttone.reddimg.core;

import java.util.ArrayList;
import java.util.List;

import net.acuttone.reddimg.prefs.SubredditsPickerActivity;
import android.util.Log;

public class RedditLinkQueue {

	private List<RedditLink> links;
	private String lastT3;
	private String subreddits;
	
	public RedditLinkQueue() {
		initSubreddits();
	}
	
	public void initSubreddits() {
		links = new ArrayList<RedditLink>();
		lastT3 = "";
		List<String> list = SubredditsPickerActivity.getSubredditsFromPref();
		if(list.isEmpty()) {
			list = SubredditsPickerActivity.getDefaultSubreddits();
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			sb.append(list.get(i));
			if(i != list.size() -1) {
				sb.append("+");
			}
		}
		subreddits = sb.toString();
	}

	public RedditLink get(int index) {
		while(index >= links.size()) {
			getNewLinks();
		}
		return links.get(index);
	}	
	
	private void getNewLinks() {
		Log.d(ReddimgApp.APP_NAME, "Fetching links from " + subreddits);
		List<RedditLink> newLinks = new ArrayList<RedditLink>();
		String result = ReddimgApp.instance().getRedditClient().getLinks(newLinks, subreddits, lastT3);
		if (result != null && !result.equals("")) {
			lastT3 = result;
		}

		synchronized (links) {
			for (RedditLink l : newLinks) {
				if (links.contains(l) == false) {
					links.add(l);
				}
			}
		}
	}

}
