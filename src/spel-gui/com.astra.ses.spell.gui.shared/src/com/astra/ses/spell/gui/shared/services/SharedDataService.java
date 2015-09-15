///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.shared.services
// 
// FILE      : SharedDataService.java
//
// DATE      : Sep 19, 2013
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
package com.astra.ses.spell.gui.shared.services;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.interfaces.BaseService;
import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.shared.messages.SPELLmessageAddSharedVariableScope;
import com.astra.ses.spell.gui.shared.messages.SPELLmessageGetSharedVariableScopes;
import com.astra.ses.spell.gui.shared.messages.SPELLmessageRemoveSharedVariableScope;

/*******************************************************************************
 * 
 * SharedDataService will handle propeties which are only valid during
 * application lifecycle. That means that they won't be stored as preferences
 * are being made.
 ******************************************************************************/
public class SharedDataService extends BaseService implements ISharedDataService
{
	/** Service identifier */
	public static final String	                ID	= "com.astra.ses.spell.gui.shareddata";
	

	private static IContextProxy s_proxy = null;
	
	private Map<String,ISharedScope> m_tables;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public SharedDataService()
	{
		super(ID);
		m_tables = new TreeMap<String,ISharedScope>();
		if (s_proxy == null)
		{
			s_proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void cleanup()
	{
		m_tables.clear();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setup()
	{
		SharedScope global = new SharedScope(GLOBAL_SCOPE,s_proxy);
		m_tables.put(GLOBAL_SCOPE,global);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void subscribe()
	{
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public ISharedScope getSharedScope( String scope )
	{
	    if (m_tables.containsKey(scope))
	    {
		    return m_tables.get(scope);
	    }
	    return null;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public List<String> getSharedScopes()
	{
		List<String> list = new LinkedList<String>();
		list.addAll(m_tables.keySet());
		return list;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void removeSharedScope( String scope )
	{
		if (scope.equals(GLOBAL_SCOPE)) return;
	    if (m_tables.containsKey(scope))
	    {
			SPELLmessage msg = new SPELLmessageRemoveSharedVariableScope( s_proxy.getClientKey(), scope);
			SPELLmessage response;
	        try
	        {
		        response = s_proxy.sendRequest(msg);
				if (response != null)
				{
					if (SPELLmessageRemoveSharedVariableScope.isSuccess(response))
					{
						Logger.info("Remove shared scope " + scope, Level.PROC, this);
						m_tables.remove(scope);
					}
				}
	        }
	        catch(Exception ex)
	        {
	        	ex.printStackTrace();
	        }
	    }
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void addSharedScope( String scope )
	{
		if (scope.equals(GLOBAL_SCOPE)) return;
	    if (!m_tables.containsKey(scope))
	    {
			SPELLmessage msg = new SPELLmessageAddSharedVariableScope( s_proxy.getClientKey(), scope);
			SPELLmessage response;
	        try
	        {
		        response = s_proxy.sendRequest(msg);
				if (response != null)
				{
					if (SPELLmessageAddSharedVariableScope.isSuccess(response))
					{
				    	Logger.info("Add shared scope " + scope, Level.PROC, this);
				    	ISharedScope scopeObj = new SharedScope(scope,s_proxy);
				    	m_tables.put(scope,scopeObj);
					}
				}
	        }
	        catch(Exception ex)
	        {
	        	ex.printStackTrace();
	        }
	    }
	}

	@Override
    public void update(IProgressMonitor monitor)
    {
		List<String> scopes = null;
		SPELLmessage msg = new SPELLmessageGetSharedVariableScopes();
		SPELLmessage response;
        try
        {
	        response = s_proxy.sendRequest(msg);
			if (response != null)
			{
				scopes = SPELLmessageGetSharedVariableScopes.getScopes(response);
			}
			if (scopes != null)
			{
				scopes.add(GLOBAL_SCOPE);
				// Create local model scope tables if needed
				for(String scope : scopes)
				{
					if (!m_tables.containsKey(scope))
					{
				    	SharedScope table = new SharedScope(scope,s_proxy);
				    	m_tables.put(scope, table);
				    	Logger.debug("Added new scope during update: " + scope, Level.PROC, this);
					}
					if (monitor.isCanceled()) return; 
				}
				// Clean local model scope tables if needed
				List<String> toRemove = new LinkedList<String>();
				for(String scope : m_tables.keySet())
				{
					if (!scopes.contains(scope))
					{
						toRemove.add(scope);
				    	Logger.debug("Remove scope during update: " + scope, Level.PROC, this);
					}
					if (monitor.isCanceled()) return; 
				}
				for(String scope : toRemove)
				{
					m_tables.remove(scope);
					if (monitor.isCanceled()) return; 
				}
			}
			
			// Once the set of scopes is aligned, update the tables
			for(ISharedScope table : m_tables.values())
			{
		    	Logger.debug("Updating scope: " + table.getScopeName(), Level.PROC, this);
				table.update(monitor);
				if (monitor.isCanceled()) return; 
			}
        }
        catch (Exception e)
        {
	        e.printStackTrace();
        }
    }
}
