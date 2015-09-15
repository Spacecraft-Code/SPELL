///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.commands
// 
// FILE      : ConfigureExecution.java
//
// DATE      : 2008-11-21 08:55
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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.dialogs.ConfigDialog;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.services.IRuntimeSettings;
import com.astra.ses.spell.gui.services.IRuntimeSettings.RuntimeProperty;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ConfigureExecution extends AbstractHandler
{
	public static final String ID = "com.astra.ses.spell.gui.commands.ConfigureExecution";

	/***************************************************************************
	 * The constructor.
	 **************************************************************************/
	public ConfigureExecution()
	{
	}

	/***************************************************************************
	 * The command has been executed, so extract extract the needed information
	 * from the application context.
	 **************************************************************************/
	public CommandResult execute(ExecutionEvent event) throws ExecutionException
	{
		IRuntimeSettings runtime = (IRuntimeSettings) ServiceManager.get(IRuntimeSettings.class);
		String procId = (String) runtime.getRuntimeProperty(RuntimeProperty.ID_PROCEDURE_SELECTION);
		IProcedureManager mgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
		IProcedure proc = mgr.getProcedure(procId);

		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		try
		{
			proc.getController().updateConfig();
		}
		catch(Exception ex)
		{
			MessageDialog.openError(shell, "Configure Execution", "Failed to obtain current procedure configuration");
			return CommandResult.FAILED;
		}
		
		ConfigDialog dialog = new ConfigDialog(shell, proc);
		int result = dialog.open();

		if (result == IDialogConstants.OK_ID)
		{
			boolean runIntoOrig = proc.getExecutionManager().isRunInto();
			boolean byStepOrig = proc.getRuntimeInformation().isStepByStep();
			boolean tcConfirmOrig = proc.getRuntimeInformation().isForceTcConfirmation();
			int delayOrig = proc.getRuntimeInformation().getExecutionDelay();
			int promptDelayOrig = proc.getRuntimeInformation().getPromptWarningDelay();

			if (runIntoOrig != dialog.getRunInto())
			{
				proc.getController().setRunInto(dialog.getRunInto());
			}
			if (byStepOrig != dialog.getByStep())
			{
				proc.getController().setStepByStep(dialog.getByStep());
			}
			if (delayOrig != dialog.getExecutionDelay())
			{
				proc.getController().setExecutionDelay(dialog.getExecutionDelay());
			}
			if (promptDelayOrig != dialog.getPromptWarningDelay())
			{
				proc.getController().setPromptWarningDelay(dialog.getPromptWarningDelay());
			}
			if (tcConfirmOrig != dialog.getTcConfirm())
			{
				proc.getController().setForceTcConfirmation(dialog.getTcConfirm());
			}
		}

		return CommandResult.SUCCESS;
	}
}
