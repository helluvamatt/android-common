package com.schneenet.android.common.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public abstract class Loader extends AsyncTask<Request, Void, Response>
{
	private Request mRequest;
	private Target mTargetCallbacks;
	private Context mContext;
	
	private int mPlaceholder;
	private int mErrorImage;
	
	/**
	 * Initialize the Loader with it's context and request
	 * @param context Application context
	 * @param request Request
	 */
	public final void init(Context context, Request request)
	{
		mRequest = request;
		mContext = context;
	}
	
	/**
	 * Use an application image resource as the placeholder for an ImageView
	 * @param resId Resource identifier
	 * @return Current Loader instance for chaining calls
	 */
	public final Loader withPlaceholder(int resId) 
	{
		mPlaceholder = resId;
		return this;
	}
	
	/**
	 * Use an application image resource as an erro image when an image fails to load
	 * @param resId Resource identifier
	 * @return Current Loader instance for chaining calls
	 */
	public final Loader withErrorImage(int resId)
	{
		mErrorImage = resId;
		return this;
	}
	
	/**
	 * Start the image load with the specified Target
	 * @param target Target to receive callbacks on the image load
	 * @return Request object representing this request
	 */
	public final Request to(Target target)
	{
		if (target == null)
		{
			throw new IllegalArgumentException("Argument 'target' cannot be null!");
		}
		mTargetCallbacks = target;
		execute(mRequest);
		return mRequest;
	}
	
	/**
	 * Start the image load with the specified ImageView as the target
	 * @param imageView ImageView to receive the loaded image
	 * @return Request object representing this request
	 */
	public final Request to(final ImageView target)
	{
		if (target == null)
		{
			throw new IllegalArgumentException("Argument 'target' cannot be null!");
		}
		if (mPlaceholder != 0) target.setImageResource(mPlaceholder);
		mTargetCallbacks = new Target()
		{
			@Override
			public void onImageLoaded(Bitmap image)
			{
				target.setImageBitmap(image);
			}

			@Override
			public void onImageFailed(Response response)
			{
				Log.e("AsyncImageLoader", "Failed to load image: '" + response.getRequest().getUrl().toString() + "'.", response.getError());
				if (mErrorImage != 0) target.setImageResource(mErrorImage);
			}
		};
		execute(mRequest);
		return mRequest;
	}
	
	@Override
	protected Response doInBackground(Request... params)
	{
		Request req = params[0];
		try
		{
			return doLoad(req);
		}
		catch (Throwable ex)
		{
			return new Response(req, null, ex);
		}
	}
	
	@Override
	protected void onPostExecute(Response result)
	{
		Bitmap image = result.getImage();
		Throwable error = result.getError();
		if (error == null && image != null)
		{
			mTargetCallbacks.onImageLoaded(image);
		}
		else
		{
			mTargetCallbacks.onImageFailed(result);
		}
	}
	
	/**
	 * Get reference to Context for this loader
	 * @return Android Context
	 */
	protected Context getContext()
	{
		return mContext;
	}
	
	// Abstract interface
	protected abstract Response doLoad(Request request) throws Throwable;
	
}
