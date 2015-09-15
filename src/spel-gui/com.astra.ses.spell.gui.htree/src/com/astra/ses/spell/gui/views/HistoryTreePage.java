///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views
// 
// FILE      : HistoryTreePage.java
//
// DATE      : Sep 5, 2013
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
package com.astra.ses.spell.gui.views;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Menu;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.files.AsRunFile;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.model.htree.HistoryTreeChildProcedureNode;
import com.astra.ses.spell.gui.model.htree.HistoryTreeContentProvider;
import com.astra.ses.spell.gui.model.htree.HistoryTreeLabelProvider;
import com.astra.ses.spell.gui.model.htree.HistoryTreeRootNode;
import com.astra.ses.spell.gui.model.jobs.ExportAsRunFileJob;
import com.astra.ses.spell.gui.model.jobs.SaveAsRunFileJob;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.replay.commands.OpenProcedureReplay;
import com.astra.ses.spell.gui.services.IViewManager;

public class HistoryTreePage extends Composite
{
	private static IProcedureManager s_pmgr = null;
	private static IViewManager s_vmgr = null;
	
	private IProcedure m_model;
	
	static 
	{
		s_pmgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
		s_vmgr = (IViewManager) ServiceManager.get(IViewManager.class);
	}
	
	public HistoryTreePage( String procId, Composite parent, HistoryTreeView view )
	{
		super(parent, SWT.NONE);
		setLayout( new GridLayout(1,true) );
		
		m_model = s_pmgr.getProcedure(procId);
		
		TreeViewer viewer = new TreeViewer(this);
		viewer.getControl().setLayoutData( new GridData( GridData.FILL_BOTH ));
		viewer.setContentProvider(new HistoryTreeContentProvider());
		viewer.setLabelProvider(new HistoryTreeLabelProvider());
		viewer.setInput(m_model);
		viewer.addDoubleClickListener( new IDoubleClickListener()
		{
			@Override
			public void doubleClick(DoubleClickEvent event)
			{
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				Object obj = sel.getFirstElement();
				String instanceId = null;
				if (obj instanceof HistoryTreeRootNode)
				{
					HistoryTreeRootNode node = (HistoryTreeRootNode) obj;
					instanceId = node.getInstanceId();
				}
				else if (obj instanceof HistoryTreeChildProcedureNode)
				{
					HistoryTreeChildProcedureNode node = (HistoryTreeChildProcedureNode) obj;
					instanceId = node.getInstanceId();
				}
				if (instanceId != null)
				{
					if (s_vmgr.containsProcedureView(instanceId))
					{
						s_vmgr.showProcedureView(instanceId);
					}
					else
					{
						HistoryTreeChildProcedureNode node = (HistoryTreeChildProcedureNode) obj;
						HashMap<String, String> args = new HashMap<String, String>();
						args.put(OpenProcedureReplay.ARG_ASRUN, node.getAsRunPath());
						CommandHelper.execute(OpenProcedureReplay.ID, args);
					}
				}
			}
		});
		ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);
		viewer.expandAll();
		
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() 
		{
			public void menuAboutToShow(IMenuManager manager) 
			{
				Action exportAsRunTree = new Action () 
				{
					public void run() 
					{
						HistoryTreePage.this.exportAsRunTree();
					}
				};
				exportAsRunTree.setText("Export ASRUN...");
				manager.add(exportAsRunTree);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		view.getSite().registerContextMenu(menuMgr, viewer);
	}
	
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private void exportAsRunTree()
	{
		ExportAsRunFileJob job = new ExportAsRunFileJob(m_model);
		CommandHelper.executeInProgress(job, true, true);
		List<AsRunFile> toExport = new LinkedList<AsRunFile>();
		
		if (job.result.equals(CommandResult.SUCCESS))
		{
			toExport.add(job.asrunFile);
			if (!job.asrunFile.getChildren().isEmpty())
			{
				boolean alsoChildren = MessageDialog.openQuestion(getShell(), "Save children ASRUN files", "This procedure has executed sub-procedures.\n\nDo you want to export these ASRUN files as well?");
				if (alsoChildren)
				{
					gatherChildAsRunFiles(job.asrunFile, toExport);
				}
			}
		}
		
		DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SAVE );
		dialog.setMessage("Select directory to export ASRUN file(s)");
		dialog.setText("Save ASRUN");
		String destination = dialog.open();
		if (destination != null && !destination.isEmpty())
		{
			SaveAsRunFileJob saveJob = new SaveAsRunFileJob(destination, toExport);
			CommandHelper.executeInProgress(saveJob, true, true);
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

}
