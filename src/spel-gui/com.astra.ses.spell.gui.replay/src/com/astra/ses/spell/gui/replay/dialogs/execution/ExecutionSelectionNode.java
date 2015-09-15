///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.replay.dialogs.execution
// 
// FILE      : ExecutionSelectionNode.java
//
// DATE      : Jun 21, 2013
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
package com.astra.ses.spell.gui.replay.dialogs.execution;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;



public class ExecutionSelectionNode 
{
	private String m_label;
	private List<ExecutionSelectionNode> m_children;
	private ExecutionSelectionNode m_parent;
	private Calendar m_date;
	private NodeType m_type;

	/***************************************************************************
	 * 
	 **************************************************************************/
	public ExecutionSelectionNode( String label, Date date, NodeType type )
	{
		m_parent = null;
		m_type = type;
		m_children = new LinkedList<ExecutionSelectionNode>();
		m_label = label;
		if (date != null)
		{
			m_date = Calendar.getInstance();
			m_date.setTime(date);
		}
		else
		{
			m_date = null;
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public NodeType getType()
	{
		return m_type;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void addChild( ExecutionSelectionNode child )
	{
		m_children.add(child);
		child.setParent(this);
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setParent( ExecutionSelectionNode node )
	{
		m_parent = node;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean hasChildren()
	{
		return !m_children.isEmpty();
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public List<ExecutionSelectionNode> getChildren()
	{
		return m_children;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getLabel()
	{
		return m_label;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public ExecutionSelectionNode getParent()
	{
		return m_parent;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public Date getDate()
	{
		if (m_date == null) return null;
		return m_date.getTime();
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public int getYear()
	{
		if (m_date == null) return 0;
		return m_date.get(Calendar.YEAR);
			
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public int getMonth()
	{
		if (m_date == null) return 0;
		return m_date.get(Calendar.MONTH);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public int getDay()
	{
		if (m_date == null) return 0;
		return m_date.get(Calendar.DAY_OF_MONTH);
	}

}
