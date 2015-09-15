///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs
// 
// FILE      : ServerFileSelectionDialog.java
//
// DATE      : Feb 8, 2012
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
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.model.server.DirectoryFile;
import com.astra.ses.spell.gui.core.model.server.DirectoryTree;
import com.astra.ses.spell.gui.dialogs.controls.ServerDirectoryTreeViewer;

/*******************************************************************************
 * @brief Dialog for server file selection
 ******************************************************************************/
public class ServerFileSelectionDialog extends TitleAreaDialog implements ISelectionChangedListener
{
	public static final String ID = "com.astra.ses.spell.gui.dialogs.ServerFileSelectionDialog";


	/** Holds the dialog image icon */
	private Image m_image;
	private DirectoryTree m_tree;
	private ServerDirectoryTreeViewer m_viewer;
	private DirectoryFile m_selected;
	
	/***************************************************************************
	 * Constructor
	 * 
	 * @param shell
	 *            The parent shell
	 **************************************************************************/
	public ServerFileSelectionDialog(Shell shell, DirectoryTree directory )
	{
		super(shell);
		// Obtain the image for the dialog icon
		ImageDescriptor descr = Activator.getImageDescriptor("icons/dlg_exec.png");
		m_image = descr.createImage();
		m_tree = directory;
		m_selected = null;
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
		setMessage("Select a data file");
		setTitle("Server file selection");
		setTitleImage(m_image);
		getShell().setText("Server file selection");
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
		areaData.widthHint = 600;
		areaData.heightHint = 400;
		top.setLayoutData(areaData);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing = 10;
		top.setLayout(layout);

		m_viewer = new ServerDirectoryTreeViewer(top);
		m_viewer.getControl().setLayoutData( new GridData( GridData.FILL_BOTH ));
		m_viewer.setInput(m_tree);
		m_viewer.refresh();
		
		m_viewer.addSelectionChangedListener(this);
		
		return parent;
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
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
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
		if (buttonId == IDialogConstants.OK_ID)
		{
		}
		else if (buttonId == IDialogConstants.CANCEL_ID)
		{
			m_selected = null;
		}
		close();
	}

	@Override
    public void selectionChanged(SelectionChangedEvent event)
    {
	    ISelection sel = event.getSelection();
	    if (!sel.isEmpty())
	    {
	    	IStructuredSelection isel = (IStructuredSelection) sel;
	    	Object selected = isel.getFirstElement();
	    	if (selected instanceof DirectoryFile)
	    	{
		    	getButton(IDialogConstants.OK_ID).setEnabled(true);
		    	DirectoryFile file = (DirectoryFile) selected;
		    	setMessage("File selected: " + file.getFilename() );
		    	m_selected = file;
	    	}
	    	else
	    	{
		    	getButton(IDialogConstants.OK_ID).setEnabled(false);
		    	setMessage("Select a data file");
		    	m_selected = null;
	    	}
	    }
	    else
	    {
	    	setMessage("Select a data file");
	    	m_selected = null;
		    getButton(IDialogConstants.OK_ID).setEnabled(false);
	    }
    }
	
	public DirectoryFile getSelectedFile()
	{
		return m_selected;
	}
}
