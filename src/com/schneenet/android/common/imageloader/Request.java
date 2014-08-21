package com.schneenet.android.common.imageloader;

import android.net.Uri;

public class Request
{
	private Uri url;
	
	public Request(Uri url)
	{
		this.url = url;
	}
	
	public Uri getUrl()
	{
		return url;
	}
}
