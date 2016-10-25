package com.car.wirelesscontrol.util;

import com.sasa.singleaxisjoystick.SingleAxisJoystickControl;

public class CommandByteBuilder
{

	public static int	MAX_NUMBER_OF_SECTORS	= 8;
	private static byte	m_sec;

	public static byte Digitize(int value)
	{
		byte res = 0;
		boolean sign = (value < 0);
		int v = Math.abs(value);

		if ((SingleAxisJoystickControl.MAX_VALUE / 10) < v && v < (SingleAxisJoystickControl.MAX_VALUE / 3))
		{
			res = 1;
		}
		else if ((SingleAxisJoystickControl.MAX_VALUE / 3) < v && v < (2 * SingleAxisJoystickControl.MAX_VALUE / 3))
		{
			res = 2;
		}
		else if (v > (2 * SingleAxisJoystickControl.MAX_VALUE / 3))
		{
			res = 3;
		}
		if (sign)
		{
			res = (byte) (res|1<<2);
		}
		return res;
	}

	public static byte PrepareCommandBytelt(int left, int right)
	{
		byte l = Digitize(left);
		byte r = Digitize(right);

		byte res = (byte) (l << 3 | r);

		res = (byte) (res | (1 << 6));

		return res;

	}
	
	
	
	
	
	
	public static byte Sector()
	{
		return m_sec;
	}


	public static byte GetSector(int angle)
	{

		byte b = (byte) (((angle * MAX_NUMBER_OF_SECTORS + 180.0) / 360.0) % MAX_NUMBER_OF_SECTORS);
		m_sec = b;
		return b;

	}

	public static byte GetSpeed(int power)
	{
//		byte speed = (byte) ((power * 16) / JoystickControl.POWER_MAX);
//		if (speed == 16)
//		{
//			speed = 15;
//		}
//		return speed;
		return 0;
	}

	public static byte PrepareCommandByte(int angle, int power)
	{
		byte[] lut = { 0, 1, 3, 5, 6, 7, 9, 11 };
		byte speed = GetSpeed(power);
		byte sector = lut[GetSector(angle)];//// GetSector(angle);
		byte res = (byte) (speed << 4 | sector);
		return res;
	}

	public static String ByteToStr(byte bt)
	{
		StringBuilder sb = new StringBuilder();
		for (int k = 0; k < 8; ++k)
		{
			sb.append((bt >> (7 - k)) & 1);
		}
		//String x = Integer.toHexString(bt & 0xFF);
		return sb.toString();// + " : " + bt + " : " + x;
	}

}// class CommandByteBuilder
