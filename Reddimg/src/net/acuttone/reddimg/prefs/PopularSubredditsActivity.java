package net.acuttone.reddimg.prefs;

import java.util.Arrays;
import java.util.List;

import net.acuttone.reddimg.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class PopularSubredditsActivity extends Activity {
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.popularsubreddits);

		listView = (ListView) findViewById(R.id.popular_subreddits_listView);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SubredditArrayAdapter adapter = (SubredditArrayAdapter) listView.getAdapter();
				String item = adapter.getItem((int) id);
				List<String> subreddits = SubredditsPickerActivity.getSubredditsFromPref();
				if(subreddits.contains(item) == false) {
					subreddits.add(item);
					SubredditsPickerActivity.saveSubreddits(subreddits);
					((SubredditArrayAdapter)(listView.getAdapter())).notifyDataSetChanged();
				}
			}
		});
		
		List<String> popularSubreddits = Arrays.asList(getResources().getStringArray(R.array.popular_subreddits));
		SubredditArrayAdapter adapter = new SubredditArrayAdapter(PopularSubredditsActivity.this, popularSubreddits, R.drawable.plus, true);
		listView.setAdapter(adapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(listView != null && listView.getAdapter() != null) {
			((SubredditArrayAdapter)(listView.getAdapter())).notifyDataSetChanged();
		}
	}
	
}
