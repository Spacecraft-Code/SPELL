///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.services
// 
// FILE      : ProcedureModelManager.java
//
// DATE      : 2008-11-24 08:34
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
package com.astra.ses.spell.gui.procs.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.astra.ses.spell.gui.core.exceptions.ContextError;
import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.IExecutorInfo;
import com.astra.ses.spell.gui.core.interfaces.IFileManager;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.ProcProperties;
import com.astra.ses.spell.gui.core.model.types.ServerFileType;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.ProcedureNotifications;
import com.astra.ses.spell.gui.procs.exceptions.LoadFailed;
import com.astra.ses.spell.gui.procs.exceptions.NoSuchProcedure;
import com.astra.ses.spell.gui.procs.exceptions.NotConnected;
import com.astra.ses.spell.gui.procs.interfaces.model.AsRunProcessing;
import com.astra.ses.spell.gui.procs.interfaces.model.AsRunReplayResult;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformationHandler;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.procs.model.Procedure;
import com.astra.ses.spell.gui.procs.model.RemoteProcedure;

/**
 * @author Rafael Chinchilla
 * 
 */
class ProcedureModelManager
{
	/** Holds the reference to the context proxy */
	private IContextProxy m_proxy;
	/** Holds the reference to the context proxy */
	private IFileManager m_fileMgr;
	/** Holds the list of local procedures */
	private Map<String, IProcedure> m_localModels;
	/** Holds the list of remote procedures */
	private Map<String, IProcedure> m_remoteModels;
	/** Holds the list of valid procedure identifiers */
	private Map<String, String> m_availableProcedures;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ProcedureModelManager(IContextProxy proxy, IFileManager fileMgr)
	{
		m_localModels = new TreeMap<String, IProcedure>();
		m_remoteModels = new TreeMap<String, IProcedure>();
		m_availableProcedures = new HashMap<String, String>();
		m_proxy = proxy;
		m_fileMgr = fileMgr;
	}

	/***************************************************************************
	 * Request the list of available procedures to the context. Called as soon
	 * as the context proxy Connected Event is received.
	 **************************************************************************/
	Map<String, String> obtainAvailableProcedures( boolean refresh )
	{
		try
		{
			// Ensure we are connected
			checkConnectivity();
			Logger.debug("Loading available procedures", Level.PROC, this);
			m_availableProcedures = m_proxy.getAvailableProcedures(refresh);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			m_availableProcedures.clear();
		}
		return m_availableProcedures;
	}

	/***************************************************************************
	 * Request the list of available procedures to the context. Called as soon
	 * as the context proxy Connected Event is received.
	 **************************************************************************/
	void obtainRemoteProcedures()
	{
		try
		{
			m_remoteModels.clear();
			Logger.debug("Loading active executor models", Level.PROC, this);
			List<String> executorIds = m_proxy.getAvailableExecutors();
			for (String instanceId : executorIds)
			{
				// Do not load those which are local
				if (m_localModels.containsKey(instanceId))
					continue;
				createRemoteProcedureModel(instanceId);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			clearRemoteProcedures();
		}
	}

	/***************************************************************************
	 * Create a new procedure model for the given procedure identifier. The
	 * sequence of operations is:
	 * 
	 * 1) Obtain a proper procedure instance id 2) Create a model for the
	 * procedure and store it
	 * 
	 * @param procedureId
	 *            The procedure identifier, no instance number info
	 * @returns The procedure model
	 * @throws LoadFailed
	 *             if the procedure could not be loaded
	 **************************************************************************/
	IProcedure createLocalProcedureModel(String procedureId, IProgressMonitor monitor) throws LoadFailed
	{
		Logger.debug("Creating local model " + procedureId, Level.PROC, this);
		// Check proxy connection
		checkConnectivity();

		// Procedure ID shall exist
		if (!m_availableProcedures.containsKey(procedureId))
		{
			throw new LoadFailed("No such procedure: '" + procedureId + "'");
		}

		// Obtain a valid instance identifier from the context
		String instanceId = getAvailableId(procedureId);
		Logger.debug("Getting procedure properties for " + procedureId, Level.PROC, this);
		TreeMap<ProcProperties, String> props = m_proxy.getProcedureProperties(procedureId);

		Logger.debug("Instantiate model " + instanceId, Level.PROC, this);
		IProcedure proc = new Procedure(instanceId, props, ClientMode.CONTROL);
		
		// Provoke the first source code acquisition
		Logger.debug("Acquire source code for the first time", Level.PROC, this);
		proc.getExecutionManager().initialize(monitor);

		Logger.debug("Store local model " + instanceId, Level.PROC, this);
		m_localModels.put(instanceId, proc);

		return proc;
	}

	/***************************************************************************
	 * Add a new procedure model created by an external party.
	 * 
	 **************************************************************************/
	void addLocalProcedureModel(String instanceId, IProcedure model)
	{
		Logger.debug("Store local model " + instanceId, Level.PROC, this);
		m_localModels.put(instanceId, model);
	}

	/***************************************************************************
	 * Remove a procedure model created by an external party.
	 * 
	 **************************************************************************/
	void removeLocalProcedureModel(String instanceId)
	{
		Logger.debug("Remove local model manually " + instanceId, Level.PROC, this);
		m_localModels.remove(instanceId);
	}

	/***************************************************************************
	 * Add a background procedure model.
	 **************************************************************************/
	String addBackgroundProcedureModel( String procId )
	{
		try
		{
			String instanceId = getAvailableId(procId);
			Logger.debug("Registered remote background model: " + instanceId, Level.PROC, this);
			RemoteProcedure proc = new RemoteProcedure(instanceId);
			IExecutionInformationHandler runtime = (IExecutionInformationHandler) proc.getRuntimeInformation();
			runtime.setBackground(true);
			Logger.debug("Store remote model " + instanceId, Level.PROC, this);
			m_remoteModels.put(instanceId, proc);
			return instanceId;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	
	/***************************************************************************
	 * Create a remote procedure model and add it to the list
	 * 
	 * @param instanceId
	 *            Procedure instance identifier
	 **************************************************************************/
	synchronized void createRemoteProcedureModel(String instanceId)
	{
		try
		{
			if (!m_remoteModels.containsKey(instanceId))
			{
				Logger.debug("Registered remote model: " + instanceId, Level.PROC, this);
				IProcedure proc = new RemoteProcedure(instanceId);
				Logger.debug("Updating remote model for " + instanceId, Level.PROC, this);
				proc.getController().refresh();
				Logger.debug("Store remote model " + instanceId, Level.PROC, this);
				m_remoteModels.put(instanceId, proc);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/***************************************************************************
	 * Remove a remote procedure model
	 * 
	 * @param instanceId
	 *            Procedure instance identifier
	 **************************************************************************/
	synchronized void deleteRemoteProcedureModel(String instanceId)
	{
		if (m_remoteModels.containsKey(instanceId))
		{
			Logger.debug("Deleting remote model: " + instanceId, Level.PROC, this);
			m_remoteModels.remove(instanceId);
		}
	}

	/***************************************************************************
	 * Remove a local procedure model
	 * 
	 * @param instanceId
	 *            Procedure instance identifier
	 **************************************************************************/
	synchronized void deleteLocalProcedureModel(String instanceId)
	{
		if (m_localModels.containsKey(instanceId))
		{
			Logger.debug("Deleting local model: " + instanceId, Level.PROC, this);
			IProcedure proc = m_localModels.remove(instanceId);
			proc.onClose();
		}
	}

	/***************************************************************************
	 * Convert a local model to remote model
	 * 
	 * @param instanceId
	 *            Procedure model to convert
	 **************************************************************************/
	synchronized void convertToRemote(String instanceId, IProgressMonitor monitor)
	{
		Logger.debug("Converting local model to remote" + instanceId, Level.PROC, this);
		IProcedure proc = getProcedure(instanceId);
		// The procedure becomes remote
		IProcedure remote = new RemoteProcedure(proc);
		Logger.debug("Store remote model" + instanceId, Level.PROC, this);
		m_remoteModels.put(instanceId, remote);
		// IMPORTANT do not delete the local model yet. It will be done by the
		// procedure manager. In case of load failure, we will not want to
		// remove the local, but the remote (rollback)
	}

	/***************************************************************************
	 * Convert a remote model to local
	 * 
	 * @param procedureId
	 *            The procedure identifier, no instance number info
	 **************************************************************************/
	IProcedure convertToLocal(String instanceId, ClientMode mode) throws LoadFailed
	{
		// Check proxy connection
		checkConnectivity();

		Logger.debug("Converting remote procedure model to local: " + instanceId, Level.PROC, this);

		if (!m_remoteModels.containsKey(instanceId))
		{
			throw new LoadFailed("Could not find remote procedure: '" + instanceId + "'");
		}

		String[] elements = instanceId.split("#");

		Logger.debug("Retrieving procedure properties", Level.PROC, this);
		TreeMap<ProcProperties, String> props = m_proxy.getProcedureProperties(elements[0]);

		Logger.debug("Instantiate model", Level.PROC, this);
		IProcedure proc = new Procedure(instanceId, props, mode);

		// Provoke the first source code acquisition
		Logger.debug("Acquire source code for the first time", Level.PROC, this);
		proc.getExecutionManager().initialize(new NullProgressMonitor());

		Logger.debug("Store local model: " + instanceId, Level.PROC, this);
		m_localModels.put(instanceId, proc);

		return proc;
	}

	/***************************************************************************
	 * Update a remote procedure model
	 * 
	 * @param instanceId
	 *            Procedure instance identifier
	 **************************************************************************/
	void updateRemoteProcedureModel(String instanceId) throws Exception
	{
		if (m_remoteModels.containsKey(instanceId))
		{
			Logger.debug("Updating remote procedure model: " + instanceId, Level.PROC, this);
			IProcedure proc = m_remoteModels.get(instanceId);
			IExecutorInfo info = m_proxy.getExecutorInfo(instanceId);
			((IExecutionInformationHandler) proc.getRuntimeInformation()).copyFrom(info);
		}
	}

	/***************************************************************************
	 * Obtain a local procedure model.
	 * 
	 * @param instanceId
	 *            The instance identifier
	 * @return The procedure model
	 **************************************************************************/
	IProcedure getProcedure(String instanceId) throws NoSuchProcedure
	{
		if (m_localModels.containsKey(instanceId))
		{
			return m_localModels.get(instanceId);
		}
		else
		{
			throw new NoSuchProcedure(instanceId);
		}
	}

	/***************************************************************************
	 * Obtain a remote procedure model.
	 * 
	 * @param instanceId
	 *            The instance identifier
	 * @return The procedure reduced model
	 **************************************************************************/
	synchronized IProcedure getRemoteProcedure(String instanceId) throws NoSuchProcedure
	{
		if (m_remoteModels.containsKey(instanceId))
		{
			return m_remoteModels.get(instanceId);
		}
		else
		{
			throw new NoSuchProcedure(instanceId);
		}
	}

	/**************************************************************************
	 * Get procedure name given its Id
	 * 
	 * @param procId
	 * @return
	 *************************************************************************/
	String getProcedureName(String procId)
	{
		if (!isProcedureIdAvailable(procId))
		{
			return null;
		}
		return m_availableProcedures.get(procId);
	}

	/**************************************************************************
	 * Complete a procedure identifier with leading folders if any. The
	 * StartProc function in procedures can be used without the leading
	 * folders, and GUI mechanisms like the dependencies manager need to
	 * translate those dependencies to the complete ones.
	 *************************************************************************/
	String getCompleteProcedureId( String procId )
	{
		// Try to find the id without the leading folders (SPELL StartProc allows to
		// start procedures without these folders, thus the GUI shall be compliant)
		for(String id : m_availableProcedures.keySet())
		{
			int idx = id.lastIndexOf("/");
			if (idx != -1)
			{
				String pid = id.substring(idx+1);
				if (pid.equals(procId)) return id;
			}
		}
		return null;
	}
	
	/***************************************************************************
	 * Check if the given procedure identifier is available
	 **************************************************************************/
	boolean isProcedureIdAvailable( String procId )
	{
		return m_availableProcedures.containsKey(procId);
	}
	
	/***************************************************************************
	 * Request the procedure properties
	 **************************************************************************/
	public TreeMap<ProcProperties, String> getProcedureProperties(String procedureId)
	{
		TreeMap<ProcProperties, String> map = null;
		try
		{
			// Ensure we are connected
			checkConnectivity();
			map = m_proxy.getProcedureProperties(procedureId);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return map;
	}

	/***************************************************************************
	 * Check if a model is local
	 * 
	 * @param instanceId
	 *            The instance identifier
	 * @return True if locally loaded
	 **************************************************************************/
	boolean isLocal(String instanceId)
	{
		return (m_localModels.containsKey(instanceId));
	}

	/***************************************************************************
	 * Check if a model is remote
	 * 
	 * @param instanceId
	 *            The instance identifier
	 * @return True if remotelly loaded
	 **************************************************************************/
	boolean isRemote(String instanceId)
	{
		return (m_remoteModels.containsKey(instanceId));
	}

	/***************************************************************************
	 * Update a remote procedure model
	 * 
	 * @param instanceId
	 *            Procedure instance identifier
	 **************************************************************************/
	void updateLocalProcedureModel(String instanceId, String asRunFilePath, ClientMode mode, AsRunReplayResult result, IProgressMonitor monitor)
	        throws Exception
	{
		if (m_localModels.containsKey(instanceId))
		{
			Logger.debug("Updating local procedure model: " + instanceId, Level.PROC, this);
			IProcedure proc = m_localModels.get(instanceId);
			
			// Limits for file retrieval and processing
			IFileManager.ProcessingLimits limits = new IFileManager.ProcessingLimits();
			limits.fileLineCount    = 100000;
			limits.processLineCount = 10000;
			
			// Reset the executor info for replaying execution
			String localAsRunFile = null;
			boolean replaySuccess = true;

			if (result != null)
			{
				monitor.subTask("Retrieving AS-RUN file");
				if (asRunFilePath != null)
				{
					// Obtain the base path for ASRUN files
					String basePath = m_fileMgr.getServerFilePath(ServerFileType.ASRUN, monitor);
					// Compose the absolute file path
					String toDownload = basePath + "/" + asRunFilePath;
					
					Logger.debug("Retrieving As-Run file: " + toDownload, Level.PROC, this);
					localAsRunFile = m_fileMgr.downloadServerFile(toDownload, monitor);
				}
				else
				{
					Logger.debug("Retrieving As-Run file for " + instanceId, Level.PROC, this);
					String path = m_fileMgr.getServerFilePath(instanceId, ServerFileType.ASRUN, monitor);
					localAsRunFile = m_fileMgr.downloadServerFile(path, monitor);
				}
				monitor.worked(1);

				// Check cancellation
				if (monitor.isCanceled() || localAsRunFile == null)
					return;

				replayExecution(proc, localAsRunFile, result, monitor);
				replaySuccess = !result.status.equals(AsRunProcessing.FAILED);
			}

			if (!replaySuccess)
			{
				monitor.subTask("Failed to replay AsRun data");
				monitor.setCanceled(true);
				return;
			}

			// Check cancellation
			if (monitor.isCanceled())
				return;

			monitor.subTask("Retrieving executor information");
			monitor.worked(1);

			// Check cancellation
			if (monitor.isCanceled())
				return;

			// Refresh the proc status
			Logger.debug("Refresh controller status (update local model)", Level.PROC, this);
			proc.getController().refresh();
			
			Logger.debug("Set client mode " + mode, Level.PROC, this);
			((IExecutionInformationHandler) proc.getRuntimeInformation()).setClientMode(mode);

			monitor.worked(1);
		}
	}

	/***************************************************************************
	 * Obtain the list of available procedures in context
	 * 
	 * @return The procedure list
	 **************************************************************************/
	Map<String, String> getAvailableProcedures(boolean refresh)
	{
		if (refresh)
		{
			m_availableProcedures = m_proxy.getAvailableProcedures(refresh);
		}
		return m_availableProcedures;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	Set<String> getOpenLocalProcedures()
	{
		Set<String> list = new HashSet<String>();
		for(String instanceId : m_localModels.keySet())
		{
			IProcedure model = m_localModels.get(instanceId);
			if (model.getRuntimeInformation().getClientMode().equals(ClientMode.CONTROL) ||
				model.getRuntimeInformation().getClientMode().equals(ClientMode.MONITOR) )
			{
				list.add(instanceId);
			}
		}
		return list;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	Set<String> getOpenRemoteProcedures()
	{
		return m_remoteModels.keySet();
	}

	/***************************************************************************
	 * Clear the list of available procedures. Called as soon as the context
	 * proxy Disconnected Event is received.
	 **************************************************************************/
	void clearAvailableProcedures()
	{
		m_availableProcedures.clear();
	}

	/***************************************************************************
	 * Clear the list of available procedures. Called as soon as the context
	 * proxy Disconnected Event is received.
	 **************************************************************************/
	void clearRemoteProcedures()
	{
		m_remoteModels.clear();
	}

	/***************************************************************************
	 * When the context connection is lost, all models shall be disabled. This
	 * should result on disabling procedure model views as well, but this is up
	 * to the view provider plugin.
	 **************************************************************************/
	void disableProcedures(String reason)
	{
		// Notify the consumers of the local procedures that they cannot be
		// used any more
		for (IProcedure proc : m_localModels.values())
		{
			disableProcedure(reason, proc);
		}
	}

	/***************************************************************************
	 * When the context connection is lost, all models shall be disabled. This
	 * should result on disabling procedure model views as well, but this is up
	 * to the view provider plugin.
	 **************************************************************************/
	void disableProcedure(String reason, IProcedure procedure)
	{
		String procId = procedure.getProcId();
		Logger.warning("Disable procedure: " + procId + " (" + reason + ")", Level.PROC, this);
		
		ErrorData data = new ErrorData(procId, reason, "", true);
		IExecutionInformationHandler handler = (IExecutionInformationHandler) procedure.getRuntimeInformation();
		handler.setExecutorLost();
		procedure.getRuntimeProcessor().notifyProcedureError(data);
		ProcedureNotifications.get().fireProcedureError(procedure, data);
		ProcedureNotifications.get().fireModelDisabled(procedure);
	}

	/***************************************************************************
	 * When the context connection is recovered, all models can be reenabled.
	 * This should result on enabling procedure model views as well, but this is
	 * up to the view provider plugin.
	 **************************************************************************/
	void enableProcedures()
	{
		// Notify the consumers of the local procedures that they cannot be
		// used any more
		for (IProcedure proc : m_localModels.values())
		{
			// We shall refresh the model first
			try
			{
				Logger.warning("Enable procedure: " + proc.getProcId(), Level.PROC, this);
				proc.getController().refresh();
				// Then notify consumers
				ProcedureNotifications.get().fireModelEnabled(proc);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	/***************************************************************************
	 * Replay the AsRun data on a procedure model
	 * 
	 * @param instanceId
	 *            Procedure instance identifier
	 **************************************************************************/
	private void replayExecution(IProcedure model, String asrunPath, AsRunReplayResult result, IProgressMonitor monitor)
	{
		if (!model.isInReplayMode())
		{
			Logger.error("Cannot replay, model is not in the correct mode", Level.PROC, this);
			result.message = "procedure model is not in the correct mode";
			result.status = AsRunProcessing.FAILED;
		}
		else
		{
			monitor.setTaskName("Restoring AsRun informatio...");
			Logger.info("Start execution replay on " + model.getProcId(), Level.PROC, this);
			ExecutionPlayer player = new ExecutionPlayer(model,asrunPath,m_proxy);
			player.replay(monitor,0,result);
			monitor.worked(1);
			Logger.info("Finished execution replay on " + model.getProcId() + ", processed " + result.processedLines + " lines, result " + result.status, Level.PROC, this);
		}
	}

	/***************************************************************************
	 * Build a proper procedure identifier with free instance number
	 * 
	 * @param procId
	 * @return The identifier with instance number
	 **************************************************************************/
	private String getAvailableId(String procId)
	{
		String instanceId = null;
		try
		{
			Logger.debug("Obtaining available instance for " + procId, Level.PROC, this);
			instanceId = m_proxy.getProcedureInstanceId(procId);
			Logger.debug("Available instance is " + instanceId, Level.PROC, this);
		}
		catch (ContextError ex)
		{
			ex.printStackTrace();
		}
		return instanceId;
	}

	/***************************************************************************
	 * Check if we have connection to context
	 * 
	 * @throws NotConnected
	 *             if context proxy is not connected
	 **************************************************************************/
	private void checkConnectivity() throws NotConnected
	{
		if (!m_proxy.isConnected())
		{
			throw new NotConnected("Cannot operate: not conected to context");
		}
	}
}
