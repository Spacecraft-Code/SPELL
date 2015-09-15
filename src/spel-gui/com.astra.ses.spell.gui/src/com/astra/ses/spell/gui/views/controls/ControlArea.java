///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls
// 
// FILE      : ControlArea.java
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

import java.util.Arrays;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.astra.ses.spell.gui.core.exceptions.CommandFailed;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.InputData;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification.UserActionStatus;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.interfaces.IControlArea;
import com.astra.ses.spell.gui.interfaces.IProcedureView;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformation;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.types.ExecutorStatus;
import com.astra.ses.spell.gui.types.GuiExecutorCommand;
import com.astra.ses.spell.gui.views.controls.input.InputArea;

public class ControlArea extends Composite implements IControlArea
{
	/** Handle to the console manager */
	private static IProcedureManager	s_pmgr	= null;

	/** Dynamic input area for receiving user input from the code page */
	private InputArea	             m_input;
	/** Button set for controlling the procedure in the code page */
	private ProcedureControlPanel	 m_controlPanel;
	/** Model */
	private IProcedure	             m_model;
	/** Container */
	private Composite	             m_top;
	/** Holds the prompt message if any */
	private InputData	             m_promptData;

	private static final int LAYOUT_MARGIN = 5;
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public ControlArea(IProcedureView view, IProcedure model, Composite top, String procId)
	{
		super(top, SWT.NONE);
		m_top = top;
		m_model = model;
		m_promptData = null;
		if (s_pmgr == null)
		{
			s_pmgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
		}

		GridLayout ca_layout = new GridLayout();
		// We do not want extra margins
		ca_layout.marginTop = LAYOUT_MARGIN;
		ca_layout.marginBottom = LAYOUT_MARGIN;
		ca_layout.marginLeft = LAYOUT_MARGIN;
		ca_layout.marginRight = LAYOUT_MARGIN;
		ca_layout.marginHeight = 0;
		ca_layout.marginWidth = 0;
		// Will place each component below the previous one
		ca_layout.numColumns = 1;
		setLayout(ca_layout);

		// Create the procedure control panel
		Logger.debug("Creating control panel", Level.INIT, this);
		m_controlPanel = createControlPanel(view,model);
		// Construct the composite contents
		m_controlPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create the input area
		Logger.debug("Creating input area", Level.INIT, this);
		m_input = new InputArea(this, model);

		GridData ldata = new GridData(GridData.FILL_HORIZONTAL);
		m_input.setLayoutData(ldata);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	protected ProcedureControlPanel createControlPanel( IProcedureView view, IProcedure model )
	{
		return new ProcedureControlPanel(view, model, this, SWT.NONE);
	}
	
	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.views.controls.IControlArea#setEnabled(boolean)
     */
	@Override
	public void setEnabled(boolean enabled)
	{
		m_controlPanel.setEnabled(enabled);
		m_input.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.views.controls.IControlArea#setFocus()
     */
	@Override
	public boolean setFocus()
	{
		return m_input.setFocus();
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.views.controls.IControlArea#zoom(boolean)
     */
	@Override
    public void zoom(boolean increase)
	{
		m_input.zoom(increase);
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.views.controls.IControlArea#setProcedureStatus(com.astra.ses.spell.gui.core.model.types.ExecutorStatus, boolean)
     */
	@Override
    public void setProcedureStatus(ExecutorStatus status, boolean fatalError)
	{
		m_controlPanel.notifyProcStatus(status, fatalError);
		if (status == ExecutorStatus.UNINIT || status == ExecutorStatus.UNKNOWN || status == ExecutorStatus.ERROR
		        || status == ExecutorStatus.LOADED)
		{
			m_input.setEnabled(false);
		}
		else
		{
			m_input.setEnabled(true);
		}
		// Update the user action status if needed
		IExecutionInformation info = m_model.getRuntimeInformation();
		String actionLabel = info.getUserAction();
		boolean actionEnabled = info.getUserActionStatus().equals(UserActionStatus.ENABLED);
		Severity actionSev = info.getUserActionSeverity();
		if ((actionLabel != null) && (!actionLabel.isEmpty()))
		{
			updateUserAction(actionEnabled ? UserActionStatus.ENABLED : UserActionStatus.DISABLED, actionLabel,
			        actionSev);
		}
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.views.controls.IControlArea#setClientMode(com.astra.ses.spell.gui.core.model.types.ClientMode)
     */
	@Override
    public void setClientMode(ClientMode mode)
	{
		m_controlPanel.setClientMode(mode);
		m_input.setClientMode(mode);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void startPrompt(InputData promptData)
	{
		// Store the prompt data, used later for cancel or reset
		m_promptData = promptData;

		// Update the prompt input field and re-layout the area
		m_input.prompt(promptData);

		m_top.layout();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void resetPrompt()
	{
		Logger.debug("Reset prompt", Level.PROC, this);
		if (m_promptData != null)
		{
			// If it is not a notification but a controlling prompt, update
			// the control panel buttons. If it is a notification the
			// buttons must be kept grayed out since this is a monitoring GUI.
			if (!m_promptData.isNotification())
			{
				m_controlPanel.setFocus();
			}
			// Clear the prompt data
			m_promptData = null;
			// Reset the input area and re-layout
			m_input.reset();
			m_top.layout();
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean cancelPrompt( boolean uponNotification )
	{
		Logger.debug("Cancel prompt (notified:" + uponNotification + ")", Level.PROC, this);
		if (m_promptData != null)
		{
			// If it is not a notification but a controlling prompt, tell
			// the prompt data object to assume 'cancel' value. If this
			// is a notification this is a monitoring GUI and no return value
			// is expected.
			if (!uponNotification)
			{
				m_input.cancelPrompt();
			}
			m_promptData = null;
			m_top.layout();
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.views.controls.IControlArea#isPrompt()
     */
	@Override
    public boolean isPrompt()
	{
		return ((m_promptData != null) && (!m_promptData.isReady()));
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.views.controls.IControlArea#issueCommand(java.lang.String)
     */
	@Override
    public void issueCommand(String cmdString)
	{
		String[] elements = cmdString.split(" ");
		for (GuiExecutorCommand action : GuiExecutorCommand.values())
		{
			if (elements[0].toLowerCase().equals(action.command.toString().toLowerCase()))
			{
				String[] arguments = new String[0];
				if (elements.length > 1)
				{
					arguments = Arrays.copyOfRange(elements, 1, elements.length - 1);
				}
				try
				{
					m_model.getController().issueCommand(action.command, arguments);
				}
				catch (CommandFailed ex)
				{
					MessageDialog.openError(getShell(), "Command error", ex.getLocalizedMessage());
				}
				return;
			}
		}
		MessageDialog.openError(getShell(), "Command error", "Unrecognised command: " + cmdString);
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.views.controls.IControlArea#updateUserAction(com.astra.ses.spell.gui.core.model.notification.UserActionNotification.UserActionStatus, java.lang.String, com.astra.ses.spell.gui.core.model.types.Severity)
     */
	@Override
    public void updateUserAction(UserActionStatus st, String action, Severity sev)
	{
		m_controlPanel.updateUserAction(st, action, sev);
	}

	@Override
    public void onSashMoved( int offset )
    {
		// Substract the margins and the height of the control panel
		int toSubstract = (m_controlPanel.getBounds().height + LAYOUT_MARGIN*2);
		int height = getClientArea().height-toSubstract;
		m_input.setSize( getClientArea().width-10, height ); // Take into account the height of the internal controls
	    m_input.onSashMoved( height );
    }

	@Override
    public Composite getControl()
    {
	    return this;
    }

}
