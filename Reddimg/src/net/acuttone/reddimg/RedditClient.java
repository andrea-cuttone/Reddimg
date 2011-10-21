package net.acuttone.reddimg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

// TODO: encrypt ALL the things
public class RedditClient {

	private static final String UH_KEY = "UH";

	public final static String UPVOTE = "1";
	public final static String DOWNVOTE = "-1";
	public final static String RESCIND = "0";
	
	private HttpClient httpclient;
	private String uh;
	private HttpContext localContext;
	private File cookiesCacheDir;
	
	public RedditClient(File cacheDir) {
		httpclient = new DefaultHttpClient();
		CookieStore cookieStore = new BasicCookieStore();
		localContext = new BasicHttpContext();
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		this.cookiesCacheDir = new File(cacheDir, "cookies");
		if (cookiesCacheDir.exists()) {
			for (File cookieFile : cookiesCacheDir.listFiles()) {
				FileInputStream fis = null;
				ObjectInputStream in = null;
				try {
					fis = new FileInputStream(cookieFile);
					in = new ObjectInputStream(fis);
					SerializableCookie sc = (SerializableCookie) in.readObject();
					Cookie sessionCookie = SerializableCookie.toBasicClientCookie(sc);
					cookieStore.addCookie(sessionCookie);
					Log.d(RedditApplication.APP_NAME, "session cookie read successfully");
				} catch (Exception ex) {
					Log.e(RedditApplication.APP_NAME, "error loading cookie: " + ex.toString());
				} finally {
					try {
						in.close();
					} catch (IOException ex) {
						Log.e(RedditApplication.APP_NAME, "error loading cookie: " + ex.toString());
					}
				}
			}
		} else {
			Log.d(RedditApplication.APP_NAME, "no session cookies found");
			cookiesCacheDir.mkdir();
		}

		uh = RedditApplication.instance().getSharedPrefs().getString(UH_KEY, "");
		if (!"".equals(uh)) {
			Log.d(RedditApplication.APP_NAME, "UH found");
		} else {
			Log.d(RedditApplication.APP_NAME, "UH not found");
		}
	}

	public boolean doLogin(String username, String password) {
		boolean success = true;

		HttpPost httppost = new HttpPost("http://www.reddit.com/api/login/" + username);
		CookieStore cookieStore = (CookieStore) localContext.getAttribute(ClientContext.COOKIE_STORE);

		// TODO: perform logout
		cookieStore.clear();
		for (File cookieFile : cookiesCacheDir.listFiles()) {
			cookieFile.delete();
		}
		
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
			if (errors.length() == 0) {
				JSONObject data = (JSONObject) jo.get("data");
				uh = (String) data.get("modhash");
				saveLoginInfo(cookieStore.getCookies());
			} else {
				success = false;
			}
		} catch (Exception exc) {
			Log.e(RedditApplication.APP_NAME, "error while logging in: " + exc.toString());
			success = false;
		}

		return success;
	}
	
	private void saveLoginInfo(List<Cookie> cookies) {
		// save uh
		SharedPreferences sharedPrefs = RedditApplication.instance().getSharedPrefs();
		Editor edit = sharedPrefs.edit();
		edit.putString(UH_KEY, uh);
		edit.commit();

		// save cookies
		for (int i = 0; i < cookies.size(); i++) {
			BasicClientCookie c = (BasicClientCookie) cookies.get(i);
			File cookieFile = new File(cookiesCacheDir, "sessioncookie" + i);
			FileOutputStream fos = null;
			ObjectOutputStream out = null;
			try {
				fos = new FileOutputStream(cookieFile);
				out = new ObjectOutputStream(fos);
				out.writeObject(new SerializableCookie(c));
			} catch (IOException ex) {
				Log.e(RedditApplication.APP_NAME, "Error while writing cookie: " + ex.toString());
			} finally {
				try {
					out.close();
				} catch (IOException ex) {
					Log.e(RedditApplication.APP_NAME, "Error while writing cookie: " + ex.toString());
				}
			}
		}
	}

	public boolean vote(String id, String vote) {
		if (httpclient == null || localContext == null || "".equals(uh)) {
			Log.e(RedditApplication.APP_NAME, "error on vote");
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
			boolean success = result.equals("{}");
			if(success == false) {
				Log.e(RedditApplication.APP_NAME, "error on vote");
			}
			return success;
		} catch (Exception exc) {
			Log.e(RedditApplication.APP_NAME, "error on vote: " + exc.toString());
			return false;
		}
	}

	private static class SerializableCookie implements Serializable {

		private static final String EXPIRES_KEY = "expires";

		private static final long serialVersionUID = 1L;

		private String domain;
		private String value;
		private String name;
		private String comment;
		private Date expiryDate;
		private String path;

		private String expiryDateString;

		public SerializableCookie(BasicClientCookie cookie){
			this.domain = cookie.getDomain();
			this.value =  cookie.getValue(); //new URLDecoder().decode(cookie.getValue());
			this.name = cookie.getName();
			this.comment = cookie.getComment();
			this.expiryDate = cookie.getExpiryDate();
			this.path = cookie.getPath();
			this.expiryDateString = cookie.getAttribute(EXPIRES_KEY);
		}

		public static BasicClientCookie toBasicClientCookie(SerializableCookie serializable) {
			BasicClientCookie basicClientCookie = new BasicClientCookie(serializable.getName(), serializable.getValue());
			basicClientCookie.setDomain(serializable.getDomain());
			basicClientCookie.setComment(serializable.getComment());
			basicClientCookie.setExpiryDate(serializable.getExpiryDate());
			basicClientCookie.setPath(serializable.getPath());
			basicClientCookie.setAttribute(BasicClientCookie.DOMAIN_ATTR, serializable.getDomain());
			basicClientCookie.setAttribute(BasicClientCookie.PATH_ATTR, serializable.getPath());
			basicClientCookie.setAttribute(EXPIRES_KEY, serializable.getExpiryDateString());
			return basicClientCookie;
		}

		public String getDomain() {
			return domain;
		}

		public String getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public String getComment() {
			return comment;
		}

		public Date getExpiryDate() {
			return expiryDate;
		}

		public String getPath() {
			return path;
		}

		public String getExpiryDateString() {
			return expiryDateString;
		}
		
	}

}
