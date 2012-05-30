package net.acuttone.reddimg.prefs;

import net.acuttone.reddimg.R;
import net.acuttone.reddimg.core.ReddimgApp;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.Window;
import android.view.WindowManager;

public class PrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	public static final String DEFAULT_CACHE_SIZE = "10";
	public final static String CACHE_SIZE_KEY = "cache_size";
	public static final String TITLE_SIZE_KEY = "titleSize";
    
	private ListPreference titleSizePref;
	private Preference clearCachePref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);		
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		titleSizePref = (ListPreference) findPreference(TITLE_SIZE_KEY);
		clearCachePref = (Preference) findPreference("pref_clear_cache");
		updateCurrentCacheSizeText();
		clearCachePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				ReddimgApp.instance().getImageCache().clearCache();
				updateCurrentCacheSizeText();
				return true;
			}

		});
	}

	private void updateCurrentCacheSizeText() {
		String text = "Current size: ";
		text += String.format("%.1f", ReddimgApp.instance().getImageCache().getCurrentCacheSize());
		text += " MB";
		clearCachePref.setSummary(text);
	}
	
	@Override
    protected void onResume() {
        super.onResume();
		titleSizePref.setSummary(titleSizePref.getEntry());
        ReddimgApp.instance().getPrefs().registerOnSharedPreferenceChangeListener(this);
    }
	
	@Override
    protected void onPause() {
        super.onPause();
        ReddimgApp.instance().getPrefs().unregisterOnSharedPreferenceChangeListener(this);    
    }
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Preference pref = findPreference(key);

		if (CACHE_SIZE_KEY.equals(key)) {
			int size = Integer.parseInt(sharedPreferences.getString(key,
					DEFAULT_CACHE_SIZE));
			if (size < 1) {
				Editor editor = sharedPreferences.edit();
				editor.putString(key, "1");
				editor.commit();
			}
		}

		if (pref instanceof ListPreference) {
			ListPreference listPref = (ListPreference) pref;
			pref.setSummary(listPref.getEntry());
		}
	}

}
