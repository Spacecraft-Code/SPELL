///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.nav
// 
// FILE      : ProcedureListContentProvider.java
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
package com.astra.ses.spell.gui.model.nav;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.model.nav.content.BaseProcedureSystemElement;
import com.astra.ses.spell.gui.model.nav.content.CategoryNode;

/***************************************************************************
 * Content provider for the tree view.
 **************************************************************************/
public class ProcedureListContentProvider implements
        IStructuredContentProvider, ITreeContentProvider
{

	/***********************************************************************
	 * Tree view input callback
	 **********************************************************************/
	public void inputChanged(Viewer v, Object oldInput, Object newInput)
	{
		// Nothing to do
	}

	/***********************************************************************
	 * Called on navigation view closure
	 **********************************************************************/
	public void dispose()
	{
		Logger.debug("Content provider disposed", Level.PROC, this);
	}

	/***********************************************************************
	 * Obtain the tree elements
	 * 
	 * @param parent
	 *            The root item
	 * @return The list of child items
	 **********************************************************************/
	@SuppressWarnings("rawtypes")
	public Object[] getElements(Object parent)
	{
		return ((List) parent).toArray();
	}

	/***********************************************************************
	 * Obtain the parent of a given item
	 * 
	 * @param child
	 *            The child item
	 * @return The parent item if any
	 **********************************************************************/
	public Object getParent(Object child)
	{
		BaseProcedureSystemElement element = (BaseProcedureSystemElement) child;
		return element.getParent();
	}

	/***********************************************************************
	 * Obtain the tree childs of a given item
	 * 
	 * @param parent
	 *            The parent item
	 * @return The list of child items
	 **********************************************************************/
	public Object[] getChildren(Object parent)
	{
		BaseProcedureSystemElement element = (BaseProcedureSystemElement) parent;
		if (element.isLeaf()) { return null; }
		CategoryNode cat = (CategoryNode) parent;
		return cat.getChildren().toArray();
	}

	/***********************************************************************
	 * Check if a given item has children
	 * 
	 * @param parent
	 *            The item
	 * @return True if there are children
	 **********************************************************************/
	public boolean hasChildren(Object parent)
	{
		BaseProcedureSystemElement element = (BaseProcedureSystemElement) parent;
		return !element.isLeaf();
	}
}
