package com.car.wirelesscontrol.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sasa.logger.Logger;

public class CommandSender
{
	public final int		Timeout			= 700;						// msec
	private BlueToothHelper	m_bth			= null;
	private AtomicBoolean	m_keepRunning	= new AtomicBoolean(true);

	private final Lock		m_SenderLock	= new ReentrantLock();
	private final Condition	m_SenderCond	= m_SenderLock.newCondition();
	private boolean			m_Received		= false;
	private AtomicInteger	m_byte			= new AtomicInteger(0);
	private Thread			m_Thread		= null;

	public CommandSender(final BlueToothHelper bth)
	{
		m_bth = bth;
	}

	public void Send(byte bt)
	{
		Logger.Log.t("SEND 303", Integer.toHexString(bt & 0xFF), Integer.toHexString(m_byte.byteValue() & 0xFF));
		if (m_byte.byteValue() == bt)
		{
			return;
		}
		
		m_byte.set(bt & 0x00ff); // set outside from the lock, so the frequently updated commands are debounced
		
		m_SenderLock.lock();
		try
		{
			m_Received = true;
			m_SenderCond.signal();
			Logger.Log.t("SEND 304", Integer.toHexString(m_byte.byteValue() & 0xFF));
		}
		finally
		{
			m_SenderLock.unlock();
		}
	}

	private void __Send(byte val)
	{
		if (null != m_bth)
		{
			m_bth.Send(val);

			Logger.Log.t("SEND", Integer.toHexString(val & 0xFF));
		}

	}

	public void StopSending()
	{
		m_byte.set(0);
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
					//Logger.Log.t("SEND 305", Integer.toHexString(m_byte.byteValue() & 0xFF), m_Received);
					
					byte command = 0;
					boolean bReceived = false;
					m_SenderLock.lock();
					try
					{
						if (!m_Received)
						{
							m_SenderCond.await(Timeout, TimeUnit.MILLISECONDS);
						}
						bReceived = m_Received;
						command = m_byte.byteValue();
						m_Received = false;
					}
					catch (InterruptedException e)
					{
						Logger.Log.t("SEND", e.getMessage());
					}
					finally
					{
						m_SenderLock.unlock();
					}
					
					if (!bReceived)
					{
						if (command == 0)
						{
							continue;
						}
					}
					if (m_keepRunning.get())
					{
						__Send(command);
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
