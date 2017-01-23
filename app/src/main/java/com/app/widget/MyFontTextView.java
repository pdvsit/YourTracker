package com.app.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.app.tracker.R;
import com.app.utility.AppLog;

public class MyFontTextView extends TextView {

	private static final String TAG = "TextView";

	private Typeface typeface;

	public MyFontTextView(Context context) {
		super(context);
	}

	public MyFontTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setCustomFont(context, attrs);
	}

	public MyFontTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setCustomFont(context, attrs);
	}

	private void setCustomFont(Context ctx, AttributeSet attrs) {
		TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.app);
		String customFont = a.getString(R.styleable.app_customFont);
		setCustomFont(ctx, customFont);
		a.recycle();
	}

	private boolean setCustomFont(Context ctx, String asset) {
		try {
			if (typeface == null) {
				Log.i(TAG, "asset:: " + "fonts/" + asset);
				typeface = Typeface.createFromAsset(ctx.getAssets(),
						"fonts/AGENCYR.TTF");
			}

		} catch (Exception e) {
			e.printStackTrace();
			AppLog.handleException(TAG, e);
			return false;
		}

		setTypeface(typeface);
		return true;
	}

}