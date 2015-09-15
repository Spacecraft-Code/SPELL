///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.engineering.ses.spell.clientstubj.cmdclient
// 
// FILE      : CommandClient.java
//
// DATE      : Aug 29, 2013
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
package com.engineering.ses.spell.clientstubj.cmdclient;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo.ServerRole;
import com.astra.ses.spell.gui.core.utils.Logger;

/*******************************************************************************
 * This class provides a simple command line interface to interact with a SPELL
 * execution environment. 
 */
public class CommandClient 
{
	private static final String OPT_HOSTNAME    = "host";
	private static final String OPT_PORT        = "port";
	private static final String OPT_CONTEXT     = "context"; 
	private static final String OPT_EXEC        = "exec";
	
	/** Command line argument definitions */
	private Options m_optionsDef;
	/** Passed command line arguments */
	private CommandLine m_options; 
	/** The command processor */
	private CommandProcessor m_cmdProcessor;
	/** The listener interface */
	private ListenerInterface m_listener;
	/** The context interface */
	private ContextInterface m_context;
	/** Reader for stdin */
	private BufferedReader m_stdin;

	/***************************************************************************
	 * Main program
	 **************************************************************************/
	public static void main(String[] args)
	{
		CommandClient client = new CommandClient();
		
		try
		{
			client.mainLoop(args);
		}
		catch(Exception ex)
		{
			System.err.println("ERROR: " + ex.getLocalizedMessage());
		}
		finally
		{
			client.stop();
		}
	}
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	@SuppressWarnings("static-access")
    public CommandClient()
	{
		m_listener = new ListenerInterface();
		m_context = new ContextInterface();
		m_cmdProcessor = new CommandProcessor(m_listener, m_context);
		m_options = null;
		
		m_optionsDef = new Options();
		m_optionsDef.addOption( OptionBuilder.withArgName("h").withLongOpt(OPT_HOSTNAME).withDescription("host name of the SPELL server (required)").hasArg().withType( new String() ).isRequired().create() );
		m_optionsDef.addOption( OptionBuilder.withArgName("p").withLongOpt(OPT_PORT).withDescription("port of the SPELL server (required)").hasArg().withType( new Integer(0) ).isRequired().create() );
		m_optionsDef.addOption( OptionBuilder.withArgName("c").withLongOpt(OPT_CONTEXT).withDescription("name of the SPELL context (required)").hasArg().withType( new String() ).isRequired().create() );
		m_optionsDef.addOption( OptionBuilder.withArgName("e").withLongOpt(OPT_EXEC).withDescription("list of commands to executed separated by ';'").isRequired(false).withType( new String() ).hasArg().create() );
	}
	
	/***************************************************************************
	 * Main start method
	 **************************************************************************/
	private void start()
	{
		String hostname = m_options.getOptionValue(OPT_HOSTNAME);
		int port = Integer.parseInt(m_options.getOptionValue(OPT_PORT));
		
				
		// No authentication data used at the moment
		ServerInfo listenerInfo = new ServerInfo("SPELL",hostname,port,ServerRole.COMMANDING,null);
		
		// Login into listener
		m_listener.login(listenerInfo);

		System.out.println("Connected to " + m_listener.getConnectionString());

		// Attach to the given context
		String contextName = m_options.getOptionValue(OPT_CONTEXT);
		ContextInfo contextInfo = m_listener.contextStartup(contextName);
		m_context.login(contextInfo);
		System.out.println("Connected to context " + contextName);
	}

	/***************************************************************************
	 * Main stop method
	 **************************************************************************/
	private void stop()
	{
		m_context.logout();
		m_listener.logout();
	}

	/***************************************************************************
	 * Show help text 
	 **************************************************************************/
	private void showHelp()
	{
		System.out.println("Usage: java -jar <jarfile> [options]");
		System.out.println();
		System.out.println("Available options:");
		for(Object obj : m_optionsDef.getOptions())
		{
			Option opt = (Option) obj;
			System.out.println("     -" + opt.getArgName() + ", --" + opt.getLongOpt() + " : " + opt.getDescription()); 
		}
		System.out.println();
	}
	
	/***************************************************************************
	 * Main loop of the command client
	 * @throws Exception 
	 **************************************************************************/
	private void mainLoop( String[] args ) throws Exception
	{
		CommandLineParser parser = new BasicParser();
		
		try
		{
			m_options = parser.parse( m_optionsDef, args );
		}
		catch(ParseException ex)
		{
			showHelp();
			throw new RuntimeException("Invalid arguments: " + ex.getLocalizedMessage());
		}
		
		start();
		
		if (m_options.hasOption(OPT_EXEC))
		{
			String exec = m_options.getOptionValue(OPT_EXEC);
			if (exec == null || exec.trim().isEmpty())
			{
				showHelp();
				throw new RuntimeException("Invalid value for --exec (-e) option: '" + exec + "'");
			}
			executionLoop( exec );
		}
		else
		{
			interactiveLoop();
		}
	}
	
	/***************************************************************************
	 * Interactive loop 
	 * @throws Exception 
	 **************************************************************************/
	private void interactiveLoop() throws Exception
	{
		m_stdin = new BufferedReader( new InputStreamReader( System.in ) );
		System.out.println();
		while(true)
		{
			System.out.print("> ");
			try
			{
				String command = m_stdin.readLine();
				if (command == null || command.trim().isEmpty()) continue;
				m_cmdProcessor.processCommand(command);
				System.out.println();
			}
			catch(Exception ex)
			{
				if (ex.getLocalizedMessage().equals("exit")) break;
				System.err.println("ERROR: " + ex.getLocalizedMessage());
			}
		}
	}
	
	/***************************************************************************
	 * Automatic command execution loop 
	 **************************************************************************/
	private void executionLoop( String commandsStr )
	{
		String[] commands = commandsStr.split(";");
		for(String command : commands)
		{
			// Will interrupt the loop in case of failure
			m_cmdProcessor.processCommand(command);
		}
	}
	

	

}
