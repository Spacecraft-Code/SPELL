////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.watchvariables.views.controls
// 
// FILE      : WatchVariablesPage.java
//
// DATE      : Sep 22, 2010 10:27:15 AM
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
////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.watchvariables.views.controls;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.Page;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.extensions.GuiNotifications;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureStatusListener;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.watchvariables.Activator;
import com.astra.ses.spell.gui.watchvariables.commands.ChangeVariable;
import com.astra.ses.spell.gui.watchvariables.commands.args.IWatchCommandArgument;
import com.astra.ses.spell.gui.watchvariables.dialogs.VariableDetailDialog;
import com.astra.ses.spell.gui.watchvariables.interfaces.IVariableManager;
import com.astra.ses.spell.gui.watchvariables.interfaces.IVariableView;
import com.astra.ses.spell.gui.watchvariables.interfaces.IWatchVariables;
import com.astra.ses.spell.gui.watchvariables.jobs.FormatVariableValueJob;
import com.astra.ses.spell.gui.watchvariables.jobs.UpdateVariablesJob;
import com.astra.ses.spell.gui.watchvariables.model.WatchVariablesContentProvider;
import com.astra.ses.spell.gui.watchvariables.model.WatchVariablesLabelProvider;
import com.astra.ses.spell.gui.watchvariables.model.WatchVariablesTableColumns;
import com.astra.ses.spell.gui.watchvariables.notification.VariableData;
import com.astra.ses.spell.gui.watchvariables.notification.VariableNotification;

/*******************************************************************************
 * 
 * Variables page shows the existing variables inside an execution scope
 * 
 ******************************************************************************/
public class WatchVariablesPage extends Page implements ISelectionProvider, IVariableView, IGuiProcedureStatusListener
{

	/***************************************************************************
	 * 
	 * {@link IWatchVariablesPageListener} implementing objects are notified
	 * whenever a page state becomes active/inactive
	 * 
	 **************************************************************************/
	public interface IWatchVariablesPageListener
	{
		/***********************************************************************
		 * Notify that this page becomes active/inactive
		 * 
		 * @param active
		 **********************************************************************/
		public void notifyActive(IPage page, boolean active);
	}

	/** Procedure identifier */
	private IProcedure m_proc;
	/** Table viewer for showing variable values */
	private TableViewer m_viewer;
	/** Enable/Disable */
	private Button m_btnEnable;
	/** Globals checkbox */
	private Button m_chkGlobals;
	/** Locals checkbox */
	private Button m_chkLocals;
	/** Manual Refresh */
	private Button m_btnRefresh;
	/** Top composite */
	private Composite m_top;
	/** Variable manager */
	private IVariableManager m_manager;
	/** Content provider */
	private WatchVariablesContentProvider m_contentProvider;
	/** Refresh image */
	private Image m_refreshImg;

    private VariableViewerComparator m_comparator;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param procId
	 *            the procedure identifier which this page is showing the
	 *            variables
	 **************************************************************************/
	public WatchVariablesPage( IProcedure proc, IWatchVariablesPageListener listener)
	{
		m_proc = proc;
		m_refreshImg = Activator.getImageDescriptor("icons/16x16/refresh.png").createImage();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public String getProcId()
	{
		return m_proc.getProcId();
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void createControl(Composite parent)
	{
		m_top = new Composite(parent, SWT.BORDER);
		m_top.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridLayout gLayout = new GridLayout();
		gLayout.numColumns = 1;
		gLayout.marginTop = 0;
		m_top.setLayout(gLayout);

		m_viewer = new TableViewer(m_top, SWT.MULTI | SWT.BORDER);
		m_contentProvider = new WatchVariablesContentProvider();
		m_viewer.setContentProvider(m_contentProvider);
		m_viewer.setLabelProvider(new WatchVariablesLabelProvider());
		m_viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		m_viewer.getTable().addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseDoubleClick(MouseEvent e)
			{
				IStructuredSelection sel = (IStructuredSelection) m_viewer.getSelection();
				VariableData var = (VariableData) sel.getFirstElement();
				FormatVariableValueJob job = new FormatVariableValueJob( var );
				CommandHelper.executeInProgress(job, true, true);
				if (job.result.equals(CommandResult.SUCCESS))
				{
					VariableDetailDialog dialog = new VariableDetailDialog( getSite().getShell(), var.getName(), job.details );
					dialog.open();
				}
			}
		});
		
		IWatchVariables watch = (IWatchVariables) ServiceManager.get(IWatchVariables.class);
		m_manager = watch.getVariableManager(m_proc.getProcId());
		
        m_comparator = new VariableViewerComparator();
		
        createColumns();
		createOptions(m_top);

		Logger.debug("Assign variable manager", Level.PROC, this);
		
		m_viewer.setInput(m_manager);

		m_manager.addListener(this);
		GuiNotifications.get().addListener(this, IGuiProcedureStatusListener.class);
		
		// Make cells editable
		setCellEditors();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void dispose()
	{
		super.dispose();
		m_refreshImg.dispose();
		m_manager.removeListener(this);
		GuiNotifications.get().removeListener(this);
	}


	/**************************************************************************
	 * Prepare the viewer for editing the elements
	 *************************************************************************/
	private void setCellEditors()
	{
		/*
		 * Attach cell modifier
		 */
		m_viewer.setCellModifier(new ICellModifier()
		{
			@Override
			public void modify(Object element, String property, Object value)
			{
				VariableData var = null;
				if (element instanceof Item)
				{
					var = (VariableData) ((TableItem) element).getData();
				}
				else
				{
					var = (VariableData) element;
				}
				String valueExpression = value.toString();

				/*
				 * If value has not change then exit
				 */
				if (var.getValue().equals(valueExpression))
				{
					return;
				}

				/*
				 * Prepare command arguments
				 */
				HashMap<String, String> args = new HashMap<String, String>();
				args.put(IWatchCommandArgument.PROCEDURE_ID, m_proc.getProcId());
				args.put(IWatchCommandArgument.VARIABLE_NAME, var.getName());
				args.put(IWatchCommandArgument.VARIABLE_VALUE_EXPR, valueExpression);
				args.put(IWatchCommandArgument.VARIABLE_GLOBAL, Boolean.toString(var.isGlobal()));
				/*
				 * Execute the command
				 */
				CommandHelper.execute(ChangeVariable.ID, args);
			}

			@Override
			public Object getValue(Object element, String property)
			{
				VariableData data = (VariableData) element;
				if (property.equals(WatchVariablesTableColumns.NAME_COLUMN.name()))
				{
					return data.getName();
				}
				else if (property.equals(WatchVariablesTableColumns.VALUE_COLUMN.name()))
				{
					return data.getValue();
				}
				return null;
			}

			@Override
			public boolean canModify(Object element, String property)
			{
				return property.equals(WatchVariablesTableColumns.VALUE_COLUMN.name());
			}
		});
		/*
		 * Set column properties
		 */
		String[] properties = new String[WatchVariablesTableColumns.values().length];
		for (WatchVariablesTableColumns column : WatchVariablesTableColumns.values())
		{
			properties[column.ordinal()] = column.name();
		}
		m_viewer.setColumnProperties(properties);
		/*
		 * Set cell editors
		 */
		CellEditor[] cellEditors = new CellEditor[WatchVariablesTableColumns.values().length];
		cellEditors[WatchVariablesTableColumns.NAME_COLUMN.ordinal()] = null;
		cellEditors[WatchVariablesTableColumns.VALUE_COLUMN.ordinal()] = new TextCellEditor(m_viewer.getTable());
		m_viewer.setCellEditors(cellEditors);
        m_viewer.setComparator(m_comparator);
	}

	/**************************************************************************
	 * Create the viewer columns
	 *************************************************************************/
	private void createColumns()
	{
		Table table = m_viewer.getTable();
        int index = 0;
		for (WatchVariablesTableColumns column : WatchVariablesTableColumns.values())
		{
			TableColumn col = new TableColumn(table, SWT.NONE);
			col.setText(column.text);
			col.setAlignment(column.alignment);
			col.setWidth(column.width);
            col.addSelectionListener(getSelectionAdapter(col, index));
            Logger.debug("--- ADDING " + column.text + " COLUMN ---", Level.PROC, this);
            index++;
		}
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}

	/**************************************************************************
	 * Add SelectionAdapter to Columns
	 *************************************************************************/

    private SelectionAdapter getSelectionAdapter(final TableColumn column, final int index){
        SelectionAdapter selectionAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e){
                Logger.debug("--- REFRESHING VARIABLE LIST ---", Level.PROC, this);
                m_comparator.setColumn(index);
                int dir = m_comparator.getDirection();
                m_viewer.getTable().setSortDirection(dir);
                m_viewer.getTable().setSortColumn(column);
                m_viewer.refresh();
            }
        };
        return selectionAdapter;
    }
            
	/**************************************************************************
	 * Create the extra controls
	 *************************************************************************/
	private void createOptions(Composite parent)
	{
		Composite group = new Composite(parent, SWT.NONE);
		group.setLayout(new RowLayout(SWT.HORIZONTAL));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		m_btnEnable = new Button(group, SWT.CHECK);
		m_btnEnable.setText("Auto");
		m_btnEnable.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				boolean enabled = m_btnEnable.getSelection();
				m_manager.setEnabled(enabled);
				if (enabled)
				{
					updateModel();
				}
				updateMechanismStatus();
			}
		});
		
		m_chkGlobals = new Button(group, SWT.CHECK);
		m_chkGlobals.setText("Globals");
		m_chkGlobals.setSelection(true);
		m_chkGlobals.setToolTipText("Show global variables");
		m_chkGlobals.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				showGlobals(m_chkGlobals.getSelection());
			}

		});

		m_chkLocals = new Button(group, SWT.CHECK);
		m_chkLocals.setText("Locals");
		m_chkLocals.setSelection(true);
		m_chkLocals.setToolTipText("Show global variables");
		m_chkLocals.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				showLocals(m_chkLocals.getSelection());
			}

		});
		
		m_btnRefresh = new Button(group, SWT.PUSH);
		m_btnRefresh.setImage(m_refreshImg);
		m_btnRefresh.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				updateModel();
			}

		});

		updateMechanismStatus();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public Control getControl()
	{
		return m_top;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void setFocus()
	{
		m_top.setFocus();
	}

	/**************************************************************************
	 * Refresh the page contents.
	 *************************************************************************/
	public void updateModel()
	{
		switch(m_proc.getRuntimeInformation().getStatus())
		{
		case PAUSED:
		case WAITING:
		case PROMPT:
		case FINISHED:
		case ABORTED:
			break;
		default:
			m_viewer.getControl().setEnabled(false);
			return;
		}
		Logger.debug("Updating model", Level.PROC, this);
		UpdateVariablesJob job = new UpdateVariablesJob(m_manager);
		try
		{
			m_viewer.getControl().setEnabled(false);
			CommandHelper.executeInProgress(job, true, false);
			if (job.result.equals(CommandResult.SUCCESS))
			{
				m_viewer.refresh();
			}
		}
		finally
		{
			m_viewer.getControl().setEnabled(true);
		}
		Logger.debug("Updating model done", Level.PROC, this);
	}

	/**************************************************************************
	 * Refresh the enable status
	 *************************************************************************/
	public void updateMechanismStatus()
	{
		if (m_manager.isEnabled())
		{
			m_btnEnable.setSelection(true);
			m_btnRefresh.setEnabled(false);
		}
		else
		{
			m_btnRefresh.setEnabled(true);
			m_btnEnable.setSelection(false);
		}
	}

	/**************************************************************************
	 * Set show mode.
	 * 
	 * @param mode
	 *************************************************************************/
	public void showGlobals( boolean show )
	{
		m_contentProvider.showGlobals(show);
		m_viewer.refresh();
	}

	/**************************************************************************
	 * Set show mode.
	 * 
	 * @param mode
	 *************************************************************************/
	public void showLocals( boolean show )
	{
		m_contentProvider.showLocals(show);
		m_viewer.refresh();
	}

	/***************************************************************************
	 * Check if this page is active A page is considered active when its
	 * associated {@link IProcedure} object status is different from FINISHED,
	 * ABORTED or ERROR
	 * 
	 * @return
	 **************************************************************************/
	public boolean isActive()
	{
		return m_chkGlobals.isEnabled();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public void cleanup()
	{
		IWatchVariables watch = (IWatchVariables) ServiceManager.get(IWatchVariables.class);
		watch.removeVariableManager(m_proc.getProcId());
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener)
	{
		m_viewer.addSelectionChangedListener(listener);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public ISelection getSelection()
	{
		return m_viewer.getSelection();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener)
	{
		m_viewer.removeSelectionChangedListener(listener);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void setSelection(ISelection selection)
	{
		m_viewer.setSelection(selection);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void variablesChanged(final List<VariableData> added, final List<VariableData> updated, final List<VariableData> removed)
    {
		Display.getDefault().syncExec( new Runnable(){
			
			public void run()
			{
				m_viewer.add(added.toArray());
				m_viewer.remove(removed.toArray());
				m_viewer.update(updated.toArray(),null);
			}
		});
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void scopeChanged( final VariableNotification data )
    {
		Display.getDefault().syncExec( new Runnable(){
			
			public void run()
			{
				m_viewer.refresh();
			}
		});
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void connectionLost()
    {
		Display.getDefault().syncExec( new Runnable(){
			
			public void run()
			{
				m_viewer.refresh();
			}
		});
    }

	@Override
    public void notifyStatus(IProcedure model, StatusNotification data)
    {
		updateMechanismStatus();
    }

	@Override
    public void notifyError(IProcedure model, ErrorData data) {}

	@Override
    public String getListenerId()
    {
	    return "Watch Variables Page";
    }

}
