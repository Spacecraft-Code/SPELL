///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views
// 
// FILE      : TabbedView.java
//
// DATE      : 2008-11-24 08:34
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
package com.astra.ses.spell.gui.views;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.files.AsRunFile;
import com.astra.ses.spell.gui.core.model.files.IServerFile;
import com.astra.ses.spell.gui.core.model.files.LogFile;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.ServerFileType;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.model.jobs.GetAsRunFileJob;
import com.astra.ses.spell.gui.model.jobs.GetLogFileJob;
import com.astra.ses.spell.gui.services.IRuntimeSettings;
import com.astra.ses.spell.gui.services.IRuntimeSettings.RuntimeProperty;
import com.astra.ses.spell.gui.views.providers.TabbedViewContentProvider;
import com.astra.ses.spell.gui.views.providers.TabbedViewLabelProvider;

public class TabbedView extends ViewPart implements ControlListener
{
	/** Browser ID used by main plugin for preparing the perspective layout */
	public static final String	ID	= "com.astra.ses.spell.gui.views.TabbedView";

	/** Table viewer */
	private TableViewer	       m_viewer;
	/** Tabbed file to fill the table */
	private IServerFile	       m_tabbedFile;
	/** Holds the procedure id */
	private String	           m_procId;
	/** Server file type */
	private ServerFileType     m_type;


	/**************************************************************************
	 * The constructor.
	 *************************************************************************/
	public TabbedView()
	{
	}

	@Override
	public void createPartControl(Composite parent)
	{
		IRuntimeSettings runtime = (IRuntimeSettings) ServiceManager.get(IRuntimeSettings.class);
		m_procId = (String) runtime.getRuntimeProperty(RuntimeProperty.ID_PROCEDURE_SELECTION);
		m_viewer = new TableViewer(parent, SWT.VIRTUAL | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		
		m_type = ServerFileType.EXECUTOR_LOG;
		if (getViewSite().getSecondaryId().matches(".* - AS-RUN"))
		{
			m_type = ServerFileType.ASRUN;
		}

		String[] headerLabels = getTableHeaders();
		Table table = getTable();
		table.addControlListener(this);

		for (int i = 0; i < headerLabels.length; i++)
		{
			TableColumn col = new TableColumn(table, SWT.LEFT);
			col.setText(headerLabels[i]);
			col.setWidth(10); // Temporary
		}

		// Empty file
		ArrayList<String> lines = new ArrayList<String>();
		lines.add( "(No data)" );
		
		GridData viewerData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		getTable().setLayoutData(viewerData);
		getTable().setHeaderVisible(true);
		getTable().setLinesVisible(true);

		setPartName(getViewSite().getSecondaryId());
		setTitleToolTip(getViewSite().getSecondaryId());
		
		initContents();
		
		refreshView();
	}

	/**************************************************************************
	 * Get the table.
	 *************************************************************************/
	protected Table getTable()
	{
		return m_viewer.getTable();
	}

	/**************************************************************************
	 * Get the table headers
	 *************************************************************************/
	protected String[] getTableHeaders()
	{
		String[] headerLabels = null;
		switch(m_type)
		{
		case ASRUN:
			{
				headerLabels = new AsRunFile("", "", new ArrayList<String>() ).getHeaderLabels();
				break;
			}
		case EXECUTOR_LOG:
			{
				headerLabels = new LogFile("", "", new ArrayList<String>() ).getHeaderLabels();
				break;
			}
		}
		return headerLabels;
	}

	/**************************************************************************
	 * Get the table headers
	 *************************************************************************/
	protected int[] getTableHeaderSizes()
	{
		int[] headerSizes = null;
		switch(m_type)
		{
		case ASRUN:
			{
				headerSizes = new AsRunFile("", "", new ArrayList<String>() ).getHeaderLabelsSize();
				break;
			}
		case EXECUTOR_LOG:
			{
				headerSizes = new LogFile("", "", new ArrayList<String>() ).getHeaderLabelsSize();
				break;
			}
		}
		return headerSizes;
	}

	/**************************************************************************
	 * Get the table.
	 *************************************************************************/
	protected IServerFile getTabbedFile()
	{
		return m_tabbedFile;
	}

	/**************************************************************************
	 * Initialize contents
	 *************************************************************************/
	public void initContents()
	{
		m_tabbedFile = null;
		m_viewer.setContentProvider(new TabbedViewContentProvider());
		m_viewer.setLabelProvider(new TabbedViewLabelProvider());
	}

	@Override
	public void setFocus()
	{
	}

	/***************************************************************************
	 * Get table viewer
	 * 
	 * @return
	 **************************************************************************/
	protected TableViewer getViewer()
	{
		return m_viewer;
	}

	/***************************************************************************
	 * Get ProcId
	 * 
	 * @return
	 **************************************************************************/
	public String getProcId()
	{
		return m_procId;
	}

	@Override
	public void controlMoved(ControlEvent arg0)
	{
	}

	/***************************************************************************
	 * Dimension the columns
	 **************************************************************************/
	@Override
	public void controlResized(ControlEvent arg0)
	{
		Table table = getTable();

		int tableWidth = table.getClientArea().width;
		
		
		int[] headerLabelsSize = getTableHeaderSizes();

		int count = 0;
		for (TableColumn col : table.getColumns())
		{
			int width = (int) (tableWidth * (((double) headerLabelsSize[count]) / 100.0));
			col.setWidth(width);
			count++;
		}
	}

	/***************************************************************************
	 * Refresh this view's contents
	 **************************************************************************/
	public void refreshView()
	{
		try
		{
			switch(m_type)
			{
			case ASRUN:
				GetAsRunFileJob job = new GetAsRunFileJob(m_procId);
				CommandHelper.executeInProgress(job, true, true);
				if (job.result.equals(CommandResult.SUCCESS))
				{
					m_tabbedFile = (IServerFile) job.asRunFile;
				}
				else
				{
					if (job.error != null)
					{
						MessageDialog.openError(getSite().getShell(), "Unable to display ASRUN file", job.error.getLocalizedMessage());
					}
					else
					{
						MessageDialog.openError(getSite().getShell(), "Unable to display ASRUN file", "Retrieval failed");
					}
					return;
				}
				break;
			case EXECUTOR_LOG:
				GetLogFileJob ljob = new GetLogFileJob(m_procId);
				CommandHelper.executeInProgress(ljob, true, true);
				if (ljob.result.equals(CommandResult.SUCCESS))
				{
					m_tabbedFile = (IServerFile) ljob.logFile;
				}
				else
				{
					if (ljob.error != null)
					{
						MessageDialog.openError(getSite().getShell(), "Unable to display log file", ljob.error.getLocalizedMessage());
					}
					else
					{
						MessageDialog.openError(getSite().getShell(), "Unable to display log file", "Retrieval failed");
					}
					return;
				}
				break;
			}

			m_viewer.setInput(m_tabbedFile.getLines());
			m_viewer.getControl().setFocus();

			IRuntimeSettings runtime = (IRuntimeSettings) ServiceManager.get(IRuntimeSettings.class);
			runtime.setRuntimeProperty(RuntimeProperty.ID_PROCEDURE_SELECTION,m_procId);
		}
		catch (Exception ex)
		{
			Logger.warning("Error: cannot display the file: " + ex.getLocalizedMessage(), Level.PROC,this);
		}
	}
}
