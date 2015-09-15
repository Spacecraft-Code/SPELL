///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.messages
// 
// FILE      : SPELLmessageExecOperation.java
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
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ExecutorOperation;
import com.astra.ses.spell.gui.core.model.types.ExecutorOperationSummary;
import com.astra.ses.spell.gui.types.ExecutorStatus;

public class SPELLmessageExecOperation extends SPELLmessageOneway
{
	private String m_procId;
	private ExecutorOperation m_operation;
	private ClientMode m_clientMode;
	private String m_clientKey;
	private ExecutorStatus m_status;
	private ExecutorOperationSummary m_summary;

	public SPELLmessageExecOperation(TreeMap<String, String> data)
	{
		super(data);
		try
		{
			m_procId = get(IMessageField.FIELD_PROC_ID);
			m_operation = ExecutorOperation.valueOf(get(IMessageField.FIELD_EXEC_OP));
			m_clientMode = ClientMode.CONTROL;
			m_clientKey = null;
			m_summary = new ExecutorOperationSummary();
			m_status = ExecutorStatus.UNKNOWN;
			if (hasKey(IMessageField.FIELD_GUI_KEY))
			{
				m_clientKey = get(IMessageField.FIELD_GUI_KEY);
			}
			if (hasKey(IMessageField.FIELD_GUI_MODE))
			{
				String mstr = get(IMessageField.FIELD_GUI_MODE);
				m_clientMode = ClientMode.valueOf(mstr);
			}
			if (hasKey(IMessageField.FIELD_STAGE_ID))
			{
				m_summary.stageId = get(IMessageField.FIELD_STAGE_ID);
			}
			if (hasKey(IMessageField.FIELD_STAGE_TL))
			{
				m_summary.stageTitle = get(IMessageField.FIELD_STAGE_TL);
			}
			if (hasKey(IMessageField.FIELD_EXEC_STATUS))
			{
				m_status = ExecutorStatus.valueOf(get(IMessageField.FIELD_EXEC_STATUS));
			}
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
	}

	public String getProcId()
	{
		return m_procId;
	}

	public ExecutorOperation getOperation()
	{
		return m_operation;
	}

	public ClientMode getClientMode()
	{
		return m_clientMode;
	}

	public String getClientKey()
	{
		return m_clientKey;
	}

	public ExecutorStatus getStatus()
	{
		return m_status;
	}
	
	public ExecutorOperationSummary getSummary()
	{
		return m_summary;
	}
}
