package com.car.wirelesscontrol.util;

import java.util.Stack;

import com.sasa.logger.Logger;

public class CommandSender
{
	public final int		Timeout			= 1000;				// msec
	private BlueToothHelper	m_bth			= null;
	private boolean			m_brun			= true;
	private byte			m_byte			= 0;
	private byte			m_last_sended	= 1;
	Stack<Byte>				cmd				= new Stack<Byte>();

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
			Logger.Log.t("SEND", Integer.toHexString(m_byte & 0xFF));
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
						int size = cmd.size();
						if (0 < size)
						{
							m_byte = cmd.pop();
						}
						if (0 == m_byte && 0 == m_last_sended)
						{
							continue;
						}
						__Send();
						m_last_sended = m_byte;

					}
					catch (InterruptedException e)
					{
						Logger.Log.t("SEND", e.getMessage());
					}
				}
			}
		}, "CommandSender").start();
	}

	public void Stop()
	{
		m_brun = false;
	}

}// class CommandSender
