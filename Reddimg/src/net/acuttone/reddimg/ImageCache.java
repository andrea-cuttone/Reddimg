package net.acuttone.reddimg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;

public class ImageCache {
	private static final int MEGABYTE = 1000000;
	private static final int MAX_IMAGE_SIZE = 1 * MEGABYTE;
	private static final long MAX_INTERNAL_CACHE_SIZE = 2 * MEGABYTE;
	public static final int IN_MEM_CACHE_SIZE = 3;
	private static final String FILE_PREFIX = "__RDIMG_";
	
	private File reddimgDir;
	private LinkedHashMap<String, Bitmap> inMemCache;
	private ImageResizer imgResizer;
	private TreeSet<File> diskCacheFiles;
	private boolean useSD;
	
	public ImageCache(ImageResizer imgResizer, Context context) {
		this.imgResizer = imgResizer;
		initDiskCache(context);
		inMemCache = new LinkedHashMap<String, Bitmap>();
	}

	private void initDiskCache(Context context) {
		reddimgDir = context.getExternalCacheDir();
		if(reddimgDir == null) {
			reddimgDir = context.getCacheDir();
			useSD = false;
		} else {
			useSD = true;
		}
		
		diskCacheFiles = new TreeSet<File>(new Comparator<File>() {

			@Override
			public int compare(File f1, File f2) {
				return (int) (f1.lastModified() - f2.lastModified());
			}
		});
		File[] listFiles = reddimgDir.listFiles();
		for (File f : listFiles) {
			if(f.isFile() && f.getName().startsWith(FILE_PREFIX)) {
				diskCacheFiles.add(f);
			}
		}
		Log.d(ReddimgApp.APP_NAME, "Cache dir : " + reddimgDir.getAbsolutePath());
	}

	public Bitmap getFromMem(String url) {
		synchronized (inMemCache) {
			if (inMemCache.containsKey(url)) {
				Bitmap bitmap = inMemCache.get(url);
				return bitmap;
			} else {
				return null;
			}
		}
	}
	
	public Bitmap prepareImage(String url) {
		Log.d(ReddimgApp.APP_NAME, "Preparing " + url);
		Bitmap result = getFromDisk(url);
		if (result != null) {
			Log.d(ReddimgApp.APP_NAME, url + " found on disk cache");
			return result;
		}
		
		result = getFromWeb(url);
		if(result != null) {
			Log.d(ReddimgApp.APP_NAME, url + " dl from web");
			return result;
		}
		
		Log.w(ReddimgApp.APP_NAME, url + " could not be dl from web");
		return null;
	}
	
	public String getDiskPath(String url) {
		return reddimgDir.getAbsolutePath() + "/" + urlToFilename(url);
	}

	public Bitmap getFromDisk(String url) {
		Bitmap result = null;
		File img = new File(reddimgDir, urlToFilename(url));
		if (img.exists()) {
			try {
				FileInputStream is = new FileInputStream(img);
				// TODO: add downsampling for large imgs
				try {
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inSampleSize = 1;
					result = BitmapFactory.decodeStream(is, null, options);
				} catch(OutOfMemoryError err) {
					Log.w(ReddimgApp.APP_NAME, "ERROR WHILE DECODING " + url + " " + img.length() + " : " + err.toString());
				}
				
				is.close();
			} catch (FileNotFoundException e) {
				Log.e(ReddimgApp.APP_NAME, e.toString());
			} catch (IOException e) {
				Log.e(ReddimgApp.APP_NAME, e.toString());				
			}
		}
		if (result != null) {
			storeInMem(url, result);
			result.recycle();
		}
		return getFromMem(url);
	}

	private Bitmap getFromWeb(String url) {
		HttpURLConnection connection = null;
		InputStream is = null;
		OutputStream out = null;

		try {
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setConnectTimeout(5000);
			int contentLength = connection.getContentLength();
			if (contentLength > MAX_IMAGE_SIZE) {
				Log.w(ReddimgApp.APP_NAME, url + " exceeds max image size");
			} else {
				boolean enoughSpace = false;
				synchronized (diskCacheFiles) {
					enoughSpace = checkDiskCacheSize(contentLength);
				}
				if (!enoughSpace) {
					Log.w(ReddimgApp.APP_NAME, "Insufficient space on disk to store " + url);
				} else {
					File img = new File(reddimgDir, urlToFilename(url));
					is = connection.getInputStream();
					out = new FileOutputStream(img);
					byte buf[] = new byte[1024];
					int len;
					while ((len = is.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					out.close();
					is.close();
					synchronized (diskCacheFiles) {
						diskCacheFiles.add(img);
					}
					return getFromDisk(url);
				}
			}
		} catch (MalformedURLException e) {
			Log.e(ReddimgApp.APP_NAME, e.toString());
		} catch (IOException e) {
			Log.e(ReddimgApp.APP_NAME, e.toString());
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				Log.e(ReddimgApp.APP_NAME, e.toString());
			}
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				Log.e(ReddimgApp.APP_NAME, e.toString());
			}
		}
		return null;
	}
	
	private boolean checkDiskCacheSize(int contentLength) {
		long totSize = contentLength;
		for(File f : diskCacheFiles) {
			totSize += f.length();
		}

		long cacheSize = getCacheSize();
		while(totSize > cacheSize && diskCacheFiles.size() > 0) {
			File oldest = diskCacheFiles.first();
			totSize -= oldest.length();
			diskCacheFiles.remove(oldest);
			if(oldest.exists() && 
			   oldest.getAbsolutePath().contains("net.acuttone.reddimg/cache") && 
			   oldest.isFile() && 
			   oldest.getName().startsWith(FILE_PREFIX)) {
				Log.d(ReddimgApp.APP_NAME, "Deleting from disk " + oldest.getName());
				oldest.delete();
			}
		}
		
		return totSize < cacheSize;
	}

	private long getCacheSize() {		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ReddimgApp.instance());
		if(useSD) {
			int size = Integer.parseInt(sp.getString(PrefsActivity.SD_CACHE_SIZE_KEY, PrefsActivity.DEFAULT_SD_CACHE_SIZE));
			return size * MEGABYTE;
		} else {
			return MAX_INTERNAL_CACHE_SIZE;
		}
	}

	private void storeInMem(String url, Bitmap bitmap) {
		synchronized (inMemCache) {
			if (inMemCache.size() >= IN_MEM_CACHE_SIZE) {
				Iterator<Entry<String, Bitmap>> iterator = inMemCache.entrySet().iterator();
				Entry<String, Bitmap> entry = iterator.next();
				entry.getValue().recycle();
				iterator.remove();
				Log.d(ReddimgApp.APP_NAME, entry.getKey() + " removed from mem cache");
			}
			Bitmap resizedImg = imgResizer.resize(bitmap);
			inMemCache.put(url, resizedImg);
		}
	}
	
	public void clearMemCache() {
		synchronized (inMemCache) {
			for(Iterator<Entry<String, Bitmap>> iterator = inMemCache.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, Bitmap> entry = iterator.next();
				entry.getValue().recycle();
				iterator.remove();
			}
			Log.d(ReddimgApp.APP_NAME, "mem cache cleared");
		}
	}
	
	private static String urlToFilename(String url) {
		url = FILE_PREFIX + url; 
		if(url.length() > 256) {
			url = url.substring(url.length() - 256);
		}
		url = url.replaceAll("[\\W&&[^.]]+", "_");
		return url;
	}

}
