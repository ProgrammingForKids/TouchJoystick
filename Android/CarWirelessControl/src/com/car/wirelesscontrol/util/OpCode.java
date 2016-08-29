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
	public static final int					_INSERT		= _BACK + 4;

	private static SparseArray<Character>	opCodes		= new SparseArray<Character>()
														{

															{
																put(ImageId('b'), 'b');
																put(ImageId('f'), 'f');
																put(ImageId('l'), 'l');
																put(ImageId('r'), 'r');
															}
														};

	private static SparseIntArray			_rc			= new SparseIntArray()
														{

															{
																put(ImageId('f'), R.drawable.f);
																put(ImageId('b'), R.drawable.b);
																put(ImageId('l'), R.drawable.l);
																put(ImageId('r'), R.drawable.r);
																put(OpCode._INSERT, R.drawable.insert);
															}
														};

	public static int ImageId(char c)
	{
		switch (c)
		{
			case 'f':
				return OpCode._FORWARD;
			case 'b':
				return OpCode._BACK;

			case 'l':
				return OpCode._LEFT;

			case 'r':
				return OpCode._RIGHT;

			default:
				return 0;
		}

	}

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
