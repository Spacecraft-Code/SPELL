///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs
// 
// FILE      : StartWithParametersDialog.java
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
package com.astra.ses.spell.gui.dialogs;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.astra.ses.spell.gui.Activator;

/*******************************************************************************
 * @brief Dialog for starting a procedure with arguments
 * @date 18/09/07
 ******************************************************************************/
public class StartWithParametersDialog extends TitleAreaDialog
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	public static final String	ID	= "com.astra.ses.spell.gui.dialogs.ConditionDialog";

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the dialog image icon */
	private Image	            m_image;
	/** Holds the arguments table viewer */
	private TableViewer	        m_viewer;
	/** Holds the argument expression */
	private Map<String, String>	m_argExpression;

	// =========================================================================
	// # INNER CLASSES
	// =========================================================================

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 * 
	 * @param shell
	 *            The parent shell
	 **************************************************************************/
	public StartWithParametersDialog(Shell shell)
	{
		super(shell);
		// Obtain the image for the dialog icon
		ImageDescriptor descr = Activator
		        .getImageDescriptor("icons/dlg_exec.png");
		m_image = descr.createImage();
		m_argExpression = null;
	}

	/***************************************************************************
	 * Called when the dialog is about to close.
	 * 
	 * @return The superclass return value.
	 **************************************************************************/
	public boolean close()
	{
		m_image.dispose();
		return super.close();
	}

	/***************************************************************************
	 * Obtain the defined arguments
	 * 
	 * @return The arguments
	 **************************************************************************/
	public Map<String, String> getArguments()
	{
		return m_argExpression;
	}

	// =========================================================================
	// # NON-ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Creates the dialog contents.
	 * 
	 * @param parent
	 *            The base composite of the dialog
	 * @return The resulting contents
	 **************************************************************************/
	protected Control createContents(Composite parent)
	{
		Control contents = super.createContents(parent);
		setMessage("Define the arguments for the procedure:");
		setTitle("Start with arguments");
		setTitleImage(m_image);
		return contents;
	}

	/***************************************************************************
	 * Create the dialog area contents.
	 * 
	 * @param parent
	 *            The base composite of the dialog
	 * @return The resulting contents
	 **************************************************************************/
	protected Control createDialogArea(Composite parent)
	{
		Composite ctrl = new Composite(parent, SWT.NONE);
		ctrl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
		        | GridData.GRAB_HORIZONTAL));
		GridLayout controlBarLayout = new GridLayout();
		controlBarLayout.numColumns = 3;
		ctrl.setLayout(controlBarLayout);

		Button b1 = new Button(ctrl, SWT.PUSH);
		b1.setText("More");
		b1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b1.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				addArgument();
			}
		});
		Button b2 = new Button(ctrl, SWT.PUSH);
		b2.setText("Less");
		b2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b2.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				delArgument();
			}
		});
		Button b3 = new Button(ctrl, SWT.PUSH);
		b3.setText("Clear");
		b3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b3.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				clearArguments();
			}
		});

		// Main composite of the dialog area -----------------------------------
		m_viewer = new TableViewer(parent, SWT.BORDER);
		TableColumn c0 = new TableColumn(m_viewer.getTable(), SWT.NONE);
		c0.setText("#");
		c0.setAlignment(SWT.LEFT);
		c0.setResizable(false);
		TableColumn c1 = new TableColumn(m_viewer.getTable(), SWT.NONE);
		c1.setText("Argument");
		c1.setResizable(false);
		TableColumn c2 = new TableColumn(m_viewer.getTable(), SWT.NONE);
		c2.setText("Value");
		c2.setResizable(false);
		m_viewer.getTable().setHeaderVisible(true);
		m_viewer.getTable().setLinesVisible(true);
		m_viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

		final TableEditor editor = new TableEditor(m_viewer.getTable());
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;

		m_viewer.getTable().addListener(SWT.MouseDown, new Listener()
		{
			public void handleEvent(Event event)
			{
				Table table = (Table) event.widget;
				Rectangle clientArea = table.getClientArea();
				Point pt = new Point(event.x, event.y);
				int index = table.getTopIndex();
				while (index < table.getItemCount())
				{
					boolean visible = false;
					final TableItem item = table.getItem(index);
					for (int i = 1; i < table.getColumnCount(); i++)
					{
						Rectangle rect = item.getBounds(i);
						if (rect.contains(pt))
						{
							final int column = i;
							final Text text = new Text(table, SWT.NONE);
							Listener textListener = new Listener()
							{
								public void handleEvent(final Event e)
								{
									switch (e.type)
									{
									case SWT.FocusOut:
										item.setText(column, text.getText());
										if (item.getText(1).isEmpty()
										        || item.getText(2).isEmpty())
										{
											item.setBackground(
											        0,
											        Display.getCurrent()
											                .getSystemColor(
											                        SWT.COLOR_YELLOW));
										}
										else
										{
											item.setBackground(
											        0,
											        Display.getCurrent()
											                .getSystemColor(
											                        SWT.COLOR_WHITE));
										}
										text.dispose();
										break;
									case SWT.Traverse:
										switch (e.detail)
										{
										case SWT.TRAVERSE_RETURN:
											item.setText(column, text.getText());
											// FALL THROUGH
										case SWT.TRAVERSE_ESCAPE:
											if (item.getText(1).isEmpty()
											        || item.getText(2)
											                .isEmpty())
											{
												item.setBackground(
												        0,
												        Display.getCurrent()
												                .getSystemColor(
												                        SWT.COLOR_YELLOW));
											}
											else
											{
												item.setBackground(
												        0,
												        Display.getCurrent()
												                .getSystemColor(
												                        SWT.COLOR_WHITE));
											}
											text.dispose();
											e.doit = false;
										}
										break;
									}
								}
							};
							text.addListener(SWT.FocusOut, textListener);
							text.addListener(SWT.Traverse, textListener);
							editor.setEditor(text, item, i);
							text.setText(item.getText(i));
							text.selectAll();
							text.setFocus();
							return;
						}
						if (!visible && rect.intersects(clientArea))
						{
							visible = true;
						}
					}
					if (!visible) return;
					index++;
				}
			}
		});

		m_viewer.getControl().addControlListener(

		new ControlAdapter()
		{
			@Override
			public void controlResized(ControlEvent e)
			{
				Table table = (Table) e.widget;
				int width = table.getBounds().width;
				int w1 = width / 2 - 15;
				int w2 = width - w1 - table.getBorderWidth() * 2 - 15;
				table.getColumn(0).setWidth(15);
				table.getColumn(1).setWidth(w1);
				table.getColumn(2).setWidth(w2);
			}

		});

		addArgument();

		return parent;
	}

	/***************************************************************************
	 * Add an argument
	 **************************************************************************/
	protected void addArgument()
	{
		TableItem item = new TableItem(m_viewer.getTable(), SWT.NONE);
		item.setText(0, Integer.toString(m_viewer.getTable().getItemCount()));
		item.setBackground(0,
		        Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW));
	}

	/***************************************************************************
	 * Delete the last argument
	 **************************************************************************/
	protected void delArgument()
	{
		int numItems = m_viewer.getTable().getItemCount();
		if (numItems > 1)
		{
			m_viewer.getTable().remove(numItems - 1);
		}
	}

	/***************************************************************************
	 * Clear all the arguments
	 **************************************************************************/
	protected void clearArguments()
	{
		m_viewer.getTable().removeAll();
	}

	/***************************************************************************
	 * Create the button bar buttons.
	 * 
	 * @param parent
	 *            The Button Bar.
	 **************************************************************************/
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
		        false);
		createButton(parent, IDialogConstants.CANCEL_ID,
		        IDialogConstants.CANCEL_LABEL, true);
	}

	/***************************************************************************
	 * Called when one of the buttons of the button bar is pressed.
	 * 
	 * @param buttonId
	 *            The button identifier.
	 **************************************************************************/
	protected void buttonPressed(int buttonId)
	{
		switch (buttonId)
		{
		case IDialogConstants.OK_ID:
			try
			{
				if (parseArguments())
				{
					close();
				}
			}
			catch (Exception ex)
			{
				MessageDialog.openError(Display.getCurrent().getActiveShell(),
				        "Argument definition error", ex.getLocalizedMessage());
			}
			break;
		case IDialogConstants.CANCEL_ID:
			m_argExpression = null;
			close();
		}
	}

	/***************************************************************************
	 * Process given arguments
	 **************************************************************************/
	protected boolean parseArguments()
	{
		m_argExpression = new TreeMap<String, String>();
		int count = 0;
		for (TableItem item : m_viewer.getTable().getItems())
		{
			count++;
			String arg = item.getText(1);
			String val = item.getText(2);
			if (arg.trim().isEmpty() || val.trim().isEmpty())
			{
				MessageDialog.openError(getShell(), "Malformed arguments",
				        "Argument " + count + " is incomplete");
				m_argExpression = null;
				return false;
			}
			m_argExpression.put(arg, val);
		}
		return true;
	}
}
