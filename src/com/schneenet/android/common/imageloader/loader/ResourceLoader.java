package com.schneenet.android.common.imageloader.loader;

import java.io.FileNotFoundException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.schneenet.android.common.imageloader.Loader;
import com.schneenet.android.common.imageloader.Request;
import com.schneenet.android.common.imageloader.Response;

public class ResourceLoader extends Loader
{

	@Override
	protected Response doLoad(Request request) throws Throwable
	{
		Bitmap image = loadResourceBitmap(getContext(), request.getUrl());
		return new Response(request, image, null);
	}
	
	/**
	 * Load a resource image synchronously
	 * @param context Application context
	 * @param url Resource url ('android.resource://&lt;package&gt;/&lt;resource id&gt;', etc.)
	 * @return Bitmap image
	 * @throws FileNotFoundException When the resource with the url was not found
	 */
	public static Bitmap loadResourceBitmap(Context context, Uri url) throws FileNotFoundException
	{
		InputStream imageIs = context.getContentResolver().openInputStream(url);
		return BitmapFactory.decodeStream(imageIs);
	}
	
	/**
	 * Create a URL representing the application resource with the specified resId
	 * @param context Application context
	 * @param resId Resource identifier
	 * @return URI string
	 */
	public static String createResourceUrl(Context context, int resId)
	{
		return ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/" + resId;
	}

}
