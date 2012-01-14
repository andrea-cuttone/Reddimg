package net.acuttone.reddimg;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class GalleryActivity extends Activity {
	private static final int PICS_PER_PAGE = 12;

	private Bitmap icon;
	private int page;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.gallery);

		icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
		icon = Bitmap.createScaledBitmap(icon, ReddimgApp.instance().getScreenW() / 2, 
				ReddimgApp.instance().getScreenW() / 2, true);
		page = 0;
		
		GridView gridView = (GridView) findViewById(R.id.MyGrid);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Log.d(ReddimgApp.APP_NAME, "" + position);
				if(isLeftButton(position)) {
					page = Math.max(0, page - 1);
					loadLinks();
				} else if(isRightButton(position)) {
					page++;
					loadLinks();
				} else {
					Intent i= new Intent(getApplicationContext(), LinkViewerActivity.class);
					i.putExtra(LinkViewerActivity.LINK_INDEX, page * PICS_PER_PAGE + position);
					startActivity(i);
				}
			}
		});
		
		loadLinks();
	}

	public void loadLinks() {
		final List<RedditLink> links = new ArrayList<RedditLink>();
		final ImageAdapter imageAdapter = new ImageAdapter(GalleryActivity.this, links);
		GridView gridView = (GridView) findViewById(R.id.MyGrid);
		gridView.setAdapter(imageAdapter);
		final ProgressDialog progressDialog = ProgressDialog.show(this, "Reddimg", "Fetching links...");
		AsyncTask<Integer, Void, Void> loadLinksTask = new AsyncTask<Integer, Void, Void>() {

			@Override
			protected Void doInBackground(Integer... params) {
				int page = params[0];
				int startPos = page * PICS_PER_PAGE;
				int endPos = (page + 1) * PICS_PER_PAGE;
				for (int i = startPos; i < endPos; i++) {
					links.add(ReddimgApp.instance().getLinksQueue().get(i));
				}
				// empty items for the arrows
				links.add(null);
				links.add(null);

				publishProgress(null);
				for (RedditLink l : links) {
					if(l != null) {
						l.prepareThumb();
						publishProgress(null);
					}
				}
				return null;
			}

			@Override
			protected void onProgressUpdate(Void... values) {
				super.onProgressUpdate(values);
				if(progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				imageAdapter.notifyDataSetChanged();
			}
		};
		loadLinksTask.execute(page);
	}
	
	public boolean isRightButton(int position) {
		return position == PICS_PER_PAGE + 1;
	}

	public boolean isLeftButton(int position) {
		return position == PICS_PER_PAGE;
	}

	public class ImageAdapter extends BaseAdapter {
		Context context;
		private List<RedditLink> links;

		public ImageAdapter(Context context, List<RedditLink> links) {
			this.context = context;
			this.links = links;
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return links.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			// todo: attempt to reuse convertView
			LayoutInflater li = getLayoutInflater();
			view = li.inflate(R.layout.grid_item, null);
			ImageView iv = (ImageView) view.findViewById(R.id.grid_item_image);
			if (isLeftButton(position)) {
				iv.setImageResource(R.drawable.left_arrow);
			} else if(isRightButton(position)) {
				iv.setImageResource(R.drawable.right_arrow);
			} else {
				RedditLink link = links.get(position);
				if (link.getThumb() != null && link.getThumb().isRecycled() == false) {
					iv.setImageBitmap(link.getThumb());
				} else {
					iv.setImageBitmap(icon);
				}
			}
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
}