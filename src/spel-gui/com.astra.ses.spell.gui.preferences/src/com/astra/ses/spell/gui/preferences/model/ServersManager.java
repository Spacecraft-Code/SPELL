///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.preferences.model
// 
// FILE      : ServersManager.java
//
// DATE      : 2010-05-27
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
package com.astra.ses.spell.gui.preferences.model;

import java.util.HashMap;

import com.astra.ses.spell.gui.core.model.server.ServerInfo;

/*******************************************************************************
 * 
 * ServersManager class handles with all the SPELLServers stored in the
 * preferences system
 * 
 ******************************************************************************/
public class ServersManager
{

	private static final String	        SEPARATOR	= "<server>";
	private HashMap<String, ServerInfo>	m_serversMap;

	/***************************************************************************
	 * Create a ServersManager instance from a string
	 * 
	 * @return
	 **************************************************************************/
	public static ServersManager fromString(String representation)
	{
		String[] strServers = representation.split(SEPARATOR, 0);
		ServerInfo[] servers = new ServerInfo[strServers.length];
		for (int i = 0; i < strServers.length; i++)
		{
			ServerInfo server = ServerInfo.valueOf(strServers[i]);
			servers[i] = server;
		}
		return new ServersManager(servers);
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ServersManager(ServerInfo[] servers)
	{
		m_serversMap = new HashMap<String, ServerInfo>();
		for (ServerInfo server : servers)
		{
			m_serversMap.put(server.getName(), server);
		}
	}

	/***************************************************************************
	 * Return server information by giving its ID
	 * 
	 * @param id
	 * @return
	 **************************************************************************/
	public ServerInfo getServer(String id)
	{
		return m_serversMap.get(id);
	}

	/***************************************************************************
	 * Get available servers in preferences store
	 * 
	 * @return
	 **************************************************************************/
	public String[] getServerIds()
	{
		String[] ids = new String[m_serversMap.size()];
		return m_serversMap.keySet().toArray(ids);
	}

	/***************************************************************************
	 * Returns a string representation of this object
	 **************************************************************************/
	public String toString()
	{
		String repr = "";
		for (ServerInfo server : m_serversMap.values())
		{
			repr = repr + server.toString() + SEPARATOR;
		}
		return repr;
	}
}
