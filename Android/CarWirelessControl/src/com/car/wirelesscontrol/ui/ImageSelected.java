package com.car.wirelesscontrol.ui;

import com.car.wirelesscontrol.util.OpCode;

import android.app.Activity;
import android.view.View;

public class ImageSelected
{
	private final ImageHelper	_selected	= new ImageHelper();
	private final ImageHelper	_insert		= new ImageHelper();
	private final Activity		activity;

	public ImageSelected(Activity activity)
	{
		this.activity = activity;
	}

	View SelectedView()
	{
		return _selected.view;
	}

	View InsertView()
	{
		return _insert.view;
	}

	void Unselect()
	{
		_selected.UnSelect().Ini();
		_insert.Ini();
	}

	void Set(View v, int index)
	{
		_selected.Set(v, index).Select();
		_insert.Set(ImageHelper.CreateImage(activity, OpCode._EMPTY), index);
	}

	int InsertIndex()
	{
		return _insert.index;
	}

	boolean IsInsertActive()
	{
		return _insert.IsActive();
	}

	boolean IsSelected()
	{
		return !((null == _selected.view) || (null == _insert.view));
	}

}// class ImageSelected
