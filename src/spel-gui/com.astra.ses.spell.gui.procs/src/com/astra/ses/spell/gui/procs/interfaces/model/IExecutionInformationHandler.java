////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.interfaces.model.priv
// 
// FILE      : IExecutionInformationHandler.java
//
// DATE      : Nov 16, 2010 10:20:07 AM
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
package com.astra.ses.spell.gui.procs.interfaces.model;

import com.astra.ses.spell.gui.core.interfaces.IExecutorInfo;
import com.astra.ses.spell.gui.core.interfaces.IProcedureClient;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification.UserActionStatus;
import com.astra.ses.spell.gui.core.model.server.ExecutorConfig;
import com.astra.ses.spell.gui.core.model.server.ExecutorInfo;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.types.ExecutorStatus;

/*******************************************************************************
 * 
 * IExecutionInformatinoHandler specifies which execution information can be
 * changed
 * 
 ******************************************************************************/
public interface IExecutionInformationHandler extends IExecutionInformation
{

	/***************************************************************************
	 * Adds a new display message
	 * 
	 * @param data
	 **************************************************************************/
	public void displayMessage(DisplayData data);

	/***************************************************************************
	 * Adds a new display message
	 * 
	 * @param data
	 **************************************************************************/
	public void setDisplayMessageCapacity( int capacity );

	/***************************************************************************
	 * Update execution' status
	 * 
	 * @param status
	 *            the new status
	 **************************************************************************/
	public void setExecutorStatus(ExecutorStatus status);

	/***************************************************************************
	 * Mark executor process lost
	 **************************************************************************/
	public void setExecutorLost();

	/***************************************************************************
	 * Set the current stage
	 **************************************************************************/
	public void setStage(String id, String title);

	/***************************************************************************
	 * Update the user action status
	 * 
	 * @param status
	 **************************************************************************/
	public void setUserActionStatus(String label, UserActionStatus status, Severity severity);

	/***************************************************************************
	 * Update the error information
	 * 
	 * @param error
	 *            current error notification (may be null)
	 **************************************************************************/
	public void setError(ErrorData error);

	/***************************************************************************
	 * Set by step flag
	 * 
	 * @param value
	 *            the flag value
	 **************************************************************************/
	public void setStepByStep(boolean value);

	/***************************************************************************
	 * Set by step flag
	 * 
	 * @param value
	 *            the flag value
	 **************************************************************************/
	public void setForceTcConfirmation(boolean value);

	/***************************************************************************
	 * Set show lib flag
	 * 
	 * @param value
	 *            the flag value
	 **************************************************************************/
	public void setShowLib(boolean value);

	/***************************************************************************
	 * Update exec delay value
	 * 
	 * @param msec
	 *            the new value
	 **************************************************************************/
	public void setExecutionDelay(int msec);
	
	/***************************************************************************
	 * Update Prompt Warning Delay
	 * 
	 * @param msec
	 *            the new value
	 **************************************************************************/
	public void setPromptWarningDelay(int msec);	

	/***************************************************************************
	 * Set automatic mode
	 **************************************************************************/
	public void setAutomatic(boolean automatic);

	/***************************************************************************
	 * Set blocking mode
	 **************************************************************************/
	public void setBlocking(boolean blocking);

	/***************************************************************************
	 * Set the execution condition
	 **************************************************************************/
	public void setCondition(String condition);

	/***************************************************************************
	 * Assign the client mode
	 **************************************************************************/
	public void setClientMode(ClientMode mode);

	/***************************************************************************
	 * Set background mode
	 **************************************************************************/
	public void setBackground( boolean background );

	/***************************************************************************
	 * Assign the current controlling client
	 **************************************************************************/
	public void setControllingClient( IProcedureClient client );

	/***************************************************************************
	 * Assign the list of current monitoring clients
	 **************************************************************************/
	public void setMonitoringClients(IProcedureClient[] clients);

	/***************************************************************************
	 * Add a client to the list of current monitoring clients
	 **************************************************************************/
	public void addMonitoringClient(IProcedureClient client);

	/***************************************************************************
	 * Remove a client from the list of current monitoring clients
	 **************************************************************************/
	public void removeMonitoringClient(IProcedureClient client);

	/***************************************************************************
	 * Set parent's id
	 * 
	 * @param parentId
	 *            the parent id
	 **************************************************************************/
	public void setParent(String parentId);

	/***************************************************************************
	 * Set parents calling line
	 **************************************************************************/
	public void setParentCallingLine( int line );

	/***************************************************************************
	 * Set group id
	 **************************************************************************/
	public void setGroupId( String id );

	/***************************************************************************
	 * Set origin id
	 **************************************************************************/
	public void setOriginId( String id );

	/***************************************************************************
	 * Set visible mode
	 **************************************************************************/
	public void setVisible(boolean visible);

	/***************************************************************************
	 * Update waiting input flag
	 * 
	 * @param waiting
	 **************************************************************************/
	public void setWaitingInput(boolean waiting);

	/***************************************************************************
	 * Reset this object to its original status
	 **************************************************************************/
	public void reset();

	/***************************************************************************
	 * Accept this {@link ExecutorInfo} object and retrieve its relevant
	 * information
	 * 
	 * @param info
	 *            the ExecutorInfo object
	 **************************************************************************/
	public void copyFrom(IExecutorInfo info);

	/***************************************************************************
	 * Accept this {@link ExecutorConfig} object and retrieve its relevant
	 * information
	 * 
	 * @param config
	 **************************************************************************/
	public void copyFrom(ExecutorConfig config);
}
