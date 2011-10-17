package net.acuttone.reddimg;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class RedditClient {

	public final static String UPVOTE = "1";
	public final static String DOWNVOTE = "-1";
	public final static String RESCIND = "0";
	
	private HttpClient httpclient;
	private String uh;
	private HttpContext localContext;

	public boolean doLogin(String username, String password) {
		boolean success = true;

		httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://www.reddit.com/api/login/" + username);
		CookieStore cookieStore = new BasicCookieStore();
		localContext = new BasicHttpContext();
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
			nameValuePairs.add(new BasicNameValuePair("user", username));
			nameValuePairs.add(new BasicNameValuePair("passwd", password));
			nameValuePairs.add(new BasicNameValuePair("api_type", "json"));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = httpclient.execute(httppost, localContext);
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity);

			JSONObject jsonObject = new JSONObject(result);
			JSONObject jo = (JSONObject) jsonObject.get("json");
			JSONArray errors = (JSONArray) jo.get("errors");
			if (errors.length() > 0) {
				success = false;
			}
			
			JSONObject data = (JSONObject) jo.get("data");
			uh = (String) data.get("modhash");
			
			List<Cookie> cookies = cookieStore.getCookies();
			for (int i = 0; i < cookies.size(); i++) {
				System.out.println("New cookie: " + cookies.get(i));
			}
		} catch (Exception exc) {
			System.out.println(exc.toString());
			httpclient = null;
			localContext = null;
			uh = null;
			success = false;
		}

		return success;
	}
	
	public boolean vote(String id, String vote) {
		if (httpclient == null || localContext == null || uh == null) {
			return false;
		}
		
		String result = "";

		try {
			HttpPost httppost = new HttpPost("http://www.reddit.com/api/vote");
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
			nameValuePairs.add(new BasicNameValuePair("id", "t3_" + id));
			nameValuePairs.add(new BasicNameValuePair("dir", vote));
			nameValuePairs.add(new BasicNameValuePair("uh", uh));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost, localContext);
			HttpEntity entity = response.getEntity();
			result = EntityUtils.toString(entity);
			System.out.println(result);
			return result.equals("{}");
		} catch (Exception e) {
			System.out.println(e.toString());
			return false;
		}
	}
	
	/*private String parseEntity(HttpEntity entity) throws IOException {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		try {
			InputStream content = entity.getContent();
			br = new BufferedReader(new InputStreamReader(content));
			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				sb.append(inputLine);
			}
		} finally {
			EntityUtils.consume(entity);
			if(br != null) {
				br.close();
			}
		}
		return sb.toString();
	}*/
	
}
