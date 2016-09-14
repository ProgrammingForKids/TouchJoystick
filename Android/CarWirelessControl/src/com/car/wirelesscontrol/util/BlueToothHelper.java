package com.car.wirelesscontrol.util;

import java.util.ArrayList;
import java.util.Set;

import com.car.programmator.ui.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

public class BlueToothHelper implements BluetoothConnectedThread.Callback
{
	static final int					REQUEST_ENABLE_BT			= 1;
	static final int					RECIEVE_MESSAGE				= 1;
	private final int					OurBluetoothClass			= BluetoothClass.Device.TOY_VEHICLE;

	private BluetoothAdapter			_bluetoothAdapter			= null;
	private BluetoothConnectedThread	_BluetoothConnectedThread	= null;
	private ArrayList<String>			_bondedDeviceList			= null;
	private ArrayList<String>			_foundDeviceList			= null;
	private final Activity				_activity;
	private ProgressDialog				_dialog						= null;

	public ArrayList<String> DevicesList()
	{
		return _bondedDeviceList;
	}

	public interface Callback
	{
		final char	CONNECT_ERROR		= BluetoothConnectedThread.Callback.CONNECT_ERROR;
		final char	SOCKET_CLOSED		= BluetoothConnectedThread.Callback.SOCKET_CLOSED;
		final char	STOP_PERFORMANCE	= BluetoothConnectedThread.Callback.STOP_PERFORMANCE;
		final char	STOP_OBSTACLE		= BluetoothConnectedThread.Callback.STOP_OBSTACLE;
		final char	PERFORM_ERROR		= BluetoothConnectedThread.Callback.PERFORM_ERROR;

		void BluetoothResponse(char c);

		void HeartbeatStart(boolean ret);
	}

	private Callback	mCallback;
	private int			mIndex			= -1;
	private boolean		mTryToConnect	= false;

	public BlueToothHelper(Activity activity, Callback callback)
	{
		this.mCallback = callback;
		_activity = activity;
		_activity.getActionBar().setTitle(BluetoothConnectedThread.DefaultDeviceName);
		_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		_bondedDeviceList = new ArrayList<String>();
		_foundDeviceList = new ArrayList<String>();
	}

	public void ShowConnectStatus(boolean bGreen)
	{
		int resId = (bGreen) ? R.drawable.green : R.drawable.red;
		this._activity.getActionBar().setIcon(resId);
	}

	public void ShowConnectStatus(String title, boolean bGreen)
	{
		this._activity.getActionBar().setTitle(title);
		ShowConnectStatus(bGreen);
	}

	public boolean IsConnected()
	{
		EndWait();
		boolean ret = false;
		String title = "";
		if (null != _BluetoothConnectedThread)
		{
			ret = _BluetoothConnectedThread.isConnected();
			title = _BluetoothConnectedThread.DeviceName();
		}

		ShowConnectStatus(title, ret);
		if (!ret)
		{
			if (mTryToConnect)
			{
				mIndex -= 1;
				mTryToConnect();
			}
		}
		else
		{
			mIndex = -1;
			mTryToConnect = false;
		}
		return ret;
	}

	public void StartDiscovery()
	{
		if (null != _bluetoothAdapter)
		{
			if (_bluetoothAdapter.isDiscovering())
			{
				_bluetoothAdapter.cancelDiscovery();
			}
			Logger.Log.t("BroadcastReceiver", "StartDiscovery");
			_bluetoothAdapter.startDiscovery();
		}
	}

	public void Finalize()
	{
		if (null != _BluetoothConnectedThread)
		{
			BluetoothConnectedThread tmpBluetoothConnectedThr = _BluetoothConnectedThread;
			tmpBluetoothConnectedThr.Cancel();
			_BluetoothConnectedThread = null;
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
				// BluetoothStatus();
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

	public void BondedDeviceList()
	{
		checkBTState();
		if (! (_bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON))
		{
			return;
		}

		_bondedDeviceList.clear();
		Set<BluetoothDevice> pairedDevices = _bluetoothAdapter.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0)
		{
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices)
			{
				if (IsOurClass(device.getBluetoothClass()))
				{
					String list = device.getName() + "@" + device.getAddress();
					_bondedDeviceList.add(list);
				}
			}
		}
	}

	public boolean IsOurClass(BluetoothClass cls)
	{
		Logger.Log.e("Bluetooth", "class", cls.getDeviceClass());
		return (OurBluetoothClass == cls.getDeviceClass());
	}

	public void ConnectToBluetothDevice(final String name_mac)
	{
		ConnectToBTDevice(name_mac);
		IsConnectedAsync();
	}

	private boolean ConnectToBTDevice(final String name_mac)
	{
		if (null == _bluetoothAdapter)
		{
			return false;
		}
		Logger.Log.t("AUTO", name_mac);
		int pos = name_mac.lastIndexOf("@");
		if (pos == -1)
		{
			return false;
		}
		final String mac = name_mac.substring(pos + 1);
		// Set up a pointer to the remote node using it's address.
		if (null != _BluetoothConnectedThread)
		{
			BluetoothConnectedThread tmp = _BluetoothConnectedThread;
			tmp.Cancel();
		}
		InitAndStartBluetoothConnectedThreadAsync(mac);
		return false;
	}

	void InitAndStartBluetoothConnectedThreadAsync(final String mac)
	{
		new android.os.Handler().postDelayed(
				new Runnable()
				{
					public void run()
					{
						boolean res = InitAndStartBluetoothConnectedThread(mac);
						Logger.Log.t("AUTO", mac, res);

					}
				},
				5000);
	}

	boolean InitAndStartBluetoothConnectedThread(final String mac)
	{
		_BluetoothConnectedThread = new BluetoothConnectedThread(this);
		if (_BluetoothConnectedThread.Init(_bluetoothAdapter, mac))
		{
			_BluetoothConnectedThread.start();
			IsConnectedAsync();
			return true;
		}
		return false;
	}

	void IsConnectedAsync()
	{
		new android.os.Handler().postDelayed(
				new Runnable()
				{
					public void run()
					{
						IsConnected();
					}
				},
				2000);
	}

	public void Send(final String comm)
	{
		if (null != _BluetoothConnectedThread)
		{
			_BluetoothConnectedThread.Send(comm);
		}
		else
		{
			mCallback.BluetoothResponse(Callback.CONNECT_ERROR);
		}
	}

	public void Send(final char c)
	{
		if (null != _BluetoothConnectedThread)
		{
			Logger.Log.t("SEND GET", c);
			_BluetoothConnectedThread.Send(c);
		}
		else
		{
			mCallback.BluetoothResponse(Callback.CONNECT_ERROR);
		}
	}

	@Override
	public void BluetoothRespose(char c)
	{
		mCallback.BluetoothResponse(c);
	}

	public void TryToConnect()
	{
		BondedDeviceList();
		mIndex = _bondedDeviceList.size() - 1;
		if (-1 < mIndex)
		{
			mTryToConnect = true;
			mTryToConnect();
		}
		else
		{
			Logger.Log.t("No Bonded Devices");
		}
	}

	public void mTryToConnect()
	{
		if (0 <= mIndex && _bondedDeviceList.size() > mIndex)
		{
			BeginWait();
			ConnectToBTDevice(_bondedDeviceList.get(mIndex));
		}
		else
		{
			mIndex = -1;
			mTryToConnect = false;
		}
	}

	private void BeginWait()
	{
		_dialog = CustomProgressDialog.ctor(_activity);// ProgressDialog.show(_activity, null, "// ",
		_dialog.show();
	}

	private void EndWait()
	{
		if (null != _dialog)
		{
			if (_dialog.isShowing())
			{
				_dialog.dismiss();
			}
		}
	}
}// class BlueToothHelper
