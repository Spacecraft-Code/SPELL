///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.htree
// 
// FILE      : HistoryTreeNode.java
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

import java.util.ArrayList;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.listeners.IDependenciesListener;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

/**************************************************************************
 * Base node of the callstack tree viewer.
 *************************************************************************/
public abstract class HistoryTreeNode implements IDependenciesListener
{
	/** Label for presenting this node */
	private String	                 m_procedureName;
	/** Procedure identifier */
	private String	                 m_procedureId;
	/** Procedure instance identifier */
	private String	                 m_instanceId;
	/** Executed flag */
	private boolean 				 m_executed;
	/** Active flag */
	private boolean 				 m_active;
	/** Line number */
	private int	                     m_lineNo;
	/** Parent node if any */
	private HistoryTreeNode	         m_parent;
	/** Children nodes if any */
	private ArrayList<HistoryTreeNode>	m_children;
	/** Reference to viewer */
	private TreeViewer m_viewer;
	/** Reference to procedure manager */
	private static IProcedureManager s_pmgr;

	
	static 
	{
		s_pmgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);	
	}
	
	/**************************************************************************
	 * Constructor.
	 * 
	 * @param type
	 *            the node type
	 * @param codeId
	 *            the code id this node represents
	 * @param lineNo
	 *            the initial lineNumber
	 *************************************************************************/
	public HistoryTreeNode( TreeViewer viewer, String procedureId, String procedureName, int lineNumber )
	{
		m_procedureName = procedureName;
		m_procedureId = procedureId;
		m_lineNo = lineNumber;
		m_executed = false;
		m_active = false;
		m_instanceId = null;
		m_children = new ArrayList<HistoryTreeNode>();
		m_viewer = viewer;
	}

	/**************************************************************************
	 * Get node identifier.
	 * 
	 * @return The identifier
	 *************************************************************************/
	public String getName()
	{
		return m_procedureName;
	}

	/**************************************************************************
	 * Get line number
	 *************************************************************************/
	public int getLine()
	{
		return m_lineNo;
	}

	/**************************************************************************
	 * Get the parent node if any.
	 * 
	 * @return
	 *************************************************************************/
	public HistoryTreeNode getParent()
	{
		return m_parent;
	}

	/**************************************************************************
	 * Get the children nodes.
	 *************************************************************************/
	public HistoryTreeNode[] getChildren()
	{
		return m_children.toArray(new HistoryTreeNode[m_children.size()]);
	}

	/**************************************************************************
	 * Check if there are children.
	 *************************************************************************/
	public boolean hasChildren()
	{
		return m_children.size() > 0;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public boolean isExecuted()
	{
		return m_executed;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public boolean isActive()
	{
		return m_active;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	void setActive( boolean active )
	{
		if (active) m_executed = true;
		m_active = active;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	void setInstanceId( String instanceId )
	{
		m_instanceId = instanceId;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public String getProcId()
	{
		return m_procedureId;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public String getInstanceId()
	{
		return m_instanceId;
	}

	/**************************************************************************
	 * Set the parent node.
	 * 
	 * @param parent
	 *************************************************************************/
	protected void setParent(HistoryTreeNode parent)
	{
		m_parent = parent;
	}

	/**************************************************************************
	 * Add a child node.
	 * 
	 * @param child
	 *************************************************************************/
	protected void addChild(HistoryTreeNode child)
	{
		m_children.add(child);
		child.setParent(this);
	}

	/**************************************************************************
	 * Clear the node children.
	 *************************************************************************/
	protected void clearChildren()
	{
		m_children.clear();
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	protected TreeViewer getViewer()
	{
		return m_viewer;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void onChildOpen(String parentInstanceId, String childInstanceId, int lineNo)
    {
		setActive(false);
		int idx = childInstanceId.indexOf("#");
		String childProcId = childInstanceId.substring(0,idx);

	    for(HistoryTreeNode node : getChildren())
	    {
			HistoryTreeChildProcedureNode cnode = (HistoryTreeChildProcedureNode) node;
	    	if (node.getProcId().equals(childProcId) && lineNo == node.getLine())
	    	{
	    		cnode.setInstanceId(childInstanceId);
	    		cnode.setActive(true);
	    		IProcedure childModel = s_pmgr.getProcedure(childInstanceId);
	    		cnode.setModel(childModel);
	    		final HistoryTreeChildProcedureNode ref = cnode;
	    		Display.getDefault().syncExec( new Runnable()
	    		{
	    			public void run()
	    			{
	    				getViewer().refresh();
	    				getViewer().setSelection(null);
	    				getViewer().expandToLevel(ref, TreeViewer.ALL_LEVELS);
	    			}
	    		});
	    		break;
	    	}
	    }
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void onChildClosed(String parentInstanceId, String childInstanceId)
    {
		setActive(true);
	    for(HistoryTreeNode node : getChildren())
	    {
	    	if (node.getInstanceId().equals(childInstanceId))
	    	{
	    		node.setActive(false);
	    		Display.getDefault().syncExec( new Runnable()
	    		{
	    			public void run()
	    			{
	    				getViewer().refresh();
	    				getViewer().setSelection(null);
	    			}
	    		});
	    		break;
	    	}
	    }
    }
}
