///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.messages
// 
// FILE      : SPELLmessageCtxInfo.java
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

import com.astra.ses.spell.gui.core.interfaces.IMessageField;
import com.astra.ses.spell.gui.core.interfaces.IMessageId;
import com.astra.ses.spell.gui.core.interfaces.IMessageValue;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.types.ContextStatus;

public class SPELLmessageCtxInfo extends SPELLmessageRequest
{
	public SPELLmessageCtxInfo(String ctxName)
	{
		super(IMessageId.REQ_CTX_INFO);
		set(IMessageField.FIELD_CTX_NAME, ctxName);
		setSender(IMessageValue.CLIENT_SENDER);
		setReceiver(IMessageValue.LISTENER_RECEIVER);
	}

	public static void fillCtxInfo(ContextInfo model, SPELLmessage response)
	{
		String driver = "UKNKNOWN";
		String family = "UNKNOWN";
		String port = "0";
		String desc = "";
		String gcs = "???";
		String sc = "???";
		String status = "UNKNOWN";
		String maxproc = "20";
		try
		{
			status = response.get(IMessageField.FIELD_CTX_STATUS);
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		try
		{
			port = response.get(IMessageField.FIELD_CTX_PORT);
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		try
		{
			driver = response.get(IMessageField.FIELD_CTX_DRV);
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		try
		{
			desc = response.get(IMessageField.FIELD_CTX_DESC);
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		try
		{
			sc = response.get(IMessageField.FIELD_CTX_SC);
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		try
		{
			gcs = response.get(IMessageField.FIELD_CTX_GCS);
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		try
		{
			family = response.get(IMessageField.FIELD_CTX_FAM);
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		try
		{
			maxproc = response.get(IMessageField.FIELD_CTX_MAXPROC);
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		model.setFamily(family);
		model.setDriver(driver);
		model.setDescription(desc);
		model.setGCS(gcs);
		model.setSC(sc);
		model.setStatus(ContextStatus.valueOf(status));
		model.setPort( Integer.valueOf(port) );
		model.setMaxProc(maxproc);
	}
}
