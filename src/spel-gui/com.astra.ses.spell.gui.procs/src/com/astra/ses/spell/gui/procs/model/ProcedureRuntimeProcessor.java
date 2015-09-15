////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : ProcedureRuntimeProcessor.java
//
// DATE      : 2010-07-30
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
////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.procs.model;

import java.util.concurrent.atomic.AtomicLong;

import com.astra.ses.spell.gui.core.model.notification.ControlNotification;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification.UserActionStatus;
import com.astra.ses.spell.gui.core.model.server.ExecutorConfig;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.ProcedureNotifications;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformationHandler;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedureRuntimeProcessor;
import com.astra.ses.spell.gui.types.ExecutorStatus;

/*******************************************************************************
 * 
 *
 ******************************************************************************/
public class ProcedureRuntimeProcessor implements IProcedureRuntimeProcessor
{
	/** Listener id */
	private static final String LISTENER_ID = "com.astra.ses.spell.gui.procs.model.Procedure";

	/** Holds the procedure model */
	private IProcedure m_model;
	/** Ready flag */
	private boolean m_ready = false;
	/** Holds the notification sequence for status */
	private AtomicLong m_statusSequence;
	/** Holds the notification sequence for stack */
	private AtomicLong m_stackSequence;
	/** Holds the buffer for unordered stack notifications */
	private StackNotificationBuffer m_stackBuffer;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ProcedureRuntimeProcessor(IProcedure model)
	{
		m_model = model;
		m_statusSequence = new AtomicLong(0);
		m_stackSequence = new AtomicLong(-1);
		m_stackBuffer = new StackNotificationBuffer();
	}

	/*
	 * IProcedureRuntime methods
	 */

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getListenerId()
	{
		return LISTENER_ID;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void reset()
	{
		m_statusSequence.set(0);
		m_stackSequence.set(-1);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureDisplay(DisplayData data)
	{
		((IExecutionInformationHandler) m_model.getRuntimeInformation()).displayMessage(data);
		// Redirect the data to the consumers
		ProcedureNotifications.get().fireProcedureDisplay(m_model, data);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureError(ErrorData data)
	{
		((IExecutionInformationHandler) m_model.getRuntimeInformation()).setExecutorStatus(ExecutorStatus.ERROR);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void clearNotifications()
	{
		m_model.getExecutionManager().clearNotifications();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureItem(ItemNotification data)
	{
		m_model.getExecutionManager().onItemNotification(data);
		// Redirect the data to the consumers
		ProcedureNotifications.get().fireProcedureItem(m_model, data);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureStack(StackNotification data)
	{
		//System.out.println("[" + data.getSequence() + "]  " + data.getStackType() + ": " + Arrays.toString(data.getStackPosition().toArray()) );

		long ss = m_stackSequence.get();
		if ( data.getSequence() > ss && (ss != -1))
		{
			//System.out.println("STORE " + data.getStageId() + " (SS=" + ss + ")");
			m_stackBuffer.put(data);
		}
		else
		{
			StackNotification sdata = data;
			
			if (ss == -1 && !m_model.getExecutionManager().isInReplay())
			{
				m_stackSequence.set(data.getSequence());
				ss = data.getSequence();
				//System.out.println("SET SS=" + data.getSequence());
			}
			
			do
			{
				m_model.getExecutionManager().onStackNotification(sdata);
				//System.out.println("RELEASE " + sdata.getSequence());
				((IExecutionInformationHandler) m_model.getRuntimeInformation()).setStage(sdata.getStageId(), sdata.getStageTitle());
				// Redirect the data to the consumers
				ProcedureNotifications.get().fireProcedureStack(m_model, sdata);
				if (ss != -1)
				{
					long nextSequence = m_stackSequence.incrementAndGet();
					sdata = m_stackBuffer.get(nextSequence);
				}
				else
				{
					sdata = null;
				}
			}
			while(sdata != null);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureStatus(StatusNotification data)
	{
		Logger.debug("Notified status: " + data.getStatus(), Level.PROC, this);

		if (!m_ready && data.getStatus().equals(ExecutorStatus.PAUSED))
		{
			m_ready = true;
			Logger.debug("Set procedure ready", Level.PROC, this);
			m_model.getExecutionManager().onProcedureReady();
		}

		if (m_statusSequence.get() == 0)
		{
			m_statusSequence.set(data.getSequence());
		}
		else
		{
			// Discard possible notifications that may come in bad order due to network issues
			if (m_statusSequence.get()>data.getSequence())
			{
				Logger.warning("Discarded status notification due to sequence number " + data.getSequence() + "<" + m_statusSequence.get(), Level.PROC, this);
				return;
			}
			m_statusSequence.set(data.getSequence());
		}

		// Update the status
		Logger.debug("Update the status in model", Level.PROC, this);
		m_model.getController().setExecutorStatus(data.getStatus());
		m_model.getExecutionManager().onStatusNotification(data);

		if (!m_model.isInReplayMode())
		{
			Logger.debug("Not in replay mode", Level.PROC, this);
			if (data.getStatus() == ExecutorStatus.RELOADING)
			{
				m_model.reset();
				ProcedureNotifications.get().fireModelReset(m_model);
			}
		}

		Logger.info("Notify status event", Level.PROC, this);

		// Redirect the data to the consumers
		ProcedureNotifications.get().fireProcedureStatus(m_model, data);
		
		Logger.debug("Process status notification finished", Level.PROC, this);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureUserAction(UserActionNotification data)
	{
		UserActionStatus status = data.getUserActionStatus();
		Severity severity = data.getSeverity();
		String action = data.getAction();
		((IExecutionInformationHandler) m_model.getRuntimeInformation()).setUserActionStatus(action,status, severity);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureConfiguration(ExecutorConfig data)
	{
		// Nothing to do at the moment
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureControl(ControlNotification data)
	{
		// To be coded if the procedure model requires this info
	}
}
