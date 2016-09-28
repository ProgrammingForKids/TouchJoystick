package com.car.wirelesscontrol.util;

import java.util.concurrent.atomic.AtomicBoolean;

import com.sasa.logger.Logger;

public class CommandSender
{
	public final int		Timeout			= 700;						// msec
	private BlueToothHelper	m_bth			= null;
	private AtomicBoolean	m_keepRunning	= new AtomicBoolean(true);

	private AtomicBoolean	m_Received		= new AtomicBoolean(false);
	private Byte			m_byte			= new Byte((byte) 0);
	private byte			m_prev			= 0;
	private Thread			m_Thread		= null;

	public CommandSender(final BlueToothHelper bth)
	{
		m_bth = bth;
	}

	public void Send(byte bt)
	{
		Logger.Log.t("SEND 303", Integer.toHexString(bt & 0xFF), Integer.toHexString(m_byte.byteValue() & 0xFF));
		if (m_prev == bt)
		{
			return;
		}
		synchronized (this)
		{
			m_byte = bt;
			m_Received.set(true);
			this.notify();
			Logger.Log.t("SEND 304", Integer.toHexString(m_byte.byteValue() & 0xFF), Integer.toHexString(m_prev & 0xFF));
		}
	}

	private void __Send()
	{
		if (null != m_bth)
		{
			m_bth.Send(m_byte);

			Logger.Log.t("SEND", Integer.toHexString(m_byte & 0xFF));
		}

	}

	public void StopSending()
	{
		m_byte = 0;
	}

	public void Start()
	{
		m_Thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{

				while (m_keepRunning.get())
				{
					try
					{
						Logger.Log.t("SEND 305", Integer.toHexString(m_byte.byteValue() & 0xFF), Integer.toHexString(m_prev & 0xFF), m_Received.get());
						synchronized (this)
						{
							if (!m_Received.get())
							{
								this.wait(Timeout);
							}
							m_Received.set(false);
							if (m_byte == m_prev)
							{
								if (m_prev == 0)
								{
									continue;
								}
							}
							m_prev = m_byte;
						}
						if (m_keepRunning.get())
						{
							__Send();
						}
					}
					catch (InterruptedException e)
					{
						Logger.Log.t("SEND", e.getMessage());
					}
				}
			}
		}, "CommandSender");
		m_keepRunning.set(true);
		m_Thread.start();
	}

	public void Stop()
	{
		m_keepRunning.set(false);

		final boolean bRunning = m_keepRunning.get();

		synchronized (m_keepRunning)
		{
			m_keepRunning.set(false);

			if (!bRunning)
			{
				if (m_Thread != null)
				{
					try
					{
						m_Thread.join();
					}
					catch (InterruptedException nevermind)
					{
					}
				}
			}
		} // synchronized(keepRunning)
	}

}// class CommandSender
