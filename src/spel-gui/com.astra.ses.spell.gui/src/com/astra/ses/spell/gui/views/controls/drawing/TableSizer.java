///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.drawing
// 
// FILE      : TableSizer.java
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
package com.astra.ses.spell.gui.views.controls.drawing;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/*******************************************************************************
 * @brief Manages and adjusts table columns on resize events
 * @date 09/10/07
 ******************************************************************************/
public class TableSizer extends ControlAdapter
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

	// PRIVATE -----------------------------------------------------------------
	private Composite	m_container;
	private Table	  m_table;
	private int	      m_adjustableColumn;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public TableSizer(Composite container, Table table, int adjustableColumn)
	{
		m_adjustableColumn = adjustableColumn;
		m_container = container;
		m_table = table;
		m_container.addControlListener(this);
	}

	/***************************************************************************
	 * Resize callback
	 **************************************************************************/
	public void controlResized(ControlEvent event)
	{
		Rectangle area = m_container.getClientArea();

		Point preferredSize = m_table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int width = area.width - 2 * m_table.getBorderWidth();
		if (m_table.getVerticalBar().isVisible())
		{
			width -= m_table.getVerticalBar().getSize().x;
		}

		if (preferredSize.y > area.height + m_table.getHeaderHeight())
		{
			// Subtract the scrollbar width from the total column width
			// if a vertical scrollbar will be required
			Point vBarSize = m_table.getVerticalBar().getSize();
			width -= vBarSize.x;
		}

		Point oldSize = m_table.getSize();
		TableColumn[] columns = m_table.getColumns();
		if (oldSize.x > area.width)
		{
			// table is getting smaller so make the columns
			// smaller first and then resize the table to
			// match the client area width
			int fixedWidth = 0;
			for (int cidx = 0; cidx < columns.length; cidx++)
			{
				if (cidx != m_adjustableColumn)
				{
					fixedWidth += columns[cidx].getWidth();
				}
			}
			for (int cidx = 0; cidx < columns.length; cidx++)
			{
				if (m_adjustableColumn == cidx)
				{
					int cw = width - fixedWidth;
					if (m_table.getVerticalBar().isVisible())
					{
						cw -= m_table.getVerticalBar().getSize().x * 2 / 5;
					}
					columns[cidx].setWidth(cw);
					break;
				}
			}
			m_table.setSize(area.width, area.height);
		}
		else if (oldSize.x < area.width)
		{
			// table is getting bigger so make the table
			// bigger first and then make the columns wider
			// to match the client area width
			m_table.setSize(area.width, area.height);
			int fixedWidth = 0;
			for (int cidx = 0; cidx < columns.length; cidx++)
			{
				if (m_adjustableColumn != cidx)
				{
					fixedWidth += columns[cidx].getWidth();
				}
			}
			for (int cidx = 0; cidx < columns.length; cidx++)
			{
				if (m_adjustableColumn == cidx)
				{
					int cw = width - fixedWidth;
					if (m_table.getVerticalBar().isVisible())
					{
						cw -= m_table.getVerticalBar().getSize().x * 2 / 5;
					}
					columns[cidx].setWidth(cw);
					break;
				}
			}
		}
	}
}
