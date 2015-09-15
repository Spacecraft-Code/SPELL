///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.notification
// 
// FILE      : NotificationData.java
//
// DATE      : 2008-11-21 08:58
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
package com.astra.ses.spell.gui.core.model.notification;

import java.util.LinkedList;
import java.util.List;

import com.astra.ses.spell.gui.core.model.types.ExecutionMode;

public class NotificationData implements Comparable<NotificationData>
{
	/** Holds the procedure id */
	private String	        m_procId;
	/** Holds the call stack position. */
	private List<String>	m_csp;
	/** Holds the number of executions of the current position */
	private int	            m_numExecutions;
	/** Holds the notification time */
	private String	        m_time;
	/** Holds the execution mode */
	private ExecutionMode	m_mode;
	/** Holds the notification sequence */
	private Long	        m_sequence;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param procId
	 *            The procedure identifier
	 * @param stack
	 *            The stack position string
	 ***************************************************************************/
	public NotificationData(String procId, String stack)
	{
		m_procId = procId;
		m_csp = new LinkedList<String>();
		m_numExecutions = 0;
		String[] levels = stack.split(":");
		int count = 0;
		for (String element : levels)
		{
			if (count == levels.length - 1)
			{
				String[] tokens = element.split("/");
				if (tokens.length > 1)
				{
					m_numExecutions = Integer.parseInt(tokens[1]);
					m_csp.add(tokens[0]);
				}
				else
				{
					m_csp.add(element);
				}
			}
			else
			{
				m_csp.add(element);
			}
			count++;
		}
		m_time = "";
		m_mode = ExecutionMode.PROCEDURE;
		m_sequence = new Long(-1);
	}

	/***************************************************************************
	 * Constructor
	 * 
	 * @param procId
	 *            The procedure identifier
	 * @param stack
	 *            The stack position string
	 ***************************************************************************/
	public NotificationData(String procId, List<String> stack, int numExecutions)
	{
		m_procId = procId;
		m_csp = new LinkedList<String>();
		m_csp.addAll(stack);
		m_numExecutions = numExecutions;
		m_time = "";
		m_mode = ExecutionMode.PROCEDURE;
		m_sequence = new Long(-1);
	}

	/***************************************************************************
	 * 
	 ***************************************************************************/
	public String getProcId()
	{
		return m_procId;
	}

	/***************************************************************************
	 * 
	 ***************************************************************************/
	public int getNumExecutions()
	{
		return m_numExecutions;
	}

	/***************************************************************************
	 * 
	 ***************************************************************************/
	public List<String> getStackPosition()
	{
		return m_csp;
	}

	/***************************************************************************
	 * 
	 ***************************************************************************/
	public void setTime(String time)
	{
		m_time = time;
	}

	/***************************************************************************
	 * 
	 ***************************************************************************/
	public String getTime()
	{
		return m_time;
	}

	/***************************************************************************
	 * 
	 ***************************************************************************/
	public void setExecutionMode(ExecutionMode mode)
	{
		m_mode = mode;
	}

	/***************************************************************************
	 * 
	 ***************************************************************************/
	public ExecutionMode getExecutionMode()
	{
		return m_mode;
	}

	/***************************************************************************
	 * 
	 ***************************************************************************/
	public void setSequence(long seq)
	{
		m_sequence = seq;
	}

	/***************************************************************************
	 * 
	 ***************************************************************************/
	public Long getSequence()
	{
		return m_sequence;
	}

	@Override
    public int compareTo(NotificationData other)
    {
	    return m_sequence.compareTo(other.m_sequence);
    }
}
