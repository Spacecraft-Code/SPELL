///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.interfaces
// 
// FILE      : IServerProxy.java
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

import java.util.Vector;

import com.astra.ses.spell.gui.core.exceptions.ServerError;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;

/*******************************************************************************
 * 
 * Note: we need to migrate this interface in order to be generic and uncoupled
 * the specific services which are provided by other plugins
 * 
 * @author Rafael Chinchilla
 *
 ******************************************************************************/
public interface IServerProxy extends IBaseProxy
{
	/***************************************************************************
	 * Change the currently selected server name and port
	 * 
	 * @param serverID
	 *            The server identifier
	 **************************************************************************/
	public void changeServer(ServerInfo server) throws ServerError;

	/***************************************************************************
	 * Obtain the currently selected server
	 * 
	 * @return The current server
	 **************************************************************************/
	public String getCurrentServerID();

	/***************************************************************************
	 * Obtain the currently selected server data
	 * 
	 * @return The current server data
	 **************************************************************************/
	public ServerInfo getCurrentServer();

	/***************************************************************************
	 * Open a context on the server
	 * 
	 * @param ctxName
	 *            The context name
	 **************************************************************************/
	public void openContext(String ctxName) throws ServerError;

	/***************************************************************************
	 * Close a context on the server
	 * 
	 * @param ctxName
	 *            The context name
	 **************************************************************************/
	public void closeContext(String ctxName) throws ServerError;

	/***************************************************************************
	 * Destroy a context on the server
	 * 
	 * @param ctxName
	 *            The context name
	 **************************************************************************/
	public void destroyContext(String ctxName) throws ServerError;

	/***************************************************************************
	 * Attach to an execution context on server
	 * 
	 * @param ctxName
	 *            The context name
	 **************************************************************************/
	public void attachContext(String ctxName) throws ServerError;

	/***************************************************************************
	 * Attach to an execution context on server
	 * 
	 * @param ctxName
	 *            The context name
	 **************************************************************************/
	public void detachContext() throws ServerError;

	/***************************************************************************
	 * Obtain the list of available contexts
	 * 
	 * @return The list of context names
	 **************************************************************************/
	public Vector<String> getAvailableContexts() throws ServerError;

	/***************************************************************************
	 * Obtain context details
	 * 
	 * @param ctxName
	 *            The context name
	 * @return The context details
	 **************************************************************************/
	public ContextInfo getContextInfo(String ctxName) throws ServerError;

    /***************************************************************************
     * Force the implementation of the comm listener interface
     **************************************************************************/
    public void connectionFailed(ErrorData data);

    /***************************************************************************
     * Force the implementation of the comm listener interface
     **************************************************************************/
    public void connectionLost(ErrorData data);
}
