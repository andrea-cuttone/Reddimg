package net.acuttone.reddimg;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.text.Html;
import android.util.Log;

public class RedditLinkQueue {

	private List<RedditLink> links;
	private String lastT3;
	private String redditURL;
	private String subreddit;
	private int lastRequestedIndex = 0;
	
	public RedditLinkQueue() {
		links = new ArrayList<RedditLink>();
		lastT3 = "";
		redditURL = "http://www.reddit.com/.json";
		subreddit = "";
	}
	
	private boolean isUrlValid(String url) {
		return url.matches(".*(gif|jpeg|jpg|png)$");
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
		int count = 0;

		try {
			URLConnection connection = new URL(redditURL + subreddit
					+ "?after=t3_" + lastT3).openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuilder sb = new StringBuilder();
			while ((inputLine = in.readLine()) != null)
				sb.append(inputLine);
			in.close();

			JSONObject jsonObject = new JSONObject(sb.toString());
			JSONObject data = (JSONObject) jsonObject.get("data");
			JSONArray children = (JSONArray) data.get("children");
			for (int i = 0; i < children.length(); i++) {
				JSONObject obj = (JSONObject) children.get(i);
				JSONObject cData = (JSONObject) obj.get("data");
				String url = (String) cData.get("url");
				String title = Html.fromHtml((String) cData.get("title")).toString();
				lastT3 = (String) cData.get("id");
				Log.d(RedditApplication.APP_NAME, "" + count + " [" + lastT3 + "] " + title + " ("
						+ url + ")");
				RedditLink newRedditLink = new RedditLink(lastT3, url, title);
				synchronized (links) {
					if(isUrlValid(url) && links.contains(newRedditLink) == false) {
						links.add(newRedditLink);
					}
				}
				count++;
			}
		} catch (Exception e) {
			Log.e(RedditApplication.APP_NAME, e.toString());
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
	
}
