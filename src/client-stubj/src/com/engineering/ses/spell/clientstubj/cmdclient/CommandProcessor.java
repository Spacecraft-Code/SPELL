///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.engineering.ses.spell.clientstubj.cmdclient
// 
// FILE      : CommandProcessor.java
//
// DATE      : Aug 29, 2015
//
// Copyright (C) 2008, 2011 SES ENGINEERING, Luxembourg S.A.R.L.
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
package com.engineering.ses.spell.clientstubj.cmdclient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.astra.ses.spell.gui.core.interfaces.IProcedureClient;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ExecutorInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo.ServerRole;
import com.astra.ses.spell.gui.core.model.types.ProcProperties;

public class CommandProcessor
{
	/** Reference to listener interface */
	private ListenerInterface m_listener;
	/** Reference to context interface */
	private ContextInterface m_context;
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public CommandProcessor( ListenerInterface listener, ContextInterface context )
	{
		m_listener = listener;
		m_context = context;
	}
	
	/***************************************************************************
	 * Processing of commands 
	 **************************************************************************/
	void processCommand( String command )
	{
		String[] items = command.split(" ");
		String main = items[0];
		main = main.toLowerCase();
		if (main.equals("list") || main.startsWith("li") || main.startsWith("ls"))
		{
			processListCommands(items);
		}
		else if (main.equals("attach") || main.startsWith("at") )
		{
			processAttachCommands(items);
		}
		else if (main.equals("detach") || main.startsWith("de") )
		{
			processDetachCommands(items);
		}
		else if (main.equals("connect") || main.startsWith("co") )
		{
			processConnectCommands(items);
		}
		else if (main.equals("disconnect") || main.startsWith("di") )
		{
			processDisconnectCommands(items);
		}
		else if (main.equals("info") || main.startsWith("inf") || main.startsWith("if"))
		{
			processInfoCommands(items);
		}
		else if (main.equals("start") || main.startsWith("sta"))
		{
			processStartCommands(items);
		}
		else if (main.equals("stop") || main.startsWith("sto"))
		{
			processStopCommands(items);
		}
		else if (main.equals("kill") || main.startsWith("k"))
		{
			processKillCommands(items);
		}
		else if (main.equals("help") || main.startsWith("h"))
		{
			processHelpCommands(items);
		}
		else if (main.equals("exit") || main.startsWith("q"))
		{
			throw new RuntimeException("exit");
		}
		else 
		{
			throw new RuntimeException("Unknown command: '" + items[0] + "'");
		}
	}
	
	/***************************************************************************
	 * Processing of LIST commands 
	 **************************************************************************/
	private void processListCommands( String[] items )
	{
		if (items.length == 2)
		{
			String item = items[1];
			item = item.toLowerCase();
			if (item.equals("procedures") || item.startsWith("p") )
			{
				Map<String,String> procs = m_context.getAvailableProcedures();
				for(String procId : procs.keySet())
				{
					System.out.println("  - " + procs.get(procId) + " (" + procId + ")");
				}
			}
			else if (item.equals("executors") || item.startsWith("e"))
			{
				List<String> executors = m_context.getAvailableExecutors();
				for(String exec : executors)
				{
					System.out.println("  - " + exec);
				}
			}
			else if (item.equals("contexts") || item.startsWith("c"))
			{
				List<String> contexts = m_listener.getAvailableContexts();
				for(String ctx : contexts)
				{
					System.out.println("  - " + ctx);
				}
			}
			else
			{
				throw new RuntimeException("Unknown element: '" + item + "' (syntax: list {procedures|executors|contexts})");
			}
		}
		else
		{
			throw new RuntimeException("Incorrect number of arguments (syntax: list {procedures|executors|contexts}) (given " + items.length + ": '" + Arrays.toString(items) + "')");
		}
	}
	
	/***************************************************************************
	 * Processing of START commands 
	 **************************************************************************/
	private void processStartCommands( String[] items )
	{
		if (items.length >= 3)
		{
			String what = items[1];
			String item = items[2];
			what = what.toLowerCase();
			if (what.equals("executor") || what.startsWith("e"))
			{
				Map<String,String> availableProcs = m_context.getAvailableProcedures();
				if (availableProcs.containsKey(item))
				{
					String instanceId = null;
					if (items.length==3)
					{
						instanceId = m_context.startProcedure( item, null );
					}
					else
					{
						//instanceId = startProcedure( item, args );
					}
					ExecutorInfo exec = m_context.getExecutorInformation(instanceId);
					displayExecutorInformation(exec);
				}
				else
				{
					throw new RuntimeException("Unknown procedure identifier: '" + item + "'");
				}
			}
			else if (what.equals("context")|| what.startsWith("c"))
			{
				List<String> availableContexts = m_listener.getAvailableContexts();
				if (availableContexts.contains(item))
				{
					ContextInfo ctx = m_listener.getContextInfo(item);
					if (ctx.isRunning())
					{
						System.err.println("Context '" + item + "' is already running");
						return;
					}
					m_listener.startContext(item);
					ContextInfo info = m_listener.getContextInfo(item);
					System.out.println("  - Spacecraft : " + info.getSC());
					System.out.println("  - Status     : " + info.getStatus());
				}
				else
				{
					throw new RuntimeException("Unknown context identifier: '" + item + "'");
				}
			}
			else
			{
				throw new RuntimeException("Unknown parameter '" + what + "' (syntax: start {executor|context} <identifier>)");
			}
		}
		else
		{
			throw new RuntimeException("Incorrect number of arguments (syntax: start {executor|context} <identifier>) (given " + items.length + ": '" + Arrays.toString(items) + "')");
		}
	}

	/***************************************************************************
	 * Processing of STOP commands 
	 **************************************************************************/
	private void processStopCommands( String[] items )
	{
		if (items.length >= 3)
		{
			String what = items[1];
			String item = items[2];
			what = what.toLowerCase();
			if (what.equals("executor") || what.startsWith("e"))
			{
				List<String> availableExecs = m_context.getAvailableExecutors();
				if (item.toLowerCase().equals("all"))
				{
					for(String exec : availableExecs)
					{
						m_context.stopProcedure(exec);
						System.out.println("Executor '" + item + "' stopped.");
					}
				}
				else
				{
					if (availableExecs.contains(item))
					{
						m_context.stopProcedure(item);
						System.out.println("Executor '" + item + "' stopped.");
					}
					else
					{
						throw new RuntimeException("Unknown executor identifier: '" + item + "'");
					}
				}
			}
			else if (what.equals("context")|| what.startsWith("c"))
			{
				List<String> availableContexts = m_listener.getAvailableContexts();
				if (item.toLowerCase().equals("all"))
				{
					for(String ctx : availableContexts)
					{
						ContextInfo ctxInfo = m_listener.getContextInfo(item);
						if (!ctxInfo.isRunning())
						{
							continue;
						}
						if (m_context.isConnected(ctx))
						{
							m_context.logout();
							System.out.println("Disconnected from context '" + item + "'");
						}
						m_listener.stopContext(ctx);
						System.out.println("Context '" + ctx + "' stopped.");
					}
				}
				else
				{
					if (availableContexts.contains(item))
					{
						ContextInfo ctx = m_listener.getContextInfo(item);
						if (!ctx.isRunning())
						{
							System.err.println("Context '" + item + "' is not running");
							return;
						}
						if (m_context.isConnected(item))
						{
							m_context.logout();
							System.out.println("Disconnected from context '" + item + "'");
						}
						m_listener.stopContext(item);
						ctx = m_listener.getContextInfo(item);
						System.out.println("Context '" + item + "' stopped.");
					}
					else
					{
						throw new RuntimeException("Unknown context identifier: '" + item + "'");
					}
				}
			}
			else
			{
				throw new RuntimeException("Unknown parameter '" + what + "' (syntax: stop {executor|context} <identifier>)");
			}
		}
		else
		{
			throw new RuntimeException("Incorrect number of arguments (syntax: stop {executor|context} <identifier>) (given " + items.length + ": '" + Arrays.toString(items) + "')");
		}
	}

	/***************************************************************************
	 * Processing of KILL commands 
	 **************************************************************************/
	private void processKillCommands( String[] items )
	{
		if (items.length >= 3)
		{
			String what = items[1];
			String item = items[2];
			what = what.toLowerCase();
			if (what.equals("executor") || what.startsWith("e"))
			{
				List<String> availableExecs = m_context.getAvailableExecutors();
				if (item.toLowerCase().equals("all"))
				{
					for(String exec : availableExecs)
					{
						m_context.killProcedure(exec);
						System.out.println("Executor '" + item + "' killed.");
					}
				}
				else
				{
					if (availableExecs.contains(item))
					{
						m_context.killProcedure(item);
						System.out.println("Executor '" + item + "' killed.");
					}
					else
					{
						throw new RuntimeException("Unknown executor identifier: '" + item + "'");
					}
				}
			}
			else if (what.equals("context")|| what.startsWith("c") )
			{
				List<String> availableContexts = m_listener.getAvailableContexts();
				if (item.toLowerCase().equals("all"))
				{
					for(String ctx : availableContexts)
					{
						ContextInfo ctxInfo = m_listener.getContextInfo(item);
						if (!ctxInfo.isRunning())
						{
							continue;
						}
						if (m_context.isConnected(ctx))
						{
							m_context.logout();
							System.out.println("Disconnected from context '" + item + "'");
						}
						m_listener.killContext(ctx);
						System.out.println("Context '" + item + "' killed.");
					}
				}
				else
				{
					if (availableContexts.contains(item))
					{
						ContextInfo ctx = m_listener.getContextInfo(item);
						if (!ctx.isRunning())
						{
							System.err.println("Context '" + item + "' is not running");
							return;
						}
						if (m_context.isConnected(item))
						{
							m_context.logout();
							System.out.println("Disconnected from context '" + item + "'");
						}
						m_listener.killContext(item);
						ctx = m_listener.getContextInfo(item);
						System.out.println("Context '" + item + "' killed.");
					}
					else
					{
						throw new RuntimeException("Unknown context identifier: '" + item + "'");
					}
				}
			}
			else
			{
				throw new RuntimeException("Unknown parameter '" + what + "' (syntax: kill {executor|context} <identifier>)");
			}
		}
		else
		{
			throw new RuntimeException("Incorrect number of arguments (syntax: kill {executor|context} <identifier>) (given " + items.length + ": '" + Arrays.toString(items) + "')");
		}
	}

	/***************************************************************************
	 * Processing of INFO commands 
	 **************************************************************************/
	private void processInfoCommands( String[] items )
	{
		if (items.length == 3)
		{
			String what = items[1];
			String item = items[2];
			if (what.equals("procedure") || what.startsWith("p"))
			{
				Map<String,String> availableProcs = m_context.getAvailableProcedures();
				if (item.toLowerCase().equals("all"))
				{
					for(String procId : availableProcs.keySet())
					{
						Map<ProcProperties,String> properties = m_context.getProcedureProperties(item);
						System.out.println("Information for procedure '" + procId + "':");
						for(ProcProperties key : properties.keySet())
						{
							System.out.println("  - " + key.tag + " = " + properties.get(key));
						}
					}
				}
				else
				{
					if (availableProcs.containsKey(item))
					{
						Map<ProcProperties,String> properties = m_context.getProcedureProperties(item);
						System.out.println("Information for procedure '" + item + "':");
						for(ProcProperties key : properties.keySet())
						{
							System.out.println("  - " + key.tag + " = " + properties.get(key));
						}
					}
					else
					{
						throw new RuntimeException("Unknown procedure identifier: '" + item + "'");
					}
				}
			}
			else if (what.equals("executor") || what.startsWith("e"))
			{
				List<String> executors = m_context.getAvailableExecutors();
				if (item.toLowerCase().equals("all"))
				{
					for(String exec : executors)
					{
						ExecutorInfo info = m_context.getExecutorInformation(exec);
						System.out.println("Information for executor '" + exec + "':");
						displayExecutorInformation(info);
					}
				}
				else
				{
					if (executors.contains(item))
					{
						ExecutorInfo info = m_context.getExecutorInformation(item);
						System.out.println("Information for executor '" + item + "':");
						displayExecutorInformation(info);
					}
					else
					{
						throw new RuntimeException("Unknown executor identifier: '" + item + "'");
					}
				}
			}
			else if (what.equals("context") || what.startsWith("c"))
			{
				List<String> availableContexts = m_listener.getAvailableContexts();
				if (item.toLowerCase().equals("all"))
				{
					for(String context : availableContexts)
					{
						ContextInfo info = m_listener.getContextInfo(context);
						System.out.println("Information for context '" + context + "':");
						if (m_context.isConnected(context))
						{
							System.out.println("(Currently connected to this context)");
						}
						System.out.println("  - Spacecraft : " + info.getSC());
						System.out.println("  - Status     : " + info.getStatus());
						System.out.println("  - Description: " + info.getDescription());
						System.out.println("  - Driver     : " + info.getDriver());
						System.out.println("  - Family     : " + info.getFamily());
						System.out.println("  - GCS        : " + info.getGCS());
						System.out.println("  - Max procs  : " + info.getMaxProc());
						System.out.println("  - Port       : " + info.getPort());
					}
				}
				else
				{
					if (availableContexts.contains(item))
					{
						ContextInfo info = m_listener.getContextInfo(item);
						System.out.println("Information for context '" + item + "':");
						if (m_context.isConnected(item))
						{
							System.out.println("  (Currently connected to this context)");
						}
						System.out.println("  - Spacecraft : " + info.getSC());
						System.out.println("  - Status     : " + info.getStatus());
						System.out.println("  - Description: " + info.getDescription());
						System.out.println("  - Driver     : " + info.getDriver());
						System.out.println("  - Family     : " + info.getFamily());
						System.out.println("  - GCS        : " + info.getGCS());
						System.out.println("  - Max procs  : " + info.getMaxProc());
						System.out.println("  - Port       : " + info.getPort());
					}
					else
					{
						throw new RuntimeException("Unknown context identifier: '" + item + "'");
					}
				}
			}
			else
			{
				throw new RuntimeException("Unknown parameter '" + item + "' (syntax: info {procedure|executor|context} <identifier>)");
			}
		}
		else
		{
			throw new RuntimeException("Incorrect number of arguments (syntax: info {procedure|executor|context} <identifier>) (given " + items.length + ": '" + Arrays.toString(items) + "')");
		}
	}

	/***************************************************************************
	 * Processing of ATTACH commands 
	 **************************************************************************/
	private void processAttachCommands( String[] items )
	{
		if (items.length != 2)
		{
			String item = items[1];
			if (m_context.isConnected())
			{
				throw new RuntimeException("Currently connected to context " + m_context.getContextName());
			}
			List<String> availableContexts = m_listener.getAvailableContexts();
			if (availableContexts.contains(item))
			{
				ContextInfo ctx = m_listener.getContextInfo(item);
				if (!ctx.isRunning())
				{
					throw new RuntimeException("Context '" + item + "' is not running");
				}
				ContextInfo ctxInfo = m_listener.contextStartup(item);
				m_context.login(ctxInfo);
				System.out.println("Connected to context '" + item + "'.");
			}
			else
			{
				throw new RuntimeException("Unknown context identifier: '" + item + "'");
			}
		}
		else
		{
			throw new RuntimeException("Incorrect number of arguments (syntax: attach <identifier>) (given " + items.length + ": '" + Arrays.toString(items) + "')");
		}
	}

	/***************************************************************************
	 * Processing of DETACH commands 
	 **************************************************************************/
	private void processDetachCommands( String[] items )
	{
		if (items.length == 1)
		{
			if (!m_context.isConnected())
			{
				throw new RuntimeException("Not connected to any context");
			}
			String ctx = m_context.getContextName();
			m_context.logout();
			System.out.println("Disconnected from context '" + ctx + "'.");
		}
		else
		{
			throw new RuntimeException("This command takes no arguments (syntax: detach) (given " + items.length + ": '" + Arrays.toString(items) + "')");
		}
	}

	/***************************************************************************
	 * Processing of CONNECT commands 
	 **************************************************************************/
	private void processConnectCommands( String[] items )
	{
		if (items.length != 3)
		{
			String hostname = items[1];
			int port = 0;
			try
			{
				port = Integer.parseInt(items[2]);
			}
			catch(NumberFormatException ex)
			{
				throw new RuntimeException("Invalid port number '" + items[2] + "'");
			}
			
			if (m_listener.isConnected())
			{
				throw new RuntimeException("Already connected to " + m_listener.getConnectionString());
			}

			// No authentication data used at the moment
			ServerInfo listenerInfo = new ServerInfo("SPELL",hostname,port,ServerRole.COMMANDING,null);
			// Login into listener
			m_listener.login(listenerInfo);
			System.out.println("Connected to " + m_listener.getConnectionString());
		}
		else
		{
			throw new RuntimeException("Incorrect number of arguments (syntax: connect <hostname> <port>) (given " + items.length + ": '" + Arrays.toString(items) + "')");
		}
	}

	/***************************************************************************
	 * Processing of DISCONNECT commands 
	 **************************************************************************/
	private void processDisconnectCommands( String[] items )
	{
		if (items.length != 1)
		{
			if (!m_listener.isConnected())
			{
				throw new RuntimeException("Not connected to any SPELL server");
			}
			if (m_context.isConnected())
			{
				String ctx = m_context.getContextName();
				m_context.logout();
				System.out.println("Disconnected from context " + ctx);
			}
			String conn = m_listener.getConnectionString();
			m_listener.logout();
			System.out.println("Disconnected from " + conn);
		}
		else
		{
			throw new RuntimeException("This command accepts no arguments (syntax: disconnect) (given " + items.length + ": '" + Arrays.toString(items) + "')");
		}
	}

	/***************************************************************************
	 * Processing of HELP commands 
	 **************************************************************************/
	private void processHelpCommands( String[] items )
	{
		if (items.length == 1)
		{
			System.out.println("Available commands (type 'help <command>' for more information):");
			System.out.println("  - list       : list existing items");
			System.out.println("  - info       : show information about items");
			System.out.println("  - connect    : connect this client to a SPELL execution environment");
			System.out.println("  - disconnect : disconnect this client from a SPELL execution environment");
			System.out.println("  - attach     : attach this client to SPELL contexts");
			System.out.println("  - detach     : detach this client from SPELL contexts");
			System.out.println("  - start      : start processes");
			System.out.println("  - stop       : stop processes");
			System.out.println("  - kill       : kill processes");
			System.out.println("  - help       : show command help");
			System.out.println("  - exit       : exit this client");
		}
		else 
		{
			String what = items[1].toLowerCase();
			if (what.startsWith("li"))
			{
				System.out.println("Help for 'list' command:");
				System.out.println("  - Syntax 1: list (p)rocedures : list existing procedures in the current context.");
				System.out.println("  - Syntax 2: list (e)xecutors  : list started procedures (executors) in the current context.");
				System.out.println("  - Syntax 3: list (c)ontexts   : list existing contexts in the current environment");
				System.out.println("  - Notes   : This command requires that the client is connected to a SPELL execution");
				System.out.println("              environment. For syntax 1 and 2 it is also required that the client is");
				System.out.println("              attached to a running context on that environment.");
			}
			else if (what.startsWith("in"))
			{
				System.out.println("Help for 'info' command:");
				System.out.println("  - Syntax 1: info (p)rocedure <id> : provide information about the procedure with the given identifier.");
				System.out.println("  - Syntax 2: info (p)rocedure all  : provide information about all the available procedures.");
				System.out.println("  - Syntax 3: info (e)xecutor <id>  : provide information about the executor with the given identifier.");
				System.out.println("  - Syntax 4: info (e)xecutor all   : provide information about all running executors.");
				System.out.println("  - Syntax 5: info (c)ontext <id>   : provide information about the context with the given identifier.");
				System.out.println("  - Syntax 6: info (c)ontext  all   : provide information about all existing contexts.");
				System.out.println("  - Notes   : This command requires that the client is connected to a SPELL execution");
				System.out.println("              environment. For all syntax except 5 and 6 it is also required that the client is");
				System.out.println("              attached to a running context on that environment.");
			}
			else if (what.startsWith("co"))
			{
				System.out.println("Help for 'connect' command:");
				System.out.println("  - Syntax 1: connect <hostname> <port> : connect this client to the given SPELL execution environment.");
				System.out.println("  - Notes   : This command requires that the client is NOT connected to a SPELL execution");
				System.out.println("              environment already.");
			}
			else if (what.startsWith("di"))
			{
				System.out.println("Help for 'disconnect' command:");
				System.out.println("  - Syntax 1: disconnect : disconnect this client from the current SPELL execution environment.");
				System.out.println("  - Notes   : This command requires that the client is connected to a SPELL execution");
				System.out.println("              environment already. It may or may not be attached to a running context.");
			}
			else if (what.startsWith("at"))
			{
				System.out.println("Help for 'attach' command:");
				System.out.println("  - Syntax 1: attach <id> : attach this client to the indicated context.");
				System.out.println("  - Notes   : This command requires that the indicated context process is running,");
				System.out.println("              that the client is connected to the SPELL listener, and that the client");
				System.out.println("              is not already attached to any context.");
			}
			else if (what.startsWith("de"))
			{
				System.out.println("Help for 'detach' command:");
				System.out.println("  - Syntax 1: detach : detach this client from the current context.");
				System.out.println("  - Notes   : This command requires that the client is currently attached to a context.");
			}
			else if (what.startsWith("sta"))
			{
				System.out.println("Help for 'start' command:");
				System.out.println("  - Syntax 1: start (e)xecutor <id> : start the indicated procedure.");
				System.out.println("  - Syntax 2: start (e)xecutor all  : start the available procedures (not recommended).");
				System.out.println("  - Syntax 3: start (c)ontext <id>   : start the indicated context.");
				System.out.println("  - Syntax 4: start (c)ontext all    : start all the available contexts.");
				System.out.println("  - Notes   : This command requires that the client is connected to a SPELL execution");
				System.out.println("              environment. For syntax 1 and 2 it is also required that the client is");
				System.out.println("              attached to a running context on that environment.");
			}
			else if (what.startsWith("sto"))
			{
				System.out.println("Help for 'stop' command:");
				System.out.println("  - Syntax 1: stop (e)xecutor <id>  : stop the indicated executor.");
				System.out.println("  - Syntax 2: stop (e)xecutor all   : stop all the running executors.");
				System.out.println("  - Syntax 3: stop (c)ontext <id>   : stop the indicated context.");
				System.out.println("  - Syntax 4: stop (c)ontext all    : stop all the running contexts.");
				System.out.println("  - Notes   : This command requires that the client is connected to a SPELL execution");
				System.out.println("              environment. For syntax 1 and 2 it is also required that the client is");
				System.out.println("              attached to a running context on that environment. If a context is");
				System.out.println("              stopped and the client is connected to it, a detach command will be");
				System.out.println("              executed first.");
			}
			else if (what.startsWith("k"))
			{
				System.out.println("Help for 'kill' command:");
				System.out.println("  - Syntax 1: kill (e)xecutor <id>  : kill the indicated executor.");
				System.out.println("  - Syntax 2: kill (e)xecutor all   : kill all the running executors.");
				System.out.println("  - Syntax 3: kill (c)ontext <id>   : kill the indicated context.");
				System.out.println("  - Syntax 4: kill (c)ontext all    : kill all the running contexts.");
				System.out.println("  - Notes   : This command requires that the client is connected to a SPELL execution");
				System.out.println("              environment. For syntax 1 and 2 it is also required that the client is");
				System.out.println("              attached to a running context on that environment. If a context is");
				System.out.println("              killed and the client is connected to it, a detach command will be");
				System.out.println("              executed first.");
			}
			else if (what.startsWith("ex"))
			{
				System.out.println("Help for 'exit' command:");
				System.out.println("  - Syntax 1: exit   : terminate the client");
				System.out.println("  - Syntax 2: (q)uit : terminate the client");
				System.out.println("  - Notes   : all necessary disconnections are automatically done");
			}
			else
			{
				throw new RuntimeException("Ambiguous command: '" + what + "'");
			}
		}
	}

	/***************************************************************************
	 * Display executor information
	 **************************************************************************/
	private void displayExecutorInformation( ExecutorInfo info )
	{
		System.out.println("  - Procedure ID     : " + info.getProcId());
		System.out.println("  - Status           : " + info.getStatus());
		System.out.println("  - Automatic        : " + info.getAutomatic());
		System.out.println("  - Blocking         : " + info.getBlocking());
		System.out.println("  - Visible          : " + info.getVisible());
		System.out.println("  - Background       : " + info.isBackground());
		if (info.getParent() != null && !info.getParent().trim().isEmpty())
		{
			System.out.println("  - Parent procedure : " + info.getParent());
			System.out.println("  - Invocation line  : " + info.getParentCallingLine());
		}
		if (info.getStageId() != null && !info.getStageId().trim().isEmpty())
		{
			System.out.println("  - Current stage    : " + info.getStageId() + " (" + info.getStageTitle() + ")");
		}
		if (info.getUserAction() != null && !info.getUserAction().trim().isEmpty())
		{
			System.out.println("  - User action      : " + info.getUserAction() + " ( enabled: " + info.getUserActionEnabled() + " )");
		}
		System.out.println("--------------------------------------------");
		if (info.getError() != null)
		{
			System.out.println("  - Error information:");
			System.out.println("        message: " + info.getError().getMessage());
			System.out.println("        origin : " + info.getError().getOrigin());
			System.out.println("        reason : " + info.getError().getReason());
			System.out.println("        time   : " + info.getError().getTime());
			System.out.println("        stack  : " + Arrays.toString(info.getError().getStackPosition().toArray()));
			System.out.println("--------------------------------------------");
		}
		boolean haveClients = false;
		if (!info.isBackground())
		{
			haveClients = true;
			System.out.println("  - Controlling client: " + info.getControllingClient().getHost() + ":" + info.getControllingClient().getKey());
		}
		if (info.getMonitoringClients() != null && info.getMonitoringClients().length>0)
		{
			haveClients = true;
			System.out.println("  - Monitoring clients: ");
			for(IProcedureClient clt : info.getMonitoringClients())
			{
				System.out.println("  * " + clt.getHost() + ":" + clt.getKey() );
			}
		}
		if (haveClients)
		{
			System.out.println("--------------------------------------------");
		}
	}

}
