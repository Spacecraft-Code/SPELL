///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.presentations
// 
// FILE      : PresentationNotifier.java
//
// DATE      : 2008-11-24 08:34
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
package com.astra.ses.spell.gui.views.presentations;

import java.util.ArrayList;

import com.astra.ses.spell.gui.core.model.notification.ControlNotification;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.InputData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification;
import com.astra.ses.spell.gui.core.model.server.ExecutorConfig;
import com.astra.ses.spell.gui.interfaces.IPresentationNotifier;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureItemsListener;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureMessageListener;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedurePromptListener;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureRuntimeListener;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureStackListener;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureStatusListener;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

/*******************************************************************************
 * @brief Manages procedure presentations for a view
 * @date 09/10/07
 ******************************************************************************/
public class PresentationNotifier implements IPresentationNotifier
        
{
	public static final String	                 ID	= "com.astra.ses.spell.gui.views.models.PresentationNotifier";

	/** Item listeners */
	private ArrayList<IGuiProcedureItemsListener>	 m_itemsListeners;
	/** Message listeners */
	private ArrayList<IGuiProcedureMessageListener>	m_msgListeners;
	/** Status listeners */
	private ArrayList<IGuiProcedureStatusListener>	 m_statusListeners;
	/** Runtime listeners */
	private ArrayList<IGuiProcedureRuntimeListener>	m_runtimeListeners;
	/** Stack listeners */
	private ArrayList<IGuiProcedureStackListener>	 m_stackListeners;
	/** Prompt listeners */
	private ArrayList<IGuiProcedurePromptListener>	 m_promptListeners;
	/** Holds reference to the procedure model */
	private IProcedure	                         m_model;

	/***************************************************************************
	 * Constructor.
	 **************************************************************************/
	public PresentationNotifier(IProcedure model)
	{
		m_model = model;
		m_itemsListeners = new ArrayList<IGuiProcedureItemsListener>();
		m_msgListeners = new ArrayList<IGuiProcedureMessageListener>();
		m_statusListeners = new ArrayList<IGuiProcedureStatusListener>();
		m_runtimeListeners = new ArrayList<IGuiProcedureRuntimeListener>();
		m_stackListeners = new ArrayList<IGuiProcedureStackListener>();
		m_promptListeners = new ArrayList<IGuiProcedurePromptListener>();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void addMessageListener(IGuiProcedureMessageListener listener)
	{
		m_msgListeners.add(listener);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void removeMessageListener(IGuiProcedureMessageListener listener)
	{
		m_msgListeners.remove(listener);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void addStatusListener(IGuiProcedureStatusListener listener)
	{
		m_statusListeners.add(listener);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void removeStatusListener(IGuiProcedureStatusListener listener)
	{
		m_statusListeners.remove(listener);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void addItemListener(IGuiProcedureItemsListener listener)
	{
		m_itemsListeners.add(listener);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void removeItemListener(IGuiProcedureItemsListener listener)
	{
		m_itemsListeners.remove(listener);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void addRuntimeListener(IGuiProcedureRuntimeListener listener)
	{
		m_runtimeListeners.add(listener);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void removeRuntimeListener(IGuiProcedureRuntimeListener listener)
	{
		m_runtimeListeners.remove(listener);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void addStackListener(IGuiProcedureStackListener listener)
	{
		m_stackListeners.add(listener);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void removeStackListener(IGuiProcedureStackListener listener)
	{
		m_stackListeners.remove(listener);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void addPromptListener(IGuiProcedurePromptListener listener)
	{
		m_promptListeners.add(listener);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void removePromptListener(IGuiProcedurePromptListener listener)
	{
		m_promptListeners.remove(listener);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getListenerId()
	{
		return ID;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureDisplay(DisplayData data)
	{
		for (IGuiProcedureMessageListener listener : m_msgListeners)
		{
			listener.notifyDisplay(m_model, data);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureError(ErrorData data)
	{
		for (IGuiProcedureStatusListener listener : m_statusListeners)
		{
			listener.notifyError(m_model, data);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureItem(ItemNotification data)
	{
		for (IGuiProcedureItemsListener listener : m_itemsListeners)
		{
			listener.notifyItem(m_model, data);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureStack(StackNotification data)
	{
		for (IGuiProcedureStackListener listener : m_stackListeners)
		{
			listener.notifyStack(m_model, data);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureStatus(StatusNotification data)
	{
		for (IGuiProcedureStatusListener listener : m_statusListeners)
		{
			listener.notifyStatus(m_model, data);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureUserAction(UserActionNotification data)
	{
		// Not issued to presentations
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureConfiguration( ExecutorConfig data )
	{
		// Not issued to presentations
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyModelDisabled()
	{
		for (IGuiProcedureRuntimeListener listener : m_runtimeListeners)
		{
			listener.notifyModelDisabled(m_model);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyModelEnabled()
	{
		for (IGuiProcedureRuntimeListener listener : m_runtimeListeners)
		{
			listener.notifyModelEnabled(m_model);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyModelLoaded()
	{
		for (IGuiProcedureRuntimeListener listener : m_runtimeListeners)
		{
			listener.notifyModelLoaded(m_model);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyModelReset()
	{
		for (IGuiProcedureRuntimeListener listener : m_runtimeListeners)
		{
			listener.notifyModelReset(m_model);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyModelUnloaded()
	{
		for (IGuiProcedureRuntimeListener listener : m_runtimeListeners)
		{
			listener.notifyModelUnloaded(m_model);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyModelConfigured()
	{
		for (IGuiProcedureRuntimeListener listener : m_runtimeListeners)
		{
			listener.notifyModelConfigured(m_model);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedurePrompt( InputData inputData )
	{
		for (IGuiProcedurePromptListener listener : m_promptListeners)
		{
			listener.notifyPrompt(m_model);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureFinishPrompt( InputData inputData )
	{
		for (IGuiProcedurePromptListener listener : m_promptListeners)
		{
			listener.notifyFinishPrompt(m_model);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureCancelPrompt( InputData inputData )
	{
		for (IGuiProcedurePromptListener listener : m_promptListeners)
		{
			listener.notifyCancelPrompt(m_model);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
    @Override
    public void notifyProcedureControl(ControlNotification data)
    {
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void clearNotifications()
    {
	    // Nothing to do
    }

}
