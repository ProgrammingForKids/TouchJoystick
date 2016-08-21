package com.car.programmator.ui;

import java.util.ArrayList;
import com.car.programmator.util.*;

import android.app.Activity;
import android.app.Dialog;
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
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements BlueToothHelper.Callback, UIHelper.Callback
{
	private BlueToothHelper		_bth			= null;
	private UIHelper			_ui				= null;
	private BroadcastReceiver	mReceiver		= null;

	Handler						mHandler		= new Handler();
	final long					mInterval		= 10000;
	Thread						mCheckThread	= null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// _eraser = new Eraser(this, _selected);
		_bth = new BlueToothHelper(this);
		_bth.registerCallBack(this);
		_ui = new UIHelper(this);
		_ui.registerCallBack(this);
		// Create a BroadcastReceiver for ACTION_FOUND
		mReceiver = new BroadcastReceiver()
		{
			public void onReceive(Context context, Intent intent)
			{
				if (null == _bth)
				{
					return;
				}
				String action = intent.getAction();
				// When discovery finds a device
				if (BluetoothDevice.ACTION_FOUND.equals(action))
				{
					// Get the BluetoothDevice object from the Intent
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					if (null != device)
					{

						_bth.FoundDeviceList().add(device.getName() + "@" + device.getAddress());
					}
				}
			}
		};
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter);

		mCheckThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					try
					{
						Thread.sleep(10000);
						mHandler.post(new Runnable()
						{

							@Override
							public void run()
							{
								if (null != _bth)
								{
									_bth.isConnected();
								}
							}
						});
					}
					catch (Exception e)
					{
					}
				}
			}
		});

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
			}
		}
		// mCheckThread.start();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		mCheckThread.interrupt();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		_bth.Finalize();
		if (null != mReceiver)
		{
			unregisterReceiver(mReceiver);
		}
	}

	private void BTConnect()
	{
		_bth.StartDiscovery();
		_bth.checkBTState();
		if (_bth.isReady())
		{
			_bth.IsBondedDevice();
			ShowDevices(_bth.DevicesList());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
		// else if (id == R.id.action_devices_list)
		// {
		// // ShowDevices(_bth.FoundDeviceList());
		// }

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
		Toast.makeText(this, "Response: " + c, Toast.LENGTH_LONG).show();
		if (!_ui.IsPerformedValid())
		{
			return;
		}
		char cd = _ui.OpcodeCurrent();
		_ui.Unselect();
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
		StartPerform();
	}

	@Override
	public void onStopPerform()
	{
		StopPerform();
	}

	@Override
	public void BTRespose(final char c)
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
						_bth.BTGetDevice(item);
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

}// class MainActivity
