package com.car.programmator.util;

import java.util.ArrayList;
import java.util.Set;

import com.car.programmator.ui.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

public class BlueToothHelper implements BluetoothConnectedThread.Callback
{
	static final int			REQUEST_ENABLE_BT			= 1;
	final static int			RECIEVE_MESSAGE				= 1;
	private final String		device_name					= "Unknown";
	private BluetoothAdapter	_bluetoothAdapter			= null;
	BluetoothConnectedThread	_BluetoothConnectedThread	= null;
	private ArrayList<String>	_bondedDeviceList			= null;
	private ArrayList<String>	_foundDeviceList			= null;
	private String				_device_name				= device_name;
	final Activity				_activity;

	public ArrayList<String> DevicesList()
	{
		return _bondedDeviceList;
	}

	public interface Callback
	{
		final char	CONNECT_ERROR		= BluetoothConnectedThread.Callback.CONNECT_ERROR;
		final char	SOCKET_CLOSED		= BluetoothConnectedThread.Callback.SOCKET_CLOSED;
		final char	STOP_PERFORMANCE	= BluetoothConnectedThread.Callback.STOP_PERFORMANCE;

		void BTRespose(char c);

		void HeartbeatStart(boolean ret);
	}

	private Callback			mCallback;

	public void registerCallBack(Callback callback)
	{
		this.mCallback = callback;
	}

	public BlueToothHelper(Activity activity)
	{
		_activity = activity;
		_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		_bondedDeviceList = new ArrayList<String>();
		_foundDeviceList = new ArrayList<String>();
	}

	public void SetLED(boolean bGreen)
	{
		int resId = (bGreen) ? R.drawable.green : R.drawable.red;
		this._activity.getActionBar().setIcon(resId);
	}

	public boolean isConnected()
	{
		boolean ret = false;
		if (null != _BluetoothConnectedThread)
		{
			ret = _BluetoothConnectedThread.isConnected();
		}
		String title = _device_name;
		this._activity.getActionBar().setTitle(title);
		int resId = (ret) ? R.drawable.green : R.drawable.red;
		mCallback.HeartbeatStart(ret);
		this._activity.getActionBar().setIcon(resId);
		return ret;
	}

	public void startScanDevice()
	{
//		// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//		{
//			mScanCallback = new ScanCallback()
//			{
//				@Override
//				public void onScanResult(int callbackType, ScanResult result)
//				{
//					super.onScanResult(callbackType, result);
//					Logger.Log.t("SCANNER", result.getDevice());
//				}
//			};
//
//			final BluetoothManager bluetoothManager = (BluetoothManager) _activity.getSystemService(Context.BLUETOOTH_SERVICE);
//			mBluetoothAdapter = bluetoothManager.getAdapter();
//			BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
//			bluetoothLeScanner.startScan(mScanCallback);
//		}
//		// else
//		// {
//		// bluetoothAdapter.startLeScan(leScanCallback);
//		// }
	}

	public void StartDiscovery()
	{
		if (null != _bluetoothAdapter)
		{
			Logger.Log.t("BroadcastReceiver", "StartDiscovery");
			_bluetoothAdapter.startDiscovery();
		}
	}

	public void Finalize()
	{
		_device_name = device_name;
		if (null != _BluetoothConnectedThread)
		{
			BluetoothConnectedThread tmp = _BluetoothConnectedThread;
			tmp.Cancel();
			tmp.interrupt();
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

	public void ConnectToBTDevice(final String name_mac)
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
		final String mac = name_mac.substring(pos + 1);
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
		if (null != _BluetoothConnectedThread)
		{
			BluetoothConnectedThread tmp = _BluetoothConnectedThread;
			tmp.Cancel();
			tmp.interrupt();
		}

		_BluetoothConnectedThread = new BluetoothConnectedThread(this);
		_BluetoothConnectedThread.Init(_bluetoothAdapter, mac);
		_BluetoothConnectedThread.start();

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
		if (null != _BluetoothConnectedThread)
		{
			_BluetoothConnectedThread.Send(comm);
		}
		else
		{
			mCallback.BTRespose(Callback.CONNECT_ERROR);
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
			mCallback.BTRespose(Callback.CONNECT_ERROR);
		}
	}

	@Override
	public void BluetoothRespose(char c)
	{
		mCallback.BTRespose(c);
	}
}// class BlueToothHelper
