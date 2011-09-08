package tst.drd;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class RedditLinkQueue {

	private List<RedditLink> links;
	private String lastT3;
	private String redditURL;
	private String subreddit;
	
	public RedditLinkQueue() {
		links = new ArrayList<RedditLink>();
		lastT3 = "";
		redditURL = "http://www.reddit.com/.json";
		subreddit = "";
	}
	
	private boolean isUrlValid(String url) {
		return url.matches(".*(gif|jpeg|jpg|png)$");
	}

	public RedditLink at(int index) {
		if(index < 0) {
			return new RedditLink("", "");
		}
		
		if(index >= links.size()) {
			getNewLinks();
		}
		return links.get(index);
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
				String title = (String) cData.get("title");
				lastT3 = (String) cData.get("id");
				Log.d(TstActivity.APP_NAME, "" + count + " [" + lastT3 + "] " + title + " ("
						+ url + ")");
				RedditLink newRedditLink = new RedditLink(url, title);
				if(isUrlValid(url) && links.contains(newRedditLink) == false) {
					links.add(newRedditLink);
				}
				count++;
			}
		} catch (Exception e) {
			Log.e(TstActivity.APP_NAME, e.toString());
		}

	}
	
}
