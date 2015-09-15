///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.interfaces
// 
// FILE      : IExecutorInfo.java
//
// DATE      : Jan 11, 2012
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
package com.astra.ses.spell.gui.core.interfaces;

import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.server.ExecutorInfo;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.types.ExecutorStatus;

public interface IExecutorInfo
{

	/***************************************************************************
	 * Reset runtime data
	 **************************************************************************/
	public void reset();

	/***************************************************************************
	 * Copy data from given info
	 **************************************************************************/
	public void copyFrom(ExecutorInfo info);

	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getName();

	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getAsRunName();

	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getTimeId();

	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getGroupId();

	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getOriginId();

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setName(String name);

	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getContextName();

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setContextName(String name);

	/***************************************************************************
	 * Assign the client mode
	 **************************************************************************/
	public void setMode(ClientMode mode);

	/***************************************************************************
	 * Obtain the client mode
	 **************************************************************************/
	public ClientMode getMode();

	/***************************************************************************
	 * Assign the parent procedure
	 **************************************************************************/
	public void setParent(String parentId);

	/***************************************************************************
	 * Assign the group id
	 **************************************************************************/
	public void setGroupId(String groupId);

	/***************************************************************************
	 * Assign the origin id
	 **************************************************************************/
	public void setOriginId(String originId);

	/***************************************************************************
	 * Assign the parent procedure calling line
	 **************************************************************************/
	public void setParentCallingLine( int line );

	/***************************************************************************
	 * Get the parent procedure if any
	 **************************************************************************/
	public String getParent();

	/***************************************************************************
	 * Get the parent procedure calling line if any
	 **************************************************************************/
	public int getParentCallingLine();

	/***************************************************************************
	 * Obtain the execution condition
	 **************************************************************************/
	public String getCondition();

	/***************************************************************************
	 * Set the execution condition
	 **************************************************************************/
	public void setCondition(String condition);

	/***************************************************************************
	 * Set automatic mode
	 **************************************************************************/
	public void setAutomatic(boolean automatic);

	/***************************************************************************
	 * Set visible mode
	 **************************************************************************/
	public void setVisible(boolean visible);

	/***************************************************************************
	 * Set blocking mode
	 **************************************************************************/
	public void setBlocking(boolean blocking);

	/***************************************************************************
	 * Get automatic mode
	 **************************************************************************/
	public boolean getAutomatic();

	/***************************************************************************
	 * Get visible mode
	 **************************************************************************/
	public boolean getVisible();

	/***************************************************************************
	 * Get blocking mode
	 **************************************************************************/
	public boolean getBlocking();

	/***************************************************************************
	 * Assing the current controlling client
	 **************************************************************************/
	public void setControllingClient(IProcedureClient client);

	/***************************************************************************
	 * Set the procedure to background
	 **************************************************************************/
	public void setBackground( boolean isBackground );

	/***************************************************************************
	 * Check if the procedure runs in background
	 **************************************************************************/
	public boolean isBackground();

	/***************************************************************************
	 * Assign the list of current monitoring clients
	 **************************************************************************/
	public void setMonitoringClients(IProcedureClient[] clients);

	/***************************************************************************
	 * Obtain the procedure identifier
	 **************************************************************************/
	public String getProcId();

	/***************************************************************************
	 * Obtain the list of monitoring clients
	 **************************************************************************/
	public IProcedureClient[] getMonitoringClients();

	/***************************************************************************
	 * Obtain the controlling client
	 **************************************************************************/
	public IProcedureClient getControllingClient();

	/***************************************************************************
	 * Obtain the current executor status
	 **************************************************************************/
	public ExecutorStatus getStatus();

	/***************************************************************************
	 * Obtain the current error data
	 **************************************************************************/
	public ErrorData getError();

	/***************************************************************************
	 * Set the current error data
	 **************************************************************************/
	public void setError(ErrorData data);

	/***************************************************************************
	 * Set the executor status
	 **************************************************************************/
	public void setStatus(ExecutorStatus st);

	/***************************************************************************
	 * Obtain the current stage identifier
	 **************************************************************************/
	public String getStageId();

	/***************************************************************************
	 * Obtain the current stage title
	 **************************************************************************/
	public String getStageTitle();

	/***************************************************************************
	 * Set the current stage
	 **************************************************************************/
	public void setStage(String id, String title);

	/***************************************************************************
	 * Set the current stage
	 **************************************************************************/
	public void setStack(String csp, String codeName);

	/***************************************************************************
	 * Obtain the current stack
	 **************************************************************************/
	public String getStack();

	/***************************************************************************
	 * Obtain the current code name
	 **************************************************************************/
	public String getCodeName();

	/***************************************************************************
	 * Set user action label
	 **************************************************************************/
	public void setUserAction(String actionLabel);

	/***************************************************************************
	 * Set user action status
	 **************************************************************************/
	public void setUserActionEnabled(boolean enabled);

	/***************************************************************************
	 * Set asrun name
	 **************************************************************************/
	public void setAsRunName( String asrunName );

	/***************************************************************************
	 * Set time Id
	 **************************************************************************/
	public void setTimeId( String timeId );

	/***************************************************************************
	 * Get user action label
	 **************************************************************************/
	public String getUserAction();

	/***************************************************************************
	 * Get user action status
	 **************************************************************************/
	public boolean getUserActionEnabled();

	/***************************************************************************
	 * Get user action severity
	 **************************************************************************/
	public Severity getUserActionSeverity();

	/***************************************************************************
	 * Set user action severity
	 **************************************************************************/
	public void setUserActionSeverity(Severity sev);

}
