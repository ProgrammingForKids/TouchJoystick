package com.sasa.joystick.carwirelesscontrol;

import java.lang.reflect.Method;
import java.util.ArrayList;

import com.car.wirelesscontrol.util.*;
import com.sasa.joystick.JoystickControl;
import com.sasa.joystick.JoystickControl.OnJoystickMoveListener;
import com.sasa.logger.Logger;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements BlueToothHelper.Callback
{
	private BlueToothHelper		m_bth			= null;
	private BroadcastReceiver	m_receiver		= null;
	private JoystickControl		m_joystick		= null;
	private TextView			m_angle			= null;
	private TextView			m_power			= null;;
	private TextView			m_byte_command	= null;
	private ImageView			mImageView		= null;
	private byte				m_comm			= 0;
	private final SoundHelper	m_sound			= new SoundHelper();

	private final int			max_count_click	= 5;
	private int					m_count_click	= 0;
	private boolean				mTrace			= false;
	private MenuItem			m_adlistItem	= null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		m_bth = new BlueToothHelper(this, this);

		m_angle = (TextView) findViewById(R.id.angleTextView);
		m_power = (TextView) findViewById(R.id.powerTextView);
		m_byte_command = (TextView) findViewById(R.id.byte_to_bt);
		mImageView = (ImageView) findViewById(R.id.carImageView);

		m_joystick = (JoystickControl) findViewById(R.id.joystickView);
		m_joystick.setOnJoystickMoveListener(new OnJoystickMoveListener()
		{

			@Override
			public void onValueChanged(int angle, int power, int direction)
			{
				byte t = CommandByteBuilder.PrepareCommandByte(angle, power, direction);
				if (m_comm != t)
				{
					m_comm = t;
					m_byte_command.setText(" " + CommandByteBuilder.ByteToStr(m_comm));
					mImageView.setRotation(angle);
					m_bth.Send(m_comm);
				}
				m_angle.setText(" " + String.valueOf(angle) + "°");
				m_power.setText(" " + String.valueOf(power) + "%");
				//Logger.Log.t("Direction",JoystickControl.DirectionToPrompt(direction));
			}
		}, JoystickControl.DEFAULT_LOOP_INTERVAL);

		m_receiver = new BroadcastReceiver()// Create a BroadcastReceiver for ACTION_FOUND
		{
			public void onReceive(Context context, Intent intent)
			{
				String action = intent.getAction();
				Logger.Log.t("BroadcastReceiver", action);
				if (BluetoothDevice.ACTION_FOUND.equals(action))
				{
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					if (null != device)
					{
						if (m_bth.IsOurClass(device.getBluetoothClass()))
						{
							m_bth.FoundDeviceList().add(device.getName() + "@" + device.getAddress());
						}
					}
				}
				if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
				{
					int size = m_bth.FoundDeviceList().size();
					Logger.Log.t("BroadcastReceiver", "List", size);
					if (1 > size)
					{
						m_bth.FoundDeviceList().add("Found Device List is empty");
					}
					m_bth.EndWait();
					m_bth.StartDiscovery();
					ShowDevices(m_bth.FoundDeviceList());
				}
			}
		};
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(m_receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(m_receiver, filter);
	}// onCreate

	@Override
	public void onResume()
	{
		super.onResume();
		m_sound.Create(this);
		if (null != m_bth)
		{
			if (!m_bth.IsConnected())
			{
				// m_bth.TryToConnect();
			}
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		m_sound.Release();
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
		m_bth.Finalize();
		if (null != m_receiver)
		{
			unregisterReceiver(m_receiver);
		}
	}

	private void ShowBondedDeviceList()
	{
		m_bth.BondedDeviceList();
		ShowDevices(m_bth.DevicesList());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		m_adlistItem = menu.findItem(R.id.action_devices_list);
		if (null != m_adlistItem)
		{
			m_adlistItem.setVisible(mTrace);
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
			m_count_click = 0;
			ShowBondedDeviceList();
		}
		else if (id == R.id.action_devices_list)
		{
			m_bth.BeginWait();
			m_bth.FoundDeviceList().clear();
			m_bth.StartDiscovery();
			m_count_click = 0;
		}
		else if (id == R.id.action_current)
		{
			m_count_click += 1;
			if (m_count_click >= max_count_click)
			{
				mTrace = !mTrace;
				if (null != m_adlistItem)
				{
					m_adlistItem.setVisible(mTrace);
				}
				m_count_click = 0;
			}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void BluetoothResponse(final char c)
	{
		Logger.Log.t(c);
		this.runOnUiThread(new Runnable()
		{
			public void run()
			{
				// ResposeToUiThread(c);
			}
		});
	}

	void ShowDevices(final ArrayList<String> list)
	{
		if (null == m_bth)
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
						m_bth.ConnectToBluetothDevice(item);
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
