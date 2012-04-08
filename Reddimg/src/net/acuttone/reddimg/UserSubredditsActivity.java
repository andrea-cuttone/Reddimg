package net.acuttone.reddimg;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

public class UserSubredditsActivity extends Activity implements OnSharedPreferenceChangeListener {
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.usersubreddits);

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
		
		AsyncTask<Void, Void, List<String>> loadTask = new AsyncTask<Void, Void, List<String>>() {

			private ProgressDialog progressDialog;

			@Override
			protected void onPreExecute() {
				progressDialog = ProgressDialog.show(UserSubredditsActivity.this, "Reddimg", "Loading subreddits...");
			}
			
			@Override
			protected List<String> doInBackground(Void... params) {
				List<String> names = ReddimgApp.instance().getRedditClient().getMySubreddits();
				Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
				return names;
			}

			@Override
			protected void onPostExecute(List<String> result) {
				SubredditArrayAdapter adapter = new SubredditArrayAdapter(UserSubredditsActivity.this, result, R.drawable.plus, true);
				listView.setAdapter(adapter);
				progressDialog.dismiss();
				super.onPostExecute(result);
			}
			
		};
		loadTask.execute(null);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(SubredditsPickerActivity.SUBREDDITS_LIST_KEY.equals(key)) {
			((SubredditArrayAdapter)(listView.getAdapter())).notifyDataSetChanged();
		}
	}	
}
