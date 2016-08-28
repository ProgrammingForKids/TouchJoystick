package com.car.wirelesscontrol.util;

import com.car.programmator.ui.R;

import android.util.SparseArray;
import android.util.SparseIntArray;

public class OpCode
{

	public static final int					_BACK		= 107;
	public static final int					_FORWARD	= _BACK + 1;
	public static final int					_LEFT		= _BACK + 2;
	public static final int					_RIGHT		= _BACK + 3;
	public static final int					_EMPTY		= _BACK + 4;

	private static SparseArray<Character>	opCodes		= new SparseArray<Character>()
														{

															{
																put(OpCode._BACK, 'b');
																put(OpCode._FORWARD, 'f');
																put(OpCode._LEFT, 'l');
																put(OpCode._RIGHT, 'r');
															}
														};

	private static SparseIntArray			_rc			= new SparseIntArray()
														{

															{
																put(OpCode._FORWARD, R.drawable.f);
																put(OpCode._BACK, R.drawable.b);
																put(OpCode._LEFT, R.drawable.l);
																put(OpCode._RIGHT, R.drawable.r);
																put(OpCode._EMPTY, R.drawable.empty);
															}
														};

	public static int DrawableId(final int opcode)
	{
		try
		{
			return _rc.get(opcode);
		}
		catch (Exception e)
		{
		}
		return 0;
	}

	public static char OpcodeC(final int opcode)
	{
		try
		{
			return opCodes.get(opcode);
		}
		catch (Exception e)
		{
		}
		return 0;
	}
}// class OpCode
