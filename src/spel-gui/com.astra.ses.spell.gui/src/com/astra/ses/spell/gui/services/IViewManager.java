///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.services
// 
// FILE      : IViewManager.java
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
package com.astra.ses.spell.gui.services;

import org.eclipse.ui.part.ViewPart;

import com.astra.ses.spell.gui.core.interfaces.IService;
import com.astra.ses.spell.gui.exceptions.NoSuchViewException;
import com.astra.ses.spell.gui.interfaces.IProcedureView;

/*******************************************************************************
 * @brief This class mantains a registry of all relevant views of the GUI,
 *        including procedure views, control view and the navigation view.
 * @date 09/10/07
 ******************************************************************************/
public interface IViewManager extends IService 
{
	/***************************************************************************
	 * Register a view part
	 * 
	 * @param viewId
	 *            View identifier
	 * @param view
	 *            View reference
	 **************************************************************************/
	public void registerView(String viewId, ViewPart view);

	/***************************************************************************
	 * Check if the given view is visible
	 * 
	 * @param viewId
	 * @return True if visible
	 **************************************************************************/
	public boolean isVisible(String viewId);

	/***************************************************************************
	 * Check if the given view exists
	 * 
	 * @param viewId
	 * @return True if exists
	 **************************************************************************/
	public boolean containsProcedureView(String viewId);

	/***************************************************************************
	 * Obtain a registered view
	 * 
	 * @param viewId
	 *            View identifier
	 * @return The view reference
	 * @throws NoSuchViewException
	 **************************************************************************/
	public ViewPart getView(String viewId) throws NoSuchViewException;

	/***************************************************************************
	 * Obtain a procedure view
	 * 
	 * @param viewId
	 *            View identifier
	 * @return The view reference
	 * @throws NoSuchViewException
	 **************************************************************************/
	public IProcedureView getProcedureView(String viewId) throws NoSuchViewException;
	
	/***************************************************************************
	 * Put a procedure view on top
	 * 	 
	 * @param viewId
	 *            View identifier
	 * @throws NoSuchViewException
	 **************************************************************************/
	public void showProcedureView( String procId );
}
