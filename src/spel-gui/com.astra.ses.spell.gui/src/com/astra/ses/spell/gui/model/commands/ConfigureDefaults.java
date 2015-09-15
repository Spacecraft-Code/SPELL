///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.commands
// 
// FILE      : ConfigureDefaults.java
//
// DATE      : 2014-02-24 11:17
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
package com.astra.ses.spell.gui.model.commands;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.IServerProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.server.ExecutorDefaultParams;
import com.astra.ses.spell.gui.core.model.server.ExecutorDefaults;
import com.astra.ses.spell.gui.core.model.server.ServerInfo.ServerRole;
import com.astra.ses.spell.gui.dialogs.DefaultsDialog;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ConfigureDefaults extends AbstractHandler
{
	public static final String ID = "com.astra.ses.spell.gui.commands.ConfigureDefaults";

	/***************************************************************************
	 * The constructor.
	 **************************************************************************/
	public ConfigureDefaults()
	{
	}

	/***************************************************************************
	 * The command has been executed, so extract extract the needed information
	 * from the application context.
	 **************************************************************************/
	public CommandResult execute(ExecutionEvent event) throws ExecutionException
	{
		// Variables declaration and initialization
		IServerProxy sProxy = null;
		IContextProxy cProxy = null;
		Shell shell = null;
		DefaultsDialog dialog = null;
		int dialogResult = 0;
		ExecutorDefaults dialogValues;
		
		/** Holds the Context Executor Defaults of the form */
		ExecutorDefaults executorDefaults = new ExecutorDefaults();
		/** Transfer the updated executor defaults */
		Map<String, String> updatedDefaults = new TreeMap<String, String>(); 

		// Get server Proxy
		sProxy = (IServerProxy) ServiceManager.get(IServerProxy.class);
		
		if(sProxy.isConnected() && sProxy.getCurrentServer().getRole() == ServerRole.COMMANDING )
		{
			//Get cProxy
			cProxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
			if( cProxy.isConnected() )
			{
				//Get shell
				shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				try 
				{
					//Get context defaults from the Context Server through the Context Proxy.
					cProxy.getExecutorDefaults(executorDefaults);
					
					//Prepare dialog
					dialog = new DefaultsDialog(shell, executorDefaults); 
					dialogResult = dialog.open();
					
					if (dialogResult == IDialogConstants.OK_ID)
					{
						//Get defaults values of the dialog
						dialogValues = dialog.getExecutorDefaults();
						
						//Only send updated values.
						if( dialogValues.getRunInto() != executorDefaults.getRunInto() )
							updatedDefaults.put(ExecutorDefaultParams.RUN_INTO.getParam(), dialogValues.getRunInto() ? "True" : "False");	
						if( dialogValues.getByStep() != executorDefaults.getByStep() )
							updatedDefaults.put(ExecutorDefaultParams.BY_STEP.getParam(), dialogValues.getByStep() ? "True" : "False");
						
						if (!dialogValues.getBrowsableLib().equals(executorDefaults.getBrowsableLib()))
						{
							switch(dialogValues.getBrowsableLib())
							{
							case DISABLED:
								updatedDefaults.put(ExecutorDefaultParams.BROWSABLE_LIB.getParam(), "Disabled");
								break;
							case OFF:
								updatedDefaults.put(ExecutorDefaultParams.BROWSABLE_LIB.getParam(), "False");
								break;
							case ON:
								updatedDefaults.put(ExecutorDefaultParams.BROWSABLE_LIB.getParam(), "True");
								break;
							}
						}

						if( dialogValues.getForceTcConfirm() != executorDefaults.getForceTcConfirm() )
							updatedDefaults.put(ExecutorDefaultParams.FORCE_TC_CONFIRM.getParam(), dialogValues.getForceTcConfirm() ? "True" : "False");
						if( dialogValues.getExecDelay() != executorDefaults.getExecDelay() )
							updatedDefaults.put(ExecutorDefaultParams.EXEC_DELAY.getParam(), Integer.toString(dialogValues.getExecDelay()));
						if( dialogValues.getPromptWarningDelay() != executorDefaults.getPromptWarningDelay() )
							updatedDefaults.put(ExecutorDefaultParams.PROMPT_DELAY.getParam(), Integer.toString(dialogValues.getPromptWarningDelay()));
						if( dialogValues.getMaxVerbosity() != executorDefaults.getMaxVerbosity() )
							updatedDefaults.put(ExecutorDefaultParams.MAX_VERBOSITY.getParam(), Integer.toString(dialogValues.getMaxVerbosity()));
						if( dialogValues.getWatchVariables() != executorDefaults.getWatchVariables() )
							updatedDefaults.put(ExecutorDefaultParams.WATCH_VARIABLES.getParam(), dialogValues.getWatchVariables() ? "True" : "False");
						if( dialogValues.getSaveStateMode() != executorDefaults.getSaveStateMode() )
							updatedDefaults.put(ExecutorDefaultParams.SAVE_STATE_MODE.getParam(), dialogValues.getSaveStateMode().getKey() );						

						//Send updated default fields to server
						cProxy.setExecutorDefaults(updatedDefaults);
					}
					
				}
				catch (Exception ex) 
				{
					MessageDialog.openError(shell, "Configure Defaults", "Failed to obtain exector defaults of the context.");
					return CommandResult.FAILED;
				}
			} //context connected
		} //Server proxy connected and Commanding

		return CommandResult.SUCCESS;
	} // execute

} //ConfigureDefaults
