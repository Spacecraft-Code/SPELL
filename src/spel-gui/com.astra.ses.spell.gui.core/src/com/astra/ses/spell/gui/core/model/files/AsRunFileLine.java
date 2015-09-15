///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.files
// 
// FILE      : AsRunFileLine.java
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
package com.astra.ses.spell.gui.core.model.files;

import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.NotificationData;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification.UserActionStatus;
import com.astra.ses.spell.gui.core.model.types.AsRunType;
import com.astra.ses.spell.gui.core.model.types.DisplayType;
import com.astra.ses.spell.gui.core.model.types.ExecutionMode;
import com.astra.ses.spell.gui.core.model.types.ItemType;
import com.astra.ses.spell.gui.core.model.types.Scope;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.types.ExecutorStatus;

/*******************************************************************************
 * Representation of an AsRun file line
 * 
 ******************************************************************************/
public class AsRunFileLine extends BasicServerFileLine
{
	private String	  m_procId;
	private String	  m_timestamp;
	private AsRunType	m_type;
	private String	  m_subType;
	private String	  m_stack;
	private String	  m_dataA;
	private String	  m_dataB;
	private String	  m_dataC;
	private String	  m_dataD;
	private String	  m_comment;
	private long	  m_sequence;

	/***********************************************************************
	 * Constructor
	 **********************************************************************/
	public AsRunFileLine(String procId, String source)
	{
		super(source);
		m_procId = procId;
		try
		{
			m_type = AsRunType.valueOf(getElement(AsRunColumns.TYPE.ordinal()));
			m_timestamp = getElement(AsRunColumns.TIMESTAMP.ordinal());
			m_sequence = Long.parseLong(getElement(AsRunColumns.SEQUENCE.ordinal()));
			m_stack = getElement(AsRunColumns.STACK_POSITION.ordinal());
			m_subType = getElement(AsRunColumns.SUBTYPE.ordinal());
			m_dataA = getElement(AsRunColumns.DATA_A.ordinal());
			m_dataB = getElement(AsRunColumns.DATA_B.ordinal());
			m_dataC = getElement(AsRunColumns.DATA_C.ordinal());
			m_dataD = getElement(AsRunColumns.DATA_D.ordinal());
			m_comment = getElement(AsRunColumns.COMMENTS.ordinal());
		}
		catch (Exception ex)
		{
			String src = source.replace("\t", "|");
			System.err.println("[" + src + "]");
			ex.printStackTrace();
			m_type = AsRunType.UNKNOWN;
		};
	}

	/***********************************************************************
	 * Getter
	 **********************************************************************/
	public AsRunType getAsRunType()
	{
		return m_type;
	}

	/***********************************************************************
	 * Getter
	 **********************************************************************/
	public String getType()
	{
		return m_type.toString();
	}

	/***********************************************************************
	 * Getter
	 **********************************************************************/
	public String getSubType()
	{
		return m_subType;
	}

	/***********************************************************************
	 * Getter
	 **********************************************************************/
	public String getStackPosition()
	{
		return m_stack;
	}

	/***********************************************************************
	 * Getter
	 **********************************************************************/
	public String getTimestamp()
	{
		return m_timestamp;
	}

	/***********************************************************************
	 * Getter
	 **********************************************************************/
	public String getDataA()
	{
		return m_dataA;
	}

	/***********************************************************************
	 * Getter
	 **********************************************************************/
	public String getDataB()
	{
		return m_dataB;
	}

	/***********************************************************************
	 * Getter
	 **********************************************************************/
	public String getDataC()
	{
		return m_dataC;
	}

	/***********************************************************************
	 * Getter
	 **********************************************************************/
	public String getDataD()
	{
		return m_dataD;
	}

	/***********************************************************************
	 * Getter
	 **********************************************************************/
	public String getComment()
	{
		return m_comment;
	}

	/***********************************************************************
	 * Getter
	 **********************************************************************/
	public long getSequence()
	{
		return m_sequence;
	}

	/***********************************************************************
	 * Getter
	 **********************************************************************/
	public NotificationData getNotificationData()
	{
		NotificationData data = null;
		switch (m_type)
		{
		case STATUS:
		{
			ExecutorStatus st = ExecutorStatus.valueOf(getDataA());
			data = new StatusNotification(m_procId, st);
			break;
		}
		case LINE:
		{
			data = new StackNotification(StackNotification.StackType.LINE,
			        m_procId, m_stack, "");
			break;
		}
		case CALL:
		{
			data = new StackNotification(StackNotification.StackType.CALL,
			        m_procId, m_stack, "");
			break;
		}
		case RETURN:
		{
			data = new StackNotification(StackNotification.StackType.RETURN,
			        m_procId, m_stack, "");
			break;
		}
		case DISPLAY:
		{
			DisplayType dtype = DisplayType.DISPLAY;
			Severity sev = Severity.INFO;
			try
			{
				sev = Severity.valueOf(getSubType());
			}
			catch (Exception ex){;};
			String msg = getDataA();
			msg = msg.replace("%C%", "\n");
			Scope scope = Scope.fromCode(getDataB());
			data = new DisplayData(m_procId, msg, dtype, sev, scope);
			break;
		}
		case STAGE:
		{
			String stageId = getDataA();
			String stageTitle = getDataB();
			data = new StackNotification(StackNotification.StackType.STAGE,
			        m_procId, m_stack, "");
			if (stageTitle == null) stageTitle = "";
			((StackNotification) data).setStage(stageId, stageTitle);
			break;
		}
		case ITEM:
		{
			String itemTypeStr = getSubType();
			ItemType itype = ItemType.fromName(itemTypeStr);
			data = new ItemNotification(m_procId, itype, m_stack);
			((ItemNotification) data).setItems(getDataA(), getDataB(),
			        getDataC(), getComment(), getDataD());
			break;
		}
		case PROMPT:
		{
			String msg = getDataA();
			msg = msg.replace("%C%", "\n");
			data = new DisplayData(m_procId, msg, DisplayType.DISPLAY,
			        Severity.PROMPT, Scope.PROMPT);
			break;
		}
		case ANSWER:
		{
			String msg = getDataA();
			msg = msg.replace("%C%", "\n");
			msg = "Answer: '" + msg + "'";
			data = new DisplayData(m_procId, msg, DisplayType.DISPLAY,
			        Severity.PROMPT, Scope.PROMPT);
			break;
		}
		case UACTION:
		{
			UserActionStatus st = UserActionStatus.valueOf(getSubType());
			switch(st)
			{
			case DISABLED:
			case DISMISSED:
				data = new UserActionNotification(st, m_procId);
				break;
			case ENABLED:
				String action = null;
				Severity sev = Severity.INFO;
				if (getDataA() != null && !getDataA().trim().isEmpty())
				{
					action = getDataA();
				}
				if (getDataB() != null && !getDataB().trim().isEmpty())
				{
					sev = Severity.valueOf(getDataB());
				}
				if (action != null)
				{
					data = new UserActionNotification(st, m_procId, action, sev);
				}
				else
				{
					data = new UserActionNotification(st, m_procId);
				}
				break;
			}
			break;
		}
		case COMMAND:
		case ERROR:
		case INIT:
		default:
			data = null;
		}
		if (data != null)
		{
			data.setExecutionMode(ExecutionMode.REPLAY);
			data.setSequence(m_sequence);
		}
		return data;
	}
}
