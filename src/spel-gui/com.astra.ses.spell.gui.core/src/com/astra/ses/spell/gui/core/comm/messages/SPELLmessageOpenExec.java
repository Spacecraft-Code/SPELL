///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.messages
// 
// FILE      : SPELLmessageOpenExec.java
//
// DATE      : 2008-11-21 08:58
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
// SUBPROJECT: SPELL GUI Client
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.core.comm.messages;

import java.util.Map;

import com.astra.ses.spell.gui.core.interfaces.IMessageField;
import com.astra.ses.spell.gui.core.interfaces.IMessageId;
import com.astra.ses.spell.gui.core.interfaces.IMessageValue;
import com.astra.ses.spell.gui.core.model.types.ClientMode;

public class SPELLmessageOpenExec extends SPELLmessageRequest
{
	public SPELLmessageOpenExec(String procId, boolean background)
	{
		super(IMessageId.REQ_OPEN_EXEC);
		set(IMessageField.FIELD_PROC_ID, procId);
		if (background)
		{
			set(IMessageField.FIELD_GUI_MODE, ClientMode.BACKGROUND.name());
		}
		else
		{
			set(IMessageField.FIELD_GUI_MODE, ClientMode.CONTROL.name());
		}
		setSender(IMessageValue.CLIENT_SENDER);
		setReceiver(IMessageValue.CONTEXT_RECEIVER);
	}

	public void setArguments(Map<String, String> arguments)
	{
		String argStr = "{";
		for (String key : arguments.keySet())
		{
			if (argStr.length() > 1) argStr += ",";
			String value = arguments.get(key);
			if (!isStringLiteral(value) && !isInteger(value) && !isDouble(value))
			{
				value = "'" + value + "'";
			}
			String arg = "'" + key + "':" + value;
			argStr += arg;
		}
		argStr += "}";
		set(IMessageField.FIELD_ARGS, argStr);
	}

	public void setCondition(String condition)
	{
		set(IMessageField.FIELD_CONDITION, condition);
		set(IMessageField.FIELD_GUI_MODE, ClientMode.SCHEDULE.name());//TODO remove SCHEDULE
	}
	
	private boolean isStringLiteral( String value )
	{
		if (value.startsWith("\"") && value.endsWith("\"")) return true;
		if (value.startsWith("'") && value.endsWith("'")) return true;
		return false;
	}
	
	private boolean isInteger( String value )
	{
		try
		{
			Integer.parseInt(value);
			return true;
		}
		catch(NumberFormatException ex1)
		{
			try
			{
				Integer.parseInt(value,16);
				return true;
			}
			catch(NumberFormatException ex2)
			{
				try
				{
					Integer.parseInt(value,2);
					return true;
				}
				catch(NumberFormatException ex3)
				{
					try
					{
						Integer.parseInt(value,8);
						return true;
					}
					catch(NumberFormatException ex4)
					{
					}
				}
			}
			return false;
		}
	}

	private boolean isDouble( String value )
	{
		try
		{
			Integer.parseInt(value);
			return true;
		}
		catch(NumberFormatException ex)
		{
			return false;
		}
	}
}
