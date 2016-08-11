package com.control.ui;

import java.util.HashMap;

import com.control.util.Logger;

import android.support.v4.widget.DrawerLayout.LayoutParams;
import android.text.method.ScrollingMovementMethod;
import android.app.Activity;
import android.content.ClipData;
import android.graphics.Color;
import android.hardware.Camera.Area;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.ViewParent;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity
{

	private final int	_DOWN	= 107;
	private final int	_UP		= _DOWN + 1;
	private final int	_LEFT	= _DOWN + 2;
	private final int	_RIGHT	= _DOWN + 3;

	class Item extends HashMap<Integer, Integer>
	{
		private static final long	serialVersionUID	= 1L;
	}// class Item

	private Item	_rc	= new Item()
						{
							private static final long	serialVersionUID	= 1L;

							{
								put(_DOWN, R.drawable.d);
								put(_UP, R.drawable.u);
								put(_LEFT, R.drawable.l);
								put(_RIGHT, R.drawable.r);
							}
						};

	class Areas extends HashMap<Integer, LinearLayout>
	{
		public LinearLayout put(Integer key, LinearLayout ll)
		{
			LinearLayout ret = super.put(key, ll);
			ll.setOnDragListener(myOnDragListener);
			return ret;
		}
	}// class Areas

	LinearLayout	_area_tools		= null;
	// LinearLayout _area20 = null;
	// LinearLayout _area21 = null;
	int				_current_index	= 0;
	LinearLayout	_current_area	= null;
	ImageView		_iv_prev		= null;
	TextView		prompt			= null;
	private Areas	_areas			= new Areas();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		_area_tools = (LinearLayout) findViewById(R.id.area1);

		_areas.put(0, (LinearLayout) findViewById(R.id.area20));
		_areas.put(1, (LinearLayout) findViewById(R.id.area21));
		_areas.put(2, (LinearLayout) findViewById(R.id.area22));
		_areas.put(3, (LinearLayout) findViewById(R.id.area23));

		// _area20 = (LinearLayout) findViewById(R.id.area20);
		// _area21 = (LinearLayout) findViewById(R.id.area21);

		prompt = (TextView) findViewById(R.id.prompt);
		// make TextView scrollable
		prompt.setMovementMethod(new ScrollingMovementMethod());
		// clear prompt area if LongClick
		prompt.setOnLongClickListener(new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(View v)
			{
				prompt.setText("");
				return true;
			}
		});
		Store(_UP);
		Store(_DOWN);
		Store(_LEFT);
		Store(_RIGHT);

		// _area20.setOnDragListener(myOnDragListener);
		// _area21.setOnDragListener(myOnDragListener);
		_current_area = _areas.get(_current_index);// _area20;
	}

	@Override
	protected void onStart()
	{
		super.onStart();

	}

	OnClickListener		_OnClickListener		= new OnClickListener()
												{

													@Override
													public void onClick(View v)
													{
														boolean new_line = false;
														Logger.Log.t("longTouch", "ID", v.getId());
														ImageView i = CreateImage(v.getId());
														i.setOnLongClickListener(myOnLongClickListener);
														if (null != _iv_prev)
														{
															int w = _current_area.getWidth();
															int c = _current_area.getChildCount();
															int cw = _current_area.getChildAt(0).getWidth();
															new_line = (cw * (c + 1) > w);
															if (new_line)
															{
																_current_area = _areas.get(++_current_index);
																if (null == _current_area)
																{
																	return;
																}
																_iv_prev.setPadding(0, 5, 5, 0);
															}
															else
															{
																_iv_prev.setPadding(0, 5, 0, 5);
															}

														}
														int direction = _current_area.getLayoutDirection();
														if (View.LAYOUT_DIRECTION_RTL == direction)
														{
															i.setPadding(5,(new_line)? 0: 5, 0, 5);
														}
														else
														{
															i.setPadding(0, (new_line)? 0: 5, 5, 5);
														}
														_current_area.addView(i);
														_iv_prev = i;
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
																// prompt.append("ACTION_DRAG_STARTED: "
																// + area + "\n");
																break;
															case DragEvent.ACTION_DRAG_ENTERED:
																// prompt.append("ACTION_DRAG_ENTERED: "
																// + area + "\n");
																break;
															case DragEvent.ACTION_DRAG_EXITED:
																// prompt.append("ACTION_DRAG_EXITED: "
																// + area + "\n");
																break;
															case DragEvent.ACTION_DROP:
																// prompt.append("ACTION_DROP: " +
																// area + "\n");
																break;
															case DragEvent.ACTION_DRAG_ENDED:
																// prompt.append("ACTION_DRAG_ENDED: "
																// + area + "\n");
																View view1 = (View) event.getLocalState();
																// _area20.removeView(view1);
															default:
																break;
														}
														return true;
													}

												};

	void Store(int id)
	{
		ImageView imageView1 = CreateImage(id);
		imageView1.setOnClickListener(_OnClickListener);
		_area_tools.addView(imageView1);
	}

	ImageView CreateImage(int id)
	{
		int drawableId = _rc.get(id);
		ImageView iv = new ImageView(this);
		iv.setImageDrawable(getResources().getDrawable(drawableId));
		iv.setId(id);
		iv.setAdjustViewBounds(true);
		iv.setScaleType(ScaleType.CENTER_INSIDE);
		iv.setBackgroundColor(Color.RED);
		// iv.setPadding(0, 5, 5, 5);
		return iv;
	}

}// class MainActivity
