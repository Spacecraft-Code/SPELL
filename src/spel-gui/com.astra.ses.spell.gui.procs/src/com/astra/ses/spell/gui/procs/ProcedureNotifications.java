///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs
// 
// FILE      : ProcedureNotifications.java
//
// DATE      : 2008-11-21 08:55
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
package com.astra.ses.spell.gui.procs;

import java.util.ArrayList;
import java.util.Collection;

import com.astra.ses.spell.gui.core.interfaces.listeners.IBaseListener;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.UnloadType;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.extensionpoints.IProcedureListener;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureModelListener;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

public class ProcedureNotifications
{
	private static ProcedureNotifications s_instance = null;

	private Collection<IProcedureListener> m_procedureListeners;
	private Collection<IProcedureModelListener> m_procedureModelListeners;

	/***************************************************************************
	 * Singleton accessor
	 * 
	 * @return The singleton instance
	 **************************************************************************/
	public static ProcedureNotifications get()
	{
		if (s_instance == null)
		{
			s_instance = new ProcedureNotifications();
		}
		return s_instance;
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	protected ProcedureNotifications()
	{
		m_procedureListeners = new ArrayList<IProcedureListener>();
		m_procedureModelListeners = new ArrayList<IProcedureModelListener>();
	}

	/***************************************************************************
	 * Add listener
	 **************************************************************************/
	public void addListener( IBaseListener listener, Class<?> api )
	{
		if (api.equals(IProcedureListener.class))
		{
			if (!m_procedureListeners.contains(listener))
			{
				m_procedureListeners.add( (IProcedureListener) listener);
			}
		}
		else if (api.equals(IProcedureModelListener.class))
		{
			if (!m_procedureModelListeners.contains(listener))
			{
				m_procedureModelListeners.add( (IProcedureModelListener) listener);
			}
		}
		else
		{
			Logger.error("Unknown listener class: " + listener, Level.PROC, this);
		}
	}

	/***************************************************************************
	 * Remove listener
	 **************************************************************************/
	public void removeListener(IBaseListener listener)
	{
		if (m_procedureListeners.contains(listener))
		{
			m_procedureListeners.remove(listener);
		}
		if (m_procedureModelListeners.contains(listener))
		{
			m_procedureModelListeners.remove(listener);
		}
	}

	/***************************************************************************
	 * Fire prompt event corresponding to ICoreProcedureInputListener extensions
	 * 
	 * @param inputData
	 *            Event information
	 **************************************************************************/
	public void firePrompt(IProcedure model)
	{
		for (IProcedureListener listener : m_procedureListeners)
		{
			listener.notifyProcedurePrompt(model);
			return;
		}
	}

	/***************************************************************************
	 * Fire prompt finish event corresponding to ICoreProcedureInputListener
	 * extensions
	 * 
	 * @param inputData
	 *            Event information
	 **************************************************************************/
	public void fireFinishPrompt(IProcedure model)
	{
		for (IProcedureListener listener : m_procedureListeners)
		{
			listener.notifyProcedureFinishPrompt(model);
			return;
		}
	}

	/***************************************************************************
	 * Fire prompt cancel event corresponding to ICoreProcedureInputListener
	 * extensions
	 * 
	 * @param inputData
	 *            Event information
	 **************************************************************************/
	public void fireCancelPrompt(IProcedure model)
	{
		for (IProcedureListener listener : m_procedureListeners)
		{
			listener.notifyProcedureCancelPrompt(model);
			return;
		}
	}

	/***************************************************************************
	 * Fire display event corresponding to IProcedureRuntime extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireProcedureDisplay(IProcedure model, DisplayData data)
	{
		for (IProcedureListener listener : m_procedureListeners)
		{
			listener.notifyProcedureDisplay(model, data);
		}
	}

	/***************************************************************************
	 * Fire error event corresponding to IProcedureRuntime extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireProcedureError(IProcedure model, ErrorData data)
	{
		for (IProcedureListener listener : m_procedureListeners)
		{
			listener.notifyProcedureError(model, data);
		}
	}

	/***************************************************************************
	 * Fire item event corresponding to IProcedureRuntime extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireProcedureItem(IProcedure model, ItemNotification data)
	{
		for (IProcedureListener listener : m_procedureListeners)
		{
			listener.notifyProcedureItem(model, data);
		}
	}

	/***************************************************************************
	 * Fire stack event corresponding to IProcedureRuntime extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireProcedureStack(IProcedure model, StackNotification data)
	{
		for (IProcedureListener listener : m_procedureListeners)
		{
			listener.notifyProcedureStack(model, data);
		}
	}

	/***************************************************************************
	 * Fire status event corresponding to IProcedureRuntime extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireProcedureStatus(IProcedure model, StatusNotification data)
	{
		for (IProcedureListener listener : m_procedureListeners)
		{
			listener.notifyProcedureStatus(model, data);
		}
	}

	/***************************************************************************
	 * Fire user action event corresponding to IProcedureRuntime extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireProcedureUserAction(IProcedure model, UserActionNotification data)
	{
		for (IProcedureListener listener : m_procedureListeners)
		{
			listener.notifyProcedureUserAction(model, data);
		}
	}

	/***************************************************************************
	 * Fire configuration event corresponding to IProcedureRuntime extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireProcedureConfiguration(IProcedure model)
	{
		for (IProcedureModelListener listener : m_procedureModelListeners)
		{
			listener.notifyProcedureModelConfigured(model);
		}
	}

	/***************************************************************************
	 * Fire model loaded event corresponding to IProcedureView extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireModelLoaded(IProcedure model)
	{
		for (IProcedureModelListener listener : m_procedureModelListeners)
		{
			listener.notifyProcedureModelLoaded(model);
		}
	}

	/***************************************************************************
	 * Fire model unloaded event corresponding to IProcedureView extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireModelUnloaded(IProcedure model, UnloadType type)
	{
		for (IProcedureModelListener listener : m_procedureModelListeners)
		{
			listener.notifyProcedureModelUnloaded(model, type);
		}
	}

	/***************************************************************************
	 * Fire model configured event corresponding to IProcedureView extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireModelConfigured(IProcedure model)
	{
		for (IProcedureModelListener listener : m_procedureModelListeners)
		{
			listener.notifyProcedureModelConfigured(model);
		}
	}

	/***************************************************************************
	 * Fire model enabled event corresponding to IProcedureView extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireModelEnabled(IProcedure model)
	{
		for (IProcedureModelListener listener : m_procedureModelListeners)
		{
			listener.notifyProcedureModelEnabled(model);
		}
	}

	/***************************************************************************
	 * Fire model disabled event corresponding to IProcedureView extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireModelDisabled(IProcedure model)
	{
		for (IProcedureModelListener listener : m_procedureModelListeners)
		{
			listener.notifyProcedureModelDisabled(model);
		}
	}

	/***************************************************************************
	 * Fire model reset event corresponding to IProcedureView extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireModelReset(IProcedure model)
	{
		for (IProcedureModelListener listener : m_procedureModelListeners)
		{
			listener.notifyProcedureModelReset(model);
		}
	}

}
