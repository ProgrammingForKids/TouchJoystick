package com.car.programmator.util;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Environment;

public class ChooseFileDialog
{
	// In an Activity
	private String[]			mFileList;
	private File				mPath				= new File(Environment.getExternalStorageDirectory() + "//carpr//");
	private String				mChosenFile;
	private static final String	FTYPE				= ".car";
	public static final int	DIALOG_LOAD_FILE	= 1000;

	public void loadFileList()
	{
		try
		{
			mPath.mkdirs();
		}
		catch (SecurityException e)
		{
		}
		if (mPath.exists())
		{
			FilenameFilter filter = new FilenameFilter()
			{

				@Override
				public boolean accept(File dir, String filename)
				{
					File sel = new File(dir, filename);
					return filename.contains(FTYPE) || sel.isDirectory();
				}

			};
			mFileList = mPath.list(filter);
		}
		else
		{
			mFileList = new String[0];
		}
	}

	public Dialog onCreateDialog(Activity activity, int id)
	{
		Dialog dialog = null;
		AlertDialog.Builder builder = new Builder(activity);

		switch (id)
		{
			case DIALOG_LOAD_FILE:
				builder.setTitle("Choose your file");
				if (mFileList == null)
				{
					dialog = builder.create();
					return dialog;
				}
				builder.setItems(mFileList, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						mChosenFile = mFileList[which];
						Logger.Log.t(mChosenFile);
					}
				});
				break;
		}
		dialog = builder.show();
		return dialog;
	}
}//class ChooseFileDialog
