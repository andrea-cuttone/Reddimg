package tst.drd;

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
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class ImageCache {

	private static final int MAX_IMAGE_SIZE = 1000000;
	private static final int IN_MEN_CACHE_SIZE = 3;
	private File reddimgDir;
	private LinkedHashMap<String, Bitmap> inMemCache;
	private Bitmap brokenImg;
	private ImageResizer imgResizer;

	public ImageCache(ImageResizer imgResizer) {
		this.imgResizer = imgResizer;
		
		File sdCard = Environment.getExternalStorageDirectory();
		reddimgDir = new File(sdCard.getAbsolutePath() + "/Reddimg");
		if(reddimgDir.exists() == false) {
			reddimgDir.mkdir();
		}
		inMemCache = new LinkedHashMap<String, Bitmap>();
		brokenImg = Bitmap.createBitmap( 100, 100, Bitmap.Config.ARGB_8888);
	}

	public Bitmap getImage(String url) {		
		Bitmap result = getFromMem(url);

		if (result == null) {			
			result = getFromDisk(url);
		}

		if (result == null) {
			result = getFromWeb(url);
		}

		if(result == null) {
			result = brokenImg;
			Log.w(TstActivity.APP_NAME, url + " could not be dl from web");
		}
		
		return result;
	}

	private Bitmap getFromMem(String url) {
		if (inMemCache.containsKey(url)) {
			Log.d(TstActivity.APP_NAME, url + " found in mem cache");
			// bump the image in the cache
			Bitmap bitmap = inMemCache.get(url);
			inMemCache.remove(url);
			inMemCache.put(url, bitmap);
			return bitmap;
		} else {
			return null;
		}
	}

	private Bitmap getFromDisk(String url) {
		Bitmap result = null;
		File img = new File(reddimgDir, urlToFilename(url));
		if (img.exists()) {
			Log.d(TstActivity.APP_NAME, url + " found on disk cache");
			try {
				FileInputStream is = new FileInputStream(img);
				result = BitmapFactory.decodeStream(is, null, null);
				is.close();
			} catch (FileNotFoundException e) {
				Log.e(TstActivity.APP_NAME, e.toString());
			} catch (IOException e) {
				Log.e(TstActivity.APP_NAME, e.toString());				
			}
		}
		if (result != null) {
			storeInMem(url, result);
			result.recycle();
		}
		return getFromMem(url);
	}

	private void storeInMem(String url, Bitmap bitmap) {
		if (inMemCache.size() >= IN_MEN_CACHE_SIZE) {
			Iterator<Entry<String, Bitmap>> iterator = inMemCache.entrySet().iterator();
			Entry<String, Bitmap> entry = iterator.next();
			entry.getValue().recycle();
			iterator.remove();
			Log.d(TstActivity.APP_NAME, url + " removed from mem cache");
		}
		Bitmap resizedImg = imgResizer.resize(bitmap);
		inMemCache.put(url, resizedImg);
	}

	private Bitmap getFromWeb(String url) {
		try {
			Log.d(TstActivity.APP_NAME, url + " dl from web");
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setConnectTimeout(10000);
			if (connection.getContentLength() < MAX_IMAGE_SIZE) {
				File img = new File(reddimgDir, urlToFilename(url));
				InputStream is = connection.getInputStream();
				OutputStream out = new FileOutputStream(img);
				byte buf[] = new byte[1024];
				int len;
				while ((len = is.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				out.close();
				is.close();
			}
		} catch (MalformedURLException e) {
			Log.e(TstActivity.APP_NAME, e.toString());
		} catch (IOException e) {
			Log.e(TstActivity.APP_NAME, e.toString());
		}

		return getFromDisk(url);
	}
	
	private static String urlToFilename(String url) {
		return url.replaceAll("[\\W&&[^\\.]]+", "_");
	}

}
