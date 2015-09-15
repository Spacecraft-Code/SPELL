///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.htree
// 
// FILE      : HistoryTreeContentProvider.java
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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

/******************************************************************************
 * Provides the contents of the history tree viewer
 *****************************************************************************/
public class HistoryTreeContentProvider implements ITreeContentProvider
{
	/** Tree model */
	private HistoryTreeProcedureModel m_input;

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void inputChanged(Viewer v, Object oldInput, Object newInput)
	{
		m_input = null;
		if (newInput != null)
		{
			IProcedure proc = (IProcedure) newInput;
			m_input = new HistoryTreeProcedureModel(proc.getProcName(), (TreeViewer)v, proc);
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void dispose()
	{
		if (m_input != null)
		{
			m_input.dispose();
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public Object[] getElements(Object parent)
	{
		return new Object[] { m_input.getRootNode() };
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public Object getParent(Object child)
	{
		if (child instanceof HistoryTreeNode)
		{
			return ((HistoryTreeNode) child).getParent();
		}
		return null;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public Object[] getChildren(Object parent)
	{
		if (parent instanceof HistoryTreeNode)
		{
			return ((HistoryTreeNode) parent).getChildren();
		}
		return new Object[0];
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public boolean hasChildren(Object parent)
	{
		if (parent instanceof HistoryTreeNode)
		{
			return ((HistoryTreeNode) parent).hasChildren();
		}
		return false;
	}
}
