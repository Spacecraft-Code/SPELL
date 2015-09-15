///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.socket.utils
// 
// FILE      : Tunneler.java
//
// DATE      : 2008-11-21 09:02
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
package com.astra.ses.spell.gui.core.comm.socket.utils;

import java.io.IOException;
import java.net.ServerSocket;

import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.LocalPortForwarder;

/*******************************************************************************
 * @brief Utility class for establishing SSH tunnels
 * @date 28/04/08
 ******************************************************************************/
public class Tunneler
{
	/** Holds the server characteristics */
	private ServerInfo	       m_serverInfo;
	/** Keeps the local tunnel port */
	private int	               m_localPort;
	/** Keeps the remote tunnel port */
	private int	               m_remotePort;
	/** Holds the SSH connection */
	private Connection	       m_connection;
	/** Holds the port forwarder */
	private LocalPortForwarder	m_fwd;
	/** True if the tunnel is established */
	private boolean	           m_connected	= false;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param data
	 *            Server data to be used to establish the tunnel
	 **************************************************************************/
	public Tunneler(ServerInfo info)
	{
		m_connection = null;
		m_connected = false;
		m_serverInfo = info;
		m_localPort = info.getPort();
		m_remotePort = info.getPort();
	}

	/***************************************************************************
	 * Open the SSH tunnel
	 **************************************************************************/
	public void openTunnel() throws IOException
	{
		if (m_connected)
		{
			Logger.debug("Closing previous session", Level.COMM, this);
			closeTunnel();
		}
		Logger.debug("Creating SSH connection", Level.COMM, this);
		m_connection = new Connection(m_serverInfo.getHost());
		Logger.debug("Opening SSH session", Level.COMM, this);
		m_connection.connect();
		Logger.debug("Authenticating", Level.COMM, this);
		Authenticator.authenticate(m_connection, m_serverInfo.getAuthentication());
		Logger.debug("Creating port forwarder", Level.COMM, this);
		m_localPort = findFreePort(m_localPort + 1);
		Logger.debug("localhost:" + m_localPort + " -> " + m_serverInfo.getHost() + ":" + m_serverInfo.getPort(), Level.COMM, this);
		m_fwd = m_connection.createLocalPortForwarder(m_localPort, "localhost", m_serverInfo.getPort());
		m_serverInfo.setPort(m_localPort);
		m_connected = true;
		Logger.debug("Tunnel connected", Level.COMM, this);
	}

	/***************************************************************************
	 * Close the SSH tunnel
	 **************************************************************************/
	public void closeTunnel()
	{
		if (!m_connected) return;
		try
		{
			Logger.debug("Closing port forwarder", Level.COMM, this);
			m_fwd.close();
		}
		catch (IOException e)
		{
		}
		Logger.debug("Closing connection", Level.COMM, this);
		m_connection.close();
		m_fwd = null;
		m_connection = null;
		m_connected = false;
		m_serverInfo.setPort(m_remotePort);
		m_serverInfo = null;
		Logger.debug("Tunnel disconnected", Level.COMM, this);
	}

	/***************************************************************************
	 * Obtain the local tunnel port
	 **************************************************************************/
	public int getLocalPort()
	{
		return m_localPort;
	}
	
	/***************************************************************************
	 * Get the connection object
	 **************************************************************************/
	public Connection getConnection()
	{
		return m_connection;
	}

	/***************************************************************************
	 * Find a free port in the system
	 **************************************************************************/
	protected int findFreePort(int defaultPort)
	{
		int port = defaultPort;
		try
		{
			ServerSocket server = new ServerSocket(0);
			port = server.getLocalPort();
			server.close();
		}
		catch (IOException ex)
		{
		}
		return port;
	}
}
