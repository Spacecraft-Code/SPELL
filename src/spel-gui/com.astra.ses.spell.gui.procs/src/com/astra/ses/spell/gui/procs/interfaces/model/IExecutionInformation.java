////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.interfaces.model
// 
// FILE      : IExecutionInformation.java
//
// DATE      : 2010-08-03
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

import java.util.Collection;

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
 * IProcedureInformation handles procedure runtime configuration
 * 
 ******************************************************************************/
public interface IExecutionInformation
{
	/***************************************************************************
	 * Type of step over approach
	 **************************************************************************/
	public enum StepOverMode
	{
		STEP_OVER_ONCE, STEP_INTO_ONCE, STEP_OVER_ALWAYS, STEP_INTO_ALWAYS
	};

	/***************************************************************************
	 * Get maximum amount of display messages to store
	 **************************************************************************/
	public int getDisplayMessageCapacity();

	/***************************************************************************
	 * Get running condition
	 * 
	 * @return the running condition
	 **************************************************************************/
	public String getCondition();

	/***************************************************************************
	 * Return controlling client's key
	 * 
	 * @return
	 **************************************************************************/
	public IProcedureClient getControllingClient();

	/***************************************************************************
	 * Check if in background mode
	 * 
	 * @return
	 **************************************************************************/
	public boolean isBackground();

	/***************************************************************************
	 * Return the monitoring clients
	 * 
	 * @return a collection
	 **************************************************************************/
	public IProcedureClient[] getMonitoringClients();

	/***************************************************************************
	 * Get current error data
	 * 
	 * @return an ErrorData object containing the error information, or null if
	 *         it does not have any
	 **************************************************************************/
	public ErrorData getError();

	/***************************************************************************
	 * Get current executor status
	 * 
	 * @return the current status
	 **************************************************************************/
	public ExecutorStatus getStatus();

	/***************************************************************************
	 * Check if the executor is lost in server side
	 **************************************************************************/
	public boolean isExecutorLost();

	/***************************************************************************
	 * Get current client mode for the IProcedure that holds this object
	 * 
	 * @return
	 **************************************************************************/
	public ClientMode getClientMode();

	/***************************************************************************
	 * Return execution delay current value
	 * 
	 * @return
	 **************************************************************************/
	public int getExecutionDelay();
	
	/***************************************************************************
	 * Return prompt warning Delay
	 * 
	 * @return
	 **************************************************************************/
	public int getPromptWarningDelay();

	/***************************************************************************
	 * Get parent procedure's id
	 * 
	 * @return the parent's id
	 **************************************************************************/
	public String getParent();

	/***************************************************************************
	 * Get parent procedure calling line if applicable
	 * 
	 * @return the parents calling line
	 **************************************************************************/
	public int getParentCallingLine();

	/***************************************************************************
	 * Get origin id
	 * 
	 * @return the id
	 **************************************************************************/
	public String getOriginId();

	/***************************************************************************
	 * Get group id
	 * 
	 * @return the id
	 **************************************************************************/
	public String getGroupId();


	/***************************************************************************
	 * Get the Display messages received from the server to perform a replay
	 * 
	 * @return a {@link Collection} of {@link DisplayData} objects, which
	 *         represents all the Display messages received from the server
	 **************************************************************************/
	public DisplayData[] getDisplayMessages();

	/***************************************************************************
	 * Get stage identifier
	 * 
	 * @return the current stage's identifier
	 **************************************************************************/
	public String getStageId();

	/***************************************************************************
	 * Get stage title
	 * 
	 * @return the current stage's title
	 **************************************************************************/
	public String getStageTitle();

	/***************************************************************************
	 * Get current user action's name
	 * 
	 * @return the name of the current user action
	 **************************************************************************/
	public String getUserAction();

	/***************************************************************************
	 * Get the user action severity
	 * 
	 * @return a {@link Severity} value for the current user action
	 **************************************************************************/
	public Severity getUserActionSeverity();

	/***************************************************************************
	 * Get user action current status
	 * 
	 * @return the current status for the user action feature
	 **************************************************************************/
	public UserActionStatus getUserActionStatus();

	/***************************************************************************
	 * Check autoamtic flag
	 * 
	 * @return
	 **************************************************************************/
	public boolean isAutomatic();

	/***************************************************************************
	 * Check blocking flag
	 * 
	 * @return
	 **************************************************************************/
	public boolean isBlocking();

	/***************************************************************************
	 * Check browsable lib flag
	 * 
	 * @return
	 **************************************************************************/
	public boolean isShowLib();

	/***************************************************************************
	 * Check step by step flag
	 * 
	 * @return
	 **************************************************************************/
	public boolean isStepByStep();

	/***************************************************************************
	 * Check TC confirmation flag
	 * 
	 * @return
	 **************************************************************************/
	public boolean isForceTcConfirmation();

	/***************************************************************************
	 * Check visible flag
	 * 
	 * @return
	 **************************************************************************/
	public boolean isVisible();

	/***************************************************************************
	 * Check if the procedure execution is waiting for user input
	 * 
	 * @return true if the procedure is waiting, false otherwise
	 **************************************************************************/
	public boolean isWaitingInput();

	/***************************************************************************
	 * Fill the given {@link ExecutorInfo} object with the procedure information
	 * 
	 * @param info
	 *            the {@link ExecutorInfo} object to fill
	 **************************************************************************/
	public void visit(IExecutorInfo info);

	/***************************************************************************
	 * Fill the given {@link ExecutorConfig} object with the procedure info
	 * 
	 * @param cfg
	 *            the {@link ExecutorConfig} object to fill
	 **************************************************************************/
	public void visit(ExecutorConfig cfg);

	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getAsRunName();
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getTimeId();
}
