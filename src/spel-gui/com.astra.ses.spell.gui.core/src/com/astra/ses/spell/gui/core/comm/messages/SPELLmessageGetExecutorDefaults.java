///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.messages
// 
// FILE      : SPELLmessageGetExecutorDefaults.java
//
// DATE      : 2014-02-24 10:48
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
import com.astra.ses.spell.gui.core.model.server.ExecutorDefaults;
import com.astra.ses.spell.gui.core.model.types.BrowsableLibMode;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;

public class SPELLmessageGetExecutorDefaults extends SPELLmessageRequest
{
	public SPELLmessageGetExecutorDefaults(String ctxName)
	{
		super(IMessageId.REQ_GET_CTX_EXEC_DFLT);
		set(IMessageField.FIELD_CTX_NAME, ctxName);
		setSender(IMessageValue.CLIENT_SENDER);
		setReceiver(IMessageValue.LISTENER_RECEIVER);
	}

	/** When getting the values from the Executor */
	public static void fillDefaults(ExecutorDefaults defaults,
	        SPELLmessage response)
	{
		ExecutorDefaults execDef = new ExecutorDefaults();
		String value = null;
		
		// Getting values from response message
		try
		{
			value = response.get(IMessageField.FIELD_RUN_INTO);
			execDef.setRunInto( Boolean.parseBoolean(value) );
		}
		catch (MessageException ex)
		{
			Logger.warning( ex.getMessage(),  Level.COMM, "SPELLmessageGetExecutorDefaults.fillExecConfig" );
		}
		
		try
		{
			value = response.get(IMessageField.FIELD_EXEC_DELAY);
			execDef.setExecDelay( Integer.parseInt(value) );
		}
		catch (MessageException ex)
		{
			Logger.warning( ex.getMessage(),  Level.COMM, "SPELLmessageGetExecutorDefaults.fillExecConfig" );
		}
		
		try
		{
			value = response.get(IMessageField.FIELD_PROMPT_DELAY);
			execDef.setPromptWarningDelay(  Integer.parseInt(value) );
		}
		catch (MessageException ex)
		{
			Logger.warning( ex.getMessage(),  Level.COMM, "SPELLmessageGetExecutorDefaults.fillExecConfig" );
		}
		
		try
		{
			value = response.get(IMessageField.FIELD_BY_STEP);
			execDef.setByStep( Boolean.parseBoolean(value) );
		}
		catch (MessageException ex)
		{
			Logger.warning( ex.getMessage(),  Level.COMM, "SPELLmessageGetExecutorDefaults.fillExecConfig" );
		}
		
		try
		{
			value = response.get(IMessageField.FIELD_BROWSABLE_LIB);
			BrowsableLibMode mode = BrowsableLibMode.OFF;
			if (value != null)
			{
				if (value.trim().toUpperCase().equals("DISABLED"))
				{
					mode = BrowsableLibMode.DISABLED;
				}
				else
				try
				{
					// Backwards compatibility: check for boolean values true/false
					boolean bValue = Boolean.parseBoolean(value.toLowerCase().trim());
					if (bValue) mode = BrowsableLibMode.ON;
				}
				catch(Exception ex) {}
			}
			execDef.setBrowsableLib( mode );
		}
		catch (MessageException ex)
		{
			Logger.warning( ex.getMessage(),  Level.COMM, "SPELLmessageGetExecutorDefaults.fillExecConfig" );
		}
		
		try
		{
			value = response.get(IMessageField.FIELD_FORCE_TC_CONFIG);
			execDef.setForceTcConfirm( Boolean.parseBoolean(value) );
		}
		catch (MessageException ex)
		{
			Logger.warning( ex.getMessage(),  Level.COMM, "SPELLmessageGetExecutorDefaults.fillExecConfig" );
		}
		
		try
		{
			value = response.get(IMessageField.FIELD_WATCH_VARIABLES);
			execDef.setWatchVariables( Boolean.parseBoolean(value) );
		}
		catch (MessageException ex)
		{
			Logger.warning( ex.getMessage(),  Level.COMM, "SPELLmessageGetExecutorDefaults.fillExecConfig" );
		}

		try
		{
			value = response.get(IMessageField.FIELD_MAX_VERBOSITY);
			execDef.setMaxVerbosity(  Integer.parseInt(value) );
		}
		catch (MessageException ex)
		{
			Logger.warning( ex.getMessage(),  Level.COMM, "SPELLmessageGetExecutorDefaults.fillExecConfig" );
		}
		
		try
		{
			value = response.get(IMessageField.FIELD_SAVE_STATE_MODE);
			execDef.setSaveStateModeStr( value );
		}
		catch (MessageException ex)
		{
			Logger.warning( ex.getMessage(),  Level.COMM, "SPELLmessageGetExecutorDefaults.fillExecConfig" );
		}
		
		// return defaults object
		defaults.setRunInto(execDef.getRunInto());
		defaults.setExecDelay(execDef.getExecDelay());
		defaults.setPromptWarningDelay(execDef.getPromptWarningDelay());
		defaults.setByStep(execDef.getByStep());
		defaults.setBrowsableLib(execDef.getBrowsableLib());
		defaults.setForceTcConfirm(execDef.getForceTcConfirm());
		defaults.setMaxVerbosity(execDef.getMaxVerbosity());
		defaults.setWatchVariables(execDef.getWatchVariables());
		defaults.setSaveStateMode(execDef.getSaveStateMode());
	}
}
