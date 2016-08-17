package com.car.programmator.ui;

import java.util.ArrayList;
import com.car.programmator.util.*;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.SparseArray;
import android.util.SparseIntArray;
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
import android.widget.ImageView.ScaleType;

public class MainActivity extends Activity implements BlueToothHelper.Callback
{

	private final int				_BACK		= 107;
	private final int				_FORWARD	= _BACK + 1;
	private final int				_LEFT		= _BACK + 2;
	private final int				_RIGHT		= _BACK + 3;
	private final int				_EMPTY		= _BACK + 4;

	private SparseArray<Character>	opCodes		= new SparseArray<Character>()
												{

													{
														put(_BACK, 'b');
														put(_FORWARD, 'f');
														put(_LEFT, 'l');
														put(_RIGHT, 'r');
													}
												};

	private SparseIntArray			_rc			= new SparseIntArray()
												{

													{
														put(_FORWARD, R.drawable.f);
														put(_BACK, R.drawable.b);
														put(_LEFT, R.drawable.l);
														put(_RIGHT, R.drawable.r);
														put(_EMPTY, R.drawable.empty);
													}
												};

	class Selected
	{
		public View	view	= null;
		public int	index	= -1;

		void Ini()
		{
			this.view = null;
			this.index = -1;
		}

		Selected Set(View v, int index)
		{
			this.view = v;
			this.index = index;
			return this;
		}

		int GetId()
		{
			if (null != view)
			{
				return view.getId();
			}
			return -1;
		}

		void Select()
		{
			if (null != this.view)
			{
				this.view.setAlpha((float) 0.5);
				this.view.setPadding(6, 6, 6, 6);
				this.view.setBackgroundColor(Color.BLUE);
				Logger.Log.t("SELECT");
			}
		}

		void UnSelect()
		{
			if (null != this.view)
			{
				Logger.Log.t("UN-SELECT");
				// this.view.getPaddingBottom()
				this.view.setAlpha((float) 1.0);
				this.view.setPadding(0, 0, 0, 0);
			}
		}

		void SetImage(Context context, int recId)
		{
			if (null == view)
			{
				return;
			}
			if (null != context)
			{
				Drawable drawable = ContextCompat.getDrawable(context, recId);
				((ImageView) _performed.view).setImageDrawable(drawable);
			}
		}

		void RestoreImage(Context context)
		{
			if (null == view)
			{
				return;
			}
			if (null != context)
			{
				int drawableId = _rc.get(view.getId());
				Drawable drawable = ContextCompat.getDrawable(context, drawableId);
				((ImageView) _performed.view).setImageDrawable(drawable);
			}
		}

	}// class Selected

	LinearLayout			_area_tools		= null;
	LinearLayout			_current_area	= null;
	TextView				_prompt			= null;
	Selected				_performed		= new Selected();
	Selected				_selected		= new Selected();
	Selected				_insert			= new Selected();
	private FlowLayout		_command_aria	= null;
	private BlueToothHelper	_bth			= null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		_bth = new BlueToothHelper(this);
		_bth.registerCallBack(this);

		_area_tools = (LinearLayout) findViewById(R.id.area_tools);
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
		Store(_FORWARD);
		Store(_BACK);
		Store(_LEFT);
		Store(_RIGHT);
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
		if (null != _bth)
		{
			_bth.isConnected();
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

	OnClickListener		_OnClickCommStep		= new OnClickListener()
												{
													@Override
													public void onClick(View v)
													{
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
																_insert.Set(CreateImage(_EMPTY), k);
																_command_aria.addView(_insert.view, _insert.index);
																break;
															}
														}
													}
												};
	OnClickListener		_OnClickListener		= new OnClickListener()
												{

													@Override
													public void onClick(View v)
													{
														Logger.Log.t("longTouch", "ID", v.getId());
														ImageView iv = CreateImage(v.getId());
														iv.setOnClickListener(_OnClickCommStep);
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

	OnLongClickListener	myOnLongClickListener	= new OnLongClickListener()
												{

													@Override
													public boolean onLongClick(View v)
													{
														ClipData data = ClipData.newPlainText("", "");
														DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
														v.startDrag(data, shadowBuilder, v, 0);
														return true;
													}

												};

	OnDragListener		myOnDragListener		= new OnDragListener()
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
																View view = (View) event.getLocalState();
																_command_aria.removeView(view);
															default:
																break;
														}
														return true;
													}

												};

	void Store(int id)
	{
		ImageView iv = CreateImage(id);
		iv.setPadding(5, 5, 5, 5);
		iv.setOnClickListener(_OnClickListener);
		_area_tools.addView(iv);
	}

	ImageView CreateImage(int id)
	{
		int drawableId = _rc.get(id);
		ImageView iv = new ImageView(this);
		Drawable drawable = ContextCompat.getDrawable(this, drawableId);

		iv.setImageDrawable(drawable);
		iv.setId(id);
		iv.setAdjustViewBounds(true);
		iv.setScaleType(ScaleType.CENTER_INSIDE);
		return iv;
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
		else if (id == R.id.action_start)
		{
			StartPerform();
		}
		return super.onOptionsItemSelected(item);
	}

	private void StartPerform()
	{
		_performed.RestoreImage(this);
		_performed.index = 0;
		Perform();
	}

	void Perform()
	{
		if (null != _insert.view)
		{
			return;
		}
		if (-1 < _performed.index && _performed.index < _command_aria.getChildCount())
		{
			_performed.view = _command_aria.getChildAt(_performed.index);
			_performed.Select();
			_bth.Send(opCodes.get(_performed.view.getId()));
			_performed.index += 1;
		}
	}

	@Override
	public void BTRespose(char c)
	{
		Logger.Log.t(c);
		Toast.makeText(this, "Response: " + c, Toast.LENGTH_LONG).show();
		char pc = opCodes.get(_performed.GetId());
		if ((pc - c) == ('a' - 'A'))
		{
			_performed.UnSelect();
			Perform();
		}
		else if (SOCKET_CLOSED == c
				|| STOP_PERFORMANCE == c
				|| CONNECT_ERROR == c)
		{
			_performed.UnSelect();
		}
		else
		{
			_performed.UnSelect();
			_performed.SetImage(this, R.drawable.x);
		}

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
