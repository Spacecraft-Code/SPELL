///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.interfaces
// 
// FILE      : IProcedureModelListener.java
//
// DATE      : 2008-11-21 08:55
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
package com.astra.ses.spell.gui.procs.interfaces;

import com.astra.ses.spell.gui.core.interfaces.listeners.IBaseListener;
import com.astra.ses.spell.gui.core.model.types.UnloadType;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

public interface IProcedureModelListener extends IBaseListener
{
	/**************************************************************************
	 * Callback for notifying that the procedure has been loaded
	 * 
	 * @param model
	 *            The corresponding procedure model
	 **************************************************************************/
	public void notifyProcedureModelLoaded(IProcedure model);

	/**************************************************************************
	 * Callback for notifying that the procedure has been unloaded
	 * 
	 * @param model
	 *            The corresponding procedure model
	 **************************************************************************/
	public void notifyProcedureModelUnloaded(IProcedure model, UnloadType type);

	/**************************************************************************
	 * Callback for notifying that the procedure has been reset
	 * 
	 * @param model
	 *            The corresponding procedure model
	 **************************************************************************/
	public void notifyProcedureModelReset(IProcedure model);

	/**************************************************************************
	 * Callback for notifying that the procedure has been enabled
	 * 
	 * @param model
	 *            The corresponding procedure model
	 **************************************************************************/
	public void notifyProcedureModelEnabled(IProcedure model);

	/**************************************************************************
	 * Callback for notifying that the procedure has been disabled
	 * 
	 * @param model
	 *            The corresponding procedure model
	 **************************************************************************/
	public void notifyProcedureModelDisabled(IProcedure model);

	/**************************************************************************
	 * Callback for notifying that the procedure has changed its configuration
	 * 
	 * @param model
	 *            The corresponding procedure model
	 **************************************************************************/
	public void notifyProcedureModelConfigured(IProcedure model);
}
