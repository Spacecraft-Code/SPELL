///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs
// 
// FILE      : ServerFileSaveDialog.java
//
// DATE      : Aug 1, 2014
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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.model.server.DirectoryFile;
import com.astra.ses.spell.gui.core.model.server.DirectoryTree;
import com.astra.ses.spell.gui.dialogs.controls.ServerDirectoryTreeViewer;

/*******************************************************************************
 * @brief Dialog for server file save
 ******************************************************************************/
public class ServerFileSaveDialog extends TitleAreaDialog implements ISelectionChangedListener
{
	public static final String ID = "com.astra.ses.spell.gui.dialogs.ServerFileSaveDialog";


	/** Holds the dialog image icon */
	private Image m_image;
	private DirectoryTree m_tree;
	private ServerDirectoryTreeViewer m_viewer;
	private String m_path;
	private Text m_fileName;
	
	/***************************************************************************
	 * Constructor
	 * 
	 * @param shell
	 *            The parent shell
	 **************************************************************************/
	public ServerFileSaveDialog(Shell shell, DirectoryTree directory )
	{
		super(shell);
		// Obtain the image for the dialog icon
		ImageDescriptor descr = Activator.getImageDescriptor("icons/dlg_exec.png");
		m_image = descr.createImage();
		m_tree = directory;
		m_path = null;
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
		setTitle("Save to file");
		setTitleImage(m_image);
		getShell().setText("Save to file");
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

		m_fileName = new Text(top,SWT.BORDER);
		m_fileName.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));
		m_fileName.addModifyListener( new ModifyListener()
		{
			@Override
            public void modifyText(ModifyEvent ev)
            {
		    	IStructuredSelection isel = (IStructuredSelection) m_viewer.getSelection();
		    	if (isel == null || isel.isEmpty())
				{
		        	setErrorMessage("Must select a directory: only file name given");
		    		getButton(IDialogConstants.OK_ID).setEnabled(false);
				}
		    	else
		    	{
			    	Object selected = isel.getFirstElement();
			    	if (selected instanceof DirectoryTree)
			    	{
			    		String filename = m_fileName.getText().trim();
			    		if (filename.isEmpty()) 
						{
			            	setErrorMessage("Must provide a file name, or select an existing file.");
			        		getButton(IDialogConstants.OK_ID).setEnabled(false);
						}
			    		else
			    		{
			    			setErrorMessage(null);
			        		getButton(IDialogConstants.OK_ID).setEnabled(true);
			        		DirectoryTree dir = (DirectoryTree) selected;
			            	m_path = dir.getPath() + "/" + filename;
				        	setMessage("File: '" + m_path + "'");
			    		}
			    	}
			    	else 
			    	{
			    		DirectoryFile file = (DirectoryFile) selected;
			    		final DirectoryTree parent = file.getParent();
			    		m_viewer.setSelection( new IStructuredSelection()
			    		{
							@Override
                            public boolean isEmpty() { return false; }

							@Override
                            public Object getFirstElement() { return parent; }; 

							@SuppressWarnings("rawtypes")
                            @Override
                            public Iterator iterator() { return null; }

							@Override
                            public int size() { return 1; }

							@Override
                            public Object[] toArray() 
                            {
	                            DirectoryTree[] array = new DirectoryTree[1];
	                            array[0] = parent;
	                            return array;
                            }

							@SuppressWarnings("rawtypes")
                            @Override
                            public List toList() { return Arrays.asList(toArray()); }
			    			
			    		});
			    	}
		    	}
            }
		});
		
		m_viewer = new ServerDirectoryTreeViewer(top);
		m_viewer.getControl().setLayoutData( new GridData( GridData.FILL_BOTH ));
		m_viewer.setInput(m_tree);
		m_viewer.refresh();
		
		m_viewer.addSelectionChangedListener(this);

    	setErrorMessage("Must select a directory and provide a file name, or select an existing file.");

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
			m_path = null;
		}
		close();
	}

	@Override
    public void selectionChanged(SelectionChangedEvent event)
    {
    	IStructuredSelection isel = (IStructuredSelection) m_viewer.getSelection();
    	if (isel == null || isel.isEmpty())
		{
        	setErrorMessage("Must select a directory and provide a file name, or select an existing file.");
    		getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
    	else
    	{
	    	Object selected = isel.getFirstElement();
	    	if (selected instanceof DirectoryTree)
	    	{
	    		String filename = m_fileName.getText().trim();
	    		if (filename.isEmpty()) 
				{
	            	setErrorMessage("Must provide a file name, or select an existing file.");
	        		getButton(IDialogConstants.OK_ID).setEnabled(false);
				}
	    		else
	    		{
	    			setErrorMessage(null);
	        		getButton(IDialogConstants.OK_ID).setEnabled(true);
	        		DirectoryTree dir = (DirectoryTree) selected;
	            	m_path = dir.getPath() + "/" + filename;
		        	setMessage("File: '" + m_path + "'");
	    		}
	    	}
	    	else
	    	{
	        	setErrorMessage(null);
	        	m_path = ((DirectoryFile) selected).getAbsolutePath();
	        	m_fileName.setText(((DirectoryFile) selected).getFilename());
	        	setMessage("File: '" + m_path + "'");
        		getButton(IDialogConstants.OK_ID).setEnabled(true);
	    	}
    	}
    }
	
	public String getPath()
	{
		return m_path;
	}
}
