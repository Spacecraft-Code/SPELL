///////////////////////////////////////////////////////////////////////////////
//
// Copyright (C) 2013 GMV S.A.U.
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
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.clock;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.astra.ses.spell.gui.core.CoreNotifications;
import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreContextOperationListener;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;

/**
 * This class provides the current time in the server.
 */
public class ClockService implements ICoreContextOperationListener
{
	/**
	 * This class provides a thread that updates the time offset.
	 */
	private class UpdateThread extends Thread
	{
		/** The period between clock offset refreshes */
		private static final int REFRESH_MS = 20000;

		/** The thread runs while this variable is true */
		public volatile boolean m_keepRunning = true;
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run()
		{
			while (m_keepRunning)
			{
				if (m_contextProxy.isConnected())
				{
					Date serverDate = m_contextProxy.getCurrentTime();
					if (serverDate != null)
					{
						Calendar serverTime = Calendar.getInstance();
						serverTime.setTimeZone(TimeZone.getTimeZone("GMT"));
						serverTime.setTime(serverDate);
	
						Calendar localTime = Calendar.getInstance();
						localTime.setTimeZone(TimeZone.getTimeZone("GMT"));
					
						m_offsetMillis = serverTime.getTimeInMillis() - localTime.getTimeInMillis();
					}
					else
					{
						Logger.error("Unable to obtain context time", Level.PROC, this);
					}
				}
				
				try
				{
					Thread.sleep(REFRESH_MS);
				}
				catch (InterruptedException e)
				{
					// Ok, probably someone asked the thread to stop
				}
			}
		}
		
		/** Stop the thread when possible */
		public void finish()
		{
			m_keepRunning = false;
			interrupt();
		}		
	}
	
	/** The id of this class for connecting to the CoreNotifications services */ 
	public static final String ID = "com.astra.ses.spell.gui.clock.ClockService";
	
	/** The interface with the context in the server */
	private IContextProxy m_contextProxy;
	
	/** Offset between local time and time in the server */
	private volatile long m_offsetMillis;
	
	/** The thread updating the offset between local and server time */
	private volatile UpdateThread m_updateThread;
	
	/**
	 * Constructor
	 */
	public ClockService()
	{
		m_contextProxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		m_offsetMillis = 0;
	}
	
	/**
	 * Returns the current time in the server.
	 * This is the time that the procedures see during its execution.
	 */
	public Date getTime()
	{
		Calendar date = Calendar.getInstance();
		date.setTimeZone(TimeZone.getTimeZone("GMT"));
		date.setTimeInMillis(date.getTimeInMillis() + m_offsetMillis);
		return date.getTime();
	}
	
	/**
	 * Starts the clock service
	 */
	public void start()
	{
		CoreNotifications.get().addListener(this, ICoreContextOperationListener.class);
	}
	
	/**
	 * Stops the clock service
	 */
	public void finish()
	{
		CoreNotifications.get().removeListener(this);
		if (m_updateThread != null)
		{
			m_updateThread.finish();
		}
	}

	/* (non-Javadoc)
	 * @see com.astra.ses.spell.gui.core.interfaces.IBaseListener#getListenerId()
	 */
	@Override
	public String getListenerId() {
		return ID;
	}

	/* (non-Javadoc)
	 * @see com.astra.ses.spell.gui.core.interfaces.ICoreContextOperationListener#notifyContextAttached(com.astra.ses.spell.gui.core.model.server.ContextInfo)
	 */
	@Override
	public void notifyContextAttached(ContextInfo ctx)
	{
		m_updateThread = new UpdateThread();
		m_updateThread.start();
	}

	/* (non-Javadoc)
	 * @see com.astra.ses.spell.gui.core.interfaces.ICoreContextOperationListener#notifyContextDetached()
	 */
	@Override
	public void notifyContextDetached()
	{
		if (m_updateThread != null)
		{
			m_updateThread.finish();
			m_updateThread = null;
		}
	}

	/* (non-Javadoc)
	 * @see com.astra.ses.spell.gui.core.interfaces.ICoreContextOperationListener#notifyContextError(com.astra.ses.spell.gui.core.model.notification.ErrorData)
	 */
	@Override
	public void notifyContextError(ErrorData error)
	{
		// do nothing
	}
}
