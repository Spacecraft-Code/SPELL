///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.watchvariables.service
// 
// FILE      : WatchVariablesProxy.java
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

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageRequest;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageResponse;
import com.astra.ses.spell.gui.core.interfaces.ICommListener;
import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.server.TransferData;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.watchvariables.interfaces.IVariableListener;
import com.astra.ses.spell.gui.watchvariables.interfaces.IWatchVariablesProxy;
import com.astra.ses.spell.gui.watchvariables.messages.IWVMessageId;
import com.astra.ses.spell.gui.watchvariables.messages.SPELLmessageChangeVariable;
import com.astra.ses.spell.gui.watchvariables.messages.SPELLmessageCheckVariablesEnabled;
import com.astra.ses.spell.gui.watchvariables.messages.SPELLmessageGetVariables;
import com.astra.ses.spell.gui.watchvariables.messages.SPELLmessageScopeChange;
import com.astra.ses.spell.gui.watchvariables.messages.SPELLmessageSetVariablesDisabled;
import com.astra.ses.spell.gui.watchvariables.messages.SPELLmessageSetVariablesEnabled;
import com.astra.ses.spell.gui.watchvariables.messages.SPELLmessageVariableChange;
import com.astra.ses.spell.gui.watchvariables.notification.VariableData;
import com.astra.ses.spell.gui.watchvariables.notification.VariableNotification;

public class WatchVariablesProxy implements IWatchVariablesProxy, ICommListener
{
	private IContextProxy m_ctxProxy = null;
	private Map<String,IVariableListener> m_listeners;
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public WatchVariablesProxy()
	{
		Logger.debug("Created", Level.INIT, this);
		m_listeners = new TreeMap<String,IVariableListener>();
	}


	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void setup()
	{
		m_ctxProxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		m_ctxProxy.addCommListener(this);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void cleanup()
	{
		m_ctxProxy.removeCommListener(this);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public boolean isEnabled( String procId )
	{
		SPELLmessage msg = new SPELLmessageCheckVariablesEnabled(procId);
		
		try
		{
			// Perform the request. May throw an exception.
			SPELLmessage response = m_ctxProxy.sendRequest(msg);
			if (response != null)
			{
				return SPELLmessageCheckVariablesEnabled.isEnabled(response);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return false;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void setEnabled( String procId, boolean enable )
	{
		SPELLmessage msg = null;
		
		if (enable)
		{
			msg = new SPELLmessageSetVariablesEnabled(procId);
		}
		else
		{
			msg = new SPELLmessageSetVariablesDisabled(procId);
		}
		
		try
		{
			m_ctxProxy.sendMessage(msg);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public Map<String,VariableData> retrieveVariables( String procId, IProgressMonitor monitor ) throws Exception
	{
		VariableData[] variables = null;
		Map<String,VariableData> result = null;
		
		Logger.debug("Requesting variables in " + procId, Level.PROC, this);
		SPELLmessage msg = null;
		SPELLmessage response = null;

		boolean getMoreChunks = true;
		int chunkNo = 0;
		TransferData chunk = null;
		String valueList = "";

		while (getMoreChunks)
		{
			if (monitor.isCanceled())
			{
				return result;
			}
			
			// If chunkNo == 0, it is the initial request
			if (chunkNo == 0)
			{
				msg = new SPELLmessageGetVariables(procId);
			}
			// Subsequent requests
			else
			{
				msg = new SPELLmessageGetVariables(procId, chunkNo);
			}
			// Perform the request. May throw an exception.
			response = m_ctxProxy.sendRequest(msg);

			if (monitor.isCanceled())
			{
				return result;
			}

			// Process the response and obtain the transfer data
			if (response != null)
			{
				if (chunkNo == 0)
				{
					variables = SPELLmessageGetVariables.getVariables(response);
				}
				chunk = SPELLmessageGetVariables.getValueChunk(response);
			}

			// If data is not chunked
			if (chunk.getTotalChunks() == 0)
			{
				valueList = chunk.getData();
				getMoreChunks = false;
				monitor.subTask("Variable values obtained.");
			}
			// Else if this is the last chunk to obtain
			else if (chunk.getChunkNo() == chunk.getTotalChunks()) // This
			                                                       // is the
			                                                       // last
			                                                       // chunk
			{
				valueList += chunk.getData();
				getMoreChunks = false;
				monitor.worked(1);
				monitor.subTask("Variable values obtained.");
			}
			// Otherwise, get the next chunk
			else
			{
				if (chunkNo == 0)
				{
					monitor.beginTask("Obtaining variable values", chunk.getTotalChunks());
				}
				monitor.worked(1);
				valueList += chunk.getData();
				monitor.subTask("Variable values: chunk " + chunkNo + " of " + chunk.getTotalChunks());
				if (chunkNo < chunk.getTotalChunks() - 1)
				{
					getMoreChunks = true;
					chunkNo = chunk.getChunkNo() + 1;
				}
				else
				{
					getMoreChunks = false;
				}
			}

			if (monitor.isCanceled())
			{
				return result;
			}
		}
		
		if (variables == null)
		{
			monitor.setTaskName("No variables obtained");
			return null;
		}
		
		monitor.beginTask("Updating variable values", variables.length);
		String[] values = SPELLmessageGetVariables.getValues(valueList);
		for( int index=0; index< variables.length; index++)
		{
			if (monitor.isCanceled())
			{
				return result;
			}
			monitor.subTask("Variable: " + variables[index].getName());
			monitor.worked(1);
			variables[index].setValue(values[index]);
		}
		if (values.length==variables.length+1)
		{
			if (monitor.isCanceled())
			{
				return result;
			}
			variables[ variables.length-1 ].setValue(values[values.length-1]);
		}
		result = new TreeMap<String,VariableData>();
		for(VariableData data : variables)
		{
			result.put(data.getName(),data);
		}
		return result;
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void changeVariable(String procId, String varName, String valueExpression, boolean isGlobal) throws Exception
	{
		Logger.debug("Requesting variable change in " + procId, Level.COMM, this);
		SPELLmessage msg = new SPELLmessageChangeVariable(procId, varName, valueExpression, isGlobal);
		m_ctxProxy.sendRequest(msg);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public SPELLmessageResponse receiveRequest(SPELLmessageRequest msg)
    {
	    return null;
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void receiveMessage(SPELLmessage msg)
    {
		if (msg.getId().equals(IWVMessageId.MSG_VARIABLE_CHANGE))
		{
			SPELLmessageVariableChange chg = new SPELLmessageVariableChange(msg);
			VariableNotification data = chg.getData();
			if (m_listeners.containsKey(data.getProcId()))
			{
				m_listeners.get(data.getProcId()).variableChanged(data);
			}
		}
		else if (msg.getId().equals(IWVMessageId.MSG_SCOPE_CHANGE))
		{
			SPELLmessageScopeChange chg = new SPELLmessageScopeChange(msg);
			if (m_listeners.containsKey(chg.getProcId()))
			{
				m_listeners.get(chg.getProcId()).scopeChanged(chg.getData());
			}
		}
    }
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void connectionLost(ErrorData data)
    {
		for(IVariableListener listener : m_listeners.values())
		{
			listener.connectionLost();
		}
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void connectionFailed(ErrorData data)
    {
		for(IVariableListener listener : m_listeners.values())
		{
			listener.connectionLost();
		}
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void connectionClosed()
    {
		for(IVariableListener listener : m_listeners.values())
		{
			listener.connectionLost();
		}
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void addListener(String procId, IVariableListener listener)
    {
		m_listeners.put(procId,listener); 
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void removeListener(String procId, IVariableListener listener)
    {
		m_listeners.remove(procId); 
    }

}
