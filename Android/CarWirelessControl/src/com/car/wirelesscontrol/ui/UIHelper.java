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
	private final int		SB_START		= R.drawable.start;
	private final int		SB_STOP			= R.drawable.stop;

	ImageHelper				_performed		= new ImageHelper();
	ImageSelected			_imgselected	= null;
	TextView				_prompt			= null;
	ImageView				_startBnt		= null;
	Eraser					_eraser			= null;
	LinearLayout			_area_tools		= null;
	LinearLayout			_current_area	= null;
	private FlowLayout		_command_area	= null;
	private boolean			_performMode	= false;
	final Activity			_activity;
	private final Chunkof	Chunk;

	public interface Callback
	{
		void onStartPerform();

		void onStopPerform();
	}

	private Callback mCallback;

	void PerformMode(boolean b)
	{
		_performMode = b;
	}

	boolean PerformMode()
	{
		return _performMode;
	}

	public UIHelper(Activity activity, Callback callback)
	{
		mCallback = callback;
		Chunk = new Chunkof();
		_activity = activity;
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

		_command_area = (FlowLayout) activity.findViewById(R.id.commandArea);
		_command_area.setOnDragListener(myOnDragListener);

		_prompt = (TextView) activity.findViewById(R.id.prompt);
		_prompt.setVisibility(View.GONE);
		// if (_prompt.isShown())
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

	public void SetPrompt(boolean b)
	{
		int vis = (b) ? View.VISIBLE : View.GONE;
		_prompt.setVisibility(vis);
	}

	public void SetPrompt(String txt)
	{
		if (_prompt.isShown())
		{
			String str = _prompt.getText().toString();
			_prompt.setText(str + txt);
		}

	}

	private void SelectNewStep(View v)
	{
		int count = _command_area.getChildCount();
		for (int index = 0; index < count; ++index)
		{
			View test = _command_area.getChildAt(index);
			if (test.equals(v))
			{
				_imgselected.Set(v, index);
				_command_area.addView(_imgselected.InsertView(), index);
				_imgselected.InsertView().setOnLongClickListener(myOnLongClickListener);
				IsCommandStringChanged();
				break;
			}
		}
	}

	private void UnselectSelectedStep()
	{
		_command_area.removeView(_imgselected.InsertView());
		_imgselected.Unselect();
		IsCommandStringChanged();
	}

	OnClickListener		_OnClickSelectInsert			= new OnClickListener()
														{
															@Override
															public void onClick(View v)
															{
																if (_performMode)
																{
																	return;
																}
																Logger.Log.t("SELECT");
																if (_imgselected.IsSelected() && !v.equals(_imgselected.SelectedView()))
																{
																	UnselectSelectedStep();
																	SelectNewStep(v);
																	return;
																}

																if (v.equals(_imgselected.SelectedView()))
																{
																	UnselectSelectedStep();
																	return;
																}
																SelectNewStep(v);
															}

														};

	OnClickListener		_OnClickListenerOpcodeToView	= new OnClickListener()
														{

															@Override
															public void onClick(View v)
															{
																if (_performMode)
																{
																	return;
																}
																ImageView iv = ImageToCommandArea(v.getId());
																if (-1 < _imgselected.InsertIndex())
																{
																	_command_area.addView(iv, _imgselected.InsertIndex());
																	_command_area.removeView(_imgselected.InsertView());
																	_imgselected.Unselect();
																}
																else
																{
																	_command_area.addView(iv);
																}
																IsCommandStringChanged();
															}
														};

	OnLongClickListener	myOnLongClickListener			= new OnLongClickListener()
														{

															@Override
															public boolean onLongClick(View v)
															{
																if (_performMode)
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
																	case DragEvent.ACTION_DRAG_ENDED:
																		if (!_performMode)
																		{
																			View view = (View) event.getLocalState();
																			if (OpCode._INSERT == view.getId())
																			{
																				UnselectSelectedStep();
																			}
																			_command_area.removeView(view);
																			IsCommandStringChanged();
																		}
																		break;
																	case DragEvent.ACTION_DRAG_STARTED:
																	case DragEvent.ACTION_DRAG_ENTERED:
																	case DragEvent.ACTION_DRAG_EXITED:
																	case DragEvent.ACTION_DROP:
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
		return Select().OpcodeCurrent();
	}

	UIHelper Select()
	{
		if (!IsSelected())
		{
			if (-1 < _performed.index && _performed.index < _command_area.getChildCount())
			{
				_performed.view = _command_area.getChildAt(_performed.index);
				++_performed.index;
				_performed.Select();
			}
		}
		return this;
	}

	char OpcodeCurrent()
	{
		if (null != _performed)
		{
			return OpCode.OpcodeC(_performed.Opcode());
		}
		return 0;
	}

	void PreparationForStart()
	{
		_performed.RestoreImage(this._activity);
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
			_performed.SetImage(this._activity, R.drawable.x);
		}
	}

	public void PerformObstacle()
	{
		if (IsPerformedValid())
		{
			_performed.SetImage(this._activity, R.drawable.o);
		}
	}

	public void StartBntToStop()
	{
		_startBnt.setId(SB_STOP);
		_startBnt.setImageDrawable(ContextCompat.getDrawable(this._activity, SB_STOP));
	}

	public void StartBntToStart()
	{
		_startBnt.setId(SB_START);
		_startBnt.setImageDrawable(ContextCompat.getDrawable(this._activity, SB_START));
	}

	boolean IsSelected()
	{
		return _imgselected.IsSelected();
	}

	// Command area
	public String CommandToString()
	{
		String ret = "";
		int count = _command_area.getChildCount();
		for (int k = 0; k < count; ++k)
		{
			View v = _command_area.getChildAt(k);
			char c = OpCode.OpcodeC(v.getId());
			ret += c;
		}
		return ret;
	}

	public void StringToCommand(String commands)
	{
		SaveCommandString(commands);
		_command_area.removeAllViews();
		int length = commands.length();
		for (int k = 0; k < length; ++k)
		{
			char c = commands.charAt(k);
			int id = OpCode.ImageId(c);
			if (0 != id)
			{
				ImageView iv = ImageToCommandArea(id);
				_command_area.addView(iv);
			}
		}
	}

	private ImageView ImageToCommandArea(int id)
	{
		ImageView iv = ImageHelper.CreateImage(_activity, id);
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

	// ------------------------------------------------------------------
	// class Chunkof
	// ------------------------------------------------------------------
	public String GetFirstCommandChunk()
	{
		Chunk.Init();
		return Chunk.GetFirst();
	}

	public String GetNextCommandChunk()
	{
		return Chunk.GetNext();
	}

	static final int	YES		= 1;
	static final int	NO		= YES + 1;
	static final int	STOP	= YES + 2;

	public int AskNextCommandChunk()
	{
		Chunk.IncrementPosition();
		if (Chunk.IsStop())
		{
			return STOP;
		}
		if (Chunk.IsNeedNext())
		{
			return YES;
		}
		return NO;
	}

	class Chunkof
	{
		final int	MaxBufferSize	= 4;
		private int	_pos			= 0;
		private int	_curr			= 0;
		private int	_childCount		= 0;

		public Chunkof()
		{
		}

		void IncrementPosition()
		{
			_curr += 1;
		}

		boolean IsNeedNext()
		{
			if (0 == _pos - _curr)
			{
				return true;
			}
			return false;
		}

		boolean IsStop()
		{
			return _childCount == _curr;
		}

		void Init()
		{
			_curr = 0;
			_pos = 0;
			_childCount = _command_area.getChildCount();
		}

		String GetChunk(int start, int number)
		{

			String ret = "";
			if (_childCount <= start)
			{
				return ret;
			}
			int nCount = (start + number);
			if (nCount > _childCount)
			{
				nCount = _childCount;
			}

			for (int k = start; k < nCount; ++k)
			{
				View v = _command_area.getChildAt(k);
				char c = OpCode.OpcodeC(v.getId());
				ret += c;
			}
			_pos += ret.length();
			return ret;
		}

		String GetFirst()
		{
			return GetChunk(0, MaxBufferSize);
		}

		String GetNext()
		{
			if (_pos >= _childCount)
			{
				return "";
			}
			return GetChunk(_pos, MaxBufferSize - 1);
		}
	}// class Chunkof

}// class UIHelper
