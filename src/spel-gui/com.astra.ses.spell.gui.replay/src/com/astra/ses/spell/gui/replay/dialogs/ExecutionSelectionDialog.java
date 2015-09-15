///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.replay.dialogs
// 
// FILE      : ExecutionSelectionDialog.java
//
// DATE      : Jun 20, 2013
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
package com.astra.ses.spell.gui.replay.dialogs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import com.astra.ses.spell.gui.core.interfaces.IFileManager;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.files.AsRunFile;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.ServerFileType;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.model.jobs.ExportAsRunFileJob;
import com.astra.ses.spell.gui.model.jobs.SaveAsRunFileJob;
import com.astra.ses.spell.gui.replay.Activator;
import com.astra.ses.spell.gui.replay.commands.OpenProcedureReplay;
import com.astra.ses.spell.gui.replay.dialogs.execution.ExecutionSelectionLeafNode;
import com.astra.ses.spell.gui.replay.dialogs.execution.IExecutionSelectionModel;
import com.astra.ses.spell.gui.replay.dialogs.execution.name.ExecutionSelectionModelName;
import com.astra.ses.spell.gui.replay.dialogs.execution.name.ExecutionSelectionViewerName;
import com.astra.ses.spell.gui.replay.dialogs.execution.time.ExecutionSelectionModelTime;
import com.astra.ses.spell.gui.replay.dialogs.execution.time.ExecutionSelectionViewerTime;

/*******************************************************************************
 * @brief
 ******************************************************************************/
public class ExecutionSelectionDialog extends TitleAreaDialog implements ISelectionChangedListener
{
	public static final String ID = "com.astra.ses.spell.gui.replay.dialogs.ExecutionSelectionDialog";

	/** Reference to the file manager */
	private static IFileManager s_fmgr = null;
	private static String s_asrunPath = null;
	private static final int SAVE_ID = 990099;
	/** Holds the dialog image icon */
	private Image m_image;
	/** Holds the data model */
	private IExecutionSelectionModel m_modelTime;
	private IExecutionSelectionModel m_modelName;
	/** Holds the tree viewer by time */
	private TreeViewer m_viewerTime;
	/** Holds the tree viewer by name */
	private TreeViewer m_viewerName;
	/** Holds the mapping of procedure identifiers and names */
	private Map<String,String> m_procNames;
	/** Holds the stack */
	private StackLayout m_stack;
	private Composite m_nameBase;
	private Composite m_timeBase;
	private Composite m_base;
	/** Holds the currently selected node */
	private ExecutionSelectionLeafNode m_selection = null;

	
	/***************************************************************************
	 * Constructor
	 * 
	 * @param shell
	 *            The parent shell
	 **************************************************************************/
	public ExecutionSelectionDialog(Shell shell)
	{
		super(shell);
		if (s_fmgr == null)
		{
			s_fmgr = (IFileManager) ServiceManager.get(IFileManager.class);
			s_asrunPath = s_fmgr.getServerFilePath(ServerFileType.ASRUN, new NullProgressMonitor());
		}
		m_procNames = new TreeMap<String,String>();
		// Obtain the image for the dialog icon
		ImageDescriptor descr = Activator.getImageDescriptor("icons/dlg_detail.png");
		m_image = descr.createImage();
		m_modelTime = new ExecutionSelectionModelTime( s_asrunPath );
		m_modelName = new ExecutionSelectionModelName( s_asrunPath );
	}

	/***************************************************************************
	 * Defines the shell characteristics
	 **************************************************************************/
	@Override
	protected void setShellStyle(int newShellStyle)
	{
		super.setShellStyle(SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE);
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
		setMessage("Select the procedure execution to be replayed");
		setTitle("Selection of procedure execution");
		setTitleImage(m_image);
		getShell().setText("Execution Selection");
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

		List<String> list = s_fmgr.getAsRunFileList();

		// Load the model data
		m_modelTime.load(list,m_procNames);
		m_modelName.load(list,m_procNames);

		// Create the controls to switch between the two viewers
		Composite control = new Composite(top,SWT.BORDER);
		control.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));
		control.setLayout( new GridLayout(2,true) );
		Button btn1 = new Button(control,SWT.RADIO );
		btn1.setText("Sort by time");
		btn1.setSelection(true);
		btn1.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected( SelectionEvent event )
			{
				showTimeViewer();
			}
		});
		
		Button btn2 = new Button(control,SWT.RADIO );
		btn2.setText("Sort by procedure");
		btn2.setSelection(false);
		btn2.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected( SelectionEvent event )
			{
				showNameViewer();
			}
		});
		
		// Prepare the stack composites holding both viewers
		m_base = new Composite(top,SWT.BORDER);
		m_base.setLayoutData( new GridData( GridData.FILL_BOTH ));

		m_timeBase = new Composite(m_base,SWT.NONE);
		m_timeBase.setLayout( new GridLayout(1,true) );

		m_nameBase = new Composite(m_base,SWT.NONE);
		m_nameBase.setLayout( new GridLayout(1,true) );

		m_stack = new StackLayout();
		m_base.setLayout(m_stack);
		// Initially set to time viewer
		m_stack.topControl = m_timeBase;

		// Create the time tree and the associated viewer
		Tree timeTree = new Tree(m_timeBase, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION );
		timeTree.setHeaderVisible(true);
		timeTree.setLinesVisible(true);

		m_viewerTime = new ExecutionSelectionViewerTime(timeTree);
		m_viewerTime.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		m_viewerTime.setInput(m_modelTime);

		// Create the name tree and the associated viewer
		Tree nameTree = new Tree(m_nameBase, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION );
		nameTree.setHeaderVisible(true);
		nameTree.setLinesVisible(true);

		m_viewerName = new ExecutionSelectionViewerName(nameTree);
		m_viewerName.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		m_viewerName.setInput(m_modelName);

		// Layout the controls in the stack
		m_base.layout();
		
		// Add this dialog as selection listener
		m_viewerTime.addSelectionChangedListener(this);
		m_viewerName.addSelectionChangedListener(this);

		return parent;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void showTimeViewer()
	{
		m_stack.topControl = m_timeBase;
		m_base.layout();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void showNameViewer()
	{
		m_stack.topControl = m_nameBase;
		m_base.layout();
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
		createButton(parent, IDialogConstants.OK_ID, "Replay", false);
		createButton(parent, SAVE_ID, "Save as...", false);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		getButton(SAVE_ID).setEnabled(false);
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
		close();
		if (buttonId == IDialogConstants.OK_ID)
		{
			if (m_selection != null)
			{
				HashMap<String, String> args = new HashMap<String, String>();
				args.put(OpenProcedureReplay.ARG_ASRUN, m_selection.getASRUN());
				CommandHelper.execute(OpenProcedureReplay.ID, args);
			}
		}
		else if (buttonId == SAVE_ID)
		{
			if (m_selection != null)
			{
				ExportAsRunFileJob job = new ExportAsRunFileJob(m_selection.getASRUN());
				CommandHelper.executeInProgress(job, true, true);
				List<AsRunFile> toExport = new LinkedList<AsRunFile>();
				
				if (job.result.equals(CommandResult.SUCCESS))
				{
					toExport.add(job.asrunFile);
					if (!job.asrunFile.getChildren().isEmpty())
					{
						boolean alsoChildren = MessageDialog.openQuestion(getParentShell(), "Save children ASRUN files", "This procedure has executed sub-procedures.\n\nDo you want to export these ASRUN files as well?");
						if (alsoChildren)
						{
							gatherChildAsRunFiles(job.asrunFile, toExport);
						}
					}
				}
				
				DirectoryDialog dialog = new DirectoryDialog(getParentShell(), SWT.SAVE );
				dialog.setMessage("Select directory to export ASRUN file(s)");
				dialog.setText("Save ASRUN");
				String destination = dialog.open();
				if (destination != null && !destination.isEmpty())
				{
					SaveAsRunFileJob saveJob = new SaveAsRunFileJob(destination, toExport);
					CommandHelper.executeInProgress(saveJob, true, true);
				}
			}
		}
	}

	/***************************************************************************
	 * Iterate over all ASRUN files for children
	 **************************************************************************/
	private void gatherChildAsRunFiles( AsRunFile parent, List<AsRunFile> list )
	{
		Logger.debug("Fetching child ASRUN files for " + parent.getProcId(), Level.PROC, this);
		for(String childFile : parent.getChildren())
		{
			Logger.debug("   - Get child ASRUN file: " + childFile, Level.PROC, this);
			ExportAsRunFileJob childJob = new ExportAsRunFileJob( childFile );
			CommandHelper.executeInProgress(childJob, true, true);
			if (childJob.result.equals(CommandResult.SUCCESS))
			{
				list.add(childJob.asrunFile);
				Logger.debug("   - ASRUN file has children: " + childJob.asrunFile.getChildren().size(), Level.PROC, this);
				if (!childJob.asrunFile.getChildren().isEmpty())
				{
					gatherChildAsRunFiles(childJob.asrunFile, list);
				}
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void selectionChanged(SelectionChangedEvent event)
	{
		IStructuredSelection sel = (IStructuredSelection) event.getSelection();
		if (sel.isEmpty())
		{
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			getButton(SAVE_ID).setEnabled(false);
		}
		else
		{
			if (sel.getFirstElement() instanceof ExecutionSelectionLeafNode)
			{
				m_selection = (ExecutionSelectionLeafNode) sel.getFirstElement();
				getButton(IDialogConstants.OK_ID).setEnabled(true);
				getButton(SAVE_ID).setEnabled(true);
			}
			else
			{
				m_selection = null;
				getButton(IDialogConstants.OK_ID).setEnabled(false);
				getButton(SAVE_ID).setEnabled(false);
			}
		}
	}
}
