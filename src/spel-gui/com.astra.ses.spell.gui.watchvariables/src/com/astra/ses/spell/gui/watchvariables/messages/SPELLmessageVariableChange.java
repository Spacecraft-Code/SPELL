///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.watchvariables.messages
// 
// FILE      : SPELLmessageVariableChange.java
//
// DATE      : Nov 28, 2011
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
package com.astra.ses.spell.gui.watchvariables.messages;

import com.astra.ses.spell.gui.core.comm.messages.MessageException;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageOneway;
import com.astra.ses.spell.gui.core.interfaces.IMessageField;
import com.astra.ses.spell.gui.watchvariables.notification.VariableData;
import com.astra.ses.spell.gui.watchvariables.notification.VariableNotification;

/*******************************************************************************
 * 
 * Variable change message
 * 
 ******************************************************************************/
public class SPELLmessageVariableChange extends SPELLmessageOneway
{

	/** Procedure Id */
	private String	m_procId;
	/** Holds the changed variable names */
	private String	m_name;
	/** Holds the changed variable types */
	private String	m_type;
	/** Holds the changed variable values */
	private String	m_value;
	/** True if the variable is global */
	private String	m_global;
	/** If the variable is deleted */
	private String	m_deleted;
	/** If the variable is new */
	private String	m_added;

	/***************************************************************************
	 * Tag based constructor
	 * 
	 * @param data
	 **************************************************************************/
	public SPELLmessageVariableChange( SPELLmessage msg )
	{
		super(msg);
		setId(IWVMessageId.MSG_VARIABLE_CHANGE);
		try
		{
			m_procId = get(IMessageField.FIELD_PROC_ID);
			m_name = get(IWVMessageField.FIELD_VARIABLE_NAME);
			m_type = get(IWVMessageField.FIELD_VARIABLE_TYPE);
			m_value = get(IWVMessageField.FIELD_VARIABLE_VALUE);
			m_global = get(IWVMessageField.FIELD_VARIABLE_GLOBAL);
			m_deleted = get(IWVMessageField.FIELD_VARIABLE_DELETE);
			m_added = get(IWVMessageField.FIELD_VARIABLE_ADDED);
		}
		catch (MessageException e)
		{
			e.printStackTrace();
		}
	}

	/***************************************************************************
	 * Get the user action to be performed on demand
	 * 
	 * @return
	 **************************************************************************/
	public VariableNotification getData()
	{
		VariableNotification data = new VariableNotification(m_procId);

		String[] names    = m_name.split(IMessageField.VARIABLE_SEPARATOR);
		String[] types    = m_type.split(IMessageField.VARIABLE_SEPARATOR);
		String[] values   = m_value.split(IMessageField.VARIABLE_SEPARATOR);
		String[] globals  = m_global.split(IMessageField.VARIABLE_SEPARATOR);
		String[] deleteds = m_deleted.split(IMessageField.VARIABLE_SEPARATOR);
		String[] addeds   = m_added.split(IMessageField.VARIABLE_SEPARATOR);

		for (int index = 0; index < names.length; index++)
		{
			boolean added = addeds[index].equals("True");
			boolean deleted = deleteds[index].equals("True");
			VariableData vdata = new VariableData(names[index], types[index],
			        values[index], globals[index].equals("True"), deleted, added);
			
			if (added)
			{
				data.addNewVariable(vdata);
			}
			else if (deleted)
			{
				data.addDeletedVariable(vdata);
			}
			else
			{
				data.addChangedVariable(vdata);
			}
		}
		return data;
	}
}
