////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.watchvariables.service
// 
// FILE      : VariableManager.java
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
package com.astra.ses.spell.gui.watchvariables.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.types.ExecutorStatus;
import com.astra.ses.spell.gui.watchvariables.interfaces.IVariableListener;
import com.astra.ses.spell.gui.watchvariables.interfaces.IVariableManager;
import com.astra.ses.spell.gui.watchvariables.interfaces.IVariableView;
import com.astra.ses.spell.gui.watchvariables.interfaces.IWatchVariablesProxy;
import com.astra.ses.spell.gui.watchvariables.notification.VariableData;
import com.astra.ses.spell.gui.watchvariables.notification.VariableNotification;

/*******************************************************************************
 * 
 * Variable manager
 * 
 ******************************************************************************/
public class VariableManager implements IVariableManager, IVariableListener
{

	private IProcedure						  m_procedure;
	/** Holds the list of listeners */
	private ArrayList<IVariableView>          m_listeners;
	/** Holds the list of currently used variables */
	private Map<String,VariableData>     	  m_variables;
	/** Watch variables proxy */
	private IWatchVariablesProxy m_proxy = null;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public VariableManager( IProcedure proc, IWatchVariablesProxy proxy )
	{
		m_proxy = proxy;
		m_procedure = proc;
		m_listeners = new ArrayList<IVariableView>();
		m_variables = new TreeMap<String,VariableData>();
		m_proxy.addListener(proc.getProcId(), this);
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public Map<String,VariableData> getVariables()
	{
		return m_variables;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void updateModel( IProgressMonitor monitor )
	{
		if (!checkValidStatus()) return;
		try
		{
			m_variables = m_proxy.retrieveVariables(m_procedure.getProcId(), monitor);
		}
		catch(Exception ex)
		{
			monitor.setCanceled(true);
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Cannot retrieve procedure variables", ex.getLocalizedMessage());
			ex.printStackTrace();
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void setEnabled( boolean enable )
	{
		if (!checkValidStatus()) return;
		try
		{
			m_proxy.setEnabled(m_procedure.getProcId(), enable);
		}
		catch(Exception ex)
		{
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Cannot enable or disable the variable watch mechanism", ex.getLocalizedMessage());
			ex.printStackTrace();
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public boolean isEnabled()
	{
		try
		{
			return m_proxy.isEnabled(m_procedure.getProcId());
		}
		catch(Exception ex)
		{
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Cannot check if the variable watch mechanism is enabled", ex.getLocalizedMessage());
			ex.printStackTrace();
		}
		return false;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void addListener(IVariableView listener)
	{
		m_listeners.add(listener);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void removeListener(IVariableView listener)
	{
		m_listeners.remove(listener);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public boolean changeVariable(String varName, String valueExpression,
	        boolean global)
	{
		boolean result = true;
		try
		{
			m_proxy.changeVariable(m_procedure.getProcId(), varName, valueExpression, global);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			result = false;
		}
		return result;
	}

	/***************************************************************************
	 * Check if the current {@link ExecutorStatus} is valid for requesting
	 * variables through the proxy
	 * 
	 * @return
	 **************************************************************************/
	private boolean checkValidStatus()
	{
		boolean valid = true;

		ExecutorStatus st = m_procedure.getRuntimeInformation().getStatus();
		switch (st)
		{
		case RUNNING:
		case WAITING:
		case LOADED:
		case RELOADING:
		case UNINIT:
		case UNKNOWN:
			valid = false;
			break;
		default:
			valid = true;
		}

		return valid;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void variableChanged(VariableNotification data)
    {
		if (m_variables == null) m_variables = new TreeMap<String,VariableData>();
		List<VariableData> added = new LinkedList<VariableData>();
		List<VariableData> updated = new LinkedList<VariableData>();
		List<VariableData> removed = new LinkedList<VariableData>();
		for(VariableData var : data.getAddedVariables())
		{
			m_variables.put(var.getName(),var);
			added.add(var);
		}
		for(VariableData var : data.getChangedVariables())
		{
			m_variables.get(var.getName()).updateFrom(var);
			updated.add(m_variables.get(var.getName()));
		}
		for(VariableData var : data.getDeletedVariables())
		{
			removed.add(m_variables.remove(var.getName()));
		}
	    notifyVariableChange(added,updated,removed);
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void scopeChanged(VariableNotification data)
    {
		try
        {
			if (m_variables == null)
			{
				m_variables = new TreeMap<String,VariableData>();
			}
			else
			{
				m_variables.clear();
			}
			for(VariableData var : data.getChangedVariables())
			{
				m_variables.put(var.getName(),var);
			}
			notifyScopeChange(data);
        }
        catch (Exception e)
        {
	        e.printStackTrace();
        }
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void connectionLost()
    {
	    m_variables = null;
	    notifyConnectionLost();
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void notifyScopeChange(VariableNotification data)
	{
		for (IVariableView listener : m_listeners)
		{
			listener.scopeChanged(data);
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void notifyVariableChange( List<VariableData> added, List<VariableData> updated, List<VariableData> removed)
	{
		for (IVariableView listener : m_listeners)
		{
			listener.variablesChanged(added,updated,removed);
		}
	}


	/**************************************************************************
	 * 
	 *************************************************************************/
	private void notifyConnectionLost()
	{
		for (IVariableView listener : m_listeners)
		{
			listener.connectionLost();
		}
	}
}
