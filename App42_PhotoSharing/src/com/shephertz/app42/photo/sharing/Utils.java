package com.shephertz.app42.photo.sharing;

import java.io.File;
import java.io.InputStream;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.widget.ImageView;

public class Utils {
	
	/*
	 * Used to check user is autenticated with facebook or not
	 */
	public static boolean isAuthenticated() {
		return (FacebookService.instance().isFacebookSessionValid() && UserContext.MyUserName
				.length() > 0);
	}
	
/*
 * used to load image from Url in a background Thraed
 *  @param image ImageView on which image is loaded
 *  @param url of image
 */
	public static void loadImageFromUrl(final ImageView image, final String url) {
		final Handler callerThreadHandler = new Handler();
		new Thread() {
			@Override
			public void run() {
				final Bitmap bitmap = loadBitmap(url);
				callerThreadHandler.post(new Runnable() {
					@Override
					public void run() {
						if (bitmap != null) {
							image.setImageBitmap(bitmap);
						}
					}
				});
			}
		}.start();
	}
/*
 * Used to load bitmap from url
 * @param url of image
 * @return Bitmap 
 */
	public static Bitmap loadBitmap(String url) {
		Bitmap bitmap = null;
		try {
			InputStream in = new java.net.URL(url).openStream();
			bitmap = BitmapFactory.decodeStream(in);
			in.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	/*
	 * Used to load image in preferred size given by user
	 * Used to do sampling of image in desired size
	 * @param image ImageView on which image is loaded
	 * @param url of image
	 * @param size size of image(same width and height)
	 */
	public static void loadImageGridFromUrl(final ImageView image,
			final String url, final int size) {
		final Handler callerThreadHandler = new Handler();
		new Thread() {
			@Override
			public void run() {
				final Bitmap bitmap = loadGridBitmap(url, size);
				callerThreadHandler.post(new Runnable() {
					@Override
					public void run() {
						if (bitmap != null) {
							image.setImageBitmap(bitmap);
						}
					}
				});
			}
		}.start();
	}
	
/*
 * used to load bitmap with desired size of image.
 * This method first calculate the size of URL image 
 * Than re sample it with the size given by user
 * @param url of image
 * @size size of image
 * 
 */
	public static Bitmap loadGridBitmap(String url, int size) {
		Bitmap bitmap = null;
		try {
			InputStream in = new java.net.URL(url).openStream();
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(in, null, o);
			in.close();
			int sampleSize = 1;
			//intially sample size is 1 as defualt
			//calculate it
			if (o.outHeight > size || o.outWidth > size) {
				sampleSize = (int) Math.pow(
						2,
						(int) Math.round(Math.log(size
								/ (double) Math.max(o.outHeight, o.outWidth))
								/ Math.log(0.5)));
			}
			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = sampleSize;
			in = new java.net.URL(url).openStream();
			bitmap = BitmapFactory.decodeStream(in, null, o2);
			in.close();
		}
		catch (Exception e) {
		}
		return bitmap;
	}

	/*
	 * Used to load image for usrAlbul'
	 * @param url url of image
	 * @param image size of sampling
	 * @param callback which we have to return
	 */
	public static void loadAlbumPic(final String url, final int imageSize,
			final FacebookAlbum callback) {
		final Handler callingThreadHandler = new Handler();
		new Thread() {
			@Override
			public void run() {
				try {
					final Bitmap bitmap = loadGridBitmap(url, imageSize);
					callingThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							if (callback != null) {
								callback.onLoadImage(bitmap);
							}
						}
					});
				} catch (Exception ex) {
					callingThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							if (callback != null) {
								callback.onError();
							}
						}
					});
				}
			}
		}.start();

	}

	/*
	 * This method is used to check availability of network connection in
	 * android device uses CONNECTIVITY_SERVICE of android device to get desired
	 * network internet connection
	 * 
	 * @return status of availability of internet connection in true or false
	 * manner
	 */
	public static boolean haveNetworkConnection(Context context) {
		boolean haveConnectedWifi = false;
		boolean haveConnectedMobile = false;
		try {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo[] netInfo = cm.getAllNetworkInfo();
			for (NetworkInfo ni : netInfo) {
				if (ni.getTypeName().equalsIgnoreCase("WIFI"))
					if (ni.isConnected())
						haveConnectedWifi = true;
				if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
					if (ni.isConnected())
						haveConnectedMobile = true;
			}

		} catch (Exception e) {

		}
		return haveConnectedWifi || haveConnectedMobile;
	}
	
	/*
	 * Used to call for clearing the cache of application
	 */
	public static void clearCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);

            }
        } catch (Exception e) {
            // TODO: handle exception
        }

    }
	/*
	 * Used to delete cache 
	 */
	 public static boolean deleteDir(File dir) {
	        if (dir!=null && dir.isDirectory()) {
	            String[] children = dir.list();
	            for (int i = 0; i < children.length; i++) {
	                boolean success = deleteDir(new File(dir, children[i]));
	                if (!success) {
	                    return false;
	                }
	            }
	        }
	        return dir.delete();
	    }
}
