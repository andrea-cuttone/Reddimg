package net.acuttone.reddimg.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.acuttone.reddimg.R;
import net.acuttone.reddimg.core.ReddimgApp;
import net.acuttone.reddimg.core.RedditLink;
import net.acuttone.reddimg.prefs.PrefsActivity;
import net.acuttone.reddimg.prefs.SubredditsPickerActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.Interpolator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class GalleryActivity extends Activity implements OnSharedPreferenceChangeListener {
	private static final String CURRENT = "CURRENT";

	private int current;
	private int thumbSize;
	private boolean doReload;
	private List<LoadThumbTask> tasks;
	private int pendingTasks;
	private ImageAdapter imageAdapter;
	private GridView gridView;
	private Button btnLoadMore;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.gallery);
		
		ReddimgApp.instance().getPrefs().registerOnSharedPreferenceChangeListener(this);
		
		tasks = new ArrayList<LoadThumbTask>();
		pendingTasks = 0;
		
		current = savedInstanceState == null ? 0 : savedInstanceState.getInt(CURRENT);
		
		gridView = (GridView) findViewById(R.id.gridview_gallery);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Log.d(ReddimgApp.APP_NAME, "" + position);
				Intent i = new Intent(getApplicationContext(), LinkViewerActivity.class);
				i.putExtra(LinkViewerActivity.LINK_INDEX, position);
				startActivity(i);
			}
		});
		
		thumbSize = ReddimgApp.instance().getScreenW() / 3;
		imageAdapter = new ImageAdapter(getBaseContext());
		gridView.setAdapter(imageAdapter);		
		btnLoadMore = (Button) findViewById(R.id.gridview_loadmore);
		btnLoadMore.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				loadMore();
			}
		});
		ReddimgApp.instance().startLinksQueueTimer();
		loadMore();
	}

	private void loadMore() {
		btnLoadMore.setText("Loading...");
		btnLoadMore.setEnabled(false);
		tasks = new ArrayList<LoadThumbTask>();
		pendingTasks = 12;
		for(int i = 0; i < pendingTasks; i++) {
			LoadThumbTask t = new LoadThumbTask();
			tasks.add(t);
			t.execute(current);
			current++;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(doReload) {
			loadMore();
			doReload = false;
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(CURRENT, current);
		super.onSaveInstanceState(outState);
	}
	
	private class LoadThumbTask extends AsyncTask<Integer, Void, GridItem> {

		@Override
		protected GridItem doInBackground(Integer... arg0) {
				try {
					RedditLink link = null;
					while(link == null) {
						link = ReddimgApp.instance().getLinksQueue().get(arg0[0]);
					}
					Bitmap thumb = ReddimgApp.instance().getImageCache().getImage(link.getThumbUrl());
					Bitmap resized = resizeThumb(thumb);
					if(resized != null) {
						return new GridItem(link, resized);
					} else {
						return null;
					}
				} catch (IOException e) {
					
				}
				return null;
		}
		
		@Override
		protected void onPostExecute(GridItem result) {
			super.onPostExecute(result);
			if(result != null) {
				imageAdapter.addItem(result);
			}
			pendingTasks--;
			if(pendingTasks == 0) {
				btnLoadMore.setText("Load more");
				btnLoadMore.setEnabled(true);
			}
		}
		
		private Bitmap resizeThumb(Bitmap thumbBmp) {
			if (thumbBmp != null && thumbBmp.getWidth() != thumbSize) {
				try {
					Bitmap bmpSquared = thumbBmp;
					if (thumbBmp.getWidth() != thumbBmp.getHeight()) {
						int minDim = Math.min(thumbBmp.getWidth(), thumbBmp.getHeight());
						int x0 = (thumbBmp.getWidth() - minDim) / 2;
						int x1 = (thumbBmp.getWidth() + minDim) / 2;
						int y0 = (thumbBmp.getHeight() - minDim) / 2;
						int y1 = (thumbBmp.getHeight() + minDim) / 2;
						bmpSquared = Bitmap.createBitmap(thumbBmp, x0, y0, x1, y1);
					}
					return Bitmap.createScaledBitmap(bmpSquared, thumbSize, thumbSize, true);
				} catch (Throwable e) {
					Log.w(ReddimgApp.APP_NAME, e.toString());
				}
			}
			return null;
		}
	}

	private class GridItem {
		
		private RedditLink redditLink;
		private Bitmap thumb;
		
		public GridItem(RedditLink link, Bitmap thumb) {
			this.redditLink = link;
			this.thumb = thumb;
		}
		
		public RedditLink getRedditLink() {
			return redditLink;
		}

		public Bitmap getThumb() {
			return thumb;
		}
		
	}
	
	private class ImageAdapter extends BaseAdapter {
		private List<GridItem> items;

		public ImageAdapter(Context context) {
			this.items = new ArrayList<GalleryActivity.GridItem>();
		}

		public void addItem(GridItem itm) {
			items.add(itm);
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView != null) {
				return convertView;
			}
			LayoutInflater li = getLayoutInflater();
			View view = li.inflate(R.layout.grid_item, null);
			ImageView iv = (ImageView) view.findViewById(R.id.grid_item_image);
			Bitmap thumb = items.get(position).getThumb();
			if(thumb != null) {
				iv.setImageBitmap(thumb);
			}
			TextView tv = (TextView) view.findViewById(R.id.grid_item_text);
			String title  = items.get(position).getRedditLink().getTitle();
			if(title.length() > 40) {
				title = title.substring(0, 40) + "...";
			}
			tv.setText(title);
			Animation tranAnim = new TranslateAnimation(0, 0f, 50f, 0f);
			AlphaAnimation alphaAnim = new AlphaAnimation(0.5f, 1f);
			AnimationSet as = new AnimationSet(true);
			as.addAnimation(alphaAnim);
			as.addAnimation(tranAnim);
			as.setDuration(700);
			view.startAnimation(as);
			return view;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.menu_gallery, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem loginItem = menu.findItem(R.id.menuitem_login);
		if(ReddimgApp.instance().getRedditClient().isLoggedIn()) {
			loginItem.setTitle("Logout");
		} else {
			loginItem.setTitle("Login");
		}
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.menuitem_firstpage:
			current = 0;
			ReddimgApp.instance().getLinksQueue().initSubreddits();
			loadMore();
			return true;
		case R.id.menuitem_login:
			if (ReddimgApp.instance().getRedditClient().isLoggedIn()) {
				ReddimgApp.instance().getRedditClient().doLogout();
			} else {
				intent = new Intent(this, LoginActivity.class);
				startActivity(intent);
			}
			return true;
		case R.id.menuitem_settings:
			intent = new Intent(this, PrefsActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ReddimgApp.instance().stopLinksQueueTimer();
		for (LoadThumbTask t : tasks) {
			t.cancel(true);
		}
		ReddimgApp.instance().getPrefs().unregisterOnSharedPreferenceChangeListener(this);    
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (SubredditsPickerActivity.SUBREDDITS_LIST_KEY.equals(key)) {
			current = 0;
			doReload = true;
		}
	}
	
}