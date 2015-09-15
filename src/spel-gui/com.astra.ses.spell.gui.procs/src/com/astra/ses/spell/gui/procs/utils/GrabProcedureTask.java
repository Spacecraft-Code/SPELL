///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.utils
// 
// FILE      : GrabProcedureTask.java
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
package com.astra.ses.spell.gui.procs.utils;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.model.AsRunReplayResult;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.types.ExecutorStatus;

public class GrabProcedureTask extends Thread
{
	private IProcedureManager m_mgr;
	private String m_procId;
	private boolean m_downloadData;
	private ClientMode m_mode;

	/************************************************************************************
	 * Constructor
	 * @param mgr
	 * @param procId
	 * @param downloadData
	 ***********************************************************************************/
	public GrabProcedureTask(IProcedureManager mgr, String procId, ClientMode mode, boolean downloadData)
	{
		m_procId = procId;
		m_mgr = mgr;
		m_downloadData = downloadData;
		m_mode = mode;
	}

	/************************************************************************************
	 *
	 ***********************************************************************************/
	public void run()
	{
		Logger.debug("Grab procedure " + m_procId + " task started (download: " + m_downloadData + ")", Level.PROC, this);
		ExecutorStatus status = ExecutorStatus.UNINIT;
		IProcedure proc = null;
		while (status == ExecutorStatus.UNINIT || status == ExecutorStatus.LOADED)
		{
			try
			{
				proc = m_mgr.getRemoteProcedure(m_procId);
				status = proc.getRuntimeInformation().getStatus();
			}
			finally
			{
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
				}
				;
			}
		}
		Logger.debug("Procedure status is now " + status , Level.PROC, this);
		
		AsRunReplayResult result = null;
		// A null instance given as ASRUN result will prevent the system from
		// downloading and processing the ASRUN
		if (m_downloadData)
		{
			result = new AsRunReplayResult();
		}
		
		if (m_mode.equals(ClientMode.CONTROL))
		{
			Logger.debug("Start operation to control procedure", Level.PROC, this);
			m_mgr.controlProcedure(m_procId, result, new NullProgressMonitor());
		}
		else
		{
			Logger.debug("Start operation to monitor procedure", Level.PROC, this);
			m_mgr.monitorProcedure(m_procId, result, new NullProgressMonitor());
		}
			
		Logger.debug("Client mode        : " + proc.getRuntimeInformation().getClientMode(), Level.PROC, this);
		Logger.debug("Client info        : " + proc.getRuntimeInformation().getControllingClient().getKey(), Level.PROC, this);
		Logger.debug("Automatic procedure: " + proc.getRuntimeInformation().isAutomatic(), Level.PROC, this);
		Logger.debug("Visible            : " + proc.getRuntimeInformation().isVisible(), Level.PROC, this);

		// Once it is controlled, run it if it is automatic and visible.
		// IMPORTANT if it is not visible, the procedure will run by itself.
		if (proc.getRuntimeInformation().isAutomatic() && proc.getRuntimeInformation().isVisible())
		{
			Logger.debug("Invoke run command", Level.PROC, this);
			m_mgr.getProcedure(m_procId).getController().run();
		}
	}

}
