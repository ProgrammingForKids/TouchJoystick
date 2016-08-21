package com.car.programmator.ui;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;

public class Eraser
{
	private final ImageView		_eraser;
	private final FlowLayout	_command_aria;
	private final ImageHelper	_selected_image;

	public Eraser(Activity activity, final ImageHelper selectedImage)
	{
		_selected_image = selectedImage;
		_command_aria = (FlowLayout) activity.findViewById(R.id.test);
		_eraser = (ImageView) activity.findViewById(R.id.image_eraser);
		_eraser.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				int index = -1;
				if (_selected_image.linked_image.IsActive())
				{
					index = _selected_image.index;
					if (null != _selected_image)
					{
						_command_aria.removeView(_selected_image.linked_image.view);
						_selected_image.UnSelect().InitAll();
					}
				}
				else
				{
					index = _command_aria.getChildCount();
				}
				if (0 < index)
				{
					_command_aria.removeViewAt(index - 1);
				}
			}
		});
		_eraser.setOnLongClickListener(new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(View v)
			{
				_command_aria.removeAllViews();
				return false;
			}
		});
	}

}// class Eraser
