///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.master
// 
// FILE      : ExecutorsModel.java
//
// DATE      : Jun 26, 2014
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
package com.astra.ses.spell.gui.views.controls.master.executors;

import java.util.LinkedList;
import java.util.List;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

public class ExecutorsModel
{
	private static IProcedureManager s_pMgr = null;
	private List<IProcedure> m_models;
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public ExecutorsModel()
	{
		if (s_pMgr == null)
		{
			s_pMgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
		}
	    m_models = new LinkedList<IProcedure>();
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void refresh()
	{
		Logger.debug("Refresh executors model", Level.GUI, this);
		m_models.clear();
	    if (s_pMgr.canOperate())
	    {
	    	List<String> currentOpenLocalProcs = new LinkedList<String>();
	    	currentOpenLocalProcs.addAll(s_pMgr.getOpenLocalProcedures());

			Logger.debug("Local procedures: " + currentOpenLocalProcs.size(), Level.GUI, this);

		    for(String id : currentOpenLocalProcs) 
		    {
		    	m_models.add(s_pMgr.getProcedure(id));
		    }
		    
	    	List<String> currentOpenRemoteProcs = new LinkedList<String>();
	    	currentOpenRemoteProcs.addAll(s_pMgr.getOpenRemoteProcedures(false));

			Logger.debug("Remote procedures: " + currentOpenRemoteProcs.size(), Level.GUI, this);

		    for(String id : currentOpenRemoteProcs) 
		    {
		    	m_models.add(s_pMgr.getRemoteProcedure(id));
		    }
	    }
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public IProcedure elementAt( int index )
	{
		if (index>=0 && index<m_models.size())
		{
			return m_models.get(index);
		}
		return null;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public IProcedure[] getElements()
	{
		return m_models.toArray( new IProcedure[0] );  
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean contains( IProcedure element )
	{
		return m_models.contains(element);
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void refresh( IProcedure element )
	{
		Logger.debug("Refresh single model: " + element.getProcId(), Level.GUI, this);

		int index = m_models.indexOf(element);
		if (index != -1)
		{
			IProcedure model = null;
			if (s_pMgr.isLocallyLoaded(element.getProcId()))
			{
				Logger.debug("Is locally loaded", Level.GUI, this);
				model = s_pMgr.getProcedure(element.getProcId());	
			}
			else
			{
				Logger.debug("Is remotely loaded", Level.GUI, this);
				model = s_pMgr.getRemoteProcedure(element.getProcId());
			}
			if (model != null)
			{
				try
                {
					Logger.debug("Refresh executor information for executors model", Level.GUI, this);
	                model.getController().refresh();
                }
                catch (Exception e)
                {
	                e.printStackTrace();
                }
			}
			else
			{
				Logger.debug("Remove executor from model", Level.GUI, this);
				m_models.remove(element);
			}
		}
	}
}
