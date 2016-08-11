package com.control.util;

public class Logger
{
	private final String	mTag	= "LD-APP";
	private int				mSeverity	= Severity.trace;

//No info , No debug	

	public static class Severity
	{
		public final static int	none	= 6;
		public final static int	fatal	= 5;
		public final static int	error	= 4;
		public final static int	warning	= 3;
		public final static int	info	= 2;
		public final static int	debug	= 1;
		public final static int	trace	= 0;
	}

	public Logger()
	{
		// TODO Auto-generated constructor stub
	}
	public void printStack(String msg, Exception exc)
	{
		if (mSeverity == Severity.none)
		{
			return;
		}
		if (exc != null)
		{
			if (msg != null && msg.length() > 0)
			{
				android.util.Log.w(mTag, Thread.currentThread().getName() + ": " + msg, exc);
			}
			else
			{
				android.util.Log.w(mTag, Thread.currentThread().getName(), exc);
			}
		}
	}

	public String getStackTraceString(Throwable tr)
	{
		return android.util.Log.getStackTraceString(tr);
	}

	//
	private String args2str(Object[] args)
	{
		StringBuilder sb = new StringBuilder(Thread.currentThread().getName());
		for (final Object str : args)
		{
			sb.append(":");
			sb.append(str);
		}

		return sb.toString();
	}


	public void LoggingOn()
	{
		mSeverity = Severity.trace;
	}

	public void LoggingOff()
	{
		mSeverity = Severity.none;
	}

	public boolean isTrace()
	{
		return (mSeverity == Severity.trace);
	}


	void PrintAndSave(final Object... s)
	{
		String msg = args2str(s);
		android.util.Log.e(this.mTag, msg);
	}

	public void n(final Object... s)
	{
		PrintAndSave(s);
	}

	public void f(final Object... s)
	{
		if (mSeverity > Severity.fatal)
		{
			return;
		}
		PrintAndSave(s);
	}

	public void e(final Object... s)
	{
		if (mSeverity > Severity.error)
		{
			return;
		}
		PrintAndSave(s);
	}

	public void w(final Object... s)
	{
		if (mSeverity > Severity.warning)
		{
			return;
		}
		String msg = args2str(s);
		android.util.Log.w(this.mTag, msg);
	}

	public void i(final Object... s)
	{
	}

	public void d(final Object... s)
	{
	}

	public void t(final Object... s)
	{
		if (mSeverity > Severity.trace)
		{
			return;
		}
		String msg = args2str(s);
		android.util.Log.e(this.mTag, msg);
	}
	public static void trace(final Object... s)
	{
		Log.PrintAndSave(s);
	}
	public static Logger	Log	= new Logger();

};// class CuppLogger
