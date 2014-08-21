package com.schneenet.android.common.imageloader;

import android.graphics.Bitmap;

public class Response
{
	private Bitmap image;
	private Throwable error;
	private Request request;
	
	public Response(Request request, Bitmap image, Throwable error)
	{
		this.request = request;
		this.image = image;
		this.error = error;
	}
	
	public Bitmap getImage()
	{
		return image;
	}

	public Request getRequest()
	{
		return request;
	}

	public Throwable getError()
	{
		return error;
	}
}
