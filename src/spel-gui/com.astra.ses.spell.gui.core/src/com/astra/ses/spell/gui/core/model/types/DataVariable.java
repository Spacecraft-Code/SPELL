///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.types
// 
// FILE      : DataVariable.java
//
// DATE      : Feb 23, 2012
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

import java.util.Arrays;
import java.util.Date;


public class DataVariable
{
	
	private String m_name;
	private String m_value;
	private String[] m_range;
	private String[] m_expected;
	private ValueFormat m_format;
	private ValueType m_type;
	private boolean m_confirmGet;
	
	private String m_newValue;
	private Boolean m_newConfirm;
	
	private boolean m_new;

	/***************************************************************************
	 * Construct an untyped variable for a regular dictionary
	 **************************************************************************/
	public DataVariable( String name, String value )
	{
		this(name,value,null,null,ValueFormat.NONE,ValueType.UNTYPED,false,false);
	}

	/***************************************************************************
	 * Construct an untyped variable for a regular dictionary
	 **************************************************************************/
	public DataVariable( String name, String value, boolean newVar )
	{
		this(name,value,null,null,ValueFormat.NONE,ValueType.UNTYPED,false,newVar);
	}

	/***************************************************************************
	 * Construct a typed variable for a data container
	 **************************************************************************/
	public DataVariable( String name, String value, String[] range, String[] expected, ValueFormat format, ValueType type, boolean confirmGet )
	{
		this(name,value,range,expected,format,type,confirmGet,false);
	}

	/***************************************************************************
	 * Construct a NEW typed variable for a data container
	 **************************************************************************/
	public DataVariable( String name, String value, String[] range, String[] expected, ValueFormat format, ValueType type, boolean confirmGet, boolean newVar )
	{
		m_name = name;
		m_value = value;
		m_range = range;
		m_expected = expected;
		m_format = format;
		m_type = type;
		m_confirmGet = confirmGet;
		
		m_newValue = null;
		m_newConfirm = null;
		
		m_new = newVar;
		
		if (m_type == null || m_type.equals(ValueType.NONE))
		{
			m_type = inferTypeFromValue(m_value);
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private DataVariable( DataVariable other )
	{
		m_name = new String(other.m_name);
		m_value = new String(other.m_value);
		if (other.m_range != null)
		{
			m_range = Arrays.copyOf(other.m_range, other.m_range.length);
		}
		else
		{
			m_range = null;
		}
		if (other.m_expected != null)
		{
			m_expected = Arrays.copyOf( other.m_expected, other.m_expected.length);
		}
		else
		{
			m_expected = null;
		}
		m_format = other.m_format;
		m_type = other.m_type;
		m_confirmGet = other.m_confirmGet;
	}
	
	/***************************************************************************
	 * Check if is typed or not
	 **************************************************************************/
	public boolean isTyped()
	{
		return m_type.equals(ValueType.UNTYPED);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public DataVariable copy()
	{
		return new DataVariable(this);
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean isModified()
	{
		return (m_new || (m_newValue != null) || (m_newConfirm != null));
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean isNew()
	{
		return m_new;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void revert()
	{
		m_newValue = null;
		m_newConfirm = null;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void save()
	{	
		if (m_newValue != null)
		{
			m_value = m_newValue;
			m_newValue = null;
		}
		if (m_newConfirm != null)
		{
			m_confirmGet = m_newConfirm;
			m_newConfirm = null;
		}
		m_new = false;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean setValue( String value )
	{
		if (value == null) return false;
		value = value.trim();
		if ((value.isEmpty())||value.equals("None"))
		{
			return false;
		}
		
		value = unformatValue(value);

		// Reset to original value by user
		if (value.equals(m_value))
		{
			m_newValue = null;
			return true;
		}
		
		// Do nothing
		if (value.equals(m_newValue))
		{
			return false;
		}
		
		try
		{
			checkValueAgainstType(value);
			checkValueAgainstRange(value);
			checkValueAgainstExpected(value);

			m_newValue = value;
			
			if (m_type.equals(ValueType.NONE))
			{
				inferTypeFromValue( m_newValue );
			}
			return true;
		}
		catch(Exception ex)
		{
			throw new RuntimeException("Cannot accept value '" +value + "': " + ex.getLocalizedMessage());
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setConfirmGet( boolean confirm )
	{
		if (confirm != m_confirmGet)
		{
			m_newConfirm = confirm;
		}
		else
		{
			m_newConfirm = null;
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private String unformatValue( String value )
	{
		if (m_type.equals(ValueType.UNTYPED) || value.equals("None"))
		{
			return value;
		}
		else if (m_type.equals(ValueType.LONG))
		{
			int intValue = 0;
			if (value.startsWith("0b"))
			{
				intValue = Integer.valueOf(value.substring(2),2);
				m_format = ValueFormat.BIN;
			}
			else if (value.startsWith("0x"))
			{
				intValue = Integer.valueOf(value.substring(2),16);
				m_format = ValueFormat.HEX;
			}
			else if (value.startsWith("0") && !value.equals("0"))
			{
				intValue = Integer.valueOf(value.substring(1),8);
				m_format = ValueFormat.OCT;
			}
			else
			{
				intValue = Integer.valueOf(value);
				m_format = ValueFormat.DEC;
			}
			return Integer.toString(intValue);
		}
		return value;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public String formatValue( String value )
	{
		if (m_type.equals(ValueType.UNTYPED) || value.equals("None"))
		{
			return value;
		}
		String toReturn = value;
		int intValue = 0;
		if (m_type.equals(ValueType.LONG))
		{
			switch(m_format)
			{
			case BIN:
				if (value.startsWith("0b")) return value;
				try
				{
					intValue = Integer.valueOf(toReturn);
					toReturn = "0b" + Integer.toString(intValue, 2).toUpperCase();
				}
				catch(NumberFormatException ex)
				{
					// Check if it was already formatted
					intValue = Integer.valueOf(toReturn,2);
					toReturn = "0b" + Integer.toString(intValue, 2).toUpperCase();
				}
				break;
			case HEX:
				if (value.startsWith("0x")) return value;
				try
				{
					intValue = Integer.valueOf(toReturn);
					toReturn = "0x" + Integer.toString(intValue, 16).toUpperCase();
				}
				catch(NumberFormatException ex)
				{
					// Check if it was already formatted
					intValue = Integer.valueOf(toReturn,16);
					toReturn = "0x" + Integer.toString(intValue, 16).toUpperCase();
				}
				break;
			case OCT:
				if (value.startsWith("0")) return value;
				try
				{
					intValue = Integer.valueOf(toReturn);
					toReturn = "0" + Integer.toString(intValue, 8).toUpperCase();
				}
				catch(NumberFormatException ex)
				{
					// Check if it was already formatted
					intValue = Integer.valueOf(toReturn,8);
					toReturn = "0" + Integer.toString(intValue, 8).toUpperCase();
				}
				break;
			default:
				break;
			}
		}
		return toReturn;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void markNew()
	{
		m_new = true;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getValue()
	{
		String toReturn = m_value;
		if (m_newValue != null) 
		{
			toReturn = m_newValue;
		}
		return toReturn.trim();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getName()
	{
		return m_name;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public String[] getRange()
	{
		return m_range;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public String[] getExpected()
	{
		return m_expected;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getFormat()
	{
		return m_format.name();
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getType()
	{
		return m_type.name();
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getConfirmGet()
	{
		if (m_newConfirm != null)
		{
			return (m_newConfirm ? "True" : "False");
		}
		return (m_confirmGet ? "True" : "False");
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void checkValueAgainstType( String value ) throws Exception
	{
		switch(m_type)
		{
		case UNTYPED:
		case NONE:
			return;
		case LONG:
			if (value.startsWith("0x"))
			{
				Integer.parseInt(value.substring(2), 16);
				m_format = ValueFormat.HEX;
			}
			else if (value.startsWith("0b"))
			{
				Integer.parseInt(value.substring(2), 2);
				m_format = ValueFormat.BIN;
			}
			else if (value.startsWith("0") && !value.equals("0"))
			{
				Integer.parseInt(value.substring(1), 8);
				m_format = ValueFormat.OCT;
			}
			else
			{
				Integer.parseInt(value);
			}
			break;
		case FLOAT:
			Double.parseDouble(value);
			break;
		case DATETIME:
			if (!SpellDate.isSpellDate(value))
			{
				throw new RuntimeException("Not a valid SPELL date.\n\n" + SpellDate.absFormats());
			}
			break;
		case RELTIME:
			if (!SpellDate.isRelativeSpellDate(value))
			{
				throw new RuntimeException("Not a valid relative SPELL date.\n\n" + SpellDate.relFormats());
			}
			break;
		case BOOL:
			if (!value.equals("True") && !value.equals("False"))
			{
				throw new RuntimeException("Not a valid SPELL boolean (True,False).");
			}
			break;
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public static ValueType inferTypeFromValue( String value )
	{
		value = value.trim();
		if (value == null || value.equals("None") || value.trim().isEmpty() )
		{
			return ValueType.NONE;
		}
		try
		{
			Integer.parseInt(value);
			return ValueType.LONG;
		}
		catch(Exception ex1)
		{
			try
			{
				Double.parseDouble(value);
				return ValueType.FLOAT;
			}
			catch(Exception ex2)
			{
				if (SpellDate.isRelativeSpellDate(value))
				{
					return ValueType.RELTIME;
				}
				else if (SpellDate.isSpellDate(value))
				{
					return ValueType.DATETIME;
				}
				else
				{
					if (value.equals("True") || value.equals("False"))
					{
						return ValueType.BOOL;
					}
					else
					{
						return ValueType.STRING;
					}
				}
			}
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private void checkValueAgainstRange( String value )
	{
		if (m_type.equals(ValueType.NONE) || m_type.equals(ValueType.UNTYPED)) return;
		if (m_range != null && m_range.length == 2)
		{
			switch(m_type)
			{
			case FLOAT:
				double doubleV = Double.parseDouble(value);
				double doubleL = Double.parseDouble(m_range[0]);
				double doubleH = Double.parseDouble(m_range[1]);
				if ((doubleL <= doubleV) && (doubleV <= doubleH)) return;
				break;
			case LONG:
				int intV = Integer.parseInt(value);
				int intL = Integer.parseInt(m_range[0]);
				int intH = Integer.parseInt(m_range[1]);
				if ((intL <= intV) && (intV <= intH)) return;
				break;
			case RELTIME:
			{
				Date valueDate = SpellDate.getRelativeTime(value);
				Date lowTime = SpellDate.getRelativeTime(m_range[0]);
				Date hiTime = SpellDate.getRelativeTime(m_range[1]);
				if ( lowTime.compareTo(valueDate) == 1 )
				{
					throw new RuntimeException("Given value (" + value + ") does not match range of accepted values " + Arrays.toString(m_range) + "\n\nValue is less than lower limit.");
				}
				if ( hiTime.compareTo(valueDate) == -1 )
				{
					throw new RuntimeException("Given value (" + value + ") does not match range of accepted values " + Arrays.toString(m_range) + "\n\nValue is greater than upper limit.");
				}
				break;
			}
			case DATETIME:
			{
				Date valueDate = SpellDate.getAbsoluteTime(value);
				Date lowTime = SpellDate.getAbsoluteTime(m_range[0]);
				Date hiTime = SpellDate.getAbsoluteTime(m_range[1]);
				if ( lowTime.compareTo(valueDate) == 1 )
				{
					throw new RuntimeException("Given value (" + value + ") does not match range of accepted values " + Arrays.toString(m_range) + "\n\nValue is less than lower limit.");
				}
				if ( hiTime.compareTo(valueDate) == -1 )
				{
					throw new RuntimeException("Given value (" + value + ") does not match range of accepted values " + Arrays.toString(m_range) + "\n\nValue is greater than upper limit.");
				}
				break;
			}
			default:
				break;
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void checkValueAgainstExpected( String value )
	{
		if (m_type.equals(ValueType.NONE) || m_type.equals(ValueType.UNTYPED)) return;
		if (m_expected != null && m_expected.length > 0)
		{
			for( String exv : m_expected )
			{
				if (value.equals(exv)) return;
			}
			throw new RuntimeException("Given value (" + value + ") does not match list of expected values " + Arrays.toString(m_expected));
		}
	}
}
