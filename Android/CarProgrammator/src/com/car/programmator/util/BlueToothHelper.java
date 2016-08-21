package com.car.programmator.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import com.car.programmator.ui.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;

public class BlueToothHelper
{
	static final int			REQUEST_ENABLE_BT	= 1;
	final static int			RECIEVE_MESSAGE		= 1;
	private final String		device_name			= "Unknown";
	final Activity				_activity;
	private BluetoothAdapter	_bluetoothAdapter	= null;
	private ConnectedThread		_connectedThread	= null;
	private BluetoothHandler	_handler			= null;
	private ArrayList<String>	_bondedDeviceList	= null;
	private ArrayList<String>	_foundDeviceList	= null;
	private String				_device_name		= device_name;

	public ArrayList<String> DevicesList()
	{
		return _bondedDeviceList;
	}

	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	public interface Callback
	{
		final char	CONNECT_ERROR		= 'Y';
		final char	SOCKET_CLOSED		= 'Z';
		final char	STOP_PERFORMANCE	= 'S';

		void BTRespose(char c);
	}

	static private Callback mCallback;

	public void registerCallBack(Callback callback)
	{
		BlueToothHelper.mCallback = callback;
	}

	public BlueToothHelper(Activity activity)
	{
		this._activity = activity;
		_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		_handler = new BluetoothHandler();
		_bondedDeviceList = new ArrayList<String>();
		_foundDeviceList = new ArrayList<String>();

	}

	public boolean isConnected()
	{
		boolean ret = false;
		if (null != _connectedThread)
		{
			ret = _connectedThread.isConnected();
		}
		String title = _device_name;// + " : " + ((ret) ? "Connected" : "Disconnected");
		this._activity.getActionBar().setTitle(title);
		int resId = (ret) ? R.drawable.green : R.drawable.red;
		this._activity.getActionBar().setIcon(resId);
		return ret;
	}

	public void StartDiscovery()
	{
		_bluetoothAdapter.startDiscovery();
	}

	public void Finalize()
	{
		_device_name = device_name;
		if (null != _connectedThread)
		{
			_connectedThread.Cancel();
		}
	}

	public ArrayList<String> FoundDeviceList()
	{
		return _foundDeviceList;
	}

	public boolean checkBTState()
	{
		if (_bluetoothAdapter == null)
		{
			return false;
		}
		else
		{
			if (_bluetoothAdapter.isEnabled())
			{
				BluetoothStatus();
				return true;
			}
			else
			{
				// Prompt user to turn on Bluetooth
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				this._activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
		return true;
	}

	public void IsBondedDevice()
	{
		_bondedDeviceList.clear();
		Set<BluetoothDevice> pairedDevices = _bluetoothAdapter.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0)
		{
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices)
			{
				String list = device.getName() + "@" + device.getAddress();
				_bondedDeviceList.add(list);
				Logger.Log.t("BondedDevice", list);
			}
		}
	}

	void BluetoothStatus()
	{
		String status;
		if (_bluetoothAdapter.isEnabled())
		{
			String mydeviceaddress = _bluetoothAdapter.getAddress();
			String mydevicename = _bluetoothAdapter.getName();
			int state = _bluetoothAdapter.getState();
			status = mydevicename + " : " + mydeviceaddress + " : " + State(state);
		}
		else
		{
			status = "Bluetooth Off";
		}
		Logger.Log.t("BluetoothStatus", status);
		// this._activity.getActionBar().setTitle(status);
		// Toast.makeText(this._activity.getBaseContext(), status, Toast.LENGTH_LONG).show();
	}

	public boolean isReady()
	{
		return (_bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON);
	}

	String State(int s)
	{
		switch (s)
		{
			case BluetoothAdapter.STATE_ON:
				return "On";
			case BluetoothAdapter.STATE_TURNING_ON:
				return "Turning On";
			case BluetoothAdapter.STATE_OFF:
				return "Off";
			case BluetoothAdapter.STATE_TURNING_OFF:
				return "Turning Off";
			default:
				break;
		}
		return "";
	}

	public void BTGetDevice(final String name_mac)
	{
		if (null == _bluetoothAdapter)
		{
			return;
		}
		int pos = name_mac.lastIndexOf("@");
		if (pos == -1)
		{
			return;
		}
		String mac = name_mac.substring(pos + 1);
		// Set up a pointer to the remote node using it's address.
		BluetoothDevice device = _bluetoothAdapter.getRemoteDevice(mac);
		if (null != device)
		{
			_device_name = device.getName();
		}
		else
		{
			_device_name = device_name;
			this._activity.getActionBar().setTitle("No Bluetooth Connection");
		}
		if (null == _connectedThread)
		{
			_connectedThread = new ConnectedThread(device);
			_connectedThread.start();
		}
		else
		{
			ConnectedThread tmp = _connectedThread;
			tmp.Cancel();

			_connectedThread = new ConnectedThread(device);
			_connectedThread.start();
		}
		new android.os.Handler().postDelayed(
				new Runnable()
				{
					public void run()
					{
						isConnected();
					}
				},
				2000);
	}

	public void Send(final String comm)
	{
		if (null != _connectedThread)
		{
			_connectedThread.Send(comm);
		}
		else
		{
			mCallback.BTRespose(Callback.CONNECT_ERROR);
		}
	}

	public void Send(final char c)
	{
		if (null != _connectedThread)
		{
			_connectedThread.Send(c);
		}
		else
		{
			mCallback.BTRespose(Callback.CONNECT_ERROR);
		}
	}

	private class ConnectedThread extends Thread
	{
		private BluetoothSocket	mmSocket;
		private InputStream		mmInStream;
		private OutputStream	mmOutStream;

		public ConnectedThread(BluetoothDevice device)
		{

			BluetoothSocket tmp = null;
			try
			{
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			}
			catch (IOException e)
			{
			}
			mmSocket = tmp;
			// mmSocket = socket;
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
					Logger.Log.t("mmSocket.*Stream()", "is failed");
				}

			}
			else
			{
				Logger.Log.t("mmSocket", "is null");
			}
			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run()
		{
			_bluetoothAdapter.cancelDiscovery();
			try
			{
				mmSocket.connect();
			}
			catch (IOException connectException)
			{
				Logger.Log.t("mmSocket.connect()", "is failed");
				try
				{
					mmSocket.close();
				}
				catch (IOException closeException)
				{
					Logger.Log.t("mmSocket.close()", "is failed");
				}
				mCallback.BTRespose(Callback.CONNECT_ERROR);
				return;
			}
			byte[] buffer = new byte[256]; // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true)
			{
				try
				{
					// Read from the InputStream
					bytes = mmInStream.read(buffer);
					_handler.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();
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
				mCallback.BTRespose(Callback.SOCKET_CLOSED);
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
				mmInStream.close();
				mmInStream = null;
			}
			catch (IOException e1)
			{
			}
			try
			{
				mmOutStream.close();
				mmOutStream = null;
			}
			catch (IOException e1)
			{
			}
			try
			{
				mmSocket.close();
				mmSocket = null;
			}
			catch (IOException e)
			{
			}
		}
	}// class ConnectedThread

	private static class BluetoothHandler extends Handler
	{
		public void handleMessage(android.os.Message msg)
		{
			switch (msg.what)
			{
				case BlueToothHelper.RECIEVE_MESSAGE:
					byte[] readBuf = (byte[]) msg.obj;
					String strIncom = new String(readBuf, 0, msg.arg1);
					mCallback.BTRespose(strIncom.charAt(0));
					break;
				default:
					break;
			}
		};
	};

	public static boolean checkConnected()
	{
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		boolean connected = false;
		for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices())
		{
			try
			{
				try
				{
					Method m = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
					try
					{
						BluetoothSocket bs = (BluetoothSocket) m.invoke(device, Integer.valueOf(1));
						bs.connect();
						connected = true;
						break;
					}
					catch (IOException e)
					{
					}
				}
				catch (IllegalArgumentException e)
				{
				}
				catch (IllegalAccessException e)
				{
				}
				catch (InvocationTargetException e)
				{
				}
			}
			catch (SecurityException e)
			{
			}
			catch (NoSuchMethodException e)
			{
			}
		}
		return connected;
	}

}// class BlueToothHelper
