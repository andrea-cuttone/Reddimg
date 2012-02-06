package net.acuttone.reddimg;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SubredditArrayAdapter extends ArrayAdapter<String> {
	private final Activity context;
	private final List<String> subreddits;
	private int drawableId;

	public SubredditArrayAdapter(Activity context, List<String> names, int drawableId) {
		super(context, R.layout.subredditlistview, names);
		this.context = context;
		this.subreddits = names;
		this.drawableId = drawableId;
	}

	@Override
	public void add(String object) {
		if (subreddits.contains(object) == false) {
			subreddits.add(object);
			Collections.sort(subreddits, String.CASE_INSENSITIVE_ORDER);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View itemView = inflater.inflate(R.layout.subredditlistview, null, true);
		TextView textView = (TextView) itemView.findViewById(R.id.subredditlistview_textview);
		textView.setText(subreddits.get(position));
		ImageView imgview = (ImageView) itemView.findViewById(R.id.subredditlistview_imageview);
		imgview.setImageResource(drawableId);
		return itemView;
	}

	public List<String> getSubreddits() {
		return subreddits;
	}
}