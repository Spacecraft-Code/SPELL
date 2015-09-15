///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.commands
// 
// FILE      : ScheduleProcedure.java
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.astra.ses.spell.gui.core.interfaces.IServerProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.server.ServerInfo.ServerRole;
import com.astra.ses.spell.gui.dialogs.ConditionDialog;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.model.jobs.ScheduleProcedureJob;
import com.astra.ses.spell.gui.services.IRuntimeSettings;
import com.astra.ses.spell.gui.services.IRuntimeSettings.RuntimeProperty;

/**
 */
public class ScheduleProcedure extends AbstractHandler
{
	public static final String	ID	= "com.astra.ses.spell.gui.commands.ScheduleProcedure";

	/***************************************************************************
	 * The constructor.
	 **************************************************************************/
	public ScheduleProcedure()
	{
	}

	/***************************************************************************
	 * The command has been executed, so extract extract the needed information
	 * from the application context.
	 **************************************************************************/
	public CommandResult execute(ExecutionEvent event) throws ExecutionException
	{
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		IServerProxy proxy = (IServerProxy) ServiceManager.get(IServerProxy.class);
		ServerRole role = proxy.getCurrentServer().getRole();
		if (role != ServerRole.COMMANDING)
		{
			MessageDialog.openError(window.getShell(), "Cannot open",
			        "Cannot schedule procedures on the current server,\n" + "the role is monitoring");
			return CommandResult.NO_EFFECT;
		}

		IRuntimeSettings runtime = (IRuntimeSettings) ServiceManager.get(IRuntimeSettings.class);
		String procId = (String) runtime.getRuntimeProperty(RuntimeProperty.ID_NAVIGATION_VIEW_SELECTION);
		if (procId != null)
		{
			ConditionDialog dlg = new ConditionDialog(window.getShell());
			dlg.open();
			String condition = dlg.getCondition();
			if (condition != null)
			{
				ScheduleProcedureJob job = new ScheduleProcedureJob(procId, condition);
				CommandHelper.executeInProgress(job, true, true);
				if (job.result != CommandResult.SUCCESS)
				{
					MessageDialog.openError(window.getShell(), "Schedule error", job.message);
				}
				return job.result;
			}
			else
			{
				return CommandResult.NO_EFFECT;
			}
		}
		else
		{
			return CommandResult.NO_EFFECT;
		}
	}
}
