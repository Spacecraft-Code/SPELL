///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.master
// 
// FILE      : ExecutorsTable.java
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
package com.astra.ses.spell.gui.views.controls.master.executors;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Font;

import com.astra.ses.spell.gui.core.interfaces.IExecutorInfo;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.dialogs.ControlInfoDialog;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

/*******************************************************************************
 * @brief
 * @date 09/10/07
 ******************************************************************************/
public class ExecutorsTable extends GridTableViewer implements IExecutorsTable
{
	// Owned model of executors
	private ExecutorsModel m_model;
	// Cell renderers, one per column
	private ExecutorsTableRenderer[] m_cellRenderers;

	/***************************************************************************
	 * Constructor.
	 * 
	 * @param procId
	 *            The procedure identifier
	 * @param parent
	 *            The parent composite
	 **************************************************************************/
	public ExecutorsTable(ExecutorComposite parent)
	{
		super(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		m_model = new ExecutorsModel();

		// Table look and feel
		getGrid().setHeaderVisible(true);
		getGrid().setLinesVisible(true);
		applyFonts();
		createColumns();
		
		// Model and contents
		setContentProvider(new ExecutorsContentProvider());
		setLabelProvider(new ExecutorsLabelProvider());
		setInput(m_model);
		
		// Setup internal listeners
		addDoubleClickListener( new IDoubleClickListener()
		{
			@Override
			public void doubleClick(DoubleClickEvent arg0)
			{
				onDoubleClick();
			}
		});
		getGrid().addControlListener( new ControlAdapter()
		{
			public void controlResized( ControlEvent ev )
			{
				onControlResized();
			}
		});
		
		refreshAll();
	}

	/***************************************************************************
	 * Apply font changes
	 **************************************************************************/
	@Override
    public void applyFonts()
	{
		IConfigurationManager cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		Font font = cfg.getFont( FontKey.GUI_BIG );
		getGrid().setFont(font);
		int itemHeight = (int)(font.getFontData()[0].getHeight()*1.8);
		getGrid().setItemHeight( itemHeight );
		getGrid().redraw();
	}

	/***************************************************************************
	 * Refresh a given procedure
	 **************************************************************************/
	@Override
    public void refreshItem( String procId )
	{
		Logger.debug("Refresh item for " + procId, Level.GUI, this);
		for (IProcedure proc  : m_model.getElements())
		{
			if (proc.getProcId().equals(procId))
			{
				Logger.debug("Updating element " + proc.getProcId(), Level.GUI, this);
				m_model.refresh(proc);
				refresh(proc);
			}
		}
	}
	
	/***************************************************************************
	 * Refresh all models
	 **************************************************************************/
	@Override
    public void refreshAll()
	{
		try
		{
			Logger.debug("Refresh complete executors table", Level.GUI, this);
			if (getControl().isDisposed()) return;
			m_model.refresh();
			refresh();
			Logger.debug("Refresh complete executors table done", Level.GUI, this);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/***************************************************************************
	 * Get all selected procedures
	 **************************************************************************/
	@Override
    public String[] getSelectedProcedures()
	{
		ArrayList<String> ids = new ArrayList<String>();
		IStructuredSelection sel = (IStructuredSelection) getSelection();
		if (!sel.isEmpty())
		{
			@SuppressWarnings("unchecked")
			Iterator<IProcedure> it = sel.iterator();
			while (it.hasNext())
			{
				Object proc = it.next();
				if (proc instanceof IProcedure)
				{
					ids.add(((IProcedure) proc).getProcId());
				}
				else if (proc instanceof IExecutorInfo)
				{
					ids.add(((IExecutorInfo) proc).getProcId());
				}
			}
		}
		return ids.toArray(new String[0]);
	}

	/***************************************************************************
	 * Create grid columns
	 **************************************************************************/
	private void createColumns()
	{
		m_cellRenderers = new ExecutorsTableRenderer[ExecutorsTableColumns.values().length];
		for (int index = 0; index < ExecutorsTableColumns.values().length; index++)
		{
			ExecutorsTableColumns item = ExecutorsTableColumns.index(index);
			GridViewerColumn col = new GridViewerColumn(this, SWT.NONE);
			col.getColumn().setText(item.title);
			
			ExecutorsTableRenderer renderer = new ExecutorsTableRenderer(getGrid(),item,m_model);
			col.getColumn().setCellRenderer(renderer);
			m_cellRenderers[index] = renderer;
			
			if (item.width > 0)
				col.getColumn().setWidth(item.width);
			col.getColumn().setResizeable((index < ExecutorsTableColumns.values().length - 1));
			col.getColumn().setMoveable(false);
			if (item.center)
				col.getColumn().setAlignment(SWT.CENTER);
		}
	}

	/***************************************************************************
	 * Double-click event
	 **************************************************************************/
	private void onDoubleClick()
	{
		ISelection sel = getSelection();
		if ((sel != null) && (!sel.isEmpty()))
		{
			IStructuredSelection isel = (IStructuredSelection) sel;
			if (isel.getFirstElement() instanceof IProcedure)
			{
				IProcedure proc = (IProcedure) isel.getFirstElement();
				ControlInfoDialog dialog = new ControlInfoDialog(getGrid().getShell(), proc);
				dialog.open();
			}
		}
	}

	/***************************************************************************
	 * Adjust grid columns on control size changes
	 **************************************************************************/
	private void onControlResized()
	{
		Grid t = getGrid();
		int totalWidth = t.getClientArea().width;
		if (t.getVerticalBar() != null && t.getVerticalBar().isVisible())
		{
			totalWidth -= t.getVerticalBar().getSize().x;
		}
		int colWidth = 0;
		for (int index = 0; index < ExecutorsTableColumns.values().length - 1; index++)
		{
			colWidth += t.getColumn(index).getWidth();
		}
		t.getColumn(ExecutorsTableColumns.values().length - 1).setWidth(totalWidth - colWidth);
	}
}
