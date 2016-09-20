package com.car.wirelesscontrol.ui;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Stack;

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
	private boolean				__SIMULATOR__	= false;
	private BlueToothHelper		m_bth			= null;
	private UIHelper			m_ui			= null;
	private BroadcastReceiver	m_receiver		= null;
	private final SoundHelper	m_sound			= new SoundHelper();
	private final Simulator		m_simulator		= new Simulator();

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
		m_ui = new UIHelper(this, this);
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
		m_ui.SetMenuItem(menu.findItem(R.id.action_current));

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
		else if (id == R.id.action_load_file)
		{
			m_count_click = 0;
			ChooseFileDialog dlg = new ChooseFileDialog(ChooseFileDialog.DIALOG_LOAD_FILE);
			dlg.ShowDialog(this, m_ui);
		}
		else if (id == R.id.action_save_file)
		{
			m_count_click = 0;
			ChooseFileDialog dlg = new ChooseFileDialog(ChooseFileDialog.DIALOG_SAVE_FILE);
			dlg.ShowDialog(this, m_ui);
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
				m_ui.SetPrompt(mTrace);
				m_count_click = 0;
			}
		}

		return super.onOptionsItemSelected(item);
	}

	private void StopPerform()
	{
		m_ui.PerformMode(false);
		m_ui.StartBntToStart();
	}

	private void ResposeToUiThread(char c)
	{
		char cd = m_ui.Unselect().OpcodeCurrent();
		Logger.Log.e("RESPONSE", c, cd);
		m_ui.SetPrompt("[" + c + ":" + cd + "] ");
		if ((cd - c) == ('a' - 'A'))
		{
			m_ui.Select();
			int next = m_ui.AskNextCommandChunk();
			switch (next)
			{
				case UIHelper.YES:
					String pkg = m_ui.GetNextCommandChunk();
					SendComandPkg(pkg);
					return;
				case UIHelper.NO:
					return;
				case UIHelper.STOP:
					c = STOP_PERFORMANCE;
					break;
				default:
					break;
			}
		}
		StopPerform();
		switch (c)
		{
			case STOP_PERFORMANCE:
			{
				m_sound.PlayDing();
				break;
			}
			case STOP_OBSTACLE:
			{
				m_sound.PlayHorn();
				m_ui.PerformObstacle();
				break;
			}
			case SOCKET_CLOSED:
			case CONNECT_ERROR:
			{
				break;
			}
			default:
			{
				m_sound.PlayCrush();
				m_ui.PerformError();
			}

		}

	}

	@Override
	public void onStartPerform()
	{
		if (m_bth.IsConnected() || __SIMULATOR__)
		{
			m_ui.PerformMode(true);
			m_ui.StartBntToStop();
			m_ui.PreparationForStart();
			m_ui.Select();
			String pkg = m_ui.GetFirstCommandChunk();
			if (pkg.length() == 0)
			{
				BluetoothResponse(PERFORM_ERROR);
				return;
			}
			m_sound.PlayDing();
			if (__SIMULATOR__)
			{
				m_simulator.Start();
			}
			SendComandPkg(pkg);
		}
	}

	void SendComandPkg(final String pkg)
	{
		if (__SIMULATOR__)
		{
			m_simulator.Send(pkg);
		}
		else
		{
			m_bth.Send(pkg);
		}
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

	class Simulator
	{
		Stack<Character> cmd = new Stack<Character>();

		void Send(String buf)
		{
			for (int k = 0; k < buf.length(); ++k)
			{
				cmd.add(0, buf.charAt(k));
			}
			return;
		}

		void Start()
		{
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{

					while (true)
					{
						try
						{
							Thread.sleep(1000);
						}
						catch (InterruptedException e)
						{
						}
						if (0 == cmd.size())
						{
							break;
						}
						char c = cmd.pop();
						c -= ('a' - 'A');
						BluetoothResponse(c);

					}
				}
			}, "SendImitator").start();
		}
	}// Simulator

}// class MainActivity
