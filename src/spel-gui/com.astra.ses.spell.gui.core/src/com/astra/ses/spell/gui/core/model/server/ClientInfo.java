///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.server
// 
// FILE      : ClientInfo.java
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

import com.astra.ses.spell.gui.core.comm.messages.MessageException;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ClientOperation;

/*******************************************************************************
 * @brief Data structure holding the client info
 * @date 28/04/08
 ******************************************************************************/
public class ClientInfo
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// Execution status
	private static final String[]	CLIENT_MODE_STR	= { "CONTROL", "MONITOR",
	        "UNKNOWN"	                            };
	private static final String[]	CLIENT_OP_STR	= { "LOGIN", "LOGOUT",
	        "UNKNOWN"	                            };

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	/** Holds the server hostname */
	private String	              m_host;
	/** Holds the connection mode */
	private ClientMode	          m_mode;
	/** Holds the client key id */
	private String	              m_key;

	/***************************************************************************
	 * Constructor
	 * 
	 * @throws MessageException
	 **************************************************************************/
	public ClientInfo() throws MessageException
	{
		m_host = null;
		m_mode = null;
		m_key = null;
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
	public void setKey(String key)
	{
		m_key = key;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getKey()
	{
		return m_key;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setMode(ClientMode mode)
	{
		m_mode = mode;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public ClientMode getMode()
	{
		return m_mode;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public static ClientMode modeFromString(String mode)
	{
		if (mode == null) return ClientMode.UNKNOWN;
		for (ClientMode md : ClientMode.values())
		{
			if (CLIENT_MODE_STR[md.ordinal()].equals(mode)) return md;
		}
		return ClientMode.UNKNOWN;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public static ClientOperation operationFromString(String operation)
	{
		if (operation == null) return ClientOperation.UNKNOWN;
		for (ClientOperation op : ClientOperation.values())
		{
			if (CLIENT_OP_STR[op.ordinal()].equals(operation)) return op;
		}
		return ClientOperation.UNKNOWN;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public static String modeToString(ClientMode mode)
	{
		int idx = mode.ordinal();
		if (idx >= CLIENT_MODE_STR.length) { return CLIENT_MODE_STR[ClientMode.UNKNOWN
		        .ordinal()]; }
		return CLIENT_MODE_STR[mode.ordinal()];
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public String toString()
	{
		return m_host + ":" + m_key;
	}
}
