package net.acuttone.reddimg;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class UserSubredditsActivity extends Activity {
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
				List<String> subreddits = SubredditsPickerActivity.getSubredditsFromPref(getBaseContext());
				subreddits.add(item);
				SubredditsPickerActivity.saveSubreddits(getBaseContext(), subreddits);
			}
		});
		
		AsyncTask<Void, Void, List<String>> loadTask = new AsyncTask<Void, Void, List<String>>() {

			private ProgressDialog progressDialog;

			@Override
			protected void onPreExecute() {
				progressDialog = ProgressDialog.show(UserSubredditsActivity.this, "Reddimg", "Loading subreddits...");
			}
			
			@Override
			protected List<String> doInBackground(Void... params) {
				List<String> names = ReddimgApp.instance().getRedditClient().getMySubreddits();
				return names;
			}

			@Override
			protected void onPostExecute(List<String> result) {
				SubredditArrayAdapter adapter = new SubredditArrayAdapter(UserSubredditsActivity.this, result, R.drawable.plus);
				listView.setAdapter(adapter);
				progressDialog.dismiss();
				super.onPostExecute(result);
			}
			
		};
		loadTask.execute(null);
	}	
}
