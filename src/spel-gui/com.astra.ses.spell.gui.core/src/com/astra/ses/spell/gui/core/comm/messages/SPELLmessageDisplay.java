///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.messages
// 
// FILE      : SPELLmessageDisplay.java
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
package com.astra.ses.spell.gui.core.comm.messages;

import java.util.TreeMap;

import com.astra.ses.spell.gui.core.interfaces.IMessageField;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.types.DisplayType;
import com.astra.ses.spell.gui.core.model.types.ExecutionMode;
import com.astra.ses.spell.gui.core.model.types.Scope;
import com.astra.ses.spell.gui.core.model.types.Severity;

/*******************************************************************************
 * 
 * Represents a SPELL message of type WRITE
 * 
 ******************************************************************************/
public class SPELLmessageDisplay extends SPELLmessage
{
	/** Holds the message text */
	private String	      m_msg;
	/** Holds the message severity */
	private Severity	  m_severity;
	/** Holds the message type */
	private DisplayType	  m_type;
	/** Holds the source procedure id */
	private String	      m_procId;
	/** Holds the execution mode */
	private ExecutionMode	m_mode;
	/** Message time */
	private String	      m_time;
	/** Message sequence */
	private long	      m_sequence;
	/** Message scope */
	private Scope	      m_scope;

	public DisplayData getData()
	{
		DisplayData data = new DisplayData(m_procId, m_msg, m_type, m_severity,
		        m_scope);
		data.setExecutionMode(m_mode);
		data.setTime(m_time);
		data.setSequence(m_sequence);
		return data;
	}

	/***************************************************************************
	 * Constructor
	 * 
	 * @param data
	 *            XML data source
	 * @throws MessageException
	 **************************************************************************/
	public SPELLmessageDisplay(TreeMap<String, String> data)
	        throws MessageException
	{
		super(data);
		m_msg = get(IMessageField.FIELD_TEXT);
		m_msg = m_msg.replace("%C%", "\n");
		m_severity = Severity.valueOf(get(IMessageField.FIELD_LEVEL));
		m_type = DisplayType.valueOf(get(IMessageField.FIELD_MSG_TYPE));
		m_procId = get(IMessageField.FIELD_PROC_ID);
		m_time = get(IMessageField.FIELD_TIME);
		m_sequence = Long.parseLong(get(IMessageField.FIELD_MSG_SEQUENCE));
		if (hasKey(IMessageField.FIELD_EXECUTION_MODE))
		{
			m_mode = ExecutionMode
			        .valueOf(get(IMessageField.FIELD_EXECUTION_MODE));
		}
		else
		{
			m_mode = ExecutionMode.PROCEDURE;
		}
		if (hasKey(IMessageField.FIELD_SCOPE))
		{
			m_scope = Scope.fromCode(get(IMessageField.FIELD_SCOPE));
		}
		else
		{
			m_scope = Scope.SYS;
		}
	}
}
