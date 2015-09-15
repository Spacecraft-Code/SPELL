///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.preferences
// 
// FILE      : CodeViewPreferencePage.java
//
// DATE      : Aug 20, 2013
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
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.presentation.code.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.PropertyKey;

public class CodeViewPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IPropertyChangeListener
{
	/** Configuration manager */
	private IConfigurationManager	m_conf;
	/** Show date line or not */
	private Button m_useDateLine;
	/** What fields should go in the data column */
	private Combo	m_displayData;
	/** Max items kept for each code line */
	private Text	m_lineHistoryItems;

	
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public CodeViewPreferencePage()
	{
		super();
		setDescription("Preferences for Code View presentation\n");
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void init(IWorkbench workbench)
	{
		m_conf = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		m_conf.addPropertyChangeListener(this);
	}
	
	/***************************************************************************
	 * Get the configuration manager to use for retrieving or storing
	 * preferences values
	 * 
	 * @return
	 **************************************************************************/
	public IConfigurationManager getConfigurationManager()
	{
		return m_conf;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean performOk()
	{
		if (okToLeave())
		{
			performApply();
			m_conf.removePropertyChangeListener(this);
		}
		return okToLeave();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean performCancel()
	{
		m_conf.removePropertyChangeListener(this);
		return super.performCancel();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	protected Control createContents( Composite parent )
	{
		// CONTAINER
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, true);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		container.setLayout(layout);
		container.setLayoutData(layoutData);

		// Show date line
		Label showDateLineLabel = new Label (container, SWT.NONE);
		showDateLineLabel.setText("Show datetime on each line");
		m_useDateLine = new Button(container, SWT.CHECK);
		m_useDateLine.setSelection(true);
		new Label(container, SWT.NONE);

		// What fields should go in the data column
		Label displayDataLabel = new Label(container, SWT.NONE);
		displayDataLabel.setText("Fields in data column");
		// Browse button
		m_displayData = new Combo(container, SWT.READ_ONLY);
		m_displayData.add("Name");
		m_displayData.add("Value");
		m_displayData.add("Both name and value");
		new Label(container, SWT.NONE);
		
		// Max items kept for each code line
		new Label(container, SWT.NONE).setText("Line history");
		m_lineHistoryItems = new Text(container, SWT.BORDER | SWT.RIGHT);
		m_lineHistoryItems.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		new Label(container, SWT.NONE).setText("items");
		
		refreshPage();

		return container;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		String property = event.getProperty();
		if (property.equals("shall_refresh_page"))
		{
			refreshPage();
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void performApply()
	{
		// time line 
		boolean timeLine = m_useDateLine.getSelection();
		m_conf.setBooleanProperty(PropertyKey.SHOW_DATE_LINE, timeLine);
		// display data
		String displayData = m_displayData.getItem(m_displayData.getSelectionIndex());
		if (displayData.equals("Name"))
		{
			m_conf.setProperty(PropertyKey.DISPLAY_DATA, "NAME");
		}
		else if (displayData.equals("Value"))
		{
			m_conf.setProperty(PropertyKey.DISPLAY_DATA, "VALUE");
		}
		else
		{
			m_conf.setProperty(PropertyKey.DISPLAY_DATA, "BOTH");
		}
		// line history items
		String lineHistoryItems = m_lineHistoryItems.getText();
		m_conf.setProperty(PropertyKey.LINE_HISTORY_ITEMS, lineHistoryItems);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void refreshPage()
	{
		boolean showDateLine = m_conf.getBooleanProperty(PropertyKey.SHOW_DATE_LINE);
		m_useDateLine.setSelection(showDateLine);
		
		String displayData = m_conf.getProperty(PropertyKey.DISPLAY_DATA);
		if (displayData.equals("NAME"))
		{
			m_displayData.select(0);
		}
		else if (displayData.equals("VALUE"))
		{
			m_displayData.select(1);
		}
		else // BOTH
		{
			m_displayData.select(2);
		}
		
		String lineHistoryItems = m_conf.getProperty(PropertyKey.LINE_HISTORY_ITEMS);
		m_lineHistoryItems.setText(lineHistoryItems);
	}

}