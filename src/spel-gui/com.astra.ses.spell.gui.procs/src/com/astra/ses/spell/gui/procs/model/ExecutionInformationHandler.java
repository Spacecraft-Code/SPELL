////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : ExecutionInformationHandler.java
//
// DATE      : 2010-07-30
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
package com.astra.ses.spell.gui.procs.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.astra.ses.spell.gui.core.interfaces.IExecutorInfo;
import com.astra.ses.spell.gui.core.interfaces.IProcedureClient;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification.UserActionStatus;
import com.astra.ses.spell.gui.core.model.server.ExecutorConfig;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.PropertyKey;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformationHandler;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.types.ExecutorStatus;

/*******************************************************************************
 * 
 * RuntimeModel holds the procedure execution trace
 * 
 ******************************************************************************/
class ExecutionInformationHandler implements IExecutionInformationHandler
{

	/** Display messages */
	private List<DisplayData> m_displayMessages;
	/** Maximum capacity for display messages */
	private int m_displayMessagesCapacity;
	/** Error messages */
	private ErrorData m_error;
	/** Executor status */
	private ExecutorStatus m_status;
	/** Holds the current stage identifier if any */
	private String m_stageId;
	/** Holds the current stage name if any */
	private String m_stageTitle;
	/** User action name */
	private String m_userAction;
	/** Current user action status */
	private UserActionStatus m_userActionStatus;
	/** User action severity */
	private Severity m_userActionSeverity;
	/** By step execution mode */
	private boolean m_byStep;
	/** Holds the show lib state */
	private boolean m_showLib;
	/** TC confirmation flag */
	private boolean m_tcConfirmation;
	/** Holds the execution delay in secs */
	private int m_execDelay;
	/** PromptWaringDelay */
	private int m_promptWarningDelay;
	/** Mode of this client for this executor */
	private ClientMode m_mode;
	/** Holds the list of monitoring clients, if any */
	private IProcedureClient[] m_monitoringClients;
	/** Holds the execution condition */
	private String m_condition;
	/** Holds the controlling client if any */
	private IProcedureClient m_controllingClient;
	/** True if the procedure is started in visible mode */
	private boolean m_visible;
	/** True if the procedure is started in automatic mode */
	private boolean m_automatic;
	/** True if the procedure is started in blocking mode */
	private boolean m_blocking;
	/** Waiting input flag */
	private boolean m_waitingInput;
	/** Executor lost flag */
	private boolean m_executorLost;
	/** Parent procedure */
	private String m_parent;
	/** Parent procedure */
	private int m_parentCallingLine;
	/** Reference to procedure model */
	private IProcedure m_model;
	/** Background flag */
	private boolean m_background;
	/** Time id */
	private String m_timeId;
	/** Group id */
	private String m_groupId;
	/** Origin id */
	private String m_originId;
	/** AsRun name */
	private String m_asrunName;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ExecutionInformationHandler(ClientMode mode, IProcedure model )
	{
		m_displayMessages = new ArrayList<DisplayData>();
		m_displayMessagesCapacity = -1;
		m_error = null;
		m_status = ExecutorStatus.UNKNOWN;
		m_stageId = null;
		m_stageTitle = null;
		m_userAction = null;
		m_userActionStatus = UserActionStatus.DISMISSED;
		m_userActionSeverity = Severity.INFO;
		m_byStep = false;
		m_showLib = false;
		m_tcConfirmation = false;
		m_execDelay = 0;
		m_promptWarningDelay = 0;
		m_condition = null;
		m_controllingClient = null;
		m_monitoringClients = null;
		m_mode = mode;
		m_visible = true;
		m_automatic = true;
		m_blocking = true;
		m_waitingInput = false;
		m_executorLost = false;
		m_parent = "";
		m_timeId = "";
		m_originId = "";
		m_groupId = "";
		m_parentCallingLine = 0;
		m_model = model;
		m_background = false;
		
		// Obtain the maximum amount of display messages to store
		m_displayMessagesCapacity = -1;
		try
		{
			IConfigurationManager cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
			m_displayMessagesCapacity = Integer.parseInt(cfg.getProperty(PropertyKey.TEXT_HISTORY_ITEMS));
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		};

	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public ClientMode getClientMode()
	{
		return m_mode;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getCondition()
	{
		return m_condition;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IProcedureClient getControllingClient()
	{
		return m_controllingClient;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean isBackground()
	{
		return m_background;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public ErrorData getError()
	{
		return m_error;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean isExecutorLost()
	{
		return m_executorLost;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IProcedureClient[] getMonitoringClients()
	{
		return m_monitoringClients;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public int getExecutionDelay()
	{
		return m_execDelay;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public int getPromptWarningDelay() 
	{
		return m_promptWarningDelay;
	}	

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getParent()
	{
		return m_parent;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public int getParentCallingLine()
	{
		return m_parentCallingLine;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getOriginId()
	{
		return m_originId;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getGroupId()
	{
		return m_groupId;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public ExecutorStatus getStatus()
	{
		return m_status;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public DisplayData[] getDisplayMessages()
	{
		return m_displayMessages.toArray(new DisplayData[0]);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getStageId()
	{
		return m_stageId;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getStageTitle()
	{
		return m_stageTitle;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getUserAction()
	{
		return m_userAction;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public Severity getUserActionSeverity()
	{
		return m_userActionSeverity;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public UserActionStatus getUserActionStatus()
	{
		return m_userActionStatus;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean isAutomatic()
	{
		return m_automatic;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean isBlocking()
	{
		return m_blocking;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean isShowLib()
	{
		return m_showLib;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean isStepByStep()
	{
		return m_byStep;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setExecutorLost()
	{
		m_executorLost = true;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean isVisible()
	{
		return m_visible;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean isWaitingInput()
	{
		return m_waitingInput;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public synchronized void displayMessage(DisplayData data)
	{
		m_displayMessages.add(data);
		if ( (m_displayMessagesCapacity != -1) && 
			 (m_displayMessages.size()>m_displayMessagesCapacity) )
		{
			m_displayMessages.remove(0);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public synchronized void setDisplayMessageCapacity( int capacity )
	{
		m_displayMessagesCapacity = capacity;
		if (capacity != -1 && m_displayMessages.size()>m_displayMessagesCapacity)
		{
			int start = m_displayMessages.size() - m_displayMessagesCapacity;
			m_displayMessages = m_displayMessages.subList(start, m_displayMessages.size()-1);
			System.gc();
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public int getDisplayMessageCapacity()
	{
		return m_displayMessagesCapacity;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setExecutorStatus(ExecutorStatus status)
	{
		Logger.debug("Execution information new status: " + status, Level.PROC, this);
		m_status = status;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setStage(String id, String title)
	{
		m_stageId = id;
		m_stageTitle = title;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setUserActionStatus(String label, UserActionStatus status, Severity severity)
	{
		Logger.debug("Set user action '" + label + "' (" + status + ")", Level.PROC, this);
		m_userActionStatus = status;
		switch(status)
		{
		case DISABLED:
			// Do not erase the action or the severity, we need to keep this information
			// for possible asrun replays
			break;
		case DISMISSED:
			// Here delete the info as we are removing the action
			m_userAction = null;
			m_userActionSeverity = null;
			break;
		case ENABLED:
			// Override any preexisting information if any given; if action is null
			// do not override anything as we are just re-enabling the action
			if (label != null)
			{
				m_userAction = label;
				m_userActionSeverity = severity;
			}
			break;
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setError(ErrorData error)
	{
		m_error = error;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setStepByStep(boolean value)
	{
		m_byStep = value;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setShowLib(boolean value)
	{
		m_showLib = value;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean isForceTcConfirmation()
	{
		return m_tcConfirmation;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setForceTcConfirmation(boolean value)
	{
		m_tcConfirmation = value;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setExecutionDelay(int msec)
	{
		m_execDelay = msec;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setPromptWarningDelay(int msec)
	{
		m_promptWarningDelay = msec;
	}	
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setAutomatic(boolean automatic)
	{
		m_automatic = automatic;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setBlocking(boolean blocking) {
		m_blocking = blocking;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setCondition(String condition)
	{
		m_condition = condition;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setClientMode(ClientMode mode)
	{
		m_mode = mode;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setBackground( boolean background )
	{
		m_background = background;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setControllingClient(IProcedureClient client)
	{
		Logger.debug("Set controlling client " + client, Level.PROC, this);
		m_controllingClient = client;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setMonitoringClients(IProcedureClient[] clients)
	{
		if (clients == null)
		{
			m_monitoringClients = new IProcedureClient[0];
		}
		else
		{
			Logger.debug("Set monitoring clients " + clients.length, Level.PROC, this);
			m_monitoringClients = Arrays.copyOf(clients, clients.length);
		}
	}

	/***************************************************************************
	 * Add a client to the list of current monitoring clients
	 **************************************************************************/
	@Override
	public void addMonitoringClient(IProcedureClient client)
	{
		if (m_monitoringClients == null)
		{
			m_monitoringClients = new IProcedureClient[1];
			m_monitoringClients[0] = client;
		}
		else
		{
			Logger.debug("Add monitoring client " + client, Level.PROC, this);
			for (IProcedureClient clt : m_monitoringClients)
			{
				if (clt.getKey().equals(client.getKey()))
					return;
			}
			List<IProcedureClient> aux = new LinkedList<IProcedureClient>();
			aux.addAll(Arrays.asList(m_monitoringClients));
			aux.add(client);
			m_monitoringClients = aux.toArray(new IProcedureClient[0]);
		}
	}

	/***************************************************************************
	 * Remove a client from the list of current monitoring clients
	 **************************************************************************/
	@Override
	public void removeMonitoringClient(IProcedureClient client)
	{
		if (m_monitoringClients == null)
		{
			return;
		}
		else
		{
			Logger.debug("Remove monitoring client " + client, Level.PROC, this);
			List<IProcedureClient> aux = new LinkedList<IProcedureClient>();
			aux.addAll(Arrays.asList(m_monitoringClients));
			IProcedureClient toDelete = null;
			for (IProcedureClient clt : aux)
			{
				if (clt.getKey().equals(client.getKey()))
				{
					toDelete = clt;
					break;
				}
			}
			if (toDelete != null)
			{
				aux.remove(toDelete);
				m_monitoringClients = aux.toArray(new IProcedureClient[0]);
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setParent(String parentId)
	{
		m_parent = parentId;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setParentCallingLine( int line )
	{
		m_parentCallingLine = line;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setGroupId( String id )
	{
		m_groupId = id;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setOriginId( String id )
	{
		m_originId = id;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setVisible(boolean visible)
	{
		m_visible = visible;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setWaitingInput(boolean waiting)
	{
		Logger.debug("Procedure waiting input: " + waiting, Level.PROC, this);
		m_waitingInput = waiting;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getAsRunName()
	{
		return m_asrunName;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getTimeId()
	{
		return m_timeId;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void visit(IExecutorInfo info)
	{
		info.setAutomatic(m_automatic);
		info.setBlocking(m_blocking);
		info.setCondition(m_condition);
		info.setControllingClient(m_controllingClient);
		info.setError(m_error);
		info.setMode(m_mode);
		info.setMonitoringClients(m_monitoringClients);
		info.setStage(m_stageId, m_stageTitle);
		info.setStatus(m_status);
		info.setUserAction(m_userAction);
		info.setUserActionEnabled(m_userActionStatus.equals(UserActionStatus.ENABLED));
		info.setUserActionSeverity(m_userActionSeverity);
		info.setVisible(m_visible);
		info.setParent(m_parent);
		info.setOriginId(m_originId);
		info.setGroupId(m_groupId);
		info.setBackground(m_background);
		info.setParentCallingLine(m_parentCallingLine);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void visit(ExecutorConfig config)
	{
		config.setBrowsableLib(m_showLib);
		config.setExecDelay(m_execDelay);
		config.setRunInto(m_model.getExecutionManager().isRunInto());
		config.setStepByStep(m_byStep);
		config.setTcConfirmation(m_tcConfirmation);
		config.setPromptWarningDelay(m_promptWarningDelay);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void copyFrom(IExecutorInfo info)
	{
		setAutomatic(info.getAutomatic());
		setBlocking(info.getBlocking());
		setCondition(info.getCondition());
		setControllingClient(info.getControllingClient());
		if (getError() == null)
		{
			setError(info.getError());
		}
		else
		{
			if (info.getError() != null)
				setError(info.getError());
		}
		setBackground(info.isBackground());
		setMonitoringClients(info.getMonitoringClients());
		setStage(info.getStageId(), info.getStageTitle());
		setExecutorStatus(info.getStatus());
		setParent(info.getParent());
		setParentCallingLine(info.getParentCallingLine());
		m_asrunName = info.getAsRunName();
		m_timeId = info.getTimeId();
		m_groupId = info.getGroupId();
		m_originId = info.getOriginId();
		if (info.getUserAction() != null)
		{
			setUserActionStatus(info.getUserAction(), info.getUserActionEnabled() ? UserActionStatus.ENABLED : UserActionStatus.DISABLED, info.getUserActionSeverity());
		}
		else
		{
			setUserActionStatus(null, UserActionStatus.DISMISSED, null);
		}
		logUpdate();
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private void logUpdate()
	{
		try
		{
			Logger.debug("Updated information: " + m_model.getProcId(), Level.PROC, this);
			Logger.debug("   automatic  : " + m_automatic, Level.PROC, this);
			Logger.debug("   blocking   : " + m_blocking, Level.PROC, this);
			Logger.debug("   background : " + m_background, Level.PROC, this);
			Logger.debug("   condition  : " + m_condition, Level.PROC, this);
			Logger.debug("   controller : " + m_controllingClient, Level.PROC, this);
			Logger.debug("   error      : " + m_error, Level.PROC, this);
			if (m_monitoringClients != null)
			Logger.debug("   monitoring : " + m_monitoringClients.length, Level.PROC, this);
			Logger.debug("   stage      : " + m_stageTitle, Level.PROC, this);
			Logger.debug("   status     : " + m_status, Level.PROC, this);
			Logger.debug("   parent     : " + m_parent, Level.PROC, this);
			Logger.debug("   time ID    : " + m_timeId, Level.PROC, this);
			Logger.debug("   group ID   : " + m_groupId, Level.PROC, this);
			Logger.debug("   origin ID  : " + m_originId, Level.PROC, this);
			Logger.debug("   by step    : " + m_byStep, Level.PROC, this);
			Logger.debug("   exec delay : " + m_execDelay, Level.PROC, this);
			Logger.debug("   warn delay : " + m_promptWarningDelay, Level.PROC, this);
			Logger.debug("   TC confirm : " + m_tcConfirmation, Level.PROC, this);
			Logger.debug("   Action     : " + m_userAction + " (" + m_userActionSeverity + ")", Level.PROC, this);
			Logger.debug("   Action st  : " + m_userActionStatus, Level.PROC, this);
		}
		catch(Exception ignore){};
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void copyFrom(ExecutorConfig config)
	{
		setShowLib(config.getBrowsableLib());
		setExecutionDelay(config.getExecDelay());
		setPromptWarningDelay(config.getPromptWarningDelay());
		
		if (m_model.getExecutionManager() != null)
		{
			m_model.getExecutionManager().setRunInto(config.getRunInto());
		}
		setStepByStep(config.getStepByStep());
		setForceTcConfirmation(config.getTcConfirmation());

		logUpdate();
	}

	@Override
	/***************************************************************************
	 * NOTE: ClientMode keeps the same value
	 **************************************************************************/
	public void reset()
	{
		m_displayMessages.clear();
		m_error = null;
		m_status = ExecutorStatus.UNKNOWN;
		m_stageId = null;
		m_stageTitle = null;
		m_userAction = null;
		m_userActionStatus = UserActionStatus.DISMISSED;
		m_userActionSeverity = Severity.INFO;
		m_showLib = false;
		m_tcConfirmation = false;
		m_condition = null;
		m_controllingClient = null;
		m_monitoringClients = null;
		m_visible = true;
		m_automatic = true;
		m_blocking = true;
		m_waitingInput = false;
		m_background = false;
		m_parent = "";
		m_asrunName = "";
		m_timeId = "";
		m_originId = "";
		m_groupId = "";
		logUpdate();
	}
}
