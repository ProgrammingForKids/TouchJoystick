package com.sasa.joystick.carwirelesscontrol;

import java.lang.reflect.Method;
import java.util.ArrayList;

import com.car.wirelesscontrol.util.*;

import com.sasa.logger.Logger;
import com.sasa.singleaxisjoystick.SingleAxisJoystickControl;
import com.sasa.singleaxisjoystick.SingleAxisJoystickControl.OnJoystickMoveListener;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements BlueToothHelper.Callback
{
	private BlueToothHelper				m_bth			= null;
	private CommandSender				m_csend			= null;
	private BroadcastReceiver			m_receiver		= null;
	private SingleAxisJoystickControl	joystickLeft;
	private SingleAxisJoystickControl	joystickRight;
	private TextView					m_byte_command	= null;
	private final SoundHelper			m_sound			= new SoundHelper();

	private final int					max_count_click	= 5;
	private int							m_count_click	= 0;
	private boolean						mTrace			= false;
	private MenuItem					m_adlistItem	= null;
	private MenuItem					m_actCurrent	= null;
	private TextView					m_prompt		= null;
	private LinearLayout				m_traceArea		= null;

	private Integer[]					m_gradation		= { 10, 100, 30, 60 };
	private int							m_valueleft		= 0;
	private int							m_valueright	= 0;
	private byte						m_byte			= 0;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		m_bth = new BlueToothHelper(this, this);
		m_csend = new CommandSender(m_bth);
		m_byte_command = (TextView) findViewById(R.id.byte_to_bt);
		m_byte_command.setText(CommandByteBuilder.ByteToStr((byte) 0) + ";left: " + m_valueleft + "; right: " + m_valueright);

		m_traceArea = (LinearLayout) findViewById(R.id.traceArea);
		m_prompt = (TextView) findViewById(R.id.promptTextView);
		m_prompt.setMovementMethod(new ScrollingMovementMethod());
		// clear prompt area if LongClick
		m_prompt.setOnLongClickListener(new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(View v)
			{
				m_prompt.setText("");
				return true;
			}
		});
		m_traceArea.setVisibility((mTrace) ? View.VISIBLE : View.GONE);

		joystickLeft = (SingleAxisJoystickControl) findViewById(R.id.joystickViewLeft);
		joystickLeft.SetGradation(m_gradation);
		joystickLeft.setOnJoystickMoveListener(new OnJoystickMoveListener()
		{

			@Override
			public void onValueChanged(int value, boolean bTogether)
			{
				m_valueleft = value;
				if (bTogether)
				{
					m_valueright = value;
				}
				BuildAndSendCommand();
			}
		}, SingleAxisJoystickControl.DEFAULT_LOOP_INTERVAL);
		joystickRight = (SingleAxisJoystickControl) findViewById(R.id.joystickViewRight);
		joystickRight.SetGradation(m_gradation);
		joystickRight.setOnJoystickMoveListener(new OnJoystickMoveListener()
		{

			@Override
			public void onValueChanged(int value, boolean bTogether)
			{
				m_valueright = value;
				if (bTogether)
				{
					m_valueleft = value;
				}
				BuildAndSendCommand();
			}

		}, SingleAxisJoystickControl.DEFAULT_LOOP_INTERVAL);
		joystickLeft.SetSibling(joystickRight);
		joystickRight.SetSibling(joystickLeft);
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
					m_bth.CancelDiscovery();
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
		if (null != m_csend)
		{
			m_csend.Start();
		}
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
		if (null != m_csend)
		{
			m_csend.Stop();
		}
		m_sound.Release();
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

	private void BuildAndSendCommand()
	{
		m_byte = CommandByteBuilder.PrepareCommandBytelt(m_valueleft, m_valueright);
		m_byte_command.setText(CommandByteBuilder.ByteToStr(m_byte) + ";left: " + m_valueleft + "; right: " + m_valueright);
		m_csend.Send(m_byte);
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
		m_actCurrent = menu.findItem(R.id.action_current);
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
			// ByteHelper bh = new ByteHelper();
			// bh.test((byte) 0);
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
					if (null != m_actCurrent)
					{
						int iconId = (mTrace) ? R.drawable.debug : R.drawable.runtime;
						m_actCurrent.setIcon(iconId);
					}
					if (null != m_traceArea)
					{
						m_traceArea.setVisibility((mTrace) ? View.VISIBLE : View.GONE);
					}
					m_adlistItem.setVisible(mTrace);
				}
				m_count_click = 0;
			}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void BluetoothResponse(final byte c)
	{
		Logger.Log.t("SEND RESPONSE", Integer.toHexString(c & 0xFF));
		if ((0 != ((c >> 7) & 1)) || (0 != ((c >> 6) & 1)))
		{
			m_csend.StopSending();
		}
		this.runOnUiThread(new Runnable()
		{
			public void run()
			{
				ResposeToUiThread(c);
			}
		});
	}

	private void ResposeToUiThread(byte b)
	{
		SetPrompt("\u21fe" + CommandByteBuilder.ByteToStr(b) + "\n");
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

	public void SetPrompt(String txt)
	{
		if (m_prompt.isShown())
		{
			String str = m_prompt.getText().toString();
			m_prompt.setText(txt + str);
		}

	}
}// class MainActivity
