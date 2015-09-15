///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.types
// 
// FILE      : SpellDate.java
//
// DATE      : Feb 9, 2012
//
// Copyright (C) 2008, 2015 SES ENGINEERING, Luxembourg S.A.R.L.
//
// By using this software in any way, you are agreeing to be bound by
// the terms of this license.
//
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// NO WARRANTY
// EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, THE PROGRAM IS PROVIDED
// ON AN "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER
// EXPRESS OR IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR
// CONDITIONS OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A
// PARTICULAR PURPOSE. Each Recipient is solely responsible for determining
// the appropriateness of using and distributing the Program and assumes all
// risks associated with its exercise of rights under this Agreement ,
// including but not limited to the risks and costs of program errors,
// compliance with applicable laws, damage to or loss of data, programs or
// equipment, and unavailability or interruption of operations.
//
// DISCLAIMER OF LIABILITY
// EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, NEITHER RECIPIENT NOR ANY
// CONTRIBUTORS SHALL HAVE ANY LIABILITY FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING WITHOUT LIMITATION
// LOST PROFITS), HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THE PROGRAM OR THE
// EXERCISE OF ANY RIGHTS GRANTED HEREUNDER, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGES.
//
// Contributors:
//    SES ENGINEERING - initial API and implementation and/or initial documentation
//
// PROJECT   : SPELL
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.core.model.types;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class SpellDate
{
	private static List<DateFormat> s_absFormats;
	private static List<DateFormat> s_relFormats;
	private static ScriptEngine s_engine;
	
	private static final long SECOND_SEC = 1;
	private static final long MINUTE_SEC = SECOND_SEC*60;
	private static final long HOUR_SEC = MINUTE_SEC*60;
	private static final long DAY_SEC = HOUR_SEC*24;

	private static String[] s_absFormatsStr = {
		"yyyy-MM-dd HH:mm:ss" ,
  		"dd-MMM-yyyy HH:mm:ss",
  		"dd/MM/yyyy HH:mm:ss" ,
  		"dd-MM-yyyy HH:mm:ss" ,
  		"yyyy-MM-dd:HH:mm:ss" ,
  		"dd-MMM-yyyy:HH:mm:ss",
  		"dd/MM/yyyy:HH:mm:ss" ,
  		"dd-MM-yyyy:HH:mm:ss" ,
  		"yyyy-MM-dd"          ,
  		"dd-MMM-yyyy"         ,
  		"dd/MM/yyyy"          ,
  		"dd-MM-yyyy"          ,
  		"yyyy-MM-dd HH:mm"    ,
  		"dd-MMM-yyyy HH:mm"   ,
  		"dd/MM/yyyy HH:mm"    ,
  		"dd-MM-yyyy HH:mm"    ,
  		"+ddd HH:mm:ss"       ,
  		"-ddd HH:mm:ss"       ,
  		"+HH:mm:ss"           ,
  		"+HH:mm"              ,
  		"-HH:mm:ss"           ,
  		"-HH:mm"              ,
  		"+ss.SSS"             ,
  		"-ss.SSS"            				  };

	private static String[] s_relFormatsStr = {
		 	 "+ddd HH:mm:ss"       ,
		 	 "-ddd HH:mm:ss"       ,
		 	 "+HH:mm:ss"           ,
		 	 "+HH:mm"              ,
		 	 "-HH:mm:ss"           ,
		 	 "-HH:mm"              ,
		 	 "+ss.SSS"             ,
		 	 "-ss.SSS"                        };

	static
	{
		ScriptEngineManager mgr = new ScriptEngineManager();
		s_engine = mgr.getEngineByName("JavaScript");
		
		
		s_absFormats = new ArrayList<DateFormat>();
		s_relFormats = new ArrayList<DateFormat>();

		for(String f : s_absFormatsStr)
		{
			s_absFormats.add( new SimpleDateFormat(f) );
		}
		
		for(String f : s_relFormatsStr)
		{
			s_relFormats.add( new SimpleDateFormat(f) );
		}
	}

	public static String absFormats()
	{
		String fs = "Absolute dates:\n";
		for(String f : s_absFormatsStr)
		{
			fs += "   " + f + "\n";
		}
		return fs;
	}

	public static String relFormats()
	{
		String fs = "Relative times:\n";
		for(String f : s_relFormatsStr)
		{
			fs += "   " + f + "\n";
		}
		return fs;
	}

	private static boolean tryFormat( String value, DateFormat f )
	{
		try
		{
			ParsePosition pos = new ParsePosition(0);
			int idx = value.indexOf(".");
			if (idx != -1)
			{
				String aux = value.substring(0,idx); 
				f.parse(aux,pos);
				if (pos.getIndex() == aux.length())
				{
					try
					{
						Integer.parseInt(value.substring(idx+1,value.length()-1));
						return true;
					}
					catch(Exception ex) {};
				}
			}
			else
			{
				f.parse(value,pos);
				if (pos.getIndex() == value.length())
				{
					return true;
				}
			}
		}
		catch(Exception ex) {};
		return false;
	}
	
	public static boolean isRelativeSpellDateString( String value )
	{
		String toCheck = value;
		if (toCheck.startsWith("'") && toCheck.endsWith("'")) toCheck = toCheck.substring(1,toCheck.length()-2);
		if (toCheck.startsWith("\"") && toCheck.endsWith("\"")) toCheck = toCheck.substring(1,toCheck.length()-2);

		for(DateFormat f : s_relFormats)
		{
			if (tryFormat(toCheck,f))
			{
				return true;
			}
		}
		return false;
	}

	public static boolean isAbsoluteSpellDateString( String value )
	{
		String toCheck = value;
		if (toCheck.startsWith("'") && toCheck.endsWith("'")) toCheck = toCheck.substring(1,toCheck.length()-2);
		if (toCheck.startsWith("\"") && toCheck.endsWith("\"")) toCheck = toCheck.substring(1,toCheck.length()-2);

		for(DateFormat f : s_absFormats)
		{
			if (tryFormat(toCheck,f))
			{
				return true;
			}
		}
		return false;
	}

	public static boolean isSpellDateString( String value )
	{
		if (isAbsoluteSpellDateString(value))
		{
			return true;
		}
		else if (isRelativeSpellDateString(value))
		{
			return true;
		}
		return false;
	}

	public static boolean isRelativeSpellDate( String value )
	{
		if (isRelativeSpellDateString(value))
		{
			return true;
		}
		else
		{
			if (isTimeExpression(value))
			{
				if (isRelativeExpression(value))
				{
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isAbsoluteSpellDate( String value )
	{
		if (isAbsoluteSpellDateString(value))
		{
			return true;
		}
		else if (isTimeExpression(value))
		{
			return true;
		}
		return false;
	}

	public static boolean isSpellDate( String value )
	{
		if (isAbsoluteSpellDate(value))
		{
			return true;
		}
		if (isRelativeSpellDate(value))
		{
			return true;
		}
		if (isTimeExpression(value))
		{
			return true;
		}
		return false;
	}
	
	public static Date getAbsoluteTime( String value )
	{
		String toEval = value.trim();
		if (toEval.startsWith("TIME(") && toEval.endsWith(")"))
		{
			toEval = toEval.replace("TIME(", "").substring(0,toEval.length()-1);
		}
		try
		{
			for(DateFormat f : s_absFormats)
			{
				if (tryFormat(toEval,f))
				{
					return f.parse(toEval);
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		throw new RuntimeException("Could not parse expression: '" + value + "'");
	}

	public static Date getRelativeTime( String value )
	{
		String toEval = value.trim();
		if (toEval.contains("TIME(") && toEval.endsWith(")"))
		{
			toEval = toEval.replace("TIME(", "").substring(0,toEval.length()-1);
		}
		try
		{
			for(DateFormat f : s_relFormats)
			{
				if (tryFormat(value,f))
				{
					return f.parse(value);
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return evaluateRelativeExpression(toEval);
	}

	private static Date evaluateRelativeExpression( String value )
	{
		Date theDate = null;
		String toEval = value;
		if (toEval.contains("MINUTE"))
		{
			toEval = toEval.replace("MINUTE", String.valueOf(MINUTE_SEC));
		}
		if (toEval.contains("HOUR"))
		{
			toEval = toEval.replace("HOUR", String.valueOf(HOUR_SEC));
		}
		if (toEval.contains("SECOND"))
		{
			toEval = toEval.replace("SECOND", String.valueOf(SECOND_SEC));
		}
		if (toEval.contains("DAY"))
		{
			toEval = toEval.replace("DAY", String.valueOf(DAY_SEC));
		}
		long result = -1;
		try
		{
			// Given in seconds
			result = (Long) s_engine.eval(toEval);
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(result*1000);
			theDate = c.getTime();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException("Could not parse expression: '" + value + "'");
		}
		return theDate;
	}
	
	public static boolean isRelativeExpression( String value )
	{
		String aux = value;
		aux = aux.replace("MINUTE", "").replace("HOUR", "").replace("SECOND", "").replace("DAY", "");
		aux = aux.replace("MONTH", "").replace("TIME", "");
		aux = aux.replace("*", "").replace("(", "").replace(")", "").replace("+ ", "");
		aux = aux.replace(".", "").replace("-", "").replace("0", "").replace("1", "");		
		aux = aux.replace("2", "").replace("3", "").replace("4", "").replace("5", "");		
		aux = aux.replace("6", "").replace("7", "").replace("8", "").replace("9", "");		
		if (!aux.trim().isEmpty())
		{
			return false;
		}
		return true;
	}

	public static boolean isTimeExpression( String value )
	{
		String aux = value;
		aux = aux.replace("NOW", "").replace("TOMORROW", "").replace("TODAY", "").replace("YESTERDAY", "");
		aux = aux.replace("MINUTE", "").replace("HOUR", "").replace("SECOND", "").replace("DAY", "");
		aux = aux.replace("MONTH", "").replace("TIME", "");
		aux = aux.replace("*", "").replace("(", "").replace(")", "").replace("+ ", "");
		aux = aux.replace(".", "").replace("-", "").replace("0", "").replace("1", "");		
		aux = aux.replace("2", "").replace("3", "").replace("4", "").replace("5", "");		
		aux = aux.replace("6", "").replace("7", "").replace("8", "").replace("9", "");		
		if (!aux.trim().isEmpty())
		{
			return false;
		}
		return true;
	}
}
