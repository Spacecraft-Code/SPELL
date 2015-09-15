///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls.menu
// 
// FILE      : CodeViewerMenuManager.java
//
// DATE      : 2010-08-26
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
package com.astra.ses.spell.gui.presentation.code.controls.menu;

import java.util.HashMap;

import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.astra.ses.spell.gui.core.model.types.BreakpointType;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.model.commands.ClearBreakpoints;
import com.astra.ses.spell.gui.model.commands.CmdGotoLine;
import com.astra.ses.spell.gui.model.commands.CmdRun;
import com.astra.ses.spell.gui.model.commands.SetBreakpoint;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.presentation.code.CodeModelProxy;
import com.astra.ses.spell.gui.presentation.code.controls.CodeViewer;
import com.astra.ses.spell.gui.presentation.code.controls.CodeViewerColumn;
import com.astra.ses.spell.gui.presentation.code.dialogs.SearchDialog;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.types.CmdConstants;
import com.astra.ses.spell.gui.types.ExecutorStatus;

/*******************************************************************************
 * 
 * {@link CodeViewerMenuManager} manages the CodeViewer's popup menu
 * 
 ******************************************************************************/
public class CodeViewerMenuManager
{

	/** Popup's parent widget */
	private CodeViewer m_viewer;
	/** Procedure data provider */
	private IProcedure m_model;
	/** Code proxy */
	private CodeModelProxy m_proxy;
	/** Menu */
	private Menu m_menu;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param parent
	 **************************************************************************/
	public CodeViewerMenuManager(CodeViewer viewer, IProcedure model, CodeModelProxy proxy)
	{
		m_viewer = viewer;
		m_model = model;
		m_proxy = proxy;
		m_menu = new Menu(viewer.getGrid());

		m_menu.addMenuListener(new MenuListener()
		{

			@Override
			public void menuShown(MenuEvent e)
			{
				fillMenu();
			}

			@Override
			public void menuHidden(MenuEvent e)
			{
			}
		});

		m_viewer.getGrid().setMenu(m_menu);
	}

	/**************************************************************************
	 * Check if control actions can be performed
	 *************************************************************************/
	private boolean gotoDoable()
	{
		if (!isExecutableLine())
			return false;
		ExecutorStatus status = m_model.getRuntimeInformation().getStatus();
		if (status.equals(ExecutorStatus.PAUSED))
			return true;
		return false;
	}

	/**************************************************************************
	 * Check if control actions can be performed
	 *************************************************************************/
	private boolean breakpointsDoable()
	{
		ExecutorStatus status = m_model.getRuntimeInformation().getStatus();
		switch (status)
		{
		case RELOADING:
		case ABORTED:
		case ERROR:
		case FINISHED:
		case LOADED:
		case PROMPT:
		case RUNNING:
		case UNINIT:
		case UNKNOWN:
			return false;
		case INTERRUPTED:
		case WAITING:
		case PAUSED:
		default:
			return true;
		}
	}

	/**************************************************************************
	 * Check if control actions can be performed
	 *************************************************************************/
	private boolean isExecutableLine()
	{
		ClientMode mode = m_model.getRuntimeInformation().getClientMode();
		if (!mode.equals(ClientMode.CONTROL))
			return false;
		int tableSelectedItems = m_viewer.getGrid().getSelectionCount();
		if (tableSelectedItems == 1)
		{
			GridItem item = m_viewer.getGrid().getSelection()[0];
			String code = item.getText(CodeViewerColumn.CODE.ordinal());
			if (code.trim().isEmpty())
				return false;
			if (code.startsWith("#"))
				return false;
			return true;
		}
		else
		{
			return false;
		}
	}

	/**************************************************************************
	 * Check if control actions can be performed
	 *************************************************************************/
	private boolean isGoodForRunUntil()
	{
		ClientMode mode = m_model.getRuntimeInformation().getClientMode();
		if (!mode.equals(ClientMode.CONTROL))
			return false;
		int tableSelectedItems = m_viewer.getGrid().getSelectionCount();
		if (tableSelectedItems == 1)
		{
			GridItem item = m_viewer.getGrid().getSelection()[0];
			int idx = item.getParent().indexOf(item);
			if (idx <= m_proxy.getCurrentLineNo()) return false;
			return true;
		}
		else
		{
			return false;
		}
	}

	/**************************************************************************
	 * Fill the menu with the appropriate actions
	 *************************************************************************/
	private void fillMenu()
	{
		// The items we provide depend on the client mode and executor status
		ClientMode mode = m_model.getRuntimeInformation().getClientMode();

		/*
		 * Remove current items from the menu
		 */
		for (MenuItem item : m_menu.getItems())
		{
			item.dispose();
		}

		if (isExecutableLine())
		{
			fillMenuControlActions();
		}

		// This item appears no matter if a line is selected or not
		if (mode.equals(ClientMode.CONTROL) && breakpointsDoable())
		{
			/*
			 * Clear breakpoints
			 */
			MenuItem clearBreakpoint = new MenuItem(m_menu, SWT.PUSH);
			clearBreakpoint.setText("Remove all breakpoints");
			clearBreakpoint.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					HashMap<String, String> args = new HashMap<String, String>();
					args.put(ClearBreakpoints.ARG_PROCID, m_model.getProcId());
					CommandHelper.execute(ClearBreakpoints.ID, args);
				}
			});

			/*
			 * Separator
			 */
			new MenuItem(m_menu, SWT.SEPARATOR);
		}

		/*
		 * Copy selected code
		 */
		MenuItem copyCode = new MenuItem(m_menu, SWT.PUSH);
		copyCode.setText("Copy source");
		copyCode.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				m_viewer.copySelected();
			}
		});

		/*
		 * Separator
		 */
		new MenuItem(m_menu, SWT.SEPARATOR);

		/*
		 * Refresh table information
		 */
		MenuItem refresh = new MenuItem(m_menu, SWT.PUSH);
		refresh.setText("Refresh");
		refresh.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				m_viewer.forceRefresh();
			}
		});

		/*
		 * Reset column widths
		 */
		MenuItem resetC = new MenuItem(m_menu, SWT.PUSH);
		resetC.setText("Reset column widths");
		resetC.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				m_viewer.resetColumnWidths();
			}
		});

		/*
		 * Separator
		 */
		new MenuItem(m_menu, SWT.SEPARATOR);

		/*
		 * Search code
		 */
		MenuItem search = new MenuItem(m_menu, SWT.PUSH);
		search.setText("Search...");
		search.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				SearchDialog dialog = new SearchDialog(m_viewer.getGrid().getShell(), m_viewer);
				dialog.open();
			}
		});

		if (m_viewer.hasMatches())
		{
			/*
			 * Clear search
			 */
			MenuItem clear = new MenuItem(m_menu, SWT.PUSH);
			clear.setText("Clear search");
			clear.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					m_viewer.clearMatches();
				}
			});
		}

	}

	/**************************************************************************
	 * Fill the menu with the active control actions
	 *************************************************************************/
	private void fillMenuControlActions()
	{
		BreakpointType type = BreakpointType.UNKNOWN;
		GridItem[] selection = m_viewer.getGrid().getSelection();
		final int lineNo = m_viewer.getGrid().indexOf(selection[0]) + 1;

		if (gotoDoable())
		{
			/*
			 * Go to line
			 */
			MenuItem gotoLine = new MenuItem(m_menu, SWT.PUSH);
			gotoLine.setText("Go to this line");
			gotoLine.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					HashMap<String, String> args = new HashMap<String, String>();
					args.put(CmdGotoLine.ARG_PROCID, m_model.getProcId());
					args.put(CmdGotoLine.ARG_LINENO, String.valueOf(lineNo));
					CommandHelper.execute(CmdGotoLine.ID, args);
					m_viewer.setSelection(null);
				}
			});

			if (isGoodForRunUntil())
			{
				/*
				 * Run to this line
				 */
				MenuItem runToLine = new MenuItem(m_menu, SWT.PUSH);
				runToLine.setText("Run until this line");
				runToLine.addSelectionListener(new SelectionAdapter()
				{
					@Override
					public void widgetSelected(SelectionEvent e)
					{
						BreakpointType type = BreakpointType.TEMPORARY;
	
						HashMap<String, String> args = new HashMap<String, String>();
						args.put(SetBreakpoint.ARG_PROCID, m_model.getProcId());
						args.put(SetBreakpoint.ARG_LINENO, String.valueOf(lineNo));
						args.put(SetBreakpoint.ARG_TYPE, type.toString());
						CommandHelper.execute(SetBreakpoint.ID, args);
	
						m_viewer.onLineChanged(m_proxy.getLine(lineNo));
	
						HashMap<String, String> runArgs = new HashMap<String, String>();
						runArgs.put(CmdRun.ARG_PROCID, m_model.getProcId());
						CommandHelper.execute(CmdConstants.CMDRUN_HANDLER, runArgs);
						m_viewer.setSelection(null);
					}
				});
			}

			/*
			 * Separator
			 */
			new MenuItem(m_menu, SWT.SEPARATOR);

		}

		if (breakpointsDoable() && isExecutableLine())
		{
			/*
			 * Get the line's breakpoint type
			 */
			type = m_proxy.getLine(lineNo - 1).getBreakpoint();

			if (type != null && type.equals(BreakpointType.PERMANENT))
			{
				/*
				 * Remove permanent breakpoint
				 */
				MenuItem removeBreakpoint = new MenuItem(m_menu, SWT.PUSH);
				removeBreakpoint.setText("Remove breakpoint");
				removeBreakpoint.addSelectionListener(new SelectionAdapter()
				{
					@Override
					public void widgetSelected(SelectionEvent e)
					{
						BreakpointType type = BreakpointType.UNKNOWN;
						HashMap<String, String> args = new HashMap<String, String>();
						args.put(SetBreakpoint.ARG_PROCID, m_model.getProcId());
						args.put(SetBreakpoint.ARG_LINENO, String.valueOf(lineNo));
						args.put(SetBreakpoint.ARG_TYPE, type.toString());
						CommandHelper.execute(SetBreakpoint.ID, args);

						m_viewer.onLineChanged(m_proxy.getLine(lineNo));
					}
				});
			}
			else
			{
				/*
				 * Add permanent breakpoint
				 */
				MenuItem addBreakpoint = new MenuItem(m_menu, SWT.PUSH);
				addBreakpoint.setText("Add breakpoint at this line");
				addBreakpoint.addSelectionListener(new SelectionAdapter()
				{
					@Override
					public void widgetSelected(SelectionEvent e)
					{
						BreakpointType type = BreakpointType.PERMANENT;

						HashMap<String, String> args = new HashMap<String, String>();
						args.put(SetBreakpoint.ARG_PROCID, m_model.getProcId());
						args.put(SetBreakpoint.ARG_LINENO, String.valueOf(lineNo));
						args.put(SetBreakpoint.ARG_TYPE, type.toString());
						CommandHelper.execute(SetBreakpoint.ID, args);

						m_viewer.onLineChanged(m_proxy.getLine(lineNo));
					}
				});
			}
		}
	}
}
