///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.extensions
// 
// FILE      : GuiNotifications.java
//
// DATE      : Jul 1, 2014
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
package com.astra.ses.spell.gui.extensions;

import com.astra.ses.spell.gui.core.CoreNotifications;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreApplicationStatusListener;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreClientOperationListener;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreContextOperationListener;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreProcedureOperationListener;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreServerOperationListener;
import com.astra.ses.spell.gui.core.model.notification.ApplicationStatus;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.types.ExecutorOperationSummary;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.UnloadType;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.ProcedureNotifications;
import com.astra.ses.spell.gui.procs.extensionpoints.IProcedureListener;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureModelListener;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.types.ExecutorStatus;

public class GuiNotifications extends GuiListeners implements ICoreClientOperationListener,
															  ICoreContextOperationListener,
															  ICoreProcedureOperationListener,
															  ICoreServerOperationListener,
															  ICoreApplicationStatusListener,
															  IProcedureListener, 
															  IProcedureModelListener
{
	// Singleton
	private static GuiNotifications s_instance = null;

	/***************************************************************************
	 * 
	 * @param listener
	 **************************************************************************/
	public static GuiNotifications get()
	{
		if (s_instance == null)
		{
			s_instance = new GuiNotifications();
		}
		return s_instance;
	}

	private GuiNotifications()
	{
		super();
	}
	
	public void subscribe()
	{
		Logger.debug("Subscribing for core notifications", Level.PROC, this);
		CoreNotifications.get().addListener(this, ICoreClientOperationListener.class);
		CoreNotifications.get().addListener(this, ICoreServerOperationListener.class);
		CoreNotifications.get().addListener(this, ICoreContextOperationListener.class);
		CoreNotifications.get().addListener(this, ICoreProcedureOperationListener.class);
		
		CoreNotifications.get().addListener(this, ICoreApplicationStatusListener.class);
		
		ProcedureNotifications.get().addListener(this, IProcedureListener.class);
		ProcedureNotifications.get().addListener(this, IProcedureModelListener.class);
	}
	
	public void unsubscribe()
	{
		Logger.debug("Unsubscribing for core notifications", Level.PROC, this);
		CoreNotifications.get().removeListener(this);
		ProcedureNotifications.get().removeListener(this);
	}

	@Override
    public String getListenerId()
    {
	    return "GUI Notifications Bridge";
    }

	@Override
    public void notifyProcedureModelLoaded(IProcedure model)
    {
		fireModelLoaded(model);
    }

	@Override
    public void notifyProcedureModelUnloaded(IProcedure model, UnloadType type)
    {
	    fireModelUnloaded(model, type);
    }

	@Override
    public void notifyProcedureModelReset(IProcedure model)
    {
	    fireModelReset(model);
    }

	@Override
    public void notifyProcedureModelEnabled(IProcedure model)
    {
	    fireModelEnabled(model);
    }

	@Override
    public void notifyProcedureModelDisabled(IProcedure model)
    {
	    fireModelDisabled(model);
    }

	@Override
    public void notifyProcedureModelConfigured(IProcedure model)
    {
		fireModelConfigured(model);
    }

	@Override
    public void notifyProcedureDisplay(IProcedure model, DisplayData data)
    {
	    fireProcedureDisplay(model, data);
    }

	@Override
    public void notifyProcedureStatus(IProcedure model, StatusNotification data)
    {
	    fireProcedureStatus(model, data);
    }

	@Override
    public void notifyProcedureError(IProcedure model, ErrorData data)
    {
	    fireProcedureError(model, data);
    }

	@Override
    public void notifyProcedureItem(IProcedure model, ItemNotification data)
    {
	    fireProcedureItem(model, data);
    }

	@Override
    public void notifyProcedureStack(IProcedure model, StackNotification data)
    {
	    fireProcedureStack(model, data);
    }

	@Override
    public void notifyProcedureUserAction(IProcedure model, UserActionNotification data)
    {
	    fireProcedureUserAction(model, data);
    }

	@Override
    public void notifyProcedurePrompt(IProcedure model)
    {
	    fireProcedurePrompt(model);
    }

	@Override
    public void notifyProcedureFinishPrompt(IProcedure model)
    {
	    fireProcedureFinishPrompt(model);
    }

	@Override
    public void notifyProcedureCancelPrompt(IProcedure model)
    {
	    fireProcedureCancelPrompt(model);
	}

	@Override
    public void notifyListenerConnected(ServerInfo info)
    {
	    fireListenerConnected(info);
    }

	@Override
    public void notifyListenerError(ErrorData error)
    {
	    fireListenerError(error);
    }

	@Override
    public void notifyListenerDisconnected()
    {
		fireListenerDisconnected();
    }

	@Override
    public void notifyContextStarted(ContextInfo info)
    {
	    fireContextStarted(info);
    }

	@Override
    public void notifyContextStopped(ContextInfo info)
    {
	    fireContextStopped(info);
    }

	@Override
    public void notifyRemoteProcedureOpen(String procId, String guiKey)
    {
	    fireProcedureOpen(procId, guiKey);
    }

	@Override
    public void notifyRemoteProcedureClosed(String procId, String guiKey)
    {
	    fireProcedureClosed(procId, guiKey);
    }

	@Override
    public void notifyRemoteProcedureKilled(String procId, String guiKey)
    {
	    fireProcedureKilled(procId, guiKey);
    }

	@Override
    public void notifyRemoteProcedureCrashed(String procId, String guiKey)
    {
	    fireProcedureCrashed(procId, guiKey);
    }

	@Override
    public void notifyRemoteProcedureControlled(String procId, String guiKey)
    {
	    fireProcedureControlled(procId, guiKey);
    }

	@Override
    public void notifyRemoteProcedureMonitored(String procId, String guiKey)
    {
	    fireProcedureMonitored(procId, guiKey);
    }

	@Override
    public void notifyRemoteProcedureReleased(String procId, String guiKey)
    {
	    fireProcedureReleased(procId, guiKey);
    }

	@Override
    public void notifyRemoteProcedureStatus(String procId, ExecutorStatus status, String guiKey)
    {
	    fireProcedureStatus(procId, status, guiKey);
    }

	@Override
    public void notifyRemoteProcedureSummary(String procId, ExecutorOperationSummary summary, String guiKey)
    {
	    fireProcedureSummary(procId, summary, guiKey);
    }

	@Override
    public void notifyContextAttached(ContextInfo ctx)
    {
	    fireContextAttached(ctx);
    }

	@Override
    public void notifyContextDetached()
    {
	    fireContextDetached();
    }

	@Override
    public void notifyContextError(ErrorData error)
    {
	    fireContextError(error);
    }

	@Override
    public void notifyClientConnected(String clientKey, String host)
    {
	    fireClientConnected(clientKey, host);
    }

	@Override
    public void notifyClientDisconnected(String clientKey, String host)
    {
	    fireClientDisconnected(clientKey, host);
    }

	@Override
    public void onApplicationStatus(ApplicationStatus status)
    {
	    fireApplicationStatus(status);
    }
}
