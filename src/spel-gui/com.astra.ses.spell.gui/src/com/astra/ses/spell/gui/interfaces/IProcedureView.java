///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.interfaces
// 
// FILE      : IProcedureView.java
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

import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification;

public interface IProcedureView
{
	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean cancelPrompt();

	/***************************************************************************
	 * Enable or disable the view
	 **************************************************************************/
	public void setEnabled(boolean enable);

	/***************************************************************************
	 * Enable or disable the autoscroll
	 **************************************************************************/
	public void setAutoScroll(boolean enable);

	/***************************************************************************
	 * Get associated procedure identifier
	 **************************************************************************/
	public String getProcId();

	/***************************************************************************
	 * Get associated procedure name
	 **************************************************************************/
	public String getProcName();

	/***************************************************************************
	 * Obtain the associated satellite name
	 * 
	 * @return The satellite name
	 **************************************************************************/
	public String getDomain();

	/***************************************************************************
	 * Show the desired page: code, log or display.
	 **************************************************************************/
	public void showPresentation(int index);

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setCloseable(boolean closeable);
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setCloseMode( ProcedureViewCloseMode mode );
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public ProcedureViewCloseMode getCloseMode();

	/***************************************************************************
	 * 
	 **************************************************************************/
	public IProcedurePresentation getPresentation( String id );

	/***************************************************************************
	 * Change font size
	 * 
	 * @param increase
	 *            If true, increase the font size. Otherwise decrease it.
	 **************************************************************************/
	public void zoom(boolean increase);

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyDisplay(DisplayData data);

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyError(ErrorData data);

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyItem(ItemNotification data);

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyStack(StackNotification data);

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyModelDisabled();

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyModelEnabled();

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyModelLoaded();

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyModelReset();

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyModelUnloaded();

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyModelConfigured();

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyPrompt();

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyFinishPrompt();

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyCancelPrompt();

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyStatus(StatusNotification data);

	/***************************************************************************
	 * An event about the user action arrives
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyUserAction(UserActionNotification data);

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void updateDependentCommands();
}