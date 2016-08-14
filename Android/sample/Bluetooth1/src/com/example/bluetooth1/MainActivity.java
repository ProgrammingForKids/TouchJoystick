package com.example.bluetooth1;

import java.util.ArrayList;
import com.example.bluetooth1.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
{
	private static final String		TAG			= "bluetooth1";

	Button							_sendCommands;
	EditText						_commands;

	BroadcastReceiver				mReceiver	= null;
	private ListView				_list;

	private ArrayAdapter<String>	_adapter;
	private BlueToothHelper			_bth;
	private Dialog					m_dialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		{
			_commands = (EditText) findViewById(R.id.commands);
			_sendCommands = (Button) findViewById(R.id.btnOn);
		}
		_bth = new BlueToothHelper(this);

		_sendCommands.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (null != _bth)
				{
					Editable comm = _commands.getText();
					_bth.Send(comm.toString());
					Toast.makeText(getBaseContext(), comm.toString(), Toast.LENGTH_SHORT).show();
				}
			}
		});

		// // Create a BroadcastReceiver for ACTION_FOUND
		// mReceiver = new BroadcastReceiver()
		// {
		// public void onReceive(Context context, Intent intent)
		// {
		// String action = intent.getAction();
		// // When discovery finds a device
		// if (BluetoothDevice.ACTION_FOUND.equals(action))
		// {
		// // Get the BluetoothDevice object from the Intent
		// BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		// // Add the name and address to an array adapter to show in a ListView
		// if (null != device)
		// {
		//
		// _arrayList.add(device.getName() + "@" + device.getAddress());
		// _adapter.notifyDataSetChanged();
		// Log.e("BLUET", "[" + device.getName() + "]:[" + device.getAddress() + "]");
		// }
		// }
		// }
		// };
		// // Register the BroadcastReceiver
		// IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		// registerReceiver(mReceiver, filter);

		// ====================================================================

	}

	@Override
	protected void onStart()
	{
		super.onStart();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		_bth.checkBTState();
		if (_bth.isReady())
		{
			ShowPopup();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode)
		{
			case BlueToothHelper.REQUEST_ENABLE_BT:
				if (RESULT_OK == resultCode)
				{
					_bth.BluetoothStatus();
				}
				else
				{
					finish();
				}
				break;
		}
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		if (null != mReceiver)
		{
			unregisterReceiver(mReceiver);
		}
		_bth.Finalize();
	}

	@Override
	public void onPause()
	{
		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	void ShowPopup()
	{
		if (null == _bth)
		{
			return;
		}
		_bth.IsBondedDevice();
		m_dialog = new Dialog(this);
		m_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		m_dialog.setContentView(R.layout.popup_window);

		{// listview
			_list = (ListView) m_dialog.findViewById(R.id.listView1);
			_adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, _bth.DevicesList())
			{
				public View getView(int position, View convertView, android.view.ViewGroup parent)
				{
					View view = super.getView(position, convertView, parent);
					TextView tv = (TextView) view.findViewById(android.R.id.text1);
					tv.setTextColor(Color.BLACK);
					return view;
				};
			};
			_list.setAdapter(_adapter);
			_list.setOnItemClickListener(new OnItemClickListener()
			{

				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					TextView tv = (TextView) view;
					if (null != tv)
					{
						String item = tv.getText().toString();
						_bth.BTGetDevice(item);
						m_dialog.dismiss();
					}

				}
			});
		}

		TextView item = (TextView) m_dialog.findViewById(R.id.dismiss);
		OnClickListener clickListener = new OnClickListener()
		{
			public void onClick(View v)
			{
				m_dialog.dismiss();
			}
		};
		item.setOnClickListener(clickListener);
		m_dialog.show();
		// Size & Position
		Window window = m_dialog.getWindow();
		window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		window.setGravity(Gravity.CENTER_HORIZONTAL);

	}
}// MainActivity
