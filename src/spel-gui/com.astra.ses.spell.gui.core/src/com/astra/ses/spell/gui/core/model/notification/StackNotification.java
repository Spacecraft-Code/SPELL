///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.notification
// 
// FILE      : StackNotification.java
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

import java.util.Arrays;
import java.util.List;

/*******************************************************************************
 * @brief Data structure used for line notifications.
 * @date 25/10/07
 ******************************************************************************/
public class StackNotification extends NotificationData 
{
	public enum StackType
	{
		LINE, CALL, RETURN, STAGE
	}

	/** Holds the stage id if any */
	private String	  m_stageId;
	/** Holds the stage name if any */
	private String	  m_stageTitle;
	/** Holds the stack data type */
	private StackType	m_stackType;
	/** Holds the code name */
	private String	  m_codeName;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param stack
	 *            The stack position
	 ***************************************************************************/
	public StackNotification(StackType type, String procId, String stack,
	        String codeName)
	{
		super(procId, stack);
		m_stackType = type;
		m_stageId = null;
		m_stageTitle = null;
		m_codeName = codeName;
	}

	/***************************************************************************
	 * Constructor
	 * 
	 * @param stack
	 *            The stack position
	 ***************************************************************************/
	public StackNotification(StackType type, String procId, List<String> stack, int numExecutions)
	{
		super(procId,stack,numExecutions);
		m_stackType = type;
		m_stageId = null;
		m_stageTitle = null;
		m_codeName = procId;
	}

	public void setStage(String id, String title)
	{
		m_stageId = id;
		m_stageTitle = title;
	}

	public String getStageId()
	{
		return m_stageId;
	}

	public String getStageTitle()
	{
		return m_stageTitle;
	}

	public StackType getStackType()
	{
		return m_stackType;
	}

	public String getCodeName()
	{
		return m_codeName;
	}
	
	public String toString()
	{
		String pos = Arrays.toString( getStackPosition().toArray( new String[0] ) );
		return m_stackType.toString() + " (" + m_codeName + ") " + pos;
	}
}
