///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.interfaces
// 
// FILE      : IBaseProxy.java
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
package com.astra.ses.spell.gui.core.interfaces;

import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageResponse;



public interface IBaseProxy extends IService
{
	/***************************************************************************
	 * Set the response timeout for requests
	 **************************************************************************/
	public void setResponseTimeout(long timeoutUSecs);

	/***************************************************************************
	 * Check if the connection is established
	 * 
	 * @return True if it is
	 **************************************************************************/
	public boolean isConnected();

	/***************************************************************************
	 * Connect to peer
	 **************************************************************************/
	public void connect() throws Exception;
	
	/***************************************************************************
	 * Disconnect from peer
	 **************************************************************************/
	public void disconnect() throws Exception;

	/***************************************************************************
	 * Perform hard disconnection
	 **************************************************************************/
	public void forceDisconnect();

	/***************************************************************************
	 * Perform a request with default timeout
	 **************************************************************************/
	public SPELLmessage sendRequest( SPELLmessage msg ) throws Exception;

	/***************************************************************************
	 * Download a file using the proxy connection
	 **************************************************************************/
	public void getFile( String remoteFileName, String targetDir ) throws Exception;

	/***************************************************************************
	 * Perform a request with timeout
	 **************************************************************************/
	public SPELLmessage sendRequest( SPELLmessage msg, long timeout ) throws Exception;

	/***************************************************************************
	 * Perform a request with timeout
	 **************************************************************************/
	public void sendMessage( SPELLmessage msg );

	/***************************************************************************
	 * Add IPC listener
	 **************************************************************************/
	public void addCommListener( ICommListener listener );
	
	/***************************************************************************
	 * Remove IPC listener
	 **************************************************************************/
	public void removeCommListener( ICommListener listener );

	/***************************************************************************
	 * Process an incoming message from IPC layer. Return false if not processed
	 **************************************************************************/
	public boolean processIncomingMessage( SPELLmessage msg );
	
	/***************************************************************************
	 * Process an incoming request from IPC layer. Return null if not processed
	 **************************************************************************/
	public SPELLmessageResponse processIncomingRequest( SPELLmessage msg );
}
