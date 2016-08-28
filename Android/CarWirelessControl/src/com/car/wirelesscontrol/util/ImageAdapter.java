package com.car.wirelesscontrol.util;

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
	private String[]	mFileList	= new String[0];
	private File		mPath		= new File(ChooseFileDialog.Path);

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
			imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(8, 8, 8, 8);
		}
		else
		{
			imageView = (ImageView) convertView;
		}
		float alpha=  (float) ((IsExist(position))?1.0:0.4);
		imageView.setImageResource(mThumbIds[position]);
		imageView.setId(position);
		imageView.setAlpha(alpha);
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

	boolean IsExist(int position)
	{
		if (0 < mFileList.length)
		{
			String fileName = Integer.toString(position) + ChooseFileDialog.FTYPE;
			for (int k = 0; k < mFileList.length; ++k)
			{
				if (mFileList[k].equals(fileName))
				{
					return true;
				}
			}
		}
		return false;
	}

	void GetFiles()
	{
		if (mPath.exists())
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
			mFileList = mPath.list(filter);
		}
		else
		{
			mPath.mkdirs();
			mFileList = new String[0];
		}
	}
}// class ImageAdapter
