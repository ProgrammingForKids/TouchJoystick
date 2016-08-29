package com.car.wirelesscontrol.ui;

import com.car.programmator.ui.R;
import com.car.wirelesscontrol.util.Logger;
import com.car.wirelesscontrol.util.OpCode;

import android.app.Activity;
import android.content.ClipData;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.view.DragEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class UIHelper implements Eraser.Callback
{
	private final int	SB_START		= R.drawable.start;
	private final int	SB_STOP			= R.drawable.stop;
	ImageHelper			_performed		= new ImageHelper();
	ImageSelected		_imgselected	= null;
	TextView			_prompt			= null;
	ImageView			_startBnt		= null;
	Eraser				_eraser			= null;
	LinearLayout		_area_tools		= null;
	LinearLayout		_current_area	= null;
	private FlowLayout	_command_aria	= null;
	private boolean		_PerformMode	= false;
	final Activity		activity;

	public interface Callback
	{
		void onStartPerform();

		void onStopPerform();
	}

	private Callback mCallback;

	void PerformMode(boolean b)
	{
		_PerformMode = b;
	}

	boolean PerformMode()
	{
		return _PerformMode;
	}

	public UIHelper(Activity activity, Callback callback)
	{
		mCallback = callback;
		this.activity = activity;
		_imgselected = new ImageSelected(activity);
		_eraser = new Eraser(activity, _imgselected);
		_eraser.registerCallBack(this);
		_area_tools = (LinearLayout) activity.findViewById(R.id.area_tools);
		_startBnt = (ImageView) activity.findViewById(R.id.image_tools);
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
						mCallback.onStartPerform();
						break;
					case SB_STOP:
						mCallback.onStopPerform();
						break;
					default:
						break;
				}
			}
		});

		_command_aria = (FlowLayout) activity.findViewById(R.id.test);
		_command_aria.setOnDragListener(myOnDragListener);

		_prompt = (TextView) activity.findViewById(R.id.prompt);
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
		ImageHelper.Store(activity, OpCode._FORWARD, _area_tools, _OnClickListenerOpcodeToView);
		ImageHelper.Store(activity, OpCode._BACK, _area_tools, _OnClickListenerOpcodeToView);
		ImageHelper.Store(activity, OpCode._LEFT, _area_tools, _OnClickListenerOpcodeToView);
		ImageHelper.Store(activity, OpCode._RIGHT, _area_tools, _OnClickListenerOpcodeToView);

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
																if (v.equals(_imgselected.SelectedView()))
																{
																	_command_aria.removeView(_imgselected.InsertView());
																	_imgselected.Unselect();
																	IsCommandStringChanged();
																	return;
																}
																int count = _command_aria.getChildCount();
																for (int index = 0; index < count; ++index)
																{
																	View test = _command_aria.getChildAt(index);
																	if (test.equals(v))
																	{
																		_imgselected.Set(v, index);
																		_command_aria.addView(_imgselected.InsertView(), index);
																		IsCommandStringChanged();
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
																ImageView iv = ImageToCommandArea(v.getId());
																if (-1 < _imgselected.InsertIndex())
																{
																	_command_aria.addView(iv, _imgselected.InsertIndex());
																	_command_aria.removeView(_imgselected.InsertView());
																	_imgselected.Unselect();
																}
																else
																{
																	_command_aria.addView(iv);
																}
																IsCommandStringChanged();
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
																			IsCommandStringChanged();
																		}
																		break;
																	default:
																		break;
																}
																return true;
															}

														};
	private MenuItem	_showFileMenuItem;
	private String		_commandString;
	private int			_fileIconId;

	char OpcodeToDo()
	{
		if (!IsSelected())
		{

			if (-1 < _performed.index && _performed.index < _command_aria.getChildCount())
			{
				_performed.view = _command_aria.getChildAt(_performed.index);
				++_performed.index;
				_performed.Select();
				return OpcodeCurrent();
			}
		}
		return 0;
	}

	char OpcodeCurrent()
	{
		return OpCode.OpcodeC(_performed.Opcode());
	}

	void PreparationForStart()
	{
		_performed.RestoreImage(this.activity);
		_performed.index = 0;
	}

	ImageHelper Performed()
	{
		return _performed;
	}

	UIHelper Unselect()
	{
		if (IsPerformedValid())
		{
			_performed.UnSelect();
		}
		return this;
	}

	boolean IsPerformedValid()
	{
		return (null != _performed);
	}

	void PerformError()
	{
		if (IsPerformedValid())
		{
			_performed.SetImage(this.activity, R.drawable.x);
		}
	}

	public void PerformObstacle()
	{
		if (IsPerformedValid())
		{
			_performed.SetImage(this.activity, R.drawable.o);
		}
	}

	public void StartBntToStop()
	{
		_startBnt.setId(SB_STOP);
		_startBnt.setImageDrawable(ContextCompat.getDrawable(this.activity, SB_STOP));
	}

	public void StartBntToStart()
	{
		_startBnt.setId(SB_START);
		_startBnt.setImageDrawable(ContextCompat.getDrawable(this.activity, SB_START));
	}

	boolean IsSelected()
	{
		return _imgselected.IsSelected();
	}

	// Command area
	public String CommandToString()
	{
		String ret = "";
		int count = _command_aria.getChildCount();
		for (int k = 0; k < count; ++k)
		{
			View v = _command_aria.getChildAt(k);
			char c = OpCode.OpcodeC(v.getId());
			ret += c;
		}
		return ret;
	}

	public void StringToCommand(String commands)
	{
		SaveCommandString(commands);
		_command_aria.removeAllViews();
		int length = commands.length();
		for (int k = 0; k < length; ++k)
		{
			char c = commands.charAt(k);
			int id = OpCode.ImageId(c);
			if (0 != id)
			{
				ImageView iv = ImageToCommandArea(id);
				_command_aria.addView(iv);
			}
		}
	}

	private ImageView ImageToCommandArea(int id)
	{
		ImageView iv = ImageHelper.CreateImage(activity, id);
		if (null != iv)
		{
			iv.setOnClickListener(_OnClickSelectInsert);
			iv.setOnLongClickListener(myOnLongClickListener);
		}
		return iv;
	}

	boolean IsCommandStringChanged()
	{
		String t = CommandToString();
		boolean ret = t.equals(_commandString);
		if (!ret)
		{
			_showFileMenuItem.setIcon(R.drawable.noload);
		}
		else
		{
			_showFileMenuItem.setIcon(_fileIconId);
		}

		return ret;
	}

	public void SaveCommandString(String string)
	{
		_commandString = string;
	}

	public void SetFileIcon(int iconId)
	{
		_fileIconId = iconId;
	}

	public void ShowFileIcon()
	{
		if (null != _showFileMenuItem)
		{
			_showFileMenuItem.setIcon(_fileIconId);
		}

	}

	public void SetMenuItem(MenuItem findItem)
	{
		_showFileMenuItem = findItem;
	}

	@Override
	public void onErase()
	{
		IsCommandStringChanged();
	}

}// class UIHelper
