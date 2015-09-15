///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs
// 
// FILE      : PropertiesDialog.java
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

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.ProcProperties;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;

/*******************************************************************************
 * @brief Dialog for defining a scheduling condition
 * @date 18/09/07
 ******************************************************************************/
public class PropertiesDialog extends TitleAreaDialog
{
	public static final String ID = "com.astra.ses.spell.gui.dialogs.PropertiesDialog";

	/** Holds the dialog image icon */
	private Image m_image;
	/** Holds the procedure model */
	private Map<ProcProperties, String> m_properties;
	/** Holds the tab for general properties */
	private Composite m_tabGenerics;
	/** Holds the tab for history of changes */
	private Composite m_tabHistory;
	/** Holds the history entries, if any */
	private ArrayList<String> m_history;
	/** Holds the history table */
	private Table m_table;
	/** Holds the table viewer */
	private TableViewer m_historyViewer;


	/** Table viewer content provider for the history table */
	class HistoryContentProvider implements IStructuredContentProvider
	{
		@Override
		public void dispose()
		{
		};

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
		{
		};

		@Override
		public Object[] getElements(Object inputElement)
		{
			return m_history.toArray();
		}
	}

	/***************************************************************************
	 * Constructor
	 * 
	 * @param shell
	 *            The parent shell
	 **************************************************************************/
	public PropertiesDialog(Shell shell, Map<ProcProperties, String> properties)
	{
		super(shell);
		// Obtain the image for the dialog icon
		ImageDescriptor descr = Activator.getImageDescriptor("icons/dlg_exec.png");
		m_image = descr.createImage();
		m_properties = properties;
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
		String name = m_properties.get(ProcProperties.PROC_NAME);
		setMessage("Properties of procedure '" + name + "'");
		setTitle("Procedure properties");
		setTitleImage(m_image);
		getShell().setText("Procedure properties");
		return contents;
	}

	/***************************************************************************
	 * Create the dialog area contents.
	 * 
	 * @param parent
	 *            The base composite of the dialog
	 * @return The resulting contents
	 **************************************************************************/
	@Override
	protected Control createDialogArea(Composite parent)
	{
		// Main composite of the dialog area -----------------------------------
		Composite top = new Composite(parent, SWT.NONE);
		GridData areaData = new GridData(GridData.FILL_BOTH);
		areaData.widthHint = 600;
		top.setLayoutData(areaData);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		top.setLayout(layout);

		TabFolder folder = new TabFolder(top, SWT.BORDER);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		createGenericProperties(folder);
		createHistoryOfChanges(folder);

		folder.layout();

		return parent;
	}

	/***************************************************************************
	 * Create the generic properties
	 **************************************************************************/
	protected void createGenericProperties(TabFolder folder)
	{
		TabItem itemGenerics = new TabItem(folder, SWT.NONE);
		itemGenerics.setText("General properties");
		m_tabGenerics = new Composite(folder, SWT.BORDER);
		m_tabGenerics.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout tabLayout1 = new GridLayout();
		tabLayout1.numColumns = 2;
		tabLayout1.marginHeight = 15;
		m_tabGenerics.setLayout(tabLayout1);
		itemGenerics.setControl(m_tabGenerics);

		for (ProcProperties prop : ProcProperties.values())
		{
			if (prop.equals(ProcProperties.PROC_HISTORY))
				continue;
			String title = prop.tag.substring(0, 1).toUpperCase() + prop.tag.substring(1);
			addGeneric(title, prop, prop.multiline);
		}

		m_tabGenerics.pack();
	}

	/***************************************************************************
	 * Add a generic property
	 **************************************************************************/
	protected void addGeneric(String title, ProcProperties tag, boolean multi)
	{
		Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);

		Label label = new Label(m_tabGenerics, SWT.NONE);
		label.setText(title + ":");
		label.setFont(boldFont);

		Text text = null;
		if (multi)
		{
			text = new Text(m_tabGenerics, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		}
		else
		{
			text = new Text(m_tabGenerics, SWT.BORDER);
		}
		String value = m_properties.get(tag);
		if (value == null)
			value = "(?)";
		text.setText(value);
		text.setEditable(false);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	/***************************************************************************
	 * Create the history of changes
	 **************************************************************************/
	protected void createHistoryOfChanges(TabFolder folder)
	{
		TabItem itemHistory = new TabItem(folder, SWT.NONE);
		itemHistory.setText("History of changes");
		m_tabHistory = new Composite(folder, SWT.BORDER);
		GridLayout tabLayout2 = new GridLayout();
		tabLayout2.numColumns = 1;
		m_tabHistory.setLayout(tabLayout2);
		itemHistory.setControl(m_tabHistory);

		m_history = new ArrayList<String>();
		parseHistory();

		m_table = new Table(m_tabHistory, SWT.NONE);
		m_historyViewer = new TableViewer(m_table);
		m_historyViewer.setContentProvider(new HistoryContentProvider());
		m_historyViewer.setInput(m_history);
		m_table.setHeaderVisible(true);
		m_table.setLinesVisible(true);
		m_table.setLayoutData(new GridData(GridData.FILL_BOTH));
		IConfigurationManager mgr = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		m_table.setFont(mgr.getFont(FontKey.CODE));
		m_tabHistory.pack();
	}

	/***************************************************************************
	 * Parse the history data
	 **************************************************************************/
	protected void parseHistory()
	{
		String history = m_properties.get(ProcProperties.PROC_HISTORY);
		if (history == null || history.isEmpty())
			return;
		String[] tokens = history.split("\n");
		for (String line : tokens)
		{
			m_history.add(line);
		}
	}

	/***************************************************************************
	 * Create the button bar buttons.
	 * 
	 * @param parent
	 *            The Button Bar.
	 **************************************************************************/
	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	/***************************************************************************
	 * Called when one of the buttons of the button bar is pressed.
	 * 
	 * @param buttonId
	 *            The button identifier.
	 **************************************************************************/
	@Override
	protected void buttonPressed(int buttonId)
	{
		close();
	}
}
