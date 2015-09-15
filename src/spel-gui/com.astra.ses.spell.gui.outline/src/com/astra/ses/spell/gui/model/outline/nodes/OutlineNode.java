////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.outline.nodes
// 
// FILE      : OutlineNode.java
//
// DATE      : Sep 22, 2010 9:32:37 AM
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
package com.astra.ses.spell.gui.model.outline.nodes;

import java.util.ArrayList;

/**************************************************************************
 * Base node of the callstack tree viewer.
 * 
 *************************************************************************/
public abstract class OutlineNode
{
	/***************************************************************************
	 * 
	 * {@link OutlineNodeType} determines the type of node
	 * 
	 **************************************************************************/
	public enum OutlineNodeType
	{
		ROOT, CATEGORY, STEP, GOTO, DECLARATION, ERROR
	}

	/** Node type */
	private OutlineNodeType	       m_type;
	/** Label for this node */
	private String	               m_label;
	/** Parent node if any */
	/** Holds the code Id */
	private String	               m_codeId;
	/** Holds the line number */
	private int	                   m_lineNo;
	/** Holds the parent node if any */
	private OutlineNode	           m_parent;
	/** Children nodes if any */
	private ArrayList<OutlineNode>	m_children;

	/**************************************************************************
	 * Constructor.
	 * 
	 * @param type
	 *            the node type
	 * @param label
	 *            the label
	 *************************************************************************/
	public OutlineNode(OutlineNodeType type, String label, String codeId,
	        int lineNo)
	{
		m_type = type;
		m_label = label;
		m_codeId = codeId;
		m_lineNo = lineNo;
		m_children = new ArrayList<OutlineNode>();
	}

	/**************************************************************************
	 * Get the parent node if any.
	 * 
	 * @return
	 *************************************************************************/
	public OutlineNode getParent()
	{
		return m_parent;
	}

	/**************************************************************************
	 * Get the children nodes.
	 *************************************************************************/
	public OutlineNode[] getChildren()
	{
		return m_children.toArray(new OutlineNode[m_children.size()]);
	}

	/**************************************************************************
	 * Get node type
	 * 
	 * @return
	 *************************************************************************/
	public OutlineNodeType getType()
	{
		return m_type;
	}

	/**************************************************************************
	 * Check if there are children.
	 *************************************************************************/
	public boolean hasChildren()
	{
		return m_children.size() > 0;
	}

	/**************************************************************************
	 * Obtain the node label.
	 *************************************************************************/
	@Override
	public String toString()
	{
		return m_label;
	}

	/**************************************************************************
	 * Obtain the code identifier
	 *************************************************************************/
	public String getCodeId()
	{
		return m_codeId;
	}

	/**************************************************************************
	 * Obtain the line number
	 *************************************************************************/
	public int getLineNo()
	{
		return m_lineNo;
	}

	/**************************************************************************
	 * Set the parent node.
	 * 
	 * @param parent
	 *************************************************************************/
	public void setParent(OutlineNode parent)
	{
		m_parent = parent;
	}

	/**************************************************************************
	 * Add a child node.
	 * 
	 * @param child
	 *************************************************************************/
	public void addChild(OutlineNode child)
	{
		m_children.add(child);
		child.setParent(this);
	}

	/**************************************************************************
	 * Clear the node children.
	 *************************************************************************/
	public void clearChildren()
	{
		m_children.clear();
	}

	/**************************************************************************
	 * Check if the node is active
	 *************************************************************************/
	public boolean isActiveNode()
	{
		switch (m_type)
		{
		case STEP: // Fall through
		case GOTO: // Fall through
		case ERROR: // Fall through
		case DECLARATION:
			return true;
		case ROOT: // Fall through
		case CATEGORY: // Fall through
		default:
			return false;
		}
	}
}
