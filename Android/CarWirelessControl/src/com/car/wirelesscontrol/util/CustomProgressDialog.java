package com.car.wirelesscontrol.util;

import com.car.programmator.ui.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;

public class CustomProgressDialog extends ProgressDialog
{
	private AnimationDrawable animation;
	public static ProgressDialog ctor(Context context)
	{
		CustomProgressDialog dialog = new CustomProgressDialog(context);
		dialog.setIndeterminate(true);
		dialog.setCancelable(false);
		return dialog;
	}

	public CustomProgressDialog(Context context)
	{
		super(context);
	}

	public CustomProgressDialog(Context context, int theme)
	{
		super(context, theme);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_custom_progress_dialog);
		ImageView la = (ImageView) findViewById(R.id.animation);
		la.setBackgroundResource(R.drawable.custom_progress_dialog_animation);
		animation = (AnimationDrawable) la.getBackground();
	}

	@Override
	public void show()
	{
		super.show();
		animation.start();
	}

	@Override
	public void dismiss()
	{
		super.dismiss();
		animation.stop();
	}
}// class CustomProgressDialog
