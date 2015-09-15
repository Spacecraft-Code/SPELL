///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs.controls
// 
// FILE      : DictVariablesTable.java
//
// DATE      : 2008-11-21 08:55
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
package com.astra.ses.spell.gui.dialogs.controls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

import com.astra.ses.spell.gui.core.model.types.DataContainer;

/*******************************************************************************
 * @brief
 * @date 
 ******************************************************************************/
public class DictVariablesTable extends TableViewer 
{
	public interface IValueEditListener
	{
		public void valueChanged();
	}

	private DictVariablesFilter m_filter;
	private boolean m_simpleTable;
	private boolean m_readOnly;
	private List<IValueEditListener> m_listeners;
	private DictVariablesColumnComparator m_comparator;
	
	/***************************************************************************
	 * Constructor.
	 * 
	 * @param procId
	 *            The procedure identifier
	 * @param parent
	 *            The parent composite
	 **************************************************************************/
	public DictVariablesTable( Composite parent, boolean simple, boolean readOnly, int style )
	{
		super(parent, style );
		m_simpleTable = simple;
		m_readOnly = readOnly;
		m_filter = new DictVariablesFilter();
		m_listeners = new ArrayList<IValueEditListener>();
		m_comparator = new DictVariablesColumnComparator(m_simpleTable);
		getTable().setHeaderVisible(true);
		getTable().setLinesVisible(true);
		createColumns();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void initialize( DataContainer container )
	{
		setContentProvider( new DictVariablesContentProvider() );
		setLabelProvider( new DictVariablesLabelProvider() );
		setInput(container);
		addFilter(m_filter);
		setComparator( m_comparator );
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void filter(String text)
	{
		m_filter.setSearchText(text);
		refresh();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void addValueEditListener( IValueEditListener listener )
	{
		if (!m_listeners.contains(listener)) m_listeners.add(listener);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void removeValueEditListener( IValueEditListener listener )
	{
		if (m_listeners.contains(listener)) m_listeners.remove(listener);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void valueEdited()
	{
		for(IValueEditListener listener : m_listeners)
		{
			listener.valueChanged();
		}
	    refresh();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void createColumns()
	{
		if (m_simpleTable)
		{
			for(int index = 0; index < DictVariablesSimpleTableItems.values().length; index++)
			{
				DictVariablesSimpleTableItems item = DictVariablesSimpleTableItems.index(index);
				TableViewerColumn col = new TableViewerColumn( this, SWT.NONE);
				col.getColumn().setText(item.title);
				if (item.width>0) col.getColumn().setWidth(item.width);
				col.getColumn().setResizable(true);
				col.getColumn().setMoveable(false);
				if (item.center) col.getColumn().setAlignment(SWT.CENTER);
				col.getColumn().addSelectionListener( getColumnSelectionAdapter( col.getColumn(), index ) );
			}
		}
		else
		{
			for(int index = 0; index < DictVariablesTableItems.values().length; index++)
			{
				DictVariablesTableItems item = DictVariablesTableItems.index(index);
				TableViewerColumn col = new TableViewerColumn( this, SWT.NONE);
				
				if (!m_readOnly)
				{
					col.setEditingSupport( new VariableEditSupport(this, item) );
				}
				
				col.getColumn().setText(item.title);
				if (item.width>0) col.getColumn().setWidth(item.width);
				col.getColumn().setResizable(true);
				col.getColumn().setMoveable(false);
				if (item.center) col.getColumn().setAlignment(SWT.CENTER);
				col.getColumn().addSelectionListener( getColumnSelectionAdapter( col.getColumn(), index ) );
			}
		}
	}
	
	private SelectionAdapter getColumnSelectionAdapter( final TableColumn column, final int index )
	{
		SelectionAdapter adapter = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				m_comparator.setColumn(index);
				int dir = m_comparator.getDirection();
				getTable().setSortDirection(dir);
				getTable().setSortColumn(column);
				refresh();
			}
		};
		return adapter;
	}
}
