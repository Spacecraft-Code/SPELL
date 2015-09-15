///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.server
// 
// FILE      : ServerInfo.java
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
package com.astra.ses.spell.gui.core.model.server;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;

/*******************************************************************************
 * @brief Data structure holding the connection info for a SPELL server
 * @date 28/04/08
 ******************************************************************************/
public class ServerInfo
{
	private static final String	SERVER_NAME	    = "name";
	private static final String	SERVER_HOST	    = "host";
	private static final String	SERVER_PORT	    = "port";
	private static final String	SERVER_ROLE	    = "role";
	private static final String	SERVER_CONN     = "connectivity";

	public static enum ServerRole
	{
		COMMANDING, MONITORING
	}

	private static final String	SEPARATOR	= "<>";
	/** Holds the server name */
	private String	            m_name;
	/** Holds the server hostname */
	private String	            m_host;
	/** Holds the server port */
	private int	                m_port;
	/** Holds the server role */
	private ServerRole	        m_role;
	/** Holds the authentication settings (empty if using local file copies) */
	private AuthenticationData  m_auth;
	/** True if the authentication data is not specific to the server but it is using defaults */
	private boolean m_defaultAuth;


	/**************************************************************************
	 * Create a SPELLServer instance from a String
	 * 
	 * @param stringifiedServer
	 *            the SPELL serve represented as a String following the pattern
	 *            id<>name<>host<>port<>user<>role
	 * @return
	 *************************************************************************/
	public static ServerInfo valueOf(String stringifiedServer)
	{
		String[] s = stringifiedServer.split(SEPARATOR);
		if (s.length==8)
		{
			AuthenticationData auth = new AuthenticationData(s[4], s[5], s[6], Boolean.valueOf(s[7]));
			return new ServerInfo(s[0], s[1], Integer.valueOf(s[2]), ServerRole.valueOf(s[3]), auth);
		}
		if (s.length==7) // backwards compatibility
		{
			AuthenticationData auth = new AuthenticationData(s[4], s[5], s[6]);
			return new ServerInfo(s[0], s[1], Integer.valueOf(s[2]), ServerRole.valueOf(s[3]), auth);
		}
		else if (s.length == 4)
		{
			return new ServerInfo(s[0], s[1], Integer.valueOf(s[2]), ServerRole.valueOf(s[3]), null);
		}
		else
		{
			Logger.error("Malformed server info serialized string", Level.CONFIG, ServerInfo.class);
			return null;
		}
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ServerInfo()
	{
		m_name = null;
		m_host = null;
		m_auth = null;
		m_role = ServerRole.COMMANDING;
		m_port = 0;
		m_defaultAuth = false;
	}

	/**************************************************************************
	 * Constructor
	 *************************************************************************/
	public ServerInfo(String name, String host, int port, ServerRole role, AuthenticationData auth)
	{
		m_name = name;
		m_host = host;
		m_port = port;
		m_auth = auth;
		m_role = role;
		m_defaultAuth = false;
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ServerInfo(Element xmlElement, AuthenticationData defaultAuthentication )
	{
		this();

		if (xmlElement.getNodeType() == Node.ELEMENT_NODE)
		{
			NodeList nodes = xmlElement.getChildNodes();
			for (int idx = 0; idx < nodes.getLength(); idx++)
			{
				Node node = (Node) nodes.item(idx);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					String name = node.getNodeName();
					if (name.equals(SERVER_HOST))
					{
						m_host = node.getTextContent();
					}
					else if (name.equals(SERVER_PORT))
					{
						m_port = Integer.parseInt(node.getTextContent());
					}
					else if (name.equals(SERVER_NAME))
					{
						m_name = node.getTextContent();
					}
					else if (name.equals(SERVER_CONN))
					{
						m_auth = new AuthenticationData( node );
						m_defaultAuth = false;
					}
					else if (name.equals(SERVER_ROLE))
					{
						try
						{
							m_role = ServerRole.valueOf(node.getTextContent());
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
							m_role = ServerRole.COMMANDING;
						}
					}
				}
			}
			if (m_auth == null)
			{
				Logger.info("Server " + m_name + " inheriting default connectivity settings", Level.CONFIG, this);
				m_auth = defaultAuthentication;
				m_defaultAuth = true;
			}
			else
			{
				Logger.info("Server " + m_name + " using specific connectivity settings", Level.CONFIG, this);
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean validate()
	{
		return (m_name != null) && (m_host != null) && (m_port != 0);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setName(String name)
	{
		m_name = name;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getName()
	{
		return m_name;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setHost(String host)
	{
		m_host = host;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getHost()
	{
		return m_host;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public AuthenticationData getAuthentication()
	{
		return m_auth;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setAuthentication( AuthenticationData auth )
	{
		m_auth = auth;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setPort(int port)
	{
		m_port = port;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public int getPort()
	{
		return m_port;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public ServerRole getRole()
	{
		return m_role;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String toString()
	{
		if (m_auth != null && !m_defaultAuth)
		{
			return m_name + SEPARATOR + m_host + SEPARATOR + m_port + SEPARATOR + m_role.toString() + SEPARATOR + m_auth.toString();
		}
		else
		{
			return m_name + SEPARATOR + m_host + SEPARATOR + m_port + SEPARATOR + m_role.toString();
		}
	}
}
