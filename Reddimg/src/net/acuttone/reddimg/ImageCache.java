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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

// TODO: split into memcache and diskcache
public class ImageCache {

	private static final int MAX_IMAGE_SIZE = 1000000;
	public static final int IN_MEM_CACHE_SIZE = 3;
	private static final long MAX_SD_CACHE_SIZE = 20971520;
	private static final long MAX_INTERNAL_CACHE_SIZE = 2097152;
	private static final String FILE_PREFIX = "__RDIMG_";
	
	private File reddimgDir;
	private LinkedHashMap<String, Bitmap> inMemCache;
	private ImageResizer imgResizer;
	private long diskCacheSize;
	private TreeSet<File> diskCacheFiles;

	public ImageCache(ImageResizer imgResizer, Context context) {
		this.imgResizer = imgResizer;
		initDiskCache(context);
		inMemCache = new LinkedHashMap<String, Bitmap>();
	}

	private void initDiskCache(Context context) {
		reddimgDir = context.getExternalCacheDir();
		diskCacheSize = MAX_SD_CACHE_SIZE;
		if(reddimgDir == null) {
			reddimgDir = context.getCacheDir();
			diskCacheSize = MAX_INTERNAL_CACHE_SIZE;
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
		Log.d(MainActivity.APP_NAME, "Cache dir : " + reddimgDir.getAbsolutePath() + " [" + diskCacheSize + "]");
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
	
	public boolean prepareImage(String url) {
		Log.d(MainActivity.APP_NAME, "Preparing " + url);
		Bitmap result = getFromDisk(url);
		if (result != null) {
			Log.d(MainActivity.APP_NAME, url + " found on disk cache");
			return true;
		}
		
		result = getFromWeb(url);
		if(result != null) {
			Log.d(MainActivity.APP_NAME, url + " dl from web");
			return true;
		}
		
		Log.w(MainActivity.APP_NAME, url + " could not be dl from web");
		return false;
	}

	private Bitmap getFromDisk(String url) {
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
					Log.w(MainActivity.APP_NAME, "ERROR WHILE DECODING " + url + " " + img.length() + " : " + err.toString());
				}
				
				is.close();
			} catch (FileNotFoundException e) {
				Log.e(MainActivity.APP_NAME, e.toString());
			} catch (IOException e) {
				Log.e(MainActivity.APP_NAME, e.toString());				
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
				Log.w(MainActivity.APP_NAME, url + " exceeds max image size");
			} else {
				boolean enoughSpace = checkDiskCacheSize(contentLength);
				if (!enoughSpace) {
					Log.w(MainActivity.APP_NAME, "Insufficient space on disk to store " + url);
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
					diskCacheFiles.add(img);
					return getFromDisk(url);
				}
			}
		} catch (MalformedURLException e) {
			Log.e(MainActivity.APP_NAME, e.toString());
		} catch (IOException e) {
			Log.e(MainActivity.APP_NAME, e.toString());
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				Log.e(MainActivity.APP_NAME, e.toString());
			}
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				Log.e(MainActivity.APP_NAME, e.toString());
			}
		}
		return null;
	}
	
	private boolean checkDiskCacheSize(int contentLength) {
		long totSize = contentLength;
		for(File f : diskCacheFiles) {
			totSize += f.length();
		}

		while(totSize > diskCacheSize && diskCacheFiles.size() > 0) {
			File oldest = diskCacheFiles.first();
			totSize -= oldest.length();
			diskCacheFiles.remove(oldest);
			if(oldest.exists() && 
			   oldest.getAbsolutePath().contains("net.acuttone.reddimg/cache") && 
			   oldest.isFile() && 
			   oldest.getName().startsWith(FILE_PREFIX)) {
				Log.d(MainActivity.APP_NAME, "Deleting from disk " + oldest.getName());
				oldest.delete();
			}
		}
		
		return totSize < diskCacheSize;
	}

	private void storeInMem(String url, Bitmap bitmap) {
		synchronized (inMemCache) {
			if (inMemCache.size() >= IN_MEM_CACHE_SIZE) {
				Iterator<Entry<String, Bitmap>> iterator = inMemCache.entrySet().iterator();
				Entry<String, Bitmap> entry = iterator.next();
				entry.getValue().recycle();
				iterator.remove();
				Log.d(MainActivity.APP_NAME, entry.getKey() + " removed from mem cache");
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
			Log.d(MainActivity.APP_NAME, "mem cache cleared");
		}
	}
	
	private static String urlToFilename(String url) {
		url = FILE_PREFIX + url; 
		if(url.length() > 256) {
			url = url.substring(url.length() - 256);
		}
		url = url.replaceAll("[\\W]+", "_");
		return url;
	}

}
