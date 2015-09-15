///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.jobs
// 
// FILE      : ConnectServerJob.java
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
package com.astra.ses.spell.gui.model.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.astra.ses.spell.gui.core.exceptions.ServerError;
import com.astra.ses.spell.gui.core.interfaces.IServerProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.dialogs.StringDialog;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.services.IRuntimeSettings;
import com.astra.ses.spell.gui.services.IRuntimeSettings.RuntimeProperty;

public class ConnectServerJob implements IRunnableWithProgress
{
	protected IWorkbenchWindow	m_window;
	public CommandResult	   result;

	public ConnectServerJob()
	{
		m_window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		result = CommandResult.FAILED;
	}

	public void run(IProgressMonitor monitor)
	{
		IServerProxy proxy = (IServerProxy) ServiceManager.get(IServerProxy.class);
		IRuntimeSettings runtime = (IRuntimeSettings) ServiceManager.get(IRuntimeSettings.class);
		if (proxy.isConnected())
		{
			MessageDialog.openWarning(m_window.getShell(), "Connect to server", "Already connected to server");
			result = CommandResult.NO_EFFECT;
		}
		else
		{
			ServerInfo info = (ServerInfo) runtime.getRuntimeProperty(RuntimeProperty.ID_SERVER_SELECTION);
			boolean promptPassword = false;
			try
			{
				if (info != null)
				{
					monitor.setTaskName("Connecting to server");
					if (info.getAuthentication() != null && info.getAuthentication().getUsername() != null && !info.getAuthentication().getUsername().trim().isEmpty())
					{
						String pwd = info.getAuthentication().getPassword();
						String key = info.getAuthentication().getKeyFile();
						if ( ((pwd == null) || pwd.trim().isEmpty()) && (key == null || key.trim().isEmpty()))
						{
							promptPassword = true;
							// Ask the user for password
							StringDialog dialog = new StringDialog(m_window.getShell(), "Connection to "
							        + info.getName(), "Secure access", "Access to this server requires authentication and there is no user key available\n"
							        + "please enter password for user '" + info.getAuthentication().getUsername() + "'", true);
							int dresult = dialog.open();
							if (dresult == StringDialog.CANCEL)
							{
								MessageDialog.openError(m_window.getShell(), "Connect to server",
								        "Cannot connect to server without password");
								result = CommandResult.FAILED;
								monitor.done();
								return;
							}
							String password = dialog.getAnswer();
							info.getAuthentication().setPassword(password);
						}
					}
					proxy.changeServer(info);
					proxy.connect();
					result = CommandResult.SUCCESS;
				}
				else
				{
					MessageDialog.openWarning(m_window.getShell(), "Connect to server", "No server selected");
					result = CommandResult.NO_EFFECT;
				}
			}
			catch (ServerError err)
			{
				MessageDialog.openError(m_window.getShell(), "Connect to server", "Cannot connect to server:\n\n" + err.getLocalizedMessage());
				if (promptPassword) info.getAuthentication().setPassword(null);
				result = CommandResult.FAILED;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				result = CommandResult.FAILED;
			}
		}
		monitor.done();
	}
}
