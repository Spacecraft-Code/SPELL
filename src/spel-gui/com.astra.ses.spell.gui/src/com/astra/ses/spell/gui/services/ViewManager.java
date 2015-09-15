///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.services
// 
// FILE      : ViewManager.java
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
package com.astra.ses.spell.gui.services;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.astra.ses.spell.gui.core.interfaces.BaseService;
import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.UnloadType;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.exceptions.NoSuchViewException;
import com.astra.ses.spell.gui.extensions.GuiNotifications;
import com.astra.ses.spell.gui.interfaces.IProcedureView;
import com.astra.ses.spell.gui.interfaces.ProcedureViewCloseMode;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.model.commands.EditDictionary;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.model.jobs.CloseProcedureJob;
import com.astra.ses.spell.gui.model.jobs.KillProcedureJob;
import com.astra.ses.spell.gui.model.jobs.ReleaseProcedureJob;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.PropertyKey;
import com.astra.ses.spell.gui.procs.extensionpoints.IProcedureListener;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureModelListener;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.types.ExecutorStatus;
import com.astra.ses.spell.gui.views.MasterView;
import com.astra.ses.spell.gui.views.ProcedureView;
import com.astra.ses.spell.gui.views.TabbedView;

/*******************************************************************************
 * @brief This class mantains a registry of all relevant views of the GUI,
 *        including procedure views, control view and the navigation view.
 * @date 09/10/07
 ******************************************************************************/
public class ViewManager extends BaseService implements IViewManager, 
														IProcedureListener, 
														IProcedureModelListener, 
														IPartListener2, 
														IPropertyChangeListener
{
	public static final String ID = "com.astra.ses.spell.gui.ViewManager";

	/** Holds the list of registered views */
	private Map<String, ViewPart> m_viewList;
	/** Holds the list of registered procedure views */
	private Map<String, IProcedureView> m_procViewList;
	/** True if procedures should be automatically closed on finish */
	private String m_autoClose;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ViewManager()
	{
		super(ID);
		Logger.debug("Created", Level.INIT, this);
	}

	@Override
	public void setup()
	{
		IConfigurationManager cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		Logger.debug("Setting up", Level.INIT, this);
		m_viewList = new TreeMap<String, ViewPart>();
		m_procViewList = new TreeMap<String, IProcedureView>();
		m_autoClose = cfg.getProperty(PropertyKey.AUTOMATIC_CLOSE);
		GuiNotifications.get().addListener(this, IProcedureListener.class);
		GuiNotifications.get().addListener(this, IProcedureModelListener.class);
		cfg.addPropertyChangeListener(this);
	}

	@Override
	public void cleanup()
	{
		GuiNotifications.get().removeListener(this);
	}

	@Override
	public void subscribe()
	{
	}

	@Override
	public void registerView(String viewId, ViewPart view)
	{
		Logger.debug("Registering view: " + viewId, Level.PROC, this);
		m_viewList.put(viewId, view);
	}

	@Override
	public boolean isVisible(String viewId)
	{
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IViewPart partRef = window.getActivePage().findView(viewId);
		if (partRef == null)
			return false;
		return window.getActivePage().isPartVisible(partRef);
	}

	/***************************************************************************
	 * Register a procedure view part
	 * 
	 * @param viewId
	 *            View identifier
	 * @param view
	 *            View reference
	 **************************************************************************/
	private void registerProcView(String viewId, IProcedureView view)
	{
		Logger.debug("Registering proc view: " + viewId, Level.PROC, this);
		m_procViewList.put(viewId, view);
	}

	/***************************************************************************
	 * Unregister a view part
	 * 
	 * @param viewId
	 *            View identifier
	 **************************************************************************/
	private void unregisterProcView(String viewId)
	{
		Logger.debug("Unregistering proc view: " + viewId, Level.PROC, this);
		m_procViewList.remove(viewId);

		// Close all tabbed views (AsRun and Log views) associated with
		// this procedure
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		for (IViewReference viewReference : page.getViewReferences())
		{
			if (viewReference.getView(false) instanceof TabbedView)
			{
				TabbedView logOrAsRunView = (TabbedView) viewReference.getView(false);
				if (logOrAsRunView.getProcId().equals(viewId))
				{
					page.hideView(logOrAsRunView);
				}
			}
		}
	}

	@Override
	public ViewPart getView(String viewId) throws NoSuchViewException
	{
		if (!m_viewList.containsKey(viewId))
			throw new NoSuchViewException("Unknown view: " + viewId);
		return m_viewList.get(viewId);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IProcedureView getProcedureView(String viewId) throws NoSuchViewException
	{
		if (!m_procViewList.containsKey(viewId))
			throw new NoSuchViewException("Unknown view: " + viewId);
		return m_procViewList.get(viewId);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean containsProcedureView( String viewId )
	{
		return m_procViewList.containsKey(viewId);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void showProcedureView( String procId )
	{
		Logger.debug("Open procedure view: " + procId, Level.PROC, this);
		try
		{
			if (m_procViewList.containsKey(procId))
			{
				IWorkbenchWindow wbw = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				wbw.getActivePage().showView(ProcedureView.ID, procId, IWorkbenchPage.VIEW_ACTIVATE);
			}
			else
			{
				Logger.error("Could not show procedure view " + procId + ", not found", Level.PROC, this);
			}
		}
		catch (PartInitException e)
		{
			Logger.error("Could not show procedure view " + procId + ": " + e.getLocalizedMessage(), Level.PROC, this);
		}
	}

	/***************************************************************************
	 * Open a procedure view
	 * 
	 * @param procId
	 *            The view identifier
	 **************************************************************************/
	private void openProcedureView(IProcedure model)
	{
		String procId = model.getProcId();
		Logger.debug("Open procedure view: " + procId, Level.PROC, this);
		IWorkbenchWindow wbw = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		try
		{
			wbw.getActivePage().showView(ProcedureView.ID, procId, IWorkbenchPage.VIEW_ACTIVATE);
		}
		catch (PartInitException e)
		{
			Logger.error("Could not open procedure view " + procId + ": " + e.getLocalizedMessage(), Level.PROC, this);
		}
	}

	/***************************************************************************
	 * Close a procedure view
	 * 
	 * @param procId
	 *            The view identifier
	 **************************************************************************/
	private void closeProcedureView(String procId)
	{
		Logger.debug("Close procedure view: " + procId, Level.PROC, this);
		IWorkbenchWindow wbw = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = wbw.getActivePage();
		if (page != null)
		{
			IViewReference ref = page.findViewReference(ProcedureView.ID, procId);
			if (ref != null)
			{
				getProcedureView(procId).setCloseable(true);
				wbw.getActivePage().hideView(ref);
				unregisterProcView(procId);
			}
		}
	}

	@Override
	public void notifyProcedureModelDisabled(IProcedure model)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId))
			m_procViewList.get(instanceId).notifyModelDisabled();
	}

	@Override
	public void notifyProcedureModelEnabled(IProcedure model)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId))
			m_procViewList.get(instanceId).notifyModelEnabled();
	}

	@Override
	public void notifyProcedureModelLoaded(IProcedure model)
	{
		openProcedureView(model);
	}

	@Override
	public void notifyProcedureModelReset(IProcedure model)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId))
			m_procViewList.get(instanceId).notifyModelReset();
	}

	@Override
	public void notifyProcedureModelUnloaded(IProcedure model, UnloadType type)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId))
		{
			ViewPart vpart = (ViewPart) m_procViewList.get(instanceId);
			Shell shell = vpart.getSite().getShell();

			switch (type)
			{
			case CONTROL_STOLEN:
				MessageDialog.openWarning(shell, "Procedure stolen", "Control of procedure '" + instanceId
				        + "' has been stolen by another client");
				break;
			case CONTROLLED_CRASHED:
				MessageDialog.openWarning(shell, "Procedure crashed", "Procedure '" + instanceId
				        + "' has crashed. Please contact Software Engineering.");
				// Do not close the view and leave it open for the user to see
				return;
			case CONTROLLED_OTHER_KILLED:
				MessageDialog.openWarning(shell, "Procedure killed", "Procedure '" + instanceId
				        + "' has been killed by another client");
				break;
			case CONTROLLED_KILLED:
			case CONTROLLED_CLOSED:
			case CONTROLLED_RELEASED:
				// Nothing to report
				break;
			case MONITORED_CLOSED:
				MessageDialog.openWarning(shell, "Procedure closed", "Procedure '" + instanceId
				        + "' has been closed by the controlling user");
				break;
			case MONITORED_CRASHED:
				MessageDialog.openWarning(shell, "Procedure crashed", "Procedure '" + instanceId
				        + "' has crashed. Please contact controlling user.");
				// Do not close the view and leave it open for the user to see
				return;
			case MONITORED_KILLED:
				MessageDialog.openWarning(shell, "Procedure killed", "Procedure '" + instanceId
				        + "' has been killed by the controlling user.");
				break;
			case MONITORED_RELEASED:
			default:
				// Nothing to do
				break;
			}
			m_procViewList.get(instanceId).setCloseMode(ProcedureViewCloseMode.NONE);
			closeProcedureView(instanceId);
		}
	}

	@Override
	public void notifyProcedureModelConfigured(IProcedure model)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId))
		{
			m_procViewList.get(instanceId).notifyModelConfigured();
		}
	}

	@Override
	public void notifyProcedureDisplay(IProcedure model, DisplayData data)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId))
			m_procViewList.get(instanceId).notifyDisplay(data);
	}

	@Override
	public void notifyProcedureError(IProcedure model, ErrorData data)
	{
		if (m_procViewList.containsKey(data.getOrigin()))
			m_procViewList.get(data.getOrigin()).notifyError(data);
	}

	@Override
	public void notifyProcedureItem(IProcedure model, ItemNotification data)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId))
			m_procViewList.get(instanceId).notifyItem(data);
	}

	@Override
	public void notifyProcedureStack(IProcedure model, StackNotification data)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId))
			m_procViewList.get(instanceId).notifyStack(data);
	}

	@Override
	public void notifyProcedureStatus(IProcedure model, StatusNotification data)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId))
		{
			m_procViewList.get(instanceId).notifyStatus(data);
		}

		// Check wether the status is from a child of a shown proc
		// If the status is a finished status, show the parent
		ExecutorStatus st = data.getStatus();
		IProcedureManager mgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
		IContextProxy proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		String parentId = null;
		
		// If the (child) procedure is visible, that is, locally loaded
		if (mgr.isLocallyLoaded(instanceId))
		{
			IProcedure proc = mgr.getProcedure(instanceId);
			// If there is parent proc, go back to it and show it
			if (m_procViewList.containsKey(proc.getParent()))
			{
				parentId = proc.getParent();
			}
		}
		
		
		switch (st)
		{
		case FINISHED:
			// Auto close feature
			if (!m_autoClose.equals("NO") && mgr.isLocallyLoaded(instanceId))
			{
				IProcedure procedure = mgr.getProcedure(instanceId);
				String controllingKey = procedure.getRuntimeInformation().getControllingClient().getKey(); 
				if (m_autoClose.equals("ALL"))
				{
					// Are we controlling it? auto-close
					if (controllingKey.equals(proxy.getClientKey()))
					{
						mgr.closeProcedure(instanceId, new NullProgressMonitor());
					}
				}
				else // Children only
				{
					String parent = procedure.getParent();
					// We also need to be controllers in order to invoke close
					if (parent != null && !parent.trim().isEmpty() && (controllingKey.equals(proxy.getClientKey())))
					{
						mgr.closeProcedure(instanceId, new NullProgressMonitor());
					}
				}
			}
			// Fall trhu
		case ERROR:
		case ABORTED:
			// If the (child) procedure is visible, that is, locally loaded
			if (parentId != null)
			{
				IProcedure parentProc = mgr.getProcedure(parentId);
				openProcedureView(parentProc);
			}
		}
	}

	@Override
	public String getListenerId()
	{
		return ID;
	}

	@Override
	public void notifyProcedurePrompt(IProcedure model)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId))
			m_procViewList.get(instanceId).notifyPrompt();
	}

	@Override
	public void notifyProcedureFinishPrompt(IProcedure model)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId))
			m_procViewList.get(instanceId).notifyFinishPrompt();
	}

	@Override
	public void notifyProcedureCancelPrompt(IProcedure model)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId))
			m_procViewList.get(instanceId).notifyCancelPrompt();
	}

	@Override
	public void notifyProcedureUserAction(IProcedure model, UserActionNotification data)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId))
		{
			m_procViewList.get(instanceId).notifyUserAction(data);
		}
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef)
	{
		IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof IProcedureView)
		{
			IProcedureView view = (IProcedureView) part;
			view.updateDependentCommands();
		}
		CommandHelper.updateCommandEnabledState(EditDictionary.ID);
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef)
	{
		IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof IProcedureView)
		{
			IProcedureView view = (IProcedureView) part;
			ProcedureViewCloseMode mode = view.getCloseMode();
			CommandResult result = CommandResult.SUCCESS;
			if (mode == ProcedureViewCloseMode.DETACH)
			{
				ReleaseProcedureJob job = new ReleaseProcedureJob(view.getProcId(),false);
				CommandHelper.executeInProgress(job, false, false);
				if (job.result != CommandResult.SUCCESS)
				{
					MessageDialog.openError(part.getSite().getShell(), "Detach error", job.message);
				}
				result = job.result;
			}
			else if (mode == ProcedureViewCloseMode.BACKGROUND)
			{
				ReleaseProcedureJob job = new ReleaseProcedureJob(view.getProcId(),true);
				CommandHelper.executeInProgress(job, false, false);
				if (job.result != CommandResult.SUCCESS)
				{
					MessageDialog.openError(part.getSite().getShell(), "Detach error", job.message);
				}
				result = job.result;
			}
			else if (mode == ProcedureViewCloseMode.KILL)
			{
				KillProcedureJob job = new KillProcedureJob(view.getProcId());
				CommandHelper.executeInProgress(job, false, false);
				if (job.result != CommandResult.SUCCESS)
				{
					MessageDialog.openError(part.getSite().getShell(), "Kill error", job.message);
				}
				result = job.result;
			}
			else if (mode == ProcedureViewCloseMode.CLOSE)
			{
				CloseProcedureJob job = new CloseProcedureJob(view.getProcId());
				CommandHelper.executeInProgress(job, false, false);
				if (job.result != CommandResult.SUCCESS)
				{
					MessageDialog.openError(part.getSite().getShell(), "Close error", job.message);
				}
				result = job.result;
			}
			if (result == CommandResult.SUCCESS)
			{
				unregisterProcView(view.getProcId());
			}
		}
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef)
	{
		IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof IProcedureView)
		{
			IProcedureView view = (IProcedureView) part;
			Logger.debug("View " + view + " part open", Level.GUI, this);
			registerProcView(view.getProcId(), view);
			m_procViewList.get(view.getProcId()).notifyModelLoaded();
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef)
	{
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef)
	{
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef)
	{
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef)
	{
		IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof MasterView)
		{
			IProcedureManager mgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
			mgr.getOpenRemoteProcedures(true);
		}
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef)
	{
	}

	@Override
    public void propertyChange(PropertyChangeEvent event)
    {
		String property = event.getProperty();
		if (property.equals(PropertyKey.AUTOMATIC_CLOSE.getPreferenceName()))
		{
			m_autoClose = (String) event.getNewValue();
		}
    }

}
