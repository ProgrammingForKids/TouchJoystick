package com.car.wirelesscontrol.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

public class BluetoothConnectedThread extends Thread
{
	private static final UUID	MY_UUID				= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	final static int			RECIEVE_MESSAGE		= 1;

	private BluetoothAdapter	mmBluetoothAdapter	= null;
	private BluetoothSocket		mmSocket			= null;
	private InputStream			mmInStream			= null;
	private OutputStream		mmOutStream			= null;
	private BluetoothHandler	mmHandler			= new BluetoothHandler();

	public interface Callback
	{
		final char	CONNECT_ERROR		= 'Y';
		final char	SOCKET_CLOSED		= 'Z';
		final char	STOP_PERFORMANCE	= 'S';
		
		void BluetoothRespose(char c);
	}

	static private BluetoothConnectedThread.Callback mCallback = null;

	public BluetoothConnectedThread(Callback callback)
	{
		mCallback = callback;
	}

	boolean Init(BluetoothAdapter bluetoothAdapter, final String mac)
	{
		if (null == bluetoothAdapter)
		{
			Logger.Log.e("bluetoothAdapter is null");
			return false;
		}
		Cancel();
		mmBluetoothAdapter = bluetoothAdapter;
		BluetoothSocket tmp = null;
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(mac);
		if (null == device)
		{
			Logger.Log.e("bluetoth device is null");
			return false;
		}
		try
		{
			tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
		}
		catch (IOException e)
		{
			Logger.Log.e("device", "createRfcommSocketToServiceRecord(MY_UUID)");
			return false;
		}
		mmSocket = tmp;
		InputStream tmpIn = null;
		OutputStream tmpOut = null;
		if (null != mmSocket)
		{ // Get the input and output streams, using temp objects because
			// member streams are final
			try
			{
				tmpIn = mmSocket.getInputStream();
				tmpOut = mmSocket.getOutputStream();
			}
			catch (IOException e)
			{
				Logger.Log.e("mmSocket.*Stream()", "is failed");
				return false;
			}

		}
		else
		{
			Logger.Log.e("mmSocket", "is null");
			return false;
		}
		mmInStream = tmpIn;
		mmOutStream = tmpOut;
		return true;
	}

	@Override
	public void run()
	{
		super.run();
		if (mmBluetoothAdapter.isDiscovering())
		{
			mmBluetoothAdapter.cancelDiscovery();
		}
		try
		{
			mmSocket.connect();
		}
		catch (IOException connectException)
		{
			Logger.Log.e("mmSocket.connect()", "is failed");
			try
			{
				mmSocket.close();
			}
			catch (IOException closeException)
			{
				Logger.Log.t("mmSocket.close()", "is failed");
			}
			mCallback.BluetoothRespose(Callback.CONNECT_ERROR);
			return;
		}

		byte[] buffer = new byte[256]; // buffer store for the stream
		int bytes = 0; // bytes returned from read()

		// Keep listening to the InputStream until an exception occurs
		while (true)
		{
			if (null == mmInStream)
			{
				try
				{
					Thread.sleep(10000);
				}
				catch (InterruptedException e)
				{
				}
				continue;
			}
			try
			{
				// Read from the InputStream
				bytes = mmInStream.read(buffer);
				mmHandler.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();
			}
			catch (IOException e)
			{
				break;
			}
		}
		Logger.Log.t("Thread END");
	}

	public boolean isConnected()
	{
		if (null == mmSocket)
		{
			return false;
		}
		return mmSocket.isConnected();
	}

	/* Call this from the main activity to send data to the remote device */
	public void Send(char c)
	{
		if (0 == c)
		{
			return;
		}
		try
		{
			mmOutStream.write(c);
		}
		catch (IOException e)
		{
			mCallback.BluetoothRespose(Callback.SOCKET_CLOSED);
			Logger.Log.t(" Send(char)", e.getMessage());
		}
	}

	public void Send(String message)
	{
		byte[] msgBuffer = message.getBytes();
		try
		{
			mmOutStream.write(msgBuffer);
		}
		catch (IOException e)
		{
			Logger.Log.t("Send(String)", e.getMessage());
		}
	}

	/* Call this from the main activity to shutdown the connection */
	public void Cancel()
	{
		interrupt();
		try
		{
			if (null != mmInStream)
			{
				mmInStream.close();
			}
		}
		catch (IOException e1)
		{
		}
		try
		{
			if (null != mmOutStream)
			{
				mmOutStream.close();
			}
		}
		catch (IOException e1)
		{
		}
		try
		{
			if (null != mmSocket)
			{
				mmSocket.close();
			}
		}
		catch (IOException e)
		{
		}
		mmInStream = null;
		mmOutStream = null;
		mmSocket = null;
	}

	private static class BluetoothHandler extends Handler
	{
		public void handleMessage(android.os.Message msg)
		{
			switch (msg.what)
			{
				case BlueToothHelper.RECIEVE_MESSAGE:
					byte[] readBuf = (byte[]) msg.obj;
					String strIncom = new String(readBuf, 0, msg.arg1);
					mCallback.BluetoothRespose(strIncom.charAt(0));
					break;
				default:
					break;
			}
		};
	};

}// class BluetoothConnectedThread
