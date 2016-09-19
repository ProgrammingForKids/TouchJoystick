package com.car.wirelesscontrol.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import com.car.programmator.ui.R;
import com.car.wirelesscontrol.ui.UIHelper;

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
								_ui.StringToCommand(comm);
								b = true;
							}
							break;
						case DIALOG_SAVE_FILE:
							String data = _ui.CommandToString();
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

