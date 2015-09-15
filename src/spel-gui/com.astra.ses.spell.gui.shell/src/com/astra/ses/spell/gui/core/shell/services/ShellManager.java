///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.shell.services
// 
// FILE      : ShellManager.java
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
package com.astra.ses.spell.gui.core.shell.services;

import java.util.ArrayList;
import java.util.Vector;

import com.astra.ses.spell.gui.core.comm.commands.Command;
import com.astra.ses.spell.gui.core.exceptions.CommandFailed;
import com.astra.ses.spell.gui.core.interfaces.IShellManager;
import com.astra.ses.spell.gui.core.interfaces.listeners.IBaseListener;
import com.astra.ses.spell.gui.core.interfaces.listeners.IShellListener;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.shell.commands.ShellCommands;
import com.astra.ses.spell.gui.core.utils.Logger;

/*******************************************************************************
 * @brief Manages a SPELL shell
 * @date 23/11/07
 ******************************************************************************/
public class ShellManager implements IShellManager, IBaseListener
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	/** The service identifier */
	public static final String	        ID	= "com.astra.ses.spell.gui.core.shell.ShellManager";

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PROTECTED ---------------------------------------------------------------
	protected ArrayList<Command>	    m_commands;
	protected ArrayList<IShellListener>	m_outputListeners;

	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor.
	 **************************************************************************/
	public ShellManager()
	{
		Logger.debug("Setting up", Level.PROC, this);
		m_outputListeners = new ArrayList<IShellListener>();
	}

	/***************************************************************************
	 * Add an output listener
	 **************************************************************************/
	@Override
	public void addShellListener(IShellListener listener)
	{
		Logger.debug("Add shell listener: " + listener, Level.PROC, this);
		m_outputListeners.add(listener);
	}

	/***************************************************************************
	 * Remove an output listener
	 **************************************************************************/
	@Override
	public void removeShellListener(IShellListener listener)
	{
		Logger.debug("Remove shell listener: " + listener, Level.PROC, this);
		m_outputListeners.remove(listener);
	}

	/***************************************************************************
	 * Provide input to the shell
	 **************************************************************************/
	public void shellInput(String input) throws CommandFailed
	{
		Logger.debug("Received input: " + input, Level.PROC, this);
		// Parse the command name
		Vector<String> args = new Vector<String>();
		String[] elements = input.split(" ");
		String cmdId = elements[0].toLowerCase();
		for (int idx = 0; idx < elements.length; idx++)
		{
			if (idx > 0)
			{
				args.add(elements[idx]);
			}
		}
		for (Command cmd : ShellCommands.getShellCommands(this))
		{
			if (cmd.getCmdString().toLowerCase().equals(cmdId))
			{
				cmd.execute(args);
				return;
			}
		}
		throw new CommandFailed("Unknown command: " + elements[0]);
	}

	/***************************************************************************
	 * Provide output to listeners
	 **************************************************************************/
	public void display(String output, Severity severity)
	{
		String[] elements = output.split("\n");
		for (IShellListener lst : m_outputListeners)
		{
			for (String element : elements)
			{
				lst.shellOutput(element, severity);
			}
		}
	}

	@Override
	public String getListenerId()
	{
		return "com.astra.ses.spell.gui.shell.Manager";
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.interfaces.IService#getServiceId()
     */
    @Override
    public String getServiceId()
    {
	    // TODO Auto-generated method stub
	    return null;
    }

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.interfaces.IService#setup()
     */
    @Override
    public void setup() {}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.interfaces.IService#cleanup()
     */
    @Override
    public void cleanup() {}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.interfaces.IService#subscribe()
     */
    @Override
    public void subscribe() {}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.interfaces.IShellManager#haveShell()
     */
    @Override
    public boolean haveShell() { return false;}
}
