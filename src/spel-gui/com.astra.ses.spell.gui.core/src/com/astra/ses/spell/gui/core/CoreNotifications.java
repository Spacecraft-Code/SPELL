///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core
// 
// FILE      : CoreNotifications.java
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
package com.astra.ses.spell.gui.core;

import java.util.ArrayList;
import java.util.Collection;

import com.astra.ses.spell.gui.core.interfaces.listeners.IBaseListener;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreApplicationStatusListener;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreClientOperationListener;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreContextOperationListener;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreProcedureInputListener;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreProcedureOperationListener;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreProcedureRuntimeListener;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreServerOperationListener;
import com.astra.ses.spell.gui.core.model.notification.ApplicationStatus;
import com.astra.ses.spell.gui.core.model.notification.ControlNotification;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.InputData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ExecutorConfig;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.types.ExecutorOperationSummary;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.types.ExecutorStatus;

public class CoreNotifications 
{
	private static CoreNotifications s_instance = null;

	private Collection<ICoreClientOperationListener> m_clientOperationListeners;
	private Collection<ICoreProcedureInputListener> m_procInputListeners;
	private Collection<ICoreProcedureOperationListener> m_procOperationListeners;
	private Collection<ICoreProcedureRuntimeListener> m_procRuntimeListeners;
	private Collection<ICoreServerOperationListener> m_serverOperationListeners;
	private Collection<ICoreContextOperationListener> m_contextOperationListeners;
	private Collection<ICoreApplicationStatusListener> m_applicationStatusListeners;

	/***************************************************************************
	 * Singleton accessor
	 * 
	 * @return The singleton instance
	 **************************************************************************/
	public static CoreNotifications get()
	{
		if (s_instance == null)
		{
			s_instance = new CoreNotifications();
		}
		return s_instance;
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	protected CoreNotifications()
	{
		m_clientOperationListeners = new ArrayList<ICoreClientOperationListener>();
		m_procInputListeners = new ArrayList<ICoreProcedureInputListener>();
		m_procOperationListeners = new ArrayList<ICoreProcedureOperationListener>();
		m_procRuntimeListeners = new ArrayList<ICoreProcedureRuntimeListener>();
		m_serverOperationListeners = new ArrayList<ICoreServerOperationListener>();
		m_contextOperationListeners = new ArrayList<ICoreContextOperationListener>();
		m_applicationStatusListeners = new ArrayList<ICoreApplicationStatusListener>();
	}

	/***************************************************************************
	 * Add listener
	 * @param listener
	 **************************************************************************/
	public void addListener( IBaseListener listener, Class<?> api )
	{
		Logger.debug("Register listener " + listener.getListenerId() + " for API " + api.getCanonicalName(), Level.PROC, this);
		if (api.equals(ICoreClientOperationListener.class))
		{
			if (!m_clientOperationListeners.contains(listener))
			{
				m_clientOperationListeners.add( (ICoreClientOperationListener) listener);
			}
		}
		else if (api.equals( ICoreProcedureInputListener.class))
		{
			if (!m_procInputListeners.contains(listener))
			{
				m_procInputListeners.add( (ICoreProcedureInputListener) listener);
			}
		}
		else if (api.equals( ICoreProcedureOperationListener.class))
		{
			if (!m_procOperationListeners.contains(listener))
			{
				m_procOperationListeners.add( (ICoreProcedureOperationListener) listener);
			}
		}
		else if (api.equals( ICoreProcedureRuntimeListener.class))
		{
			if (!m_procRuntimeListeners.contains(listener))
			{
				m_procRuntimeListeners.add( (ICoreProcedureRuntimeListener) listener);
			}
		}
		else if (api.equals( ICoreServerOperationListener.class))
		{
			if (!m_serverOperationListeners.contains(listener))
			{
				m_serverOperationListeners.add( (ICoreServerOperationListener) listener);
			}
		}
		else if (api.equals( ICoreContextOperationListener.class))
		{
			if (!m_contextOperationListeners.contains(listener))
			{
				m_contextOperationListeners.add( (ICoreContextOperationListener) listener);
			}
		}
		else if (api.equals( ICoreApplicationStatusListener.class))
		{
			if (!m_applicationStatusListeners.contains(listener))
			{
				m_applicationStatusListeners.add( (ICoreApplicationStatusListener) listener);
			}
		}
		else
		{
			Logger.error("Unknown listener class: " + listener, Level.PROC, this);
		}
	}

	/***************************************************************************
	 * Add listener
	 * @param listener
	 **************************************************************************/
	public void removeListener( IBaseListener listener )
	{
		Logger.debug("Remove listener " + listener.getListenerId(), Level.PROC, this);
		if (m_clientOperationListeners.contains(listener))
		{
			m_clientOperationListeners.remove(listener);
		}
		if (m_procInputListeners.contains(listener))
		{
			m_procInputListeners.remove(listener);
		}
		if (m_procOperationListeners.contains(listener))
		{
			m_procOperationListeners.remove(listener);
		}
		if (m_procRuntimeListeners.contains(listener))
		{
			m_procRuntimeListeners.remove(listener);
		}
		if (m_serverOperationListeners.contains(listener))
		{
			m_serverOperationListeners.remove(listener);
		}
		if (m_contextOperationListeners.contains(listener))
		{
			m_contextOperationListeners.remove(listener);
		}
		if (m_applicationStatusListeners.contains(listener))
		{
			m_applicationStatusListeners.remove(listener);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireClientConnected(String clientKey, String host)
	{
		for (ICoreClientOperationListener clt : m_clientOperationListeners)
		{
			Logger.debug("Notify [client connected] to " + clt.getListenerId(), Level.COMM, this);
			clt.notifyClientConnected(clientKey, host);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireClientDisconnected(String clientKey, String host)
	{
		for (ICoreClientOperationListener clt : m_clientOperationListeners)
		{
			Logger.debug("Notify [client disconnected] to " + clt.getListenerId(), Level.COMM, this);
			clt.notifyClientDisconnected(clientKey, host);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireContextAttached(ContextInfo info)
	{
		for (ICoreContextOperationListener clt : m_contextOperationListeners)
		{
			Logger.debug("Notify [context attached] to " + clt.getListenerId(), Level.COMM, this);
			clt.notifyContextAttached(info);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireContextDetached()
	{
		for (ICoreContextOperationListener clt : m_contextOperationListeners)
		{
			Logger.debug("Notify [context detached] to " + clt.getListenerId(), Level.COMM, this);
			clt.notifyContextDetached();
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireContextError(ErrorData data)
	{
		Logger.debug("Notify [context error]", Level.PROC, this);
		for (ICoreContextOperationListener clt : m_contextOperationListeners)
		{
			Logger.debug("Notify [context error] to " + clt.getListenerId(), Level.PROC, this);
			clt.notifyContextError(data);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void firePrompt(InputData inputData)
	{
		for (ICoreProcedureInputListener clt : m_procInputListeners)
		{
			clt.notifyProcedurePrompt(inputData);
			// Only once
			return;
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireFinishPrompt(InputData inputData)
	{
		for (ICoreProcedureInputListener clt : m_procInputListeners)
		{
			clt.notifyProcedureFinishPrompt(inputData);
			// Only once
			return;
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireCancelPrompt(InputData inputData)
	{
		for (ICoreProcedureInputListener clt : m_procInputListeners)
		{
			clt.notifyProcedureCancelPrompt(inputData);
			// Only once
			return;
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireProcedureOpen(String procId, String guiKey)
	{
		for (ICoreProcedureOperationListener clt : m_procOperationListeners)
		{
			clt.notifyRemoteProcedureOpen(procId, guiKey);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireProcedureClosed(String procId, String guiKey)
	{
		for (ICoreProcedureOperationListener clt : m_procOperationListeners)
		{
			clt.notifyRemoteProcedureClosed(procId, guiKey);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireProcedureKilled(String procId, String guiKey)
	{
		for (ICoreProcedureOperationListener clt : m_procOperationListeners)
		{
			clt.notifyRemoteProcedureKilled(procId, guiKey);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireProcedureCrashed(String procId, String guiKey)
	{
		for (ICoreProcedureOperationListener clt : m_procOperationListeners)
		{
			clt.notifyRemoteProcedureCrashed(procId, guiKey);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireProcedureControlled(String procId, String guiKey)
	{
		for (ICoreProcedureOperationListener clt : m_procOperationListeners)
		{
			clt.notifyRemoteProcedureControlled(procId, guiKey);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireProcedureMonitored(String procId, String guiKey)
	{
		for (ICoreProcedureOperationListener clt : m_procOperationListeners)
		{
			clt.notifyRemoteProcedureMonitored(procId, guiKey);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireProcedureReleased(String procId, String guiKey)
	{
		for (ICoreProcedureOperationListener clt : m_procOperationListeners)
		{
			clt.notifyRemoteProcedureReleased(procId, guiKey);
		}
	}
	
	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireProcedureSummary(String procId, ExecutorOperationSummary summary, String guiKey)
	{
		for (ICoreProcedureOperationListener clt : m_procOperationListeners)
		{
			clt.notifyRemoteProcedureSummary(procId, summary, guiKey);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireProcedureStatus(String procId, ExecutorStatus status, String guiKey)
	{
		for (ICoreProcedureOperationListener clt : m_procOperationListeners)
		{
			clt.notifyRemoteProcedureStatus(procId, status, guiKey);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireProcedureDisplay(DisplayData data)
	{
		for (ICoreProcedureRuntimeListener clt : m_procRuntimeListeners)
		{
			clt.notifyProcedureDisplay(data);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireProcedureError(ErrorData data)
	{
		for (ICoreProcedureRuntimeListener clt : m_procRuntimeListeners)
		{
			clt.notifyProcedureError(data);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireProcedureItem(ItemNotification data)
	{
		for (ICoreProcedureRuntimeListener clt : m_procRuntimeListeners)
		{
			clt.notifyProcedureItem(data);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireProcedureStack(StackNotification data)
	{
		for (ICoreProcedureRuntimeListener clt : m_procRuntimeListeners)
		{
			clt.notifyProcedureStack(data);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireProcedureStatus(StatusNotification data)
	{
		for (ICoreProcedureRuntimeListener clt : m_procRuntimeListeners)
		{
			clt.notifyProcedureStatus(data);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireProcedureControl(ControlNotification data)
	{
		for (ICoreProcedureRuntimeListener clt : m_procRuntimeListeners)
		{
			clt.notifyProcedureControl(data);
		}
	}

	/***************************************************************************
	 * Notify the ProcedureRuntime clients about the action to perform on demand
	 * by the user
	 * 
	 * @param newAction
	 **************************************************************************/
	public void fireProcedureUserAction(UserActionNotification data)
	{
		for (ICoreProcedureRuntimeListener clt : m_procRuntimeListeners)
		{
			clt.notifyProcedureUserAction(data);
		}
	}

	/***************************************************************************
	 * Notify the ProcedureRuntime clients about the configuration change
	 * 
	 * @param newAction
	 **************************************************************************/
	public void fireProcedureConfigured(ExecutorConfig data)
	{
		for (ICoreProcedureRuntimeListener clt : m_procRuntimeListeners)
		{
			clt.notifyProcedureConfiguration(data);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireListenerConnected(ServerInfo info)
	{
		for (ICoreServerOperationListener clt : m_serverOperationListeners)
		{
			Logger.debug("Notify [listener connected] to " + clt.getListenerId(), Level.COMM, this);
			clt.notifyListenerConnected(info);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireListenerDisconnected()
	{
		for (ICoreServerOperationListener clt : m_serverOperationListeners)
		{
			Logger.debug("Notify [listener disconnected] to " + clt.getListenerId(), Level.COMM, this);
			clt.notifyListenerDisconnected();
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireListenerError(ErrorData data)
	{
		for (ICoreServerOperationListener clt : m_serverOperationListeners)
		{
			Logger.debug("Notify [listener error] to " + clt.getListenerId(), Level.COMM, this);
			clt.notifyListenerError(data);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireContextStarted(ContextInfo info)
	{
		for (ICoreServerOperationListener clt : m_serverOperationListeners)
		{
			Logger.debug("Notify [context started] to " + clt.getListenerId(), Level.COMM, this);
			clt.notifyContextStarted(info);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void fireContextStopped(ContextInfo info)
	{
		for (ICoreServerOperationListener clt : m_serverOperationListeners)
		{
			Logger.debug("Notify [context stopped] to " + clt.getListenerId(), Level.COMM, this);
			clt.notifyContextStopped(info);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
    public void fireApplicationStatus( final ApplicationStatus status )
    {
		for(ICoreApplicationStatusListener listener : m_applicationStatusListeners)
		{
			listener.onApplicationStatus(status);
		}
    }
}
