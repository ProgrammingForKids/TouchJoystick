package com.car.wirelesscontrol.util;

import java.util.ArrayList;
import java.util.Set;

import com.sasa.logger.Logger;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Pair;

public class BlueToothHelper implements BluetoothConnectedThread.Callback
{
	static final int					REQUEST_ENABLE_BT			= 1;
	static final int					RECIEVE_MESSAGE				= 1;
	private final int					OurBluetoothClass			= BluetoothClass.Device.TOY_VEHICLE;

	private BluetoothAdapter			_bluetoothAdapter			= null;
	private BluetoothConnectedThread	_BluetoothConnectedThread	= null;
	private ProgressDialog				_dialog						= null;
	private final ArrayList<String>		_bondedDeviceList;
	private final ArrayList<String>		_foundDeviceList;
	private final Activity				_activity;
	private final ActionBarHelper		_actionbar;

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
	}

	private Callback	mCallback		= null;
	private int			mIndex			= -1;
	private boolean		mTryToConnect	= false;

	public BlueToothHelper(Activity activity, Callback callback)
	{
		mCallback = callback;
		_activity = activity;
		_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		_bondedDeviceList = new ArrayList<String>();
		_foundDeviceList = new ArrayList<String>();
		_actionbar = new ActionBarHelper(_activity);
		_actionbar.ShowStatus(BluetoothConnectedThread.DefaultDeviceName, false);
	}

	public boolean IsConnected()
	{
		boolean ret = false;
		if (null != _BluetoothConnectedThread)
		{
			ret = _BluetoothConnectedThread.isConnected();
		}
		_actionbar.ShowStatus(ret);
		return ret;
	}

	public boolean ShowConnectStatus()
	{
		EndWait();
		if (null == _BluetoothConnectedThread)
		{
			Logger.Log.t("BluetoothConnectedThread==null");
			_actionbar.ShowStatus(BluetoothConnectedThread.DefaultDeviceName, false);
			return false;
		}

		Pair<Boolean, String> p = _BluetoothConnectedThread.IsConnected();
		_actionbar.ShowStatus(p.second, p.first);
		if (!p.first)
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
		return p.first;
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

	public void CancelDiscovery()
	{
		if (null != _bluetoothAdapter)
		{
			if (_bluetoothAdapter.isDiscovering())
			{
				_bluetoothAdapter.cancelDiscovery();
			}
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
		if (!(_bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON))
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
		BeginWait();
		if (!ConnectToBTDevice(name_mac))
		{
			EndWait();
		}
	}

	private boolean ConnectToBTDevice(final String name_mac)
	{
		if (null == _bluetoothAdapter)
		{
			return false;
		}
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
		return true;
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
						IsConnectedAsync();
					}
				},
				1000);
	}

	boolean InitAndStartBluetoothConnectedThread(final String mac)
	{
		_BluetoothConnectedThread = new BluetoothConnectedThread(this);
		if (_BluetoothConnectedThread.Init(_bluetoothAdapter, mac))
		{
			_BluetoothConnectedThread.start();
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
						ShowConnectStatus();
					}
				},
				5000);
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
			ShowBluetoothStatus();
		}
	}
	public void Send(final byte comm)
	{
		if (null != _BluetoothConnectedThread)
		{
			_BluetoothConnectedThread.Send(comm);
		}
		else
		{
			mCallback.BluetoothResponse(Callback.CONNECT_ERROR);
			ShowBluetoothStatus();
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
			if (ConnectToBTDevice(_bondedDeviceList.get(mIndex)))
			{
				EndWait();
			}
		}
		else
		{
			mIndex = -1;
			mTryToConnect = false;
		}
	}

	public void BeginWait()
	{
		_dialog = CustomProgressDialog.ctor(_activity);// ProgressDialog.show(_activity, null, "// ",
		_dialog.show();
	}

	public void EndWait()
	{
		if (null != _dialog)
		{
			if (_dialog.isShowing())
			{
				_dialog.dismiss();
			}
		}
	}

	@Override
	public void ShowBluetoothStatus()
	{
		Logger.Log.t("ShowBluetoothStatus");
		_activity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				boolean b = ShowConnectStatus();
				Logger.Log.t("Connect STATUS1",b);
			}
		});
	}

	@Override
	public void ShowBluetoothErrorStatus()
	{
		_activity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				// _actionbar.ShowStatus(false);
			}
		});
	}
}// class BlueToothHelper
