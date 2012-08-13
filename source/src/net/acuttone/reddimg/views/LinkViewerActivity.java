package net.acuttone.reddimg.views;

import java.io.IOException;

import net.acuttone.reddimg.R;
import net.acuttone.reddimg.core.ReddimgApp;
import net.acuttone.reddimg.core.RedditClient;
import net.acuttone.reddimg.core.RedditLink;
import net.acuttone.reddimg.prefs.PrefsActivity;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

public class LinkViewerActivity extends Activity implements ViewFactory {

	public static final String LINK_INDEX = "LINK_INDEX";
	private int currentLinkIndex;
	private ImageSwitcher switcher;
	private ImageView viewLeftArrow;
	private ImageView viewRightArrow;
	private TextView textViewTitle;
	private AsyncTask<Void,Integer,Void> fadeTask;
	private ImageView viewUpvote;
	private ImageView viewDownvote;
	private TextView textviewLoading;
	private AsyncTask<Integer, RedditLink, Object[]> loadTask;
	private TranslateAnimation animToLeft1;
	private TranslateAnimation animToLeft2;
	private TranslateAnimation animToRight1;
	private TranslateAnimation animToRight2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.linkviewer);
		textViewTitle = (TextView) findViewById(R.id.textViewTitle);
		textviewLoading = (TextView) findViewById(R.id.textviewLoading);
		textviewLoading.setText("");
		switcher = (ImageSwitcher) findViewById(R.id.scrollViewLink).findViewById(R.id.image_switcher);
		switcher.setFactory(this);
		animToLeft1 = new TranslateAnimation(1000, 0, 0, 0);
		animToLeft1.setDuration(500);
		animToLeft2 = new TranslateAnimation(0, -1000, 0, 0);
		animToLeft2.setDuration(500);
		animToRight1 = new TranslateAnimation(0, 1000, 0, 0);
		animToRight1.setDuration(500);
		animToRight2 = new TranslateAnimation(-1000, 0, 0, 0);
		animToRight2.setDuration(500);
		viewUpvote = (ImageView) findViewById(R.id.imageupvote);
		viewUpvote.setVisibility(View.GONE);
		viewDownvote = (ImageView) findViewById(R.id.imagedownvote);
		viewDownvote.setVisibility(View.GONE);
		viewLeftArrow = (ImageView) findViewById(R.id.imageleftarrow);
		viewRightArrow = (ImageView) findViewById(R.id.imagerightarrow);
		viewLeftArrow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(currentLinkIndex > 0) {
					currentLinkIndex--;
					switcher.setInAnimation(animToRight2);
					switcher.setOutAnimation(animToRight1);  
					loadImage();
				}
			}

		});
		viewRightArrow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				currentLinkIndex++;
				switcher.setInAnimation(animToLeft1);  
				switcher.setOutAnimation(animToLeft2);
				loadImage();
			}
		});
		ScrollView scrollView = (ScrollView) findViewById(R.id.scrollViewLink);
		scrollView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				fadeArrows();
				return false;
			}
		});
		
		if(savedInstanceState != null && savedInstanceState.containsKey(LINK_INDEX)) {
			currentLinkIndex = savedInstanceState.getInt(LINK_INDEX);
		} else {
			currentLinkIndex = getIntent().getExtras().getInt(LINK_INDEX);
		}
		loadImage();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(LINK_INDEX, currentLinkIndex);
	}
	
	private void loadImage() {
		if(loadTask != null) {
			loadTask.cancel(true);
		}
		loadTask = new AsyncTask<Integer, RedditLink, Object[]>() {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				if(fadeTask != null) {
					fadeTask.cancel(true);
				}
				textviewLoading.setText("Loading links...");
				viewLeftArrow.setAlpha(0);
				viewRightArrow.setAlpha(0);
				viewUpvote.setVisibility(View.GONE);
				viewDownvote.setVisibility(View.GONE);
				recycleBitmap();
			}

			@Override
			protected Object[] doInBackground(Integer... params) {
				RedditLink redditLink = null;
				try {
					redditLink = ReddimgApp.instance().getLinksQueue().get(params[0]);
				} catch (IOException e) {
					Log.e(ReddimgApp.APP_NAME, e.toString());
				}
				if(redditLink != null) {
					publishProgress(redditLink);
					Bitmap bitmap = ReddimgApp.instance().getImageCache().getImage(redditLink.getUrl());
					Object [] result = new Object[2];
					result[0] = bitmap;
					result[1] = redditLink;
					return result;
				} else {
					return null;
				}
			}
			
			@Override
			protected void onProgressUpdate(RedditLink... values) {
				super.onProgressUpdate(values);
				RedditLink link = values[0];
				updateTitle(link);
				textviewLoading.setText("Loading " + link.getUrl());
			}

			@Override
			protected void onPostExecute(Object[] result) {
				super.onPostExecute(result);
				final String errorMsg = "Error loading link (no connection?)";
				if(result != null) {
					Bitmap bitmap = (Bitmap) ((Object []) result)[0];
					if(bitmap != null) {
						textviewLoading.setText("");
						RedditLink redditLink = (RedditLink) ((Object []) result)[1];
						switcher.setImageDrawable(new BitmapDrawable(bitmap));
						refreshVoteIndicators(redditLink); 
						fadeArrows();
					} else {
						textviewLoading.setText(errorMsg);
					}
				} else {
					textviewLoading.setText(errorMsg);
				}
			}
		};
		loadTask.execute(currentLinkIndex);
	}

	private void fadeArrows() {
		if (fadeTask == null || fadeTask.isCancelled() || fadeTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
			fadeTask = new AsyncTask<Void, Integer, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					for (int alpha = 256; alpha >= 0 && isCancelled() == false; alpha -= 4) {
						try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
						}
						publishProgress(alpha);
					}
					return null;
				}

				@Override
				protected void onProgressUpdate(Integer... values) {
					if (isCancelled() == false) {
						viewLeftArrow.setAlpha(values[0]);
						viewRightArrow.setAlpha(values[0]);
					}
					super.onProgressUpdate(values);
				}

			};
			fadeTask.execute(null);
		}
	}

	private void refreshVoteIndicators(RedditLink redditLink) {
		if(RedditClient.UPVOTE.equals(redditLink.getVoteStatus())) {
			viewUpvote.setVisibility(View.VISIBLE);
			viewDownvote.setVisibility(View.GONE);
		} else if(RedditClient.DOWNVOTE.equals(redditLink.getVoteStatus())) {
			viewUpvote.setVisibility(View.GONE);
			viewDownvote.setVisibility(View.VISIBLE);
		} else {
			viewUpvote.setVisibility(View.GONE);
			viewDownvote.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		recycleBitmap();
		
		if(fadeTask != null) {
			fadeTask.cancel(true);
		}
	}
	
	private void recycleBitmap() {
		/*Drawable drawable = switcher.get.getDrawable();
		if (drawable instanceof BitmapDrawable) {
		    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
		    Bitmap bitmap = bitmapDrawable.getBitmap();
		    switcher.setImageBitmap(null);
		    if(bitmap != null && bitmap.isRecycled() == false) {
		    	bitmap.recycle();
		    }
		}*/
	}

	public void updateTitle(RedditLink link) {
		StringBuilder sb = new StringBuilder();
		SharedPreferences sp = ReddimgApp.instance().getPrefs();
		if(sp.getBoolean("showScore", false)) {
			sb.append("[" + link.getScore() + "] ");
		}
		sb.append(link.getTitle());
		if(sp.getBoolean("showAuthor", false)) {
			sb.append(" | by " + link.getAuthor());
		}		
		if(sp.getBoolean("showSubreddit", false)) {
			sb.append(" in " + link.getSubreddit());
		}
		textViewTitle.setText(sb.toString());
		int size = Integer.parseInt(sp.getString(PrefsActivity.TITLE_SIZE_KEY, "24"));
		textViewTitle.setTextSize(size);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.menu_linkviewer, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem upvoteItem = menu.findItem(R.id.menuitem_upvote);
		MenuItem downvoteItem = menu.findItem(R.id.menuitem_downvote);
		if(ReddimgApp.instance().getRedditClient().isLoggedIn()) {
			upvoteItem.setEnabled(true);
			downvoteItem.setEnabled(true);
		} else {
			upvoteItem.setEnabled(false);
			downvoteItem.setEnabled(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		RedditLink currentLink = null;
		try {
			currentLink = ReddimgApp.instance().getLinksQueue().get(currentLinkIndex);
		} catch (IOException e) {
			Log.e(ReddimgApp.APP_NAME, e.toString());
		}
		switch (item.getItemId()) {
		case R.id.menuitem_upvote:
			ReddimgApp.instance().getRedditClient().vote(currentLink, RedditClient.UPVOTE);
			refreshVoteIndicators(currentLink);
			return true;
		case R.id.menuitem_downvote:
			ReddimgApp.instance().getRedditClient().vote(currentLink, RedditClient.DOWNVOTE);
			refreshVoteIndicators(currentLink);
			return true;
		case R.id.menuitem_openimg:
			String imageDiskPath = ReddimgApp.instance().getImageCache().getImageDiskPath(currentLink.getUrl());
			Uri uri = Uri.parse("file://" + imageDiskPath);
			intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(uri, "image/*");
			startActivity(intent);
			return true;
		case R.id.menuitem_opencomments:
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentLink.getCommentUrl() + ".compact"));
			startActivity(intent);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public View makeView() {
		ImageView i = new ImageView(this);  
		i.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		i.setAdjustViewBounds(true);
		return i;
	}
}
