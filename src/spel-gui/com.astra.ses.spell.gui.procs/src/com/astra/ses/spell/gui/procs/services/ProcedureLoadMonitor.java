///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.services
// 
// FILE      : ProcedureLoadMonitor.java
//
// DATE      : 2008-11-24 08:34
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
package com.astra.ses.spell.gui.procs.services;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.exceptions.LoadFailed;
import com.astra.ses.spell.gui.types.ExecutorStatus;

/*******************************************************************************
 * @brief Manages the load process of a procedure
 * @date 09/10/07
 ******************************************************************************/
class ProcedureLoadMonitor 
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
	// PUBLIC ------------------------------------------------------------------
	// PRIVATE -----------------------------------------------------------------
	/** Holds the execution until procedure is loaded */
	private boolean 	m_loadSuccess;
	private Lock 		m_loadLock;
	private Condition 	m_loadCondition;
	private String 		m_instanceId;

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	public ProcedureLoadMonitor( String instanceId )
	{
		m_instanceId = instanceId;
		m_loadSuccess = false;
		m_loadLock	= new ReentrantLock();
		m_loadCondition = m_loadLock.newCondition();
	}

	/***************************************************************************
	 * Receive model status change
	 **************************************************************************/
	void setProcedureStatus( ExecutorStatus status )
	{
		if (status.equals(ExecutorStatus.LOADED))
		{
			m_loadSuccess = true;
			signalProcedureLoaded();
		}
		else if (status.equals(ExecutorStatus.ERROR) || status.equals(ExecutorStatus.ABORTED))
		{
			m_loadSuccess = false;
			signalProcedureLoaded();
		}
		// Do nothing for other status codes
	}

	/***************************************************************************
	 * Wait a signal until a procedure finishes the loading process
	 **************************************************************************/
	boolean waitProcedureLoaded()
	{
		m_loadLock.lock();
		try
		{
			// Wait 15 seconds
			Logger.debug("Waiting for procedure load signal", Level.PROC, this);
			long remaining = m_loadCondition.awaitNanos((long) 60e9);
			if (remaining <= 0)
			{ 
				throw new LoadFailed("Could not open the procedure '" + m_instanceId
			                + "'\nIt took too long to load. Please try again."); 
			}
			Logger.debug("Received procedure load signal", Level.PROC, this);
		}
		catch (InterruptedException err)
		{
			throw new LoadFailed("Could not load the procedure '" + m_instanceId
			        + "'");
		}
		finally
		{
			m_loadLock.unlock();
		}
		return m_loadSuccess;
	}

	/***************************************************************************
	 * Trigger the signal when a procedure finishes the loading process
	 **************************************************************************/
	private void signalProcedureLoaded()
	{
		m_loadLock.lock();
		try
		{
			Logger.debug("Signalling procedure load", Level.PROC, this);
			m_loadCondition.signalAll();
		}
		finally
		{
			m_loadLock.unlock();
		}
	}
}
