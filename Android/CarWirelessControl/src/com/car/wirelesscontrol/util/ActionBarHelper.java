package com.car.wirelesscontrol.util;

import com.car.programmator.ui.R;

import android.app.ActionBar;
import android.app.Activity;

public class ActionBarHelper
{
	final String			tag			= "ActionBarHelper";
	private final int		LEDGreen	= R.drawable.green;
	private final int		LEDRed		= R.drawable.red;
	private final Activity	activity;
	private final ActionBar	mActionBar;

	public ActionBarHelper(Activity activity)
	{
		this.activity = activity;
		mActionBar = (null == this.activity) ? null : this.activity.getActionBar();
	}

	public void SetIcon(int iconId)
	{
		if (null != mActionBar)
		{
			this.activity.getActionBar().setIcon(iconId);
		}
		else
		{
			Logger.Log.e(tag, "ActionBar is null");
		}
	}

	public void SetTitle(final String title)
	{
		if (null != mActionBar)
		{
			this.activity.getActionBar().setTitle(title);
		}
		else
		{
			Logger.Log.e(tag, "ActionBar is null");
		}
	}

	public void ShowStatus(final String title, int iconId)
	{
		SetTitle(title);
		SetIcon(iconId);
	}

	public void ShowStatus(final String title, boolean bGreen)
	{
		ShowStatus(title, LEDIconId(bGreen));
	}

	public void ShowStatus(boolean bGreen)
	{
		SetIcon(LEDIconId(bGreen));
	}

	private int LEDIconId(boolean bGreen)
	{
		int resId = (bGreen) ? LEDGreen : LEDRed;
		return resId;
	}

}// class ActionBarHelper
