///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.shell.commands
// 
// FILE      : Show.java
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.astra.ses.spell.gui.core.exceptions.CommandFailed;
import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.IServerProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.shell.services.ShellManager;

/*******************************************************************************
 * Command to show information about items
 ******************************************************************************/
public class Show extends ShellCommand
{
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	Show(ShellManager mgr)
	{
		super(mgr);
	}

	/***************************************************************************
	 * Execute the command
	 **************************************************************************/
	@Override
	public void execute(Vector<String> args) throws CommandFailed
	{
		if (args == null || args.size() < 1) { throw new CommandFailed("Expected: item to show"); }
		try
		{
			m_args = args;
			String toShow = args.get(0).toLowerCase();
			if (toShow.equals("contexts"))
			{
				showContexts();
			}
			else if (toShow.equals("procedures"))
			{
				showProcedures();
			}
			else if (toShow.equals("executors"))
			{
				showExecutors();
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
		return "show";
	}

	/***************************************************************************
	 * Get command help
	 **************************************************************************/
	@Override
	public String getHelp()
	{
		return "show information";
	}

	/***************************************************************************
	 * Get command syntax
	 **************************************************************************/
	@Override
	public String getSyntax()
	{
		return "show [contexts|procedures|executors]";
	}

	// =========================================================================
	// # NON-ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Show the available contexts
	 **************************************************************************/
	protected void showContexts()
	{
		IServerProxy proxy = (IServerProxy) ServiceManager.get(IServerProxy.class);
		if (!proxy.isConnected()) { throw new CommandFailed("Not connected to a server"); }
		Vector<String> ctxs = proxy.getAvailableContexts();
		if (ctxs.size() == 0)
		{
			display("No contexts available");
		}
		else
		{
			display("Available contexts: ");
			for (String ctx : ctxs)
			{
				ContextInfo info = proxy.getContextInfo(ctx);
				display("    - " + ctx + ": " + info.getStatus());
			}
		}
	}

	/***************************************************************************
	 * Show available procedures
	 **************************************************************************/
	protected void showProcedures()
	{
		IContextProxy cproxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		Map<String, String> procs = cproxy.getAvailableProcedures(true);
		if (procs.size() == 0)
		{
			display("No procedures available");
		}
		else
		{
			ArrayList<String> ids = new ArrayList<String>();
			ids.addAll(procs.keySet());
			Collections.sort(ids);
			display("Available procedures:");
			for (String procId : ids)
			{
				display("    - " + procs.get(procId) + " ID='" + procId + "'");
			}
		}

	}

	/***************************************************************************
	 * Show running executors
	 **************************************************************************/
	protected void showExecutors()
	{
		IContextProxy cproxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		List<String> execs = cproxy.getAvailableExecutors();
		if (execs.size() == 0)
		{
			display("No procedures running");
		}
		else
		{
			ArrayList<String> ids = new ArrayList<String>();
			ids.addAll(execs);
			Collections.sort(ids);
			display("Running procedures:");
			for (String procId : ids)
			{
				display("    - '" + procId + "'");
			}
		}
	}
}
