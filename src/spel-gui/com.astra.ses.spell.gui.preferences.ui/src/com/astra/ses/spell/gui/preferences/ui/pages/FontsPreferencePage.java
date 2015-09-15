///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.preferences.ui.pages
// 
// FILE      : FontsPreferencePage.java
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

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.preferences.Activator;
import com.astra.ses.spell.gui.preferences.initializer.GUIPreferencesLoader;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;

public class FontsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{

	private static final String	  TEST_TEXT	= "Lorem ipsum";
	/** Configuration manager */
	private IConfigurationManager	m_conf;
	/** Color selector */
	private FontFieldEditor[]	  m_colorSelectors;

	/***************************************************************************
	 * Default constructor
	 **************************************************************************/
	public FontsPreferencePage()
	{
		super(GRID);
	}

	/***************************************************************************
	 * Constructor defining a
	 * 
	 * @param title
	 **************************************************************************/
	public FontsPreferencePage(String title)
	{
		super(title, GRID);
	}

	@Override
	protected void createFieldEditors()
	{
		// TODO Auto-generated method stub
		m_colorSelectors = new FontFieldEditor[FontKey.values().length];
		Composite parent = getFieldEditorParent();
		for (FontKey key : FontKey.values())
		{
			FontFieldEditor selector = new FontFieldEditor(key.getPreferenceName(), key.description, TEST_TEXT, parent);
			m_colorSelectors[key.ordinal()] = selector;
			addField(selector);
		}
	}

	@Override
	public void init(IWorkbench workbench)
	{
		m_conf = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	public void performApply()
	{
		for (FontKey key : FontKey.values())
		{
			FontFieldEditor selector = m_colorSelectors[key.ordinal()];
			Font font = selector.getPreviewControl().getFont();
			m_conf.setFont(key, font);
		}
	}

	@Override
	public boolean performOk()
	{
		performApply();
		return okToLeave();
	}

	@Override
	public void propertyChange(PropertyChangeEvent event)
	{

		if (event.getProperty().equals(GUIPreferencesLoader.REFRESH_PROPERTY))
		{
			refreshPage();
		}
		else
		{
			super.propertyChange(event);
		}
	}

	/***************************************************************************
	 * Refresh this page with appropiate values
	 **************************************************************************/
	public void refreshPage()
	{
		for (FontKey key : FontKey.values())
		{
			m_colorSelectors[key.ordinal()].load();
		}
	}
}
