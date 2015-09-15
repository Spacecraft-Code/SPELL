///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.server
// 
// FILE      : ContextInfo.java
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
import com.astra.ses.spell.gui.core.model.types.ContextStatus;

/*******************************************************************************
 * @brief Data structure holding the connection info for a SPELL server
 * @date 28/04/08
 ******************************************************************************/
public class ContextInfo extends ServerInfo
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	private String	      m_driver;
	private String	      m_family;
	private String	      m_gcs;
	private String	      m_sc;
	private int	          m_maxProcs;
	private ContextStatus m_status;
	private String	      m_description;

	/***************************************************************************
	 * Constructor
	 * 
	 * @throws MessageException
	 **************************************************************************/
	public ContextInfo(String ctxName)
	{
		super();
		setName(ctxName);
		m_driver = "";
		m_family = "";
		m_gcs = "";
		m_sc = "";
		m_status = ContextStatus.UNKNOWN;
		m_description = "";
		m_maxProcs = 20;
	}

	/***************************************************************************
	 * Obtain the context spacecraft
	 * 
	 * @return The spacecraft ID
	 **************************************************************************/
	public String getSC()
	{
		return m_sc;
	}

	/***************************************************************************
	 * Obtain the maximum number of procedures on context
	 * 
	 * @return The maximum number of procedures
	 **************************************************************************/
	public int getMaxProc()
	{
		return m_maxProcs;
	}

	/***************************************************************************
	 * Set the maximum number of procedures on context
	 **************************************************************************/
	public void setMaxProc(String maxproc)
	{
		m_maxProcs = Integer.parseInt(maxproc);
	}

	/***************************************************************************
	 * Assign the context spacecraft
	 * 
	 * @param sc
	 *            The spacecraft ID
	 **************************************************************************/
	public void setSC(String sc)
	{
		m_sc = sc;
	}

	/***************************************************************************
	 * Assign the GCS host for this context
	 * 
	 * @param gcs
	 *            The GCS host
	 **************************************************************************/
	public void setGCS(String gcs)
	{
		m_gcs = gcs;
	}

	/***************************************************************************
	 * Obtain the GCS for this context
	 * 
	 * @return The GCS host name
	 **************************************************************************/
	public String getGCS()
	{
		return m_gcs;
	}

	/***************************************************************************
	 * Set the driver name for this context
	 * 
	 * @param driver
	 *            The driver name
	 **************************************************************************/
	public void setDriver(String driver)
	{
		m_driver = driver;
	}

	/***************************************************************************
	 * Obtain the driver name for this context
	 * 
	 * @return The driver name
	 **************************************************************************/
	public String getDriver()
	{
		return m_driver;
	}

	/***************************************************************************
	 * Set the family of the GCS
	 * 
	 * @param fmName
	 *            The family name
	 **************************************************************************/
	public void setFamily(String fmName)
	{
		m_family = fmName;
	}

	/***************************************************************************
	 * Obtain the family of the GCS
	 * 
	 * @return The family name
	 **************************************************************************/
	public String getFamily()
	{
		return m_family;
	}

	/***************************************************************************
	 * Obtain the status of the context process
	 * 
	 * @return The status of the process
	 **************************************************************************/
	public ContextStatus getStatus()
	{
		return m_status;
	}

	/***************************************************************************
	 * Assign the status of the context process
	 * 
	 * @param st
	 *            Status of the process
	 **************************************************************************/
	public void setStatus(ContextStatus st)
	{
		m_status = st;
	}

	/***************************************************************************
	 * Check if the context process is running
	 * 
	 * @return True if the context process is running
	 **************************************************************************/
	public boolean isRunning()
	{
		return (m_status == ContextStatus.RUNNING);
	}

	/***************************************************************************
	 * Set the context description
	 * 
	 * @param desc
	 *            Description string
	 **************************************************************************/
	public void setDescription(String desc)
	{
		m_description = desc;
	}

	/***************************************************************************
	 * Obtain the context description
	 * 
	 * @return The context description
	 **************************************************************************/
	public String getDescription()
	{
		return m_description;
	}
}
