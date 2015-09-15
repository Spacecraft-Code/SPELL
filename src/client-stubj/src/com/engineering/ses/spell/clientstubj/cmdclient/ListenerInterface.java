///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.engineering.ses.spell.clientstubj.cmdclient
// 
// FILE      : ListenerInterface.java
//
// DATE      : Aug 29, 2013
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
package com.engineering.ses.spell.clientstubj.cmdclient;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageAttachCtx;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageCloseCtx;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageCtxInfo;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageCtxList;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageDestroyCtx;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageError;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageLogin;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageLogout;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageOpenCtx;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageRequest;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageResponse;
import com.astra.ses.spell.gui.core.comm.socket.ifc.CommInterfaceSocket;
import com.astra.ses.spell.gui.core.interfaces.ICommInterface;
import com.astra.ses.spell.gui.core.interfaces.ICommListener;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.types.ContextStatus;

public class ListenerInterface implements ICommListener
{
	/** Holds the IPC interface to the listener */
	private ICommInterface m_listenerIfc;
	/** Holds the IPC information of the listener */
	private ServerInfo m_listenerInfo;
	/** True when the client succeeded to establish connection with listener */
	private boolean m_listenerReady;

	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ListenerInterface()
	{
		m_listenerIfc = new CommInterfaceSocket();
		m_listenerIfc.setCommListener(this);
		m_listenerReady = false;
	}

	/***************************************************************************
	 * Log into listener
	 **************************************************************************/
	public void login( ServerInfo lst )
	{
		if (!m_listenerReady)
		{
			m_listenerInfo = lst;
			m_listenerIfc.configure(lst);
			m_listenerIfc.connect();
			// Send login message
			SPELLmessage login = new SPELLmessageLogin();
			m_listenerIfc.sendRequest(login);
			m_listenerReady = true;
		}
		else
		{
			throw new RuntimeException("Already connected to the server " + m_listenerInfo.getName());
		}
	}
	
	/***************************************************************************
	 * Logout from listener
	 **************************************************************************/
	public void logout()
	{
		if (m_listenerReady)
		{
			SPELLmessage logout = new SPELLmessageLogout();
			m_listenerIfc.sendRequest(logout);
			m_listenerIfc.disconnect();
			m_listenerReady = false;
		}
	}

	/***************************************************************************
	 * Check if connected
	 **************************************************************************/
	boolean isConnected( String serverName )
	{
		if (m_listenerReady)
		{
			if (m_listenerInfo.getName().equals(serverName)) return true;
		}
		return false;
	}

	/***************************************************************************
	 * Check if connected
	 **************************************************************************/
	boolean isConnected()
	{
		return m_listenerReady;
	}

	/***************************************************************************
	 * Get listener server name and port
	 **************************************************************************/
	String getConnectionString()
	{
		if (!m_listenerReady) return null;
		return m_listenerInfo.getHost() + ":" + m_listenerInfo.getPort();
	}

	/***************************************************************************
	 * Check for the context and start it if needed
	 **************************************************************************/
	ContextInfo contextStartup( String ctxName )
	{
		// Obtain list of available contexts
		// Check that context is there
		List<String> contextNames = getAvailableContexts();
		if (!contextNames.contains(ctxName))
		{
			throw new RuntimeException("Cannot find context " + ctxName);
		}

		// Get context status
		ContextInfo contextInfo = getContextInfo(ctxName);
		
		// Start the context if needed
		if (contextInfo.getStatus().equals(ContextStatus.AVAILABLE))
		{
			startContext(ctxName);
		}
		else if (!contextInfo.getStatus().equals(ContextStatus.RUNNING))
		{
			throw new RuntimeException("Context is not in nominal status");
		}
		
		// Request the listener information to attach
		SPELLmessageAttachCtx attach = new SPELLmessageAttachCtx(ctxName);
		SPELLmessage resp = m_listenerIfc.sendRequest(attach);

		if (resp == null)
		{
			throw new RuntimeException("Failed to attach to context: no response from server");
		}
		else if (resp instanceof SPELLmessageError)
		{
			SPELLmessageError error = (SPELLmessageError) resp;
			throw new RuntimeException("Failed to attach to context: " + error.getData().getMessage());
		}
		// Extract the context information
		SPELLmessageCtxInfo.fillCtxInfo(contextInfo, resp);
		contextInfo.setHost(m_listenerInfo.getHost());

		return contextInfo;
	}

	/***************************************************************************
	 * Operation: obtain list of contexts
	 **************************************************************************/
	List<String> getAvailableContexts()
	{
		// Obtain list of available contexts
		SPELLmessageCtxList ctxList = new SPELLmessageCtxList();
		SPELLmessage resp = m_listenerIfc.sendRequest(ctxList);
		List<String> contexts = null;
		if (resp != null)
		{
			if (resp instanceof SPELLmessageError )
			{
				SPELLmessageError errorMsg = (SPELLmessageError) resp;
				throw new RuntimeException("Unable to obtain context list: " + errorMsg.getData().getMessage());
			}
			else
			{
				String contextList = SPELLmessageCtxList.getCtxListFrom(resp);
				// Check that context is there
				String[] contextNamesArray = contextList.split(",");
				contexts = new LinkedList<String>();
				contexts.addAll( Arrays.asList(contextNamesArray) );
			}
		}
		return contexts;
	}

	/***************************************************************************
	 * Obtain context information
	 **************************************************************************/
	ContextInfo getContextInfo( String name )
	{
		SPELLmessageCtxInfo ctxInfo = new SPELLmessageCtxInfo(name);
		SPELLmessage resp = m_listenerIfc.sendRequest(ctxInfo);
		ContextInfo info = null;
		if (resp != null)
		{
			if (resp instanceof SPELLmessageError )
			{
				SPELLmessageError errorMsg = (SPELLmessageError) resp;
				throw new RuntimeException("Unable to obtain context information: " + errorMsg.getData().getMessage());
			}
			else
			{
				info = new ContextInfo(name);
				SPELLmessageCtxInfo.fillCtxInfo(info, resp);
			}
		}
		else
		{
			throw new RuntimeException("Unable to obtain context information: no response from server");
		}
		return info;
	}

	/***************************************************************************
	 * Start a context
	 **************************************************************************/
	void startContext( String name )
	{
		SPELLmessageOpenCtx openCtx = new SPELLmessageOpenCtx(name);
		SPELLmessage resp = m_listenerIfc.sendRequest(openCtx);
		if (resp != null)
		{
			if (resp instanceof SPELLmessageError )
			{
				SPELLmessageError errorMsg = (SPELLmessageError) resp;
				throw new RuntimeException("Unable to start context: " + errorMsg.getData().getMessage());
			}
		}
		else
		{
			throw new RuntimeException("Unable to start context: no response from server");
		}
	}

	/***************************************************************************
	 * Stop a context
	 **************************************************************************/
	void stopContext( String name )
	{
		SPELLmessageCloseCtx stopCtx = new SPELLmessageCloseCtx(name);
		SPELLmessage resp = m_listenerIfc.sendRequest(stopCtx);
		if (resp != null)
		{
			if (resp instanceof SPELLmessageError )
			{
				SPELLmessageError errorMsg = (SPELLmessageError) resp;
				throw new RuntimeException("Unable to stop context: " + errorMsg.getData().getMessage());
			}
		}
		else
		{
			throw new RuntimeException("Unable to stop context: no response from server");
		}
	}

	/***************************************************************************
	 * Kill a context
	 **************************************************************************/
	void killContext( String name )
	{
		SPELLmessageDestroyCtx killCtx = new SPELLmessageDestroyCtx(name);
		SPELLmessage resp = m_listenerIfc.sendRequest(killCtx);
		if (resp != null)
		{
			if (resp instanceof SPELLmessageError )
			{
				SPELLmessageError errorMsg = (SPELLmessageError) resp;
				throw new RuntimeException("Unable to kill context: " + errorMsg.getData().getMessage());
			}
		}
		else
		{
			throw new RuntimeException("Unable to kill context: no response from server");
		}
	}

	@Override
    public SPELLmessageResponse receiveRequest(SPELLmessageRequest msg)
    {
	    // TODO Auto-generated method stub
	    return null;
    }


	@Override
    public void receiveMessage(SPELLmessage msg)
    {
	    // TODO Auto-generated method stub
	    
    }


	@Override
    public void connectionLost(ErrorData data)
    {
	    // TODO Auto-generated method stub
	    
    }


	@Override
    public void connectionFailed(ErrorData data)
    {
	    // TODO Auto-generated method stub
	    
    }


	@Override
    public void connectionClosed()
    {
	    // TODO Auto-generated method stub
	    
    }
}
