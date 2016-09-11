package com.car.wirelesscontrol.ui;

import java.io.IOException;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

public class SoundHelper
{
	private SoundPool		mSoundPool;
	private AssetManager	mAssetManager;
	private int				mStreamID	= 0;
	private int				mDing;
	private int				mHorn;
	private int				mCrush;

	public void PlayDing()
	{
		mStreamID = playSound(mDing);
	}

	public void PlayHorn()
	{
		mStreamID = playSound(mHorn);
	}

	public void PlayCrush()
	{
		mStreamID = playSound(mCrush);
	}

	public void Stop()
	{
		if (mStreamID > 0)
		{
			mSoundPool.stop(mStreamID);
		}
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void createNewSoundPool()
	{
		AudioAttributes attributes = new AudioAttributes.Builder()
				.setUsage(AudioAttributes.USAGE_GAME)
				.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
				.build();
		mSoundPool = new SoundPool.Builder()
				.setAudioAttributes(attributes)
				.build();
	}

	public void Create(Context context)
	{
		CreateSoundPool();
		mAssetManager = context.getAssets();
		mDing = loadSound("ding.ogg");
		mHorn = loadSound("vwhorn.mp3");
		mCrush = loadSound("crush.ogg");
	}

	public void CreateSoundPool()
	{
		if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
		{
			createOldSoundPool();
		}
		else
		{
			createNewSoundPool();
		}
	}

	@SuppressWarnings("deprecation")
	private void createOldSoundPool()
	{
		mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
	}

	public void Release()
	{
		if (null != mSoundPool)
		{
			mSoundPool.release();
			mSoundPool = null;
		}
	}

	private int loadSound(String fileName)
	{
		AssetFileDescriptor afd;
		try
		{
			afd = mAssetManager.openFd(fileName);
		}
		catch (IOException e)
		{
			return -1;
		}
		return mSoundPool.load(afd, 1);
	}

	private int playSound(int sound)
	{
		if (sound > 0)
		{
			mStreamID = mSoundPool.play(sound, 1, 1, 1, 0, 1);
		}
		return mStreamID;
	}
}// class SoundHelper
