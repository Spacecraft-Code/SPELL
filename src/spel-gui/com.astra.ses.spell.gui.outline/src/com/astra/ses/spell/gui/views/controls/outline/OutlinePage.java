///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.outline
// 
// FILE      : OutlinePage.java
//
// DATE      : 2010-09-01
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
package com.astra.ses.spell.gui.views.controls.outline;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.Page;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.interfaces.IProcedureView;
import com.astra.ses.spell.gui.model.outline.OutlineContentProvider;
import com.astra.ses.spell.gui.model.outline.OutlineDecoratingLabelProvider;
import com.astra.ses.spell.gui.model.outline.nodes.OutlineNode;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.services.IViewManager;
import com.astra.ses.spell.gui.types.ExecutorStatus;
import com.astra.ses.spell.gui.views.ProcedureView;

/******************************************************************************
 * 
 * Call stack page shows the focused procedure call stack tree view
 *****************************************************************************/
public class OutlinePage extends Page
{

	/** Tree viewer */
	private TreeViewer	m_viewer;
	/** Proc id */
	private String	   m_procId;
	/** Action to perform when an item is double clicked */
	private Action	   m_doubleClickAction;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param procId
	 **************************************************************************/
	public OutlinePage(String procId)
	{
		m_procId = procId;
	}

	@Override
	public void createControl(Composite parent)
	{
		m_viewer = new TreeViewer(parent);
		m_viewer.setContentProvider(new OutlineContentProvider());
		m_viewer.setLabelProvider(new OutlineDecoratingLabelProvider());
		// Actions
		createActions();
		hookDoubleClickAction();
		// Set the input
		IProcedureManager mgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
		m_viewer.setInput(mgr.getProcedure(m_procId));
		m_viewer.expandToLevel(TreeViewer.ALL_LEVELS);
	}

	@Override
	public Control getControl()
	{
		return m_viewer.getControl();
	}

	@Override
	public void setFocus()
	{
		m_viewer.refresh();
		m_viewer.getControl().setFocus();
	}

	/***************************************************************************
	 * Create actions to performs on events
	 **************************************************************************/
	private void createActions()
	{
		m_doubleClickAction = new Action()
		{
			public void run()
			{
				ISelection selection = m_viewer.getSelection();
				OutlineNode node = (OutlineNode) ((IStructuredSelection) selection)
				        .getFirstElement();
				if (node.isActiveNode())
				{
					// Get the procedure
					IProcedureManager mgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
					IProcedure proc = mgr.getProcedure(m_procId);
					// Get the view
					IViewManager vmgr = (IViewManager) ServiceManager.get(IViewManager.class);
					IProcedureView view = vmgr.getProcedureView(m_procId);
					// This command can only be executed while the procedure is
					// paused
					ExecutorStatus st = proc.getRuntimeInformation()
					        .getStatus();
					switch (st)
					{
					case PAUSED:
					case PROMPT:
					case WAITING:
					case ERROR:
					case ABORTED:
					case FINISHED:
					case INTERRUPTED:
						// Switch the view
						// THIS SHOULD BE REMOVED: showline is not generic
						((ProcedureView)view).showLine(node.getLineNo());
					default:
					}
				}
			}
		};
	}

	/***************************************************************************
	 * Register the double click action
	 **************************************************************************/
	private void hookDoubleClickAction()
	{
		m_viewer.addDoubleClickListener(new IDoubleClickListener()
		{
			public void doubleClick(DoubleClickEvent event)
			{
				m_doubleClickAction.run();
			}
		});
	}
}
