package com.example.bluetooth1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import com.example.bluetooth1.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
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
	private static final String		TAG					= "bluetooth1";
	final int						RECIEVE_MESSAGE		= 1;
	Button							_sendCommands;
	EditText						_commands;

	private static final int		REQUEST_ENABLE_BT	= 1;
	private BluetoothAdapter		mBluetoothAdapter	= null;
	private BluetoothSocket			_btSocket			= null;
	BroadcastReceiver				mReceiver			= null;
	private StringBuilder			_sb					= new StringBuilder();
	private ListView				_list;

	private ArrayList<String>		_arrayList;

	private ArrayAdapter<String>	_adapter;
	Handler							h;
	private ConnectedThread			mConnectedThread;

	private static final UUID		MY_UUID				= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		// {//listview
		// _list = (ListView) findViewById(R.id.listView1);
		// _arrayList = new ArrayList<String>();
		// _adapter = new ArrayAdapter<String>(getApplicationContext(),
		// android.R.layout.simple_spinner_item, _arrayList)
		// {
		// public View getView(int position, View convertView, android.view.ViewGroup parent)
		// {
		// // Get the Item from ListView
		// View view = super.getView(position, convertView, parent);
		//
		// // Initialize a TextView for ListView each Item
		// TextView tv = (TextView) view.findViewById(android.R.id.text1);
		//
		// // Set the text color of TextView (ListView Item)
		// tv.setTextColor(Color.RED);
		//
		// // Generate ListView Item using TextView
		// return view;
		// };
		// };
		// _list.setAdapter(_adapter);
		// _list.setOnItemClickListener(new OnItemClickListener()
		// {
		//
		// public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		// {
		// TextView tv = (TextView) view;
		// if (null != tv)
		// {
		// String item = tv.getText().toString();
		// int pos = item.lastIndexOf("@");
		// String mac = item.substring(pos + 1);
		// Toast.makeText(getBaseContext(), mac, Toast.LENGTH_SHORT).show();
		// BTGetDevice(mac);
		// }
		//
		// }
		// });
		// }
		{
			_commands = (EditText) findViewById(R.id.commands);
			_sendCommands = (Button) findViewById(R.id.btnOn);
		}
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		checkBTState();

		_sendCommands.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Editable comm = _commands.getText();
				if (null != mConnectedThread)
				{
					mConnectedThread.Send(comm.toString());
				}
				Toast.makeText(getBaseContext(), comm.toString(), Toast.LENGTH_SHORT).show();
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
		h = new Handler()
		{
			public void handleMessage(android.os.Message msg)
			{
				switch (msg.what)
				{
					case RECIEVE_MESSAGE:
						byte[] readBuf = (byte[]) msg.obj;
						String strIncom = new String(readBuf, 0, msg.arg1);
						Log.e("ZHOPA", strIncom);
						break;
				}
			};
		};

	}

	@Override
	protected void onStart()
	{
		super.onStart();
	}

	private void IsBondedDevice()
	{
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0)
		{
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices)
			{
				// Add the name and address to an array adapter to show in a ListView
				// mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
				Log.e("BLUET", device.getName() + "\n" + device.getAddress());

			}
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		// mBluetoothAdapter.startDiscovery();
		String address = "98:D3:31:FB:21:B2";
		BTGetDevice(address);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		btFinalize();
	}

	private void btFinalize()
	{
		if (null != mReceiver)
		{
			unregisterReceiver(mReceiver);
		}
		if (null != _btSocket)
		{
			try
			{
				_btSocket.close();
			}
			catch (IOException e)
			{

			}
		}
		if (null != mConnectedThread)
		{
			mConnectedThread.Cancel();
		}
	}

	private void BTGetDevice(final String address)
	{
		if (null == mBluetoothAdapter)
			return;
		// Set up a pointer to the remote node using it's address.
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

		// Two things are needed to make a connection:
		// A MAC address, which we got above.
		// A Service ID or UUID. In this case we are using the
		// UUID for SPP.
		if (null != _btSocket)
		{
			try
			{
				_btSocket.close();
			}
			catch (IOException e2)
			{
			}
		}
		try
		{
			_btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
		}
		catch (IOException e)
		{
			errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
		}
		// Discovery is resource intensive. Make sure it isn't going on
		// when you attempt to connect and pass your message.
		mBluetoothAdapter.cancelDiscovery();
		try
		{
			_btSocket.connect();
		}
		catch (IOException e)
		{
			try
			{
				_btSocket.close();
			}
			catch (IOException e2)
			{
				errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
			}
		}
		mConnectedThread = new ConnectedThread(_btSocket);
		mConnectedThread.start();
	}

	@Override
	public void onPause()
	{
		super.onPause();

		// try
		// {
		// _btSocket.close();
		// }
		// catch (IOException e2)
		// {
		// errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage());
		// }
	}

	private void checkBTState()
	{
		// Check for Bluetooth support and then check to make sure it is turned on
		// Emulator doesn't support Bluetooth and will return null
		if (mBluetoothAdapter == null)
		{
			errorExit("Fatal Error", "Bluetooth null");
		}
		else
		{
			if (mBluetoothAdapter.isEnabled())
			{
				Log.d(TAG, "Bluetooth isEnabled");
			}
			else
			{
				// Prompt user to turn on Bluetooth
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
	}

	private void errorExit(String title, String message)
	{
		Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
		finish();
	}

	private class ConnectedThread extends Thread
	{
		private final BluetoothSocket	mmSocket;
		private final InputStream		mmInStream;
		private final OutputStream		mmOutStream;

		public ConnectedThread(BluetoothSocket socket)
		{
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try
			{
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			}
			catch (IOException e)
			{
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run()
		{
			byte[] buffer = new byte[256]; // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true)
			{
				try
				{
					// Read from the InputStream
					bytes = mmInStream.read(buffer);
					h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();
				}
				catch (IOException e)
				{
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void Send(String message)
		{
			Log.d(TAG, "..." + message + "...");
			byte[] msgBuffer = message.getBytes();
			try
			{
				mmOutStream.write(msgBuffer);
			}
			catch (IOException e)
			{
				Log.d(TAG, "...: " + e.getMessage() + "...");
			}
		}

		/* Call this from the main activity to shutdown the connection */
		public void Cancel()
		{
			try
			{
				mmSocket.close();
			}
			catch (IOException e)
			{
			}
		}
	}

}// MainActivity
