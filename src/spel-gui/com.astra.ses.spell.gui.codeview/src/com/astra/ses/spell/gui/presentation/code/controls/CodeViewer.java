///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls
// 
// FILE      : CodeViewer.java
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
package com.astra.ses.spell.gui.presentation.code.controls;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid.GridVisibleRange;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.presentation.code.CodeModelProxy;
import com.astra.ses.spell.gui.presentation.code.controls.menu.CodeViewerMenuManager;
import com.astra.ses.spell.gui.presentation.code.dialogs.ItemInfoDialog;
import com.astra.ses.spell.gui.presentation.code.dialogs.SearchDialog;
import com.astra.ses.spell.gui.presentation.code.renderer.BpRenderer;
import com.astra.ses.spell.gui.presentation.code.renderer.DataRenderer;
import com.astra.ses.spell.gui.presentation.code.renderer.LineRenderer;
import com.astra.ses.spell.gui.presentation.code.renderer.SourceRenderer;
import com.astra.ses.spell.gui.presentation.code.renderer.StatusRenderer;
import com.astra.ses.spell.gui.presentation.code.search.CodeSearch;
import com.astra.ses.spell.gui.presentation.code.search.CodeSearch.SearchMatch;
import com.astra.ses.spell.gui.procs.interfaces.listeners.IExecutionListener;
import com.astra.ses.spell.gui.procs.interfaces.model.ICodeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.ICodeModel;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.types.ExecutorStatus;

/*******************************************************************************
 * @brief This viewer uses a table for showing the procedure code and the
 *        execution live status.
 * @date 09/10/07
 ******************************************************************************/
public class CodeViewer extends GridTableViewer implements IExecutionListener, IPropertyChangeListener
{
	/***************************************************************************
	 * This interface will allow ItemInfoDialog to receive notifications
	 **************************************************************************/
	public interface ItemNotificationListener
	{
		/***********************************************************************
		 * Notify when an ItemNotification object arrives
		 * 
		 * @param data
		 **********************************************************************/
		public void notifyItem(ItemNotification data, String csPosition);
	}

	private static IConfigurationManager s_cfg = null;

	/** Autoscroll flag */
	private boolean m_autoScroll;
	/** Procedure model */
	private IProcedure m_procedure;
	/** Code proxy */
	private CodeModelProxy m_proxy;
	/** Holds the code search mechanism */
	private CodeSearch m_search;
	/** ItemInfo dialog */
	private ItemInfoDialog m_infoDialog;
	/** Navigation bar */
	private StackControl m_navBar;
	/** Holds the procedure status. Used to know which status transitions take place */
	private ICodeLine m_currentLine = null;
	private SourceRenderer[] m_renderers;
	/** Rendering buffer */
	private UpdateBuffer m_buffer;

	/***************************************************************************
	 * Constructor.
	 * 
	 * @param procId
	 *            The procedure identifier
	 * @param parent
	 *            The parent composite
	 **************************************************************************/
	public CodeViewer(Composite parent, IProcedure model)
	{
		super(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );

		if (s_cfg == null)
		{
			s_cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		}

		m_buffer = new UpdateBuffer(this);
		m_buffer.start();
		
		m_navBar = new StackControl(parent, SWT.BORDER, model, this);
		m_navBar.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));
		GridLayout lo = new GridLayout();
		lo.numColumns = 6;
		lo.makeColumnsEqualWidth = false;
		lo.marginHeight = 1;
		m_navBar.setLayout( lo );
		
		m_procedure = model;
		m_proxy = new CodeModelProxy(model.getExecutionManager());
		m_infoDialog = null;
		
		m_autoScroll = true;
		getGrid().setHeaderVisible(true);
		getGrid().setLinesVisible(false);

		createColumns();

		final CodeViewer theViewer = this;
		getGrid().addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				if ((e.stateMask & SWT.CONTROL) > 0)
				{
					if (e.keyCode == 99) // C
					{
						copySelected();
						getGrid().deselectAll();
					}
					else if (e.keyCode == 102) // F
					{
						getGrid().deselectAll();
						SearchDialog dialog = new SearchDialog(getGrid().getShell(), theViewer);
						dialog.open();
					}
				}
			}
		});

		getGrid().addMouseListener(new MouseAdapter()
		{
			public void mouseDoubleClick(MouseEvent e)
			{
				Point p = new Point(e.x, e.y);
				GridItem item = getGrid().getItem(p);
				if (item != null)
				{
					int itemIndex = getGrid().indexOf(item);
					ICodeLine line = m_proxy.getLine(itemIndex);

					if (line.hasNotifications() && m_infoDialog == null)
					{
						m_infoDialog = new ItemInfoDialog(getGrid().getShell(), m_procedure, line);
						m_infoDialog.open();
						m_infoDialog = null;
					}
				}
			}
		});

		getGrid().getColumn(CodeViewerColumn.BREAKPOINT.ordinal()).addControlListener(new ColumnSizer(getGrid(), CodeViewerColumn.RESULT));
		getGrid().getColumn(CodeViewerColumn.LINE_NO.ordinal()).addControlListener(new ColumnSizer(getGrid(), CodeViewerColumn.RESULT));
		getGrid().getColumn(CodeViewerColumn.CODE.ordinal()).addControlListener(new ColumnSizer(getGrid(), CodeViewerColumn.RESULT));
		getGrid().getColumn(CodeViewerColumn.DATA.ordinal()).addControlListener(new ColumnSizer(getGrid(), CodeViewerColumn.RESULT));
		getGrid().addControlListener(new ColumnSizer(getGrid(), CodeViewerColumn.CODE));

		/*
		 * Popup menu manager
		 */
		new CodeViewerMenuManager(this, m_procedure, m_proxy);

		m_search = new CodeSearch(this);

		s_cfg.addPropertyChangeListener(this);
		
		getGrid().addDisposeListener(new DisposeListener()
		{
			@Override
            public void widgetDisposed(DisposeEvent e)
            {
				dispose();
            }
			
		});
	}

	
	/***************************************************************************
	 * 
	 **************************************************************************/
	 void displayCodeFirst()
	{
		if (m_proxy.stackFirst())
		{
			onCodeChanged(m_proxy.getCurrentCode());
		}
	}
	 
	 
	 /***************************************************************************
	 * 
	 **************************************************************************/
	 void displayCode( int pos )
	{
		if (m_proxy.stackTo(pos))
		{
			onCodeChanged(m_proxy.getCurrentCode());
		}
	}
	 
	/***************************************************************************
	 * 
	 **************************************************************************/
	 void displayCodeLast()
	{
		if (m_proxy.stackTop())
		{
			onCodeChanged(m_proxy.getCurrentCode());
		}
	}
	 
	/***************************************************************************
	 * 
	 **************************************************************************/
	 void displayCodeLeft()
	{
		if (m_proxy.stackDown())
		{
			onCodeChanged(m_proxy.getCurrentCode());
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	void displayCodeRight()
	{
		if (m_proxy.stackUp())
		{
			onCodeChanged(m_proxy.getCurrentCode());
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void scheduleUpdate( ICodeLine line )
	{
		m_buffer.scheduleUpdate(line);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void scheduleUpdate( List<ICodeLine> lines )
	{
		m_buffer.scheduleUpdate(lines);
	}

	/***************************************************************************
	 * Dispose resources
	 **************************************************************************/
	public void dispose()
	{
		m_buffer.stopUpdate();
		m_procedure.getExecutionManager().removeListener(this);
		m_navBar.dispose();
		s_cfg.removePropertyChangeListener(this);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public IProcedure getModel()
	{
		return m_procedure;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void applyItemHeight()
	{
		getGrid().setItemHeight(m_renderers[0].getFontSize() + 8);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setModel()
	{
		Logger.debug("Set model", Level.GUI, this);
		m_renderers = new SourceRenderer[5];
		m_renderers[0] = new BpRenderer(m_procedure,m_proxy);
		m_renderers[1] = new LineRenderer(m_procedure,m_proxy);
		m_renderers[2] = new SourceRenderer(m_procedure,m_proxy);
		m_renderers[3] = new DataRenderer(m_procedure,m_proxy);
		m_renderers[4] = new StatusRenderer(m_procedure,m_proxy);
		for (CodeViewerColumn col : CodeViewerColumn.values())
		{
			getGrid().getColumn(col.ordinal()).setCellRenderer(m_renderers[col.ordinal()]);
		}
		m_procedure.getExecutionManager().addListener(this);
		applyItemHeight();
		super.setInput(m_proxy);
	}

	/***************************************************************************
	 * Enable or disable the autoscroll
	 **************************************************************************/
	public void setAutoScroll(boolean enable)
	{
		m_autoScroll = enable;
	}

	/***************************************************************************
	 * Check the autoscroll
	 **************************************************************************/
	public boolean isAutoScroll()
	{
		return m_autoScroll;
	}

	/***************************************************************************
	 * Gain the focus
	 **************************************************************************/
	public void setFocus()
	{
		getGrid().setFocus();
		showCurrentLine();
	}

	/***************************************************************************
	 * Increase or decrease the font size
	 * 
	 * @param increase
	 *            If true, increase the font size
	 **************************************************************************/
	public void zoom(boolean increase)
	{
		boolean changed = false;
		for (SourceRenderer rnd : m_renderers)
		{
			changed = changed | rnd.zoom(increase);
		}
		if (changed)
		{
			getGrid().setItemHeight(m_renderers[0].getFontSize() + 8);
			getGrid().getColumn(CodeViewerColumn.LINE_NO.ordinal()).pack();
			getGrid().redraw();
			showCurrentLine();
		}
	}

	/***************************************************************************
	 * Search for a string in the code
	 **************************************************************************/
	public int searchString(String toSearch)
	{
		clearMatches();
		int count = m_search.searchString(toSearch);
		if (count > 0)
		{
			showMatches(m_search.getMatches());
		}
		return count;
	}

	/***************************************************************************
	 * Show next occurrence
	 **************************************************************************/
	public boolean hasMatches()
	{
		return (m_search.getMatches() != null) && (m_search.getMatches().length > 0);
	}

	/***************************************************************************
	 * Show next occurrence
	 **************************************************************************/
	public boolean searchNext()
	{
		SearchMatch match = m_search.getNext();
		if (match == null)
			return false;
		getGrid().deselectAll();
		getGrid().select(match.lineNo - 1);
		showLine(match.lineNo, false);
		return true;
	}

	/***************************************************************************
	 * Show previous occurrence
	 **************************************************************************/
	public boolean searchPrevious()
	{
		SearchMatch match = m_search.getPrevious();
		if (match == null)
			return false;
		getGrid().deselectAll();
		getGrid().select(match.lineNo - 1);
		showLine(match.lineNo > 1 ? match.lineNo - 1 : match.lineNo, false);
		return true;
	}

	/***************************************************************************
	 * Highlight the given ranges
	 **************************************************************************/
	private void showMatches(SearchMatch[] matches)
	{
		getGrid().setRedraw(false);
		for (SearchMatch match : matches)
		{
			GridItem item = getGrid().getItem(match.lineNo - 1);
			item.setData("DATA_SEARCH_MATCH", match);
		}
		getGrid().deselectAll();
		getGrid().setRedraw(true);
		getGrid().select(matches[0].lineNo - 1);
		showLine(matches[0].lineNo, false);
	}

	/***************************************************************************
	 * Clear the highlight of the given ranges
	 **************************************************************************/
	public void clearMatches()
	{
		m_search.clear();
		getGrid().setRedraw(false);
		getGrid().deselectAll();
		for (GridItem item : getGrid().getItems())
		{
			item.setData("DATA_SEARCH_MATCH", null);
		}
		getGrid().setRedraw(true);
	}

	/***************************************************************************
	 * Copy selected source rows
	 **************************************************************************/
	public void copySelected()
	{
		GridItem[] selection = getGrid().getSelection();
		Clipboard clipboard = new Clipboard(Display.getCurrent());
		String data = "";
		for (GridItem item : selection)
		{
			if (!data.isEmpty())
				data += "\n";
			String line = item.getText(CodeViewerColumn.CODE.ordinal());
			if (line != null)
			{
				line = line.trim();
			}
			else
			{
				line = "";
			}
			data += line;
		}
		clipboard.setContents(new Object[] { data }, new Transfer[] { TextTransfer.getInstance() });
		clipboard.dispose();
	}

	/***************************************************************************
	 * Create the table columns
	 **************************************************************************/
	protected void createColumns()
	{
		for (CodeViewerColumn colModel : CodeViewerColumn.values())
		{
			// Create the TableColumn with right alignment
			int style = colModel.getAlignment() | SWT.H_SCROLL;
			GridViewerColumn viewerColumn = new GridViewerColumn(this, style);

			GridColumn column = viewerColumn.getColumn();
			column.setText(colModel.getName());
			column.setAlignment(colModel.getAlignment());
			column.setWidth(colModel.getInitialWidth());
			column.setResizeable(colModel.isResizable());
		}
	}

	/***************************************************************************
	 * Center the table view on the given line.
	 **************************************************************************/
	public void showLine(int lineNo, boolean select)
	{
		if (getGrid().isDisposed() || !getGrid().isVisible()) return;
		int numLines = getGrid().getItemCount();
		if (lineNo > 0 && numLines > lineNo)
		{
			GridVisibleRange range = getGrid().getVisibleRange();
			
			int visibleCount = range.getItems().length;
			
			GridItem first = range.getItems()[0];
			GridItem last  = range.getItems()[visibleCount-1];
			int firstIndex = getGrid().getIndexOfItem(first);
			int lastIndex = getGrid().getIndexOfItem(last);
			
			int itemToShow = lineNo;
			if (lineNo<firstIndex)
			{
				itemToShow = lineNo - visibleCount/2;
			}
			else if (lineNo>lastIndex)
			{
				itemToShow = lineNo + visibleCount/2;
			}
			
			if (itemToShow<0) itemToShow = 0;
			else if (itemToShow>numLines) itemToShow = numLines-1;
			
			getGrid().showItem(getGrid().getItem(itemToShow));
			if (select)
			{
				getGrid().deselectAll();
				getGrid().select(lineNo - 1);
			}
		}
	}

	/***************************************************************************
	 * Center the table view on the currently executed line.
	 **************************************************************************/
	public void showCurrentLine()
	{
		if (!m_autoScroll) return;
		int currentLine = m_proxy.getCurrentLineNo();
		showLine(currentLine, false);
	}

	/***************************************************************************
	 * Update the executor status
	 **************************************************************************/
	public void setExecutorStatus(ExecutorStatus st)
	{
		// If the executor status is not PAUSED or equivalent so that the
		// source code is kept the same, disable the stack control buttons
		m_navBar.updateCodeNavigation( st, m_proxy );

		// Set the status in the renderers so that the background changes
		for (SourceRenderer r : m_renderers)
		{
			r.setExecutorStatus(st);
		}
	}

	/***************************************************************************
	 * Force a refresh of the item notifications
	 **************************************************************************/
	public void forceRefresh()
	{
		GridVisibleRange range = getGrid().getVisibleRange();
		
		int visibleCount = range.getItems().length;
		
		GridItem first = range.getItems()[0];
		GridItem last  = range.getItems()[visibleCount-1];
		int firstIndex = getGrid().getIndexOfItem(first);
		int lastIndex = getGrid().getIndexOfItem(last);

		for(int index=firstIndex; index < lastIndex; index++)
		{
			ICodeLine line = m_proxy.getLine(index);
			if (line.hasNotifications())
			{
				line.calculateSummary();
			}
			scheduleUpdate(line);
		}
		showCurrentLine();
	}

	/***************************************************************************
	 * Reset the column widths to defaults
	 **************************************************************************/
	public void resetColumnWidths()
	{
		int totalWidth = 0;
		for (CodeViewerColumn colModel : CodeViewerColumn.values())
		{
			if (colModel.equals(CodeViewerColumn.CODE)) continue;
			GridColumn column = getGrid().getColumn(colModel.ordinal());
			int width = colModel.getInitialWidth();
			totalWidth += width;
			column.setWidth(width);
		}
		int gridWidth = getGrid().getBounds().width;
		if (getGrid().getVerticalBar().isVisible())
		{
			gridWidth -= getGrid().getVerticalBar().getSize().x;
		}
		if (totalWidth < gridWidth)
		{
			int widthForCode = gridWidth - totalWidth - 4;
			getGrid().getColumn(CodeViewerColumn.CODE.ordinal()).setWidth( widthForCode );
		}
	}
	
	/***************************************************************************
	 * Force a control resize event
	 **************************************************************************/
	public void forceControlEvent( Control ctrl )
	{
		for(Listener lst : ctrl.getListeners(SWT.Resize))
		{
			if (lst instanceof ControlListener)
			{
				ControlListener clst = (ControlListener) lst;
				clst.controlResized(null);
			}
		}
	}

	/***************************************************************************
	 * Notifications from the procedure code model
	 **************************************************************************/
	@Override
	public void onLinesChanged(final List<ICodeLine> lines )
	{
		if (getGrid().isDisposed()) return;
		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				if (lines == null || lines.isEmpty()) return;
				
				// Schedule the update of all lines in the viewer, so that
				// all these lines reflect the executed flag correctly, etc.
				scheduleUpdate(lines);
				
				ICodeLine line = lines.get(lines.size()-1); 
				// Ensure the code in place still matches the line to update 
				// it may not be the case with source code changes
				if (!m_proxy.getCurrentCodeName().equals(line.getParentCodeId()))
				{
					return;
				}
				m_navBar.updateCodeNavigation( null, m_proxy );
				if (m_currentLine != null)
				{
					scheduleUpdate(m_currentLine);
				}
				m_currentLine = line;
				scheduleUpdate(line);
				if (m_proxy.getStackDepth() == m_proxy.getViewDepth())
				{
					if (m_autoScroll)
					{
						showLine(line.getLineNo(), false);
					}
				}
			}
		});
	}

	/***************************************************************************
	 * Notifications from the procedure code model
	 **************************************************************************/
	@Override
	public void onLineChanged(final ICodeLine line )
	{
		if (getGrid().isDisposed()) return;
		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				if (line == null) return;
				// Ensure the code in place still matches the line to update 
				// it may not be the case with source code changes
				if (!m_proxy.getCurrentCodeName().equals(line.getParentCodeId()))
				{
					return;
				}
				m_navBar.updateCodeNavigation( null, m_proxy );
				if (m_currentLine != null)
				{
					scheduleUpdate(m_currentLine);
				}
				m_currentLine = line;
				scheduleUpdate(line);
				if (m_proxy.getStackDepth() == m_proxy.getViewDepth())
				{
					if (m_autoScroll)
					{
						showLine(line.getLineNo(), false);
					}
				}
			}
		});
	}

	/***************************************************************************
	 * Notifications from the procedure code model
	 **************************************************************************/
	@Override
    public void onItemsChanged( final List<ICodeLine> lines )
    {
		if (getGrid().isDisposed()) return;
		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				scheduleUpdate(lines);
				for(ICodeLine line : lines)
				{
					if (m_infoDialog != null && m_infoDialog.getLine() == line)
					{
						m_infoDialog.onNotification();
						break;
					}
				}
			}
		});
    }

	/***************************************************************************
	 * Notifications from the procedure code model
	 **************************************************************************/
	@Override
	public void onCodeChanged( final ICodeModel code )
	{
		if (getGrid().isDisposed()) return;
		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				m_navBar.updateCodeNavigation( null, m_proxy );
				List<ICodeLine> source = code.getLines();
				List<String> plainSource = new LinkedList<String>();
				for (ICodeLine line : source) plainSource.add(line.getSource());
				m_renderers[CodeViewerColumn.CODE.ordinal()].onNewSource(code.getCodeId(), plainSource.toArray(new String[0]));
				refresh();
				getGrid().getColumn(CodeViewerColumn.LINE_NO.ordinal()).pack();  
				showCurrentLine();
			}
		});
	}

	/***************************************************************************
	 * Notifications from the procedure code model
	 **************************************************************************/
	@Override 
    public void onProcessingDelayChanged(long delay) {}

	/***************************************************************************
	 * Notifications from the configuration property changes
	 **************************************************************************/
	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		for (SourceRenderer rnd : m_renderers)
		{
			rnd.onPropertiesChanged(event);
		}
		String property = event.getProperty();
		if (property.equals(FontKey.CODE.getPreferenceName()))
		{
			applyItemHeight();
			getGrid().getColumn(CodeViewerColumn.LINE_NO.ordinal()).pack();
		}
		getGrid().redraw();
	}

}
