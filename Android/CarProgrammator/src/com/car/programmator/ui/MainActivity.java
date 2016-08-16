package com.car.programmator.ui;

import java.util.HashMap;

import com.car.programmator.util.Logger;

import android.app.Activity;
import android.content.ClipData;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class MainActivity extends Activity
{

	private final int	_DOWN	= 107;
	private final int	_UP		= _DOWN + 1;
	private final int	_LEFT	= _DOWN + 2;
	private final int	_RIGHT	= _DOWN + 3;

	class Item extends HashMap<Integer, Integer>
	{
		private static final long serialVersionUID = 1L;
	}// class Item

	private Item		_rc				= new Item()
										{
											private static final long serialVersionUID = 1L;

											{
												put(_DOWN, R.drawable.d);
												put(_UP, R.drawable.u);
												put(_LEFT, R.drawable.l);
												put(_RIGHT, R.drawable.r);
											}
										};

	LinearLayout		_area_tools		= null;
	LinearLayout		_current_area	= null;
	ImageView			_iv_prev		= null;
	TextView			prompt			= null;
	private FlowLayout	test;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		_area_tools = (LinearLayout) findViewById(R.id.area_tools);
		test = (FlowLayout) findViewById(R.id.test);
		test.setOnDragListener(myOnDragListener);

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
														Logger.Log.t("longTouch", "ID", v.getId());
														ImageView i = CreateImage(v.getId());
														i.setOnLongClickListener(myOnLongClickListener);
														test.addView(i);
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
																View view1 = (View) event.getLocalState();
																test.removeView(view1);
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
		// iv.setBackgroundColor(Color.RED);
		// iv.setPadding(0, 5, 5, 5);
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
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
