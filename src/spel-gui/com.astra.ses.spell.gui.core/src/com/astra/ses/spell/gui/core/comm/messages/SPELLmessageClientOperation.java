///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.messages
// 
// FILE      : SPELLmessageClientOperation.java
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
import com.astra.ses.spell.gui.core.interfaces.IMessageId;
import com.astra.ses.spell.gui.core.model.server.ClientInfo;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ClientOperation;

public class SPELLmessageClientOperation extends SPELLmessageOneway
{
	private ClientMode	    m_mode;
	private String	        m_host;
	private String	        m_key;
	private ClientOperation	m_operation;
	private String	        m_procId;

	public SPELLmessageClientOperation(TreeMap<String, String> data)
	{
		super(data);
		setId(IMessageId.MSG_CLIENT_OP);
		m_mode = ClientMode.UNKNOWN;
		m_operation = ClientOperation.UNKNOWN;
		m_procId = "";
		m_host = "";
		m_key = "";
		try
		{
			if (hasKey(IMessageField.FIELD_GUI_MODE))
			m_mode = ClientInfo.modeFromString(get(IMessageField.FIELD_GUI_MODE));
		}
		catch (MessageException ex)
		{
		}
		try
		{
			if (hasKey(IMessageField.FIELD_CLIENT_OP))
			m_operation = ClientInfo.operationFromString(get(IMessageField.FIELD_CLIENT_OP));
		}
		catch (MessageException ex)
		{
		}
		try
		{
			if (hasKey(IMessageField.FIELD_GUI_KEY))
			m_key = get(IMessageField.FIELD_GUI_KEY);
		}
		catch (MessageException ex)
		{
		}
		try
		{
			if (hasKey(IMessageField.FIELD_PROC_ID))
			m_procId = get(IMessageField.FIELD_PROC_ID);
		}
		catch (MessageException ex)
		{
		}
		try
		{
			if (hasKey(IMessageField.FIELD_HOST))
			m_host = get(IMessageField.FIELD_HOST);
		}
		catch (MessageException ex)
		{
		}
	}

	public ClientMode getClientMode()
	{
		return m_mode;
	}

	public ClientOperation getOperation()
	{
		return m_operation;
	}

	public String getClientKey()
	{
		return m_key;
	}

	public String getProcId()
	{
		return m_procId;
	}

	public String getHost()
	{
		return m_host;
	}
}
