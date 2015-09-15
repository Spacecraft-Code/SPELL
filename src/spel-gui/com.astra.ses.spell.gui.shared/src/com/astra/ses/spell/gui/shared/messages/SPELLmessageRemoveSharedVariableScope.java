///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.shared.messages
// 
// FILE      : SPELLmessageRemoveSharedVariableScope.java
//
// DATE      : Oct 25, 2013
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
package com.astra.ses.spell.gui.shared.messages;

import com.astra.ses.spell.gui.core.comm.messages.MessageException;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageRequest;
import com.astra.ses.spell.gui.core.interfaces.IMessageField;
import com.astra.ses.spell.gui.core.interfaces.IMessageValue;


public class SPELLmessageRemoveSharedVariableScope extends SPELLmessageRequest
{
	/***************************************************************************
	 * 
	 **************************************************************************/
	public SPELLmessageRemoveSharedVariableScope(String key, String scope)
	{
		super(ISDMessageId.REQ_DEL_SHARED_DATA_SCOPE);
		setSender(IMessageValue.CLIENT_SENDER);
		setReceiver(IMessageValue.CONTEXT_RECEIVER);
		set(IMessageField.FIELD_GUI_KEY, key);
		set(ISDMessageField.FIELD_SHARED_SCOPE, scope);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public static boolean isSuccess( SPELLmessage response )
	{
		if (response.hasKey(ISDMessageField.FIELD_SHARED_SUCCESS))
		{
			try
            {
	            return response.get(ISDMessageField.FIELD_SHARED_SUCCESS).equals("True");
            }
            catch (MessageException e)
            {
	            e.printStackTrace();
            }
		}
		return false;
	}
}
