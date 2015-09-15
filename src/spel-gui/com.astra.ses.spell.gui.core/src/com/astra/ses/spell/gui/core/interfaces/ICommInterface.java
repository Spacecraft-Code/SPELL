///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.interfaces
// 
// FILE      : ICommInterface.java
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
import com.astra.ses.spell.gui.core.exceptions.CommException;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;

/*******************************************************************************
 * @brief Parent of all communication interfaces
 * @date 22/04/08
 ******************************************************************************/
public interface ICommInterface
{
	/***************************************************************************
	 * Configure the interface to connect to the given server and port
	 **************************************************************************/
	public void configure(ServerInfo info);

	/***************************************************************************
	 * Connect the interface
	 **************************************************************************/
	public void connect() throws CommException;

	/***************************************************************************
	 * Disconnect the interface
	 **************************************************************************/
	public void disconnect() throws CommException;

	/***************************************************************************
	 * Force interface disconnection
	 **************************************************************************/
	public void forceDisconnect();

	/***************************************************************************
	 * Check if the interface is connected
	 **************************************************************************/
	public boolean isConnected();

	/***************************************************************************
	 * Set the response timeout
	 **************************************************************************/
	public void setResponseTimeout(long timeoutUSecs);

	/***************************************************************************
	 * Assign the message receiver
	 **************************************************************************/
	public void setCommListener(ICommListener listener);

	/***************************************************************************
	 * Assign the message receiver
	 **************************************************************************/
	public void setTunneling(ServerInfo tunnelInfo);

	/***************************************************************************
	 * Send a request to peer
	 **************************************************************************/
	public SPELLmessage sendRequest(SPELLmessage msg) throws CommException;

	/***************************************************************************
	 * Send a request to peer using a custom timeout
	 **************************************************************************/
	public SPELLmessage sendRequest(SPELLmessage msg, long timeout)
	        throws CommException;

	/***************************************************************************
	 * Send a message to peer
	 **************************************************************************/
	public void sendMessage(SPELLmessage msg) throws CommException;

	/***************************************************************************
	 * Download a file from peer
	 **************************************************************************/
	public void getFile( String fileName, String targetDir ) throws CommException;

	/***************************************************************************
	 * Get client registration key
	 **************************************************************************/
	public String getKey();
}
