///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.interfaces
// 
// FILE      : IControlArea.java
//
// DATE      : Jun 17, 2013
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
package com.astra.ses.spell.gui.interfaces;

import org.eclipse.swt.widgets.Composite;

import com.astra.ses.spell.gui.core.model.notification.InputData;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification.UserActionStatus;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.types.ExecutorStatus;

public interface IControlArea extends ISashListener
{
	/***************************************************************************
	 * 
	 **************************************************************************/
	public Composite getControl();

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setEnabled(boolean enabled);

	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean setFocus();

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void zoom(boolean increase);

	/***************************************************************************
	 * Set the procedure status
	 **************************************************************************/
	public void setProcedureStatus(ExecutorStatus status, boolean fatalError);

	/***************************************************************************
	 * Set the client mode (will enable or disable the control panel and input
	 * area)
	 **************************************************************************/
	public void setClientMode(ClientMode mode);

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void startPrompt(InputData promptData);

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void resetPrompt();

	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean cancelPrompt( boolean uponNotification );

	/***************************************************************************
	 * Determine if we are inside a prompt
	 * 
	 * @return
	 **************************************************************************/
	public boolean isPrompt();

	/***************************************************************************
	 * Parse and issue a command
	 **************************************************************************/
	public void issueCommand(String cmdString);

	/***************************************************************************
	 * Update user action related controls
	 * 
	 * @param st
	 * @param action
	 **************************************************************************/
	public void updateUserAction(UserActionStatus st, String action, Severity sev);

}