package com.car.programmator.ui;


import com.car.programmator.util.Logger;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ImageView;

class ImageHelper
{
	public View	view	= null;
	public int	index	= -1;

	void Ini()
	{
		this.view = null;
		this.index = -1;
	}

	ImageHelper Set(View v, int index)
	{
		this.view = v;
		this.index = index;
		return this;
	}

	int GetId()
	{
		if (null != view)
		{
			return view.getId();
		}
		return -1;
	}

	void Select()
	{
		if (null != this.view)
		{
			this.view.setAlpha((float) 0.5);
			this.view.setPadding(6, 6, 6, 6);
			this.view.setBackgroundColor(Color.BLUE);
			Logger.Log.t("SELECT");
		}
	}

	void UnSelect()
	{
		if (null != this.view)
		{
			Logger.Log.t("UN-SELECT");
			// this.view.getPaddingBottom()
			this.view.setAlpha((float) 1.0);
			this.view.setPadding(0, 0, 0, 0);
		}
	}

	void SetImage(Context context, int recId)
	{
		if (null == view)
		{
			return;
		}
		if (null != context)
		{
			Drawable drawable = ContextCompat.getDrawable(context, recId);
			((ImageView) view).setImageDrawable(drawable);
		}
	}

	void RestoreImage(Context context, SparseIntArray rc)
	{
		if (null == view)
		{
			return;
		}
		if (null != context)
		{
			int drawableId = rc.get(view.getId());
			Drawable drawable = ContextCompat.getDrawable(context, drawableId);
			((ImageView) view).setImageDrawable(drawable);
		}
	}

}// class Selected

