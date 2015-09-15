///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.server
// 
// FILE      : ExecutorInfo.java
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
package com.astra.ses.spell.gui.core.model.server;

import java.util.Arrays;

import com.astra.ses.spell.gui.core.interfaces.IExecutorInfo;
import com.astra.ses.spell.gui.core.interfaces.IProcedureClient;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.types.ExecutorStatus;

public class ExecutorInfo implements IExecutorInfo
{
	/** Holds the procedure identifier */
	private String	       m_procId;
	/** Holds the procedure name */
	private String	       m_procName;
	/** Holds the ASRUN name */
	private String	       m_asrunName;
	/** Holds the time identifier name */
	private String	       m_timeId;
	/** Holds the context name */
	private String	       m_contextName;
	/** Holds the parent procedure identifier, if any */
	private String	       m_parentProcId;
	/** Holds the group identifier, if any */
	private String	       m_groupId;
	/** Holds the origin identifier, if any */
	private String	       m_originId;
	/** Holds the parent procedure calling line, if any */
	private int m_parentCallingLine;
	/** Holds the execution status */
	private ExecutorStatus	m_status;
	/** Holds the error data if any */
	private ErrorData	   m_errorData;
	/** Holds the execution condition */
	private String	       m_condition;
	/** Holds the controlling client if any */
	private IProcedureClient m_controllingClient;
	/** Holds the list of monitoring clients, if any */
	private IProcedureClient[] m_monitoringClients;
	/** Mode of this client for this executor */
	private ClientMode	   m_mode;
	/** Holds the current stage identifier if any */
	private String	       m_stageId;
	/** Holds the current stage name if any */
	private String	       m_stageTitle;
	/** Holds the current stack string */
	private String	       m_stack;
	/** Holds the current code name */
	private String	       m_codeName;
	/** True if the procedure is started in visible mode */
	private boolean	       m_visible;
	/** True if the procedure is started in automatic mode */
	private boolean	       m_automatic;
	/** True if the procedure is started in blocking mode */
	private boolean	       m_blocking;
	/** Holds the current action if any */
	private String	       m_currentAction;
	/** Holds the enablement status of the current action if any */
	private boolean	       m_currentActionEnabled;
	/** Holds the severity of the current action if any */
	private Severity	   m_currentActionSeverity;
	/** Background flag */
	private boolean        m_inBackground;
	

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ExecutorInfo(String procId)
	{
		m_controllingClient = null;
		m_monitoringClients = null;
		m_procId = procId;
		m_groupId = "";
		m_originId = "";
		m_procName = procId;
		m_contextName = "???";
		m_parentProcId = null;
		m_parentCallingLine = 0;
		m_mode = ClientMode.UNKNOWN;
		m_inBackground = false;
		m_condition = null;
		m_asrunName = null;
		m_timeId = null;
		reset();
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#reset()
     */
	@Override
    public void reset()
	{
		m_stageId = null;
		m_stageTitle = null;
		m_stack = null;
		m_codeName = null;
		m_status = ExecutorStatus.UNKNOWN;
		m_visible = true;
		m_automatic = true;
		m_blocking = true;
		m_errorData = null;
		m_currentAction = "";
		m_currentActionEnabled = false;
		m_currentActionSeverity = Severity.INFO;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#copyFrom(com.astra.ses.spell.gui.core.model.server.ExecutorInfo)
     */
	@Override
    public void copyFrom(ExecutorInfo info)
	{
		m_status = info.m_status;
		m_procName = info.m_procName;
		m_controllingClient = info.m_controllingClient;
		m_monitoringClients = Arrays.copyOf( info.m_monitoringClients, info.m_monitoringClients.length );
		m_mode = info.m_mode;
		m_procId = info.m_procId;
		m_groupId = info.m_groupId;
		m_originId = info.m_originId;
		m_parentProcId = info.m_parentProcId;
		m_parentCallingLine = info.m_parentCallingLine;
		m_condition = info.m_condition;
		m_stageId = info.m_stageId;
		m_stageTitle = info.m_stageTitle;
		m_stack = info.m_stack;
		m_codeName = info.m_codeName;
		m_visible = info.m_visible;
		m_automatic = info.m_automatic;
		m_blocking = info.m_blocking;
		m_currentAction = info.m_currentAction;
		m_currentActionEnabled = info.m_currentActionEnabled;
		m_currentActionSeverity = info.m_currentActionSeverity;
		m_asrunName = info.m_asrunName;
		m_timeId = info.m_timeId;
	}
	
	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#getName()
     */
	@Override
    public String getName()
	{
		return m_procName;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#setName(java.lang.String)
     */
	@Override
    public void setName( String name )
	{
		m_procName = name;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#getContextName()
     */
	@Override
    public String getContextName()
	{
		return m_contextName;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#setContextName(java.lang.String)
     */
	@Override
    public void setContextName( String name )
	{
		m_contextName = name;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#setMode(com.astra.ses.spell.gui.core.model.types.ClientMode)
     */
	@Override
    public void setMode(ClientMode mode)
	{
		m_mode = mode;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#getMode()
     */
	@Override
    public ClientMode getMode()
	{
		return m_mode;
	}

	@Override
    public void setParent(String parentId)
	{
		m_parentProcId = parentId;
	}

	@Override
    public void setOriginId(String originId)
	{
		m_originId = originId;
	}

	@Override
    public void setGroupId(String groupId)
	{
		m_groupId = groupId;
	}

	@Override
    public void setParentCallingLine( int line )
	{
		m_parentCallingLine = line;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#getParent()
     */
	@Override
    public String getParent()
	{
		return m_parentProcId;
	}

	@Override
    public String getGroupId()
	{
		return m_groupId;
	}

	@Override
    public String getOriginId()
	{
		return m_originId;
	}

	@Override
    public int getParentCallingLine()
	{
		return m_parentCallingLine;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#getCondition()
     */
	@Override
    public String getCondition()
	{
		return m_condition;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#setCondition(java.lang.String)
     */
	@Override
    public void setCondition(String condition)
	{
		m_condition = condition;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#setAutomatic(boolean)
     */
	@Override
    public void setAutomatic(boolean automatic)
	{
		m_automatic = automatic;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#setVisible(boolean)
     */
	@Override
    public void setVisible(boolean visible)
	{
		m_visible = visible;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#setBlocking(boolean)
     */
	@Override
    public void setBlocking(boolean blocking)
	{
		m_blocking = blocking;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#getAutomatic()
     */
	@Override
    public boolean getAutomatic()
	{
		return m_automatic;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#getVisible()
     */
	@Override
    public boolean getVisible()
	{
		return m_visible;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#getBlocking()
     */
	@Override
    public boolean getBlocking()
	{
		return m_blocking;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#setControllingClient(com.astra.ses.spell.gui.core.interfaces.IProcedureClient)
     */
	@Override
    public void setControllingClient( IProcedureClient client )
	{
		m_controllingClient = client;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#setMonitoringClients(com.astra.ses.spell.gui.core.interfaces.IProcedureClient[])
     */
	@Override
    public void setMonitoringClients( IProcedureClient[] clients)
	{
		if (clients != null)
		m_monitoringClients = Arrays.copyOf(clients, clients.length);
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#getProcId()
     */
	@Override
    public String getProcId()
	{
		return m_procId;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#getMonitoringClients()
     */
	@Override
    public IProcedureClient[] getMonitoringClients()
	{
		return m_monitoringClients;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#getControllingClient()
     */
	@Override
    public IProcedureClient getControllingClient()
	{
		return m_controllingClient;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#getControllingClient()
     */
	@Override
    public boolean isBackground()
	{
		return m_inBackground;
	}
	
	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#getControllingClient()
     */
	@Override
    public void setBackground( boolean background )
	{
		m_inBackground = background;
	}


	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#getStatus()
     */
	@Override
    public ExecutorStatus getStatus()
	{
		return m_status;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#getError()
     */
	@Override
    public ErrorData getError()
	{
		return m_errorData;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#setError(com.astra.ses.spell.gui.core.model.notification.ErrorData)
     */
	@Override
    public void setError(ErrorData data)
	{
		m_status = ExecutorStatus.ERROR;
		m_errorData = data;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#setStatus(com.astra.ses.spell.gui.core.model.types.ExecutorStatus)
     */
	@Override
    public void setStatus(ExecutorStatus st)
	{
		m_status = st;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#getStageId()
     */
	@Override
    public String getStageId()
	{
		return m_stageId;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#getStageTitle()
     */
	@Override
    public String getStageTitle()
	{
		return m_stageTitle;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#getStageId()
     */
	@Override
    public String getStack()
	{
		return m_stack;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#getStageTitle()
     */
	@Override
    public String getCodeName()
	{
		return m_codeName;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#setStage(java.lang.String, java.lang.String)
     */
	@Override
    public void setStage(String id, String title)
	{
		m_stageId = id;
		m_stageTitle = title;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#setStage(java.lang.String, java.lang.String)
     */
	@Override
    public void setStack(String csp, String codeName)
	{
		m_stack = csp;
		m_codeName = codeName;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#setUserAction(java.lang.String)
     */
	@Override
    public void setUserAction(String actionLabel)
	{
		m_currentAction = actionLabel;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#setUserActionEnabled(boolean)
     */
	@Override
    public void setUserActionEnabled(boolean enabled)
	{
		m_currentActionEnabled = enabled;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#getUserAction()
     */
	@Override
    public String getUserAction()
	{
		return m_currentAction;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#getUserActionEnabled()
     */
	@Override
    public boolean getUserActionEnabled()
	{
		return m_currentActionEnabled;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#getUserActionSeverity()
     */
	@Override
    public Severity getUserActionSeverity()
	{
		return m_currentActionSeverity;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.model.server.IExecutorInfo#setUserActionSeverity(com.astra.ses.spell.gui.core.model.types.Severity)
     */
	@Override
    public void setUserActionSeverity(Severity sev)
	{
		m_currentActionSeverity = sev;
	}

	@Override
    public String getAsRunName()
    {
	    return m_asrunName;
    }

	@Override
    public String getTimeId()
    {
	    return m_timeId;
    }

	@Override
    public void setAsRunName( String asrunName )
    {
	    m_asrunName = asrunName;
    }

	@Override
    public void setTimeId( String timeId )
    {
	    m_timeId = timeId;
    }
}
