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

	private BluetoothAdapter			m_bluetoothAdapter			= null;
	private BluetoothConnectedThread	m_bluetoothConnectedThread	= null;
	private ProgressDialog				m_dialog					= null;
	private final ArrayList<String>		m_bondedDeviceList;
	private final ArrayList<String>		m_foundDeviceList;
	private final Activity				m_activity;
	private final ActionBarHelper		m_actionbar;

	public ArrayList<String> DevicesList()
	{
		return m_bondedDeviceList;
	}

	public interface Callback
	{
		final byte	CONNECT_ERROR	= BluetoothConnectedThread.Callback.CONNECT_ERROR;
		final byte	SOCKET_CLOSED	= BluetoothConnectedThread.Callback.SOCKET_CLOSED;
		final byte	PERFORM_ERROR	= BluetoothConnectedThread.Callback.PERFORM_ERROR;

		void BluetoothResponse(byte c);
	}

	private Callback	mCallback		= null;
	private int			mIndex			= -1;
	private boolean		mTryToConnect	= false;

	public BlueToothHelper(Activity activity, Callback callback)
	{
		mCallback = callback;
		m_activity = activity;
		m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		m_bondedDeviceList = new ArrayList<String>();
		m_foundDeviceList = new ArrayList<String>();
		m_actionbar = new ActionBarHelper(m_activity);
		m_actionbar.ShowStatus(BluetoothConnectedThread.DefaultDeviceName, false);
	}

	public boolean IsConnected()
	{
		boolean ret = false;
		if (null != m_bluetoothConnectedThread)
		{
			ret = m_bluetoothConnectedThread.isConnected();
		}
		m_actionbar.ShowStatus(ret);
		return ret;
	}

	public boolean ShowConnectStatus()
	{
		EndWait();
		if (null == m_bluetoothConnectedThread)
		{
			Logger.Log.t("BluetoothConnectedThread==null");
			m_actionbar.ShowStatus(BluetoothConnectedThread.DefaultDeviceName, false);
			return false;
		}

		Pair<Boolean, String> p = m_bluetoothConnectedThread.IsConnected();
		m_actionbar.ShowStatus(p.second, p.first);
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
		if (null != m_bluetoothAdapter)
		{
			if (m_bluetoothAdapter.isDiscovering())
			{
				m_bluetoothAdapter.cancelDiscovery();
			}
			Logger.Log.t("BroadcastReceiver", "StartDiscovery");
			m_bluetoothAdapter.startDiscovery();
		}
	}

	public void CancelDiscovery()
	{
		if (null != m_bluetoothAdapter)
		{
			if (m_bluetoothAdapter.isDiscovering())
			{
				m_bluetoothAdapter.cancelDiscovery();
			}
		}
	}

	public void Finalize()
	{
		if (null != m_bluetoothConnectedThread)
		{
			BluetoothConnectedThread tmpBluetoothConnectedThr = m_bluetoothConnectedThread;
			tmpBluetoothConnectedThr.Cancel();
			m_bluetoothConnectedThread = null;
		}
	}

	public ArrayList<String> FoundDeviceList()
	{
		return m_foundDeviceList;
	}

	public boolean checkBTState()
	{
		if (m_bluetoothAdapter == null)
		{
			return false;
		}
		else
		{
			if (m_bluetoothAdapter.isEnabled())
			{
				// BluetoothStatus();
				return true;
			}
			else
			{
				// Prompt user to turn on Bluetooth
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				this.m_activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
		return true;
	}

	public void BondedDeviceList()
	{
		checkBTState();
		if (!(m_bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON))
		{
			return;
		}

		m_bondedDeviceList.clear();
		Set<BluetoothDevice> pairedDevices = m_bluetoothAdapter.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0)
		{
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices)
			{
				if (IsOurClass(device.getBluetoothClass()))
				{
					String list = device.getName() + "@" + device.getAddress();
					m_bondedDeviceList.add(list);
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
		if (null == m_bluetoothAdapter)
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
		if (null != m_bluetoothConnectedThread)
		{
			BluetoothConnectedThread tmp = m_bluetoothConnectedThread;
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
		m_bluetoothConnectedThread = new BluetoothConnectedThread(this);
		if (m_bluetoothConnectedThread.Init(m_bluetoothAdapter, mac))
		{
			m_bluetoothConnectedThread.start();
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
		if (null != m_bluetoothConnectedThread)
		{
			m_bluetoothConnectedThread.Send(comm);
		}
		else
		{
			mCallback.BluetoothResponse(Callback.CONNECT_ERROR);
			ShowBluetoothStatus();
		}
	}

	public void Send(final byte comm)
	{
		if (null != m_bluetoothConnectedThread)
		{
			m_bluetoothConnectedThread.Send(comm);
		}
		else
		{
			mCallback.BluetoothResponse(Callback.CONNECT_ERROR);
			ShowBluetoothStatus();
		}
	}

	@Override
	public void BluetoothRespose(byte c)
	{
		mCallback.BluetoothResponse(c);
	}

	public void TryToConnect()
	{
		BondedDeviceList();
		mIndex = m_bondedDeviceList.size() - 1;
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
		if (0 <= mIndex && m_bondedDeviceList.size() > mIndex)
		{
			BeginWait();
			if (ConnectToBTDevice(m_bondedDeviceList.get(mIndex)))
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
		m_dialog = CustomProgressDialog.ctor(m_activity);// ProgressDialog.show(m_activity, null, "// ",
		m_dialog.show();
	}

	public void EndWait()
	{
		if (null != m_dialog)
		{
			if (m_dialog.isShowing())
			{
				m_dialog.dismiss();
			}
		}
	}

	@Override
	public void ShowBluetoothStatus()
	{
		Logger.Log.t("ShowBluetoothStatus");
		m_activity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				boolean b = ShowConnectStatus();
				Logger.Log.t("Connect STATUS1", b);
			}
		});
	}

	@Override
	public void ShowBluetoothErrorStatus()
	{
		m_activity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				// m_actionbar.ShowStatus(false);
			}
		});
	}
}// class BlueToothHelper
