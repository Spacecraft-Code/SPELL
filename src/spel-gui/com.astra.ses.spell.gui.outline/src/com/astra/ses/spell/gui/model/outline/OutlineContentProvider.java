////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.outline
// 
// FILE      : OutlineContentProvider.java
//
// DATE      : Sep 22, 2010
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
////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.model.outline;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.model.outline.nodes.OutlineNode;
import com.astra.ses.spell.gui.procs.interfaces.listeners.IExecutionListener;
import com.astra.ses.spell.gui.procs.interfaces.model.ICodeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.ICodeModel;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

/**************************************************************************
 * Provides the content of the outline tree
 *************************************************************************/
public class OutlineContentProvider implements IStructuredContentProvider, ITreeContentProvider, IExecutionListener
{
	/** Procedure call stack */
	private OutlineProcedureModel m_input;
	/** Procedure data provider reference */
	private IProcedure m_model;

	@Override
	public void inputChanged(Viewer v, Object oldInput, Object newInput)
	{
		// Update the input
		m_input = null;
		if (newInput != null)
		{
			m_model = (IProcedure) newInput;
			// Set the input
			m_input = new OutlineProcedureModel(m_model);

			// Subscribe to events
			if (oldInput == null)
			{
				m_model.getExecutionManager().addListener(this);
				onCodeChanged(null);
			}
		}
	}

	@Override
	public void dispose()
	{
		m_model.getExecutionManager().removeListener(this);
	}

	@Override
	public Object[] getElements(Object parent)
	{
		return new Object[] { m_input.getRootNode() };
	}

	@Override
	public Object getParent(Object child)
	{
		if (child instanceof OutlineNode)
		{
			return ((OutlineNode) child).getParent();
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parent)
	{
		if (parent instanceof OutlineNode)
		{
			return ((OutlineNode) parent).getChildren();
		}
		return new Object[0];
	}

	@Override
	public boolean hasChildren(Object parent)
	{
		if (parent instanceof OutlineNode)
		{
			return ((OutlineNode) parent).hasChildren();
		}
		return false;
	}

	@Override
	public void onCodeChanged( ICodeModel model )
	{
		// Force the recreation of the tree contents
		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				m_input.createContents();
			}
		});
	}

	@Override
	public void onLineChanged( ICodeLine line ) {}

	@Override
	public void onLinesChanged( List<ICodeLine> lines ) {}

	@Override
    public void onItemsChanged( List<ICodeLine> lines ) {}

	@Override
    public void onProcessingDelayChanged(long delay) {}
}
