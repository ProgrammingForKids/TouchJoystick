package com.car.programmator.util;

import java.io.File;
import java.io.FilenameFilter;

import com.car.programmator.ui.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter
{
	private Context		mContext;
	private String[]	_FileList	= new String[0];
	private File		_Path		= new File(ChooseFileDialog.Path);

	// private String[] FileList
	public ImageAdapter(Context c)
	{
		mContext = c;
		GetFiles();
	}

	@Override
	public int getCount()
	{
		return mThumbIds.length;
	}

	@Override
	public Object getItem(int position)
	{
		return null;
	}

	@Override
	public long getItemId(int position)
	{
		return 0;
	}

	// create a new ImageView for each item referenced by the Adapter
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ImageView imageView;
		if (convertView == null)
		{
			// if it's not recycled, initialize some attributes
			imageView = new ImageView(mContext);
			imageView.setLayoutParams(new GridView.LayoutParams(185, 185));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(8, 8, 8, 8);
		}
		else
		{
			imageView = (ImageView) convertView;
		}
		imageView.setImageResource(mThumbIds[position]);
		imageView.setId(position);
		imageView.setAlpha((float)0.4);
		if (0 < _FileList.length)
		{
			for (int k = 0; k < _FileList.length; ++k)
			{
				String fileName = Integer.toString(position) + ChooseFileDialog.FTYPE;
				if(_FileList[k].equals(fileName))
				{
					imageView.setAlpha((float)1.0);
					break;
				}
			}
		}
		return imageView;
	}

	// references to our images
	private Integer[] mThumbIds = {
			R.drawable.f1, R.drawable.f2,
			R.drawable.f3, R.drawable.f4,
			R.drawable.f5, R.drawable.f6,
			R.drawable.f7, R.drawable.f8,
			R.drawable.f9, R.drawable.f10,
			R.drawable.f11, R.drawable.f12
	};

	int GetChoosed(int position)
	{
		return mThumbIds[position];
	}
	
	void GetFiles()
	{
		if (_Path.exists())
		{
			FilenameFilter filter = new FilenameFilter()
			{

				@Override
				public boolean accept(File dir, String filename)
				{
					File sel = new File(dir, filename);
					return filename.contains(ChooseFileDialog.FTYPE) || sel.isDirectory();
				}

			};
			_FileList = _Path.list(filter);
		}
		else
		{
			_Path.mkdirs();
			_FileList = new String[0];
		}
	}
}// class ImageAdapter
