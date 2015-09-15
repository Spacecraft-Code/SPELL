///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.watchvariables.messages
// 
// FILE      : SPELLmessageGetVariables.java
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

import java.util.ArrayList;

import com.astra.ses.spell.gui.core.comm.messages.MessageException;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageRequest;
import com.astra.ses.spell.gui.core.interfaces.IMessageField;
import com.astra.ses.spell.gui.core.interfaces.IMessageValue;
import com.astra.ses.spell.gui.core.model.server.TransferData;
import com.astra.ses.spell.gui.watchvariables.notification.VariableData;

public class SPELLmessageGetVariables extends SPELLmessageRequest
{
	/**************************************************************************
	 * 
	 *************************************************************************/
	public SPELLmessageGetVariables(String procId)
	{
		this(procId, -1);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public SPELLmessageGetVariables(String procId, int chunkNo)
	{
		super(IWVMessageId.REQ_GET_VARIABLES);
		set(IMessageField.FIELD_PROC_ID, procId);
		setSender(IMessageValue.CLIENT_SENDER);
		if (chunkNo >= 0)
		{
			set(IMessageField.FIELD_CHUNK, Integer.toString(chunkNo));
		}
		setReceiver(procId);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public static TransferData getValueChunk(SPELLmessage response)
	{
		TransferData data = null;
		try
		{
			String value = response.get(IWVMessageField.FIELD_VARIABLE_VALUE);
			if (response.hasKey(IMessageField.FIELD_CHUNK))
			{
				int chunkNo = Integer.parseInt(response.get(IMessageField.FIELD_CHUNK));
				int totalChunks = Integer.parseInt(response.get(IMessageField.FIELD_TOTAL_CHUNKS));
				data = new TransferData(value, chunkNo, totalChunks);
			}
			else
			{
				data = new TransferData(value, 0, 0);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return data;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public static String[] getValues( String valueList)
	{
		return valueList.split(IMessageField.VARIABLE_SEPARATOR);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public static VariableData[] getVariables(SPELLmessage response)
	{
		ArrayList<VariableData> result = new ArrayList<VariableData>();
		try
		{
			String nameList = response.get(IWVMessageField.FIELD_VARIABLE_NAME);
			String typeList = response.get(IWVMessageField.FIELD_VARIABLE_TYPE);
			String globalList = response.get(IWVMessageField.FIELD_VARIABLE_GLOBAL);

			if (nameList.trim().isEmpty()) return null;
			
			String[] names = nameList.split(IMessageField.VARIABLE_SEPARATOR);
			String[] types = typeList.split(IMessageField.VARIABLE_SEPARATOR);
			String[] globals = globalList.split(IMessageField.VARIABLE_SEPARATOR);

			VariableData var = null;
			for (int index = 0; index < names.length; index++)
			{
				boolean global = globals[index].equals("True");
				var = new VariableData(names[index], types[index], null, global);
				result.add(var);
			}
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		if (result.size() == 0)
			return null;
		return result.toArray(new VariableData[0]);
	}
}
