package com.schneenet.android.common.imageloader;

import com.schneenet.android.common.imageloader.loader.HttpLoader;
import com.schneenet.android.common.imageloader.loader.ResourceLoader;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

/**
 * AsyncImageLoader - lightweight asynchronous image loader inspired by Picasso
 * @author Matt Schneeberger
 *
 */
public class AsyncImageLoader
{
	private Context mContext;
	
	private AsyncImageLoader(Context context)
	{
		mContext = context;
	}
	
	// -----------------------------------------------------------------------
	// Main API 
	// -----------------------------------------------------------------------
	
	/**
	 * Load an application resource
	 * @param resId Resource identifier
	 * @return Loader to load the image
	 */
	public Loader load(int resId)
	{
		return load(ResourceLoader.createResourceUrl(mContext, resId));
	}
	
	/**
	 * Load an arbirary URL, see {@link AsyncImageLoader.load(Uri uri)} for supported URL schemes 
	 * @param url String url
	 * @return Loader to load the image
	 */
	public Loader load(String url)
	{
		Uri uri = Uri.parse(url);
		return load(uri);
	}
	
	/**
	 * Load an arbitrary URI, supported URI schemes are:
	 * <ul>
	 * <li>http</li>
	 * <li>https</li>
	 * <li>android.resource</li>
	 * <li>file</li>
	 * <li>content</li> 
	 * @param uri Uri object representing the URI
	 * @return Loader to load the image
	 */
	public Loader load(Uri uri)
	{
		String scheme = uri.getScheme();
		Loader ldr = null;
		if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme) || ContentResolver.SCHEME_CONTENT.equals(scheme) || ContentResolver.SCHEME_FILE.equals(scheme))
		{
			ldr = new ResourceLoader();
		}
		else if ("http".equals(scheme) || "https".equals(scheme))
		{
			ldr = new HttpLoader();
		}
		else
		{
			throw new IllegalArgumentException("Unsupported URI Scheme: " + scheme);
		}
		ldr.init(mContext, new Request(uri));
		return ldr;
	}
	
	/**
	 * Convenience method for preparing a custom Loader with a context and a request 
	 * @param loader Custom loader
	 * @param uri Uri to load
	 * @return Prepared loader
	 */
	public Loader loadUsing(Loader loader, Uri uri)
	{
		loader.init(mContext, new Request(uri));
		return loader;
	}
	
	// -----------------------------------------------------------------------
	// Singleton pattern 
	// -----------------------------------------------------------------------
	
	private static Object mSingletonLock = new Object();
	private static AsyncImageLoader mSingleton = null;
	
	public static AsyncImageLoader with(Context context)
	{
		synchronized (mSingletonLock)
		{
			if (mSingleton == null)
			{
				mSingleton = new AsyncImageLoader(context.getApplicationContext());
			}
			return mSingleton;
		}
	}
	
}
