///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.messages
// 
// FILE      : SPELLmessageGetInputFile.java
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
package com.astra.ses.spell.gui.core.comm.messages;

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.gui.core.interfaces.IMessageField;
import com.astra.ses.spell.gui.core.interfaces.IMessageId;
import com.astra.ses.spell.gui.core.interfaces.IMessageValue;
import com.astra.ses.spell.gui.core.model.server.TransferData;
import com.astra.ses.spell.gui.core.model.types.DataContainer;
import com.astra.ses.spell.gui.core.model.types.DataVariable;
import com.astra.ses.spell.gui.core.model.types.ValueFormat;
import com.astra.ses.spell.gui.core.model.types.ValueType;

public class SPELLmessageGetInputFile extends SPELLmessageRequest
{
	public SPELLmessageGetInputFile(String path)
	{
		super(IMessageId.REQ_INPUT_FILE);
		set(IMessageField.FIELD_FILE_PATH, path);
		setSender(IMessageValue.CLIENT_SENDER);
		setReceiver(IMessageValue.CONTEXT_RECEIVER);
	}

	public SPELLmessageGetInputFile(String path, int chunkNo)
	{
		super(IMessageId.REQ_INPUT_FILE);
		set(IMessageField.FIELD_FILE_PATH, path);
		set(IMessageField.FIELD_CHUNK, Integer.toString(chunkNo));
		setSender(IMessageValue.CLIENT_SENDER);
		setReceiver(IMessageValue.CONTEXT_RECEIVER);
	}

	public static TransferData getDataChunk(SPELLmessage response)
	{
		TransferData data = null;
		try
		{
			String value = response.get(IMessageField.FIELD_DICT_CONTENTS);
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
	
	public static void updateContainer( String valueList, DataContainer container, IProgressMonitor monitor )
	{
		String[] varData = valueList.split(IMessageField.VARIABLE_SEPARATOR);
		monitor.beginTask("Updating input file variables", varData.length);
		for( int index=0; index< varData.length; index++)
		{
			if (monitor.isCanceled())
			{
				return;
			}
			String variableStr = varData[index];
			String[] fields = variableStr.split(IMessageField.VARIABLE_PROPERTY_SEPARATOR);
			monitor.subTask("Variable: " + fields[0]);
			monitor.worked(1);
			
			String varname = fields[0];
			String value = "";
			if (fields.length == 2)
			{
				value = fields[1];
			}
			
			// Infer the format when reading from server input files
			ValueType type = DataVariable.inferTypeFromValue(value);
			ValueFormat format = ValueFormat.NONE;
			if (type.equals(ValueType.LONG))
			{
				if (value.startsWith("0x"))
				{
					format = ValueFormat.HEX;
				}
				else if (value.startsWith("0b"))
				{
					format = ValueFormat.BIN;
				}
				else if (value.startsWith("0"))
				{
					format = ValueFormat.OCT;
				}
				else 
				{
					format = ValueFormat.DEC;
				}
			}

			DataVariable var = new DataVariable(varname, value,null, null, format, type, false);
			container.addVariable(varname, var);
		}
	}
}
