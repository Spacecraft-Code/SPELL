///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.server
// 
// FILE      : AuthenticationData.java
//
// DATE      : Aug 1, 2013
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
package com.astra.ses.spell.gui.core.model.server;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;

public class AuthenticationData
{
	private static final String CONNECTIVITY_USER = "user";
	private static final String CONNECTIVITY_PWD = "pwd";
	private static final String CONNECTIVITY_KEY = "key";
	private static final String CONNECTIVITY_LOCAL = "local";

	private String m_username;
	private String m_password;
	private String m_keyFile;
	private final boolean m_localAccess;

	private static final String	SEPARATOR	= "<>";

	/***************************************************************************
	 * 
	 **************************************************************************/
	public static enum Mode
	{
		PASSWORD,
		KEY,
		NONE
	}
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public AuthenticationData( 	String username,
								String password,
								String keyFile,
								boolean localAccess )
	{
		m_username = username;
		m_password = password;
		m_keyFile = keyFile;
		m_localAccess = localAccess;
	}

	/***************************************************************************
	 * Constructor. Assumes no local access.
	 **************************************************************************/
	public AuthenticationData( 	String username,
								String password,
								String keyFile)
	{
		this(username, password, keyFile, false);
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public AuthenticationData( Node node )
	{
		m_username = null;
		m_password = null;
		m_keyFile = null;

		Node localAccessAttribute =
			node.getAttributes().getNamedItem(CONNECTIVITY_LOCAL);

		m_localAccess = localAccessAttribute != null
						&& "true".equals(localAccessAttribute.getNodeValue());

		NodeList nodes = node.getChildNodes();
		for (int idx = 0; idx < nodes.getLength(); idx++)
		{
			Node cnode = (Node) nodes.item(idx);
			if (cnode.getNodeType() == Node.ELEMENT_NODE)
			{
				String name = cnode.getNodeName();
				if (name.equals(CONNECTIVITY_USER))
				{
					m_username = cnode.getTextContent();
				}
				else if (name.equals(CONNECTIVITY_PWD))
				{
					m_password = cnode.getTextContent();
				}
				else if (name.equals(CONNECTIVITY_KEY))
				{
					m_keyFile = cnode.getTextContent();
				}
			}
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public static AuthenticationData valueOf(String stringified)
	{
		String[] s = stringified.split(SEPARATOR);
		if (s.length==4)
		{
			return new AuthenticationData(
							emptyToNull(s[0]),
							emptyToNull(s[1]),
							emptyToNull(s[2]),
							Boolean.valueOf(s[3]));
		}
		else if (s.length == 3) // backwards compatibility
		{
			return new AuthenticationData(
					emptyToNull(s[0]),
					emptyToNull(s[1]),
					emptyToNull(s[2]));
		}
		else
		{
			Logger.error("Malformed authentication data in serialized string", Level.CONFIG, ServerInfo.class);
			return null;
		}
	}


	/***************************************************************************
	 * 
	 **************************************************************************/
	public Mode getMode()
	{
		if (m_keyFile != null && !m_keyFile.trim().isEmpty()) return Mode.KEY;
		if (m_password != null && !m_password.trim().isEmpty()) return Mode.PASSWORD;
		return Mode.NONE;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getUsername()
	{
		return m_username;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getPassword()
	{
		return m_password;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setPassword( String pwd )
	{
		m_password = pwd;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getKeyFile()
	{
		return m_keyFile;
	}

	/***************************************************************************
	 * Returns whether the file access is to be done via the local filesystem.
	 **************************************************************************/
	public boolean isLocalAccess()
	{
		return m_localAccess;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String toString()
	{
		return nullToEmpty(m_username)  + SEPARATOR
		      + nullToEmpty(m_password) + SEPARATOR
		      + nullToEmpty(m_keyFile)  + SEPARATOR
		      + m_localAccess;
	}
	/***************************************************************************
	 * Utility method to handle null strings when serializing
	 **************************************************************************/
	private static String nullToEmpty(String string)
	{
		return string == null? "" : string; 
	}

	/***************************************************************************
	 * Utility method to handle null strings when de-serializing
	 **************************************************************************/
	private static String emptyToNull (String string)
	{
		return string == null || string.isEmpty()? null : string;
	}
}
