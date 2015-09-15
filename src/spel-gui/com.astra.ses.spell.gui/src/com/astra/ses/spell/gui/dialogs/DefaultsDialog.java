///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs
// 
// FILE      : DefaultsDialog.java
//
// DATE      : 2014-02-17 14:30
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.model.server.ExecutorDefaults;
import com.astra.ses.spell.gui.core.model.types.BrowsableLibMode;
import com.astra.ses.spell.gui.core.model.types.SaveStateMode;

/*******************************************************************************
 * @brief Dialog for execution configuration on gui
 * @date 18/09/07
 ******************************************************************************/
public class DefaultsDialog extends TitleAreaDialog
{

	/** Holds the dialog image icon */
	private Image m_image;
	/** Holds the Context Executor Defaults of the form */
	private ExecutorDefaults m_executorDefaults;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param shell
	 *            The parent shell
	 **************************************************************************/
	public DefaultsDialog(Shell shell, ExecutorDefaults executorDefaults)
	{
		super(shell);
		// Obtain the image for the dialog icon
		ImageDescriptor descr = Activator.getImageDescriptor("icons/dlg_configuration.png");
		m_image = descr.createImage();
		m_executorDefaults = new ExecutorDefaults();
		m_executorDefaults.copyFrom(executorDefaults);

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
		setMessage("Configure the context execution default parameters");
		setTitle("Executor defaults");
		setTitleImage(m_image);
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

		// GUI objects
		Composite top;
		Composite leftColumn;
		Composite rightColumn;
		GridLayout layout;
		GridLayout rightLayout;
		GridData gdSpan;
		GridData leftGD;

		Label execDelay_lbl;
		Label promptDelay_lbl;
		Label maxVerbosity_lbl;
		Label saveStateMode_lbl;

		final Button runInto_btn;
		final Button byStep_btn;
		final Button browsableLib_btn;
		final Button forceTcConfirm_btn;

		final Spinner execDelay_spn;
		final Spinner promptDelay_spn;
		final Spinner maxVerbosity_spn;

		final Combo saveStateMode_cmb;

		// Layout
		layout = new GridLayout();
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		layout.marginTop = 2;
		layout.marginBottom = 2;
		layout.marginLeft = 2;
		layout.marginRight = 2;
		layout.numColumns = 2;

		rightLayout = new GridLayout();
		rightLayout.numColumns = 2;

		top = new Composite(parent, SWT.NONE);
		top.setLayoutData(new GridData(GridData.FILL_BOTH));
		top.setLayout(layout);

		leftGD = new GridData(GridData.FILL_BOTH);
		leftGD.widthHint = 225;

		leftColumn = new Composite(top, SWT.NONE);
		leftColumn.setLayoutData(leftGD);
		leftColumn.setLayout(new GridLayout());

		rightColumn = new Composite(top, SWT.NONE);
		rightColumn.setLayoutData(new GridData(GridData.FILL_BOTH));
		rightColumn.setLayout(rightLayout);

		gdSpan = new GridData(GridData.FILL_HORIZONTAL);
		gdSpan.horizontalAlignment = SWT.RIGHT;

		// Controls initialization
		// left column
		runInto_btn = new Button(leftColumn, SWT.CHECK);
		runInto_btn.setText("Run into");

		byStep_btn = new Button(leftColumn, SWT.CHECK);
		byStep_btn.setText("By step");

		browsableLib_btn = new Button(leftColumn, SWT.CHECK);
		browsableLib_btn.setText("Browsable libraries");

		forceTcConfirm_btn = new Button(leftColumn, SWT.CHECK);
		forceTcConfirm_btn.setText("Force TC confirm");

		// right column
		execDelay_lbl = new Label(rightColumn, SWT.NONE);
		execDelay_lbl.setText("Execution delay (msec)");

		execDelay_spn = new Spinner(rightColumn, SWT.NONE);
		execDelay_spn.setMinimum(0);
		execDelay_spn.setMaximum(2000);
		execDelay_spn.setIncrement(1);
		execDelay_spn.setPageIncrement(100);

		execDelay_spn.setLayoutData(gdSpan);

		promptDelay_lbl = new Label(rightColumn, SWT.NONE);
		promptDelay_lbl.setText("Prompt warning delay (sec)");

		promptDelay_spn = new Spinner(rightColumn, SWT.NONE);
		promptDelay_spn.setMinimum(0);
		promptDelay_spn.setMaximum(2000);
		promptDelay_spn.setIncrement(1);
		promptDelay_spn.setPageIncrement(100);

		promptDelay_spn.setLayoutData(gdSpan);

		maxVerbosity_lbl = new Label(rightColumn, SWT.NONE);
		maxVerbosity_lbl.setText("Maximum verbosity");

		maxVerbosity_spn = new Spinner(rightColumn, SWT.NONE);
		maxVerbosity_spn.setMinimum(0);
		maxVerbosity_spn.setMaximum(2000);
		maxVerbosity_spn.setIncrement(1);
		maxVerbosity_spn.setPageIncrement(100);

		maxVerbosity_spn.setLayoutData(gdSpan);

		saveStateMode_lbl = new Label(rightColumn, SWT.NONE);
		saveStateMode_lbl.setText("Save state mode");

		saveStateMode_cmb = new Combo(rightColumn, SWT.READ_ONLY);
		for (SaveStateMode mode : SaveStateMode.values())
		{
			if (!mode.getKey().isEmpty())
				saveStateMode_cmb.add(mode.getKey());
		} // for SaveStateMode

		// Update values from context defaults
		runInto_btn.setSelection(m_executorDefaults.getRunInto());
		byStep_btn.setSelection(m_executorDefaults.getByStep());
		switch(m_executorDefaults.getBrowsableLib())
		{
		case DISABLED:
			browsableLib_btn.setSelection(false);
			browsableLib_btn.setEnabled(false);
			break;
		case OFF:
			browsableLib_btn.setSelection(false);
			break;
		case ON:
			browsableLib_btn.setSelection(true);
			break;
		}
		forceTcConfirm_btn.setSelection(m_executorDefaults.getForceTcConfirm());

		execDelay_spn.setSelection(m_executorDefaults.getExecDelay());
		promptDelay_spn.setSelection(m_executorDefaults.getPromptWarningDelay());
		maxVerbosity_spn.setSelection(m_executorDefaults.getMaxVerbosity());
		saveStateMode_cmb.select(saveStateMode_cmb.indexOf(m_executorDefaults.getSaveStateModeStr()));

		// Assign handlers to update the values of the m_executorDefaults with
		// the form fields changes

		runInto_btn.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				m_executorDefaults.setRunInto(runInto_btn.getSelection());
			}
		});

		byStep_btn.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				m_executorDefaults.setByStep(byStep_btn.getSelection());
			}
		});

		browsableLib_btn.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				if (browsableLib_btn.getSelection())
				{
					MessageDialog.openWarning(getShell(), "WARNING!", "Enabling browsable libraries may render the system unusable in this SPELL version.\n\n" + 
							"Use it at your own risk (no UTDs can be reported if this flag is activated)");				
				}
				if (browsableLib_btn.getSelection())
				{
					m_executorDefaults.setBrowsableLib( BrowsableLibMode.ON );
				}
				else
				{
					m_executorDefaults.setBrowsableLib( BrowsableLibMode.OFF );
				}
			}
		});

		forceTcConfirm_btn.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				m_executorDefaults.setForceTcConfirm(forceTcConfirm_btn.getSelection());
			}
		});

		execDelay_spn.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				m_executorDefaults.setExecDelay(execDelay_spn.getSelection());
			}
		});

		promptDelay_spn.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				m_executorDefaults.setPromptWarningDelay(promptDelay_spn.getSelection());
			}
		});

		maxVerbosity_spn.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				m_executorDefaults.setMaxVerbosity(maxVerbosity_spn.getSelection());
			}
		});

		saveStateMode_cmb.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				m_executorDefaults.setSaveStateModeStr(saveStateMode_cmb.getItem(saveStateMode_cmb.getSelectionIndex()));
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

	public ExecutorDefaults getExecutorDefaults()
	{
		return m_executorDefaults;
	}

	public void setExecutorDefaults(ExecutorDefaults executorDefaults)
	{
		m_executorDefaults = executorDefaults;
	}

} // DefaultsDialog

