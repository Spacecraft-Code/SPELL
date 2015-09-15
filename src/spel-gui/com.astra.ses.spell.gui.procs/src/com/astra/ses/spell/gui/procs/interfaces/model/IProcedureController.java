////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.interfaces.model
// 
// FILE      : IProcedureController.java
//
// DATE      : 2010-08-13
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

import com.astra.ses.spell.gui.core.exceptions.CommandFailed;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreProcedureInputListener;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.InputData;
import com.astra.ses.spell.gui.core.model.types.BreakpointType;
import com.astra.ses.spell.gui.procs.interfaces.exceptions.UninitProcedureException;
import com.astra.ses.spell.gui.types.ExecutorCommand;
import com.astra.ses.spell.gui.types.ExecutorStatus;

/*******************************************************************************
 * 
 * IProcedureController determines which actions can be performed on a
 * {@link IProcedure} object
 * 
 ******************************************************************************/
public interface IProcedureController extends ICoreProcedureInputListener
{
	/***************************************************************************
	 * Set error information
	 **************************************************************************/
	public void setError(ErrorData data);

	/***************************************************************************
	 * Abort procedure execution
	 **************************************************************************/
	public void abort();

	/***************************************************************************
	 * Remove all the breakpoints in the procedure
	 **************************************************************************/
	public void clearBreakpoints();

	/***************************************************************************
	 * Branch to the line indexed with lineNumber
	 * 
	 * @param lineNumber
	 *            the line number to branch
	 **************************************************************************/
	public void gotoLine(int lineNumber);

	/***************************************************************************
	 * Branch to a line tagged with the given label
	 * 
	 * @param label
	 *            the label which with the line has been tagged
	 **************************************************************************/
	public void gotoLabel(String label);

	/***************************************************************************
	 * Pause procedure execution
	 **************************************************************************/
	public void pause();

	/***************************************************************************
	 * Interrupt the ongoing driver operation
	 **************************************************************************/
	public void interrupt();

	/***************************************************************************
	 * Send a command
	 * 
	 * @param cmd
	 *            the command to be sent
	 * @param args
	 *            the command arguments
	 **************************************************************************/
	public void issueCommand(ExecutorCommand cmd, String[] args)
	        throws CommandFailed;

	/***************************************************************************
	 * Recover procedure execution from a crash
	 **************************************************************************/
	public void recover();

	/***************************************************************************
	 * Reload the procedure
	 **************************************************************************/
	public void reload();

	/***************************************************************************
	 * Refresh this procedure information
	 **************************************************************************/
	public void refresh() throws Exception;

	/***************************************************************************
	 * Update info
	 **************************************************************************/
	public void updateInfo() throws Exception;

	/***************************************************************************
	 * Update configuration
	 **************************************************************************/
	public void updateConfig() throws Exception;

	/***************************************************************************
	 * Run the procedure
	 **************************************************************************/
	public void run();

	/***************************************************************************
	 * Perform the given script
	 * 
	 * @param script
	 *            the script source
	 **************************************************************************/
	public void script(String script);

	/***************************************************************************
	 * Set browsable lib flag value
	 * 
	 * @param showLib
	 *            the new value
	 **************************************************************************/
	public void setBrowsableLib(boolean showLib);

	/***************************************************************************
	 * Change run into flag value
	 * 
	 * @param runInto
	 *            the new value
	 **************************************************************************/
	public void setRunInto(boolean runInto);

	/***************************************************************************
	 * Update Step by step flag value
	 * 
	 * @param value
	 *            the new value
	 **************************************************************************/
	public void setStepByStep(boolean value);

	/***************************************************************************
	 * Update TC confirmation flag value
	 * 
	 * @param value
	 *            the new value
	 **************************************************************************/
	public void setForceTcConfirmation(boolean value);

	/***************************************************************************
	 * Set the execution delay between lines
	 * 
	 * @param msec
	 *            the msecs
	 **************************************************************************/
	public void setExecutionDelay(int msec);
	
	/***************************************************************************
	 * Set the prompt warning delay
	 * 
	 * @param msec
	 *            the msecs
	 **************************************************************************/	
	public void setPromptWarningDelay(int msec);

	/***************************************************************************
	 * Update the executor status
	 * 
	 * @param status
	 *            the new status
	 **************************************************************************/
	public void setExecutorStatus(ExecutorStatus status);

	/***************************************************************************
	 * Add or remove a breakpoint at the given line
	 * 
	 * @param lineNumber
	 *            the line number (using 1 for the first row)
	 * @throws UninitProcedureException
	 **************************************************************************/
	public void setBreakpoint(int lineNumber, BreakpointType type);

	/***************************************************************************
	 * Skip a line during procedure execution
	 **************************************************************************/
	public void skip();

	/***************************************************************************
	 * Perform a step in the procedure execution
	 **************************************************************************/
	public void step();

	/***************************************************************************
	 * Perform a step over during procedure execution
	 **************************************************************************/
	public void stepOver();

	/***************************************************************************
	 * Move the stack level
	 **************************************************************************/
	public void moveStack( int depth );

	/***************************************************************************
	 * Perform a step over during procedure execution
	 **************************************************************************/
	public InputData getPromptData();
}
