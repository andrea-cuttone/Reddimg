package net.acuttone.reddimg;

import java.util.HashMap;

import android.app.Application;

public class RedditApplication extends Application {

    private static RedditApplication instance;
	private HashMap<String, Object> map;

    public static RedditApplication getInstance() {
      return instance;
    }

    @Override
    public void onCreate() {
      super.onCreate();  
      instance = this;
      map = new HashMap<String, Object>();
    }

	public HashMap<String, Object> getMap() {
		return map;
	}
    
}