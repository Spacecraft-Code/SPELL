///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.shell.commands
// 
// FILE      : Info.java
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
package com.astra.ses.spell.gui.core.shell.commands;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.astra.ses.spell.gui.core.exceptions.CommandFailed;
import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.IExecutorInfo;
import com.astra.ses.spell.gui.core.interfaces.IServerProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.types.ProcProperties;
import com.astra.ses.spell.gui.core.shell.services.ShellManager;

/*******************************************************************************
 * Command to show detailed information about a given item
 ******************************************************************************/
public class Info extends ShellCommand
{
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	Info(ShellManager mgr)
	{
		super(mgr);
	}

	/***************************************************************************
	 * Execute the command
	 **************************************************************************/
	@Override
	public void execute(Vector<String> args) throws CommandFailed
	{
		if (args == null || args.size() < 1) { throw new CommandFailed("Expected: item to show information about"); }
		try
		{
			m_args = args;
			String toShow = args.get(0).toLowerCase();
			if (toShow.equals("server"))
			{
				infoServer();
			}
			else if (toShow.equals("context"))
			{
				infoContext();
			}
			else if (toShow.equals("procedure"))
			{
				infoProcedure();
			}
			else if (toShow.equals("executor"))
			{
				infoExecutor();
			}
			else
			{
				throw new CommandFailed("Unknown option: " + toShow);
			}
		}
		catch (CommandFailed ex)
		{
			throw ex;
		}
		catch (Exception ex)
		{
			throw new CommandFailed(ex.getLocalizedMessage());
		}
	}

	/***************************************************************************
	 * Get command string
	 **************************************************************************/
	@Override
	public String getCmdString()
	{
		return "info";
	}

	/***************************************************************************
	 * Get command help
	 **************************************************************************/
	@Override
	public String getHelp()
	{
		return "show detailed information";
	}

	/***************************************************************************
	 * Get command syntax
	 **************************************************************************/
	@Override
	public String getSyntax()
	{
		return "info [server|context <ctx name>|procedure <id>|executor <id>]";
	}

	// =========================================================================
	// # NON-ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Show information about a given context
	 **************************************************************************/
	protected void infoContext()
	{
		if (m_args.size() != 2) { throw new CommandFailed("No context name given"); }
		IServerProxy proxy = (IServerProxy) ServiceManager.get(IServerProxy.class);
		if (!proxy.isConnected()) { throw new CommandFailed("Not connected to a server"); }
		String contextName = m_args.get(1);
		Vector<String> ctxs = proxy.getAvailableContexts();
		if (!ctxs.contains(contextName)) { throw new CommandFailed("No such context: " + contextName); }
		ContextInfo info = proxy.getContextInfo(contextName);
		display("Context " + contextName + " information:");
		display("     - Description    : " + info.getDescription());
		display("     - Spacecraft     : " + info.getSC());
		display("     - Status         : " + info.getStatus());
		display("     - Driver         : " + info.getDriver());
		display("     - Family         : " + info.getFamily());
		display("     - Host name      : " + info.getGCS());
		display("     - Max. procedures: " + info.getMaxProc());
	}

	/***************************************************************************
	 * Show information about the current server
	 **************************************************************************/
	protected void infoServer()
	{
		IServerProxy proxy = (IServerProxy) ServiceManager.get(IServerProxy.class);
		if (!proxy.isConnected()) { throw new CommandFailed("Not connected to a server"); }
		ServerInfo info = proxy.getCurrentServer();
		display("Server " + info.getName() + " information:");
		display("     - Hostname :" + info.getHost());
		display("     - Port     :" + info.getPort());
	}

	/***************************************************************************
	 * Show information about a given procedure
	 **************************************************************************/
	protected void infoProcedure()
	{
		if (m_args.size() != 2) { throw new CommandFailed("No procedure identifier given"); }
		IContextProxy cproxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		if (!cproxy.isConnected()) { throw new CommandFailed("Not connected to a server"); }
		Map<String, String> procs = cproxy.getAvailableProcedures(true);
		String procId = m_args.get(1);
		if (!procs.containsKey(procId)) { throw new CommandFailed("No such procedure: " + procId); }
		Map<ProcProperties, String> props = cproxy.getProcedureProperties(procId);
		display("Procedure " + procId + " information:");
		for (ProcProperties prop : props.keySet())
		{
			display("     - " + prop.tag + ": " + props.get(prop));
		}
	}

	/***************************************************************************
	 * Show information about a given executor
	 **************************************************************************/
	protected void infoExecutor() throws Exception
	{
		if (m_args.size() != 2) { throw new CommandFailed("No procedure identifier given"); }
		IContextProxy cproxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		if (!cproxy.isConnected()) { throw new CommandFailed("Not connected to a server"); }
		List<String> execs = cproxy.getAvailableExecutors();
		String procId = m_args.get(1);
		if (!execs.contains(procId)) { throw new CommandFailed("No such executor: " + procId); }
		IExecutorInfo info = cproxy.getExecutorInfo(procId);
		display("Executor " + procId + " information:");
		display("     - Status       : " + info.getStatus());
		display("     - Mode         : " + info.getMode());
		display("     - Controlled by: " + info.getControllingClient());
		display("     - Monitored by : <todo>");
		display("     - Condition    : " + info.getCondition());
	}
}
