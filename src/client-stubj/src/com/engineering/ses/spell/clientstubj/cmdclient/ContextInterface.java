///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.engineering.ses.spell.clientstubj.cmdclient
// 
// FILE      : ContextInterface.java
//
// DATE      : Aug 29, 2014
//
// Copyright (C) 2008, 2011 SES ENGINEERING, Luxembourg S.A.R.L.
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

import java.util.List;
import java.util.Map;

import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageBackgroundExec;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageCloseExec;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageError;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageExecInfo;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageExecList;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageGetInstance;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageKillExec;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageLogin;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageLogout;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageOpenExec;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageProcInfo;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageProcList;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageRequest;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageResponse;
import com.astra.ses.spell.gui.core.comm.socket.ifc.CommInterfaceSocket;
import com.astra.ses.spell.gui.core.interfaces.ICommInterface;
import com.astra.ses.spell.gui.core.interfaces.ICommListener;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ExecutorInfo;
import com.astra.ses.spell.gui.core.model.types.ProcProperties;

public class ContextInterface implements ICommListener
{
	/** Holds the IPC interface to the context */
	private ICommInterface m_contextIfc;
	/** Holds the IPC information of the context */
	private ContextInfo m_contextInfo;
	/** True when the client succeeded to establish connection with context */
	private boolean m_contextReady;
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ContextInterface()
	{
		m_contextIfc = new CommInterfaceSocket();
		m_contextIfc.setCommListener(this);
		m_contextReady = false;
	}
	
	/***************************************************************************
	 * Log into context
	 **************************************************************************/
	public void login( ContextInfo ctx )
	{
		if (!m_contextReady)
		{
			m_contextInfo = ctx;
			// Connect IPC to conext
			m_contextIfc.configure(m_contextInfo);
			m_contextIfc.connect();
			
			// Send login message
			SPELLmessage login = new SPELLmessageLogin();
			m_contextIfc.sendRequest(login);
			
			m_contextReady = true;
		}
		else
		{
			throw new RuntimeException("Already connected to the context " + m_contextInfo.getName());
		}
	}
	
	/***************************************************************************
	 * Logout from context
	 **************************************************************************/
	public void logout()
	{
		if (m_contextReady)
		{
			SPELLmessage logout = new SPELLmessageLogout();
			m_contextIfc.sendRequest(logout);
			m_contextIfc.disconnect();
			m_contextReady = false;
		}
	}

	/***************************************************************************
	 * Check if connected
	 **************************************************************************/
	boolean isConnected( String ctxName )
	{
		if (m_contextReady)
		{
			if (m_contextInfo.getName().equals(ctxName)) return true;
		}
		return false;
	}

	/***************************************************************************
	 * Check if connected
	 **************************************************************************/
	boolean isConnected()
	{
		return m_contextReady;
	}

	/***************************************************************************
	 * Get context name
	 **************************************************************************/
	String getContextName()
	{
		if (!m_contextReady) return null;
		return m_contextInfo.getName();
	}

	/***************************************************************************
	 * Operation: obtain procedure properties
	 **************************************************************************/
	Map<ProcProperties,String> getProcedureProperties( String procId )
	{
		Map<ProcProperties,String> properties = null;
		SPELLmessageProcInfo info = new SPELLmessageProcInfo(procId);
		SPELLmessage resp = m_contextIfc.sendRequest(info);
		if (resp != null)
		{
			if (resp instanceof SPELLmessageError )
			{
				SPELLmessageError errorMsg = (SPELLmessageError) resp;
				throw new RuntimeException("Unable to obtain procedure properties: " + errorMsg.getData().getMessage());
			}
			else
			{
				properties = info.getProcProperties(resp);
			}
		}
		else
		{
			throw new RuntimeException("Unable to obtain procedure properties: no response from server");
		}

		return properties;
	}

	/***************************************************************************
	 * Operation: obtain executor information
	 **************************************************************************/
	ExecutorInfo getExecutorInformation( String instanceId )
	{
		ExecutorInfo info = null;
		SPELLmessageExecInfo execInfo = new SPELLmessageExecInfo(instanceId);
		SPELLmessage resp = m_contextIfc.sendRequest(execInfo);
		if (resp != null)
		{
			if (resp instanceof SPELLmessageError )
			{
				SPELLmessageError errorMsg = (SPELLmessageError) resp;
				throw new RuntimeException("Unable to obtain executor information: " + errorMsg.getData().getMessage());
			}
			else
			{
				info = new ExecutorInfo(instanceId);
				SPELLmessageExecInfo.fillExecInfo(info, resp);
			}
		}
		else
		{
			throw new RuntimeException("Unable to obtain executor information: no response from server");
		}

		return info;
	}

	/***************************************************************************
	 * Start a procedure in background mode
	 **************************************************************************/
	String startProcedure( String procId, Map<String,String> args )
	{
		SPELLmessageGetInstance inst = new SPELLmessageGetInstance(procId);
		SPELLmessage resp = m_contextIfc.sendRequest(inst);
		String instanceId = null;
		if (resp != null)
		{
			if (resp instanceof SPELLmessageError )
			{
				SPELLmessageError errorMsg = (SPELLmessageError) resp;
				throw new RuntimeException("Unable to get available instance id: " + errorMsg.getData().getMessage());
			}
			instanceId = SPELLmessageGetInstance.getInstance(resp);
		}
		else
		{
			throw new RuntimeException("Unable to get available instance id: no response from server");
		}
		
		SPELLmessageOpenExec openExec = new SPELLmessageOpenExec(instanceId,true);
		if (args != null)
		{
			openExec.setArguments(args);
		}
		resp = m_contextIfc.sendRequest(openExec);
		if (resp != null)
		{
			if (resp instanceof SPELLmessageError )
			{
				SPELLmessageError errorMsg = (SPELLmessageError) resp;
				throw new RuntimeException("Unable to start procedure: " + errorMsg.getData().getMessage());
			}
		}
		else
		{
			throw new RuntimeException("Unable to start procedure: no response from server");
		}

		SPELLmessageBackgroundExec bkg = new SPELLmessageBackgroundExec(instanceId);
		resp = m_contextIfc.sendRequest(bkg);
		if (resp != null)
		{
			if (resp instanceof SPELLmessageError )
			{
				SPELLmessageError errorMsg = (SPELLmessageError) resp;
				throw new RuntimeException("Unable to put procedure in background: " + errorMsg.getData().getMessage());
			}
		}
		else
		{
			throw new RuntimeException("Unable to put procedure in background: no response from server");
		}
		return instanceId;
	}

	/***************************************************************************
	 * Operation: obtain list of procedures 
	 **************************************************************************/
	Map<String,String> getAvailableProcedures()
	{
		Map<String,String> procedures = null;
		SPELLmessageProcList procList = new SPELLmessageProcList(true);
		SPELLmessage resp = m_contextIfc.sendRequest(procList);
		if (resp != null)
		{
			if (resp instanceof SPELLmessageError )
			{
				SPELLmessageError errorMsg = (SPELLmessageError) resp;
				throw new RuntimeException("Unable to obtain procedure list: " + errorMsg.getData().getMessage());
			}
			else
			{
				procedures = SPELLmessageProcList.getProcListFrom(resp);
			}
		}
		else
		{
			throw new RuntimeException("Unable to obtain procedure list: no response from server");
		}

		return procedures;
	}

	/***************************************************************************
	 * Operation: obtain list of procedures 
	 **************************************************************************/
	List<String> getAvailableExecutors()
	{
		List<String> executors = null;
		SPELLmessageExecList execList = new SPELLmessageExecList();
		SPELLmessage resp = m_contextIfc.sendRequest(execList);
		if (resp != null)
		{
			if (resp instanceof SPELLmessageError )
			{
				SPELLmessageError errorMsg = (SPELLmessageError) resp;
				throw new RuntimeException("Unable to obtain executor list: " + errorMsg.getData().getMessage());
			}
			else
			{
				executors = SPELLmessageExecList.getExecListFrom(resp);
			}
		}
		else
		{
			throw new RuntimeException("Unable to obtain executors list: no response from server");
		}

		return executors;
	}

	/***************************************************************************
	 * Stop a procedure 
	 **************************************************************************/
	void stopProcedure( String instanceId )
	{
		SPELLmessageCloseExec close = new SPELLmessageCloseExec(instanceId);
		SPELLmessage resp = m_contextIfc.sendRequest(close);
		if (resp != null)
		{
			if (resp instanceof SPELLmessageError )
			{
				SPELLmessageError errorMsg = (SPELLmessageError) resp;
				throw new RuntimeException("Unable to close procedure '" + instanceId + "': " + errorMsg.getData().getMessage());
			}
		}
		else
		{
			throw new RuntimeException("Unable to close procedure '" + instanceId + "': no response from server");
		}
		
	}

	/***************************************************************************
	 * Kill a procedure 
	 **************************************************************************/
	void killProcedure( String instanceId )
	{
		SPELLmessageKillExec kill = new SPELLmessageKillExec(instanceId);
		SPELLmessage resp = m_contextIfc.sendRequest(kill);
		if (resp != null)
		{
			if (resp instanceof SPELLmessageError )
			{
				SPELLmessageError errorMsg = (SPELLmessageError) resp;
				throw new RuntimeException("Unable to kill procedure '" + instanceId + "': " + errorMsg.getData().getMessage());
			}
		}
		else
		{
			throw new RuntimeException("Unable to kill procedure '" + instanceId + "': no response from server");
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
