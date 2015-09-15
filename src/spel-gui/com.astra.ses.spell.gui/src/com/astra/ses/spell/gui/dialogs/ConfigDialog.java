///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs
// 
// FILE      : ConfigDialog.java
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
package com.astra.ses.spell.gui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

/*******************************************************************************
 * @brief Dialog for execution configuration on gui
 * @date 18/09/07
 ******************************************************************************/
public class ConfigDialog extends TitleAreaDialog
{
	/** Holds the dialog image icon */
	private Image m_image;
	/** Holds the Run into option */
	private boolean m_runInto;
	/** Holds the ByStep option */
	private boolean m_byStep;
	/** Holds the exec delay value */
	private int m_delay;
	/** Holds the TC Confirm status */
	private boolean m_tcConfirm;
	/** PromptWaringDelay */
	private int m_promptWarningDelay;
	/** Holds the model reference */
	private IProcedure m_model;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param shell
	 *            The parent shell
	 **************************************************************************/
	public ConfigDialog(Shell shell, IProcedure model)
	{
		super(shell);
		// Obtain the image for the dialog icon
		ImageDescriptor descr = Activator.getImageDescriptor("icons/dlg_configuration.png");
		m_image = descr.createImage();
		m_model = model;
	}

	/***************************************************************************
	 * Called when the dialog is about to close.
	 * 
	 * @return The superclass return value.
	 **************************************************************************/
	public boolean close()
	{
		m_image.dispose();
		return super.close();
	}

	/***************************************************************************
	 * Creates the dialog contents.
	 * 
	 * @param parent
	 *            The base composite of the dialog
	 * @return The resulting contents
	 **************************************************************************/
	protected Control createContents(Composite parent)
	{
		Control contents = super.createContents(parent);
		setTitle("Execution configuration");
		setTitleImage(m_image);
		setMessage("Configure the procedure execution parameters");
		return contents;
	}

	/***************************************************************************
	 * Create the dialog area contents.
	 * 
	 * @param parent
	 *            The base composite of the dialog
	 * @return The resulting contents
	 **************************************************************************/
	protected Control createDialogArea(Composite parent)
	{
		
		// Main composite of the dialog area -----------------------------------
		Composite top = new Composite(parent, SWT.NONE );
		Composite leftColumn = new Composite(top, SWT.NONE );
		Composite rightColumn = new Composite(top, SWT.NONE );
		
		GridLayout layout = new GridLayout();
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		layout.marginTop = 2;
		layout.marginBottom = 2;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		layout.numColumns = 2;
		
		GridLayout rightLayout = new GridLayout(2, false);
		rightLayout.marginWidth = 0;
		
		GridLayout leftLayout = new GridLayout(1, false);
		leftLayout.marginWidth = 0;
		
		GridData gd = new GridData( GridData.FILL_BOTH );
		gd.horizontalAlignment = SWT.RIGHT;
		
		top.setLayout(layout);
		top.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		leftColumn.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		leftColumn.setLayout(leftLayout);
		
		rightColumn.setLayoutData(gd);
		rightColumn.setLayout(rightLayout);
		
		// Create controls
		final Button chkRunInto = new Button(leftColumn, SWT.CHECK);
		chkRunInto.setText("Run into functions");
	
		final Button chkByStep = new Button(leftColumn, SWT.CHECK);
		chkByStep.setText("Step by step");

		final Button tcConfirm = new Button(leftColumn, SWT.CHECK);
		tcConfirm.setText("TC Confirm");		


		//Exec delay
		Label labelExecDelay = new Label(rightColumn, SWT.NONE);
		labelExecDelay.setText("Execution delay (msec):");

		final Spinner spnDelay = new Spinner(rightColumn, SWT.NONE);
		spnDelay.setMinimum(0);
		spnDelay.setMaximum(2000);
		spnDelay.setIncrement(1);
		spnDelay.setPageIncrement(100);
		m_delay = m_model.getRuntimeInformation().getExecutionDelay();
		spnDelay.setSelection(m_delay);
		
		//Prompt Warning Delay
		Label labelPromptDelay = new Label(rightColumn, SWT.NONE);
		labelPromptDelay.setText("Prompt warning delay (sec):");
		
		final Spinner spnPromptWarningDelay = new Spinner(rightColumn, SWT.NONE);
		spnPromptWarningDelay.setMinimum(0);
		spnPromptWarningDelay.setMaximum(9999);
		spnPromptWarningDelay.setIncrement(1);
		spnPromptWarningDelay.setPageIncrement(100);
		m_promptWarningDelay = m_model.getRuntimeInformation().getPromptWarningDelay();
		spnPromptWarningDelay.setSelection(m_promptWarningDelay);
		
		//Run Into
		m_runInto = m_model.getExecutionManager().isRunInto();
		chkRunInto.setSelection(m_runInto);

		//By Step
		m_byStep = m_model.getRuntimeInformation().isStepByStep();
		chkByStep.setSelection(m_byStep);

		//Tc Confirm
		m_tcConfirm = m_model.getRuntimeInformation().isForceTcConfirmation();
		tcConfirm.setSelection(m_tcConfirm);
		
		// Assign handlers to register the changes
		chkRunInto.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				m_runInto = chkRunInto.getSelection();
			}
		});

		chkByStep.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				m_byStep = chkByStep.getSelection();
			}
		});
		
		spnDelay.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				m_delay = spnDelay.getSelection();
			}
		});
		
		tcConfirm.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				m_tcConfirm = tcConfirm.getSelection();
			}
		});
		
		spnPromptWarningDelay.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				m_promptWarningDelay = spnPromptWarningDelay.getSelection();
			}
		});

		return parent;
	}

	/***************************************************************************
	 * Create the button bar buttons.
	 * 
	 * @param parent
	 *            The Button Bar.
	 **************************************************************************/
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	/***************************************************************************
	 * Called when one of the buttons of the button bar is pressed.
	 * 
	 * @param buttonId
	 *            The button identifier.
	 **************************************************************************/
	protected void buttonPressed(int buttonId)
	{
		setReturnCode(buttonId);
		switch (buttonId)
		{
		case IDialogConstants.OK_ID:
		case IDialogConstants.CANCEL_ID:
			close();
			break;
		}
	}

	public int getExecutionDelay()
	{
		return m_delay;
	}

	public int getPromptWarningDelay()
	{
		return m_promptWarningDelay;
	}
	
	public boolean getRunInto()
	{
		return m_runInto;
	}

	public boolean getByStep()
	{
		return m_byStep;
	}
	
	public boolean getTcConfirm()
	{
		return m_tcConfirm;
	}
}

