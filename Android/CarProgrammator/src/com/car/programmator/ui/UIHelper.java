package com.car.programmator.ui;

import com.car.programmator.util.Logger;
import com.car.programmator.util.OpCode;
import android.app.Activity;
import android.content.ClipData;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.view.DragEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class UIHelper
{
	private final int	SB_START		= 10;
	private final int	SB_STOP			= 11;
	final Activity		activity;
	ImageHelper			_performed		= new ImageHelper(null);
	ImageHelper			_selected		= new ImageHelper(new ImageHelper(null));
	TextView			_prompt			= null;
	ImageView			_startBnt		= null;
	Eraser				_eraser			= null;
	LinearLayout		_area_tools		= null;
	LinearLayout		_current_area	= null;
	private FlowLayout	_command_aria	= null;
	private boolean		_PerformMode	= false;

	public interface Callback
	{
		void onStartPerform();

		void onStopPerform();
	}

	private Callback mCallback;

	public void registerCallBack(Callback callback)
	{
		mCallback = callback;
	}

	void PerformMode(boolean b)
	{
		_PerformMode = b;
	}

	boolean PerformMode()
	{
		return _PerformMode;
	}

	public UIHelper(Activity activity)
	{
		this.activity = activity;
		_eraser = new Eraser(activity, _selected);
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
																if (v.equals(_selected.view))
																{
																	_command_aria.removeView(_selected.linked_image.view);
																	_selected.UnSelect().InitAll();
																	return;
																}
																int count = _command_aria.getChildCount();
																for (int k = 0; k < count; ++k)
																{
																	View test = _command_aria.getChildAt(k);
																	if (test.equals(v))
																	{
																		_selected.Set(v, k).Select();
																		_selected.linked_image.Set(ImageHelper.CreateImage(activity, OpCode._EMPTY), k);
																		_command_aria.addView(_selected.linked_image.view, _selected.linked_image.index);
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
																ImageView iv = ImageHelper.CreateImage(activity, v.getId());
																iv.setOnClickListener(_OnClickSelectInsert);
																iv.setOnLongClickListener(myOnLongClickListener);
																if (-1 < _selected.linked_image.index)
																{
																	_command_aria.addView(iv, _selected.linked_image.index);

																	_command_aria.removeView(_selected.linked_image.view);
																	_selected.UnSelect().InitAll();
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

	char OpcodeToDo()
	{
		if (!IsSelected())
		{

			if (-1 < _performed.index && _performed.index < _command_aria.getChildCount())
			{
				_performed.view = _command_aria.getChildAt(_performed.index);
				_performed.Select();
				_performed.index += 1;
				return OpcodeCurrent();
			}
		}
		return 0;
	}

	char OpcodeCurrent()
	{
		return OpCode.OpcodeC(_performed.Opcode());
	}

	void ToStart()
	{
		_performed.RestoreImage(this.activity);
		_performed.index = 0;
	}

	boolean IsPerformedValid()
	{
		return (null != _performed);
	}

	ImageHelper Performed()
	{
		return _performed;
	}

	void Unselect()
	{
		if (IsPerformedValid())
		{
			_performed.UnSelect();
		}
	}

	void PerformError()
	{
		if (IsPerformedValid())
		{
			_performed.SetImage(this.activity, R.drawable.x);
		}
	}

	public void StartBntToStop()
	{
		_startBnt.setId(SB_STOP);
		_startBnt.setImageDrawable(ContextCompat.getDrawable(this.activity, R.drawable.stop));
	}

	public void StartBntToStart()
	{
		_startBnt.setId(SB_START);
		_startBnt.setImageDrawable(ContextCompat.getDrawable(this.activity, R.drawable.start));
	}

	boolean IsSelected()
	{
		return (null != _selected.linked_image.view);
	}

}// class UIHelper
