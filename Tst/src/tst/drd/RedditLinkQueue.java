package tst.drd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

public class RedditLinkQueue {

	private List<RedditLink> links;
	
	public RedditLinkQueue() {
		links = new ArrayList<RedditLink>();
		getNewLinks();
		pruneNonImages();
	}
	
	private void pruneNonImages() {
		for(Iterator<RedditLink> iter = links.iterator(); iter.hasNext();) {
			if(iter.next().getUrl().matches(".*(gif|jpeg|jpg|png)$") == false) {
				iter.remove();
			}
		}
		
	}

	public RedditLink at(int index) {
		return links.get(Math.abs(index % (links.size() - 1)));
	}
	
	private void getNewLinks() {
		String redditURL = "http://www.reddit.com/.json";
		String subreddit = "";
		String lastT3 = "";
		int count = 0;

		try {
			URLConnection connection = new URL(redditURL + subreddit
					+ "?after=t3_" + lastT3).openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
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
				String title = (String) cData.get("title");
				lastT3 = (String) cData.get("id");
				Log.d(TstActivity.APP_NAME, "" + count + " [" + lastT3 + "] " + title + " ("
						+ url + ")");
				links.add(new RedditLink(url, title));
				count++;
			}
		} catch (Exception e) {
			Log.e(TstActivity.APP_NAME, e.toString());
		}

	}
	
}
