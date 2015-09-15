///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.services
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
package com.astra.ses.spell.gui.core.services;

import com.astra.ses.spell.gui.core.interfaces.BaseService;
import com.astra.ses.spell.gui.core.interfaces.IShellManager;
import com.astra.ses.spell.gui.core.interfaces.listeners.IShellListener;
import com.astra.ses.spell.gui.core.model.shell.ShellExtension;

/*******************************************************************************
 * Shell interface. Provides access to the plugin (if any) which implements an
 * interactive shell allowing the user to execute certain commands, tipically
 * using command line (text-based) interfaces.
 ******************************************************************************/
public class ShellManager extends BaseService implements IShellManager
{
	/** Service id */
	public static final String	ID	= "com.astra.ses.spell.gui.ShellManager";

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ShellManager()
	{
		super(ID);
	}

	/***************************************************************************
	 * Setup the service
	 **************************************************************************/
	@Override
	public void setup()
	{
		ShellExtension.get().setShell(this);
	}

	/***************************************************************************
	 * Cleanup the service
	 **************************************************************************/
	@Override
	public void cleanup()
	{
		// Nothing to do
	}

	/***************************************************************************
	 * Subscribe to other services
	 **************************************************************************/
	@Override
	public void subscribe()
	{
		// Nothing to do
	}

	/***************************************************************************
	 * Check if a shell plugin is loaded
	 * 
	 * @return True if there is such a plugin
	 **************************************************************************/
	@Override
	public boolean haveShell()
	{
		return ShellExtension.get().haveShell();
	}

	/***************************************************************************
	 * Add a shell listener
	 * 
	 * @param lst
	 *            The listener
	 **************************************************************************/
	@Override
	public void addShellListener(IShellListener lst)
	{
		ShellExtension.get().addShellListener(lst);
	}

	/***************************************************************************
	 * Remove a shell listener
	 * 
	 * @param lst
	 *            The listener
	 **************************************************************************/
	@Override
	public void removeShellListener(IShellListener lst)
	{
		ShellExtension.get().removeShellListener(lst);
	}

	/***************************************************************************
	 * Provide input to the shell
	 * 
	 * @param input
	 *            InputData string
	 **************************************************************************/
	@Override
	public void shellInput(String input)
	{
		ShellExtension.get().shellInput(input);
	}
}
