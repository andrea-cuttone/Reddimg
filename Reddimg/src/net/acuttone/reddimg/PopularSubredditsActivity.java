package net.acuttone.reddimg;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class PopularSubredditsActivity extends Activity implements OnSharedPreferenceChangeListener {
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.popularsubreddits);

		listView = (ListView) findViewById(R.id.usersubreddits_listView);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SubredditArrayAdapter adapter = (SubredditArrayAdapter) listView.getAdapter();
				String item = adapter.getItem((int) id);
				List<String> subreddits = SubredditsPickerActivity.getSubredditsFromPref();
				if(subreddits.contains(item) == false) {
					subreddits.add(item);
					SubredditsPickerActivity.saveSubreddits(subreddits);
				}
			}
		});
		
		ReddimgApp.instance().getPrefs().registerOnSharedPreferenceChangeListener(this);
		
		List<String> popularSubreddits = Arrays.asList(getResources().getStringArray(R.array.popular_subreddits));
		SubredditArrayAdapter adapter = new SubredditArrayAdapter(PopularSubredditsActivity.this, popularSubreddits, R.drawable.plus, true);
		listView.setAdapter(adapter);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(SubredditsPickerActivity.SUBREDDITS_LIST_KEY.equals(key)) {
			((SubredditArrayAdapter)(listView.getAdapter())).notifyDataSetChanged();
		}
	}	
}
