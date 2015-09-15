///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls
// 
// FILE      : ItemInfoTable.java
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
package com.astra.ses.spell.gui.presentation.code.iteminfo;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.astra.ses.spell.gui.procs.interfaces.model.SummaryMode;

/*******************************************************************************
 * @brief
 * @date 09/10/07
 ******************************************************************************/
public class ItemInfoTable extends TableViewer
{
	/** ContentProvider */
	private ItemInfoTableContentProvider	m_contentProvider;
	protected Table	                        m_table;

	/***************************************************************************
	 * Constructor.
	 * 
	 * @param procId
	 *            The procedure identifier
	 * @param parent
	 *            The parent composite
	 **************************************************************************/
	public ItemInfoTable(Composite parent)
	{
		super(parent, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL);
		m_contentProvider = new ItemInfoTableContentProvider();

		setContentProvider(m_contentProvider);
		setLabelProvider(new ItemInfoTableLabelProvider());

		m_table = getTable();
		m_table.setHeaderVisible(true);
		m_table.setLinesVisible(true);
		m_table.setFont(new Font(Display.getCurrent(), "Arial", 9, SWT.NORMAL));
		m_table.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		initializeTable();
	}

	/***************************************************************************
	 * Obtain a table column
	 **************************************************************************/
	protected TableColumn getColumn(ItemInfoTableColumn column)
	{
		return m_table.getColumn(column.ordinal());
	}

	/***************************************************************************
	 * Initialize the table drawers and the table initial settings
	 **************************************************************************/
	protected void initializeTable()
	{
		// Now that we've set the text into the columns,
		// we call pack() on each one to size it to the
		// contents
		for (ItemInfoTableColumn column : ItemInfoTableColumn.values())
		{
			// Create the TableColumn with right alignment
			TableColumn tableColumn = new TableColumn(m_table, SWT.RIGHT);
			tableColumn.setText(column.title);
			tableColumn.setAlignment(SWT.LEFT);
			tableColumn.pack();
			tableColumn
			        .setWidth(ItemInfoTableColumn.values()[column.ordinal()].width);
			tableColumn.setResizable(true);
		}
		getColumn(ItemInfoTableColumn.NAME).setAlignment(SWT.LEFT);
		getColumn(ItemInfoTableColumn.STATUS).setAlignment(SWT.CENTER);
		getColumn(ItemInfoTableColumn.COMMENTS).setAlignment(SWT.LEFT);
	}

	/***************************************************************************
	 * Change the summary mode
	 * 
	 * @param summary
	 **************************************************************************/
	public void setSummaryMode( SummaryMode mode )
	{
		m_contentProvider.setSummaryMode(mode);
	}
}
