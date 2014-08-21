package com.schneenet.android.common.imageloader;

import android.graphics.Bitmap;

public interface Target
{
	public void onImageLoaded(Bitmap image);
	public void onImageFailed(Response response);
}
