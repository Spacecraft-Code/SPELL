///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.commands.helpers
// 
// FILE      : CommandHelper.java
//
// DATE      : 2008-11-21 08:55
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
package com.astra.ses.spell.gui.model.commands.helpers;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.State;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

import com.astra.ses.spell.gui.model.commands.CommandResult;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class CommandHelper
{

	/***************************************************************************
	 * Executed a command without parameters
	 * 
	 * @param cmdId
	 * @return
	 **************************************************************************/
	public static CommandResult execute(String cmdId)
	{
		try
		{

			IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
			return (CommandResult) handlerService.executeCommand(cmdId, null);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return CommandResult.FAILED;
		}
	}

	/***************************************************************************
	 * Execute a command which has parameters
	 * 
	 * @param cmdId
	 *            the command id
	 * @param arguments
	 *            a map containing key-value pairs for the arguments
	 * @return a {@link CommandResult} object resuming the command execution
	 **************************************************************************/
	public static Object execute(String cmdId, Map<String, String> arguments)
	{
		Object result = null;
		/*
		 * Get the command
		 */
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = commandService.getCommand(cmdId);

		/*
		 * Get command parameters
		 */
		Parameterization[] params = null;
		IParameter[] commandParameters = null;
		try
		{
			commandParameters = command.getParameters();
		}
		catch (NotDefinedException e2)
		{
			e2.printStackTrace();
		}
		if (commandParameters != null)
		{
			params = new Parameterization[commandParameters.length];
			int i = 0;
			for (String key : arguments.keySet())
			{
				try
				{
					IParameter param = command.getParameter(key.toString());
					if (param != null)
					{
						Parameterization parm = new Parameterization(param, arguments.get(key));
						params[i] = parm;
						i++;
					}
				}
				catch (NotDefinedException e)
				{
					// Continue
				}
			}
		}
		IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
		ParameterizedCommand paramCommand = new ParameterizedCommand(command, params);
		try
		{
			result = handlerService.executeCommand(paramCommand, null);
		}
		catch (ExecutionException e)
		{
			e.printStackTrace();
		}
		catch (NotDefinedException e)
		{
			e.printStackTrace();
		}
		catch (NotEnabledException e)
		{
			e.printStackTrace();
		}
		catch (NotHandledException e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public static void executeInProgress(IRunnableWithProgress job, boolean cancellable, boolean fork)
	{
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(window.getShell());
		try
		{
			dialog.setCancelable(cancellable);
			dialog.run(fork, cancellable, job);
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public static void setToggleCommandState(String commandId, String stateId, boolean stateValue) throws ExecutionException
	{
		ICommandService svc = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = svc.getCommand(commandId);
		State state = command.getState("org.eclipse.ui.commands.toggleState");
		if (state == null)
			throw new ExecutionException("The command does not have a toggle state"); //$NON-NLS-1$
		if (!(state.getValue() instanceof Boolean))
			throw new ExecutionException("The command's toggle state doesn't contain a boolean value"); //$NON-NLS-1$
		state.setValue(new Boolean(stateValue));
	}

	public static void updateCommandEnabledState(String commandId)
	{
		try
		{
			ICommandService svc = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
			Command command = svc.getCommand(commandId);
			command.setEnabled(null);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
