///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs
// 
// FILE      : DictionaryEditorDialog.java
//
// DATE      : Feb 1, 2012
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.exceptions.ContextError;
import com.astra.ses.spell.gui.core.model.server.DirectoryFile;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.DataContainer;
import com.astra.ses.spell.gui.core.model.types.DataVariable;
import com.astra.ses.spell.gui.dialogs.controls.DictVariablesTable;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.model.commands.SaveDictionary;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.model.jobs.BuildDirectoryTreeJob;
import com.astra.ses.spell.gui.model.jobs.GetDictionaryJob;
import com.astra.ses.spell.gui.model.jobs.GetInputFileJob;
import com.astra.ses.spell.gui.model.jobs.UpdateDataContainerJob;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

/*******************************************************************************
 * @brief Dialog for dictionary edit
 ******************************************************************************/
public class DictionaryEditorDialog extends TitleAreaDialog implements ISelectionChangedListener, DictVariablesTable.IValueEditListener
{
	public static final String ID = "com.astra.ses.spell.gui.dialogs.DictionaryEditorDialog";


	/** Holds the dialog image icon */
	private Image m_image;
	/** Holds the bold font */
	private Font m_font;
	/** Holds the procedure identifier */
	private String m_procId;
	/** Holds the dictionary name */
	private String m_dictionaryName;
	/** Holds the dictionary model */
	private DataContainer m_dictionary;
	/** Holds the dictionary model */
	private DataContainer m_fileContainer;
	/** Holds the variable viewer */
	private DictVariablesTable m_varTable;
	/** Holds the file viewer */
	private DictVariablesTable m_fileTable;
	/** Holds the merge all button */
	private Button m_mergeAllButton;
	/** Holds the merge selected button */
	private Button m_mergeSelButton;
	/** Prompt on overwrite button */
	private Button m_chkOverwrite;
	/** Merge new button */
	private Button m_chkMergeNew;
	/** Allow merging files flag */
	private boolean m_canMergeFiles;
	/** Name of the loaded file */
	private Label m_loadedFile;
	/** Read only flag */
	private boolean m_readOnly;
	/** Filter controls */
	private Button m_clearBtn;
	/** Text filter */
	private Text m_filterText;
	/** Image for refresh button */
	private Image m_imgRefresh;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param shell
	 *            The parent shell
	 **************************************************************************/
	public DictionaryEditorDialog(Shell shell, IProcedure model, String containerName, boolean canMergeFiles ) throws ContextError
	{
		super(shell);
		// Obtain the image for the dialog icon
		ImageDescriptor descr = Activator.getImageDescriptor("icons/dlg_exec.png");
		m_image = descr.createImage();
		m_font = JFaceResources.getBannerFont();
		m_procId = model.getProcId();
		m_dictionaryName = containerName;
		m_canMergeFiles = canMergeFiles;
		
		m_varTable = null;
		m_fileTable = null;
		
		m_readOnly = (!model.getRuntimeInformation().getClientMode().equals(ClientMode.CONTROL));
		
		if (m_readOnly) m_canMergeFiles = false;

		loadData(false);
		
		m_fileContainer = new DataContainer(model.getProcId(), "");
	}

	/***************************************************************************
	 * Defines the shell characteristics
	 **************************************************************************/
	protected void loadData( boolean updateViewer ) throws ContextError
	{
		GetDictionaryJob job = new GetDictionaryJob( m_procId, m_dictionaryName );
		CommandHelper.executeInProgress(job, true, true);
		
		//Checking dictionary recovery result and sending except in case of fail
		if ( job.result.equals(CommandResult.FAILED) )
		{
			throw new ContextError(m_dictionaryName + " dictionary or data dictionary in " + m_procId + " is not available."); 
		}

		m_dictionary = job.dictionary;
		
		if (m_dictionary == null)
		{
			m_dictionary = new DataContainer(m_procId, m_dictionaryName);
		}
		
		if (updateViewer) m_varTable.setInput(m_dictionary);
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
		m_varTable.removeValueEditListener(this);
		m_image.dispose();
		m_imgRefresh.dispose();
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
		setMessage("Editing dictionary " + m_dictionary.getName() + " ( " + m_dictionary.getVariables().size() + " keys )");
		setTitle("Dictionary in procedure '" + m_procId + "'");
		setTitleImage(m_image);
		getShell().setText("Dictionary Editor");
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

		Composite dictGroup = new Composite(top, SWT.BORDER );
		dictGroup.setLayout( new GridLayout(1, true) );
		GridData dgdata = new GridData( GridData.FILL_BOTH );
		dgdata.minimumHeight = 250;
		dictGroup.setLayoutData(dgdata);
		
			Composite titleComp = new Composite(dictGroup, SWT.NONE);
			titleComp.setLayout( new GridLayout(2,false) );
			titleComp.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));
		
			Label title1 = new Label(titleComp, SWT.NONE);
			title1.setText("Variable values");
			title1.setFont(m_font);
			GridData gd = new GridData( GridData.FILL_HORIZONTAL );
			gd.grabExcessHorizontalSpace = true;
			title1.setLayoutData( gd );
			
			Button refreshBtn = new Button(titleComp, SWT.PUSH);
			m_imgRefresh = Activator.getImageDescriptor("icons/arrow_refresh.png").createImage();
			refreshBtn.setImage( m_imgRefresh );
			refreshBtn.addSelectionListener( new SelectionAdapter() 
			{
				public void widgetSelected( SelectionEvent ev )
				{
					loadData(true);
				}
			});
			
			Label sep1 = new Label(dictGroup, SWT.HORIZONTAL | SWT.SEPARATOR );
			sep1.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));
			
			int style = SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL;
			if (m_readOnly)
			{
				m_varTable = new DictVariablesTable(dictGroup,false, true, style | SWT.READ_ONLY);
			}
			else
			{
				m_varTable = new DictVariablesTable(dictGroup,false, false, style);
			}
			m_varTable.getControl().setLayoutData( new GridData( GridData.FILL_BOTH ));

	/////////////////////////////////////////////////////////////////////////////////////////
		
		if (!m_dictionary.isTyped() || m_canMergeFiles)
		{
			Composite buttons = new Composite(top, SWT.BORDER);
			buttons.setLayout( new RowLayout( SWT.HORIZONTAL ));
			buttons.setLayoutData(new GridData( GridData.FILL_HORIZONTAL ));
			
				Button btnOpen = new Button(buttons, SWT.PUSH);
				btnOpen.setText("Open file...");
				btnOpen.addSelectionListener( new SelectionAdapter()
				{
					public void widgetSelected( SelectionEvent ev )
					{
						doOpenFile();
					}
				}
				);

				Button btnSave = new Button(buttons, SWT.PUSH);
				btnSave.setText("Save to file...");
				btnSave.addSelectionListener( new SelectionAdapter()
				{
					public void widgetSelected( SelectionEvent ev )
					{
						doSaveFile();
					}
				}
				);

				m_mergeAllButton = new Button(buttons, SWT.PUSH);
				m_mergeAllButton.setText("Merge all");
				m_mergeAllButton.setEnabled(false);
				m_mergeAllButton.addSelectionListener( new SelectionAdapter()
				{
					public void widgetSelected( SelectionEvent ev )
					{
						doMergeAll();
					}
				}
				);
		
				m_mergeSelButton = new Button(buttons, SWT.PUSH);
				m_mergeSelButton.setText("Merge selected");
				m_mergeSelButton.setEnabled(false);
				m_mergeSelButton.addSelectionListener( new SelectionAdapter()
				{
					public void widgetSelected( SelectionEvent ev )
					{
						doMergeSelected();
					}
				}
				);
		
				m_chkOverwrite = new Button(buttons, SWT.CHECK);
				m_chkOverwrite.setText("Prompt before overwrite");
				m_chkOverwrite.setSelection(true);
	
				m_chkMergeNew = new Button(buttons, SWT.CHECK);
				m_chkMergeNew.setText("Merge new variables as well");
				m_chkMergeNew.setSelection(false);
	
	
		/////////////////////////////////////////////////////////////////////////////////////////
	
			m_loadedFile = new Label( top, SWT.NONE );
			m_loadedFile.setText("(no file loaded)");
			m_loadedFile.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));
				
			Composite fileGroup = new Composite(top, SWT.BORDER );
			fileGroup.setLayout( new GridLayout(1, true) );
			GridData fgdata = new GridData( GridData.FILL_BOTH );
			fgdata.minimumHeight = 250;
			fileGroup.setLayoutData(fgdata);
	
				Label title2 = new Label(fileGroup, SWT.NONE);
				title2.setText("Loaded file values");
				title2.setFont(m_font);
				title2.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));
				
				Label sep2 = new Label(fileGroup, SWT.HORIZONTAL | SWT.SEPARATOR );
				sep2.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));
				
				int fstyle = SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL;
				m_fileTable = new DictVariablesTable(fileGroup,true,m_readOnly,fstyle);
				m_fileTable.getControl().setLayoutData( new GridData( GridData.FILL_BOTH ));
	
		/////////////////////////////////////////////////////////////////////////////////////////

			m_fileTable.addSelectionChangedListener(this);
			m_fileTable.initialize(m_fileContainer);

			Composite filterComposite = new Composite(fileGroup, SWT.NONE);
			filterComposite.setLayout( new RowLayout(SWT.HORIZONTAL) );
			
			Label label = new Label(filterComposite, SWT.NONE);
			label.setText("Filter variables by name: ");
			
			m_filterText = new Text(filterComposite, SWT.BORDER);
			RowData rdata = new RowData();
			rdata.width = 200;
			m_filterText.setLayoutData(rdata);
			m_filterText.setEnabled(false);
			m_filterText.addModifyListener( new ModifyListener()
			{
				@Override
                public void modifyText(ModifyEvent e)
                {
					Text filterText = (Text) e.widget;
					String text = filterText.getText();
					if (text.isEmpty())
					{
						m_fileTable.filter(null);
						m_clearBtn.setEnabled(false);
					}
					else
					{
						m_fileTable.filter(text);
						m_clearBtn.setEnabled(true);
					}
                }
			}
			);
			
			m_clearBtn = new Button( filterComposite, SWT.PUSH );
			m_clearBtn.setText("Clear");
			m_clearBtn.setEnabled(false);
			m_clearBtn.addSelectionListener( new SelectionAdapter() 
			{
				public void widgetSelected( SelectionEvent ev )
				{
					m_filterText.setText("");
				}
			});
			
		}
			
		m_varTable.addSelectionChangedListener(this);
		m_varTable.addValueEditListener(this);
		m_varTable.initialize(m_dictionary);
			
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
					m_fileContainer = job2.container;
					m_fileTable.setInput(m_fileContainer);
					m_loadedFile.setText(file.getFilename() + " (" + job2.container.getVariables().size() + " variables)");
					m_filterText.setEnabled(true);
					m_filterText.setText("");
					update();
				}
				else
				{
					if (m_fileContainer != null) m_fileContainer.clear();
					m_filterText.setText("");
					m_filterText.setEnabled(false);
					String msg = "Failed to obtain server file: " + file.getFilename() + "\n" + job2.error;
					MessageDialog.openError(getShell(), "Obtain input file", msg );
				}
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void doSaveFile()
	{
		BuildDirectoryTreeJob job = new BuildDirectoryTreeJob();
		CommandHelper.executeInProgress(job, true, true);
		if (job.result.equals(CommandResult.SUCCESS))
		{
			ServerFileSaveDialog dialog = new ServerFileSaveDialog(getShell(), job.tree);
			dialog.open();
			
			String path = dialog.getPath();
			if (path != null)
			{
				Map<String,String> arguments = new HashMap<String,String>();
				arguments.put(SaveDictionary.ARG_PROCID,m_procId);
				arguments.put(SaveDictionary.ARG_DICTNAME,m_dictionaryName);
				arguments.put(SaveDictionary.ARG_PATH,path);
				CommandResult result = (CommandResult) CommandHelper.execute(SaveDictionary.ID, arguments);
				if (result != null && result.equals(CommandResult.SUCCESS))
				{
					MessageDialog.openInformation(getShell(), "File saved", "File '" + path + "' saved");
				}
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void mergeVariables( List<DataVariable> fileVars )
	{
		if (m_chkOverwrite.getSelection())
		{
			String overwrite = "";
			for(DataVariable fileVar : fileVars )
			{
				if (m_dictionary.hasVariable( fileVar.getName() ))
				{
					DataVariable var = m_dictionary.getVariable(fileVar.getName());
					String origValue = var.getValue();
					String newValue  = fileVar.getValue();
					if (!origValue.equals(newValue))
					{
						if (!overwrite.isEmpty()) overwrite += "\n";
						overwrite += "  - " + fileVar.getName();
					}
				}
			}
			if (!overwrite.isEmpty())
			{
				LongTextDialog dialog = new LongTextDialog(getShell(), 
						"Value overwrite", 
						"The value of the following variables will be changed.\n\nDo you want to continue?",
						overwrite, false);
				dialog.open();
				if (dialog.choice != IDialogConstants.OK_ID)
				{
					return;
				}
			}
		}

		String errors = "";
		int numMerged = 0;
		
		boolean mergeNew = m_chkMergeNew.getSelection();
		
		for(DataVariable fileVar : fileVars)
		{
			boolean varExists = m_dictionary.hasVariable( fileVar.getName() );

			if ( !mergeNew && !varExists ) continue;
			
			if (varExists)
			{
				DataVariable var = m_dictionary.getVariable(fileVar.getName());
				String newValue  = fileVar.getValue();
				try
				{
					var.setValue(newValue);
					numMerged++;
				}
				catch(Exception ex)
				{
					if (!errors.isEmpty()) errors += "\n";
					errors += "  - '" + fileVar.getName()+ "': " + ex.getLocalizedMessage();
				}
			}
			else
			{
				DataVariable copy = fileVar.copy();
				copy.markNew();
				m_dictionary.addVariable( fileVar.getName(), copy );
				numMerged++;
			}
		}
		if (!errors.isEmpty())
		{
			LongTextDialog dialog = new LongTextDialog(getShell(), 
					"Failed to change value", 
					"The value of the following variables could not be changed:",
					errors, true);
			dialog.open();
		}
		
		if (numMerged==0)
		{
			MessageDialog.openWarning(getShell(), "No changes made", 
					"No value has been changed in the data dictionary");
			return;
		}
		
		m_varTable.refresh();
		m_varTable.setSelection(null);
		m_fileTable.setSelection(null);
		update();
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private void doMergeAll()
	{
		mergeVariables( m_fileContainer.getVariables() );
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	@SuppressWarnings("unchecked")
    private void doMergeSelected()
	{
		List<DataVariable> selected = new ArrayList<DataVariable>();
		IStructuredSelection sel = (IStructuredSelection) m_fileTable.getSelection();
		if (!sel.isEmpty())
		{
			selected.addAll(sel.toList());
			mergeVariables( selected );
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private void doRevert()
	{
		if (MessageDialog.openConfirm(getShell(), "Revert changes", "Revert all changes made to variables?"))
		{
			m_varTable.setSelection(null);
			m_dictionary.revert();
			m_varTable.refresh();
			if (m_fileTable != null)
			{
				m_fileTable.setSelection(null);
			}
			update();
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void update()
	{
		if (m_canMergeFiles)
		{
			m_mergeSelButton.setEnabled(!m_fileContainer.getVariables().isEmpty() && !m_fileTable.getSelection().isEmpty());
			m_mergeAllButton.setEnabled(!m_fileContainer.getVariables().isEmpty());
			m_chkOverwrite.setEnabled(!m_fileContainer.getVariables().isEmpty());
			m_chkMergeNew.setEnabled(!m_fileContainer.getVariables().isEmpty());
		}
		getButton(IDialogConstants.OK_ID).setEnabled(m_dictionary.hasChanges());
		getButton(IDialogConstants.BACK_ID).setEnabled(m_dictionary.hasChanges());
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
		if (!m_readOnly)
		{
			createButton(parent, IDialogConstants.BACK_ID, "Revert Changes", false);
			createButton(parent, IDialogConstants.OK_ID, "Apply and Close", false);
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			getButton(IDialogConstants.BACK_ID).setEnabled(false);
		}
		createButton(parent, IDialogConstants.CANCEL_ID, "Close", true);
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
			if (m_dictionary.hasChanges())
			{
				boolean mergeNew = false;
				// The button can be null for dialogs without file import
				if (m_chkMergeNew != null)
				{
					m_chkMergeNew.getSelection();
				}
				UpdateDataContainerJob job = new UpdateDataContainerJob( m_procId, m_dictionary, mergeNew );
				CommandHelper.executeInProgress(job, true, true);
				m_varTable.refresh();
				if (job.result == CommandResult.FAILED )
				{
					MessageDialog.openError(getShell(), "Update Error", "Failed to update changes to dictionary:\n" +
					                        job.error );
					return;
				}
			}
		}
		else if (buttonId == IDialogConstants.BACK_ID)
		{
			doRevert();
			return;
		}
		else if (buttonId == IDialogConstants.CANCEL_ID)
		{
			if (m_dictionary.hasChanges())
			{
				if (!MessageDialog.openConfirm(getShell(), "Changes made", "There are changes made to the data dictionary variables.\n\n" +
			        "If you close the dialog, these changes will be lost. "))
				return;
			}
			if (!m_fileContainer.getVariables().isEmpty())
			{
				if (!MessageDialog.openConfirm(getShell(), "File loaded", "There are NO changes made to the data dictionary variables, but an input file was loaded.\n\n" +
				        "If you close the dialog, no changes will be applied. Are you sure to close?"))
					return;
			}
		}
		close();
	}

	@Override
    public void selectionChanged(SelectionChangedEvent event)
    {
	    if (event.getSource() == m_fileTable)
	    {
    		m_mergeSelButton.setEnabled(!event.getSelection().isEmpty());
	    }
    }

	@Override
    public void valueChanged()
    {
	    update();
    }
}
