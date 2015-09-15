///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.messages
// 
// FILE      : SPELLmessageExecInfo.java
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

import java.util.ArrayList;
import java.util.List;

import com.astra.ses.spell.gui.core.interfaces.IExecutorInfo;
import com.astra.ses.spell.gui.core.interfaces.IMessageField;
import com.astra.ses.spell.gui.core.interfaces.IMessageId;
import com.astra.ses.spell.gui.core.interfaces.IMessageValue;
import com.astra.ses.spell.gui.core.interfaces.IProcedureClient;
import com.astra.ses.spell.gui.core.model.server.ProcedureClient;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.types.ExecutorStatus;

public class SPELLmessageExecInfo extends SPELLmessageRequest
{
	/***************************************************************************
	 * 
	 **************************************************************************/
	public SPELLmessageExecInfo(String procId)
	{
		super(IMessageId.REQ_EXEC_INFO);
		set(IMessageField.FIELD_PROC_ID, procId);
		setSender(IMessageValue.CLIENT_SENDER);
		setReceiver(IMessageValue.CONTEXT_RECEIVER);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public static void fillExecInfo(IExecutorInfo model, SPELLmessage response)
	{
		extractBasics(response,model);
		
		extractClientInformation(response,model);
		
		extractGroupInformation(response,model);
		
		extractOpenMode(response,model);

		extractUserAction(response, model);
		
		extractAsRunInformation(response,model);
		
		extractStageInformation(response,model);

		extractStackInformation(response,model);
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private static void extractBasics( SPELLmessage msg, IExecutorInfo model )
	{
		try
		{
			model.setName(msg.get(IMessageField.FIELD_PROC_NAME));
			model.setStatus(ExecutorStatus.valueOf(msg.get(IMessageField.FIELD_EXEC_STATUS)));
			if (msg.hasKey(IMessageField.FIELD_CONDITION))
			{
				model.setCondition(msg.get(IMessageField.FIELD_CONDITION));
			}
			if (msg.hasKey(IMessageField.FIELD_PARENT_PROC))
			{
				model.setParent(msg.get(IMessageField.FIELD_PARENT_PROC));
				model.setParentCallingLine(Integer.parseInt(msg.get(IMessageField.FIELD_PARENT_PROC_LINE)));
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private static void extractClientInformation( SPELLmessage msg, IExecutorInfo model )
	{
		try
		{
			String client = msg.get(IMessageField.FIELD_GUI_CONTROL);
			if ((client != null) && (!client.trim().isEmpty()))
			{
				if (client.equals("<BACKGROUND>"))
				{
					model.setBackground(true);
				}
				else
				{
					model.setBackground(false);
					String cClientHost = msg.get(IMessageField.FIELD_GUI_CONTROL_HOST);
					if (cClientHost.trim().isEmpty())
					{
						cClientHost = "(unknown host)";
					}
					model.setControllingClient(new ProcedureClient(client, cClientHost));
				}
			}

			String list = msg.get(IMessageField.FIELD_GUI_LIST);
			List<IProcedureClient> mClients = new ArrayList<IProcedureClient>();
			if ((list != null) && (!list.trim().isEmpty()))
			{
				String[] mClientList = list.split(",");
				for (String mClient : mClientList)
				{
					String[] pair = mClient.split(":");
					if (pair.length == 2)
					{
						mClients.add(new ProcedureClient(pair[1], pair[0]));
					}
					else
					{
						mClients.add(new ProcedureClient(pair[0], "(unknown)"));
					}
				}
			}
			model.setMonitoringClients(mClients.toArray( new IProcedureClient[0] ));
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private static void extractGroupInformation( SPELLmessage msg, IExecutorInfo model )
	{
		try
		{
			if (msg.hasKey(IMessageField.FIELD_GROUP_ID))
			{
				model.setGroupId(msg.get(IMessageField.FIELD_GROUP_ID));
			}

			if (msg.hasKey(IMessageField.FIELD_ORIGIN_ID))
			{
				model.setOriginId(msg.get(IMessageField.FIELD_ORIGIN_ID));
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private static void extractOpenMode( SPELLmessage msg, IExecutorInfo model )
	{
		try
		{
			String mode = msg.get(IMessageField.FIELD_OPEN_MODE);
			String elements[] = mode.split(",");
			for (String elem : elements)
			{
				String attr[] = elem.split(":");
				if (attr.length != 2)
				{
					continue;
				}
				String value = attr[1].trim();
				if (elem.indexOf(IMessageValue.OPEN_MODE_AUTOMATIC) != -1)
				{
					model.setAutomatic(value.equals("True"));
				}
				else if (elem.indexOf(IMessageValue.OPEN_MODE_VISIBLE) != -1)
				{
					model.setVisible(value.equals("True"));
				}
				else if (elem.indexOf(IMessageValue.OPEN_MODE_BLOCKING) != -1)
				{
					model.setBlocking(value.equals("True"));
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private static void extractUserAction( SPELLmessage msg, IExecutorInfo model )
	{
		if (msg.hasKey(IMessageField.FIELD_ACTION_LABEL))
		{
			try
			{
				model.setUserAction(msg.get(IMessageField.FIELD_ACTION_LABEL));
			}
			catch (MessageException ex)
			{
				ex.printStackTrace();
			}
		}

		if (msg.hasKey(IMessageField.FIELD_ACTION_SEVERITY))
		{
			try
			{
				model.setUserActionSeverity( Severity.valueOf(msg.get(IMessageField.FIELD_ACTION_SEVERITY) ));
			}
			catch (MessageException ex)
			{
				ex.printStackTrace();
			}
		}

		if (msg.hasKey(IMessageField.FIELD_ACTION_ENABLED))
		{
			try
			{
				model.setUserActionEnabled(msg.get(IMessageField.FIELD_ACTION_ENABLED).equals("true"));
			}
			catch (MessageException ex)
			{
				ex.printStackTrace();
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private static void extractAsRunInformation( SPELLmessage msg, IExecutorInfo model )
	{
		if (msg.hasKey(IMessageField.FIELD_ASRUN_NAME))
		{
			try
			{
				String asrunName = msg.get(IMessageField.FIELD_ASRUN_NAME);
				int idx = asrunName.lastIndexOf("/");
				String aux = asrunName;
				if (idx != -1)
				{
					aux = asrunName.substring(idx+1);
				}
				idx = aux.indexOf("_");
				idx = aux.indexOf("_", idx+1);
				String timeId = aux.substring(0,idx);
				model.setAsRunName(asrunName);
				model.setTimeId(timeId);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private static void extractStageInformation( SPELLmessage msg, IExecutorInfo model )
	{
		if (msg.hasKey(IMessageField.FIELD_STAGE_ID))
		{
			String stageId = null;
			String stageTl = null;
			try
			{
				stageId = msg.get(IMessageField.FIELD_STAGE_ID);
				stageTl = msg.get(IMessageField.FIELD_STAGE_TL);
				model.setStage(stageId, stageTl);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private static void extractStackInformation( SPELLmessage msg, IExecutorInfo model )
	{
		if (msg.hasKey(IMessageField.FIELD_CSP))
		{
			try
			{
				String csp = msg.get(IMessageField.FIELD_CSP);
				String code = msg.get(IMessageField.FIELD_CODE_NAME);
				model.setStack(csp, code);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
}
