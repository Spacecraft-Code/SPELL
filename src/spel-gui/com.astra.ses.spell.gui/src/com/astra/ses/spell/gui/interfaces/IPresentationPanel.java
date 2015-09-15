///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.interfaces
// 
// FILE      : IPresentationPanel.java
//
// DATE      : Jun 20, 2013
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

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.procs.interfaces.listeners.IExecutionListener;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.types.ExecutorStatus;

public interface IPresentationPanel extends IExecutionListener
{

	/***************************************************************************
	 * Dispose
	 **************************************************************************/
	public void dispose();

	/***************************************************************************
	 * Add a presentation button
	 **************************************************************************/
	public void addPresentation(String title, String desc, Image icon, int pageIndex);

	/***************************************************************************
	 * Display a message
	 **************************************************************************/
	public void displayMessage(String message, Severity sev);

	/***************************************************************************
	 * Reset all controls
	 **************************************************************************/
	public void reset();

	/***************************************************************************
	 * Obtain panel control
	 **************************************************************************/
	public Composite getControl();

	/***************************************************************************
	 * Reset all controls
	 **************************************************************************/
	public void setEnabled(boolean enabled);

	/***************************************************************************
	 * Set current stage
	 **************************************************************************/
	public void setStage(String id, String title);

	/***************************************************************************
	 * Reset the current stage
	 **************************************************************************/
	public void resetStage();

	/***************************************************************************
	 * Select the given presentation
	 **************************************************************************/
	public void selectPresentation(int index);

	/***************************************************************************
	 * Callback procedure model configuration changes
	 **************************************************************************/
	public void notifyModelConfigured(IProcedure model);

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setClientMode(ClientMode mode);

	/***************************************************************************
	 * Callback for procedure status change. Depending on the status, some
	 * buttons are enabled and other are disabled.
	 * 
	 * @param status
	 *            The procedure status
	 **************************************************************************/
	public void setProcedureStatus(ExecutorStatus status);

}