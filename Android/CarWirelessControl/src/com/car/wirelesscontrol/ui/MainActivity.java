package com.car.wirelesscontrol.ui;

import java.lang.reflect.Method;
import java.util.ArrayList;

import com.car.programmator.ui.R;
import com.car.wirelesscontrol.util.*;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements BlueToothHelper.Callback, UIHelper.Callback
{
	private BlueToothHelper		_bth					= null;
	private UIHelper			_ui						= null;
	private BroadcastReceiver	_mreceiver				= null;
	Heartbeat					_hbeat					= new Heartbeat();
	Handler						_handler				= new Handler();
	private boolean				_is_discovery_finished	= false;
	private MenuItem			_menuitem_devicelist;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		_bth = new BlueToothHelper(this, this);
		_ui = new UIHelper(this, this);

		_mreceiver = new BroadcastReceiver()// Create a BroadcastReceiver for ACTION_FOUND
		{
			public void onReceive(Context context, Intent intent)
			{
				String action = intent.getAction();
				Logger.Log.t("BroadcastReceiver", action);
				// When discovery finds a device
				if (BluetoothDevice.ACTION_FOUND.equals(action))
				{
					// Get the BluetoothDevice object from the Intent
					if (null != _menuitem_devicelist)
					{
						_menuitem_devicelist.setEnabled(false);
					}
					if (_is_discovery_finished)
					{
						_is_discovery_finished = false;
						_bth.FoundDeviceList().clear();
					}
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					if (null != device)
					{

						_bth.FoundDeviceList().add(device.getName() + "@" + device.getAddress());
					}
				}
				if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
				{
					int size = _bth.FoundDeviceList().size();
					Logger.Log.t("BroadcastReceiver", "List", size);
					_is_discovery_finished = true;
					if (1 > size)
					{
						_bth.FoundDeviceList().add("Found Device List is empty");
					}
					if (null != _menuitem_devicelist)
					{
						_menuitem_devicelist.setEnabled(true);
					}
				}
			}
		};
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(_mreceiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(_mreceiver, filter);
	}// onCreate

	@Override
	public void onResume()
	{
		super.onResume();
		if (null != _bth)
		{
			if (!_bth.isConnected())
			{
				_bth.StartDiscovery();
				if (null != _menuitem_devicelist)
				{
					_menuitem_devicelist.setEnabled(false);
				}
			}
		}
	}

	@Override
	protected void onStop()
	{
		super.onStop();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		_bth.Finalize();
		if (null != _mreceiver)
		{
			unregisterReceiver(_mreceiver);
		}
	}

	private void BTConnect()
	{
		_hbeat.Stop();
		_bth.IsBondedDevice();
		ShowDevices(_bth.DevicesList());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		_ui.SetMenuItem(menu.findItem(R.id.action_current));
		_menuitem_devicelist = menu.findItem(R.id.action_devices_list);
		if (null != _menuitem_devicelist)
		{
			_menuitem_devicelist.setEnabled(false);
		}
		if (menu.getClass().getSimpleName().equals("MenuBuilder"))
		{
			try
			{
				Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
				m.setAccessible(true);
				m.invoke(menu, true);
			}
			catch (NoSuchMethodException e)
			{
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
		{
			BTConnect();
		}
		else if (id == R.id.action_load_file)
		{
			ChooseFileDialog dlg = new ChooseFileDialog(ChooseFileDialog.DIALOG_LOAD_FILE);
			dlg.ShowDialog(this, _ui);
		}
		else if (id == R.id.action_save_file)
		{
			ChooseFileDialog dlg = new ChooseFileDialog(ChooseFileDialog.DIALOG_SAVE_FILE);
			dlg.ShowDialog(this, _ui);

		}
		else if (id == R.id.action_devices_list)
		{
			ShowDevices(_bth.FoundDeviceList());
		}

		return super.onOptionsItemSelected(item);
	}

	private void StartPerform()
	{
		_ui.StartBntToStop();
		_ui.ToStart();
		_ui.PerformMode(true);
		Performing();
	}

	private void StopPerform()
	{
		_ui.PerformMode(false);
		_ui.StartBntToStart();
	}

	void Performing()
	{
		if (!_ui.PerformMode())
		{
			return;
		}
		char opcode = _ui.OpcodeToDo();
		if (0 == opcode)
		{
			StopPerform();
		}
		else
		{
			_bth.Send(opcode);
		}
	}

	private void ResposeToUiThread(char c)
	{
		if (!_ui.PerformMode())
		{
			if (c == 'H' || c == 'X')
			{
				_bth.isConnected();
			}
			return;
		}
		if (!_ui.IsPerformedValid())
		{
			return;
		}
		char cd = _ui.Unselect().OpcodeCurrent();
		if ((cd - c) == ('a' - 'A'))
		{
			Performing();
		}
		else if (STOP_PERFORMANCE == c)
		{
			StopPerform();
		}
		else if (SOCKET_CLOSED == c || CONNECT_ERROR == c)
		{
			StopPerform();
			_bth.SetLED(false);
		}
		else
		{
			StopPerform();
			_ui.PerformError();
		}

	}

	@Override
	public void onStartPerform()
	{
		_bth.isConnected();
		StartPerform();
	}

	@Override
	public void onStopPerform()
	{
		StopPerform();
	}

	@Override
	public void BluetoothResponse(final char c)
	{
		Logger.Log.t(c);
		this.runOnUiThread(new Runnable()
		{
			public void run()
			{
				ResposeToUiThread(c);
			}
		});
	}

	void ShowDevices(final ArrayList<String> list)
	{
		if (null == _bth)
		{
			return;
		}
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.popup_window);
		{
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, list);
			ListView listView = (ListView) dialog.findViewById(R.id.listView1);
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new OnItemClickListener()
			{

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					TextView tv = (TextView) view;
					if (null != tv)
					{
						String item = tv.getText().toString();
						_bth.ConnectToBTDevice(item);
						dialog.dismiss();
					}

				}
			});
		}

		TextView item = (TextView) dialog.findViewById(R.id.dismiss);
		OnClickListener clickListener = new OnClickListener()
		{

			public void onClick(View v)
			{
				dialog.dismiss();
			}
		};
		item.setOnClickListener(clickListener);
		dialog.show();

		// Size & Position
		Window window = dialog.getWindow();
		window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		window.setGravity(Gravity.CENTER_HORIZONTAL);

	}

	class Heartbeat
	{

		private boolean	bPause	= true;

		Thread			mThread	= new Thread(new Runnable()
								{

									@Override
									public void run()
									{
										while (true)
										{
											try
											{
												Thread.sleep(2000);
												if (!bPause)
												{
													if (!_ui.PerformMode())
													{
														_bth.Send('h');
													}
												}
											}
											catch (InterruptedException e)
											{
											}
										}
									}
								}, "Heartbeat");

		public Heartbeat()
		{
			mThread.start();
		}

		void Start()
		{
			bPause = false;
		}

		void Stop()
		{
			bPause = true;
		}
	}// class Heartbeat

	@Override
	public void HeartbeatStart(boolean b)
	{
		if (b)
		{
			_hbeat.Start();
		}
		else
		{
			_hbeat.Stop();
		}
	}

}// class MainActivity
