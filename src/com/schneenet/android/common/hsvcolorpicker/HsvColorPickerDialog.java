package com.schneenet.android.common.hsvcolorpicker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.schneenet.android.common.R;
import com.schneenet.android.common.hsvcolorpicker.views.HsvColorPickerView;

public class HsvColorPickerDialog extends AlertDialog implements TextWatcher
{

	private Callbacks mListener;
	private HsvColorPickerView mColorPickerView;
	private EditText mEditText;
	
	public HsvColorPickerDialog(Context context, CharSequence title, int initialColor, Callbacks cb)
	{
		super(context);
		mListener = cb;
		
		// inflate the dialog view
		View v = getLayoutInflater().inflate(R.layout.hsv_color_picker_dialog, null);
		
		// get references to components
		mColorPickerView = (HsvColorPickerView) v.findViewById(R.id.hsv_color_picker_view);
		mEditText = (EditText) v.findViewById(R.id.hsv_color_picker_editText);
		
		// set the initial color
		mColorPickerView.setSelectedColor(initialColor);
		mEditText.setText("#" + Integer.toHexString(mColorPickerView.getSelectedColor()));
		
		// bind handler to color picker view that will change the text box
		mColorPickerView.setOnSelectedColorChangedListener(new HsvColorPickerView.OnSelectedColorChangedListener()
		{
			@Override
			public void onSelectedColorChanged(int newColor, boolean fromUser)
			{
				if (fromUser) mEditText.setText("#" + Integer.toHexString(mColorPickerView.getSelectedColor()));
				if (mListener != null) mListener.onColorChanged(newColor);
			}
		});
		
		// bind handler to text box that will change the color view
		mEditText.addTextChangedListener(this);
		
		// set the cancel button on the dialog
		setButton(AlertDialog.BUTTON_NEGATIVE, context.getText(android.R.string.cancel), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				if (mListener != null) mListener.onColorAccepted(mColorPickerView.getSelectedColor());
				dialog.dismiss();
			}
		});
		
		// set the ok button on the dialog
		setButton(AlertDialog.BUTTON_POSITIVE, context.getText(android.R.string.ok), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		
		// set the title on the dialog
		setTitle(title);
		
		// set the inflated view to the dialog
		setView(v);
	}
	
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
		try
		{
			int color = Color.parseColor(s.toString());
			mColorPickerView.setSelectedColor(color);
			if (mListener != null) mListener.onColorChanged(color);
		}
		catch (Exception ex)
		{
			Log.w("HsvColorPickerDialog", "Unable to parse '" + s + "' as a color.");
		}
	}
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{
		// No-op
	}
	
	@Override
	public void afterTextChanged(Editable s)
	{
		// No-op
	}
	
	public abstract static class SimpleCallbacks implements Callbacks
	{
		public void onColorAccepted(int newColor) {}
		public void onCancelled() {}
	}
	
	public interface Callbacks
	{
		void onColorChanged(int newColor);
		void onColorAccepted(int newColor);
		void onCancelled();
	}

}
