///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.preferences.ui.pages
// 
// FILE      : PresentationsPreferencePage.java
//
// DATE      : 2010-05-27
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
package com.astra.ses.spell.gui.preferences.ui.pages;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;

public class PresentationsPreferencePage extends BasicPreferencesPage
{

	/** Presentations list */
	private List	m_enabledList;
	/** Disabld list */
	private List	m_disabledList;

	@Override
	protected Control createContents(Composite parent)
	{

		GridLayout parentLayout = new GridLayout(3, true);
		GridData parentData = new GridData(GridData.FILL_BOTH);
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(parentLayout);
		container.setLayoutData(parentData);

		Label enabled = new Label(container, SWT.NONE);
		enabled.setText("Enabled presentations");

		Label empty = new Label(container, SWT.NONE);
		empty.setText("");

		Label disabled = new Label(container, SWT.NONE);
		disabled.setText("Disabled presentations");

		/*
		 * Enabled presentations list widget
		 */
		m_enabledList = new List(container, SWT.SINGLE | SWT.BORDER);
		GridData enabledData = new GridData(GridData.FILL_BOTH);
		enabledData.verticalSpan = 4;
		m_enabledList.setLayoutData(enabledData);

		/*
		 * List control button
		 */
		GridData expandButton = new GridData(GridData.FILL_HORIZONTAL);
		expandButton.verticalAlignment = SWT.TOP;

		ImageDescriptor imgDes = Resources.getImage(Resources.IMG_ARROW_UP);
		final Button up = new Button(container, SWT.PUSH);
		up.setText("UP");
		up.setImage(imgDes.createImage());
		up.setEnabled(false);
		up.setLayoutData(GridDataFactory.copyData(expandButton));

		/*
		 * Disable presentations list
		 */
		m_disabledList = new List(container, SWT.SINGLE | SWT.BORDER);
		m_disabledList.setLayoutData(GridDataFactory.copyData(enabledData));

		/*
		 * Rest of the list control buttons
		 */
		imgDes = Resources.getImage(Resources.IMG_ARROW_DOWN);
		final Button down = new Button(container, SWT.PUSH);
		down.setText("DOWN");
		down.setImage(imgDes.createImage());
		down.setEnabled(false);
		down.setLayoutData(GridDataFactory.copyData(expandButton));

		imgDes = Resources.getImage(Resources.IMG_ARROW_RIGHT);;
		final Button disable = new Button(container, SWT.PUSH);
		disable.setText("Disable");
		disable.setImage(imgDes.createImage());
		disable.setEnabled(false);
		disable.setLayoutData(GridDataFactory.copyData(expandButton));

		imgDes = Resources.getImage(Resources.IMG_ARROW_LEFT);
		final Button enable = new Button(container, SWT.PUSH);
		enable.setText("Enable");
		enable.setImage(imgDes.createImage());
		enable.setEnabled(false);
		enable.setLayoutData(GridDataFactory.copyData(expandButton));

		/*
		 * Enabled list listeners
		 */
		m_enabledList.addFocusListener(new FocusListener()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
			}

			@Override
			public void focusGained(FocusEvent e)
			{
				enable.setEnabled(false);
				m_disabledList.deselectAll();
			}
		});

		m_enabledList.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				List list = (List) e.widget;
				boolean presentationSelected = list.getSelectionIndex() != -1;
				up.setEnabled(presentationSelected);
				down.setEnabled(presentationSelected);
				disable.setEnabled(presentationSelected);
				if (m_enabledList.getItemCount() == 1)
				{
					disable.setEnabled(false);
				}
			}
		});

		/*
		 * Disabled list listener
		 */
		m_disabledList.addFocusListener(new FocusListener()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
			}

			@Override
			public void focusGained(FocusEvent e)
			{
				disable.setEnabled(false);
				up.setEnabled(false);
				down.setEnabled(false);
				m_enabledList.deselectAll();
			}
		});

		m_disabledList.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				List list = (List) e.widget;
				boolean presentationSelected = list.getSelectionIndex() != -1;
				enable.setEnabled(presentationSelected);
			}
		});

		/*
		 * Up button selection listener
		 */
		up.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				int selected = m_enabledList.getSelectionIndex();
				// If selected presentation is on top, then return
				if (selected <= 0) { return; }
				switchListElements(m_enabledList, selected, selected - 1);
			}
		});

		/*
		 * Down selection listener
		 */
		down.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				int selected = m_enabledList.getSelectionIndex();
				// If selected presentation is on top, then return
				if (selected == m_enabledList.getItems().length - 1) { return; }
				switchListElements(m_enabledList, selected, selected + 1);
			}
		});

		/*
		 * Enable selection listener
		 */
		enable.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				int selected = m_disabledList.getSelectionIndex();
				if (selected == -1) { return; }
				String presentation = m_disabledList.getItem(selected);
				m_enabledList.add(presentation);
				m_disabledList.remove(selected);
				m_disabledList.deselectAll();
			}
		});

		/*
		 * Disable selection listener
		 */
		disable.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				int selected = m_enabledList.getSelectionIndex();
				if (selected == -1) { return; }
				String presentation = m_enabledList.getItem(selected);
				m_disabledList.add(presentation);
				m_enabledList.remove(selected);
				m_enabledList.deselectAll();
			}
		});

		Label explanation = new Label(container, SWT.WRAP);
		explanation
		        .setText("First presentation in enabled list will be shown by default when opening a procedure");
		GridData explData = new GridData(GridData.FILL_HORIZONTAL);
		explData.horizontalSpan = 3;
		explanation.setLayoutData(explData);

		/*
		 * Fill the list with the preferences contents
		 */
		refreshPage();

		return parent;
	}

	/***************************************************************************
	 * Switch the elements at the given indexes from the given list
	 * 
	 * @param widget
	 * @param firstelement
	 * @param secondElement
	 **************************************************************************/
	private void switchListElements(List list, int firstelement,
	        int secondElement)
	{
		// Get selected presentation
		String first = list.getItem(firstelement);
		String second = list.getItem(secondElement);
		list.setItem(firstelement, second);
		list.setItem(secondElement, first);
		list.select(secondElement);
	}

	@Override
	public void performApply()
	{
		IConfigurationManager conf = getConfigurationManager();
		String[] enabledPresentations = m_enabledList.getItems();
		String[] disabledPresentations = m_disabledList.getItems();
		conf.updatePresentations(enabledPresentations, disabledPresentations);
	}

	@Override
	public void performDefaults()
	{
		IConfigurationManager conf = getConfigurationManager();
		conf.restorePresentations();
		refreshPage();
	}

	@Override
	public void refreshPage()
	{
		IConfigurationManager conf = getConfigurationManager();
		String[] disabled = conf.getDisabledPresentations().toArray(
		        new String[0]);
		m_disabledList.removeAll();
		if (disabled.length > 0)
		{
			m_disabledList.setItems(disabled);
		}
		String[] enabled = conf.getPresentations().toArray(new String[0]);
		m_enabledList.removeAll();
		if (enabled.length > 0)
		{
			m_enabledList.setItems(enabled);
		}
	}
}
