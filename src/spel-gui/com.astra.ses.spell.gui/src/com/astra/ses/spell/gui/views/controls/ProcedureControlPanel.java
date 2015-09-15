///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls
// 
// FILE      : ProcedureControlPanel.java
//
// DATE      : 2008-11-21 13:54
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
package com.astra.ses.spell.gui.views.controls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification.UserActionStatus;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.interfaces.IProcedureControlPanel;
import com.astra.ses.spell.gui.interfaces.IProcedureView;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.preferences.initializer.elements.CommandsInfo;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.preferences.keys.PropertyKey;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.types.ExecutorCommand;
import com.astra.ses.spell.gui.types.ExecutorStatus;
import com.astra.ses.spell.gui.types.GuiExecutorCommand;
import com.astra.ses.spell.gui.views.controls.generic.ColoredButton;

/*******************************************************************************
 * @brief Composite which contains the set of controls used during the procedure
 *        execution.
 * @date 09/10/07
 ******************************************************************************/
public class ProcedureControlPanel extends Composite implements SelectionListener, IProcedureControlPanel
{
	private static final String	         EXEC_CMD	 = "execCommand";
	private static final String	         CMD	     = "CommandId";
	private static final String	         USER_ACTION	= "UserAction";

	private static IConfigurationManager	s_cfg	 = null;

	/** Handle for procedure view */
	private IProcedureView	             m_view;
	/** Handle for procedure model */
	private IProcedure	                 m_model;
	/** List of control buttons */
	private Button[]	                 m_controlButtons;
	/** Holds the previously known proc status */
	private ExecutorStatus	             m_lastStatus;
	/** Holds the status display */
	private Label	                     m_statusText;
	/** Holds the client mode */
	private ClientMode	                 m_clientMode;
	/** User action button */
	private ColoredButton	             m_userActionButton;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param view
	 *            Containing view
	 * @param parent
	 *            Parent composite
	 * @style SWT style
	 **************************************************************************/
	public ProcedureControlPanel(IProcedureView view, IProcedure model, Composite parent, int style)
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
		
		// Get buttons
		List<GuiExecutorCommand> commands = getExecutorCommands();
		
		// Get status
		int statusPos = getStatusLocation();
		boolean isUsed = (statusPos >= 0);
		
		layout.numColumns = commands.size() + 4;
		setLayout(layout);
		
		m_controlButtons = new Button[commands.size()];

		// For each defined command, create a button
		// ExecutorCommand names and images are defined in the ICommands
		// interface

		// Create the buttons for commands
		int index = 0;
		for (GuiExecutorCommand action : commands)
		{
			// Add status component (if applies)
			if  ( index == statusPos ) setupStatusComponent(isUsed);
			// Add command button
			Button btn = new Button(this, SWT.PUSH);
			btn.setText(action.label);
			Image image = Activator.getImageDescriptor(action.imagePath).createImage();
			btn.setImage(image);
			btn.setData(EXEC_CMD, action.command);
			btn.setData(CMD, action.handler);
			btn.setVisible(action.used);
			btn.setToolTipText(action.description);
			btn.addSelectionListener(this);
			m_controlButtons[action.pos] = btn;
			index++;
		}

		// Add status component if applies
		if (index <= statusPos || statusPos < 0 ) setupStatusComponent(isUsed);

		m_userActionButton = new ColoredButton(this, SWT.NONE);
		m_userActionButton.setText("  ");
		m_userActionButton.setToolTipText("Perform defined user action");

		GridData gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.horizontalIndent = 3;
		m_userActionButton.setLayoutData( gd );
		m_userActionButton.setVisible(false);
		m_userActionButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				// Issue the command to the procedure
				IProcedureManager mgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
				IProcedure proc = mgr.getProcedure(m_model.getProcId());
				proc.getController().issueCommand(ExecutorCommand.ACTION, new String[0]);
			}
		});

		// Update the buttons to loaded state
		m_lastStatus = ExecutorStatus.UNINIT;
		notifyProcStatus(ExecutorStatus.UNINIT, false);
	}
	
	/**
	 * Set up status component
	 * 
	 */
	private void setupStatusComponent(boolean isUsed)
	{
		m_statusText = new Label(this, SWT.BORDER);
		m_statusText.setText("    UNINIT    ");
		m_statusText.setFont(s_cfg.getFont(FontKey.HEADER));
		m_statusText.setAlignment(SWT.CENTER);
		GridData ldata = new GridData(GridData.FILL_HORIZONTAL);
		ldata.horizontalIndent = 3;
		m_statusText.setLayoutData(ldata);
		m_statusText.setVisible(isUsed);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setEnabled(boolean enabled)
	{
		if (enabled && m_clientMode == ClientMode.CONTROL)
		{
			notifyProcStatus(m_model.getRuntimeInformation().getStatus(), false);
		}
		else
		{
			for (Button b : m_controlButtons)
			{
				b.setEnabled(false);
			}
		}
		super.setEnabled(enabled);
	}

    @Override
	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean setFocus()
	{
		for (Button button : m_controlButtons)
		{
			if (button.isEnabled())
			{
				button.setFocus();
				break;
			}
		}
		return super.setFocus();
	}

	@Override
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void dispose()
	{
	}

	@Override
	/***************************************************************************
	 * 
	 **************************************************************************/
    public void setClientMode(ClientMode mode)
	{
		m_clientMode = mode;
		if (mode != ClientMode.CONTROL)
		{
			setEnabled(false);
		}
	}

	@Override
	/***************************************************************************
	 * 
	 **************************************************************************/
    public void notifyProcStatus(ExecutorStatus status, boolean fatalError)
	{
		if (isDisposed()) return;
		
		m_statusText.setText(status.toString());
		m_statusText.setBackground(s_cfg.getProcedureColor(status));

		// Procedure control buttons enablement
		if (m_clientMode != null)
		{
			boolean controlling = false;
			boolean isError = status.equals(ExecutorStatus.ERROR);
			controlling = m_clientMode.equals(ClientMode.CONTROL);
			
			if (controlling)
			{
				for (Button procButton : m_controlButtons)
				{

					ExecutorCommand comm = (ExecutorCommand) procButton.getData(EXEC_CMD);
					boolean enabled = comm.validate(m_lastStatus,status) && controlling && (!isError);
					procButton.setEnabled(enabled);
				}
				// Update the recover button separatedly
				boolean recoverEnabled = isError && controlling && (!fatalError);
				m_controlButtons[GuiExecutorCommand.RECOVER.pos].setEnabled(recoverEnabled);
			}
			else
			{
				for (Button procButton : m_controlButtons)
				{
					procButton.setEnabled(false);
				}				
			}
		}
		
		// USER ACTION VALIDATION
		boolean userEnabled = ExecutorCommand.ACTION.validate(m_lastStatus,status);
		UserActionStatus st = (UserActionStatus) m_userActionButton.getData(USER_ACTION);
		boolean validStatus = false;
		if (st != null)
		{
			validStatus = st.equals(UserActionStatus.ENABLED);
		}
		m_userActionButton.setEnabled(userEnabled && validStatus);
		m_lastStatus = status;
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
			// Ask for confirmation if configured to do so
			if (s_cfg.getBooleanProperty(PropertyKey.CONFIRM_ABORT))
			{
				if (!MessageDialog.openConfirm(getShell(), "Abort procedure", "Do you really want to abort this procedure?"))
				{
					return;
				}
			}
			
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

	@Override
	/***************************************************************************
	 * 
	 **************************************************************************/
    public void updateUserAction(UserActionStatus st, String action, Severity sev)
	{
		boolean buttonEnabled = false;
		boolean buttonVisible = true;
		switch (st)
		{
		case ENABLED:
			buttonEnabled = true;
		case DISABLED:
			if (action != null)
			{
				String adapted = convertUserActionName(action);
				m_userActionButton.setText(adapted);
				m_userActionButton.pack(true);
				layout();
			}
			break;
		case DISMISSED:
			m_userActionButton.setText("N/A");
			m_userActionButton.pack(true);
			buttonVisible = false;
			layout();
		default:
			break;
		}
		// User action enablement depends on client mode
		boolean controlling = false;
		if (m_clientMode != null)
		{
			controlling = m_clientMode.equals(ClientMode.CONTROL);
		}
		boolean validExecStatus = false;
		switch (m_model.getRuntimeInformation().getStatus())
		{
		case LOADED:
		case PAUSED:
		case RUNNING:
			validExecStatus = true;
			break;
		}
		m_userActionButton.setData(USER_ACTION, st);
		m_userActionButton.setEnabled(buttonEnabled && controlling && validExecStatus);

		// Button visible?
		boolean currentVisible = m_userActionButton.isVisible();
		if (buttonVisible != currentVisible)
		{
			m_userActionButton.setVisible(buttonVisible);
		}
		if (buttonVisible && (sev != null))
		{
			switch (sev)
			{
			case INFO:
				m_userActionButton.setBackground(null);
				break;
			case ERROR:
				m_userActionButton.setBackground(s_cfg.getStatusColor(ItemStatus.ERROR));
				break;
			case WARN:
				m_userActionButton.setBackground(s_cfg.getStatusColor(ItemStatus.WARNING));
				break;
			}
			m_userActionButton.redraw();
		}
	}

	/***************************************************************************
	 * Update user action button with the given action name
	 **************************************************************************/
	private String convertUserActionName(String actionName)
	{
		String text = actionName;
		if (actionName.length() > 15)
		{
			text = actionName.substring(0, 11) + "...";
		}
		return text;
	}

	/***************************************************************************
	 * Get list of defined executor commands 
	 **************************************************************************/
	private List<GuiExecutorCommand> getExecutorCommands()
	{
		// Commands list
		List<GuiExecutorCommand> commands = new ArrayList<GuiExecutorCommand>();
		
		// Extract procedure execution buttons from preferences
		CommandsInfo info = s_cfg.getCommands();
		
		// Checking size
		if ( info.getMap().size() == 0 )
		{
			System.err.println("Commands not found in configuration file!! Go back to default configuration");
			commands = Arrays.asList(GuiExecutorCommand.values());
		}
		else
		{
			List<String> keys = new ArrayList<String>(info.getMap().keySet());
			List<String> values = new ArrayList<String>(info.getMap().values());
			int out = keys.size();
			for (GuiExecutorCommand command : GuiExecutorCommand.values())
			{
				int index = keys.indexOf(command.command.getId());
				if ( index != -1 )
				{
					//Updating command location/label according to configuration info
					command.pos = index;
					command.label = values.get(index);
					commands.add(command);
				}
				else 
				{
					//Adding not used command at the end of the commands group
					command.pos = out;
					command.used = false;
					commands.add(command);
					out++;
				}
			}
			//Sorting commands by actual position
			Collections.sort(commands, new Comparator<GuiExecutorCommand>() {
				@Override
				public int compare(GuiExecutorCommand command1, GuiExecutorCommand command2) 
				{
					return command1.pos - command2.pos;
				}
			});
		}
		
		return commands;
	}
	
	/***************************************************************************
	 * Get location of the Status indicator in the bar
	 **************************************************************************/
	private int getStatusLocation()
	{
		// Extract procedure execution buttons from preferences
		int location = s_cfg.getStatus().getLocation();
		return location;
	}
	
}
