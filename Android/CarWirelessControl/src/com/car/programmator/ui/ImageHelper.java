package com.car.programmator.ui;

import com.car.programmator.util.Logger;
import com.car.programmator.util.OpCode;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

class ImageHelper
{
	public View	view	= null;
	public int	index	= -1;

	public ImageHelper()
	{
	}

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

	int Opcode()
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

	public boolean IsActive()
	{
		return (null != view);
	}

	ImageHelper UnSelect()
	{
		if (null != this.view)
		{
			Logger.Log.t("UN-SELECT");
			// this.view.getPaddingBottom()
			this.view.setAlpha((float) 1.0);
			this.view.setPadding(0, 0, 0, 0);
		}
		return this;
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

	void RestoreImage(Context context)
	{
		if (null == view)
		{
			return;
		}
		if (null != context)
		{
			int drawableId = OpCode.DrawableId(view.getId());
			Drawable drawable = ContextCompat.getDrawable(context, drawableId);
			((ImageView) view).setImageDrawable(drawable);
		}
	}

	public static ImageView CreateImage(Context context, int opcode)
	{
		if (null == context)
		{
			return null;
		}
		int drawableId = OpCode.DrawableId(opcode);
		ImageView iv = new ImageView(context);
		Drawable drawable = ContextCompat.getDrawable(context, drawableId);

		iv.setImageDrawable(drawable);
		iv.setId(opcode);
		iv.setAdjustViewBounds(true);
		iv.setScaleType(ScaleType.CENTER_INSIDE);
		return iv;
	}

	public static void Store(Context context, int id, LinearLayout ll, OnClickListener listener)
	{
		ImageView iv = CreateImage(context, id);
		if (null == iv)
		{
			return;
		}
		iv.setPadding(5, 5, 5, 5);
		iv.setOnClickListener(listener);
		ll.addView(iv, 0);
	}

}// class Selected
