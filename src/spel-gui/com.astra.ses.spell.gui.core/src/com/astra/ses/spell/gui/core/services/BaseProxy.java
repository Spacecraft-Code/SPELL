///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.services
// 
// FILE      : BaseProxy.java
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
package com.astra.ses.spell.gui.core.services;

import java.util.ArrayList;
import java.util.List;

import com.astra.ses.spell.gui.core.comm.CommInterfaceFactory;
import com.astra.ses.spell.gui.core.comm.messages.RequestException;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageError;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageLogin;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageLogout;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageRequest;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageResponse;
import com.astra.ses.spell.gui.core.exceptions.CommException;
import com.astra.ses.spell.gui.core.interfaces.BaseService;
import com.astra.ses.spell.gui.core.interfaces.IBaseProxy;
import com.astra.ses.spell.gui.core.interfaces.ICommInterface;
import com.astra.ses.spell.gui.core.interfaces.ICommListener;
import com.astra.ses.spell.gui.core.interfaces.IMessageField;
import com.astra.ses.spell.gui.core.interfaces.IMessageType;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;

abstract class BaseProxy extends BaseService implements IBaseProxy, ICommListener
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	private ICommInterface m_interface;
	private List<ICommListener> m_subListeners;

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public BaseProxy(String id)
	{
		super(id);
		m_interface = CommInterfaceFactory.createCommInterface();
		m_interface.setCommListener(this);
		m_subListeners = new ArrayList<ICommListener>();
	}

	/***************************************************************************
	 * Prepare proxy
	 **************************************************************************/
	@Override
	public void setup()
	{
	}

	/***************************************************************************
	 * Cleanup proxy
	 **************************************************************************/
	@Override
	public void cleanup()
	{
		if (m_interface.isConnected())
		{
			try
			{
				m_interface.disconnect();
			}
			catch (CommException ex)
			{
				m_interface.forceDisconnect();
			}
		}
	}

	/***************************************************************************
	 * Get the interface
	 **************************************************************************/
	protected ICommInterface getIPC()
	{
		return m_interface;
	}

	/***************************************************************************
	 * Subscribe to required resources
	 **************************************************************************/
	@Override
	public void subscribe()
	{
	}

	/***************************************************************************
	 * Set the response timeout
	 **************************************************************************/
	@Override
	public void setResponseTimeout(long timeoutUSecs)
	{
		m_interface.setResponseTimeout(timeoutUSecs);
	}

	/***************************************************************************
	 * Check if the connection is established
	 * 
	 * @return True if it is
	 **************************************************************************/
	@Override
	public boolean isConnected()
	{
		return m_interface.isConnected();
	}

	/***************************************************************************
	 * Receive service update notifications
	 * 
	 * @param service
	 *            Service notifying the update
	 **************************************************************************/
	public void serviceUpdated(String service)
	{
		// Nothing to do
	}

	/***************************************************************************
	 * Force disconnection from SPELL server
	 **************************************************************************/
	@Override
	public void forceDisconnect()
	{
		m_interface.forceDisconnect();
	}

	/***************************************************************************
	 * Check a SPELL message response correctness
	 * 
	 * @param msg
	 *            SPELL message to check
	 * @throws RequestException
	 *             if the message contains errors
	 **************************************************************************/
	protected void checkRequestFailure( SPELLmessage request, SPELLmessage response ) throws RequestException
	{
		RequestException ex = null;
		if (response == null)
		{
			String error = "Request " + request.getId() + " failed on server side: no response";
			ex = new RequestException(error);
			ex.isFatal = true;
		}
		else if (response instanceof SPELLmessageError)
		{
			SPELLmessageError errorMsg = (SPELLmessageError) response;
			String error = errorMsg.getData().getMessage();
			String reason = errorMsg.getData().getReason();
			if (!reason.equals("(unknown)")) error += "\nReason: " + reason;
			ex = new RequestException(error);
		}
		if (ex != null) throw ex;
	}

	/***************************************************************************
	 * Send a generic request and return the response, controlling the
	 * communication process
	 * 
	 * @throws RequestException
	 **************************************************************************/
	protected SPELLmessage performRequest(SPELLmessage request, long timeout) throws Exception
	{
		SPELLmessage response = null;
		try
		{
			if (request == null) return null;

			if (!isConnected()) return null;

			//Logger.debug("Request: " + request.getId(), Level.COMM, this);

			if (timeout > 0)
			{
				response = m_interface.sendRequest(request, timeout);
			}
			else
			{
				response = m_interface.sendRequest(request);
			}
			checkRequestFailure(request, response);
			//Logger.debug("Response received for " + request.getId(), Level.COMM, this);
		}
		catch (RequestException ex)
		{
			throw ex;
		}
		catch (Exception ex)
		{
			String origin = "COMM";
			String message = "Request error (" + request.getId() + ")";
			String reason = ex.getLocalizedMessage();
			Logger.error(message + ": " + reason, Level.COMM, this);
			ErrorData data = new ErrorData(origin, message, reason, true);
			connectionFailed(data);
			throw new Exception(ex.getLocalizedMessage());
		}
		return response;
	}

	/***************************************************************************
	 * Send a generic request and return the response, controlling the
	 * communication process
	 * 
	 * @throws RequestException
	 **************************************************************************/
	protected SPELLmessage performRequest(SPELLmessage request) throws Exception
	{
		return performRequest(request, 0);
	}

	/***************************************************************************
	 * Send a message to peer
	 **************************************************************************/
	@Override
	public void sendMessage( SPELLmessage message )
	{
		m_interface.sendMessage(message);
	}

	/***************************************************************************
	 * Perform a request
	 **************************************************************************/
	@Override
	public SPELLmessage sendRequest( SPELLmessage message ) throws Exception
	{
		try
		{
			return performRequest(message);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
	}

	/***************************************************************************
	 * Perform a request
	 **************************************************************************/
	@Override
	public SPELLmessage sendRequest( SPELLmessage message, long timeout ) throws Exception
	{
		try
		{
			return performRequest(message, timeout);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
	}

	/***************************************************************************
	 * Download a file through the proxy connection
	 **************************************************************************/
	@Override
	public void getFile( String remoteFile, String targetDir ) throws Exception
	{
		m_interface.getFile(remoteFile, targetDir);
	}

	/***************************************************************************
	 * Perform low level IPC connect
	 **************************************************************************/
	protected void performConnect( ServerInfo connectionInfo )
	{
		Logger.debug("Connecting IPC", Level.COMM, this);
		m_interface.configure(connectionInfo);
		getIPC().connect();
		Logger.debug("Login on listener", Level.COMM, this);
		login();
		Logger.debug("Login success", Level.COMM, this);
	}

	/***************************************************************************
	 * Perform low level IPC disconnect
	 **************************************************************************/
	protected void performDisconnect()
	{
		Logger.debug("Disconnecting IPC", Level.COMM, this);
		logout();
		getIPC().disconnect();
		Logger.debug("Login on listener", Level.COMM, this);
		Logger.debug("Connected", Level.COMM, this);
	}

	/***************************************************************************
	 * Establish connection with peer
	 **************************************************************************/
    private void login()
    {
        try
        {
            Logger.info("Login", Level.COMM, this);
            SPELLmessage login = new SPELLmessageLogin();
            performRequest(login);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

	/***************************************************************************
	 * Finish connection with peer
	 **************************************************************************/
    private void logout()
    {
        try
        {
            Logger.info("Logout", Level.COMM, this);
            SPELLmessage logout = new SPELLmessageLogout();
            performRequest(logout);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.core.interfaces.ICommListener#receiveRequest(
	 * com.astra.ses.spell.gui.core.comm.messages.SPELLmessageRequest)
	 */
	@Override
	public SPELLmessageResponse receiveRequest(SPELLmessageRequest msg)
	{
		SPELLmessageResponse response = processIncomingRequest(msg);
		// Send it also to listeners. If the proxy gave a response
		// use this one; otherwise get the first response available
		// while iterating on the listeners.
		for(ICommListener listener : m_subListeners)
		{
			SPELLmessageResponse listenerResponse = listener.receiveRequest(msg);
			// We only admit one listener processing the request
			if((listenerResponse != null)&&(response == null)) 
			{
				response = listenerResponse;
				break;
			}
		}
		
		if (response == null)
		{
			Logger.error("Unprocessed request: " + msg.getId(), Level.COMM, this);
			response = new SPELLmessageResponse(msg);
			response.setType(IMessageType.MSG_TYPE_ERROR);
			response.set(IMessageField.FIELD_ERROR, "Request " + msg.getId() + " failed");
			response.set(IMessageField.FIELD_REASON, "Unprocessed request");
			response.set(IMessageField.FIELD_FATAL, "True");
		}
		
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.core.interfaces.ICommListener#receiveMessage(
	 * com.astra.ses.spell.gui.core.comm.messages.SPELLmessage)
	 */
	@Override
	public void receiveMessage(SPELLmessage msg)
	{
		processIncomingMessage(msg);
		// Also forward to listeners
		for(ICommListener listener : m_subListeners)
		{
			listener.receiveMessage(msg);
		}
	}
	
	@Override
	public void addCommListener( ICommListener listener )
	{
		if (!m_subListeners.contains(listener))
		{
			m_subListeners.add(listener);
		}
	}

	@Override
	public void removeCommListener( ICommListener listener )
	{
		if (m_subListeners.contains(listener))
		{
			m_subListeners.remove(listener);
		}
	}}
