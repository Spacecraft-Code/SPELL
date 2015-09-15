///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.master
// 
// FILE      : RecoveryComposite.java
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
package com.astra.ses.spell.gui.views.controls.master.recovery;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;

import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.server.ProcedureRecoveryInfo;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.model.jobs.DeleteRecoverFileJob;
import com.astra.ses.spell.gui.model.jobs.RecoverProcedureJob;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;

/**
 * @author Rafael Chinchilla
 *
 */
public class RecoveryComposite extends Composite implements SelectionListener

{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Button labels */
	private static final String	    BTN_RECOVER	= "Recover procedure";
	private static final String	    BTN_REFRESH	= "Refresh";
	private static final String	    BTN_DELETE	= "Delete files";
	/** Procedure manager handle */
	private static IProcedureManager	s_procMgr	        = null;
	/** Procedure manager handle */
	private static IContextProxy	    s_proxy	            = null;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	public static final String	    ID	                = "com.astra.ses.spell.gui.dialogs.RecoveryDialog";

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the table of contexts */
	private RecoveryTable	m_recoveryTable;
	/** Holds the recover button */
	private Button	        m_btnRecover;
	/** Holds the refresh button */
	private Button	        m_btnRefresh;
	/** Holds the delete files button */
	private Button	        m_btnDelete;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	/***************************************************************************
	 * 
	 **************************************************************************/
	public RecoveryComposite( Composite parent, int style )
	{
		super(parent,style);
		if (s_procMgr == null)
		{
			s_procMgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
		}
		if (s_proxy == null)
		{
			s_proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		}
		createContents();
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
	 * Button callback
	 **************************************************************************/
	@Override
	public void widgetSelected(SelectionEvent e)
	{
		if (e.widget instanceof Button)
		{
			ProcedureRecoveryInfo[] procs = m_recoveryTable.getSelectedProcedures();

			if (e.widget == m_btnRecover)
			{
				if ((procs == null) || (procs.length == 0)) return;
				doRecoverProcedure(procs);
			}
			else if (e.widget == m_btnRefresh)
			{
				doRefreshFiles();
				m_recoveryTable.getTable().deselectAll();
				updateButtons();
			}
			else if (e.widget == m_btnDelete)
			{
				doDeleteFiles( procs );
				doRefreshFiles();
				m_recoveryTable.getTable().deselectAll();
				updateButtons();
			}
		}
		else if (e.widget instanceof Table)
		{
			updateButtons();
		}
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

		m_recoveryTable = new RecoveryTable(this);
		GridData tableLayoutData = new GridData(GridData.FILL_BOTH);
		tableLayoutData.grabExcessHorizontalSpace = true;
		tableLayoutData.widthHint = 700;
		tableLayoutData.heightHint = 200;
		m_recoveryTable.getTable().setLayoutData(tableLayoutData);

		Composite buttonBar = new Composite(this, SWT.BORDER);
		buttonBar.setLayout(new FillLayout(SWT.HORIZONTAL));
		buttonBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		m_btnRecover = new Button(buttonBar, SWT.PUSH);
		m_btnRecover.setText(BTN_RECOVER);
		m_btnRecover.addSelectionListener(this);

		m_btnRefresh = new Button(buttonBar, SWT.PUSH);
		m_btnRefresh.setText(BTN_REFRESH);
		m_btnRefresh.addSelectionListener(this);

		m_btnDelete = new Button(buttonBar, SWT.PUSH);
		m_btnDelete.setText(BTN_DELETE);
		m_btnDelete.addSelectionListener(this);

		updateFiles();
	}

	/***************************************************************************
	 * Update the file table
	 **************************************************************************/
	private void updateFiles()
	{
		Logger.debug("Updating executors table", Level.GUI, this);
		m_recoveryTable.refresh();
		updateButtons();
	}

	/***************************************************************************
	 * Update button bar
	 **************************************************************************/
	private void updateButtons()
	{
		ProcedureRecoveryInfo[] procs = m_recoveryTable.getSelectedProcedures();
		m_btnRecover.setEnabled((procs.length > 0));
		m_btnDelete.setEnabled((procs.length > 0));
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private boolean doRecoverProcedure( ProcedureRecoveryInfo[] procs)
	{
		String message = "Do you really want to recover the procedure(s):\n";
		for(ProcedureRecoveryInfo proc : procs)
		{
			message += "    - " + proc.getName() + "\n";
		}
		if (MessageDialog.openConfirm( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Recover", message))
		{
			// Ensure that all are uncontrolled.
			for (ProcedureRecoveryInfo proc : procs)
			{
				Logger.debug("Recovering procedure " + proc.getOriginalInstanceId(), Level.PROC, this);
				RecoverProcedureJob job = new RecoverProcedureJob(proc);
				CommandHelper.executeInProgress(job, true, true);
				if (job.result == CommandResult.FAILED)
				{
					MessageDialog.openError(getShell(), "Recover error", job.message);
				}
				else if (job.result == CommandResult.CANCELLED)
				{
					break;
				}
			}
			return true;
		}
		return false;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private boolean doRefreshFiles()
	{
		m_recoveryTable.setInput(s_proxy);
		m_recoveryTable.refresh();
		return true;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private boolean doDeleteFiles( ProcedureRecoveryInfo[] procs )
	{
		String message = "Do you really want to delete the recovery files for the procedure(s):\n";
		for(ProcedureRecoveryInfo proc : procs)
		{
			message += "    - " + proc.getName() + "\n";
		}
		if (MessageDialog.openConfirm( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Delete files", message))
		{
			// Ensure that all are uncontrolled.
			Logger.debug("Deleting recovery file(s)", Level.PROC, this);
			DeleteRecoverFileJob job = new DeleteRecoverFileJob(procs);
			CommandHelper.executeInProgress(job, true, true);
			if (job.result == CommandResult.FAILED)
			{
				MessageDialog.openError(getShell(), "Delete error", job.message);
			}
			else if (job.result == CommandResult.CANCELLED)
			{
				return false;
			}
			return true;
		}
		return false;
	}
}
