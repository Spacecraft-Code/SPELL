///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.shell.commands
// 
// FILE      : ShellCommand.java
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

import java.util.Vector;

import com.astra.ses.spell.gui.core.comm.commands.Command;
import com.astra.ses.spell.gui.core.exceptions.CommandFailed;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.shell.services.ShellManager;

/*******************************************************************************
 * Abstract implementation of a shell command
 ******************************************************************************/
public abstract class ShellCommand implements Command
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	/** Holds the reference to the shell manager */
	protected ShellManager	 m_mgr	   = null;
	/** Holds the list of arguments */
	protected Vector<String>	m_args	= new Vector<String>();

	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ShellCommand(ShellManager mgr)
	{
		m_mgr = mgr;
	}

	/***************************************************************************
	 * Execute the command
	 **************************************************************************/
	@Override
	public abstract void execute(Vector<String> args) throws CommandFailed;

	/***************************************************************************
	 * Obtain the command arguments
	 **************************************************************************/
	@Override
	public Vector<String> getArgs()
	{
		return m_args;
	}

	/***************************************************************************
	 * Set the command arguments
	 **************************************************************************/
	@Override
	public void setArgs(Vector<String> args)
	{
		m_args = args;
	}

	/***************************************************************************
	 * Get the command string
	 **************************************************************************/
	@Override
	public abstract String getCmdString();

	/***************************************************************************
	 * Get the command help
	 **************************************************************************/
	@Override
	public abstract String getHelp();

	/***************************************************************************
	 * Get the command syntax
	 **************************************************************************/
	public abstract String getSyntax();

	// =========================================================================
	// # NON-ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Send output to shell listeners
	 **************************************************************************/
	protected void display(String output)
	{
		display(output, Severity.INFO);
	}

	/***************************************************************************
	 * Send output to shell listeners (with severity)
	 **************************************************************************/
	protected void display(String output, Severity sev)
	{
		m_mgr.display(output, sev);
	}
}
