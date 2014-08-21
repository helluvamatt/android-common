package com.schneenet.android.common.imageloader.loader;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.schneenet.android.common.imageloader.Loader;
import com.schneenet.android.common.imageloader.Request;
import com.schneenet.android.common.imageloader.Response;

public class HttpLoader extends Loader
{

	@Override
	protected Response doLoad(Request request) throws Throwable
	{
		URL url = new URL(request.getUrl().toString());
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		try
		{
			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			Bitmap image = BitmapFactory.decodeStream(in);
			return new Response(request, image, null);
		}
		finally
		{
			urlConnection.disconnect();
		}
	}

}
