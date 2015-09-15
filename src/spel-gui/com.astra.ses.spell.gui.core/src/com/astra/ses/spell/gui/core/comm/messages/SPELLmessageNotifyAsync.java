///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.messages
// 
// FILE      : SPELLmessageNotifyAsync.java
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

import java.util.TreeMap;

import com.astra.ses.spell.gui.core.interfaces.IMessageField;
import com.astra.ses.spell.gui.core.model.DataType;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.NotificationData;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.types.ItemType;
import com.astra.ses.spell.gui.types.ExecutorStatus;

public class SPELLmessageNotifyAsync extends SPELLmessage
{
	private String	       m_procId;
	private DataType	   m_dataType;
	private ItemType	   m_notType;
	private ExecutorStatus	m_status;
	private String	       m_csp;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public SPELLmessageNotifyAsync(TreeMap<String, String> data)
	{
		super(data);
		try
		{
			m_procId = get(IMessageField.FIELD_PROC_ID);
			m_dataType = DataType.valueOf(get(IMessageField.FIELD_DATA_TYPE));
			m_csp = m_procId;
			m_notType = null;
			m_status = null;
			if (m_dataType == DataType.ITEM)
			{
				m_notType = ItemType
				        .fromName(get(IMessageField.FIELD_ITEM_TYPE));
				m_csp = get(IMessageField.FIELD_CSP);
			}
			else if (m_dataType == DataType.STATUS)
			{
				m_status = ExecutorStatus
				        .valueOf(get(IMessageField.FIELD_EXEC_STATUS));
			}
			else if (m_dataType == DataType.CURRENT_LINE
			        || m_dataType == DataType.CALL
			        || m_dataType == DataType.RETURN)
			{
				m_csp = get(IMessageField.FIELD_CSP);
			}
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
	}

	/***************************************************************************
	 * Obtain the item field
	 **************************************************************************/
	private String getItemName()
	{
		String name = "<UKN>";
		try
		{
			name = get(IMessageField.FIELD_ITEM_NAME);
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		return name;
	}

	/***************************************************************************
	 * Obtain the item field
	 **************************************************************************/
	private String getItemValue()
	{
		String value = "<UKN>";
		try
		{
			value = get(IMessageField.FIELD_ITEM_VALUE);
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		return value;
	}

	/***************************************************************************
	 * Obtain the status field
	 **************************************************************************/
	private String getItemStatus()
	{
		String status = "";
		try
		{
			status = get(IMessageField.FIELD_ITEM_STATUS);
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		return status;
	}

	/***************************************************************************
	 * Obtain the comments field
	 **************************************************************************/
	private String getItemComment()
	{
		String comment = "";
		try
		{
			comment = get(IMessageField.FIELD_ITEM_COMMENT);
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		return comment;
	}

	/***************************************************************************
	 * Obtain the item time field
	 **************************************************************************/
	private String getItemTime()
	{
		String time = "";
		try
		{
			time = get(IMessageField.FIELD_ITEM_TIME);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return time;
	}

	/***************************************************************************
	 * Obtain the time field
	 **************************************************************************/
	private String getTime()
	{
		String time = "";
		try
		{
			time = get(IMessageField.FIELD_TIME);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return time;
	}

	/***************************************************************************
	 * Build a notification data structure from this SPELL message
	 * 
	 * @return The data structure, null if error
	 **************************************************************************/
	public NotificationData getData()
	{
		NotificationData data = null;
		if (m_dataType == DataType.ITEM)
		{
			data = new ItemNotification(m_procId, m_notType, m_csp);
			((ItemNotification) data).setTime(getTime());
			((ItemNotification) data).setItems(getItemName(), getItemValue(),
			        getItemStatus(), getItemComment(), getItemTime());
		}
		else if (m_dataType == DataType.STATUS)
		{
			data = new StatusNotification(m_procId, m_status);
		}
		else if (m_dataType == DataType.CURRENT_LINE)
		{
			String codeName = "";
			try
			{
				codeName = get(IMessageField.FIELD_CODE_NAME);
			}
			catch (Exception ex)
			{
			}
			;
			data = new StackNotification(StackNotification.StackType.LINE,
			        m_procId, m_csp, codeName);
			if (hasKey(IMessageField.FIELD_STAGE_ID))
			{
				try
				{
					String id = get(IMessageField.FIELD_STAGE_ID);
					String title = get(IMessageField.FIELD_STAGE_TL);
					((StackNotification) data).setStage(id, title);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				;
			}
		}
		else if (m_dataType == DataType.CALL)
		{
			String codeName = "";
			try
			{
				codeName = get(IMessageField.FIELD_CODE_NAME);
			}
			catch (Exception ex)
			{
			}
			;
			data = new StackNotification(StackNotification.StackType.CALL,
			        m_procId, m_csp, codeName);
		}
		else if (m_dataType == DataType.RETURN)
		{
			String codeName = "";
			try
			{
				codeName = get(IMessageField.FIELD_CODE_NAME);
			}
			catch (Exception ex)
			{
			}
			;
			data = new StackNotification(StackNotification.StackType.RETURN,
			        m_procId, m_csp, codeName);
		}
		try
		{
			data.setSequence(Long
			        .parseLong(get(IMessageField.FIELD_MSG_SEQUENCE)));
		}
		catch (Exception e)
		{
			data.setSequence(99999);
		}
		
		data.setTime(getTime());
		
		return data;
	}
}
