package net.acuttone.reddimg;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
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
import android.widget.TextView;
import android.widget.Toast;

public class MyGridView extends Activity {
	GridView gridView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		ReddimgApp.instance().getImagePrefetcher().setStatus(ImagePrefetcherStatus.RUNNING);

		setContentView(R.layout.gallery);
		final List<RedditLink> links = new ArrayList<RedditLink>();

		int startPos = 0;
		int endPos = startPos + 8;
		do {

		} while (ReddimgApp.instance().getLinksQueue().get(endPos) == null);
		final ImageAdapter imageAdapter = new ImageAdapter(this, links);
		for (int i = startPos; i < endPos; i++) {
			links.add(ReddimgApp.instance().getLinksQueue().get(i));
			AsyncTask<Integer, Integer, RedditLink> task = new AsyncTask<Integer, Integer, RedditLink>() {

				@Override
				protected RedditLink doInBackground(Integer... params) {
					RedditLink l = links.get(params[0]);
					if (l.getThumb() == null || l.getThumb().isRecycled()) {
						try {
							HttpURLConnection connection = (HttpURLConnection) new URL(l.getUrl()).openConnection();
							connection.setConnectTimeout(5000);
							Options options = new BitmapFactory.Options();
							options.inSampleSize = 4;
							Bitmap bitmap = BitmapFactory.decodeStream(connection.getInputStream(), null, options);
							int wSize = ReddimgApp.instance().getScreenW() / 2;
							int hSize = ReddimgApp.instance().getScreenH() / 4;
							Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, wSize, hSize, true);
							l.setThumb(scaledBitmap);
							bitmap.recycle();
						} catch (Exception e) {
							Log.e(ReddimgApp.APP_NAME, e.toString());
						}
					}
					return l;
				}

				@Override
				protected void onPostExecute(RedditLink result) {
					super.onPostExecute(result);
					imageAdapter.notifyDataSetChanged();
				}

			};
			task.execute(i);
		}
		RedditLink left = new RedditLink("LEFT", "", "", "", "", "", "", 0, false);
		left.setThumb(BitmapFactory.decodeResource(getResources(), R.drawable.left_arrow));
		links.add(left);
		RedditLink right = new RedditLink("RIGHT", "", "", "", "", "", "", 0, false);
		right.setThumb(BitmapFactory.decodeResource(getResources(), R.drawable.right_arrow));
		links.add(right);

		gridView = (GridView) findViewById(R.id.MyGrid);
		gridView.setAdapter(imageAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Toast.makeText(getApplicationContext(), "" + position, Toast.LENGTH_LONG);
			}
		});
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

			RedditLink link = links.get(position);
			LayoutInflater li = getLayoutInflater();
			view = li.inflate(R.layout.grid_item, null);
			//TextView tv = (TextView) view.findViewById(R.id.grid_item_text);
			//tv.setText(link.getTitle());
			ImageView iv = (ImageView) view.findViewById(R.id.grid_item_image);
			
			/*ImageView iv = new ImageView(context);
			iv.setLayoutParams(new GridView.LayoutParams(85, 85));
			iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
			iv.setPadding(8, 8, 8, 8);
            */
			if (link.getThumb() != null && link.getThumb().isRecycled() == false) {
				iv.setImageBitmap(link.getThumb());
			} else {
				iv.setImageResource(R.drawable.icon);
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