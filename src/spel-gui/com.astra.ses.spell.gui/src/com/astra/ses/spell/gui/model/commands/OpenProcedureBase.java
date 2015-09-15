///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.commands
// 
// FILE      : OpenProcedureBase.java
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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.IExecutorInfo;
import com.astra.ses.spell.gui.core.interfaces.IServerProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.server.ServerInfo.ServerRole;
import com.astra.ses.spell.gui.services.IRuntimeSettings;
import com.astra.ses.spell.gui.services.IRuntimeSettings.RuntimeProperty;
import com.astra.ses.spell.gui.types.ExecutorStatus;

/**
 */
public abstract class OpenProcedureBase extends AbstractHandler
{
	private boolean checkBackgroundProcedures()
	{
		try
        {
			IContextProxy cproxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
			List<String> executorIds = cproxy.getAvailableExecutors();
			if (!executorIds.isEmpty())
			{
				for( String procId : executorIds )
				{
	                IExecutorInfo info = cproxy.getExecutorInfo(procId);
	                if (info.isBackground() && !info.getStatus().equals(ExecutorStatus.FINISHED))
	                {
	                	return true;
	                }
				}
			}
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
		return false;
	}
	
	/***************************************************************************
	 * Check preconditions
	 **************************************************************************/
	private boolean preconditions( final IWorkbenchWindow window )
	{
		final AtomicBoolean confirm = new AtomicBoolean(true);
		if (checkBackgroundProcedures())
		{
			Display.getDefault().syncExec( new Runnable()
			{
				public void run()
				{
					confirm.set(MessageDialog.openConfirm( window.getShell(), "Background procedures", 
							"There are procedures running in background in the current context.\n\n" +
					        "If you open a procedure now it could interfere with the operations being performed" +
					        "by the background procedures.\n\n" +
							"Do you want to continue anyway?."));
				}
			});
		}
		return confirm.get();
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
			MessageDialog.openError(window.getShell(), "Cannot open", "Cannot open procedures on the current server,\n"
			        + "the role of this client is 'monitoring'");
			return CommandResult.NO_EFFECT;
		}

		if (preconditions(window))
		{
			IRuntimeSettings runtime = (IRuntimeSettings) ServiceManager.get(IRuntimeSettings.class);
			String procId = (String) runtime.getRuntimeProperty(RuntimeProperty.ID_NAVIGATION_VIEW_SELECTION);
			if (procId != null)
			{
				return doPerformOpen(window, procId);
			}
		}
		return CommandResult.NO_EFFECT;
	}
	
	protected abstract CommandResult doPerformOpen( IWorkbenchWindow window, String procId );
}
