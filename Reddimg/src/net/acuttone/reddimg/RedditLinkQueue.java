package net.acuttone.reddimg;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.util.Log;

public class RedditLinkQueue {

	private List<RedditLink> links;
	private List<String> lastT3List;
	private List<String> subredditsList;
	private int nextSubredditIndex;
	
	public RedditLinkQueue() {
		initSubreddits();
	}
	
	public void initSubreddits() {
		links = new ArrayList<RedditLink>();
		lastT3List = new ArrayList<String>();
		subredditsList = new ArrayList<String>();

		SharedPreferences sp = ReddimgApp.instance().getPrefs();
		String mode = sp.getString(PrefsActivity.SUBREDDIT_MODE_KEY, PrefsActivity.SUBREDDITMODE_FRONTPAGE);
		if (PrefsActivity.SUBREDDITMODE_MINE.equals(mode) &&
			ReddimgApp.instance().getRedditClient().isLoggedIn()) {
			subredditsList = ReddimgApp.instance().getRedditClient().getMySubreddits();
		} else if (PrefsActivity.SUBREDDITMODE_MANUAL.equals(mode)) {
			subredditsList = SubredditsPickerActivity.getSubredditsFromPref();
		}

		if (subredditsList.isEmpty()) {
			subredditsList.add("");
		}
		for (String s : subredditsList) {
			lastT3List.add("");
		}
		nextSubredditIndex = 0;
	}

	public RedditLink get(int index) {
		while(index >= links.size()) {
			getNewLinks();
		}
		return links.get(index);
	}	
	
	public String getCurrentSubreddit() {
		return subredditsList.get(nextSubredditIndex);
	}

	private void getNewLinks() {
		String subreddit = subredditsList.get(nextSubredditIndex);
		String lastT3 = lastT3List.get(nextSubredditIndex);
		Log.d(ReddimgApp.APP_NAME, "Fetching links from " + (subreddit.length() == 0 ? "reddit front page" : subreddit));
		List<RedditLink> newLinks = new ArrayList<RedditLink>();
		lastT3 = ReddimgApp.instance().getRedditClient().getLinks(newLinks, subreddit, lastT3);
		if (lastT3 != null && !lastT3.equals("")) {
			lastT3List.set(nextSubredditIndex, lastT3);
		}

		nextSubredditIndex++;
		if (nextSubredditIndex >= subredditsList.size()) {
			nextSubredditIndex = 0;
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
