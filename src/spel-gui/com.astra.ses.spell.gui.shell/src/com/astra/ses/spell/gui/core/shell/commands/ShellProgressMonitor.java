///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.shell.commands
// 
// FILE      : ShellProgressMonitor.java
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

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.shell.services.ShellManager;

/*******************************************************************************
 * Abstract implementation of a shell command
 ******************************************************************************/
public class ShellProgressMonitor implements IProgressMonitor
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the reference to the shell manager */
	private static ShellManager	s_mgr	= null;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private int	                m_totalTasks;
	private int	                m_completedTasks;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ShellProgressMonitor()
	{
		if (s_mgr == null)
		{
			s_mgr = (ShellManager) ServiceManager.get(ShellManager.class);
		}
		m_totalTasks = 0;
		m_completedTasks = 0;
	}

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
		s_mgr.display(output, sev);
	}

	@Override
	public void beginTask(String task, int totalSubTasks)
	{
		display(task);
		m_totalTasks = totalSubTasks;
		m_completedTasks = 0;
	}

	@Override
	public void done()
	{
		m_completedTasks = m_totalTasks;
	}

	@Override
	public void internalWorked(double amount)
	{
	}

	@Override
	public boolean isCanceled()
	{
		return false;
	}

	@Override
	public void setCanceled(boolean cancelled)
	{
	}

	@Override
	public void setTaskName(String arg0)
	{
	}

	@Override
	public void subTask(String task)
	{
		display("   - " + task);

	}

	@Override
	public void worked(int completed)
	{
		m_completedTasks += completed;

	}
}
