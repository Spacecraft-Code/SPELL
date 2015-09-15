///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs.controls
// 
// FILE      : ContextTable.java
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

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.astra.ses.spell.gui.core.interfaces.IServerProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.types.ContextStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.dialogs.ConnectionDialog;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.GuiColorKey;
import com.astra.ses.spell.gui.views.controls.tables.BasicTable;

/*******************************************************************************
 * @brief This viewer uses a table for showing the procedure code and the
 *        execution live status.
 * @date 09/10/07
 ******************************************************************************/
public class ContextTable extends BasicTable implements Listener
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Procedure manager handle */
	private static IServerProxy s_proxy = null;
	/** Resource manager handle */
	private static IConfigurationManager s_cfg = null;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private Vector<ContextInfo> m_contexts;
	/** Used for invert the sort algorithm */
	private int m_lastSort;
	/** Holds the list of filters */
	private Map<Integer, String> m_filters;

	// PROTECTED ---------------------------------------------------------------
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
	public ContextTable(Composite parent, ConnectionDialog dialog)
	{
		super(parent, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL);
		m_lastSort = 0;
		m_filters = new HashMap<Integer, String>();
		m_contexts = null;
		if (s_proxy == null)
		{
			s_proxy = (IServerProxy) ServiceManager.get(IServerProxy.class);
		}
		if (s_cfg == null)
		{
			s_cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		}
		m_table.addSelectionListener(dialog);
		for (TableColumn c : m_table.getColumns())
		{
			c.addListener(SWT.Selection, this);
		}
		m_table.setSortColumn(m_table.getColumn(0));
	}

	/***************************************************************************
	 * Update the table contents
	 * 
	 * @param ctxNames
	 *            List of known contexts
	 **************************************************************************/
	public void updateContexts(Vector<ContextInfo> contexts)
	{
		if (m_table.getItemCount() > 0 || contexts == null)
		{
			m_table.removeAll();
		}
		m_contexts = contexts;
		if (contexts == null)
		{
			return;
		}
		m_table.setRedraw(false);
		Logger.debug("Updating table rows: " + contexts.size(), Level.GUI, this);
		for (ContextInfo ctx : contexts)
		{
			TableItem titem = new TableItem(m_table, SWT.NONE);
			titem.setText(ContextTableColumn.NAME.ordinal(), ctx.getName());
			titem.setText(ContextTableColumn.STATUS.ordinal(), ctx.getStatus().toString());
			titem.setText(ContextTableColumn.SATELLITE.ordinal(), ctx.getSC());
			titem.setText(ContextTableColumn.GCS.ordinal(), ctx.getGCS());
			titem.setText(ContextTableColumn.DRIVER.ordinal(), ctx.getDriver());
			titem.setText(ContextTableColumn.ROLE.ordinal(), ctx.getFamily());
		}
		m_table.getColumn(0).setAlignment(SWT.LEFT);
		sortTable(m_lastSort, false);
		m_table.setRedraw(true);
		m_table.deselectAll();
		Logger.debug("After update: " + m_table.getItemCount(), Level.GUI, this);
	}

	/***************************************************************************
	 * Update a given context
	 **************************************************************************/
	public void updateContext(ContextInfo context)
	{
		if (context != null)
		{
			Logger.debug("Updating one context: " + context.getName() + " - " + context.getStatus(), Level.GUI, this);
			for (ContextInfo cinfo : m_contexts)
			{
				if (cinfo.getName().equals(context.getName()))
				{
					cinfo.setStatus(context.getStatus());
				}
			}
			for (TableItem item : m_table.getItems())
			{
				if (item.getText(ContextTableColumn.NAME.ordinal()).equals(context.getName()))
				{
					m_table.setRedraw(false);
					item.setText(ContextTableColumn.NAME.ordinal(), context.getName());
					item.setText(ContextTableColumn.STATUS.ordinal(), context.getStatus().toString());
					item.setText(ContextTableColumn.SATELLITE.ordinal(), context.getSC());
					item.setText(ContextTableColumn.GCS.ordinal(), context.getGCS());
					item.setText(ContextTableColumn.DRIVER.ordinal(), context.getDriver());
					item.setText(ContextTableColumn.ROLE.ordinal(), context.getFamily());
					applyColors();
					m_table.setRedraw(true);
					return;
				}
			}
		}
		m_table.deselectAll();
	}

	/***************************************************************************
	 * Apply a filter
	 **************************************************************************/
	public void addFilter(int column, String text)
	{
		m_filters.put(column, text);
		Logger.debug("Added filter: " + column + "=" + text, Level.GUI, this);
		Logger.debug("Total filters: " + m_filters.size(), Level.GUI, this);
		filterContexts();
	}

	/***************************************************************************
	 * Remove a filter
	 **************************************************************************/
	public void removeFilter(int column)
	{
		Logger.debug("Removed filter: " + column + "=" + m_filters.get(column), Level.GUI, this);
		m_filters.remove(column);
		Logger.debug("Total filters: " + m_filters.size(), Level.GUI, this);
		if (m_filters.size() == 0)
		{
			updateContexts(m_contexts);
		}
		else
		{
			filterContexts();
		}
	}

	/***************************************************************************
	 * Select the given context
	 * 
	 * @param ctxName
	 *            Context name
	 **************************************************************************/
	public void selectContext(String ctxName)
	{
		if (ctxName == null)
		{
			m_table.deselectAll();
			return;
		}
		for (int count = 0; count < m_table.getItemCount(); count++)
		{
			TableItem item = m_table.getItem(count);
			String name = item.getText(ContextTableColumn.NAME.ordinal());
			if (name.equals(ctxName))
			{
				m_table.select(count);
			}
		}
	}

	/***************************************************************************
	 * Get the selected context
	 **************************************************************************/
	public String getSelectedContext()
	{
		int idx = m_table.getSelectionIndex();
		if (idx >= 0)
		{
			return m_table.getItem(idx).getText(ContextTableColumn.NAME.ordinal());
		}
		return null;
	}

	/***************************************************************************
	 * Handle table selection events
	 **************************************************************************/
	public void handleEvent(Event e)
	{
		TableColumn column = (TableColumn) e.widget;
		int idx = 0;
		for (TableColumn c : m_table.getColumns())
		{
			if (column == c)
			{
				sortTable(idx, true);
				return;
			}
			idx++;
		}
	}

	/***************************************************************************
	 * Get the column names
	 **************************************************************************/
	protected String[] getColumnNames()
	{
		ContextTableColumn[] names = ContextTableColumn.values();
		String[] namestr = new String[names.length];
		for (ContextTableColumn col : names)
		{
			namestr[col.ordinal()] = col.toString();
		}
		return namestr;
	}

	/***************************************************************************
	 * Get the column sizes
	 **************************************************************************/
	protected Integer[] getColumnSizes()
	{
		ContextTableColumn[] names = ContextTableColumn.values();
		Integer[] widths = new Integer[names.length];
		for (ContextTableColumn col : names)
		{
			widths[col.ordinal()] = col.width();
		}
		return widths;
	}

	/***************************************************************************
	 * Obtain the adjustable column
	 **************************************************************************/
	protected int getAdjustableColumn()
	{
		return ContextTableColumn.NAME.ordinal();
	}

	@Override
	public int getTableWidthHint()
	{
		int result = 0;
		for (int columnWidth : getColumnSizes())
		{
			result += columnWidth;
		}
		return result;
	}

	@Override
	public int getTableHeightHint()
	{
		return 250;
	}

	/***************************************************************************
	 * Check if the given column is resizable
	 **************************************************************************/
	protected boolean isResizable(int idx)
	{
		return false;
	}

	/***************************************************************************
	 * Apply colors to table rows
	 **************************************************************************/
	private void applyColors()
	{
		boolean swap = true;
		for (TableItem item : m_table.getItems())
		{
			if (item.getText(ContextTableColumn.STATUS.ordinal()).equals(ContextStatus.RUNNING.toString()))
			{
				item.setBackground(s_cfg.getGuiColor(GuiColorKey.CONTEXT_ON));
			}
			else if (item.getText(ContextTableColumn.STATUS.ordinal()).equals(ContextStatus.ERROR.toString()))
			{
				item.setBackground(s_cfg.getGuiColor(GuiColorKey.CONTEXT_ERROR));
			}
			else if (item.getText(ContextTableColumn.STATUS.ordinal()).equals(ContextStatus.KILLED.toString()))
			{
				item.setBackground(s_cfg.getGuiColor(GuiColorKey.CONTEXT_ERROR));
			}
			else
			{
				if (swap)
				{
					item.setBackground(s_cfg.getGuiColor(GuiColorKey.TABLE_BG2));
					swap = false;
				}
				else
				{
					item.setBackground(s_cfg.getGuiColor(GuiColorKey.TABLE_BG));
					swap = true;
				}
			}
		}

	}

	/***************************************************************************
	 * Sort algorithm for table
	 **************************************************************************/
	private void sortTable(int columnIndex, boolean swapOrder)
	{
		boolean inverse = false;
		if (swapOrder)
		{
			if (m_lastSort == columnIndex)
			{
				m_lastSort = -1;
				inverse = true;
			}
			else
			{
				m_lastSort = columnIndex;
			}
		}
		TableItem[] items = m_table.getItems();
		Collator collator = Collator.getInstance(Locale.getDefault());
		String[] T = new String[0];
		for (int i = 1; i < items.length; i++)
		{
			String value1 = items[i].getText(columnIndex);
			ArrayList<String> values = new ArrayList<String>();
			for (int j = 0; j < i; j++)
			{
				String value2 = items[j].getText(columnIndex);
				int comp = 0;
				if (inverse)
				{
					comp = collator.compare(value2, value1);
				}
				else
				{
					comp = collator.compare(value1, value2);
				}
				if (comp < 0)
				{
					values.clear();
					for (int cidx = 0; cidx < m_table.getColumnCount(); cidx++)
					{
						values.add(items[i].getText(cidx));
					}
					String[] avalues = values.toArray(T);
					items[i].dispose();
					TableItem item = new TableItem(m_table, SWT.NONE, j);
					item.setText(avalues);
					items = m_table.getItems();
					break;
				}
			}
		}
		applyColors();
	}

	/***************************************************************************
	 * Filter the table contents
	 **************************************************************************/
	private void filterContexts()
	{
		if (m_table.getItemCount() == 0)
		{
			return;
		}
		m_table.setRedraw(false);
		m_table.removeAll();
		for (ContextInfo ctx : m_contexts)
		{
			TableItem titem = new TableItem(m_table, SWT.NONE);
			titem.setText(ContextTableColumn.NAME.ordinal(), ctx.getName());
			titem.setText(ContextTableColumn.STATUS.ordinal(), ctx.getStatus().toString());
			titem.setText(ContextTableColumn.SATELLITE.ordinal(), ctx.getSC());
			titem.setText(ContextTableColumn.GCS.ordinal(), ctx.getGCS());
			titem.setText(ContextTableColumn.DRIVER.ordinal(), ctx.getDriver());
			titem.setText(ContextTableColumn.ROLE.ordinal(), ctx.getFamily());
		}
		Logger.debug("Filtering table rows", Level.GUI, this);
		for (TableItem item : m_table.getItems())
		{
			for (Integer column : m_filters.keySet())
			{
				if (!item.getText(column).equals(m_filters.get(column)))
				{
					item.dispose();
					break;
				}
			}
		}
		m_table.getColumn(0).setAlignment(SWT.LEFT);
		sortTable(m_lastSort, false);
		m_table.setRedraw(true);
	}

}
