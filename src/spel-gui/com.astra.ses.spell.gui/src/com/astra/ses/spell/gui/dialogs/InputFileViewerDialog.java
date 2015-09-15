///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs
// 
// FILE      : InputFileViewerDialog.java
//
// DATE      : Feb 9, 2012
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
package com.astra.ses.spell.gui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.model.server.DirectoryFile;
import com.astra.ses.spell.gui.core.model.types.DataContainer;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.dialogs.controls.DictVariablesTable;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.model.jobs.BuildDirectoryTreeJob;
import com.astra.ses.spell.gui.model.jobs.GetInputFileJob;

/*******************************************************************************
 * @brief Dialog for viewing input files
 ******************************************************************************/
public class InputFileViewerDialog extends TitleAreaDialog 
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	public static final String ID = "com.astra.ses.spell.gui.dialogs.InputFileViewerDialog";

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the dialog image icon */
	private Image m_image;
	/** Holds the bold font */
	private Font m_font;
	/** Holds the dictionary model */
	private DataContainer m_container;
	/** Holds the file viewer */
	private DictVariablesTable m_fileTable;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 * 
	 * @param shell
	 *            The parent shell
	 **************************************************************************/
	public InputFileViewerDialog(Shell shell)
	{
		super(shell);
		// Obtain the image for the dialog icon
		ImageDescriptor descr = Activator.getImageDescriptor("icons/dlg_exec.png");
		m_image = descr.createImage();
		m_font = JFaceResources.getBannerFont();
		m_container = new DataContainer("<none>", "");
	}

	/***************************************************************************
	 * Defines the shell characteristics
	 **************************************************************************/
	protected void setShellStyle( int newShellStyle )
	{
		super.setShellStyle(SWT.CLOSE|SWT.MIN|SWT.MAX|SWT.RESIZE);
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

	// =========================================================================
	// # NON-ACCESSIBLE METHODS
	// =========================================================================

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
		setMessage("InputData File Viewer");
		setTitle("InputData data file viewer");
		setTitleImage(m_image);
		getShell().setText("InputData File Viewer");
		return contents;
	}

	/***************************************************************************
	 * Create the dialog area contents.
	 * 
	 * @param parent
	 *            The base composite of the dialog
	 * @return The resulting contents
	 **************************************************************************/
	@Override
	protected Control createDialogArea(Composite parent)
	{
		// Main composite of the dialog area -----------------------------------
		Composite top = new Composite(parent, SWT.NONE);
		GridData areaData = new GridData(GridData.FILL_BOTH);
		areaData.widthHint = 1000;
		areaData.heightHint = 700;
		top.setLayoutData(areaData);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing = 10;
		top.setLayout(layout);

	/////////////////////////////////////////////////////////////////////////////////////////

		Composite buttons = new Composite(top, SWT.BORDER);
		buttons.setLayout( new RowLayout( SWT.HORIZONTAL ));
		buttons.setLayoutData(new GridData( GridData.FILL_HORIZONTAL ));
		
			Button btnOpen = new Button(buttons, SWT.PUSH);
			btnOpen.setText("Select file...");
			btnOpen.addSelectionListener( new SelectionAdapter()
			{
				public void widgetSelected( SelectionEvent ev )
				{
					doOpenFile();
				}
			}
			);

	/////////////////////////////////////////////////////////////////////////////////////////
			
		Composite dictGroup = new Composite(top, SWT.BORDER );
		dictGroup.setLayout( new GridLayout(1, true) );
		dictGroup.setLayoutData(new GridData( GridData.FILL_BOTH ));
		
			Label title1 = new Label(dictGroup, SWT.NONE);
			title1.setText("File contents");
			title1.setFont(m_font);
			title1.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));
			
			Label sep1 = new Label(dictGroup, SWT.HORIZONTAL | SWT.SEPARATOR );
			sep1.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));
			
			int style = SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL;
			m_fileTable = new DictVariablesTable(dictGroup,true,true,style);
			m_fileTable.getControl().setLayoutData( new GridData( GridData.FILL_BOTH ));

	/////////////////////////////////////////////////////////////////////////////////////////
		
		m_fileTable.initialize(m_container);
			
		return parent;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void doOpenFile()
	{
		BuildDirectoryTreeJob job = new BuildDirectoryTreeJob();
		CommandHelper.executeInProgress(job, true, true);
		if (job.result.equals(CommandResult.SUCCESS))
		{
			ServerFileSelectionDialog dialog = new ServerFileSelectionDialog(this.getShell(), job.tree);
			dialog.open();
			
			DirectoryFile file = dialog.getSelectedFile();
			if (file != null)
			{
				GetInputFileJob job2 = new GetInputFileJob( file.getAbsolutePath() );
				CommandHelper.executeInProgress(job2, true, true);
				if (job2.result.equals(CommandResult.SUCCESS))
				{
					m_container = job2.container;
					Logger.debug("Obtained input file of " + m_container.getVariables().size() + " variables", Level.PROC, this);
					m_fileTable.setInput(m_container);
					m_fileTable.refresh();
				}
				else
				{
					String msg = "Failed to obtain server file: " + file.getFilename() + "\n" + job2.error;
					MessageDialog.openError(getShell(), "Obtain input file", msg );
				}
			}
		}
	}
	
	/***************************************************************************
	 * Create the button bar buttons.
	 * 
	 * @param parent
	 *            The Button Bar.
	 **************************************************************************/
	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, "Close", true);
	}

	/***************************************************************************
	 * Called when one of the buttons of the button bar is pressed.
	 * 
	 * @param buttonId
	 *            The button identifier.
	 **************************************************************************/
	@Override
	protected void buttonPressed(int buttonId)
	{
		close();
	}
}
