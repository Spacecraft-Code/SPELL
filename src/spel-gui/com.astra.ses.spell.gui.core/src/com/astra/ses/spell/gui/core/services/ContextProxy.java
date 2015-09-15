///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.services
// 
// FILE      : ContextProxy.java
//
// DATE      : 2008-11-21 08:58
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
package com.astra.ses.spell.gui.core.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.gui.core.CoreNotifications;
import com.astra.ses.spell.gui.core.comm.messages.MessageException;
import com.astra.ses.spell.gui.core.comm.messages.SPELLlistenerLost;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageAttachExec;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageBackgroundExec;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageBreakpoint;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageClearBreakpoints;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageClientInfo;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageClientOperation;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageClose;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageCloseExec;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageCtxOperation;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageDetachExec;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageDisableUserAction;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageDismissUserAction;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageDisplay;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageEnableUserAction;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageError;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageExecConfigChanged;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageExecInfo;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageExecList;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageExecOperation;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageExecStatus;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageGetCurrentTime;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageGetDictionary;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageGetExecConfig;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageGetExecutorDefaults;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageGetInputFile;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageGetInstance;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageKillExec;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageNotify;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageNotifyAsync;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageOneway;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageOpenExec;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageProcCode;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageProcInfo;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageProcList;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessagePrompt;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessagePromptEnd;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessagePromptHelper;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessagePromptStart;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageRecoverExec;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageRemoveControl;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageResponse;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageSaveDictionary;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageSetExecConfig;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageSetExecutorDefaults;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageSetUserAction;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageUpdateDictionary;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageViewNodeDepth;
import com.astra.ses.spell.gui.core.exceptions.CommException;
import com.astra.ses.spell.gui.core.exceptions.ContextError;
import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.IExecutorInfo;
import com.astra.ses.spell.gui.core.interfaces.IMessageField;
import com.astra.ses.spell.gui.core.interfaces.IMessageId;
import com.astra.ses.spell.gui.core.interfaces.IMessageType;
import com.astra.ses.spell.gui.core.interfaces.IServerProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.files.FileTransferData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.InputData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.NotificationData;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.server.ClientInfo;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ExecutorConfig;
import com.astra.ses.spell.gui.core.model.server.ExecutorDefaults;
import com.astra.ses.spell.gui.core.model.server.ExecutorInfo;
import com.astra.ses.spell.gui.core.model.server.ProcedureRecoveryInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.server.TransferData;
import com.astra.ses.spell.gui.core.model.types.BreakpointType;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ClientOperation;
import com.astra.ses.spell.gui.core.model.types.ContextStatus;
import com.astra.ses.spell.gui.core.model.types.DataContainer;
import com.astra.ses.spell.gui.core.model.types.DataVariable;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.ProcProperties;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.types.ExecutorCommand;
import com.astra.ses.spell.gui.types.ExecutorStatus;

/*******************************************************************************
 * @brief Provides access to the SPEL context services
 * @date 20/05/08
 ******************************************************************************/
public class ContextProxy extends BaseProxy implements IContextProxy
{
	/** Service identifier */
	public static final String	ID	         = "com.astra.ses.spell.gui.ContextProxy";

	/** Currently used context */
	private ContextInfo	       m_ctxInfo;
	/** Timeout for opening processes */
	private long	           m_openTimeout	= 0;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ContextProxy()
	{
		super(ID);
		Logger.debug("Created", Level.INIT, this);
		m_ctxInfo = null;
		m_openTimeout = 25000;
	}

	// ##########################################################################
	// SERVICE SETUP
	// ##########################################################################

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void cleanup()
	{
		super.cleanup();
		m_ctxInfo = null;
	}

	// ##########################################################################
	// CONTEXT SERVICES
	// ##########################################################################

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void attach(ContextInfo ctxInfo) throws Exception
	{
		// Disconnect if necessary
		disconnect();
		m_ctxInfo = ctxInfo;
		getIPC().configure(ctxInfo);
		connect();
		if (m_ctxInfo != null)
		{
			Logger.info("Context is " + ctxInfo.getHost() + ":" + ctxInfo.getPort(), Level.COMM, this);
			CoreNotifications.get().fireContextAttached(m_ctxInfo);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void close()
	{
		if (isConnected())
		{
			closeIt();
			disconnect();
			CoreNotifications.get().fireContextDetached();
			Logger.info("Context detached", Level.COMM, this);
		}
	}

	/***************************************************************************
	 * Set the timeout for opening executors in msecs
	 **************************************************************************/
	@Override
	public void setOpenTimeout(long timeout)
	{
		m_openTimeout = timeout;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getCurrentContext()
	{
		if (m_ctxInfo == null) return null;
		return m_ctxInfo.getName();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getClientKey()
	{
		return getIPC().getKey();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public ContextInfo getInfo()
	{
		return m_ctxInfo;
	}

	// ##########################################################################
	// CONTEXT OPERATIONS (EXTERNAL)
	// ##########################################################################

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public Date getCurrentTime()
	{
		Date date = null;
		try
		{
			SPELLmessageGetCurrentTime msg = new SPELLmessageGetCurrentTime();
			SPELLmessage response = performRequest(msg);
			if (response != null && response instanceof SPELLmessageResponse)
			{
				String time = response.get(IMessageField.FIELD_DRIVER_TIME);
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				date = fmt.parse(time); 
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new ContextError(ex.getLocalizedMessage());
		}
		return date;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void openExecutor(String procedureId, String condition, Map<String, String> arguments, boolean background) throws ContextError
	{
		Logger.debug("Opening executor " + procedureId, Level.COMM, this);
		try
		{
			// Build the request message
			SPELLmessageOpenExec msg = new SPELLmessageOpenExec(procedureId, background);
			if (condition != null)
			{
				msg.setCondition(condition);
			}
			if (arguments != null)
			{
				msg.setArguments(arguments);
			}
			// Send the request
			performRequest(msg, m_openTimeout);
		}
		catch (Exception ex)
		{
			throw new ContextError(ex.getLocalizedMessage());
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void recoverExecutor(ProcedureRecoveryInfo procedure) throws ContextError
	{
		Logger.debug("Recovering executor " + procedure.getName(), Level.COMM, this);
		try
		{
			// Build the request message
			SPELLmessageRecoverExec msg = new SPELLmessageRecoverExec(procedure);
			// Send the request
			performRequest(msg);
		}
		catch (Exception ex)
		{
			throw new ContextError(ex.getLocalizedMessage());
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getProcedureInstanceId(String procedureId)
	{
		String instanceId = null;
		try
		{
			// Build the request message
			SPELLmessageGetInstance msg = new SPELLmessageGetInstance(procedureId);
			// Send the request
			SPELLmessage response = performRequest(msg);
			// Response will be null if failed
			if (response != null && response instanceof SPELLmessageResponse)
			{
				instanceId = SPELLmessageGetInstance.getInstance(response);
			}
		}
		catch (Exception ex)
		{
			throw new ContextError(ex.getLocalizedMessage());
		}
		return instanceId;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean closeExecutor(String procedureId)
	{
		Logger.debug("Closing executor " + procedureId, Level.COMM, this);
		try
		{
			SPELLmessage msg = new SPELLmessageCloseExec(procedureId);
			SPELLmessage response = performRequest(msg);
			if (response != null) { return true; }
		}
		catch (Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
		return false;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean killExecutor(String procedureId)
	{
		Logger.debug("Killing executor " + procedureId, Level.COMM, this);
		try
		{
			SPELLmessage msg = new SPELLmessageKillExec(procedureId);
			SPELLmessage response = performRequest(msg);
			if (response != null) { return true; }
		}
		catch (Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
		return false;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IExecutorInfo attachToExecutor(String procId, ClientMode mode)
	{
		Logger.debug("Attaching to executor " + procId + " in mode " + ClientInfo.modeToString(mode), Level.COMM, this);
		IExecutorInfo info = null;
		try
		{
			SPELLmessage msg = new SPELLmessageAttachExec(procId, mode);
			SPELLmessage response = performRequest(msg);
			if (response != null)
			{
				info = new ExecutorInfo(procId);
				SPELLmessageExecInfo.fillExecInfo(info, response);
			}
		}
		catch (Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
		return info;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean detachFromExecutor(String procId, boolean background)
	{
		Logger.debug("Detaching from executor " + procId, Level.COMM, this);
		try
		{
			SPELLmessage msg = null;
			if (background)
			{
				msg = new SPELLmessageBackgroundExec(procId);
			}
			else
			{
				msg = new SPELLmessageDetachExec(procId);
			}
			SPELLmessage response = performRequest(msg);
			if (response != null) { return true; }
		}
		catch (Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
		return false;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean removeControl(String procId)
	{
		Logger.debug("Removing controller from executor " + procId, Level.COMM, this);
		try
		{
			SPELLmessage msg = new SPELLmessageRemoveControl(procId);
			SPELLmessage response = performRequest(msg);
			if (response != null) { return true; }
		}
		catch (Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
		return false;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public ArrayList<String> getAvailableExecutors()
	{
		ArrayList<String> executors = new ArrayList<String>();
		try
		{
			Logger.debug("Retrieving available executors", Level.COMM, this);
			SPELLmessage request = new SPELLmessageExecList();
			SPELLmessage response = performRequest(request);
			if (response != null)
			{
				executors = SPELLmessageExecList.getExecListFrom(response);
				Logger.debug("Active executors: " + executors.size(), Level.PROC, this);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
		return executors;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IExecutorInfo getExecutorInfo(String procId) throws Exception
	{
		IExecutorInfo info = null;
		try
		{
			Logger.debug("Retrieving executor information: " + procId, Level.COMM, this);
			SPELLmessage msg = new SPELLmessageExecInfo(procId);
			SPELLmessage response = performRequest(msg);
			if (response != null)
			{
				info = new ExecutorInfo(procId);
				SPELLmessageExecInfo.fillExecInfo(info, response);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
			info = null;
			throw ex;
		}
		return info;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public ExecutorStatus getExecutorStatus( String procId ) throws Exception
	{
		ExecutorStatus st = ExecutorStatus.UNKNOWN;
		try
		{
			Logger.debug("Retrieving executor status: " + procId, Level.COMM, this);
			SPELLmessage msg = new SPELLmessageExecStatus(procId);
			SPELLmessage response = performRequest(msg);
			if (response != null)
			{
				st = SPELLmessageExecStatus.getStatus(response);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
			throw ex;
		}
		return st;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public InputData getExecutorPromptData(String procId) throws Exception
	{
		InputData data = null;
		try
		{
			Logger.debug("Retrieving executor prompt information: " + procId, Level.COMM, this);
			SPELLmessage msg = new SPELLmessageExecStatus(procId);
			SPELLmessage response = performRequest(msg);
			if (response != null)
			{
				ExecutorStatus st = SPELLmessageExecStatus.getStatus(response);
				if (ExecutorStatus.PROMPT.equals(st))
				{
					data = SPELLmessagePromptHelper.createPromptData(response,false);
					Logger.debug("Executor prompt information: " + data, Level.COMM, this);
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
			throw ex;
		}
		return data;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getExecutorCallstack( String procId ) throws Exception
	{
		String csp = null;
		try
		{
			Logger.debug("Retrieving executor call stack position: " + procId, Level.COMM, this);
			SPELLmessage msg = new SPELLmessageExecStatus(procId);
			SPELLmessage response = performRequest(msg);
			if (response != null)
			{
				csp = SPELLmessageExecStatus.getCallstack(response);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
			throw ex;
		}
		return csp;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void updateDataContainer(String procId, DataContainer container, boolean mergeNew, IProgressMonitor monitor ) throws Exception
	{
		Logger.debug("Updating " + container.getName() + " data container in " + procId, Level.PROC, this);
		List<String> modifiedVars = new ArrayList<String>();
		for(DataVariable var : container.getVariables())
		{
			if (var.isModified()) modifiedVars.add(var.getName());
		}
		monitor.beginTask("Updating data container variables", modifiedVars.size());
		
		String errors = "";
		
		for(String varName : modifiedVars)
		{
			if (monitor.isCanceled()) break;
			try
			{
				monitor.subTask("Updating variable '" + varName + "'");
				SPELLmessageUpdateDictionary msg = new SPELLmessageUpdateDictionary(procId, container, varName, mergeNew);
				performRequest(msg);
				monitor.worked(1);
				container.getVariable(varName).save();
			}
			catch(Exception ex)
			{
				if (!errors.isEmpty()) errors += "\n";
				errors += ex.getLocalizedMessage();
				if (ex.getLocalizedMessage().contains("Cannot forward request")) break;
			}
		}
		
		if (!errors.isEmpty())
		{
			throw new RuntimeException(errors);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void saveDataContainer(String procId, String name, String path ) throws ContextError
	{
		Logger.debug("Requesting save " + name + " data container in " + procId, Level.PROC, this);
		SPELLmessage msg = new SPELLmessageSaveDictionary(procId, name, path);
		// Perform the request
		try
		{
			performRequest(msg);
		}
		catch(Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.PROC, this);
			throw new ContextError(ex.getLocalizedMessage());
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public DataContainer getDataContainer(String procId, String name, IProgressMonitor monitor ) throws ContextError
	{
		DataContainer container = new DataContainer(procId, name);
		Logger.debug("Requesting " + name + " data container in " + procId, Level.PROC, this);
		SPELLmessage msg = null;
		SPELLmessage response = null;

		boolean getMoreChunks = true;
		int chunkNo = 0;
		TransferData chunk = null;
		String valueList = "";

		while (getMoreChunks)
		{
			if (monitor.isCanceled())
			{
				return container;
			}
			
			// If chunkNo == 0, it is the initial request
			if (chunkNo == 0)
			{
				msg = new SPELLmessageGetDictionary(procId, name);
			}
			// Subsequent requests
			else
			{
				msg = new SPELLmessageGetDictionary(procId, name, chunkNo);
			}
			// Perform the request
			try
			{
				response = performRequest(msg);
	
				if (monitor.isCanceled())
				{
					return container;
				}
	
				// Process the response and obtain the transfer data
				if (response != null)
				{
					chunk = SPELLmessageGetDictionary.getDataChunk(response);
				}
				else
				{
					return container;
				}
				
				// Check if data container exists in the this python code frame
				if (chunk.getData().equalsIgnoreCase("None") )
				{
					throw new Exception(name + " data container in " + procId + " is not available");
				}
	
				// If data is not chunked
				if (chunk.getTotalChunks() == 0)
				{
					valueList = chunk.getData();
					getMoreChunks = false;
					monitor.subTask("Data container variables obtained.");
				}
				// Else if this is the last chunk to obtain
				else if (chunk.getChunkNo() == chunk.getTotalChunks()) // This
				                                                       // is the
				                                                       // last
				                                                       // chunk
				{
					valueList += chunk.getData();
					getMoreChunks = false;
					monitor.worked(1);
					monitor.subTask("Data container variables obtained.");
				}
				// Otherwise, get the next chunk
				else
				{
					if (chunkNo == 0)
					{
						monitor.beginTask("Obtaining data container variables", chunk.getTotalChunks());
					}
					monitor.worked(1);
					valueList += chunk.getData();
					monitor.subTask("Data container variables: chunk " + chunkNo + " of " + chunk.getTotalChunks());
					if (chunkNo < chunk.getTotalChunks() - 1)
					{
						getMoreChunks = true;
						chunkNo = chunk.getChunkNo() + 1;
					}
					else
					{
						getMoreChunks = false;
					}
				}
	
				if (monitor.isCanceled())
				{
					return container;
				}
			}
			catch(Exception ex)
			{
				Logger.error(ex.getLocalizedMessage(), Level.PROC, this);
				throw new ContextError(ex.getLocalizedMessage());
			}
		}
		if (!valueList.trim().isEmpty())
		{
			SPELLmessageGetDictionary.updateContainer( valueList, container, monitor );
		}
		return container;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public DataContainer getInputFile( String path, IProgressMonitor monitor ) 
	{
		DataContainer container = new DataContainer("<none>", path);
		Logger.debug("Requesting " + path + " input file", Level.PROC, this);
		SPELLmessage msg = null;
		SPELLmessage response = null;

		boolean getMoreChunks = true;
		int chunkNo = 0;
		TransferData chunk = null;
		String valueList = "";

		while (getMoreChunks)
		{
			if (monitor.isCanceled())
			{
				return container;
			}
			
			// If chunkNo == 0, it is the initial request
			if (chunkNo == 0)
			{
				Logger.debug("Requesting first chunk", Level.PROC, this);
				msg = new SPELLmessageGetInputFile(path);
			}
			// Subsequent requests
			else
			{
				Logger.debug("Requesting chunk " + chunkNo, Level.PROC, this);
				msg = new SPELLmessageGetInputFile(path, chunkNo);
			}
			// Perform the request
			try
			{
				response = performRequest(msg);
	
				if (monitor.isCanceled())
				{
					return container;
				}
	
				// Process the response and obtain the transfer data
				if (response != null)
				{
					chunk = SPELLmessageGetInputFile.getDataChunk(response);
				}
				else
				{
					return container;
				}
	
				Logger.debug("Number of chunks: " + chunk.getTotalChunks(), Level.PROC, this);

				// If data is not chunked
				if (chunk.getTotalChunks() == 0)
				{
					valueList = chunk.getData();
					getMoreChunks = false;
					monitor.subTask("InputData file variables obtained.");
				}
				// Else if this is the last chunk to obtain
				else if (chunk.getChunkNo() == chunk.getTotalChunks()) // This
				                                                       // is the
				                                                       // last
				                                                       // chunk
				{
					valueList += chunk.getData();
					getMoreChunks = false;
					monitor.worked(1);
					monitor.subTask("InputData file variables obtained.");
				}
				// Otherwise, get the next chunk
				else
				{
					if (chunkNo == 0)
					{
						monitor.beginTask("Obtaining input file variables", chunk.getTotalChunks());
					}
					Logger.debug("Got chunk: " + chunkNo, Level.PROC, this);
					monitor.worked(1);
					valueList += chunk.getData();
					monitor.subTask("InputData file variables: chunk " + chunkNo + " of " + chunk.getTotalChunks());
					if (chunkNo < chunk.getTotalChunks() - 1)
					{
						getMoreChunks = true;
						chunkNo = chunk.getChunkNo() + 1;
					}
					else
					{
						getMoreChunks = false;
					}
				}
	
				if (monitor.isCanceled())
				{
					return container;
				}
			}
			catch(Exception ex)
			{
				Logger.error(ex.getLocalizedMessage() , Level.PROC, this);
				return container;
			}
		}
		Logger.debug("Data obtained: " + valueList.length(), Level.PROC, this);

		if (!valueList.trim().isEmpty())
		{
			Logger.debug("Updating container with data", Level.PROC, this);
			SPELLmessageGetInputFile.updateContainer( valueList, container, monitor );
		}
		return container;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public ClientInfo getClientInfo(String clientKey)
	{
		ClientInfo info = null;
		try
		{
			Logger.debug("Retrieving client information: " + clientKey, Level.COMM, this);
			SPELLmessage msg = new SPELLmessageClientInfo(clientKey);
			SPELLmessage response = performRequest(msg);
			if (response != null)
			{
				info = new ClientInfo();
				SPELLmessageClientInfo.fillClientInfo(info, response);
				Logger.debug("Client information obtained: " + clientKey, Level.COMM, this);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
			info = null;
		}
		return info;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public Map<String, String> getAvailableProcedures( boolean refresh )
	{
		Map<String, String> procs = new HashMap<String, String>();
		if (isConnected())
		{
			try
			{
				SPELLmessage msg = new SPELLmessageProcList(refresh);
				SPELLmessage response = performRequest(msg);

				if (response != null)
				{
					// Parse the list of available procedures (comma separated)
					/*
					 * Returned list contains tuples of procIDs and Procedures
					 * Names this way procID1|procName1,procID2|procName2,
					 */
					procs = SPELLmessageProcList.getProcListFrom(response);
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
			}
		}
		return procs;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public ArrayList<String> getProcedureCode(String procedureId, IProgressMonitor monitor)
	{
		ArrayList<String> code = null;
		try
		{
			boolean getMoreChunks = true;
			SPELLmessage msg = null;
			SPELLmessage response = null;
			FileTransferData data = null;
			int chunkNo = 0;

			while (getMoreChunks)
			{
				if (monitor.isCanceled()) return null;

				if (chunkNo == 0)
				{
					msg = new SPELLmessageProcCode(procedureId);
				}
				else
				{
					msg = new SPELLmessageProcCode(procedureId, chunkNo);
				}
				response = performRequest(msg);
				if (response != null)
				{
					data = SPELLmessageProcCode.getCodeFrom(response);
				}

				if (monitor.isCanceled()) return null;
				;

				if (data.getTotalChunks() == 0) // Data is not chunked
				{
					code = data.getLines();
					getMoreChunks = false;
				}
				else if (data.getChunkNo() == data.getTotalChunks()) // This is
				                                                     // the last
				                                                     // chunk
				{
					if (code == null) code = new ArrayList<String>();
					code.addAll(data.getLines());
					getMoreChunks = false;
				}
				else
				// Get next chunk
				{
					// If this is the first chunk of series
					if (chunkNo == 0)
					{
						monitor.beginTask("Retrieving procedure file (" + data.getTotalChunks() + " parts)",
						        data.getTotalChunks());
					}
					else
					{
						monitor.setTaskName("Retrieving file...\nObtained part " + chunkNo + " of "
						        + data.getTotalChunks());
						monitor.worked(1);
					}
					if (code == null) code = new ArrayList<String>();
					code.addAll(data.getLines());
					if (chunkNo < data.getTotalChunks() - 1)
					{
						getMoreChunks = true;
						chunkNo = data.getChunkNo() + 1;
					}
					else
					{
						getMoreChunks = false;
					}
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
		if (code == null)
		{
			Logger.error("Unable to obtain procedure code: '" + procedureId + "'", Level.COMM, this);
		}
		return code;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public TreeMap<ProcProperties, String> getProcedureProperties(String procedureId)
	{
		TreeMap<ProcProperties, String> properties = null;
		try
		{
			SPELLmessageProcInfo msg = new SPELLmessageProcInfo(procedureId);
			SPELLmessage response = performRequest(msg);

			if (response != null)
			{
				properties = msg.getProcProperties(response);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
		return properties;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void command(ExecutorCommand cmd, String[] args)
	{
		try
		{
			Logger.debug("Executor command: " + cmd.toString(), Level.PROC, this);
			SPELLmessageOneway msg = new SPELLmessageOneway(cmd.getId());
			processCommandArguments(cmd, msg, args);
			getIPC().sendMessage(msg);
			Logger.debug("Executor command sent: " + cmd, Level.COMM, this);
		}
		catch (Exception e)
		{
			Logger.error(e.getLocalizedMessage(), Level.COMM, this);
		}
	}

	/***************************************************************************
	 * Process the given command arguments and update the SPELL message properly
	 * 
	 * @param cmd
	 *            ExecutorCommand identifier
	 * @param session
	 *            Procedure execution session information
	 * @param arguments
	 *            ExecutorCommand arguments
	 * @param msg
	 *            SPELL message to update
	 **************************************************************************/
	protected void processCommandArguments(ExecutorCommand cmd, SPELLmessageOneway msg, String[] args)
	{
		msg.fillCommandData(cmd, args);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setExecutorConfiguration(String procId, ExecutorConfig config)
	{
		try
		{
			SPELLmessageSetExecConfig req = new SPELLmessageSetExecConfig(procId);
			for (String key : config.getConfigMap().keySet())
			{
				req.set(key, config.getConfigMap().get(key));
			}
			SPELLmessage response = performRequest(req);
			
			Logger.debug("Executor " + procId + " SET configuration: ", Level.COMM, this);
			Logger.debug("Execution Delay   : " + config.getExecDelay(), Level.COMM, this);
			Logger.debug("Prompt Warn Delay : " + config.getPromptWarningDelay(), Level.COMM, this);
			Logger.debug("Browsable lib     : " + config.getBrowsableLib(), Level.COMM, this);
			Logger.debug("By Step           : " + config.getStepByStep(), Level.COMM, this);
			Logger.debug("Force Tc Confirm  : " + config.getTcConfirmation(), Level.COMM, this);
			Logger.debug("Run Into          : " + config.getRunInto(), Level.COMM, this);
			
			if (response == null || response instanceof SPELLmessageError)
			{
				Logger.error("Unable to set executor configuration", Level.PROC, this);
			}
		}
		catch (Exception e)
		{
			Logger.error(e.getLocalizedMessage(), Level.COMM, this);
		}

	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void updateExecutorConfig(String procId, ExecutorConfig config) throws Exception
	{
		try
		{
			Logger.debug("Retrieving executor configuration: " + procId, Level.COMM, this);
			SPELLmessage msg = new SPELLmessageGetExecConfig(procId);
			SPELLmessage response = performRequest(msg);
			if (response != null)
			{
				SPELLmessageGetExecConfig.fillExecConfig(config, response); // process received fields
				Logger.debug("Executor " + procId + " UPDATE configuration: ", Level.COMM, this);
				Logger.debug("Execution Delay   : " + config.getExecDelay(), Level.COMM, this);
				Logger.debug("Prompt Warn Delay : " + config.getPromptWarningDelay(), Level.COMM, this);
				Logger.debug("Browsable lib     : " + config.getBrowsableLib(), Level.COMM, this);
				Logger.debug("By Step           : " + config.getStepByStep(), Level.COMM, this);
				Logger.debug("Force Tc Confirm  : " + config.getTcConfirmation(), Level.COMM, this);
				Logger.debug("Run Into          : " + config.getRunInto(), Level.COMM, this);
				
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
			config = null;
			throw ex;
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void getExecutorDefaults(ExecutorDefaults defaults) throws Exception
	{
		SPELLmessage msg;
		SPELLmessage response;
		
		try
		{
			Logger.debug("Retrieving executor defaults for Context: " + getCurrentContext(), Level.COMM, this);
			msg = new SPELLmessageGetExecutorDefaults( getCurrentContext() ); //prepare message
			response = performRequest(msg); //send message
			
			if ( response != null 
				 && IMessageId.RSP_GET_CTX_EXEC_DFLT.equals(response.getId()) )
			{
				// Process received fields
				SPELLmessageGetExecutorDefaults.fillDefaults(defaults, response);
				Logger.debug( "Response message: " + response.getId() +"\nParams: " + defaults.getConfigMap().toString(), Level.COMM, this);
			} //if response !=null and RSP_GET_CTX_EXEC_DFLT
		} //try
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
			defaults = null;
			throw ex;
		}  //catch
	} // getExecutorDefaults	
	
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setExecutorDefaults(Map<String, String> defaults)
	{
		SPELLmessage msg;
		SPELLmessage response = null;
		
		try
		{
			Logger.debug("Setting executor defaults for Context: " + getCurrentContext(), Level.COMM, this);

			// When user has updated some values
			if( !defaults.isEmpty() ) {
				// Prepare message
				msg = new SPELLmessageSetExecutorDefaults( getCurrentContext() );
				//Add fields to message from executor defaults params
				for (String key : defaults.keySet())
				{
					msg.set(key, defaults.get(key));
				} //for
				Logger.debug( "Parameters to be sent to the server: " + defaults.toString() , Level.COMM, this);
				
				//Send message
				response = performRequest(msg);
				
				//Log response			
				if ( response != null 
					 && IMessageId.RSP_SET_CTX_EXEC_DFLT.equals(response.getId()) )
				{
					Logger.debug( "Values updated on server", Level.COMM, this);
				} //if response !=null and RSP_GET_CTX_EXEC_DFLT
				else
				{
					Logger.error( "Not possible to update defaults on server (" + response.getId() + ")", Level.COMM, this);
				}
			} //if defaults is not empty
			else
			{
				Logger.debug("No default fields to be updated", Level.COMM, this);
			} // else no fields
		} //try
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
			defaults = null;
		}  //catch
	} // setExecutorDefaults	
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void toggleBreakpoint(String procId, String codeId, int lineNo, BreakpointType type) throws Exception
	{
		Logger.debug("Toggling breakpoint at line " + lineNo + " in " + procId, Level.COMM, this);
		SPELLmessage msg = new SPELLmessageBreakpoint(procId, codeId, lineNo, type);
		performRequest(msg);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void clearBreakpoints(String procId) throws Exception
	{
		Logger.debug("Removing all breakpoints in " + procId, Level.COMM, this);
		SPELLmessage msg = new SPELLmessageClearBreakpoints(procId);
		performRequest(msg);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void viewNodeAtDepth(String procId, int depth)
	{
		Logger.debug("Switching view in " + procId + " to depth" + depth, Level.COMM, this);
		SPELLmessageOneway cmd = new SPELLmessageViewNodeDepth(procId, depth);
		getIPC().sendMessage(cmd);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void connect() throws Exception
	{
		try
		{
			Logger.debug("Connecting context proxy", Level.COMM, this);
			// Get password from listener proxy if any
			IServerProxy lproxy = (IServerProxy) ServiceManager.get(IServerProxy.class);
			ServerInfo sinfo = lproxy.getCurrentServer();
			m_ctxInfo.setAuthentication(sinfo.getAuthentication());
			performConnect(m_ctxInfo);
		}
		catch (Exception e)
		{
			Logger.error("Connection failed: " + e.getLocalizedMessage(), Level.COMM, this);
			getIPC().forceDisconnect();
			ErrorData data = new ErrorData(m_ctxInfo.getName(), "Cannot connect", e.getLocalizedMessage(), true);
			m_ctxInfo = null;
			connectionFailed(data);
			throw e;
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void disconnect()
	{
		if (!isConnected()) { return; }
		try
		{
			m_ctxInfo = null;
			performDisconnect();
		}
		catch (CommException e)
		{
			return;
		}
		// Create the local peer and obtain the SPELL peer
		Logger.info("Connection closed", Level.COMM, this);
	}

	/***************************************************************************
	 * Close context
	 **************************************************************************/
	private void closeIt()
	{
		try
		{
			SPELLmessage msg = new SPELLmessageClose();
			getIPC().sendMessage(msg);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void connectionFailed(ErrorData data)
	{
		Logger.error("Unable to connect to context: " + data.getMessage(), Level.PROC, this);
		data.setOrigin("CTX");
		m_ctxInfo = null;
		Logger.error("Fire context error", Level.PROC, this);
		forceDisconnect();
		CoreNotifications.get().fireContextError(data);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void connectionLost(ErrorData data)
	{
		if (m_ctxInfo != null)
		{
			Logger.error("Context proxy connection lost: " + data.getMessage(), Level.PROC, this);
			m_ctxInfo = null;
			forceDisconnect();
			CoreNotifications.get().fireContextError(data);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void connectionClosed()
	{
		Logger.info("Context proxy connection closed", Level.PROC, this);
		m_ctxInfo = null;
		CoreNotifications.get().fireContextDetached();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean listenerConnectionLost(SPELLlistenerLost error)
	{
		IServerProxy server = (IServerProxy) ServiceManager.get(IServerProxy.class);
		if (server.isConnected())
		{
			server.forceDisconnect();
			server.connectionLost(error.getData());
		}
		return false;
	}

	/***************************************************************************
	 * Process both synchronous and asynchronous notifications.
	 * 
	 * @param msg
	 **************************************************************************/
	protected synchronized void processNotificationMessage(SPELLmessage msg)
	{
		NotificationData data = null;
		
		if (msg instanceof SPELLmessageNotifyAsync)
		{
			data = ((SPELLmessageNotifyAsync) msg).getData();
		}
		else
		{
			data = ((SPELLmessageNotify) msg).getData();
		}
		
		if (data instanceof ItemNotification)
		{
			CoreNotifications.get().fireProcedureItem((ItemNotification) data);
		}
		else if (data instanceof StatusNotification)
		{
			CoreNotifications.get().fireProcedureStatus((StatusNotification) data);
		}
		else if (data instanceof StackNotification)
		{
			CoreNotifications.get().fireProcedureStack((StackNotification) data);
		}
		else
		{
			Logger.warning("Notification type not implemented: " + msg, Level.COMM, this);
		}
	}

	/***************************************************************************
	 * Process executor operation messages
	 * 
	 * @param msg
	 **************************************************************************/
	protected void processExecutorOperationMessage(SPELLmessage msg)
	{
		SPELLmessageExecOperation op = (SPELLmessageExecOperation) msg;
		
		switch(op.getOperation())
		{
		case ATTACH:
			ClientMode mode = op.getClientMode();
			if (mode == ClientMode.CONTROL)
			{
				CoreNotifications.get().fireProcedureControlled(op.getProcId(), op.getClientKey());
			}
			else if (mode == ClientMode.MONITOR)
			{
				CoreNotifications.get().fireProcedureMonitored(op.getProcId(), op.getClientKey());
			}
			break;
		case CLOSE:
			CoreNotifications.get().fireProcedureClosed(op.getProcId(), op.getClientKey());
			break;
		case CRASH:
			CoreNotifications.get().fireProcedureCrashed(op.getProcId(), op.getClientKey());
			break;
		case DETACH:
			CoreNotifications.get().fireProcedureReleased(op.getProcId(), op.getClientKey());
			break;
		case KILL:
			CoreNotifications.get().fireProcedureKilled(op.getProcId(), op.getClientKey());
			break;
		case OPEN:
			CoreNotifications.get().fireProcedureOpen(op.getProcId(), op.getClientKey());
			break;
		case SUMMARY:
			CoreNotifications.get().fireProcedureSummary(op.getProcId(), op.getSummary(), op.getClientKey());
			break;
		case STATUS:
			CoreNotifications.get().fireProcedureStatus(op.getProcId(), op.getStatus(), op.getClientKey());
			break;
		case UNKNOWN:
			Logger.error("Unknown executor operation '" + op.getOperation() + "' for " + op.getProcId(), Level.COMM,
			        this);
			break;
		}
	}

	/***************************************************************************
	 * Process context operation messages
	 * 
	 * @param msg
	 **************************************************************************/
	protected void processContextOperationMessage(SPELLmessage msg)
	{
		SPELLmessageCtxOperation ctxOp = (SPELLmessageCtxOperation) msg;
		if (ctxOp.getStatus() == ContextStatus.RUNNING)
		{
			CoreNotifications.get().fireContextStarted(ctxOp.getContextInfo());
		}
		else if (ctxOp.getStatus() == ContextStatus.AVAILABLE)
		{
			CoreNotifications.get().fireContextStopped(ctxOp.getContextInfo());
		}
		else if (ctxOp.getStatus() == ContextStatus.ERROR)
		{
			CoreNotifications.get().fireContextError(ctxOp.getErrorData());
		}
		else
		{
			Logger.error("Unknown context operation: " + ctxOp.getContextInfo().getName(), Level.COMM, this);
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void answerPrompt( InputData promptData )
	{
		SPELLmessage response = new SPELLmessageOneway( IMessageId.MSG_PROMPT_ANSWER );
		response.setType(IMessageType.MSG_TYPE_ONEWAY);
		response.set( IMessageField.FIELD_PROC_ID, promptData.getProcId() );
		
		if (promptData.isCancelled())
		{
			response.setId(IMessageId.MSG_CANCEL);
			
		}
		else if (promptData.isError())
		{
			response.setId(IMessageId.MSG_CANCEL);
			response.setType( IMessageType.MSG_TYPE_ERROR );
		}
		else 
		{
			response.set(IMessageField.FIELD_RVALUE, promptData.getReturnValue());
		}

		sendMessage(response);
		
		if (promptData.isCancelled())
		{
			Logger.debug("Procedure " + promptData.getProcId() + " cancelled prompt", Level.COMM, this);
			CoreNotifications.get().fireCancelPrompt(promptData);
		}
		else
		{
			Logger.debug("Obtained value: " + promptData.getReturnValue(), Level.COMM, this);
			CoreNotifications.get().fireFinishPrompt(promptData);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	protected void sendAcknowledge( SPELLmessage msg )
	{
		//System.err.println("ACK " + msg.dataStr());
		SPELLmessage ack = new SPELLmessage();
		ack.setId("ACKNOWLEDGE");
		ack.setType(IMessageType.MSG_TYPE_ONEWAY);
		try
		{
			ack.set(IMessageField.FIELD_MSG_SEQUENCE,msg.get(IMessageField.FIELD_MSG_SEQUENCE));
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		ack.setSender(msg.getReceiver());
		ack.setReceiver(msg.getSender());
		try
        {
            ack.set(IMessageField.FIELD_PROC_ID, msg.get(IMessageField.FIELD_PROC_ID));
        }
        catch (MessageException e)
        {
            e.printStackTrace();
        }
		sendMessage(ack);
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
    @Override
    public boolean processIncomingMessage(SPELLmessage msg)
    {
		if (msg instanceof SPELLmessageNotifyAsync)
		{
			processNotificationMessage(msg);
		}
		else if (msg instanceof SPELLmessageDisplay)
		{
			SPELLmessageDisplay display = (SPELLmessageDisplay) msg;
			CoreNotifications.get().fireProcedureDisplay(display.getData());
		}
		else if (msg instanceof SPELLmessagePrompt)
		{
			SPELLmessagePrompt prompt = (SPELLmessagePrompt) msg;
			InputData promptData = prompt.getData();
			CoreNotifications.get().firePrompt(promptData);
		}
		else if (msg instanceof SPELLmessageNotify)
		{
			sendAcknowledge(msg);
			processNotificationMessage((SPELLmessage) msg);
		}
		else if (msg instanceof SPELLlistenerLost)
		{
			boolean notify = listenerConnectionLost((SPELLlistenerLost) msg);
			if (notify)
			{
				CoreNotifications.get().fireListenerError(((SPELLlistenerLost) msg).getData());
			}
		}
		else if (msg instanceof SPELLmessageError)
		{
			SPELLmessageError error = (SPELLmessageError) msg;
			CoreNotifications.get().fireProcedureError(error.getData());
		}
		else if (msg instanceof SPELLmessagePromptStart)
		{
			SPELLmessagePromptStart prompt = (SPELLmessagePromptStart) msg;
			InputData promptData = prompt.getData();
			CoreNotifications.get().firePrompt(promptData);
		}
		else if (msg instanceof SPELLmessagePromptEnd)
		{
			SPELLmessagePromptEnd prompt = (SPELLmessagePromptEnd) msg;
			InputData promptData = prompt.getData();
			CoreNotifications.get().fireFinishPrompt(promptData);
		}
		else if (msg instanceof SPELLmessageExecOperation)
		{
			processExecutorOperationMessage(msg);
		}
		else if (msg instanceof SPELLmessageCtxOperation)
		{
			processContextOperationMessage(msg);
		}
		else if (msg instanceof SPELLmessageClientOperation)
		{
			SPELLmessageClientOperation op = (SPELLmessageClientOperation) msg;
			if (op.getOperation() == ClientOperation.LOGIN)
			{
				CoreNotifications.get().fireClientConnected(op.getHost(), op.getClientKey());
			}
			else if (op.getOperation() == ClientOperation.LOGOUT)
			{
				CoreNotifications.get().fireClientDisconnected(op.getHost(), op.getClientKey());
			}
			else
			{
				Logger.error("Unknown client operation: " + op.getOperation(), Level.COMM, this);
				return false;
			}
		}
		else if (msg instanceof SPELLmessageSetUserAction)
		{
			SPELLmessageSetUserAction dmsg = (SPELLmessageSetUserAction) msg;
			CoreNotifications.get().fireProcedureUserAction(dmsg.getData());
		}
		else if (msg instanceof SPELLmessageEnableUserAction)
		{
			SPELLmessageEnableUserAction dmsg = (SPELLmessageEnableUserAction) msg;
			CoreNotifications.get().fireProcedureUserAction(dmsg.getData());
		}
		else if (msg instanceof SPELLmessageDisableUserAction)
		{
			SPELLmessageDisableUserAction dmsg = (SPELLmessageDisableUserAction) msg;
			CoreNotifications.get().fireProcedureUserAction(dmsg.getData());
		}
		else if (msg instanceof SPELLmessageDismissUserAction)
		{
			SPELLmessageDismissUserAction dmsg = (SPELLmessageDismissUserAction) msg;
			CoreNotifications.get().fireProcedureUserAction(dmsg.getData());
		}
		else if (msg instanceof SPELLmessageExecConfigChanged)
		{
			SPELLmessageExecConfigChanged ecfg = (SPELLmessageExecConfigChanged) msg;
			ExecutorConfig config = new ExecutorConfig(ecfg.getProcId());
			ecfg.fillExecConfig(config);
			CoreNotifications.get().fireProcedureConfigured(config);
		}
		else if (msg instanceof SPELLmessageOneway)
		{
			return false;
		}
		else
		{
			Logger.error("Unexpected message: " + msg.dataStr(), Level.COMM, this);
			return false;
		}
		return true;
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
    @Override
    public SPELLmessageResponse processIncomingRequest(SPELLmessage msg)
    {
		SPELLmessageResponse response = new SPELLmessageResponse(msg);
		if (msg instanceof SPELLmessageNotify)
		{
			processNotificationMessage((SPELLmessage) msg);
		}
		else
		{
			Logger.error("Unexpected request: " + msg.dataStr(), Level.COMM, this);
		}
		return response;
    }
}
