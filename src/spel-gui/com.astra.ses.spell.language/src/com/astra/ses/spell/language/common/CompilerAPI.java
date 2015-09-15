package com.astra.ses.spell.language.common;

import com.astra.ses.spell.language.model.IParserHost;

/**
 * Implement Py methods required by PythonGrammar
 * 
 */
public class CompilerAPI implements IParserHost
{
	public Object newInteger(int i)
	{
		return new java.lang.Integer(i);
	}

	public Object newLong(String s)
	{
		return new java.lang.Long(s);
	}

	public Object newLong(java.math.BigInteger i)
	{
		return i;
	}

	public Object newImaginary(double v)
	{
		return new java.lang.Double(v);
	}

	public static Object newFloat(float v)
	{
		return new java.lang.Float(v);
	}

	public Object newFloat(double v)
	{
		return new java.lang.Float(v);
	}

	/**
	 * TODO how do I implement Unicode decoding in Java?
	 */
	public String decode_UnicodeEscape(String str, int start, int end,
	        String errors, boolean unicode)
	{
		return str.substring(start, end);
	}
}
