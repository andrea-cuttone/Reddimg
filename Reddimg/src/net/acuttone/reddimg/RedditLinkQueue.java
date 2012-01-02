package net.acuttone.reddimg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;

public class RedditLinkQueue {

	private List<RedditLink> links;
	private List<String> lastT3List;
	private List<String> subredditsList;
	private int lastRequestedIndex;
	
	public RedditLinkQueue() {
		initSubreddits();
	}
	
	public synchronized void initSubreddits() {
		links = new ArrayList<RedditLink>();
		lastT3List = new ArrayList<String>();		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(RedditApplication.instance());
		String mode = sp.getString(PrefsActivity.SUBREDDIT_MODE_KEY, PrefsActivity.SUBREDDITMODE_FRONTPAGE);
		if(PrefsActivity.SUBREDDITMODE_MINE.equals(mode)) {
			// TODO
		} else if(PrefsActivity.SUBREDDITMODE_MANUAL.equals(mode)) {
			subredditsList = SubredditsPickerActivity.getSubredditsFromPref(RedditApplication.instance());
		} else {
			subredditsList = new ArrayList<String>();
			subredditsList.add("");
		} 
		for (String s : subredditsList) {
			lastT3List.add("");
		}
		lastRequestedIndex = 0;
	}

	public synchronized RedditLink get(int index) {
		lastRequestedIndex = index;
		if(index >= links.size()) {
			return null;
		} else {
			return links.get(index);
		}
	}
	
	public RedditLink getForPrefetch(int index) {
		// links does not need synch here since it is accessed only by the prefetching thread
		if(index >= links.size()) {
			getNewLinks();
		}
		return index >= links.size() ? null : links.get(index);
	}

	private void getNewLinks() {
		List<RedditLink> newLinks = new ArrayList<RedditLink>();
		for (int i = 0; i < subredditsList.size(); i++) {
			String subreddit = subredditsList.get(i);
			String lastT3 = lastT3List.get(i);
			Log.d(RedditApplication.APP_NAME, "Fetching links from " + (subreddit.length() == 0 ? "reddit front page" : subreddit));
			BufferedReader in = null;
			try {
				URLConnection connection = new URL("http://www.reddit.com/" + subreddit + "/.json" + "?after=t3_" + lastT3).openConnection();
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				StringBuilder sb = new StringBuilder();
				while ((inputLine = in.readLine()) != null)
					sb.append(inputLine);
				in.close();

				JSONObject jsonObject = new JSONObject(sb.toString());
				JSONObject data = (JSONObject) jsonObject.get("data");
				JSONArray children = (JSONArray) data.get("children");
				for (int j = 0; j < children.length(); j++) {
					JSONObject obj = (JSONObject) children.get(j);
					JSONObject cData = (JSONObject) obj.get("data");
					String url = (String) cData.get("url");
					String title = Html.fromHtml((String) cData.get("title")).toString();
					lastT3 = (String) cData.get("id");
					if (isUrlValid(url)) {
						RedditLink newRedditLink = new RedditLink(lastT3, url, title);
						newLinks.add(newRedditLink);
						Log.d(RedditApplication.APP_NAME, " [" + lastT3 + "] " + title + " (" + url + ")");
					}
				}
			} catch (Exception e) {
				Log.e(RedditApplication.APP_NAME, e.toString());
			} finally {
				if(in != null) {
					try {
						in.close();
					} catch (IOException e) {
						Log.e(RedditApplication.APP_NAME, e.toString());
					}
				}
			}
			if(lastT3 != null && !lastT3.equals("")) {
				lastT3List.set(i, lastT3);
			}
		}

		Collections.shuffle(newLinks);

		synchronized (links) {
			for (RedditLink l : newLinks) {
				if (links.contains(l) == false) {
					links.add(l);
				}
			}
		}
	}

	public synchronized int getLastRequestedIndex() {
		return lastRequestedIndex;
	}

	public synchronized void removeUrl(String targetUrl) {
		for(Iterator<RedditLink> iter = links.iterator(); iter.hasNext();) {
			if(iter.next().getUrl().equals(targetUrl)) {
				iter.remove();
				break;
			}
		}
	}
	
	private static boolean isUrlValid(String url) {
		return url.matches(".*(gif|jpeg|jpg|png)$");
	}
	
}
