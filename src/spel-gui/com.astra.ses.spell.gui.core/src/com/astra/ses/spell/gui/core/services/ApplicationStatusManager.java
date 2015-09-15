///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.services
// 
// FILE      : ApplicationStatusManager.java
//
// DATE      : Dec 5, 2012
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
package com.astra.ses.spell.gui.core.services;

import java.util.concurrent.locks.ReentrantLock;

import com.astra.ses.spell.gui.core.CoreNotifications;
import com.astra.ses.spell.gui.core.interfaces.BaseService;
import com.astra.ses.spell.gui.core.interfaces.IApplicationStatusManager;
import com.astra.ses.spell.gui.core.model.notification.ApplicationStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;

/*******************************************************************************
 * @brief Provides access to several SPELL framework status data
 * @date 20/05/08
 ******************************************************************************/
public class ApplicationStatusManager extends BaseService implements IApplicationStatusManager
{
	/** Service identifier */
	public static final String	ID	         = "com.astra.ses.spell.gui.ApplicationStatusManager";

	private ApplicationStatus currentClientStatus;
	private ReentrantLock clientStatusLock;
	private Thread memoryMonitorThread;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ApplicationStatusManager()
	{
		super(ID);
		Logger.debug("Created", Level.INIT, this);
		clientStatusLock = new ReentrantLock();
		currentClientStatus = new ApplicationStatus();
		memoryMonitorThread = new Thread()
		{
			public void run()
			{
				while(true)
				{
					try
					{
						double totalMemory = Runtime.getRuntime().totalMemory();
						double freeMemory = Runtime.getRuntime().freeMemory();
						setMemory( (freeMemory / totalMemory)*100.0 );
						CoreNotifications.get().fireApplicationStatus(currentClientStatus);
						Thread.sleep(5000);
					}
					catch(Exception ex){};
				}
			}
		};
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setup()
	{
		super.setup();
		memoryMonitorThread.start();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void cleanup()
	{
		try
		{
			memoryMonitorThread.interrupt();
		}
		catch(Exception ex)
		{
			// Nothing to do
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private void setMemory( double freePC )
	{
	    clientStatusLock.lock();
	    try
	    {
	    	currentClientStatus.freeMemoryPC = freePC;
	    }
	    finally
	    {
	    	clientStatusLock.unlock();
	    }
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public ApplicationStatus getApplicationStatus()
    {
	    clientStatusLock.lock();
	    try
	    {
	    	return currentClientStatus;
	    }
	    finally
	    {
	    	clientStatusLock.unlock();
	    }
    }
}
