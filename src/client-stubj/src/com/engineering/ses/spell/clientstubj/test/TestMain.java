///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.engineering.ses.spell.clientstubj.test
// 
// FILE      : TestMain.java
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
package com.engineering.ses.spell.clientstubj.test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageAttachCtx;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageCtxInfo;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageCtxList;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageLogin;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageLogout;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageOpenCtx;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageProcList;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageRequest;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageResponse;
import com.astra.ses.spell.gui.core.comm.socket.ifc.CommInterfaceSocket;
import com.astra.ses.spell.gui.core.interfaces.ICommInterface;
import com.astra.ses.spell.gui.core.interfaces.ICommListener;
import com.astra.ses.spell.gui.core.interfaces.IMessageType;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo.ServerRole;
import com.astra.ses.spell.gui.core.model.types.ContextStatus;
import com.astra.ses.spell.gui.core.utils.Logger;

/*******************************************************************************
 * This class gives an example of using the SPELL GUI core jar files for
 * establishing a connection to the SPELL server. Note that the IPC messages
 * and interfaces used do not need to be defined in this program, as they are
 * already available in the core client plugin. The following jar files
 * of the GUI need to be included on the classpath:
 * 
 * - com.astra.ses.spell.gui.core
 * - com.astra.ses.spell.gui.commsocket
 * - com.astra.ses.spell.gui.types
 * 
 */
public class TestMain implements ICommListener
{
	/** Holds the IPC interface to the listener */
	private ICommInterface m_listenerIfc;
	/** Holds the IPC interface to the context */
	private ICommInterface m_contextIfc;
	/** Holds the IPC information of the listener */
	private ServerInfo m_listenerInfo;
	/** Holds the IPC information of the context */
	private ContextInfo m_contextInfo;
	
	/***************************************************************************
	 * Main program
	 **************************************************************************/
	public static void main(String[] args)
	{
		TestMain test = new TestMain();
		test.start();
	}
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public TestMain()
	{
		m_listenerIfc = new CommInterfaceSocket();
		m_contextIfc = new CommInterfaceSocket();
		
		// Note that we are not using authentication data in this case, as this client
		// will connect to the local SPELL server.
		m_listenerInfo = new ServerInfo("LOCAL","localhost",9980,ServerRole.COMMANDING,null);
		m_listenerIfc.configure(m_listenerInfo);

		m_listenerIfc.setCommListener(this);
		m_contextIfc.setCommListener(this);
	}
	
	/***************************************************************************
	 * Main start method
	 **************************************************************************/
	public void start()
	{
		// Login into listener
		login();
		
		if (checkContextAvailable())
		{
			loginContext();
			
			SPELLmessageProcList procList = new SPELLmessageProcList(true);
			SPELLmessage resp = m_contextIfc.sendRequest(procList);
			
			if (resp != null)
			{
				Map<String,String> procedures = SPELLmessageProcList.getProcListFrom(resp);
				System.out.println("Available procedures:");
				for(String proc : procedures.keySet())
				{
					System.out.println(proc + " : " + procedures.get(proc));
				}
			}
			else
			{
				System.err.println("Error: cannot get procedure list");
			}
			
			goodByeContext();
		}

		goodBye();
	}
	
	/***************************************************************************
	 * Check for the STD context and start it if needed
	 **************************************************************************/
	private boolean checkContextAvailable()
	{
		// Obtain list of available contexts
		SPELLmessageCtxList ctxList = new SPELLmessageCtxList();
		SPELLmessage resp = m_listenerIfc.sendRequest(ctxList);
		String contextList = SPELLmessageCtxList.getCtxListFrom(resp);
		System.out.println("List of available contexts: " + contextList);
		
		// Check that STD context is there
		String[] contextNamesArray = contextList.split(",");
		List<String> contextNames = new LinkedList<String>();
		contextNames.addAll( Arrays.asList(contextNamesArray) );
		if (!contextNames.contains("STD"))
		{
			System.err.println("Error: cannot find context STD");
			return false;
		}

		// Get context status
		SPELLmessageCtxInfo ctxInfo = new SPELLmessageCtxInfo("STD");
		resp = m_listenerIfc.sendRequest(ctxInfo);
		m_contextInfo = new ContextInfo("STD");
		if (resp == null)
		{
			System.err.println("Error: cannot get context information for STD");
			return false;
		}
		SPELLmessageCtxInfo.fillCtxInfo(m_contextInfo, resp);
		
		// Start the context if needed
		if (m_contextInfo.getStatus().equals(ContextStatus.AVAILABLE))
		{
			SPELLmessageOpenCtx openCtx = new SPELLmessageOpenCtx("STD");
			resp = m_listenerIfc.sendRequest(openCtx);
			if (resp == null || resp.getType().equals(IMessageType.MSG_TYPE_ERROR))
			{
				System.err.println("Error: cannot start context STD");
				return false;
			}
		}
		else if (!m_contextInfo.getStatus().equals(ContextStatus.RUNNING))
		{
			System.err.println("Error: context STD is not in nominal status");
			return false;
		}
		
		return true;
	}
	
	
	/***************************************************************************
	 * Login into the listener
	 **************************************************************************/
	private void login()
	{
		// Connect IPC to listener
		m_listenerIfc.connect();
		// Send login message
		SPELLmessage login = new SPELLmessageLogin();
		m_listenerIfc.sendRequest(login);
	}

	/***************************************************************************
	 * Logout from the listener
	 **************************************************************************/
	private void goodBye()
	{
		SPELLmessage logout = new SPELLmessageLogout();
		m_listenerIfc.sendRequest(logout);
		m_listenerIfc.disconnect();
	}
	
	/***************************************************************************
	 * Login into the context
	 **************************************************************************/
	private void loginContext()
	{
		// Request the listener information to attach
		SPELLmessageAttachCtx attach = new SPELLmessageAttachCtx("STD");
		SPELLmessage resp = m_listenerIfc.sendRequest(attach);

		// Extract the context information
		SPELLmessageCtxInfo.fillCtxInfo(m_contextInfo, resp);
		m_contextInfo.setHost(m_listenerInfo.getHost());

		// Connect IPC to conext
		m_contextIfc.configure(m_contextInfo);
		m_contextIfc.connect();
		
		// Send login message
		SPELLmessage login = new SPELLmessageLogin();
		m_contextIfc.sendRequest(login);
	}

	/***************************************************************************
	 * Logout from the context
	 **************************************************************************/
	private void goodByeContext()
	{
		SPELLmessage logout = new SPELLmessageLogout();
		m_contextIfc.sendRequest(logout);
		m_contextIfc.disconnect();
	}

	/***************************************************************************
	 * This method will receive requests from listener and context
	 **************************************************************************/
	@Override
    public SPELLmessageResponse receiveRequest(SPELLmessageRequest msg)
    {
		System.out.println("RECEIVED REQUEST: " + msg.dataStr());
	    return null;
    }

	/***************************************************************************
	 * This method will receive messages from listener and context. This include
	 * notifications of context and procedure operations, notifications from
	 * the execution of procedures, etc.
	 **************************************************************************/
	@Override
    public void receiveMessage(SPELLmessage msg)
    {
		System.out.println("RECEIVED MESSAGE: " + msg.dataStr());
    }

	/***************************************************************************
	 * This method will be called when the connection to the listener and
	 * context are closed
	 **************************************************************************/
	@Override
    public void connectionClosed()
    {
	    // TODO Auto-generated method stub
	    
    }

	/***************************************************************************
	 * This method will be called when the connection to the listener and
	 * context are lost
	 **************************************************************************/
	@Override
    public void connectionLost(ErrorData data)
    {
	    // TODO Auto-generated method stub
	    
    }

	/***************************************************************************
	 * This method will be called when the connection to the listener and
	 * context cannot be established
	 **************************************************************************/
	@Override
    public void connectionFailed(ErrorData data)
    {
	    // TODO Auto-generated method stub
	    
    }

}
