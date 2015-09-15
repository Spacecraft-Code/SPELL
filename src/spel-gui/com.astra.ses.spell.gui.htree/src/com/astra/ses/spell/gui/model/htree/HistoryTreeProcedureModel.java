///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.htree
// 
// FILE      : HistoryTreeProcedureModel.java
//
// DATE      : Jun 12, 2013
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
package com.astra.ses.spell.gui.model.htree;

import org.eclipse.jface.viewers.TreeViewer;

import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

/******************************************************************************
 * 
 *****************************************************************************/
public class HistoryTreeProcedureModel 
{
	/** Holds the root (invisible parent node on the tree) */
	private HistoryTreeRootNode	m_root;
	/** Holds the currently active node */
	private HistoryTreeNode	  m_currentNode;

	/**************************************************************************
	 * Constructor.
	 * 
	 * @param name
	 *            The identifier of the procedure
	 * @param model
	 *            The associated procedure model, used to populate the tree
	 *            model.
	 *************************************************************************/
	public HistoryTreeProcedureModel(String name, TreeViewer viewer, IProcedure model)
	{
		// If we have a model available, populate this node with the data
		m_root = new HistoryTreeRootNode(viewer, model);
		m_currentNode = m_root;
	}

	/***************************************************************************
	 * Dispose the model
	 * 
	 **************************************************************************/
	public void dispose()
	{
		m_root.dispose();
	}

	/***************************************************************************
	 * Get the current node
	 * 
	 * @return
	 **************************************************************************/
	public HistoryTreeNode getCurrentNode()
	{
		return m_currentNode;
	}

	/***************************************************************************
	 * Get this model's root node
	 * 
	 * @return
	 **************************************************************************/
	public HistoryTreeNode getRootNode()
	{
		return m_root;
	}

	/**************************************************************************
	 * Clear the model children.
	 *************************************************************************/
	public void clear()
	{
		// If we have a model available, populate this node with the data
		m_root.dispose();
		m_root.clearChildren();
	}
}
