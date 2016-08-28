package com.car.programmator.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import com.car.programmator.ui.R;
import com.car.programmator.ui.UIHelper;

import android.app.Dialog;
import android.content.Context;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;

public class ChooseFileDialog
{
	public static final String	Path				= Environment.getExternalStorageDirectory() + "//carpr//";
	public static final String	FTYPE				= ".car";
	public static final int		DIALOG_LOAD_FILE	= 1001;
	public static final int		DIALOG_SAVE_FILE	= 1002;
	private File				_Path				= new File(Path);
	private final int			_mode;

	public ChooseFileDialog(int mode)
	{
		_mode = mode;
	}

	public void ShowDialog(Context context, final UIHelper _ui)
	{
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.choosefile_layout);
		{
			GridView gridview = (GridView) dialog.findViewById(R.id.gridview);
			final ImageAdapter ia = new ImageAdapter(context);
			gridview.setAdapter(ia);
			gridview.setOnItemClickListener(new OnItemClickListener()
			{
				public void onItemClick(AdapterView<?> parent, View v, int position, long id)
				{
					String fileName = Integer.toString(v.getId()) + FTYPE;
					File file = new File(_Path, fileName);
					if (DIALOG_LOAD_FILE == _mode)
					{
						if (((ImageView) v).getAlpha() < 1)
						{
							return;
						}
					}
					boolean b = false;

					switch (_mode)
					{
						case DIALOG_LOAD_FILE:
							String comm = Load(file);
							if (null != comm)
							{
								_ui.SetCommand(comm);
								b = true;
							}
							break;
						case DIALOG_SAVE_FILE:
							String data = _ui.CommandString();
							_ui.SaveCommandString(data);
							b = Save(file, data);
							break;
						default:
							break;
					}
					if (b)
					{
						int iconId = ia.GetChoosed(position);
						_ui.SetFileIcon(iconId);
						_ui.ShowFileIcon();
					}

					dialog.dismiss();

				}
			});
		}
		dialog.show();

		// Size & Position
		Window window = dialog.getWindow();
		window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		window.setGravity(Gravity.CENTER_HORIZONTAL);

	}

	boolean Save(File file, String data)
	{
		FileOutputStream outputStream;
		try
		{
			outputStream = new FileOutputStream(file);
			outputStream.write(data.getBytes());
			outputStream.close();
			return true;
		}
		catch (IOException e)
		{
		}
		return false;
	}

	String Load(File file)
	{
		StringBuilder text = new StringBuilder();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			while ((line = br.readLine()) != null)
			{
				text.append(line);
			}
			br.close();
			return text.toString();
		}
		catch (IOException e)
		{
		}
		return null;
	}

}// class ChooseFileDialog

//
// {
// private String[] mFileList;
// private File mPath = new File(Environment.getExternalStorageDirectory() + "//carpr//");
// private String mChosenFile;
// private static final String FTYPE = ".car";
// public static final int DIALOG_LOAD_FILE = 1000;
//
// public void loadFileList()
// {
// if (mPath.exists())
// {
// FilenameFilter filter = new FilenameFilter()
// {
//
// @Override
// public boolean accept(File dir, String filename)
// {
// File sel = new File(dir, filename);
// return filename.contains(FTYPE) || sel.isDirectory();
// }
//
// };
// mFileList = mPath.list(filter);
// }
// else
// {
// mPath.mkdirs();
// mFileList = new String[0];
// }
// }
//
// public Dialog onCreateDialog(Activity activity, int id)
// {
// Dialog dialog = null;
// AlertDialog.Builder builder = new Builder(activity);
//
// switch (id)
// {
// case DIALOG_LOAD_FILE:
// builder.setTitle("Choose your file");
// if (mFileList == null)
// {
// dialog = builder.create();
// return dialog;
// }
// builder.setItems(mFileList, new DialogInterface.OnClickListener()
// {
// public void onClick(DialogInterface dialog, int which)
// {
// mChosenFile = mFileList[which];
// Logger.Log.t(mChosenFile);
// }
// });
// break;
// }
// dialog = builder.show();
// return dialog;
// }
//
// }
