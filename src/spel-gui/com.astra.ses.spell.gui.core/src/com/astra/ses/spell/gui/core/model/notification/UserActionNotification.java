///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.notification
// 
// FILE      : UserActionNotification.java
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

import com.astra.ses.spell.gui.core.model.types.Severity;

/*******************************************************************************
 * 
 * User action status notifications model
 * 
 ******************************************************************************/
public class UserActionNotification extends NotificationData
{

	/***************************************************************************
	 * User action status
	 **************************************************************************/
	public enum UserActionStatus
	{
		ENABLED, DISABLED, DISMISSED
	}

	/** Action status */
	private UserActionStatus	m_status;
	/** User action */
	private String	         m_action;
	/** Procedure id */
	private String	         m_procId;
	/** Severity */
	private Severity	     m_severity;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param st
	 * @param procId
	 **************************************************************************/
	public UserActionNotification(UserActionStatus st, String procId,
	        String action, Severity severity)
	{
		super(procId, "");
		m_action = action;
		m_status = st;
		m_procId = procId;
		m_severity = severity;
	}

	/***************************************************************************
	 * Constructor
	 * 
	 * @param st
	 * @param procId
	 **************************************************************************/
	public UserActionNotification(UserActionStatus st, String procId)
	{
		super(procId, "");
		m_action = null;
		m_status = st;
		m_procId = procId;
		m_severity = null;
	}

	/***************************************************************************
	 * Get current user action status
	 * 
	 * @return the status of the user action
	 **************************************************************************/
	public UserActionStatus getUserActionStatus()
	{
		return m_status;
	}

	/***************************************************************************
	 * Return the target action, if set
	 * 
	 * @return
	 **************************************************************************/
	public String getAction()
	{
		return m_action;
	}

	/***************************************************************************
	 * Get procedure Id
	 **************************************************************************/
	public String getProcId()
	{
		return m_procId;
	}

	/***************************************************************************
	 * Get severity
	 **************************************************************************/
	public Severity getSeverity()
	{
		return m_severity;
	}
}
