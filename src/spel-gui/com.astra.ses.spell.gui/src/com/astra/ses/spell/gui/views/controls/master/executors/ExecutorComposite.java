///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.master
// 
// FILE      : ExecutorComposite.java
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
package com.astra.ses.spell.gui.views.controls.master.executors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.IExecutorInfo;
import com.astra.ses.spell.gui.core.interfaces.IProcedureClient;
import com.astra.ses.spell.gui.core.interfaces.IServerProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreProcedureOperationListener;
import com.astra.ses.spell.gui.core.model.server.ServerInfo.ServerRole;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ExecutorOperationSummary;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.dialogs.AttachModeDialog;
import com.astra.ses.spell.gui.extensions.GuiNotifications;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.model.jobs.CloseProcedureJob;
import com.astra.ses.spell.gui.model.jobs.ControlRemoteProcedureJob;
import com.astra.ses.spell.gui.model.jobs.KillProcedureJob;
import com.astra.ses.spell.gui.model.jobs.MonitorRemoteProcedureJob;
import com.astra.ses.spell.gui.model.jobs.ReleaseProcedureJob;
import com.astra.ses.spell.gui.model.jobs.RemoveProcedureControlJob;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.preferences.keys.PropertyKey;
import com.astra.ses.spell.gui.preferences.values.YesNoPromptPref;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformation;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.types.ExecutorStatus;
import com.astra.ses.spell.gui.views.MasterView;

/**
 * @author Rafael Chinchilla
 * 
 */
public class ExecutorComposite extends Composite implements ICoreProcedureOperationListener, SelectionListener,
        IDoubleClickListener

{

	/** Button labels */
	private static final String	    BTN_TAKE_CONTROL	= "Take control";
	private static final String	    BTN_RELEASE_CONTROL	= "Release control";
	private static final String	    BTN_BACKGROUND      = "To background";
	private static final String	    BTN_START_MONITOR	= "Start monitor";
	private static final String	    BTN_STOP_MONITOR	= "Stop monitor";
	private static final String	    BTN_STOP_EXECUTOR	= "Stop execution";
	private static final String	    BTN_KILL_EXECUTOR	= "Kill execution";
	private static final String	    BTN_REFRESH         = "Refresh";
	/** Procedure manager handle */
	private static IProcedureManager	s_procMgr	        = null;
	/** Procedure manager handle */
	private static IContextProxy	    s_proxy	            = null;
	/** Configuration manager */
	private static IConfigurationManager s_cfg              = null;

	public static final String	    ID	                = "com.astra.ses.spell.gui.dialogs.ExecutorsDialog";

	/** Holds the table of contexts */
	private ExecutorsTable	m_executorsTable;
	/** Holds the take control button */
	private Button	                m_btnTakeControl;
	/** Holds the release control button */
	private Button	                m_btnReleaseControl;
	/** Holds the background button */
	private Button	                m_btnBackground;
	/** Holds the start monitor button */
	private Button	                m_btnStartMonitor;
	/** Holds the stop monitor button */
	private Button	                m_btnStopMonitor;
	/** Holds the stop executor button */
	private Button	                m_btnStopExecutor;
	/** Holds the kill executor button */
	private Button	                m_btnKillExecutor;
	/** Holds the refresh button */
	private Button	                m_btnRefresh;
	/** True if background executor warning dialog is open */
	private AtomicBoolean			m_bkgWarningOpen;
	 


	/***************************************************************************
	 * 
	 **************************************************************************/
	public ExecutorComposite(Composite parent, int style)
	{
		super(parent, style);
		
		m_bkgWarningOpen = new AtomicBoolean(false);
		
		GuiNotifications.get().addListener(this, ICoreProcedureOperationListener.class);
		
		if (s_procMgr == null)
		{
			s_procMgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
		}
		if (s_proxy == null)
		{
			s_proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		}
		if (s_cfg == null)
		{
			s_cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		}
		
		// Ensure list of remote open procedures is up to date
		if (s_procMgr.canOperate())
		{
			s_procMgr.getOpenRemoteProcedures(true);
		}
		createContents();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void dispose()
	{
		GuiNotifications.get().removeListener(this);
		super.dispose();
	}

	/***************************************************************************
	 * Button callback
	 **************************************************************************/
	@Override
	public void widgetDefaultSelected(SelectionEvent e)
	{
		widgetSelected(e);
	}
	
	/***************************************************************************
	 * Set focus
	 **************************************************************************/
	public boolean setFocus()
	{
		m_executorsTable.refresh();
		return super.setFocus();
	}

	/***************************************************************************
	 * Button callback
	 **************************************************************************/
	@Override
	public void widgetSelected(SelectionEvent e)
	{
		if (e.widget instanceof Button)
		{
			Button button = (Button) e.widget;

			Logger.debug("Button pressed: " + button.getText(), Level.GUI, this);
			
			String[] procIds = m_executorsTable.getSelectedProcedures();
			
			System.out.println("Procedures Ids: " + procIds);
			
			disableButtons();

			if ( button == m_btnTakeControl )
			{
				// For later use
				YesNoPromptPref ynp = YesNoPromptPref.valueOf(s_cfg.getProperty(PropertyKey.ASRUN_CONTROL));
				// Find out if we should use ASRUN
				boolean useAsRun = true;
				switch(ynp)
				{
				case NO:
					useAsRun = false;
					break;
				case PROMPT:
					AttachModeDialog dialog = new AttachModeDialog( getShell() );
					if (dialog.open()==IDialogConstants.OK_ID)
					{
						useAsRun = dialog.useAsRun();
					}
					else // Cancel whole operation
					{
						return;
					}
					break;
				case YES:
					useAsRun = true;
				}
				doTakeControl(procIds, useAsRun);
			}
			else if ( button == m_btnReleaseControl )
			{
				doReleaseControl(procIds,false);
			}
			else if ( button == m_btnBackground )
			{
				doReleaseControl(procIds,true);
			}
			else if ( button == m_btnStartMonitor )
			{
				// For later use
				YesNoPromptPref ynp = YesNoPromptPref.valueOf(s_cfg.getProperty(PropertyKey.ASRUN_MONITOR));
				// Find out if we should use ASRUN
				boolean useAsRun = true;
				switch(ynp)
				{
				case NO:
					useAsRun = false;
					break;
				case PROMPT:
					// Check for the procedure STATUS first
					// If Procedure Status is Prompt download the whole AsRun WITHOUT asking the user.
					
					boolean bPromptStatus = false; // initialize promptStatus boolean
					
					for (String procId : procIds) //get procedure status
					{
						System.out.println("Procedure Id: " + procId);
						IProcedure proc = s_procMgr.getRemoteProcedure(procId);
						IExecutionInformation info = proc.getRuntimeInformation();
						bPromptStatus = bPromptStatus | info.getStatus().equals(ExecutorStatus.PROMPT);
					} //for procIds
					
					if(bPromptStatus) //prompt status 
					{ 
						useAsRun=true;
					}
					else //no prompt status, ask the user. 
					{
						AttachModeDialog dialog = new AttachModeDialog( getShell() );
						if (dialog.open()==IDialogConstants.OK_ID)
						{
							useAsRun = dialog.useAsRun();
						}
						// Cancel whole operation 
						else
						{
							return;
						}
						break;
					} //else no PromptStatus, ask the user.
				case YES:
					useAsRun = true;
				}
				doStartMonitor(procIds,useAsRun);
			}
			else if ( button == m_btnStopMonitor )
			{
				doStopMonitor(procIds);
			}
			else if ( button == m_btnStopExecutor )
			{
				doStopExecutor(procIds);
			}
			else if ( button == m_btnKillExecutor )
			{
				doKillExecutor(procIds);
			}
			else  if ( button == m_btnRefresh )
			{
				doRefreshProcedures();
			}
			// Refresh button falls thru here
			refresh();
		}
	}

	/***************************************************************************
	 * ID as context service listener
	 **************************************************************************/
	@Override
	public String getListenerId()
	{
		return ID;
	}

	@Override
	public void notifyRemoteProcedureClosed(String procId, String guiKey)
	{
		Logger.debug("Procedure closed: " + procId, Level.GUI, this);
		refresh();
	}

	@Override
	public void notifyRemoteProcedureControlled(String procId, String guiKey)
	{
		if (!s_proxy.getClientKey().equals(guiKey))
		{
			Logger.debug("Procedure controlled: " + procId, Level.GUI, this);
			refresh();
		}
	}

	@Override
	public void notifyRemoteProcedureKilled(String procId, String guiKey)
	{
		Logger.debug("Procedure killed: " + procId, Level.GUI, this);
		refresh();
	}

	@Override
	public void notifyRemoteProcedureCrashed(String procId, String guiKey)
	{
		Logger.debug("Procedure crashed: " + procId, Level.GUI, this);
		refresh();
	}

	@Override
	public void notifyRemoteProcedureMonitored(String procId, String guiKey)
	{
		if (!s_proxy.getClientKey().equals(guiKey))
		{
			Logger.debug("Procedure monitored: " + procId, Level.GUI, this);
			refresh();
		}
	}

	@Override
	public void notifyRemoteProcedureOpen(String procId, String guiKey)
	{
		Logger.debug("Procedure open: " + procId, Level.GUI, this);
		refresh();
		// If the client that open the procedure is not us
		if (!guiKey.equals(s_proxy.getClientKey()))
		{
			// And we have procedures locally open
			if (!s_procMgr.getOpenLocalProcedures().isEmpty())
			{
				// And the MasterView is not in the foreground
				IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
				if (!(part instanceof MasterView))
				{
					if (!m_bkgWarningOpen.get())
					{
						m_bkgWarningOpen.set(true);
						MessageDialog.openWarning( getShell(), "Procedure Open", 
						"Another procedure has been open in the current context.\n\n" +
				        "It could interfere with the operations being performed\n" +
				        "by your procedures.\n\n" +
						"It should be recommended to check the Master View for details.");
						m_bkgWarningOpen.set(false);
					}
				}
			}
		}
	}

	@Override
	public void notifyRemoteProcedureReleased(String procId, String guiKey)
	{
		Logger.debug("Procedure released: " + procId, Level.GUI, this);
		refresh();
	}

	@Override
	public void notifyRemoteProcedureSummary(String procId, ExecutorOperationSummary summary, String guiKey)
	{
		Logger.debug("Procedure summary: " + summary.stageTitle + ": " + procId, Level.GUI, this);
		refresh();
	}

	@Override
	public void notifyRemoteProcedureStatus(String procId, ExecutorStatus status, String guiKey)
	{
		Logger.debug("Procedure status " + status + ": " + procId, Level.GUI, this);
		refresh();
	}

	/***************************************************************************
	 * Create the executor information group
	 **************************************************************************/
	private void createContents()
	{
		GridLayout clayout = new GridLayout();
		clayout.marginHeight = 2;
		clayout.marginWidth = 2;
		clayout.marginTop = 2;
		clayout.marginBottom = 2;
		clayout.marginLeft = 2;
		clayout.marginRight = 2;
		clayout.numColumns = 1;
		setLayout(clayout);
		
		
		m_executorsTable = new ExecutorsTable(this);
		GridData tableLayoutData = new GridData(GridData.FILL_BOTH);
		tableLayoutData.grabExcessHorizontalSpace = true;
		tableLayoutData.widthHint = 700;
		tableLayoutData.heightHint = 200;
		m_executorsTable.getGrid().setLayoutData(tableLayoutData);
		m_executorsTable.addDoubleClickListener(this);
		m_executorsTable.addSelectionChangedListener( new ISelectionChangedListener(){

			@Override
            public void selectionChanged(SelectionChangedEvent arg0)
            {
				updateButtons();
            }
			
		});

		Composite buttonBar = new Composite(this, SWT.BORDER);
		buttonBar.setLayout(new FillLayout(SWT.HORIZONTAL));
		buttonBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		m_btnTakeControl = new Button(buttonBar, SWT.PUSH);
		m_btnTakeControl.setText(BTN_TAKE_CONTROL);
		m_btnTakeControl.addSelectionListener(this);
		
		m_btnReleaseControl = new Button(buttonBar, SWT.PUSH);
		m_btnReleaseControl.setText(BTN_RELEASE_CONTROL);
		m_btnReleaseControl.addSelectionListener(this);
		
		m_btnBackground = new Button(buttonBar, SWT.PUSH);
		m_btnBackground.setText(BTN_BACKGROUND);
		m_btnBackground.addSelectionListener(this);

		m_btnStartMonitor = new Button(buttonBar, SWT.PUSH);
		m_btnStartMonitor.setText(BTN_START_MONITOR);
		m_btnStartMonitor.addSelectionListener(this);
		
		m_btnStopMonitor = new Button(buttonBar, SWT.PUSH);
		m_btnStopMonitor.setText(BTN_STOP_MONITOR);
		m_btnStopMonitor.addSelectionListener(this);
		
		m_btnStopExecutor = new Button(buttonBar, SWT.PUSH);
		m_btnStopExecutor.setText(BTN_STOP_EXECUTOR);
		m_btnStopExecutor.addSelectionListener(this);

		m_btnKillExecutor = new Button(buttonBar, SWT.PUSH);
		m_btnKillExecutor.setText(BTN_KILL_EXECUTOR);
		m_btnKillExecutor.addSelectionListener(this);
		
		m_btnRefresh = new Button(buttonBar, SWT.PUSH);
		m_btnRefresh.setText(BTN_REFRESH);
		m_btnRefresh.addSelectionListener(this);
		
		applyFonts();
		disableButtons();
	}

	/***************************************************************************
	 * Apply fonts to the controls
	 **************************************************************************/
	public void applyFonts()
	{
		Font font = s_cfg.getFont( FontKey.GUI_NOM );
		m_executorsTable.applyFonts();
		m_btnTakeControl.setFont( font );
		m_btnReleaseControl.setFont( font );
		m_btnBackground.setFont( font );
		m_btnStartMonitor.setFont( font );
		m_btnStopMonitor.setFont( font );
		m_btnKillExecutor.setFont( font );
		m_btnStopExecutor.setFont( font );
		m_btnRefresh.setFont( font );
	}
	
	/***************************************************************************
	 * Update the executors table
	 **************************************************************************/
	public void refresh()
	{
		Logger.debug("Updating executors table", Level.GUI, this);
		m_executorsTable.refreshAll();
		updateButtons();
	}

	/***************************************************************************
	 * Disable button bar
	 **************************************************************************/
	private void disableButtons()
	{
		m_btnTakeControl.setEnabled(false);
		m_btnReleaseControl.setEnabled(false);
		m_btnBackground.setEnabled(false);
		m_btnStartMonitor.setEnabled(false);
		m_btnStopMonitor.setEnabled(false);
		m_btnStopExecutor.setEnabled(false);
		m_btnKillExecutor.setEnabled(false);
	}

	/***************************************************************************
	 * Update button bar
	 **************************************************************************/
	private void updateButtons()
	{
		if (m_btnRefresh.isDisposed()) return;
		m_btnRefresh.setEnabled(s_proxy.isConnected());
		String[] procIds = m_executorsTable.getSelectedProcedures();
		if (procIds.length == 0)
		{
			disableButtons();
		}
		else
		{
			/*
			 * Activation rules:
			 * 
			 * 1.Take control: all selected procedures shall be remote and
			 * uncontrolled, OR local, monitored by me and uncontrolled
			 * 2.Release control: all selected procedures shall be local and
			 * controlled by me. Also, NOT waiting input. 3.Start monitor: all
			 * selected procedures shall be remote 4.Stop monitor: all selected
			 * procedures shall be local and monitored by me 5.Stop execution:
			 * all selected procedures shall be local and controlled by me
			 * 6.Kill execution: all selected procedures shall be uncontrolled
			 * or controlled by me
			 */
			boolean canTakeControl = canTakeControl(procIds);
			boolean canReleaseControl = canReleaseControl(procIds);
			boolean canPutInBackground = canPutInBackground(procIds);
			boolean canStartMonitor = canStartMonitor(procIds);
			boolean canStopMonitor = canStopMonitor(procIds);
			boolean canStopExecution = canStopExecution(procIds);

			m_btnTakeControl.setEnabled(canTakeControl);
			m_btnReleaseControl.setEnabled(canReleaseControl);
			m_btnBackground.setEnabled(canPutInBackground);
			m_btnStartMonitor.setEnabled(canStartMonitor);
			m_btnStopMonitor.setEnabled(canStopMonitor);
			m_btnStopExecutor.setEnabled(canStopExecution);
			m_btnKillExecutor.setEnabled(canStopExecution);
		}
	}

	/***************************************************************************
	 * Check if Take Control command can be used for all selected procs
	 **************************************************************************/
	private boolean canTakeControl(String[] procIds)
	{
		IServerProxy proxy = (IServerProxy) ServiceManager.get(IServerProxy.class);
		if (proxy.getCurrentServer().getRole().equals(ServerRole.MONITORING)) return false;
		// Ensure that all are uncontrolled.
		for (String procId : procIds)
		{
			if (s_procMgr.isLocallyLoaded(procId))
			{
				IProcedure proc = s_procMgr.getProcedure(procId);
				// If local and already controlled by me
				IExecutionInformation info = proc.getRuntimeInformation();
				IProcedureClient client = info.getControllingClient();
				if (client != null)
				{
					String cClient = client.getKey();
					if (cClient.equals(s_proxy.getClientKey())) { return false; }
				}

				// If local and being monitored by me
				IProcedureClient[] mClients = info.getMonitoringClients();
				if (mClients != null)
				for (IProcedureClient mclient : mClients)
				{
					if (mclient.getKey().equals(s_proxy.getClientKey())) { return false; }
				}
			}
			// When it is remote or it is not already controlled by my gui
			// allow to take control 
		}
		return true;
	}

	/***************************************************************************
	 * Check if Release Control command can be used for all selected procs
	 **************************************************************************/
	private boolean canReleaseControl(String[] procIds)
	{
		// Ensure that all are controlled by me. They shall be local for that.
		for (String procId : procIds)
		{
			if (!s_procMgr.isLocallyLoaded(procId)) return false;
			IProcedure proc = s_procMgr.getProcedure(procId);
			IExecutionInformation info = proc.getRuntimeInformation();
			ClientMode mode = info.getClientMode();
			boolean onPrompt = info.isWaitingInput();
			boolean okToRelease = (mode == ClientMode.CONTROL) && (!onPrompt);
			if (!okToRelease) return false;
		}
		return true;
	}

	/***************************************************************************
	 * Check if put in background command can be used for all selected procs
	 **************************************************************************/
	private boolean canPutInBackground(String[] procIds)
	{
		// Ensure that all are controlled by me. They shall be local for that.
		for (String procId : procIds)
		{
			if (!s_procMgr.isLocallyLoaded(procId)) return false;
			IProcedure proc = s_procMgr.getProcedure(procId);
			IExecutionInformation info = proc.getRuntimeInformation();
			ClientMode mode = info.getClientMode();
			boolean isPaused = info.getStatus().equals(ExecutorStatus.PAUSED);
			boolean isWaiting = info.getStatus().equals(ExecutorStatus.WAITING);
			boolean isRunning = info.getStatus().equals(ExecutorStatus.RUNNING);
			boolean okToRelease = (mode == ClientMode.CONTROL) && (isPaused || isWaiting || isRunning);
			if (!okToRelease) return false;
		}
		return true;
	}

	/***************************************************************************
	 * Check if Start Monitor command can be used for all selected procs
	 **************************************************************************/
	private boolean canStartMonitor(String[] procIds)
	{
		// Ensure that all are uncontrolled by me.
		for (String procId : procIds)
		{
			// It cannot be local
			if (s_procMgr.isLocallyLoaded(procId)) return false;
			IProcedure proc = s_procMgr.getRemoteProcedure(procId);
			//TODO set this in preferences
			if (proc.getRuntimeInformation().getMonitoringClients() != null)
			{
				if (proc.getRuntimeInformation().getMonitoringClients().length >= 5)
				{
					return false;
				}
			}
		}
		return true;
	}

	/***************************************************************************
	 * Check if Stop Monitor command can be used for all selected procs
	 **************************************************************************/
	private boolean canStopMonitor(String[] procIds)
	{
		// Ensure that all are uncontrolled by me.
		for (String procId : procIds)
		{
			// It cannot be remote
			if (!s_procMgr.isLocallyLoaded(procId)) return false;
			IProcedure proc = s_procMgr.getProcedure(procId);
			if (proc.getRuntimeInformation().getClientMode() != ClientMode.MONITOR) return false;
		}
		return true;
	}

	/***************************************************************************
	 * Check if Stop Execution command can be used for all selected procs
	 **************************************************************************/
	private boolean canStopExecution(String[] procIds)
	{
		// Ensure that all are uncontrolled by me.
		for (String procId : procIds)
		{
			if (s_procMgr.isLocallyLoaded(procId))
			{
				IProcedure proc = s_procMgr.getProcedure(procId);
				if (proc.getRuntimeInformation().getClientMode() != ClientMode.CONTROL) return false;
			}
			else
			{
				// We can stop/kill it if nobody is controlling or it is remote but assigned to me
				IProcedure proc = s_procMgr.getRemoteProcedure(procId);
				if (proc.getRuntimeInformation().getControllingClient() != null)
				{
					String myKey = s_proxy.getClientKey();
					if (proc.getRuntimeInformation().getControllingClient().getKey().equals(myKey))
					{
						return true;
					}
					return false;
				}
			}
		}
		return true;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void doTakeControl(String[] procIds, boolean useAsRun)
	{
		List<String> toControl = processMultipleAttach(procIds, "control");
		for (String procId : toControl)
		{
			// If we want to steal control
			IProcedure proc = s_procMgr.getRemoteProcedure(procId);
			IExecutionInformation info = proc.getRuntimeInformation();
			IProcedureClient cClient = info.getControllingClient();
			
			boolean attachControl = false;
			
			if (cClient != null)
			{
				boolean proceed = MessageDialog.openConfirm(getShell(), "Steal Procedure Control", 
						"The procedure is begin controlled already. Do you want to steal the control?");
				if (proceed)
				{
					RemoveProcedureControlJob job = new RemoveProcedureControlJob(procId);
					CommandHelper.executeInProgress(job, true, true);
					if (job.result.equals(CommandResult.CANCELLED)) continue;
					attachControl = true;
				}
			}
			else
			{
				attachControl = true;
			}
			
			if (attachControl)
			{
				Logger.debug("Taking control of " + procId, Level.PROC, this);
				ControlRemoteProcedureJob job = new ControlRemoteProcedureJob(procId,useAsRun);
				CommandHelper.executeInProgress(job, true, true);
				if (job.result == CommandResult.FAILED)
				{
					MessageDialog.openError(getShell(), "Attach error", job.message);
				}
				else if (job.result == CommandResult.CANCELLED)
				{
					break;
				}
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void doReleaseControl(String[] procIds, boolean background)
	{
		for (String procId : procIds)
		{
			if (background)
			{
				Logger.debug("Putting in background: " + procId, Level.PROC, this);
			}
			else
			{
				Logger.debug("Releasing control of " + procId, Level.PROC, this);
			}
			if (background)
			{
				if (!MessageDialog.openConfirm(getShell(), "Background mode",
			
			        "Are you sure to put this procedure in background?\n\n"+
			        "Warning: once in background mode, the procedure will continue running without controlling client!")
				)
				{
					return;
				}
			}
			ReleaseProcedureJob job = new ReleaseProcedureJob(procId,background);
			CommandHelper.executeInProgress(job, true, true);
			if (job.result == CommandResult.FAILED)
			{
				MessageDialog.openError(getShell(), "Detach error", job.message);
			}
			else if (job.result == CommandResult.CANCELLED)
			{
				break;
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private boolean hasRelatedProcedures( String instanceId )
	{
		ExecutorsModel model = (ExecutorsModel) m_executorsTable.getInput();
		for(IProcedure proc : model.getElements())
		{
			// Looking at this model
			if (instanceId.equals(proc.getProcId()))
			{
				// If this model has a parent
				if (proc.getParent() != null && !proc.getParent().trim().isEmpty()) return true;
			}
			// Looking at other models
			else
			{
				// If this model is parent of this other
				if (instanceId.equals(proc.getParent())) return true;
			}
		}
		return false;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private List<String> getRelatedProcedures( String instanceId )
	{
		List<String> relatives = new ArrayList<String>();
		ExecutorsModel model = (ExecutorsModel) m_executorsTable.getInput();
		for(IProcedure proc : model.getElements())
		{
			// Looking at this model
			if (instanceId.equals(proc.getProcId()))
			{
				// If this model has a parent
				if (proc.getParent() != null && !proc.getParent().trim().isEmpty()) 
				{
					relatives.add(proc.getParent());
				}
			}
			// Looking at other models
			else
			{
				// If this model is parent of this other
				if (instanceId.equals(proc.getParent())) 
				{
					relatives.add(proc.getProcId());
				}
			}
		}
		return relatives;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private List<String> processMultipleAttach( String[] procIds, String what )
	{
		List<String> procedures = new ArrayList<String>();
		procedures.addAll(Arrays.asList( procIds ));
		for (String procId : procIds)
		{
			String ma = s_cfg.getProperty(PropertyKey.MULTIPLE_ATTACH);
			YesNoPromptPref ynp = YesNoPromptPref.valueOf(ma);
			switch(ynp)
			{
			case NO:
				break;
			case PROMPT:
				if (hasRelatedProcedures(procId))
				{
					if (MessageDialog.openQuestion(getShell(), "Multiple attach", "The procedure " + procId + " is related to other procedures running. Do you want to " + what + " them as well?"))
					{
						procedures.addAll(getRelatedProcedures(procId)); 
					}
				}
				break;
			case YES:
				if (hasRelatedProcedures(procId))
				{
					procedures.addAll(getRelatedProcedures(procId)); 
				}
				break;
			}
		}
		return procedures;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private void doStartMonitor(String[] procIds, boolean downloadData)
	{
		List<String> toMonitor = processMultipleAttach(procIds, "monitor");
		for(String procId : toMonitor)
		{
			Logger.debug("Start monitor of " + procId, Level.PROC, this);
			MonitorRemoteProcedureJob job = new MonitorRemoteProcedureJob(procId, downloadData);
			CommandHelper.executeInProgress(job, true, true);
			if (job.result == CommandResult.FAILED)
			{
				MessageDialog.openError(getShell(), "Attach error", job.message);
			}
			else if (job.result == CommandResult.CANCELLED)
			{
				break;
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void doStopMonitor(String[] procIds)
	{
		for (String procId : procIds)
		{
			Logger.debug("Stop monitoring " + procId, Level.PROC, this);
			ReleaseProcedureJob job = new ReleaseProcedureJob(procId,false);
			CommandHelper.executeInProgress(job, true, true);
			if (job.result == CommandResult.FAILED)
			{
				MessageDialog.openError(getShell(), "Detach error", job.message);
			}
			else if (job.result == CommandResult.CANCELLED)
			{
				break;
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void doStopExecutor(String[] procIds)
	{
		for (String procId : procIds)
		{
			Logger.debug("Closing procedure " + procId, Level.PROC, this);
			CloseProcedureJob job = new CloseProcedureJob(procId);
			CommandHelper.executeInProgress(job, true, true);
			if (job.result == CommandResult.FAILED)
			{
				MessageDialog.openError(getShell(), "Close error", job.message);
			}
			else if (job.result == CommandResult.CANCELLED)
			{
				break;
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void doKillExecutor(String[] procIds)
	{
		for (String procId : procIds)
		{
			Logger.debug("Killing procedure " + procId, Level.PROC, this);
			KillProcedureJob job = new KillProcedureJob(procId);
			CommandHelper.executeInProgress(job, true, true);
			if (job.result == CommandResult.FAILED)
			{
				MessageDialog.openError(getShell(), "Kill error", job.message);
			}
			else if (job.result == CommandResult.CANCELLED)
			{
				break;
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void doRefreshProcedures()
	{
		Logger.debug("Refreshing procedures", Level.PROC, this);
		s_procMgr.getOpenRemoteProcedures(true);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void doubleClick(DoubleClickEvent event)
	{
		Object sel = ((IStructuredSelection) event.getSelection()).getFirstElement();
		if (sel instanceof IProcedure)
		{

		}
		else if (sel instanceof IExecutorInfo)
		{

		}
	}
}
