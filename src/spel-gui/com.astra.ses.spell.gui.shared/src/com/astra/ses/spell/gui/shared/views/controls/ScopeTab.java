///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.shared.views.controls
// 
// FILE      : ScopeTab.java
//
// DATE      : Oct 25, 2013
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
package com.astra.ses.spell.gui.shared.views.controls;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.shared.commands.ClearSharedVariablesJob;
import com.astra.ses.spell.gui.shared.commands.RefreshSharedVariablesJob;
import com.astra.ses.spell.gui.shared.messages.SharedDataOperation;
import com.astra.ses.spell.gui.shared.services.ISharedDataService;
import com.astra.ses.spell.gui.shared.services.ISharedScope;
import com.astra.ses.spell.gui.shared.views.SharedVariablesView;

public class ScopeTab extends TabItem implements ISelectionChangedListener
{
	private ISharedScope m_table;
	private SharedVariablesTableViewer m_viewer;
	private SharedVariablesView m_view;
	private Button m_btnNew;
	private Button m_btnDel;
	private Button m_btnClear;
	private Button m_btnRemove;
	private boolean m_monitoringMode;

	/***************************************************************************
	 * 
	 **************************************************************************/
	public ScopeTab(TabFolder parent, ISharedScope table, SharedVariablesView view, boolean monitoringMode)
    {
	    super(parent, SWT.NONE);

    	Logger.debug("Created tab: " + table.getScopeName(), Level.GUI, this);

	    m_table = table;
	    m_view = view;
	    m_monitoringMode = monitoringMode;

	    Composite top = new Composite(parent,SWT.NONE);
	    top.setLayout( new GridLayout( isGlobal() ? 4 : 5,true) );
	    top.setLayoutData( new GridData( GridData.FILL_BOTH ));
	    
	    m_viewer = new SharedVariablesTableViewer(top, table, monitoringMode);
	    GridData gd = new GridData( GridData.FILL_BOTH );
	    gd.horizontalSpan = isGlobal() ? 4 : 5;
		m_viewer.getControl().setLayoutData( gd );
		m_viewer.addSelectionChangedListener(this);
		
		
		if (isGlobal())
		{
			setText("GLOBAL");
		}
		else
		{
			setText(m_table.getScopeName());
		}

		m_btnNew = new Button(top, SWT.PUSH);
		m_btnNew.setText("New variable");
		m_btnNew.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		m_btnNew.setEnabled(false);
		m_btnNew.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected( SelectionEvent ev )
			{
				SharedVariableDialog dialog = new SharedVariableDialog(m_viewer.getControl().getShell(), true, "", "");
				if (dialog.open() == IDialogConstants.OK_ID)
				{
					String key = dialog.getKey();
					String value = dialog.getValue();
					if (!key.trim().isEmpty() && !value.trim().isEmpty())
					{
						m_table.set(dialog.getKey(), dialog.getValue());
						refresh();
					}
				}
			}
		});

		m_btnDel = new Button(top, SWT.PUSH);
		m_btnDel.setText("Delete variable");
		m_btnDel.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		m_btnDel.setEnabled(false);
		m_btnDel.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected( SelectionEvent ev )
			{
				IStructuredSelection sel = (IStructuredSelection) m_viewer.getSelection();
				if (!sel.isEmpty())
				{
					@SuppressWarnings("rawtypes")
                    Iterator it = sel.iterator();
					String toDeleteText = "Are you sure to delete the following shared variables?\n\n";
					List<SharedVariable> toDelete = new LinkedList<SharedVariable>();
					while(it.hasNext())
					{
						SharedVariable var = (SharedVariable) it.next();
						toDelete.add(var);
						toDeleteText += "  - " + var.name + "\n";
					}
					if (MessageDialog.openConfirm(m_viewer.getControl().getShell(), "Delete shared variables", toDeleteText))
					{
						ClearSharedVariablesJob job = new ClearSharedVariablesJob(m_table, toDelete);
						CommandHelper.executeInProgress(job, true, true);
						refresh();
					}
				}
			}
		});

		m_btnClear = new Button(top, SWT.PUSH);
		m_btnClear.setText("Remove all variables");
		m_btnClear.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		m_btnClear.setEnabled(false);
		m_btnClear.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected( SelectionEvent ev )
			{
				String text = "This action will remove all shared variables in the scope.\n\nDo you want to proceed?";
				if (MessageDialog.openConfirm(m_viewer.getControl().getShell(), "Delete shared variables", text))
				{
					ClearSharedVariablesJob job = new ClearSharedVariablesJob(m_table);
					CommandHelper.executeInProgress(job, true, true);
					refresh();
				}
			}
		});

		Button refreshScope = new Button(top, SWT.PUSH);
		refreshScope.setText("Refresh variables");
		refreshScope.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		refreshScope.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected( SelectionEvent ev )
			{
		    	Logger.debug("Refreshing tab: " + m_table.getScopeName(), Level.GUI, this);
				RefreshSharedVariablesJob job = new RefreshSharedVariablesJob(m_table);
				CommandHelper.executeInProgress(job, true, true);
				refresh();
			}
		});

		if (!isGlobal())
		{
			m_btnRemove = new Button(top, SWT.PUSH);
			m_btnRemove.setText("Remove scope");
			m_btnRemove.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
			m_btnRemove.setEnabled(false);
			m_btnRemove.addSelectionListener( new SelectionAdapter()
			{
				public void widgetSelected( SelectionEvent ev )
				{
					String text = "This action will completely remove the scope and its variables.\n\nDo you want to proceed?";
					if (MessageDialog.openConfirm(m_viewer.getControl().getShell(), "Delete shared variables", text))
					{
						ISharedDataService svc = (ISharedDataService) ServiceManager.get(ISharedDataService.class);
						svc.removeSharedScope(m_table.getScopeName());
						m_view.removeScope(m_table.getScopeName());
					}
				}
			});
		}
		
		if (!monitoringMode)
		{
			m_btnNew.setEnabled(true);
			m_btnClear.setEnabled(true);
			if (!isGlobal()) m_btnRemove.setEnabled(true);
		}
		
		setControl(top);
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void forceUpdate()
	{
		m_table.update( new NullProgressMonitor() );
		m_viewer.refresh();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void forceUpdate( SharedDataOperation operation, String[] varList, String[] valueList )
	{
		List<SharedVariable> added = new LinkedList<SharedVariable>();
		List<SharedVariable> updated = new LinkedList<SharedVariable>();
		List<SharedVariable> deleted = new LinkedList<SharedVariable>();
		
		m_table.update( operation, varList, valueList, added, updated, deleted );
		for(Object obj : added)
		{
			m_viewer.add(obj);
		}
		for(Object obj : updated)
		{
			m_viewer.update(obj, null);
		}
		for(Object obj : deleted)
		{
			m_viewer.remove(obj);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private boolean isGlobal()
	{
		return m_table.getScopeName().equals(ISharedDataService.GLOBAL_SCOPE);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void update( boolean enable, boolean monitoringMode )
	{
		m_monitoringMode = monitoringMode;
		if (monitoringMode)
		{
			m_btnNew.setEnabled(false);
			m_btnDel.setEnabled(false);
			m_btnClear.setEnabled(false);
			if (!isGlobal()) m_btnRemove.setEnabled(false);
		}
		else
		{
			m_btnNew.setEnabled(enable);
			m_btnDel.setEnabled( enable && !m_viewer.getSelection().isEmpty());
			m_btnClear.setEnabled(enable);
			if (!isGlobal()) m_btnRemove.setEnabled(enable);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	protected void checkSubclass(){};
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void refresh()
	{
		m_viewer.refresh();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void selectionChanged(SelectionChangedEvent event)
    {
		if (!m_monitoringMode)
		{
			m_btnDel.setEnabled(!event.getSelection().isEmpty());
		}
    }

}
