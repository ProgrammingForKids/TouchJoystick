package com.car.wirelesscontrol.util;

import com.sasa.joystick.JoystickControl;

public class CommandByteBuilder
{

	public static int	MAX_NUMBER_OF_SECTORS	= 8;
	private static byte	m_sec;

	public static byte Sector()
	{
		return m_sec;
	}

	// -------------------------------------------------
	// bit 7 -> 0 - forward, 1 - backward
	// bit 6 -> 0 - right, 1 - left
	// bits 5,4,3,2 -> forward/backward speed value
	// bits 1,0 ->right/left speed value
	// -------------------------------------------------
	public static byte PrepareCommandByteOld(int angle, int power, int direction)
	{
		double Y = power * Math.cos(Math.toRadians(angle));
		double X = power * Math.sin(Math.toRadians(angle));
		int y = (int) (Y * 16) / JoystickControl.POWER_MAX;
		int x = (int) (X * 4) / JoystickControl.POWER_MAX;
		byte by = (byte) Math.abs(y);
		byte bx = (byte) Math.abs(x);
		if (by == 16)
		{
			by = 15;
		}
		if (bx == 4)
		{
			bx = 3;
		}
		byte bp = (byte) ((by << 2) | bx);
		if (0 > Y)
		{
			bp = (byte) (bp | (1 << 7));
		}
		if (0 > X)
		{
			bp = (byte) (bp | (1 << 6));
		}
		return bp;
	}

	public static byte GetSector(int angle)
	{

		byte b = (byte) (((angle * MAX_NUMBER_OF_SECTORS + 180.0) / 360.0) % MAX_NUMBER_OF_SECTORS);
		m_sec = b;
		return b;

	}

	public static byte GetSpeed(int power)
	{
		byte speed = (byte) ((power * 16) / JoystickControl.POWER_MAX);
		if (speed == 16)
		{
			speed = 15;
		}
		return speed;
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
		String x = Integer.toHexString(bt & 0xFF);
		return sb.toString() + " : " + bt + " : " + x;
	}

}// class CommandByteBuilder
