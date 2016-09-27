package com.car.wirelesscontrol.util;

import java.util.Stack;

import com.sasa.logger.Logger;

public class CommandSender
{
	public static int		Timeout	= 1000;				// msec
	private BlueToothHelper	m_bth	= null;
	private boolean			m_brun	= true;
	private byte			m_byte	= 0;

	Stack<Byte>				cmd		= new Stack<Byte>();

	public CommandSender(final BlueToothHelper bth)
	{
		m_bth = bth;
	}

	public void Send(byte bt)
	{
		cmd.add(0, bt);
		return;
	}

	private void __Send()
	{
		if (null != m_bth)
		{
			m_bth.Send(m_byte);
			Logger.Log.t("SEND",Integer.toHexString(m_byte & 0xFF));
		}

	}

	public void Start()
	{
		m_brun = true;
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{

				while (m_brun)
				{
					try
					{
						Thread.sleep(Timeout);
					}
					catch (InterruptedException e)
					{
					}
					int size = cmd.size();
					if (0 < size)
					{
						m_byte = cmd.pop();
					}
					__Send();
				}
			}
		}, "CommandSender").start();
	}

	public void Stop()
	{
		m_brun = false;
	}

}// class CommandSender
