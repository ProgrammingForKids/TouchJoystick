package com.car.programmator.ui;

import java.util.ArrayList;
import com.car.programmator.util.*;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements BlueToothHelper.Callback
{

	private final int		SB_START		= 10;
	private final int		SB_STOP			= 11;
	ImageHelper				_performed		= new ImageHelper();
	ImageHelper				_selected		= new ImageHelper();
	ImageHelper				_insert			= new ImageHelper();
	TextView				_prompt			= null;
	ImageView				_startBnt		= null;
	LinearLayout			_area_tools		= null;
	LinearLayout			_current_area	= null;
	private FlowLayout		_command_aria	= null;
	private BlueToothHelper	_bth			= null;
	private boolean			_PerformMode	= false;
	Eraser					_eraser			= null;
	// private BroadcastReceiver mReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		_eraser = new Eraser(this);
		_bth = new BlueToothHelper(this);
		_bth.registerCallBack(this);
		_area_tools = (LinearLayout) findViewById(R.id.area_tools);
		_startBnt = (ImageView) findViewById(R.id.image_tools);
		_startBnt.setId(SB_START);
		_startBnt.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				int id = _startBnt.getId();
				switch (id)
				{
					case SB_START:
						StartPerform();
						break;
					case SB_STOP:
						StopPerform();
						break;
					default:
						break;
				}
			}
		});

		_command_aria = (FlowLayout) findViewById(R.id.test);
		_command_aria.setOnDragListener(myOnDragListener);

		_prompt = (TextView) findViewById(R.id.prompt);
		_prompt.setVisibility(View.GONE);
		if (_prompt.isShown())
		{
			// make TextView scrollable
			_prompt.setMovementMethod(new ScrollingMovementMethod());
			// clear prompt area if LongClick
			_prompt.setOnLongClickListener(new OnLongClickListener()
			{

				@Override
				public boolean onLongClick(View v)
				{
					_prompt.setText("");
					return true;
				}
			});
		}
		ImageHelper.Store(this, OpCode._FORWARD, _area_tools, _OnClickListenerOpcodeToView);
		ImageHelper.Store(this, OpCode._BACK, _area_tools, _OnClickListenerOpcodeToView);
		ImageHelper.Store(this, OpCode._LEFT, _area_tools, _OnClickListenerOpcodeToView);
		ImageHelper.Store(this, OpCode._RIGHT, _area_tools, _OnClickListenerOpcodeToView);

		// Create a BroadcastReceiver for ACTION_FOUND
		// mReceiver = new BroadcastReceiver()
		// {
		// public void onReceive(Context context, Intent intent)
		// {
		// if (null == _bth)
		// {
		// return;
		// }
		// String action = intent.getAction();
		// // When discovery finds a device
		// if (BluetoothDevice.ACTION_FOUND.equals(action))
		// {
		// // Get the BluetoothDevice object from the Intent
		// BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		// if (null != device)
		// {
		//
		// _bth.FoundDeviceList().add(device.getName() + "@" + device.getAddress());
		// }
		// }
		// ShowDevices(_bth.FoundDeviceList());
		//
		// }
		// };
		// // Register the BroadcastReceiver
		// IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		// registerReceiver(mReceiver, filter);

	}// onCreate

	@Override
	public void onResume()
	{
		super.onResume();
		if (null != _bth)
		{
			_bth.isConnected();
		}
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		// if (null != mReceiver)
		// {
		// unregisterReceiver(mReceiver);
		// }
		_bth.Finalize();
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

	OnClickListener		_OnClickSelectInsert			= new OnClickListener()
														{
															@Override
															public void onClick(View v)
															{
																if (_PerformMode)
																{
																	return;
																}
																Logger.Log.t("SELECT");
																if (v.equals(_selected.view))
																{
																	_selected.UnSelect();
																	_command_aria.removeView(_insert.view);
																	_insert.Ini();
																	_selected.Ini();
																	return;
																}
																int count = _command_aria.getChildCount();
																for (int k = 0; k < count; ++k)
																{
																	View test = _command_aria.getChildAt(k);
																	if (test.equals(v))
																	{
																		_selected.Set(v, k).Select();
																		_insert.Set(ImageHelper.CreateImage(MainActivity.this, OpCode._EMPTY), k);
																		_command_aria.addView(_insert.view, _insert.index);
																		break;
																	}
																}
															}
														};

	OnClickListener		_OnClickListenerOpcodeToView	= new OnClickListener()
														{

															@Override
															public void onClick(View v)
															{
																if (_PerformMode)
																{
																	return;
																}
																Logger.Log.t("longTouch", "ID", v.getId());
																ImageView iv = ImageHelper.CreateImage(MainActivity.this, v.getId());
																iv.setOnClickListener(_OnClickSelectInsert);
																iv.setOnLongClickListener(myOnLongClickListener);
																if (-1 < _insert.index)
																{
																	_command_aria.addView(iv, _insert.index);
																	_command_aria.removeView(_insert.view);
																	_selected.UnSelect();
																	_insert.Ini();
																	_selected.Ini();
																}
																else
																{
																	_command_aria.addView(iv);
																}
															}
														};

	OnLongClickListener	myOnLongClickListener			= new OnLongClickListener()
														{

															@Override
															public boolean onLongClick(View v)
															{
																if (_PerformMode)
																{
																	return true;
																}
																ClipData data = ClipData.newPlainText("", "");
																DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
																v.startDrag(data, shadowBuilder, v, 0);
																return true;
															}

														};

	OnDragListener		myOnDragListener				= new OnDragListener()
														{

															@Override
															public boolean onDrag(View v, DragEvent event)
															{
																switch (event.getAction())
																{
																	case DragEvent.ACTION_DRAG_STARTED:
																		break;
																	case DragEvent.ACTION_DRAG_ENTERED:
																		break;
																	case DragEvent.ACTION_DRAG_EXITED:
																		break;
																	case DragEvent.ACTION_DROP:
																		break;
																	case DragEvent.ACTION_DRAG_ENDED:
																		if (!_PerformMode)
																		{
																			View view = (View) event.getLocalState();
																			_command_aria.removeView(view);
																		}
																		break;
																	default:
																		break;
																}
																return true;
															}

														};

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
		if (id == R.id.action_devices_list)
		{
			// _bth.StartDiscovery();
		}
		else if (id == R.id.action_settings)
		{
			BTConnect();
		}
		return super.onOptionsItemSelected(item);
	}

	private void StartPerform()
	{
		_startBnt.setId(SB_STOP);
		_startBnt.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.stop));

		_performed.RestoreImage(this);
		_performed.index = 0;
		_PerformMode = true;
		Perform();
	}

	private void StopPerform()
	{
		_PerformMode = false;
		_startBnt.setId(SB_START);
		_startBnt.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.start));
	}

	void Perform()
	{
		if (!_PerformMode)
		{
			return;
		}
		if (null != _insert.view)
		{
			StopPerform();
			return;
		}
		if (-1 < _performed.index && _performed.index < _command_aria.getChildCount())
		{
			_performed.view = _command_aria.getChildAt(_performed.index);
			_performed.Select();
			_bth.Send(OpCode.OpcodeC(_performed.Opcode()));
			_performed.index += 1;
		}
		else
		{
			StopPerform();
		}
	}

	private void ResposeToUiThread(char c)
	{
		Toast.makeText(this, "Response: " + c, Toast.LENGTH_LONG).show();
		if (null == _performed)
		{
			return;
		}
		char pc = OpCode.OpcodeC(_performed.Opcode());
		if ((pc - c) == ('a' - 'A'))
		{
			_performed.UnSelect();
			Perform();
		}
		else if (SOCKET_CLOSED == c
				|| STOP_PERFORMANCE == c
				|| CONNECT_ERROR == c)
		{
			StopPerform();
			_performed.UnSelect();
		}
		else
		{
			StopPerform();
			_performed.UnSelect();
			_performed.SetImage(this, R.drawable.x);
		}

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
			ListView listview = (ListView) dialog.findViewById(R.id.listView1);
			ArrayAdapter<String> _adapter_bonded = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, list)
			{
				public View getView(int position, View convertView, android.view.ViewGroup parent)
				{
					View view = super.getView(position, convertView, parent);
					TextView tv = (TextView) view.findViewById(android.R.id.text1);
					tv.setTextColor(Color.BLACK);
					tv.setPadding(8, 16, 2, 16);
					return view;
				};
			};
			listview.setAdapter(_adapter_bonded);
			listview.setOnItemClickListener(new OnItemClickListener()
			{

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

}// MainActivity
