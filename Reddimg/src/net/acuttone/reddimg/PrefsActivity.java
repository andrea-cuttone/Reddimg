package net.acuttone.reddimg;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class PrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	public static final String SUBREDDIT_MODE_KEY = "subredditMode";
	public final static String SUBREDDITMODE_FRONTPAGE = "Front page";
	public final static String SUBREDDITMODE_MINE = "My subreddits (only logged users)";
	public final static String SUBREDDITMODE_MANUAL = "Manual selection";
    
	private ListPreference subredditModePref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		
		subredditModePref = (ListPreference) findPreference(SUBREDDIT_MODE_KEY);
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        String str = getPreferences(MODE_PRIVATE).getString(SUBREDDIT_MODE_KEY, SUBREDDITMODE_FRONTPAGE);
        subredditModePref.setSummary(str);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
    }
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	    Preference pref = findPreference(key);

	    if (pref instanceof ListPreference) {
	        ListPreference listPref = (ListPreference) pref;
	        pref.setSummary(listPref.getEntry());
	    }
	}

}
