package net.acuttone.reddimg.views;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import net.acuttone.reddimg.R;
import net.acuttone.reddimg.core.ReddimgApp;
import net.acuttone.reddimg.core.RedditClient;
import net.acuttone.reddimg.core.RedditLink;
import net.acuttone.reddimg.prefs.PrefsActivity;
import net.acuttone.reddimg.utils.GifDecoder;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

public class LinkViewerActivity extends Activity {

	private static final String TUTORIAL_SHOWN = "TUTORIAL_SHOWN";
	public static final String LINK_INDEX = "LINK_INDEX";
	private int currentLinkIndex;
	private RedditLink currentLink;
	private ImageSwitcher switcher;
	private TextView textViewTitle;
	private ImageView viewUpvote;
	private ImageView viewDownvote;
	private TextView textviewLoading;
	private AsyncTask<Integer, RedditLink, Object[]> loadTask;
	private AsyncTask<String, Bitmap, Void> loadGifTask;
	private TranslateAnimation animToLeft1;
	private TranslateAnimation animToLeft2;
	private TranslateAnimation animToRight1;
	private TranslateAnimation animToRight2;
	private GestureDetector gestureDetector;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.linkviewer);
		textViewTitle = (TextView) findViewById(R.id.textViewTitle);
		textviewLoading = (TextView) findViewById(R.id.textviewLoading);
		textviewLoading.setText("");
		switcher = (ImageSwitcher) findViewById(R.id.image_switcher);
		switcher.setFactory(new ViewFactory() {
			
			@Override
			public View makeView() {
				ImageView i = new ImageView(LinkViewerActivity.this);  
				i.setScaleType(ImageView.ScaleType.FIT_CENTER);
				i.setAdjustViewBounds(true);
				i.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				return i;
			}
		});
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
		
		if(savedInstanceState != null && savedInstanceState.containsKey(LINK_INDEX)) {
			currentLinkIndex = savedInstanceState.getInt(LINK_INDEX);
		} else {
			currentLinkIndex = getIntent().getExtras().getInt(LINK_INDEX);
		}
		gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
			
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				Intent i = new Intent(getApplicationContext(), FullImageActivity.class);
				String imageDiskPath = ReddimgApp.instance().getImageCache().getImageDiskPath(currentLink.getUrl());
				i.putExtra(FullImageActivity.IMAGE_NAME, imageDiskPath);
				startActivity(i);
				return super.onDoubleTapEvent(e);
			}
			
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				float deltax = e1.getX() - e2.getX();
				float deltay = e1.getY() - e2.getY();
				if (Math.abs(deltax) > 50) {
					if (deltax < 0) {
						currentLinkIndex--;
						switcher.setInAnimation(animToRight2);
						switcher.setOutAnimation(animToRight1);
					} else {
						currentLinkIndex++;
						switcher.setInAnimation(animToLeft1);
						switcher.setOutAnimation(animToLeft2);
					}
					loadImage();
				} else {
					if (Math.abs(deltay) > 50) {
						if (deltay < 0) {
							ReddimgApp.instance().getRedditClient().vote(currentLink, RedditClient.DOWNVOTE);
						} else {
							ReddimgApp.instance().getRedditClient().vote(currentLink, RedditClient.UPVOTE);
						}
						refreshVoteIndicators();
					}
				}
				return super.onFling(e1, e2, velocityX, velocityY);
			}
		});
		
		loadImage();
		
		if(ReddimgApp.instance().getPrefs().getBoolean(TUTORIAL_SHOWN, false) == false) {
			showTutorial();
		}
	}
	
	private void showTutorial() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Tutorial");
		builder.setMessage("Fling left and right to change image\n\nFling up and down to vote\n\nDouble tap to zoom")
				.setCancelable(false).setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Editor editor = ReddimgApp.instance().getPrefs().edit();
						editor.putBoolean(TUTORIAL_SHOWN, true);
						editor.commit();
						dialog.dismiss();
					}
				});
		AlertDialog dlg = builder.create();
		dlg.show();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
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
				textviewLoading.setText("Loading links...");
				viewUpvote.setVisibility(View.GONE);
				viewDownvote.setVisibility(View.GONE);
				if(loadGifTask != null) {
					loadGifTask.cancel(true);
				}
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
					currentLink = (RedditLink) ((Object []) result)[1];
					if(currentLink.getUrl().endsWith("gif")) {
						loadGifTask = new AsyncTask<String, Bitmap, Void>() {
							
							protected void onPreExecute() {
								switcher.setInAnimation(null);
								switcher.setOutAnimation(null);
								refreshVoteIndicators(); 
							}

							@Override
							protected Void doInBackground(String... arg0) {
								try {
									InputStream stream = new BufferedInputStream(new FileInputStream(arg0[0]));
									GifDecoder dec = new GifDecoder();
							        dec.read(stream);
							        final int n = dec.getFrameCount();
							        int i = 0;
							        while(isCancelled() == false) {
								        Bitmap bmp = dec.getFrame(i);
				                        int t = dec.getDelay(i);
				                        publishProgress(bmp);
				                        try {
											Thread.sleep(t);
										} catch (InterruptedException e) {
										}
				                        i++;
				                        if(i == n) {
				                        	i = 0;
				                        }
							        }
								} catch (FileNotFoundException e) {
									
								}
								return null;
							}
							
							protected void onCancelled() {
								switcher.setImageDrawable(null);
							}
							
							protected void onProgressUpdate(Bitmap... values) {
								switcher.setImageDrawable(new BitmapDrawable(values[0]));
								textviewLoading.setText("");
							}
						};
						loadGifTask.execute(ReddimgApp.instance().getImageCache().getImageDiskPath(currentLink.getUrl()));
					} else {
						Bitmap bitmap = (Bitmap) ((Object []) result)[0];
						if(bitmap != null) {
							textviewLoading.setText("");
							switcher.setImageDrawable(new BitmapDrawable(bitmap));
							refreshVoteIndicators(); 
						} else {
							textviewLoading.setText(errorMsg);
						}
					}
				} else {
					textviewLoading.setText(errorMsg);
				}
			}
		};
		loadTask.execute(currentLinkIndex);
	}

	private void refreshVoteIndicators() {
		if(RedditClient.UPVOTE.equals(currentLink.getVoteStatus())) {
			viewUpvote.setVisibility(View.VISIBLE);
			viewDownvote.setVisibility(View.GONE);
		} else if(RedditClient.DOWNVOTE.equals(currentLink.getVoteStatus())) {
			viewUpvote.setVisibility(View.GONE);
			viewDownvote.setVisibility(View.VISIBLE);
		} else {
			viewUpvote.setVisibility(View.GONE);
			viewDownvote.setVisibility(View.GONE);
		}
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
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
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

}
