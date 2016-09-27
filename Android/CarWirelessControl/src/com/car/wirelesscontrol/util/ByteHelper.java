package com.car.wirelesscontrol.util;

import com.sasa.logger.Logger;

public class ByteHelper
{
	private byte m_byte = 0;

	public ByteHelper()
	{
	}

	public ByteHelper(byte b)
	{
		m_byte = b;
	}

	public byte Get()
	{
		return m_byte;
	}

	public void Set(int pos)
	{
		m_byte = (byte) (m_byte | (1 << pos));
	}

	public int getBit(int pos)
	{
		return (m_byte >> pos) & 1;
	}

	public void Set(final String sbyte)
	{
		if (null == sbyte)
		{
			m_byte = 0;
		}
		else
		{
			int pos = 7;
			int length = sbyte.length();
			for (int k = 0; k < length; ++k)
			{
				if (pos < 0)
				{
					break;
				}
				if (sbyte.charAt(k) == '1')
				{
					Set(pos);
				}
				pos -= 1;
			}
		}
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (int k = 0; k < 8; ++k)
		{
			sb.append((m_byte >> (7 - k)) & 1);
		}
		return sb.toString();
	}
	
	public static String toString(byte b)
	{
		StringBuilder sb = new StringBuilder();
		for (int k = 0; k < 8; ++k)
		{
			sb.append((b >> (7 - k)) & 1);
		}
		return sb.toString();
	}
	
	
	public void test(byte b)
	{
		String s = "11111111";
		Set(s);
		byte err = (byte)(m_byte & 0xC0);
		err = (byte) (err>>6 & 2);
		String res = toString(err);
		String sb = "" + err;
		Logger.Log.t("TEST",res,sb);
	}
};// class ByteHelper
