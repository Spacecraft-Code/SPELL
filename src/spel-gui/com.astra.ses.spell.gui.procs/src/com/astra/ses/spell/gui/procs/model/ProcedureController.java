////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : ProcedureController.java
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

import com.astra.ses.spell.gui.core.exceptions.CommandFailed;
import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.IExecutorInfo;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.InputData;
import com.astra.ses.spell.gui.core.model.server.ExecutorConfig;
import com.astra.ses.spell.gui.core.model.types.BreakpointType;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.ProcedureNotifications;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformationHandler;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedureController;
import com.astra.ses.spell.gui.types.ExecutorCommand;
import com.astra.ses.spell.gui.types.ExecutorStatus;

/*******************************************************************************
 * 
 * Procedure controller
 * 
 ******************************************************************************/
public class ProcedureController implements IProcedureController
{
	/** Context proxy */
	private static IContextProxy s_proxy = null;
	/** Listener id */
	private static final String LISTENER_ID = "com.astra.ses.spell.gui.procs.model.Procedure";

	/** Holds the procedure model */
	private IProcedure m_model;
	private InputData m_promptData;

	/*
	 * Static block to retrieve the context proxy
	 */
	static
	{
		s_proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ProcedureController(IProcedure model)
	{
		m_model = model;
		m_promptData = null;
	}

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
	private IExecutionInformationHandler getInfo()
	{
		return (IExecutionInformationHandler) m_model.getRuntimeInformation();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void issueCommand(ExecutorCommand cmd, String[] args)
	{
		ExecutorStatus status = getInfo().getStatus();
		if (!cmd.validate(null,status))
		{
			throw new CommandFailed("Cannot execute command, invalid procedure status");
		}
		releaseExecutorCommand(cmd, args);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void refresh() throws Exception
	{
		updateInfo();
		updateConfig();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public InputData getPromptData()
	{
		return m_promptData;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void updateInfo() throws Exception
	{
		if (s_proxy.isConnected())
		{
			Logger.debug("Refreshing execution model", Level.PROC, this);
			String procId = m_model.getProcId();
			try
			{
				IExecutorInfo info = s_proxy.getExecutorInfo(procId);
				getInfo().copyFrom(info);
				Logger.debug("Execution model updated", Level.PROC, this);
			}
			catch(Exception ex)
			{
				getInfo().setExecutorStatus(ExecutorStatus.UNKNOWN);
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void updateConfig() throws Exception
	{
		if (s_proxy.isConnected())
		{
			Logger.debug("Refreshing configuration model", Level.PROC, this);
			ExecutorConfig cfg = new ExecutorConfig(m_model.getProcId());
			s_proxy.updateExecutorConfig(m_model.getProcId(), cfg);
			getInfo().copyFrom(cfg);
			Logger.debug("Configuration model updated", Level.PROC, this);
		}
	}

	/*
	 * ==========================================================================
	 * Breakpoints management
	 * =========================================================================
	 */

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void clearBreakpoints()
	{
		// Send the request
		try
		{
			s_proxy.clearBreakpoints(m_model.getProcId());
			m_model.getExecutionManager().clearBreakpoints();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void setBreakpoint(int lineNumber, BreakpointType type)
	{
		// Send the request
		try
		{
			
			s_proxy.toggleBreakpoint(m_model.getProcId(), m_model.getExecutionManager().getCurrentCode(), lineNumber, type);
			m_model.getExecutionManager().setBreakpoint(lineNumber, type);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void gotoLine(int lineNumber) throws CommandFailed
	{
		m_model.getExecutionManager().beforeGoto();
		releaseExecutorCommand(ExecutorCommand.GOTO, new String[] { "", String.valueOf(lineNumber) });
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void gotoLabel(String label) throws CommandFailed
	{
		m_model.getExecutionManager().beforeGoto();
		releaseExecutorCommand(ExecutorCommand.GOTO, new String[] { label, "" });
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void moveStack( int level )
	{
		System.err.println("SEND STACK MOVE " + level);
		s_proxy.viewNodeAtDepth(m_model.getProcId(), level);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void script(String script) throws CommandFailed
	{
		if (script != null && (!script.isEmpty()))
		{
			script = script.replaceAll("'", "\"");
		}
		releaseExecutorCommand(ExecutorCommand.SCRIPT, new String[] { script });
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setBrowsableLib(boolean showLib)
	{
		getInfo().setShowLib(showLib);
		ProcedureNotifications.get().fireModelConfigured(m_model);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setExecutorStatus(ExecutorStatus status)
	{
		getInfo().setExecutorStatus(status);
		switch (status)
		{
		case PAUSED:
			/*
			 * If there is a temporary breakpoint at the current line Then there
			 * is need to remove it
			 */
			m_model.getExecutionManager().removeBreakpoint();
			break;
		default:
			break;
		}
	}

	@Override
	public void setError(ErrorData data)
	{
		getInfo().setError(data);
		m_model.getRuntimeProcessor().notifyProcedureError(data);

		if (!m_model.isInReplayMode())
		{
			// Redirect the data to the consumers
			ProcedureNotifications.get().fireProcedureError(m_model, data);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setRunInto(boolean runInto)
	{
		// Update the internal information model
		m_model.getExecutionManager().setRunInto(runInto);
		
		// If we are controlling, perform the change request to the server
		if (getInfo().getClientMode().equals(ClientMode.CONTROL))
		{
			try
			{
				// Send the request to configure the server
				ExecutorConfig config = new ExecutorConfig(m_model.getProcId());
				getInfo().visit(config);
				s_proxy.setExecutorConfiguration(m_model.getProcId(), config);
				ProcedureNotifications.get().fireModelConfigured(m_model);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setExecutionDelay(int msec)
	{
		// Update the internal information model
		getInfo().setExecutionDelay(msec);
		// If we are controlling, perform the change request to the server
		if (getInfo().getClientMode().equals(ClientMode.CONTROL))
		{
			try
			{
				// Send the request to configure the server
				ExecutorConfig config = new ExecutorConfig(m_model.getProcId());
				getInfo().visit(config);
				s_proxy.setExecutorConfiguration(m_model.getProcId(), config);
				ProcedureNotifications.get().fireModelConfigured(m_model);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setPromptWarningDelay(int msec)
	{
		// Update the internal information model
		getInfo().setPromptWarningDelay(msec);
		// If we are controlling, perform the change request to the server
		if (getInfo().getClientMode().equals(ClientMode.CONTROL))
		{
			try
			{
				// Send the request to configure the server
				ExecutorConfig config = new ExecutorConfig(m_model.getProcId());
				getInfo().visit(config);
				s_proxy.setExecutorConfiguration(m_model.getProcId(), config);
				ProcedureNotifications.get().fireModelConfigured(m_model);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}	

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setStepByStep(boolean value)
	{
		getInfo().setStepByStep(value);
		// If we are controlling, perform the change request to the server
		if (getInfo().getClientMode().equals(ClientMode.CONTROL))
		{
			try
			{
				// Send the request to configure the server
				ExecutorConfig config = new ExecutorConfig(m_model.getProcId());
				getInfo().visit(config);
				s_proxy.setExecutorConfiguration(m_model.getProcId(), config);
				ProcedureNotifications.get().fireModelConfigured(m_model);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setForceTcConfirmation(boolean value)
	{
		getInfo().setForceTcConfirmation(value);
		// If we are controlling, perform the change request to the server
		if (getInfo().getClientMode().equals(ClientMode.CONTROL))
		{
			try
			{
				// Send the request to configure the server
				ExecutorConfig config = new ExecutorConfig(m_model.getProcId());
				getInfo().visit(config);
				s_proxy.setExecutorConfiguration(m_model.getProcId(), config);
				ProcedureNotifications.get().fireModelConfigured(m_model);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedurePrompt(InputData inputData)
	{
		m_promptData = inputData;
		getInfo().setWaitingInput(true);
		// Rredirect the data to the consumers
		ProcedureNotifications.get().firePrompt(m_model);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureCancelPrompt(InputData inputData)
	{
		m_promptData = null;
		getInfo().setWaitingInput(false);
		// Redirect the data to the consumers
		ProcedureNotifications.get().fireCancelPrompt(m_model);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureFinishPrompt(InputData inputData)
	{
		m_promptData = null;
		getInfo().setWaitingInput(false);
		// Redirect the data to the consumers
		ProcedureNotifications.get().fireFinishPrompt(m_model);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void abort()
	{
		releaseExecutorCommand(ExecutorCommand.ABORT);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void pause()
	{
		releaseExecutorCommand(ExecutorCommand.PAUSE);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void interrupt()
	{
		releaseExecutorCommand(ExecutorCommand.INTERRUPT);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void recover()
	{
		releaseExecutorCommand(ExecutorCommand.RECOVER);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void reload()
	{
		releaseExecutorCommand(ExecutorCommand.RELOAD);
		m_model.getExecutionManager().reset();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void run()
	{
		releaseExecutorCommand(ExecutorCommand.RUN);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void skip()
	{
		m_model.getExecutionManager().doSkip();
		releaseExecutorCommand(ExecutorCommand.SKIP);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void step()
	{
		m_model.getExecutionManager().doStepInto();
		releaseExecutorCommand(ExecutorCommand.STEP);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void stepOver()
	{
		m_model.getExecutionManager().doStepOver();
		releaseExecutorCommand(ExecutorCommand.STEP_OVER);
	}

	/***************************************************************************
	 * Send an executor command with the given command identifier
	 * 
	 * @param cmdId
	 *            the command identifier
	 * @args an array list with the arguments
	 **************************************************************************/
	private void releaseExecutorCommand(ExecutorCommand cmd, String[] args)
	{
		Logger.debug("Release command: " + cmd.getId(), Level.PROC, this);

		/*
		 * Add the procedure id at first position
		 */
		String[] argsWithProc = new String[args.length + 1];
		for (int i = 0; i < args.length; i++)
		{
			argsWithProc[i + 1] = args[i];
		}
		argsWithProc[0] = m_model.getProcId();

		/*
		 * Issue the command
		 */
		s_proxy.command(cmd, argsWithProc);
	}

	/***************************************************************************
	 * Send an executor command with the given command identifier
	 * 
	 * @param cmdId
	 *            the command identifier
	 **************************************************************************/
	private void releaseExecutorCommand(ExecutorCommand cmd)
	{
		releaseExecutorCommand(cmd, new String[0]);
	}
}
