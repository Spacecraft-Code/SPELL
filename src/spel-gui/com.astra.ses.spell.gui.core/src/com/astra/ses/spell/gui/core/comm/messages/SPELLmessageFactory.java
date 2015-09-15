///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.messages
// 
// FILE      : SPELLmessageFactory.java
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

import java.util.Arrays;
import java.util.TreeMap;

import com.astra.ses.spell.gui.core.interfaces.IMessageField;
import com.astra.ses.spell.gui.core.interfaces.IMessageId;
import com.astra.ses.spell.gui.core.interfaces.IMessageType;

public class SPELLmessageFactory
{
	public static SPELLmessage createMessage( byte[] data )
	{
		SPELLmessage msg = null;
		try
		{
			byte[] toProcess = Arrays.copyOfRange(data, 1, data.length);
			boolean compressed = (data[0] == '\2');
			
		    if (compressed)
		    {
//		    	SPELLmessage.dumpData(toProcess);
		    	toProcess = SPELLmessage.uncompress(toProcess);
//		    	SPELLmessage.dumpData(toProcess);
		    }
			TreeMap<String, String> tags = SPELLmessage.fromData( toProcess );
			String msgTypeStr = tags.get("root");
			if (msgTypeStr.equals(IMessageType.MSG_TYPE_WRITE))
			{
				msg = new SPELLmessageDisplay(tags);
			}
			else if (msgTypeStr.equals(IMessageType.MSG_TYPE_PING))
			{
				return null;
			}
			else if (msgTypeStr.equals(IMessageType.MSG_TYPE_NOTIFY_ASYNC))
			{
				msg = new SPELLmessageNotifyAsync(tags);
			}
			else if (msgTypeStr.equals(IMessageType.MSG_TYPE_ONEWAY))
			{
				String fieldId = tags.get(IMessageField.FIELD_ID);
				if (fieldId.equals(IMessageId.MSG_EXEC_OP))
				{
					msg = new SPELLmessageExecOperation(tags);
				}
				else if (fieldId.equals(IMessageId.MSG_CLIENT_OP))
				{
					msg = new SPELLmessageClientOperation(tags);
				}
				else if (fieldId.equals(IMessageId.MSG_EXEC_CONFIG))
				{
					msg = new SPELLmessageExecConfigChanged(tags);
				}
				else if (fieldId.equals(IMessageId.MSG_CONTEXT_OP))
				{
					msg = new SPELLmessageCtxOperation(tags);
				}
				// This is the prompt sent in form of oneway message, for
				// monitoring GUIs
				else if (fieldId.equals(IMessageId.MSG_PROMPT_START))
				{
					msg = new SPELLmessagePromptStart(tags);
				}
				// This is the prompt sent in form of oneway message, for
				// monitoring GUIs
				else if (fieldId.equals(IMessageId.MSG_PROMPT_END))
				{
					msg = new SPELLmessagePromptEnd(tags);
				}
				else if (fieldId.equals(IMessageId.MSG_SETUACTION))
				{
					msg = new SPELLmessageSetUserAction(tags);
				}
				else if (fieldId.equals(IMessageId.MSG_ENABLEUACTION))
				{
					msg = new SPELLmessageEnableUserAction(tags);
				}
				else if (fieldId.equals(IMessageId.MSG_DISABLEUACTION))
				{
					msg = new SPELLmessageDisableUserAction(tags);
				}
				else if (fieldId.equals(IMessageId.MSG_DISMISSUACTION))
				{
					msg = new SPELLmessageDismissUserAction(tags);
				}
				else
				{
					msg = new SPELLmessageOneway(tags);
				}
			}
			else if (msgTypeStr.equals(IMessageType.MSG_TYPE_EOC))
			{
				msg = new SPELLmessageEOC();
			}
			else if (msgTypeStr.equals(IMessageType.MSG_TYPE_ERROR))
			{
				msg = new SPELLmessageError(tags);
				if (msg.getId().equals(IMessageId.MSG_LISTENER_LOST))
				{
					msg = new SPELLlistenerLost(tags);
				}
				else if (msg.getId().equals(IMessageId.MSG_CONTEXT_LOST))
				{
					msg = new SPELLcontextLost(tags);
				}
			}
			else if (msgTypeStr.equals(IMessageType.MSG_TYPE_NOTIFY))
			{
				msg = new SPELLmessageNotify(tags);
			}
			else if (msgTypeStr.equals(IMessageType.MSG_TYPE_PROMPT))
			{
				msg = new SPELLmessagePrompt(tags);
			}
			else if (msgTypeStr.equals(IMessageType.MSG_TYPE_REQUEST))
			{
				msg = new SPELLmessageRequest(tags);
			}
			else if (msgTypeStr.equals(IMessageType.MSG_TYPE_RESPONSE))
			{
				msg = new SPELLmessageResponse(tags);
			}
			else
			{
				msg = new SPELLmessage(tags);
			}
		}
		catch (Exception e)
		{
			System.err.println("ERROR processing message");
			//SPELLmessage.dumpData(data);
			e.printStackTrace();
		}
    	//System.out.println("CREATED: " + msg.getId());
		return msg;
	}
}
