///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.replay.commands
// 
// FILE      : OpenProcedureReplay.java
//
// DATE      : Jun 19, 2013
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
package com.astra.ses.spell.gui.replay.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.replay.views.ReplayProcedureView;
import com.astra.ses.spell.gui.services.IViewManager;

/**
 */
public class OpenProcedureReplay extends AbstractHandler
{
	public static final String ID = "com.astra.ses.spell.gui.commands.OpenProcedureReplay";

	/** Proc id command argument */
	public static final String ARG_ASRUN = "com.astra.ses.spell.gui.commands.OpenProcedureReplay.id";

	/***************************************************************************
	 * The constructor.
	 **************************************************************************/
	public OpenProcedureReplay()
	{
	}

	/***************************************************************************
	 * The command has been executed, so extract extract the needed information
	 * from the application context.
	 **************************************************************************/
	public CommandResult execute(ExecutionEvent event) throws ExecutionException
	{
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		String asrunPath = event.getParameter(ARG_ASRUN);

		if (asrunPath != null)
		{
			int idx = asrunPath.indexOf("_Executor_") + "_Executor_".length();
			// Remove path, timestamp and file extension
			String instanceId = asrunPath.substring(idx);
			instanceId = instanceId.replaceAll("__", "/");

			// Remove .ASRUN
			idx = instanceId.lastIndexOf(".");
			instanceId = instanceId.substring(0,idx) + "R";

			IViewManager vmgr = (IViewManager) ServiceManager.get(IViewManager.class);
			if (!vmgr.containsProcedureView(instanceId))
			{
				OpenReplayProcedureJob job = new OpenReplayProcedureJob(instanceId, asrunPath);
				CommandHelper.executeInProgress(job, true, true);
				if (job.result != CommandResult.SUCCESS)
				{
					MessageDialog.openError(window.getShell(), "Replay execution error", job.message);
				}
				else
				{
					try
	                {
		                window.getActivePage().showView(ReplayProcedureView.ID, instanceId, IWorkbenchPage.VIEW_ACTIVATE);
	                }
	                catch (PartInitException e)
	                {
		                e.printStackTrace();
		    			return CommandResult.FAILED;
	                }
				}
				return job.result;
			}
			else
			{
				try
                {
	                window.getActivePage().showView(ReplayProcedureView.ID, instanceId, IWorkbenchPage.VIEW_ACTIVATE);
                }
                catch (PartInitException e)
                {
	                e.printStackTrace();
	    			return CommandResult.FAILED;
                }
				return CommandResult.SUCCESS;
			}
		}
		else
		{
			return CommandResult.NO_EFFECT;
		}
	}
}
