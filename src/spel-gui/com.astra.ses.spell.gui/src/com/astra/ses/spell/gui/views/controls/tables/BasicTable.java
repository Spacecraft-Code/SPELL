///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.tables
// 
// FILE      : BasicTable.java
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
package com.astra.ses.spell.gui.views.controls.tables;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/*******************************************************************************
 * @brief
 * @date 09/10/07
 ******************************************************************************/
public abstract class BasicTable extends TableViewer implements ControlListener
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PROTECTED ---------------------------------------------------------------
	protected Table	m_table;

	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor.
	 * 
	 * @param procId
	 *            The procedure identifier
	 * @param parent
	 *            The parent composite
	 **************************************************************************/
	public BasicTable(Composite parent, int style)
	{
		super(parent, style);
		m_table = getTable();
		m_table.setHeaderVisible(true);
		m_table.setLinesVisible(true);
		m_table.setFont(new Font(Display.getCurrent(), "Arial", 9, SWT.NORMAL));
		m_table.addControlListener(this);
		initializeTable();
	}

	/***************************************************************************
	 * Provide array with column names
	 **************************************************************************/
	protected abstract String[] getColumnNames();

	/***************************************************************************
	 * Provide array with column widths
	 **************************************************************************/
	protected abstract Integer[] getColumnSizes();

	/***************************************************************************
	 * Check if the column is resizable
	 **************************************************************************/
	protected abstract boolean isResizable(int columnIndex);

	/***************************************************************************
	 * Obtain the adjustable column
	 **************************************************************************/
	protected abstract int getAdjustableColumn();

	/***************************************************************************
	 * Obtain table width hint
	 **************************************************************************/
	public abstract int getTableWidthHint();

	/***************************************************************************
	 * Obtain table height hint
	 **************************************************************************/
	public abstract int getTableHeightHint();

	/***************************************************************************
	 * Initialize the table drawers and the table initial settings
	 **************************************************************************/
	protected void initializeTable()
	{
		// Now that we've set the text into the columns,
		// we call pack() on each one to size it to the
		// contents
		String[] names = getColumnNames();
		Integer[] sizes = getColumnSizes();
		for (int i = 0, n = names.length; i < n; i++)
		{
			// Create the TableColumn with right alignment
			TableColumn column = new TableColumn(m_table, SWT.RIGHT);
			column.setAlignment(SWT.CENTER);
			column.setText(names[i]);
		}
		TableColumn[] columns = m_table.getColumns();
		for (int i = 0, n = names.length; i < n; i++)
		{
			columns[i].pack();
			columns[i].setWidth(sizes[i]);
			columns[i].setResizable(isResizable(i));
		}
		m_table.removeAll();
	}

	/***************************************************************************
	 * Control callback
	 **************************************************************************/
	public void controlMoved(ControlEvent e)
	{
		// Nothing to do
	}

	/***************************************************************************
	 * Control callback
	 **************************************************************************/
	public void controlResized(ControlEvent e)
	{
		// Nothing to do
	}

}
