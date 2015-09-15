///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.replay.views.controls
// 
// FILE      : ReplayProcedureControlPanel.java
//
// DATE      : Jun 19, 2013
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
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.replay.views.controls;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.interfaces.IProcedureControlPanel;
import com.astra.ses.spell.gui.interfaces.IProcedureView;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.types.ExecutorCommand;
import com.astra.ses.spell.gui.types.ExecutorStatus;

/*******************************************************************************
 ******************************************************************************/
public class ReplayProcedureControlPanel extends Composite implements SelectionListener, IProcedureControlPanel
{
	private static final String	         EXEC_CMD	 = "execCommand";
	private static final String	         CMD	     = "CommandId";
	private static IConfigurationManager	s_cfg	 = null;

	/** Handle for procedure view */
	private IProcedureView	             m_view;
	/** Handle for procedure model */
	private IProcedure	                 m_model;
	/** Holds the current proc status */
	private ExecutorStatus	             m_currentStatus;
	/** Holds the status display */
	private Label	                     m_statusText;
	/** Holds the client mode */
	private ClientMode	                 m_clientMode;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param view
	 *            Containing view
	 * @param parent
	 *            Parent composite
	 * @style SWT style
	 **************************************************************************/
	public ReplayProcedureControlPanel(IProcedureView view, IProcedure model, Composite parent, int style)
	{
		super(parent, style);

		if (s_cfg == null)
		{
			s_cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		}

		m_view = view;
		// Store the procedure model handle
		m_model = model;
		m_clientMode = null;

		// Use gridlayout for placing the buttons
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 1;
		layout.numColumns = 1;
		setLayout(layout);

		// Control buttons: start, end, forward one line, backward one line
		m_statusText = new Label(this, SWT.BORDER);
		m_statusText.setText("    UNINIT    ");
		m_statusText.setFont(s_cfg.getFont(FontKey.HEADER));
		m_statusText.setAlignment(SWT.CENTER);
		GridData ldata = new GridData(GridData.FILL_HORIZONTAL);
		ldata.horizontalIndent = 3;
		m_statusText.setLayoutData(ldata);

		// Update the buttons to loaded state
		m_currentStatus = ExecutorStatus.UNINIT;
		notifyProcStatus(ExecutorStatus.UNINIT, false);
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.replay.views.controls.IProcedureControlPanel#setEnabled(boolean)
     */
	@Override
    public void setEnabled(boolean enabled)
	{
		if (isDisposed()) return;
		if (enabled && m_clientMode == ClientMode.CONTROL)
		{
			notifyProcStatus(m_currentStatus, false);
		}
		super.setEnabled(enabled);
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.replay.views.controls.IProcedureControlPanel#setClientMode(com.astra.ses.spell.gui.replay.core.model.types.ClientMode)
     */
	@Override
    public void setClientMode(ClientMode mode)
	{
		m_clientMode = mode;
		if (mode != ClientMode.CONTROL)
		{
			setEnabled(false);
		}
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.replay.views.controls.IProcedureControlPanel#notifyProcStatus(com.astra.ses.spell.gui.replay.core.model.types.ExecutorStatus, boolean)
     */
	@Override
    public void notifyProcStatus(ExecutorStatus status, boolean fatalError)
	{
		// Logger.instance().debug("Received status: " +
		// ProcedureHelper.toString(status), Logger.Level.GUI, this);
		m_currentStatus = status;

		if (!isDisposed())
		{
			m_statusText.setText(status.toString());
			m_statusText.setBackground(s_cfg.getProcedureColor(status));
		}
	}

	/***************************************************************************
	 * Callback of selection service, called when a button is clicked. NOT USED
	 * 
	 * @param e
	 *            Selection event
	 **************************************************************************/
	public void widgetDefaultSelected(SelectionEvent e)
	{
	}

	/***************************************************************************
	 * Callback of selection service, called when a button is clicked.
	 * 
	 * @param e
	 *            Selection event
	 **************************************************************************/
	public void widgetSelected(SelectionEvent e)
	{
		ExecutorCommand cmd = (ExecutorCommand) e.widget.getData(EXEC_CMD);
		
		if (cmd.equals(ExecutorCommand.ABORT))
		{
			// If a prompt has been cancelled as a result of the abort,
			// do not need to send the command, the procedure will be
			// aborted immediately
			if (m_view.cancelPrompt())
			{
				return;
			}
		}

		String cmdId = e.widget.getData(CMD).toString();
		HashMap<String, String> args = new HashMap<String, String>();
		args.put("procId", m_model.getProcId());
		CommandHelper.execute(cmdId, args);
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.replay.views.controls.IProcedureControlPanel#updateUserAction(com.astra.ses.spell.gui.replay.core.model.notification.UserActionNotification.UserActionStatus, java.lang.String, com.astra.ses.spell.gui.replay.core.model.types.Severity)
     */
	@Override
    public void updateUserAction(UserActionNotification.UserActionStatus st, String action, Severity sev)
	{
	}
}
