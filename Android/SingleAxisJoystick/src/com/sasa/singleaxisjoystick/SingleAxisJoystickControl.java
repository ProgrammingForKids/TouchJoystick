package com.sasa.singleaxisjoystick;

import java.util.Arrays;
import java.util.Collections;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SingleAxisJoystickControl extends View implements Runnable
{
	// Constants
	public final static long		DEFAULT_LOOP_INTERVAL	= 100;									// 100 ms
	public final static int			MAX_VALUE				= 100;
	public final int				mButtonColor			= Color.rgb(98, 134, 210);
	private final int[]				m_gradationColors		= {
			Color.parseColor("#F45329"),
			Color.parseColor("#FFC641"),
			Color.parseColor("#0B346A"),
			Color.parseColor("#DE377C"),
			Color.parseColor("#B19975"),
			Color.parseColor("#B6C894"),
			Color.parseColor("#EEC6DE"),
			Color.parseColor("#847673"),
			Color.parseColor("#966FA6"),
			Color.parseColor("#7ECCBF")
	};
	// Variables
	private Integer[]				m_gradation				= null;
	private Paint[]					m_gradationPaint		= new Paint[m_gradationColors.length];
	private OnJoystickMoveListener	onJoystickMoveListener;											// Listener
	private Thread					m_thread				= new Thread(this);
	private long					m_loopInterval			= DEFAULT_LOOP_INTERVAL;
	private int						m_yPosition				= 0;									// Touch y position
	private double					m_centerX				= 0;									// Center view x position
	private double					m_centerY				= 0;									// Center view y position
	private double					m_congruence			= 0.98;
	private Paint					m_buttonPaint;
	private Paint					m_verticalLinePaint;
	private int						m_JoystickLength		= 0;
	private int						m_ButtonRadius			= 0;
	private int						m_ButtonDiameter		= 0;

	SingleAxisJoystickControl		m_sibling				= null;
	boolean							m_bTogether				= false;

	public SingleAxisJoystickControl(Context context)
	{
		super(context);
	}

	public SingleAxisJoystickControl(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initJoystickView();
	}

	public SingleAxisJoystickControl(Context context, AttributeSet attrs, int defaultStyle)
	{
		super(context, attrs, defaultStyle);
		initJoystickView();
	}

	public void SetSibling(final SingleAxisJoystickControl sibling)
	{
		m_sibling = sibling;
	}

	public void SetGradation(Integer[] gradation)
	{
		m_gradation = gradation;
		Arrays.sort(m_gradation, Collections.reverseOrder());
	}

	protected void initJoystickView()
	{
		m_verticalLinePaint = new Paint();
		m_verticalLinePaint.setStrokeWidth(5);
		m_verticalLinePaint.setColor(Color.BLACK);
		for (int k = 0; k < m_gradationColors.length; ++k)
		{
			m_gradationPaint[k] = new Paint();

			m_gradationPaint[k].setStrokeWidth(1);
			m_gradationPaint[k].setColor(m_gradationColors[k]);
			m_gradationPaint[k].setAlpha(200);
			// m_gradationPaint.setStyle(Paint.Style.STROKE);

		}

		m_buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		m_buttonPaint.setColor(mButtonColor);
		m_buttonPaint.setStyle(Paint.Style.FILL);

	}

	@Override
	protected void onFinishInflate()
	{
	}

	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld)
	{
		super.onSizeChanged(xNew, yNew, xOld, yOld);
		// before measure, get the center of view
		int h = getHeight();
		int w = getWidth();

		if (0 < h / w)// PORTRAIT
		{
			m_congruence = 0.5;
		}
		else
		{
			m_congruence = 0.9;
		}

		m_yPosition = (int) getHeight() / 2;
		int d = Math.min(xNew, yNew);
		m_ButtonRadius = (int) (d / 2 * 0.20);
		m_ButtonDiameter = 2 * m_ButtonRadius;
		m_JoystickLength = (int) (m_congruence * yNew / 2);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		// setting the measured values to resize the view to a certain width and height
		// int d = Math.min(measure(widthMeasureSpec), measure(heightMeasureSpec));

		int w = measure(widthMeasureSpec);
		int h = measure(heightMeasureSpec);

		setMeasuredDimension(w, h);

	}

	private int measure(int measureSpec)
	{
		int result = 0;

		// Decode the measurement specifications.
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.UNSPECIFIED)
		{
			// Return a default size of 200 if no bounds are specified.
			result = 200;
		}
		else
		{
			// As you want to fill the available space
			// always return the full available bounds.
			result = specSize;
		}
		return result;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		m_centerX = (getWidth()) / 2;
		m_centerY = (getHeight()) / 2;
		if (null != m_gradation)
		{
			for (int k = 0; k < m_gradation.length; ++k)
			{

				float gradation = (float) (m_gradation[k] / 100.0);
				canvas.drawRect(
						(float) (m_ButtonDiameter),
						(float) (m_centerY - m_JoystickLength * gradation),
						(float) (2 * m_centerX - m_ButtonDiameter),
						(float) (m_centerY + m_JoystickLength * gradation),
						m_gradationPaint[k]);
			}
		}

		// m_gradationPaint lines
		canvas.drawLine((float) m_centerX, (float) (m_JoystickLength + m_centerY), (float) m_centerX, (float) (m_centerY - m_JoystickLength), m_verticalLinePaint);
		// painting the move m_buttonPaint
		canvas.drawCircle((float) m_centerX, m_yPosition, m_ButtonRadius, m_buttonPaint);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		float x = event.getX();
		m_bTogether = false;
		if (x < m_ButtonDiameter || x > (2 * m_centerX - m_ButtonDiameter))
		{
			if (null != m_sibling)
			{
				m_bTogether = true;
				m_sibling.TouchEventHandler(event);
			}
		}

		return TouchEventHandler(event);
	}

	private boolean TouchEventHandler(MotionEvent event)
	{
		if (null == onJoystickMoveListener)
		{
			return false;
		}

		m_yPosition = (int) event.getY();
		double abs = Math.abs(m_yPosition - m_centerY);
		if (abs > m_JoystickLength)
		{
			m_yPosition = (int) ((m_yPosition - m_centerY) * m_JoystickLength / abs + m_centerY);
		}
		invalidate();

		switch (event.getAction())
		{
			case MotionEvent.ACTION_UP:
			{
				m_yPosition = (int) m_centerY;
				m_thread.interrupt();
				onJoystickMoveListener.onValueChanged(GetValue(), m_bTogether);
				break;
			}
			case MotionEvent.ACTION_DOWN:
			{
				if (m_thread != null && m_thread.isAlive())
				{
					m_thread.interrupt();
				}
				m_thread = new Thread(this);
				m_thread.start();
				break;
			}
			default:
				break;
		}
		return true;
	}

	private int GetValue()
	{
		return (int) (MAX_VALUE * (m_centerY - m_yPosition) / m_JoystickLength);
	}

	public void setOnJoystickMoveListener(OnJoystickMoveListener listener, long repeatInterval)
	{
		this.onJoystickMoveListener = listener;
		this.m_loopInterval = repeatInterval;
	}

	public interface OnJoystickMoveListener
	{
		public void onValueChanged(int power, boolean bTogether);
	}

	@Override
	public void run()
	{
		while (!Thread.interrupted())
		{
			post(new Runnable()
			{
				public void run()
				{
					if (onJoystickMoveListener != null)
					{
						onJoystickMoveListener.onValueChanged(GetValue(), m_bTogether);
					}
				}
			});
			try
			{
				Thread.sleep(m_loopInterval);
			}
			catch (InterruptedException e)
			{
				break;
			}
		}
	}
}
