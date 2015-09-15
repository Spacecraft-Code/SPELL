///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.watchvariables.commands
// 
// FILE      : ChangeVariable.java
//
// DATE      : 2010-10-01
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
package com.astra.ses.spell.gui.watchvariables.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.types.ExecutorStatus;
import com.astra.ses.spell.gui.watchvariables.commands.args.IWatchCommandArgument;
import com.astra.ses.spell.gui.watchvariables.interfaces.IWatchVariables;

/*******************************************************************************
 * 
 * {@link ChangeVariable} command asigns a new value to a variable. Both value
 * and variable name are passed as arguments
 * 
 ******************************************************************************/
public class ChangeVariable extends AbstractHandler
{

	/** Command id */
	public static final String ID = "com.astra.ses.spell.gui.commands.ChangeVariable";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		/*
		 * Retrieve command parameters
		 */
		String procId = event.getParameter(IWatchCommandArgument.PROCEDURE_ID);
		String var = event.getParameter(IWatchCommandArgument.VARIABLE_NAME);
		String valueExpression = event.getParameter(IWatchCommandArgument.VARIABLE_VALUE_EXPR);
		boolean isGlobal = Boolean.parseBoolean(event.getParameter(IWatchCommandArgument.VARIABLE_GLOBAL));
		// If no value has been set to the variable, then no assignment is
		// applied
		if (valueExpression.isEmpty())
		{
			return CommandResult.NO_EFFECT;
		}
		/*
		 * Get the procedure
		 */
		IProcedureManager mgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
		IProcedure proc = mgr.getProcedure(procId);
		/*
		 * Check if the Executor status allows to send messages
		 */
		ExecutorStatus st = proc.getRuntimeInformation().getStatus();
		switch (st)
		{
		case INTERRUPTED:
		case PAUSED:
		case PROMPT:
		case WAITING:
			IWatchVariables watch = (IWatchVariables) ServiceManager.get(IWatchVariables.class);
			watch.getVariableManager(procId).changeVariable(var, valueExpression, isGlobal);
			return CommandResult.SUCCESS;
		default:
			return CommandResult.NO_EFFECT;
		}
	}

}
