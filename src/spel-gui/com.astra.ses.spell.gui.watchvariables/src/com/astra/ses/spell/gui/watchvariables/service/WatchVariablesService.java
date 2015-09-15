///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.watchvariables.service
// 
// FILE      : WatchVariablesService.java
//
// DATE      : Nov 28, 2011
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
package com.astra.ses.spell.gui.watchvariables.service;

import java.util.Map;
import java.util.TreeMap;

import com.astra.ses.spell.gui.core.interfaces.BaseService;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.UnloadType;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.ProcedureNotifications;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureModelListener;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.watchvariables.interfaces.IVariableManager;
import com.astra.ses.spell.gui.watchvariables.interfaces.IWatchVariables;
import com.astra.ses.spell.gui.watchvariables.interfaces.IWatchVariablesProxy;

/*******************************************************************************
 * @brief Provides access to the SPEL context services
 * @date 20/05/08
 ******************************************************************************/
public class WatchVariablesService extends BaseService implements IWatchVariables, IProcedureModelListener
{
	private IWatchVariablesProxy m_proxy;
	private IProcedureManager m_procMgr = null;
	private Map<String,IVariableManager> m_managers;
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public WatchVariablesService()
	{
		super(ID);
		m_managers = new TreeMap<String,IVariableManager>();
		m_proxy = new WatchVariablesProxy();
		Logger.debug("Created", Level.INIT, this);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void setup()
	{
		super.setup();
		m_procMgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
		m_proxy.setup();
		ProcedureNotifications.get().addListener(this, IProcedureModelListener.class);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void cleanup()
	{
		super.cleanup();
		m_proxy.cleanup();
		ProcedureNotifications.get().removeListener(this);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public IVariableManager getVariableManager( String procId )
	{
		createVariableManager(procId);
		return m_managers.get(procId);
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	private void createVariableManager( String procId )
	{
		if (!m_managers.containsKey(procId))
		{
			Logger.debug("Create new variable manager: " + procId, Level.PROC, this);
			IVariableManager mgr = new VariableManager(m_procMgr.getProcedure(procId), m_proxy );
			m_managers.put(procId, mgr);
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void createVariableManager( IProcedure model )
	{
		if (!m_managers.containsKey(model.getProcId()))
		{
			Logger.debug("Create new variable manager: " + model.getProcId(), Level.PROC, this);
			IVariableManager mgr = new VariableManager(model, m_proxy );
			m_managers.put(model.getProcId(), mgr);
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void removeVariableManager( IProcedure model )
	{
		if (m_managers.containsKey(model.getProcId()))
		{
			Logger.debug("Remove variable manager: " + model.getProcId(), Level.PROC, this);
			m_managers.remove(model.getProcId());
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public void removeVariableManager( String procId )
	{
		if (m_managers.containsKey(procId))
		{
			Logger.debug("Remove variable manager: " + procId, Level.PROC, this);
			m_managers.remove(procId);
		}
	}

	@Override
    public void notifyProcedureModelLoaded(IProcedure model)
    {
	    createVariableManager(model);
    }

	@Override
    public void notifyProcedureModelUnloaded(IProcedure model, UnloadType type)
    {
	    removeVariableManager(model);
    }

	@Override
    public void notifyProcedureModelReset(IProcedure model) {}

	@Override
    public void notifyProcedureModelEnabled(IProcedure model) {}

	@Override
    public void notifyProcedureModelDisabled(IProcedure model) {}

	@Override
    public void notifyProcedureModelConfigured(IProcedure model) {}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public String getListenerId()
    {
	    return "Watch Variables Service";
    }
}
