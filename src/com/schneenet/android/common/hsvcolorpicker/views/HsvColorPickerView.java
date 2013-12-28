package com.schneenet.android.common.hsvcolorpicker.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.schneenet.android.common.R;

@SuppressLint("DrawAllocation")
public class HsvColorPickerView extends View implements View.OnTouchListener
{

	private static final int[] COLORS = new int[] {
			0xFFFF0000,
			0xFFFF00FF,
			0xFF0000FF,
			0xFF00FFFF,
			0xFF00FF00,
			0xFFFFFF00,
			0xFFFF0000 };

	private Paint mAlphaBgPaint;
	private Paint mHuePaint;
	private Paint mSatValPaint;
	private Paint mAlphaRangePaint;
	private Paint mFinalColorPaint;
	private Paint mMarkerFillPaint;
	private Paint mMarkerStrokePaint;
	private Paint mMarkerSatValPaint1;
	private Paint mMarkerSatValPaint2;

	private float mPadding;
	private float mBarSize;
	
	// drawing according to predetermined grid lines (calculated in onMeasure)
	private float mOffset1 = 0.f; // furthest top/left: just mPadding
	private float mOffset2 = 0.f; // right side/bottom of sat/val box
	private float mOffset3 = 0.f; // left side/top of hue box and final color box
	private float mOffset4 = 0.f; // right side/bottom of alpha box and final color box
	
	// predefined rectangles for the 4 zones (built in onMeasure)
	private RectF mRectSatVal = new RectF();
	private RectF mRectHue = new RectF();
	private RectF mRectAlpha = new RectF();
	private RectF mRectFinalColor = new RectF();

	private OnSelectedColorChangedListener mListener;

	// HSV values: Hue: 0...360 Sat: 0...1 Val: 0...1
	//private float[] mColor;
	private float mHue;
	private float mSat;
	private float mVal;

	// Alpha (0...255)
	private int mAlpha;
	
	// drawing / touch helpers
	private Zone mCurrentZone = Zone.NONE;

	public HsvColorPickerView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init();
	}

	public HsvColorPickerView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public HsvColorPickerView(Context context)
	{
		super(context);
		init();
	}

	private void init()
	{
		// create paints
		mAlphaBgPaint = new Paint();
		mHuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mSatValPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mAlphaRangePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mFinalColorPaint = new Paint();
		mMarkerFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mMarkerStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mMarkerSatValPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
		mMarkerSatValPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		// configure alpha background paint
		Bitmap alphaBg = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888);
		Canvas alphaBgCanvas = new Canvas(alphaBg);
		Paint greyPaint = new Paint();
		greyPaint.setColor(0xFFCCCCCC);
		alphaBgCanvas.drawColor(0xFFFFFFFF);
		alphaBgCanvas.drawRect(0, 0, 10, 10, greyPaint);
		alphaBgCanvas.drawRect(10, 10, 20, 20, greyPaint);
		Shader alphaBgShader = new BitmapShader(alphaBg, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		mAlphaBgPaint.setShader(alphaBgShader);
		
		// configure marker paint
		mMarkerFillPaint.setStyle(Paint.Style.FILL);
		mMarkerFillPaint.setColor(Color.WHITE);
		mMarkerStrokePaint.setStyle(Paint.Style.STROKE);
		mMarkerStrokePaint.setColor(Color.BLACK);
		mMarkerStrokePaint.setStrokeWidth(2f);
		mMarkerSatValPaint1.setStyle(Paint.Style.STROKE);
		mMarkerSatValPaint1.setColor(Color.BLACK);
		mMarkerSatValPaint1.setStrokeWidth(5f);
		mMarkerSatValPaint2.setStyle(Paint.Style.STROKE);
		mMarkerSatValPaint2.setColor(Color.WHITE);
		mMarkerSatValPaint2.setStrokeWidth(3f);
		
		// grab dimensions from resources
		mPadding = getResources().getDimension(R.dimen.hsv_color_picker_padding);
		mBarSize = getResources().getDimension(R.dimen.hsv_color_picker_barSize);
		
		// initial color
		//mColor = new float[] { 0.f, 1.f, 1.f };
		mHue = 0.f;
		mSat = 1.f;
		mVal = 1.f;
		mAlpha = 0xFF;
		
		// set touch listener
		setOnTouchListener(this);
		
		// disable hardware acceleration
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}

	public void setSelectedColor(int color)
	{
		mAlpha = Color.alpha(color);
		float[] newColor = new float[3];
		Color.colorToHSV(color, newColor);
		mHue = newColor[0];
		mSat = newColor[1];
		mVal = newColor[2];
		if (mListener != null) mListener.onSelectedColorChanged(color, false);
		invalidate();
	}

	public int getSelectedColor()
	{
		return Color.HSVToColor(mAlpha, new float[] { mHue, mSat, mVal });
	}

	public void setOnSelectedColorChangedListener(OnSelectedColorChangedListener l)
	{
		mListener = l;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, widthMeasureSpec);
		
		mOffset1 = mPadding;
		mOffset2 = getMeasuredWidth() - (mPadding * 2) - mBarSize;
		mOffset3 = getMeasuredWidth() - mPadding - mBarSize;
		mOffset4 = getMeasuredWidth() - mPadding;
		
		mRectSatVal = new RectF(mOffset1, mOffset1, mOffset2, mOffset2);
		mRectHue = new RectF(mOffset3, mOffset1, mOffset4, mOffset2);
		mRectAlpha = new RectF(mOffset1, mOffset3, mOffset2, mOffset4);
		mRectFinalColor = new RectF(mOffset3, mOffset3, mOffset4, mOffset4);
		
		mHuePaint.setShader(new LinearGradient(mOffset3, mOffset2, mOffset3, mOffset1, COLORS, null, Shader.TileMode.CLAMP));
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		// prepare shaders
		Shader valShader = new LinearGradient(mOffset1, mOffset1, mOffset1, mOffset2, 0xFFFFFFFF, 0xFF000000, Shader.TileMode.CLAMP);
		Shader satShader = new LinearGradient(mOffset1, mOffset1, mOffset2, mOffset1, 0xFFFFFFFF, Color.HSVToColor(0xFF, new float[] {mHue, 1f, 1f}), Shader.TileMode.CLAMP);
		Shader satValShader = new ComposeShader(valShader, satShader, PorterDuff.Mode.MULTIPLY);
		mSatValPaint.setShader(satValShader);
		
		Shader alphaShader = new LinearGradient(mOffset1, mOffset3, mOffset2, mOffset3, Color.HSVToColor(0xFF, new float[] { mHue, mSat, mVal }), Color.HSVToColor(0x00, new float[] { mHue, mSat, mVal }), Shader.TileMode.CLAMP);
		mAlphaRangePaint.setShader(alphaShader);
		
		mFinalColorPaint.setColor(getSelectedColor());
		
		// draw sat/val rect
		canvas.drawRect(mRectSatVal, mSatValPaint);
		
		// draw hue rect
		canvas.drawRect(mRectHue, mHuePaint);
		
		// draw alpha rect
		canvas.drawRect(mRectAlpha, mAlphaBgPaint);
		canvas.drawRect(mRectAlpha, mAlphaRangePaint);
		
		// draw final color rect
		canvas.drawRect(mRectFinalColor, mAlphaBgPaint);
		canvas.drawRect(mRectFinalColor, mFinalColorPaint);
		
		// draw markers
		float markerSize = (float)(mOffset3 - mOffset2) * 0.75f;
		float markerPointSize = (float)(mOffset3 - mOffset2) * 0.25f;
		
		// draw sat/val marker
		float satValMarkerOffsetX = mRectSatVal.left + mSat * mRectSatVal.width();
		float satValMarkerOffsetY = mRectSatVal.bottom - mVal * mRectSatVal.height();
		canvas.drawCircle(satValMarkerOffsetX, satValMarkerOffsetY, markerSize / 2, mMarkerSatValPaint1);
		canvas.drawCircle(satValMarkerOffsetX, satValMarkerOffsetY, markerSize / 2, mMarkerSatValPaint2);
		
		// draw hue marker
		float hueMarkerOffsetY = mRectHue.top + (mHue / 360.0f) * mRectHue.height();
		Path hueMarkerPath = new Path();
		hueMarkerPath.moveTo(mOffset3 - markerSize, hueMarkerOffsetY - (markerSize / 2));
		hueMarkerPath.lineTo(mOffset3 - markerSize, hueMarkerOffsetY + (markerSize / 2));
		hueMarkerPath.lineTo(mOffset3 - markerPointSize, hueMarkerOffsetY + (markerSize / 2));
		hueMarkerPath.lineTo(mOffset3, hueMarkerOffsetY);
		hueMarkerPath.lineTo(mOffset3 - markerPointSize, hueMarkerOffsetY - (markerSize / 2));
		hueMarkerPath.close();
		canvas.drawPath(hueMarkerPath, mMarkerFillPaint);
		canvas.drawPath(hueMarkerPath, mMarkerStrokePaint);
		
		// draw alpha marker
		float alphaMarkerOffsetX = mRectAlpha.right - ((float) mAlpha / (float) 0xFF) * mRectAlpha.width();
		Path alphaMarkerPath = new Path();
		alphaMarkerPath.moveTo(alphaMarkerOffsetX - (markerSize / 2), mOffset3 - markerSize);
		alphaMarkerPath.lineTo(alphaMarkerOffsetX + (markerSize / 2), mOffset3 - markerSize);
		alphaMarkerPath.lineTo(alphaMarkerOffsetX + (markerSize / 2), mOffset3 - markerPointSize);
		alphaMarkerPath.lineTo(alphaMarkerOffsetX, mOffset3);
		alphaMarkerPath.lineTo(alphaMarkerOffsetX - (markerSize / 2), mOffset3 - markerPointSize);
		alphaMarkerPath.close();
		canvas.drawPath(alphaMarkerPath, mMarkerFillPaint);
		canvas.drawPath(alphaMarkerPath, mMarkerStrokePaint);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();
		
		// we only deal with ACTION_DOWN, ACTION_MOVE, and ACTION_UP
		if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_UP)
		{
			// find out what zone this event occurred in
			Zone eventZone = Zone.NONE;
			if (mRectSatVal.contains(x, y))
			{
				eventZone = Zone.SATVAL;
			}
			else if (mRectHue.contains(x, y))
			{
				eventZone = Zone.HUE;
			}
			else if (mRectAlpha.contains(x, y))
			{
				eventZone = Zone.ALPHA;
			}
			else
			{
				eventZone = Zone.NONE;
			}
			
			// down event sets the current zone
			if (action == MotionEvent.ACTION_DOWN)
			{
				mCurrentZone = eventZone;
			}
			
			// if the current gesture zone and the event zone are the same, perform the zone's action
			if (mCurrentZone == Zone.SATVAL && eventZone == Zone.SATVAL)
			{
				// handle saturation and value change
				mSat = (x - mRectSatVal.left) / mRectSatVal.width();
				mVal = 1.f - (y - mRectSatVal.top) / mRectSatVal.height();
				invalidate();
				if (mListener != null) mListener.onSelectedColorChanged(getSelectedColor(), true);
			}
			else if (mCurrentZone == Zone.HUE && eventZone == Zone.HUE)
			{
				// handle hue change
				mHue = (y - mRectHue.top) / mRectHue.height() * 360.0f;
				invalidate();
				if (mListener != null) mListener.onSelectedColorChanged(getSelectedColor(), true);
			}
			else if (mCurrentZone == Zone.ALPHA && eventZone == Zone.ALPHA)
			{
				// handle alpha change
				float newAlpha = (x - mRectAlpha.left) / mRectAlpha.width();
				mAlpha = 0xFF - (int)(newAlpha * 0xFF);
				invalidate();
				if (mListener != null) mListener.onSelectedColorChanged(getSelectedColor(), true);
			}
			
			return true;
		}
		return false;
	}

	private enum Zone
	{
		NONE, SATVAL, HUE, ALPHA;
	}
	
	public interface OnSelectedColorChangedListener
	{
		void onSelectedColorChanged(int newColor, boolean fromUser);
	}

}
