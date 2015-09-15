///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.services
// 
// FILE      : ProcedureManager.java
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

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.gui.core.CoreNotifications;
import com.astra.ses.spell.gui.core.exceptions.CommandFailed;
import com.astra.ses.spell.gui.core.exceptions.ContextError;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreProcedureRuntimeListener;
import com.astra.ses.spell.gui.core.interfaces.BaseService;
import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.IExecutorInfo;
import com.astra.ses.spell.gui.core.interfaces.IFileManager;
import com.astra.ses.spell.gui.core.interfaces.IProcedureClient;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreContextOperationListener;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreProcedureInputListener;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreProcedureOperationListener;
import com.astra.ses.spell.gui.core.model.notification.ControlNotification;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.InputData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification.StackType;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ExecutorConfig;
import com.astra.ses.spell.gui.core.model.server.ProcedureClient;
import com.astra.ses.spell.gui.core.model.server.ProcedureRecoveryInfo;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ExecutorOperationSummary;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.ProcProperties;
import com.astra.ses.spell.gui.core.model.types.UnloadType;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.ProcedureNotifications;
import com.astra.ses.spell.gui.procs.exceptions.LoadFailed;
import com.astra.ses.spell.gui.procs.exceptions.NoSuchProcedure;
import com.astra.ses.spell.gui.procs.exceptions.NotConnected;
import com.astra.ses.spell.gui.procs.exceptions.UnloadFailed;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.model.AsRunReplayResult;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformationHandler;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.procs.utils.GrabProcedureTask;
import com.astra.ses.spell.gui.types.ExecutorCommand;
import com.astra.ses.spell.gui.types.ExecutorStatus;

/*******************************************************************************
 * @brief Manages procedure models.
 * @date 09/10/07
 ******************************************************************************/
public class ProcedureManager extends BaseService implements IProcedureManager, 
															 ICoreContextOperationListener, 
															 ICoreProcedureInputListener,
															 ICoreProcedureRuntimeListener, 
															 ICoreProcedureOperationListener
{
	/** Holds the server proxy handle */
	private static IContextProxy s_ctx = null;
	/** Holds the file manager handle */
	private static IFileManager s_fileMgr = null;
	/** The service identifier */
	public static final String ID = "com.astra.ses.spell.gui.procs.ProcedureManager";

	/** Holds the model manager */
	private ProcedureModelManager m_models;
	/** Holds the procedure load monitor(s) */
	private Map<String, ProcedureLoadMonitor> m_loadMonitors;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ProcedureManager()
	{
		super(ID);
		Logger.debug("Created", Level.INIT, this);
		m_models = null;
		m_loadMonitors = new TreeMap<String, ProcedureLoadMonitor>();
	}

	/*
	 * #########################################################################
	 * # BASIC SERVICE METHODS
	 * #######################################################################
	 */

	/***************************************************************************
	 * Setup the service
	 **************************************************************************/
	@Override
	public void setup()
	{
		CoreNotifications.get().addListener(this, ICoreContextOperationListener.class);
		CoreNotifications.get().addListener(this, ICoreProcedureInputListener.class);
		CoreNotifications.get().addListener(this, ICoreProcedureRuntimeListener.class);
		CoreNotifications.get().addListener(this, ICoreProcedureOperationListener.class);
	}

	/***************************************************************************
	 * Cleanup the service
	 **************************************************************************/
	@Override
	public void cleanup()
	{
		CoreNotifications.get().removeListener(this);
	}

	/***************************************************************************
	 * Subscribe to required resources
	 **************************************************************************/
	@Override
	public void subscribe()
	{
		s_ctx = (IContextProxy) ServiceManager.get(IContextProxy.class);
		s_fileMgr = (IFileManager) ServiceManager.get(IFileManager.class);
		m_models = new ProcedureModelManager(s_ctx, s_fileMgr);
	}

	/***************************************************************************
	 * Listener identifier (IBaseListener)
	 **************************************************************************/
	@Override
	public String getListenerId()
	{
		return ID;
	}

	/***************************************************************************
	 * Check if we can do procedure operations
	 **************************************************************************/
	@Override
	public boolean canOperate()
	{
		return s_ctx.isConnected();
	}

	/*
	 * #########################################################################
	 * # PROCEDURE MANAGEMENT METHODS
	 * #######################################################################
	 */

	/***************************************************************************
	 * Open a new procedure instance for the given procedure.
	 * 
	 * @param procedureId
	 *            The procedure identifier, no instance number info
	 * @throws LoadFailed
	 *             if the procedure could not be loaded
	 **************************************************************************/
	@Override
	public void openProcedure(String procedureId, Map<String, String> arguments, IProgressMonitor monitor) throws LoadFailed
	{
		Logger.info("Opening procedure " + procedureId, Level.PROC, this);
		// Will hold the model
		IProcedure model = null;
		// Will hold the instance identifier
		String instanceId = null;

		// Start the task in the monitor
		monitor.beginTask("Opening procedure", 6);

		// Check cancellation
		if (monitor.isCanceled())
			return;

		try
		{
			// Create the model, Notify about the progress
			monitor.subTask("Creating model");
			model = m_models.createLocalProcedureModel(procedureId, monitor);
			instanceId = model.getProcId();
			monitor.worked(1);
		}
		catch (LoadFailed ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.PROC, this);
			monitor.subTask("ERROR: cannot open the procedure: " + ex);
			throw ex;
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			m_models.deleteLocalProcedureModel(instanceId);
			return;
		}

		// Now ask the context to start the procedure process
		// Ask the context to launch or attach to the proc. It will return the
		// executor information,
		// which is the procedure data known at core level.
		try
		{
			// Report progress
			monitor.subTask("Launching process");

			// Create a load monitor
			addLoadMonitor(instanceId);

			// Request context to load the procedure
			Logger.debug("Requesting context to open procedure " + instanceId, Level.PROC, this);
			s_ctx.openExecutor(instanceId, null, arguments, false);

			// Report progress
			monitor.worked(1);

			// Report progress
			monitor.subTask("Waiting for procedure to be ready");

			// Wait until the procedure is actually loaded on server side and
			// ready
			waitLoaded(instanceId);

			// Report progress
			monitor.worked(1);
		}
		catch (LoadFailed ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.PROC, this);
			// Remove the model
			m_models.deleteLocalProcedureModel(instanceId);
			// Kill the process
			Logger.debug("Requesting context to kill procedure " + instanceId, Level.PROC, this);
			s_ctx.killExecutor(instanceId);
			// Rethrow
			throw ex;
		}
		catch (ContextError ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.PROC, this);
			// Remove the model
			m_models.deleteLocalProcedureModel(instanceId);
			// The procedure could not be loaded due to an error in the context
			// processing
			throw new LoadFailed("Could not load the procedure '" + instanceId + "'.\n\n" + ex.getLocalizedMessage());
		}
		finally
		{
			removeLoadMonitor(instanceId);
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			Logger.debug("Requesting context to kill procedure " + instanceId, Level.PROC, this);
			s_ctx.killExecutor(instanceId);
			m_models.deleteLocalProcedureModel(instanceId);
			return;
		}

		try
		{
			// Once the model is loaded and the process is up and running,
			// update the model with the information.
			monitor.subTask("Updating procedure status");
			m_models.updateLocalProcedureModel(instanceId, null, ClientMode.CONTROL, null, monitor);
			// Report progress
			monitor.worked(1);
		}
		catch (Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.PROC, this);
			m_models.deleteLocalProcedureModel(instanceId);
			// The procedure could not be loaded due to an error in the context
			// processing
			throw new LoadFailed("Could not load the procedure '" + instanceId + "'.\n\n" + ex.getLocalizedMessage());
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			Logger.debug("Requesting context to kill procedure " + instanceId, Level.PROC, this);
			s_ctx.killExecutor(instanceId);
			m_models.deleteLocalProcedureModel(instanceId);
			return;
		}

		// Report progress
		monitor.subTask("Ending load process");
		notifyExtensionsProcedureReady(model);
		// Report progress
		monitor.done();
	}

	/***************************************************************************
	 * Open a new procedure instance for the given procedure in background.
	 * 
	 * @param procedureId
	 *            The procedure identifier, no instance number info
	 * @throws LoadFailed
	 *             if the procedure could not be loaded
	 **************************************************************************/
	@Override
	public void backgroundProcedure(String procedureId, Map<String, String> arguments, IProgressMonitor monitor) throws LoadFailed
	{
		Logger.info("Opening procedure " + procedureId + " in background", Level.PROC, this);

		String instanceId = null;
		
		// Start the task in the monitor
		monitor.beginTask("Opening procedure in background", 6);

		// Check cancellation
		if (monitor.isCanceled())
			return;

		try
		{
			// Create the model, Notify about the progress
			monitor.subTask("Creating model");
			instanceId = m_models.addBackgroundProcedureModel(procedureId);
			monitor.worked(1);
		}
		catch (LoadFailed ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.PROC, this);
			monitor.subTask("ERROR: cannot open the procedure: " + ex);
			throw ex;
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			m_models.deleteRemoteProcedureModel(instanceId);
			return;
		}

		// Now ask the context to start the procedure process
		// Ask the context to launch or attach to the proc. It will return the
		// executor information,
		// which is the procedure data known at core level.
		try
		{
			// Report progress
			monitor.subTask("Launching process");

			// Request context to load the procedure
			Logger.debug("Requesting context to open procedure " + instanceId, Level.PROC, this);
			s_ctx.openExecutor(instanceId, null, arguments, true);

			// Report progress
			monitor.worked(1);
			
			// Report progress
			monitor.subTask("Waiting for background procedure to be ready");

			// Report progress
			monitor.worked(1);
		}
		catch (LoadFailed ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.PROC, this);
			// Remove the model
			m_models.deleteRemoteProcedureModel(instanceId);
			// Kill the process
			Logger.debug("Requesting context to kill procedure " + instanceId, Level.PROC, this);
			s_ctx.killExecutor(instanceId);
			// Rethrow
			throw ex;
		}
		catch (ContextError ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.PROC, this);
			// Remove the model
			m_models.deleteRemoteProcedureModel(instanceId);
			// The procedure could not be loaded due to an error in the context
			// processing
			throw new LoadFailed("Could not load the procedure '" + instanceId + "'.\n\n" + ex.getLocalizedMessage());
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			Logger.debug("Requesting context to kill procedure " + instanceId, Level.PROC, this);
			s_ctx.killExecutor(instanceId);
			m_models.deleteRemoteProcedureModel(instanceId);
			return;
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			Logger.debug("Requesting context to kill procedure " + instanceId, Level.PROC, this);
			s_ctx.killExecutor(instanceId);
			m_models.deleteRemoteProcedureModel(instanceId);
			return;
		}

		// Report progress
		monitor.subTask("Ending load process");
		// Report progress
		monitor.done();
	}

	/***************************************************************************
	 * Add a new procedure model created by an external party
	 **************************************************************************/
	@Override
	public void addProcedure(String instanceId, IProcedure model)
	{
		m_models.addLocalProcedureModel(instanceId, model);
	}

	/***************************************************************************
	 * Remove a procedure model created by an external party
	 **************************************************************************/
	@Override
	public void removeProcedure(String instanceId)
	{
		m_models.removeLocalProcedureModel(instanceId);
	}

	/***************************************************************************
	 * Recover a procedure instance.
	 **************************************************************************/
	@Override
	public void recoverProcedure(ProcedureRecoveryInfo procedure, IProgressMonitor monitor) throws LoadFailed
	{
		Logger.info("Recovering procedure " + procedure.getOriginalInstanceId(), Level.PROC, this);
		// Will hold the model
		IProcedure model = null;
		// Will hold the instance identifier
		String instanceId = null;

		// Start the task in the monitor
		monitor.beginTask("Recovering procedure", 6);
		// Check cancellation
		if (monitor.isCanceled())
			return;

		// -----------------------------
		// PHASE 1, create a local model
		// -----------------------------
		try
		{
			Logger.debug("[1] Create local model", Level.PROC, this);
			// Create the model, Notify about the progress
			monitor.subTask("Creating model");
			// Will get an available id
			model = m_models.createLocalProcedureModel(procedure.getProcId(), monitor);
			model.setReplayMode(true);
			instanceId = model.getProcId();
			procedure.setNewInstanceId(instanceId);
			m_models.deleteLocalProcedureModel(instanceId);
			monitor.worked(1);
		}
		catch (LoadFailed ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.PROC, this);
			throw ex;
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			return;
		}

		// ----------------------------------------------------
		// PHASE 2, start the executor process in recovery mode
		// ----------------------------------------------------
		try
		{
			Logger.debug("[2] Start recovered executor", Level.PROC, this);
			// Report progress
			monitor.subTask("Starting to process");

			// Request context to load the procedure
			Logger.debug("Requesting context to recover executor " + procedure.getProcId(), Level.PROC, this);
			s_ctx.recoverExecutor(procedure);

			// Report progress
			monitor.worked(1);

		}
		catch (ContextError ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.PROC, this);
			throw new LoadFailed(ex.getLocalizedMessage());
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			return;
		}

		// Report progress
		monitor.subTask("Ending load process");
		// Report progress
		monitor.done();
		Logger.debug("Recovery process done", Level.PROC, this);
	}

	/***************************************************************************
	 * Attach an existing procedure instance in control mode.
	 * 
	 * @param instanceId
	 *            The procedure identifier, no instance number info
	 * @throws LoadFailed
	 *             if the procedure could not be attached
	 **************************************************************************/
	@Override
	public void controlProcedure(String instanceId, AsRunReplayResult result, IProgressMonitor monitor) throws LoadFailed
	{
		attachToRemoteProcedure(instanceId, ClientMode.CONTROL, result, monitor);
	}

	/***************************************************************************
	 * Attach an existing procedure instance in monitoring mode.
	 * 
	 * @param instanceId
	 *            The procedure identifier, no instance number info
	 * @throws LoadFailed
	 *             if the procedure could not be attached
	 **************************************************************************/
	@Override
	public void monitorProcedure(String instanceId, AsRunReplayResult result, IProgressMonitor monitor) throws LoadFailed
	{
		attachToRemoteProcedure(instanceId, ClientMode.MONITOR, result, monitor);
	}

	/***************************************************************************
	 * Schedule a new procedure instance for the given procedure.
	 * 
	 * @param procedureId
	 *            The procedure identifier, no instance number info
	 * @throws LoadFailed
	 *             if the procedure could not be loaded
	 **************************************************************************/
	@Override
	public void scheduleProcedure(String procedureId, String condition, IProgressMonitor monitor) throws LoadFailed
	{
		Logger.info("Scheduling procedure " + procedureId, Level.PROC, this);
		// Will hold the model
		IProcedure model = null;
		// Will hold the instance identifier
		String instanceId = null;

		// Start the task in the monitor
		monitor.beginTask("Opening procedure", 6);

		// Check cancellation
		if (monitor.isCanceled())
			return;

		try
		{
			// Create the model, Notify about the progress
			monitor.subTask("Creating model");
			model = m_models.createLocalProcedureModel(procedureId, monitor);
			instanceId = model.getProcId();
			monitor.worked(1);
		}
		catch (LoadFailed ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.PROC, this);
			monitor.subTask("ERROR: cannot open the procedure: " + ex);
			throw ex;
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			m_models.deleteLocalProcedureModel(instanceId);
			return;
		}

		// Now ask the context to start the procedure process
		// Ask the context to launch or attach to the proc. It will return the
		// executor information,
		// which is the procedure data known at core level.
		try
		{
			// Report progress
			monitor.subTask("Launching process");

			// Create a load monitor
			addLoadMonitor(instanceId);

			// Request context to load the procedure
			Logger.debug("Requesting context to schedule procedure " + instanceId, Level.PROC, this);
			s_ctx.openExecutor(instanceId, condition, null, false);

			// Report progress
			monitor.worked(1);

			// Report progress
			monitor.subTask("Waiting for procedure to be ready");

			// Wait until the procedure is actually loaded on server side and
			// ready, then
			// update the configuration. This is given by the LOADED
			// notification coming
			// from the procedure.
			waitLoaded(instanceId);

			// Report progress
			monitor.worked(1);
		}
		catch (LoadFailed ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.PROC, this);
			// Remove the model
			m_models.deleteLocalProcedureModel(instanceId);
			// Kill the process
			Logger.debug("Requesting context to kill procedure " + instanceId, Level.PROC, this);
			s_ctx.killExecutor(instanceId);
			// Rethrow
			throw ex;
		}
		catch (ContextError ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.PROC, this);
			// Remove the model
			m_models.deleteLocalProcedureModel(instanceId);
			// The procedure could not be loaded due to an error in the context
			// processing
			throw new LoadFailed("Could not load the procedure '" + instanceId + "'.\n\n" + ex.getLocalizedMessage());
		}
		finally
		{
			removeLoadMonitor(instanceId);
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			Logger.debug("Requesting context to kill procedure " + instanceId, Level.PROC, this);
			s_ctx.killExecutor(instanceId);
			m_models.deleteLocalProcedureModel(instanceId);
			return;
		}

		try
		{
			// Once the model is loaded and the process is up and running,
			// update the model with the information
			monitor.subTask("Updating procedure status");
			m_models.updateLocalProcedureModel(instanceId, null, ClientMode.CONTROL, null, monitor);
			// Report progress
			monitor.worked(1);
		}
		catch (Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.PROC, this);
			m_models.deleteLocalProcedureModel(instanceId);
			// The procedure could not be loaded due to an error in the context
			// processing
			throw new LoadFailed("Could not load the procedure '" + instanceId + "'.\n\n" + ex.getLocalizedMessage());
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			Logger.debug("Requesting context to kill procedure " + instanceId, Level.PROC, this);
			s_ctx.killExecutor(instanceId);
			m_models.deleteLocalProcedureModel(instanceId);
			return;
		}

		// Report progress
		monitor.subTask("Ending load process");
		notifyExtensionsProcedureReady(model);
		// Report progress
		monitor.done();
	}

	/***************************************************************************
	 * Close a given procedure instance.
	 * 
	 * @param instanceId
	 *            The procedure identifier, WITH instance number info
	 * @throws UnloadFailed
	 *             if the procedure could not be unloaded
	 **************************************************************************/
	@Override
	public void closeProcedure(String instanceId, IProgressMonitor monitor) throws UnloadFailed
	{
		Logger.info("Closing procedure " + instanceId, Level.PROC, this);
		if (isLocallyLoaded(instanceId))
		{
			closeLocalProcedure(instanceId, monitor);
		}
		else
		{
			unloadRemoteProcedure(instanceId, false, monitor);
		}
	}

	/***************************************************************************
	 * Release a given procedure instance.
	 * 
	 * @param instanceId
	 *            The procedure identifier, WITH instance number info
	 * @throws UnloadFailed
	 *             if the procedure could not be unloaded
	 **************************************************************************/
	@Override
	public void releaseProcedure(String instanceId, boolean background, IProgressMonitor monitor) throws UnloadFailed
	{
		Logger.info("Releasing procedure " + instanceId, Level.PROC, this);
		m_models.convertToRemote(instanceId, monitor);
		// Unload the local procedure
		releaseLocalProcedure(instanceId, background, monitor);
	}

	/***************************************************************************
	 * Force removing the control of a procedure
	 * 
	 * @param instanceId
	 *            The procedure identifier, WITH instance number info
	 **************************************************************************/
	@Override
	public void removeControl(String instanceId, IProgressMonitor monitor) throws UnloadFailed
	{
		Logger.info("Removing control from procedure " + instanceId, Level.PROC, this);
		try
		{
			s_ctx.removeControl(instanceId);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new UnloadFailed(ex.getLocalizedMessage());
		}
		monitor.done();
	}

	/***************************************************************************
	 * Kill a given procedure instance.
	 * 
	 * @param instanceId
	 *            The procedure identifier, WITH instance number info
	 * @throws UnloadFailed
	 *             if the procedure could not be unloaded
	 **************************************************************************/
	@Override
	public void killProcedure(String instanceId, IProgressMonitor monitor) throws UnloadFailed
	{
		Logger.info("Killing procedure " + instanceId, Level.PROC, this);
		if (isLocallyLoaded(instanceId))
		{
			killLocalProcedure(instanceId, monitor);
		}
		else
		{
			unloadRemoteProcedure(instanceId, true, monitor);
		}
	}

	/***************************************************************************
	 * Obtain a local procedure model.
	 * 
	 * @param instanceId
	 *            The instance identifier
	 * @return The procedure model
	 **************************************************************************/
	@Override
	public IProcedure getProcedure(String instanceId) throws NoSuchProcedure
	{
		return m_models.getProcedure(instanceId);
	}

	/***************************************************************************
	 * Check if a procedure is loaded.
	 * 
	 * @param instanceId
	 *            The instance identifier
	 * @return True if the model is loaded
	 **************************************************************************/
	@Override
	public boolean isLocallyLoaded(String instanceId)
	{
		return m_models.isLocal(instanceId);
	}

	/***************************************************************************
	 * Obtain a remote procedure model.
	 * 
	 * @param instanceId
	 *            The instance identifier
	 * @return The procedure reduced model
	 **************************************************************************/
	@Override
	public synchronized IProcedure getRemoteProcedure(String instanceId) throws NoSuchProcedure
	{
		return m_models.getRemoteProcedure(instanceId);
	}

	/***************************************************************************
	 * Obtain the list of available procedures in context
	 * 
	 * @return The procedure list
	 **************************************************************************/
	@Override
	public Map<String, String> getAvailableProcedures(boolean refresh)
	{
		return m_models.getAvailableProcedures(refresh);
	}

	/***************************************************************************
	 * Refresh the list of available procedures in context
	 * 
	 * @return The procedure list
	 **************************************************************************/
	@Override
	public void refreshAvailableProcedures()
	{
		m_models.obtainAvailableProcedures(true);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public Set<String> getOpenLocalProcedures()
	{
		return m_models.getOpenLocalProcedures();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public Set<String> getOpenRemoteProcedures(boolean refresh)
	{
		if (refresh)
		{
			m_models.obtainRemoteProcedures();
		}
		return m_models.getOpenRemoteProcedures();
	}

	/***************************************************************************
	 * Trigger a SPELL command
	 * 
	 * @param cmd
	 *            ExecutorCommand instance
	 * @param procId
	 *            The procedure identifier
	 **************************************************************************/
	@Override
	public void issueCommand(ExecutorCommand cmd, String instanceId) throws CommandFailed, NoSuchProcedure
	{
		// Check if the proxy is connected before trying to send a command
		checkConnectivity();
		IProcedure model = m_models.getProcedure(instanceId);
		model.getController().issueCommand(cmd, new String[0]);
	}

	/***************************************************************************
	 * Request the procedure properties
	 **************************************************************************/
	@Override
	public TreeMap<ProcProperties, String> getProcedureProperties(String procedureId)
	{
		return m_models.getProcedureProperties(procedureId);
	}

	/**************************************************************************
	 * Get procedure name given its Id
	 * 
	 * @param procId
	 * @return
	 *************************************************************************/
	@Override
	public String getProcedureName(String procId)
	{
		return m_models.getProcedureName(procId);
	}

	/**************************************************************************
	 * See ProcedureModelManager::getCompleteProcedureId
	 * 
	 * @param procId
	 * @return
	 *************************************************************************/
	@Override
	public String getCompleteProcedureId( String procId )
	{
		return m_models.getCompleteProcedureId(procId);
	}

	/**************************************************************************
	 * See ProcedureModelManager::isProcedureIdAvailable
	 * 
	 * @param procId
	 * @return
	 *************************************************************************/
	@Override
	public boolean isProcedureIdAvailable( String procId )
	{
		return m_models.isProcedureIdAvailable( procId );
	}
	/*
	 * #########################################################################
	 * # EXTENSION IMPLEMENTATION: IProcedureRuntime # # This set of methods is
	 * used to receive procedure data from the SPELL # client core. All data is
	 * redirected to the corresponding procedure # model.
	 * #######################################################################
	 */

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureDisplay(DisplayData data)
	{
		String instanceId = data.getProcId();
		// Check that the procedure is loaded
		if (m_models.isLocal(instanceId))
		{
			// Update the model
			IProcedure procedure = m_models.getProcedure(instanceId);
			if (!procedure.isInReplayMode())
			{
				procedure.getRuntimeProcessor().notifyProcedureDisplay(data);
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureError(ErrorData data)
	{
		String instanceId = data.getOrigin();
		// Check that the procedure is loaded
		if (m_models.isLocal(instanceId))
		{
			Logger.error("Procedure '" + instanceId + "' error: " + data.getMessage() + ", " + data.getReason(), Level.PROC, this);

			// Set the model to error status
			IProcedure procedure = m_models.getProcedure(instanceId);
			procedure.getController().setError(data);
			// Notify load monitor
			notifyLoadMonitor(instanceId, ExecutorStatus.ERROR);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureItem(ItemNotification data)
	{
		String instanceId = data.getProcId();
		// Check that the procedure is loaded
		if (m_models.isLocal(instanceId))
		{
			// Send the item notification to the model
			IProcedure procedure = m_models.getProcedure(instanceId);
			if (!procedure.isInReplayMode())
			{
				procedure.getRuntimeProcessor().notifyProcedureItem(data);
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureStack(StackNotification data)
	{
		String instanceId = data.getProcId();
		// Check that the procedure is loaded
		if (m_models.isLocal(instanceId))
		{
			// Send the item notification to the model
			IProcedure procedure = m_models.getProcedure(instanceId);
			if (!procedure.isInReplayMode())
			{
				procedure.getRuntimeProcessor().notifyProcedureStack(data);
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureStatus(StatusNotification data)
	{
		String instanceId = data.getProcId();
		// Check that the procedure is loaded
		if (m_models.isLocal(instanceId))
		{
			Logger.debug("NOTIFY from context: LOCAL procedure status " + data.getStatus(), Level.PROC, this);
			IProcedure model = m_models.getProcedure(instanceId);
			Logger.debug("Updating model", Level.PROC, this);
			// Send the item notification to the model
			model.getRuntimeProcessor().notifyProcedureStatus(data);
			Logger.debug("Notify load monitor", Level.PROC, this);
			notifyLoadMonitor(instanceId, data.getStatus());
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureUserAction(UserActionNotification data)
	{
		String instanceId = data.getProcId();
		// Check that the procedure is loaded
		if (m_models.isLocal(instanceId))
		{
			IProcedure model = m_models.getProcedure(instanceId);
			if (!model.isInReplayMode())
			{
				// Just redirect the data to the consumers
				ProcedureNotifications.get().fireProcedureUserAction(model, data);
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureConfiguration(ExecutorConfig data)
	{
		String instanceId = data.getProcId();
		// If we are monitoring the procedure (locally loaded)
		if (m_models.isLocal(instanceId))
		{
			IProcedure model = m_models.getProcedure(instanceId);
			if (model.getRuntimeInformation().getClientMode().equals(ClientMode.MONITOR))
			{
				model.getController().setRunInto(data.getRunInto());
				model.getController().setStepByStep(data.getStepByStep());
				model.getController().setBrowsableLib(data.getBrowsableLib());
				model.getController().setExecutionDelay(data.getExecDelay());
				model.getController().setPromptWarningDelay(data.getPromptWarningDelay());
				model.getController().setForceTcConfirmation(data.getTcConfirmation());
				
				// Redirect the data to the consumers
				ProcedureNotifications.get().fireProcedureConfiguration(model);
			}
		}
	}

	/*
	 * #########################################################################
	 * # EXTENSION IMPLEMENTATION: ICoreProcedureOperationListener # # This set of methods
	 * is used to keep track of the procedures active in # the execution
	 * environment (SPELL server side), which are controlled # by other clients.
	 * This is required for: # # A) Informing the user about the status of
	 * remote procedures # B) Procedure instance number management # C) User
	 * hand-over and other remote procedure interaction #
	 * #######################################################################
	 */

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyRemoteProcedureClosed(String instanceId, String guiKey)
	{
		Logger.debug("NOTIFIED from context: remote procedure closed: " + instanceId, Level.PROC, this);
		if (!m_models.isLocal(instanceId))
		{
			m_models.deleteRemoteProcedureModel(instanceId);
		}
		else
		{
			// The procedure is locally loaded. We shall be in monitoring mode,
			// since if we were controlling this notification would not come.
			IProcedure model = m_models.getProcedure(instanceId);
			ProcedureNotifications.get().fireModelUnloaded(model, UnloadType.MONITORED_CLOSED);
			m_models.deleteLocalProcedureModel(instanceId);
		}
		Logger.debug("NOTIFIED from context: remote procedure closed: " + instanceId + " DONE", Level.PROC, this);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyRemoteProcedureControlled(String instanceId, String guiKey)
	{
		Logger.debug("NOTIFIED from context: remote procedure controlled: " + instanceId, Level.PROC, this);

		// If the procedure was local but the client key is not mine
		if (m_models.isLocal(instanceId) && !s_ctx.getClientKey().equals(guiKey))
		{
			// If the procedure was being controlled, the control has been stolen.
			IProcedure model = getProcedure(instanceId);
			if (model.getRuntimeInformation().getClientMode().equals(ClientMode.CONTROL))
			{
				ProcedureNotifications.get().fireModelUnloaded(model, UnloadType.CONTROL_STOLEN);
				m_models.deleteLocalProcedureModel(instanceId);
			}
		}
		else
		{
			IProcedure proc = getRemoteProcedure(instanceId);
			IExecutionInformationHandler handler = (IExecutionInformationHandler) proc.getRuntimeInformation();
			handler.setControllingClient( new ProcedureClient(guiKey,"???"));
		}
		Logger.debug("NOTIFIED from context: remote procedure controlled: " + instanceId + " DONE", Level.PROC, this);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyRemoteProcedureKilled(String instanceId, String guiKey)
	{
		Logger.debug("NOTIFIED from context: remote procedure killed: " + instanceId, Level.PROC, this);
		if (!m_models.isLocal(instanceId))
		{
			m_models.deleteRemoteProcedureModel(instanceId);
		}
		else
		{
			// The procedure is locally loaded. We need to distinguish whether we were
			// in control or not.
			IProcedure model = m_models.getProcedure(instanceId);
			if (model.getRuntimeInformation().getClientMode().equals(ClientMode.MONITOR))
			{
				ProcedureNotifications.get().fireModelUnloaded(model, UnloadType.MONITORED_KILLED);
			}
			else
			{
				ProcedureNotifications.get().fireModelUnloaded(model, UnloadType.CONTROLLED_OTHER_KILLED);
			}
			m_models.deleteLocalProcedureModel(instanceId);
		}
		Logger.debug("NOTIFIED from context: remote procedure killed: " + instanceId + " DONE", Level.PROC, this);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyRemoteProcedureCrashed(String instanceId, String guiKey)
	{
		if (!s_ctx.isConnected()) return;
		
		Logger.debug("NOTIFIED from context: remote procedure crashed: " + instanceId, Level.PROC, this);
		if (!m_models.isLocal(instanceId))
		{
			m_models.deleteRemoteProcedureModel(instanceId);
		}
		else
		{
			Logger.debug("Procedure was locally loaded: " + instanceId, Level.PROC, this);
			// The procedure is locally loaded. We shall be in monitoring mode,
			// since if we were controlling this notification would not come.
			IProcedure model = m_models.getProcedure(instanceId);
			if (model.getRuntimeInformation().getClientMode().equals(ClientMode.CONTROL))
			{
				Logger.debug("Procedure was under our control: " + instanceId, Level.PROC, this);
				ProcedureNotifications.get().fireModelUnloaded(model, UnloadType.CONTROLLED_CRASHED);
			}
			else
			{
				Logger.debug("Procedure was being monitored: " + instanceId, Level.PROC, this);
				ProcedureNotifications.get().fireModelUnloaded(model, UnloadType.MONITORED_CRASHED);
			}
			m_models.disableProcedure("Executor process crashed", model);
		}
		Logger.debug("NOTIFIED from context: remote procedure crashed: " + instanceId + " DONE", Level.PROC, this);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyRemoteProcedureMonitored(String instanceId, String guiKey)
	{
		Logger.debug("NOTIFIED from context: remote procedure monitored: " + instanceId, Level.PROC, this);
		IProcedure proc = null;
		if (m_models.isLocal(instanceId))
		{
			Logger.debug("Adding new monitoring client to local model", Level.PROC, this);
			proc = getProcedure(instanceId);
		}
		else
		{
			Logger.debug("Adding new monitoring client to remote model", Level.PROC, this);
			proc = getRemoteProcedure(instanceId);
		}
		IExecutionInformationHandler handler = (IExecutionInformationHandler) proc.getRuntimeInformation();
		handler.addMonitoringClient(new ProcedureClient(guiKey, "???"));
		
		Logger.debug("NOTIFIED from context: remote procedure monitored: " + instanceId + " DONE", Level.PROC, this);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyRemoteProcedureOpen(String instanceId, String guiKey)
	{
		Logger.debug("NOTIFIED from context: procedure open: " + instanceId, Level.PROC, this);
		if (!m_models.isLocal(instanceId) && !m_models.isRemote(instanceId))
		{
			Logger.debug("A remote procedure model needs to be created" + instanceId, Level.PROC, this);
			m_models.createRemoteProcedureModel(instanceId);
			// If the gui key corresponds to this GUI there are two
			// possibilities:
			// 1) The procedure is loaded locally, just ignore the notification
			// 2) The procedure is not loaded. Then it is a child procedure
			// being started by a parent proc controlled by this gui.
			if (s_ctx.getClientKey().equals(guiKey))
			{
				Logger.debug("Grabbing subprocedure: " + instanceId, Level.PROC, this);
				try
				{
					IExecutorInfo info = s_ctx.getExecutorInfo(instanceId);
					if (info.getVisible())
					{
						new GrabProcedureTask(this, instanceId, ClientMode.CONTROL, true).start();
					}
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
		else if (m_models.isRemote(instanceId))
		{
			try
			{
				Logger.debug("Getting information from new remote procedure: " + instanceId, Level.PROC, this);
				m_models.updateRemoteProcedureModel(instanceId);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		Logger.debug("NOTIFIED from context: procedure open: " + instanceId + " DONE", Level.PROC, this);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyRemoteProcedureReleased(String instanceId, String guiKey)
	{
		Logger.debug("NOTIFIED from context: procedure released: " + instanceId, Level.PROC, this);
		IProcedure proc = null;
		if (m_models.isLocal(instanceId))
		{
			Logger.debug("Removing client from local model", Level.PROC, this);
			proc = getProcedure(instanceId);
		}
		else
		{
			proc = getRemoteProcedure(instanceId);
		}
		IExecutionInformationHandler handler = (IExecutionInformationHandler) proc.getRuntimeInformation();
		if (handler.getControllingClient() != null && handler.getControllingClient().getKey().equals(guiKey))
		{
			Logger.debug("Removing controlling client (" + guiKey + ") from model", Level.PROC, this);
			handler.setControllingClient(null);
			try
            {
				// Check if the procedure becomes background
	            proc.getController().refresh();
				Logger.debug("Procedure put in background: " + proc.getRuntimeInformation().isBackground(), Level.PROC, this);
            }
            catch (Exception e)
            {
	            e.printStackTrace();
            }
		}
		else 
		{
			IProcedureClient toRemove = null;
			for(IProcedureClient clt : handler.getMonitoringClients())
			{
				if (clt.getKey().equals(guiKey))
				{
					toRemove = clt; 
					break;
				}
			}
			if (toRemove != null)
			{
				Logger.debug("Removing monitoring client (" + guiKey + ") from model", Level.PROC, this);
				handler.removeMonitoringClient(toRemove);
			}
		}
		Logger.debug("NOTIFIED from context: procedure released: " + instanceId + " DONE", Level.PROC, this);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyRemoteProcedureStatus(String instanceId, ExecutorStatus status, String guiKey)
	{
		Logger.debug("NOTIFIED from context: procedure status " + status.name() + ": " + instanceId, Level.PROC, this);
		// Filter out if the procedure is local. Extra notifications may arrive
		// during a transition to local or opposite
		if (!m_models.isLocal(instanceId) && m_models.isRemote(instanceId))
		{
			try
			{
				Logger.debug("Updating status for remote procedure: " + instanceId, Level.PROC, this);
				IExecutionInformationHandler handler = (IExecutionInformationHandler) getRemoteProcedure(instanceId).getRuntimeInformation();
				handler.setExecutorStatus( status );
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		Logger.debug("NOTIFIED from context: procedure status: " + instanceId + " DONE", Level.PROC, this);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyRemoteProcedureSummary(String instanceId, ExecutorOperationSummary summary, String guiKey)
	{
		Logger.debug("NOTIFIED from context: procedure summary: " + instanceId, Level.PROC, this);
		// Filter out if the procedure is local. Extra notifications may arrive
		// during a transition to local or opposite
		if (!m_models.isLocal(instanceId) && m_models.isRemote(instanceId))
		{
			try
			{
				Logger.debug("Updating summary for remote procedure: " + instanceId, Level.PROC, this);
				IExecutionInformationHandler handler = (IExecutionInformationHandler) getRemoteProcedure(instanceId).getRuntimeInformation();
				if (summary.stageId != null && summary.stageTitle != null)
				{
					if (!summary.stageId.trim().isEmpty() && !summary.stageTitle.trim().isEmpty())
					handler.setStage( summary.stageId, summary.stageTitle );
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		Logger.debug("NOTIFIED from context: procedure status: " + instanceId + " DONE", Level.PROC, this);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureControl(ControlNotification data)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * #########################################################################
	 * # EXTENSION IMPLEMENTATION: ICoreProcedureInputListener # # This set of methods is
	 * used to receive procedure input requests from # the SPELL client core.
	 * All requests are redirected to the corresponding # procedure model.
	 * #######################################################################
	 */

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedurePrompt(InputData inputData)
	{
		String instanceId = inputData.getProcId();
		// Check that the procedure is loaded
		if (m_models.isLocal(instanceId))
		{
			IProcedure model = m_models.getProcedure(instanceId);
			model.getController().notifyProcedurePrompt(inputData);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureFinishPrompt(InputData inputData)
	{
		String instanceId = inputData.getProcId();
		// Check that the procedure is loaded
		if (m_models.isLocal(instanceId))
		{
			IProcedure model = m_models.getProcedure(instanceId);
			model.getController().notifyProcedureFinishPrompt(inputData);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureCancelPrompt(InputData inputData)
	{
		String instanceId = inputData.getProcId();
		// Check that the procedure is loaded
		if (m_models.isLocal(instanceId))
		{
			IProcedure model = m_models.getProcedure(instanceId);
			model.getController().notifyProcedureCancelPrompt(inputData);
		}
	}

	/*
	 * #########################################################################
	 * # EXTENSION IMPLEMENTATION: IContextOperation # # This set of methods is
	 * used to receive events regarding the status # of the Context proxy
	 * connection.
	 * #######################################################################
	 */

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyContextAttached(ContextInfo ctx)
	{
		// Once the context is connected, load the list of available procedures
		m_models.obtainAvailableProcedures(true);
		// Build the list of remote procedure models
		m_models.obtainRemoteProcedures();

		// FIXME: this is still not possible since re-attached contexts
		// do not have control on procedures open in previous context instances
		// We shall enable any open model since we can control the SEE
		// counterpart again
		// m_models.enableProcedures();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyContextDetached()
	{
		Logger.warning("Connection with context closed", Level.PROC, this);
		// If the context is detached we cannot open any procedure, so clear
		// the list of available procedures
		m_models.clearAvailableProcedures();
		// Clear the list of remote procedure models
		m_models.clearRemoteProcedures();
		// We shall disable any open model since we cannot control the SEE
		// counterpart
		m_models.disableProcedures("Context detached");
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyContextError(ErrorData error)
	{
		Logger.warning("Connection with context error", Level.PROC, this);
		// If the context is detached we cannot open any procedure, so clear
		// the list of available procedures
		m_models.clearAvailableProcedures();
		// Clear the list of remote procedure models
		m_models.clearRemoteProcedures();
		// We shall disable any open model since we cannot control the SEE
		// counterpart
		m_models.disableProcedures("Lost connection with context");
	}

	/*
	 * #########################################################################
	 * # INTERNAL METHODS
	 * #######################################################################
	 */

	/***************************************************************************
	 * Check if we have connection to context
	 * 
	 * @throws NotConnected
	 *             if context proxy is not connected
	 **************************************************************************/
	private void checkConnectivity() throws NotConnected
	{
		if (!s_ctx.isConnected())
		{
			throw new NotConnected("Cannot operate: not conected to context");
		}
	}

	/***************************************************************************
	 * Attach to a remote procedure, so that it becomes local
	 * 
	 * @param instanceId
	 *            Procedure instance identifier
	 **************************************************************************/
	private void attachToRemoteProcedure(String instanceId, ClientMode mode, AsRunReplayResult result, IProgressMonitor monitor) throws LoadFailed
	{
		Logger.debug("attachToRemoteProcedure()", Level.PROC, this);
		
		Logger.info("Attaching to remote procedure in mode " + mode, Level.PROC, this);

		// Will hold the model
		IProcedure model = null;

		// Start the task in the monitor
		monitor.beginTask("Attaching to procedure", 8);

		// Check cancellation
		if (monitor.isCanceled())
		{
			Logger.debug("attachToRemoteProcedure() canceled", Level.PROC, this);
			return;
		}
		
		try
		{
			Logger.debug("Converting to local model", Level.PROC, this);

			// Create the model, Notify about the progress
			monitor.subTask("Creating model");
			model = m_models.convertToLocal(instanceId, mode);
			model.setReplayMode(true);
			instanceId = model.getProcId();
			monitor.worked(1);
		}
		catch (LoadFailed ex)
		{
			Logger.error("attachToRemoteProcedure() failed: " + ex.getLocalizedMessage(), Level.PROC, this);
			monitor.subTask("ERROR: cannot open the procedure: " + ex);
			throw ex;
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			Logger.debug("attachToRemoteProcedure() canceled", Level.PROC, this);
			m_models.deleteLocalProcedureModel(instanceId);
			return;
		}

		// Now ask the context to attach the procedure process
		// Ask the context to launch or attach to the proc. It will return the
		// executor information,
		// which is the procedure data known at core level.
		try
		{

			if (mode.equals(ClientMode.CONTROL))
			{
				// Update the model before, we may not need to attach. Also, if
				// it is controlled
				// by somebody else we need to fail
				Logger.debug("Getting controlling client information" + instanceId, Level.PROC, this);
				model.getController().refresh();

				if (model.getRuntimeInformation().getControllingClient() == null)
				{
					Logger.debug("Requesting attach to context", Level.PROC, this);
					// Report progress
					monitor.subTask("Attaching to process");
					// Request context to load the procedure
					Logger.debug("Request context to attach procedure" + instanceId, Level.PROC, this);
					s_ctx.attachToExecutor(instanceId, mode);
					// Report progress
					monitor.worked(1);
				}
				else
				{
					if (!model.getRuntimeInformation().getControllingClient().getKey().equals(s_ctx.getClientKey()))
					{
						throw new ContextError("Cannot control procedure " + instanceId + ": already controlled by somebody else");
					}
					// If it was my key, do nothing
				}
			}
			// In monitoring, do attach
			else
			{
				Logger.debug("Requesting attach to context", Level.PROC, this);
				// Report progress
				monitor.subTask("Attaching to process");
				// Request context to load the procedure
				Logger.debug("Request context to attach procedure" + instanceId, Level.PROC, this);
				s_ctx.attachToExecutor(instanceId, mode);
				// Report progress
				monitor.worked(1);
			}

			// TODO check failure here!!
		}
		catch (Exception err)
		{
			Logger.error("Error when attaching to executor: " + err.getLocalizedMessage(), Level.PROC, this);
			// Remove the model
			m_models.deleteLocalProcedureModel(instanceId);
			// The procedure could not be loaded due to an error in the context
			// processing
			throw new LoadFailed("Could not load the procedure '" + instanceId + "'.\n\n" + err.getLocalizedMessage());
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			Logger.debug("attachToRemoteProcedure() canceled", Level.PROC, this);
			s_ctx.detachFromExecutor(instanceId,false);
			m_models.deleteLocalProcedureModel(instanceId);
			return;
		}

		try
		{
			Logger.debug("Update procedure status after attach", Level.PROC, this);
			// Once the model is loaded and the process is up and running,
			// update the model with the information
			monitor.subTask("Updating procedure status");
			m_models.updateLocalProcedureModel(instanceId, null, mode, result, monitor);
			
			// If we are not processing ASRUN check for procedure status: if it is in prompt
			// status we still need to reflect it.
			if (result == null)
			{
				Logger.debug("Not using ASRUN, checking for status PROMPT", Level.PROC, this);
				ExecutorStatus st = s_ctx.getExecutorStatus(instanceId);
				Logger.debug("Obtained procedure status code " + st, Level.PROC, this);
				if (ExecutorStatus.PROMPT.equals(st))
				{
					InputData inputData = s_ctx.getExecutorPromptData(instanceId);
					model.getController().notifyProcedurePrompt(inputData);
					Logger.debug("Set input data: " + inputData.getText(), Level.PROC, this);
				}
				
				// In any case recreate the stack position
				String csp = s_ctx.getExecutorCallstack(instanceId);
				if (csp  != null)
				{
					String[] elements = csp.split(":");
					for(int idx=0; idx<elements.length; idx=idx+2)
					{
						StackNotification stack = new StackNotification(StackType.CALL, instanceId, elements[idx] + ":" + elements[idx+1], elements[idx] );
						model.getRuntimeProcessor().notifyProcedureStack(stack);
					}
				}
			}
			
			// Report progress
			monitor.worked(1);
		}
		catch (Exception err)
		{
			err.printStackTrace();
			Logger.error("Error when updating local procedure model: " + err.getLocalizedMessage(), Level.PROC, this);
			Logger.debug("Requesting context to detach from procedure : " + instanceId, Level.PROC, this);
			s_ctx.detachFromExecutor(instanceId,false);
			m_models.deleteLocalProcedureModel(instanceId);
			// The procedure could not be loaded due to an error in the context
			// processing
			throw new LoadFailed("Could not load the procedure '" + instanceId + "'.\n\n" + err.getLocalizedMessage());
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			Logger.debug("attachToRemoteProcedure() canceled", Level.PROC, this);
			Logger.debug("Requesting context to detach from procedure : " + instanceId, Level.PROC, this);
			s_ctx.detachFromExecutor(instanceId,false);
			m_models.deleteLocalProcedureModel(instanceId);
			return;
		}

		// Remove the remote model
		Logger.debug("Delete model after attach", Level.PROC, this);
		m_models.deleteRemoteProcedureModel(instanceId);

		// Update dependencies if applicable
		if (!model.isMain())
		{
			try
			{
				Logger.debug("Check if parent dependency should be notified", Level.PROC, this);
				String parentInstanceId = model.getParent();
				String childInstanceId = model.getProcId();
				Logger.debug("  - parent: " + parentInstanceId, Level.PROC, this);
				Logger.debug("  - child : " + childInstanceId, Level.PROC, this);
				if (m_models.isLocal(parentInstanceId))
				{
					IProcedure parentProcedure = getProcedure(parentInstanceId);
					int lineNo = model.getRuntimeInformation().getParentCallingLine();
					parentProcedure.getDependenciesManager().onChildOpen(childInstanceId, lineNo);
				}
			}
			catch(Exception ex)
			{
				Logger.error("Failed to update dependency (parent): " + ex.getLocalizedMessage(), Level.PROC, this);
			}
		}
		
		// Report progress
		monitor.subTask("Ending load process");
		model.setReplayMode(false);
		model.getRuntimeProcessor().reset();
		notifyExtensionsProcedureReady(model);
		// Report progress
		monitor.done();
		Logger.debug("attachToRemoteProcedure() finished", Level.PROC, this);
	}

	/***************************************************************************
	 * Remove a local procedure model
	 * 
	 * @param instanceId
	 *            Procedure instance identifier
	 **************************************************************************/
	private void notifyExtensionsProcedureReady(IProcedure model)
	{
		Logger.debug("notifyExtensionsProcedureReady()", Level.PROC, this);
		
		// Reached to this point we have the model of the procedure created.
		// Now notify any plugin extensions implementing the ProcedureView
		// support.
		ProcedureNotifications.get().fireModelLoaded(model);

		// Unless the model is initially in error state, notify about the
		// model configuration and reset it
		ExecutorStatus st = model.getRuntimeInformation().getStatus();
		if (!st.equals(ExecutorStatus.ERROR))
		{
			// Notify about procedure configuration (the first time it is
			// initialized with the executor data
			ProcedureNotifications.get().fireModelConfigured(model);
		}
	}

	/***************************************************************************
	 * Close a given procedure instance. The sequence of operations is:
	 * 
	 * 1) Check that we have connectivity and the instance id 2) Request the
	 * context to unload the procedure 3) Unload the model 4) Notify extensions
	 * 
	 * @param instanceId
	 *            The procedure identifier, WITH instance number info
	 * @throws UnloadFailed
	 *             if the procedure could not be unloaded
	 **************************************************************************/
	private void closeLocalProcedure(String instanceId, IProgressMonitor monitor) throws UnloadFailed
	{
		Logger.debug("closeLocalProcedure()", Level.PROC, this);

		monitor.beginTask("Close procedure", 3);
		IProcedure model = getProcedure(instanceId);

		monitor.subTask("Removing local model");
		monitor.worked(1);
		
		// Remove the model directly
		m_models.deleteLocalProcedureModel(instanceId);

		if (!s_ctx.isConnected() || model.getRuntimeInformation().isExecutorLost())
		{
			monitor.worked(3);
			monitor.done();
		}
		else
		{
			try
			{
				// Check condition: the client mode shall be controlling
				monitor.subTask("Checking client mode");
				ClientMode mode = model.getRuntimeInformation().getClientMode();
				if (mode != ClientMode.CONTROL)
				{
					monitor.setCanceled(true);
					monitor.done();
					throw new UnloadFailed("Cannot close this procedure while in monitoring mode");
				}
				// Report progress
				monitor.worked(1);

				monitor.subTask("Closing process");
				// Request context to close the procedure
				Logger.debug("Requesting context to close procedure : " + instanceId, Level.PROC, this);
				if (!s_ctx.closeExecutor(instanceId))
				{
					// The context command was sent but it raised an error
					ProcedureNotifications.get().fireModelUnloaded(model, UnloadType.CONTROLLED_CLOSED);
					monitor.setCanceled(true);
					monitor.done();
					throw new UnloadFailed("Failed to close the procedure " + instanceId + " on the server");
				}

				// Report progress
				monitor.worked(1);
			}
			catch (ContextError err)
			{
				Logger.error(err.getLocalizedMessage(), Level.PROC, this);
				// The procedure could not be unloaded due to an error in the
				// context
				// processing
				monitor.setCanceled(true);
				monitor.done();
				throw new UnloadFailed("Failed to close the procedure " + instanceId + ": " + err.getLocalizedMessage());
			}
		}
		
		// Update dependencies if applicable
		if (!model.isMain())
		{
			String parentInstanceId = model.getParent();
			String childInstanceId = model.getProcId();
			try
			{
				IProcedure parentProcedure = getProcedure(parentInstanceId);
				parentProcedure.getDependenciesManager().onChildClosed(childInstanceId);
			}
			catch(NoSuchProcedure ignore){};
		}

		// Reached to this point we have the model of the procedure created.
		// Now notify any plugin extensions implementing the ProcedureView
		// support.
		ProcedureNotifications.get().fireModelUnloaded(model, UnloadType.CONTROLLED_CLOSED);
	}

	/***************************************************************************
	 * Release a given procedure instance. The sequence of operations is:
	 * 
	 * 1) Check that we have connectivity and the instance id 2) Request the
	 * context to unload the procedure 3) Unload the model 4) Notify extensions
	 * 
	 * @param instanceId
	 *            The procedure identifier, WITH instance number info
	 * @throws UnloadFailed
	 *             if the procedure could not be unloaded
	 **************************************************************************/
	private void releaseLocalProcedure(String instanceId, boolean background, IProgressMonitor monitor) throws UnloadFailed
	{
		Logger.debug("releaseLocalProcedure()", Level.PROC, this);

		monitor.beginTask("Release procedure", 2);

		IProcedure model = getProcedure(instanceId);

		monitor.subTask("Removing local model");
		monitor.worked(1);
		// Remove the model directly
		m_models.deleteLocalProcedureModel(instanceId);

		if (!s_ctx.isConnected())
		{
			monitor.worked(2);
			monitor.done();
		}
		else
		{
			try
			{
				// Report progress
				monitor.subTask("Detaching");
				// Request context to close the procedure
				Logger.debug("Requesting context to detach procedure " + instanceId, Level.PROC, this);
				if (!s_ctx.detachFromExecutor(instanceId,background))
				{
					// The context command was sent but it raised an error
					if (model.getRuntimeInformation().getClientMode().equals(ClientMode.CONTROL))
					{
						ProcedureNotifications.get().fireModelUnloaded(model, UnloadType.CONTROLLED_RELEASED);
					}
					else
					{
						ProcedureNotifications.get().fireModelUnloaded(model, UnloadType.MONITORED_RELEASED);
					}
					monitor.setCanceled(true);
					monitor.done();
					throw new UnloadFailed("Failed to release the procedure " + instanceId + " on the server");
				}
				// Report progress
				monitor.worked(1);
			}
			catch (ContextError err)
			{
				Logger.error(err.getLocalizedMessage(), Level.PROC, this);
				// The procedure could not be unloaded due to an error in the
				// context
				// processing
				monitor.setCanceled(true);
				monitor.done();
				throw new UnloadFailed("Failed to release the procedure " + instanceId + ": " + err.getLocalizedMessage());
			}
		}

		// Reached to this point we have the model of the procedure created.
		// Now notify any plugin extensions implementing the ProcedureView
		// support.
		if (model.getRuntimeInformation().getClientMode().equals(ClientMode.CONTROL))
		{
			ProcedureNotifications.get().fireModelUnloaded(model, UnloadType.CONTROLLED_RELEASED);
		}
		else
		{
			ProcedureNotifications.get().fireModelUnloaded(model, UnloadType.MONITORED_RELEASED);
		}
	}

	/***************************************************************************
	 * Kill a given procedure instance. The sequence of operations is:
	 * 
	 * 1) Check that we have connectivity and the instance id 2) Request the
	 * context to unload the procedure 3) Unload the model 4) Notify extensions
	 * 
	 * @param instanceId
	 *            The procedure identifier, WITH instance number info
	 * @throws UnloadFailed
	 *             if the procedure could not be unloaded
	 **************************************************************************/
	private void killLocalProcedure(String instanceId, IProgressMonitor monitor) throws UnloadFailed
	{
		Logger.debug("killLocalProcedure()", Level.PROC, this);
		
		monitor.beginTask("Kill procedure", 3);
		IProcedure model = getProcedure(instanceId);

		monitor.subTask("Removing local model");
		monitor.worked(1);

		// Remove the model directly
		m_models.deleteLocalProcedureModel(instanceId);

		if (!s_ctx.isConnected())
		{
			monitor.worked(3);
			monitor.done();
		}
		else
		{
			try
			{
				// Check condition: the client mode shall be controlling
				monitor.subTask("Checking client mode");
				ClientMode mode = model.getRuntimeInformation().getClientMode();
				if (mode != ClientMode.CONTROL)
				{
					monitor.setCanceled(true);
					monitor.done();
					throw new UnloadFailed("Cannot kill this procedure while in monitoring mode");
				}
				// Report progress
				monitor.worked(1);

				monitor.subTask("Killing process");
				// Request context to close the procedure
				Logger.debug("Requesting context to kill procedure : " + instanceId, Level.PROC, this);
				if (!s_ctx.killExecutor(instanceId))
				{
					// The context command was sent but it raised an error
					ProcedureNotifications.get().fireModelUnloaded(model, UnloadType.CONTROLLED_KILLED);
					monitor.setCanceled(true);
					monitor.done();
					throw new UnloadFailed("Failed to close the procedure " + instanceId + " on the server");
				}

				// Report progress
				monitor.worked(1);
			}
			catch (ContextError err)
			{
				Logger.error(err.getLocalizedMessage(), Level.PROC, this);
				// The procedure could not be unloaded due to an error in the
				// context
				// processing
				monitor.setCanceled(true);
				monitor.done();
				throw new UnloadFailed("Failed to close the procedure " + instanceId + ": " + err.getLocalizedMessage());
			}
		}
		// Reached to this point we have the model of the procedure created.
		// Now notify any plugin extensions implementing the ProcedureView
		// support.
		ProcedureNotifications.get().fireModelUnloaded(model, UnloadType.CONTROLLED_KILLED);
	}

	/***************************************************************************
	 * Unload a remote procedure instance. The sequence of operations is:
	 * 
	 * 1) Check that we have connectivity and the instance id 2) Request the
	 * context to unload the procedure 3) Unload the model 4) Notify extensions
	 * 
	 * @param instanceId
	 *            The procedure identifier, WITH instance number info
	 * @param kill
	 *            If true, the procedure is killed instead of closed
	 * @throws UnloadFailed
	 *             if the procedure could not be unloaded
	 **************************************************************************/
	private void unloadRemoteProcedure(String instanceId, boolean kill, IProgressMonitor monitor) throws UnloadFailed
	{
		Logger.debug("unloadRemoteProcedure()", Level.PROC, this);
		monitor.beginTask("Unload remote procedure", 2);

		// Check proxy connection
		checkConnectivity();

		// Check the identifier
		if (!m_models.isRemote(instanceId))
		{
			monitor.setCanceled(true);
			monitor.done();
			throw new UnloadFailed("No such procedure: '" + instanceId + "'");
		}

		m_models.deleteRemoteProcedureModel(instanceId);
		monitor.worked(1);

		try
		{
			// Request context to load the procedure
			boolean closed = false;
			if (kill)
			{
				monitor.subTask("Killing process");
				Logger.debug("Requesting context to kill procedure : " + instanceId, Level.PROC, this);
				closed = s_ctx.killExecutor(instanceId);
			}
			else
			{
				monitor.subTask("Closing process");
				Logger.debug("Requesting context to close procedure : " + instanceId, Level.PROC, this);
				closed = s_ctx.closeExecutor(instanceId);
			}
			if (!closed)
			{
				// The context command was sent but it raised an error
				monitor.setCanceled(true);
				monitor.done();
				throw new UnloadFailed("Could not unload the procedure " + instanceId);
			}
			monitor.worked(1);
		}
		catch (ContextError err)
		{
			Logger.error(err.getLocalizedMessage(), Level.PROC, this);
			// The procedure could not be unloaded due to an error in the
			// context
			// processing
			throw new UnloadFailed("Could not unload the procedure " + instanceId + ": " + err.getLocalizedMessage());
		}
	}

	/***************************************************************************
	 * Add a load monitor for a new procedure
	 * 
	 * @param instanceId
	 **************************************************************************/
	private void addLoadMonitor(String instanceId)
	{
		m_loadMonitors.put(instanceId, new ProcedureLoadMonitor(instanceId));
	}

	/***************************************************************************
	 * Remove a load monitor for a procedure
	 * 
	 * @param instanceId
	 **************************************************************************/
	private void removeLoadMonitor(String instanceId)
	{
		if (m_loadMonitors.containsKey(instanceId))
		{
			m_loadMonitors.remove(instanceId);
		}
	}

	/***************************************************************************
	 * Wait for the load monitor clearance
	 * 
	 * @param instanceId
	 **************************************************************************/
	private boolean waitLoaded(String instanceId)
	{
		return m_loadMonitors.get(instanceId).waitProcedureLoaded();
	}

	/***************************************************************************
	 * Notify a load monitor about a status change
	 * 
	 * @param instanceId
	 **************************************************************************/
	private void notifyLoadMonitor(String instanceId, ExecutorStatus status)
	{
		if (m_loadMonitors.containsKey(instanceId))
		{
			m_loadMonitors.get(instanceId).setProcedureStatus(status);
		}
	}

	@Override
    public void clearNotifications()
    {
	    // Nothing to do
    }
}
