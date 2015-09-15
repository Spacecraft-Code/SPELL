///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.messages
// 
// FILE      : SPELLmessageExecConfigChanged.java
//
// DATE      : Jan 12, 2012
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

import java.util.TreeMap;

import com.astra.ses.spell.gui.core.interfaces.IMessageField;
import com.astra.ses.spell.gui.core.model.server.ExecutorConfig;

public class SPELLmessageExecConfigChanged extends SPELLmessageOneway
{
	public SPELLmessageExecConfigChanged(TreeMap<String, String> data)
	{
		super(data);
	}
	
	public String getProcId()
	{
		try
		{
			return get(IMessageField.FIELD_PROC_ID);
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

	public void fillExecConfig(ExecutorConfig config)
	{
		String value = null;
		boolean runInto = false;
		boolean byStep = false;
		boolean showLib = false;
		int delay = 0;
		int promptWarningDelay = 0;
		
		try
		{
			value = get(IMessageField.FIELD_RUN_INTO);
			runInto = Boolean.parseBoolean(value);
			config.setRunInto(runInto);
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		try
		{
			value = get(IMessageField.FIELD_EXEC_DELAY);
			delay = Integer.parseInt(value);
			config.setExecDelay(delay);
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		;
		try
		{
			value = get(IMessageField.FIELD_PROMPT_DELAY);
			promptWarningDelay = Integer.parseInt(value);
			config.setPromptWarningDelay(promptWarningDelay);
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		;		
		try
		{
			value = get(IMessageField.FIELD_BY_STEP);
			byStep = Boolean.parseBoolean(value);
			config.setStepByStep(byStep);
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		try
		{
			value = get(IMessageField.FIELD_BROWSABLE_LIB);
			showLib = Boolean.parseBoolean(value);
			config.setBrowsableLib(showLib);
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
	}
}
