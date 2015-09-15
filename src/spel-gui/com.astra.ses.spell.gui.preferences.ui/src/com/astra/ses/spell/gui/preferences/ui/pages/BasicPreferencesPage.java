///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.preferences.ui.pages
// 
// FILE      : BasicPreferencesPage.java
//
// DATE      : Mar 18, 2011
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
package com.astra.ses.spell.gui.preferences.ui.pages;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.preferences.initializer.GUIPreferencesLoader;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;

/*******************************************************************************
 * 
 * {@link BasicPreferencesPage} is the base implementation of a SPELL preference
 * page. Apart from being a preference page, it listen to possible outer
 * preference modifications to refresh this page
 * 
 ******************************************************************************/
public abstract class BasicPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage,
        IPropertyChangeListener
{
	/** Configuration manager */
	private IConfigurationManager	m_conf;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public BasicPreferencesPage()
	{
		super();
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public BasicPreferencesPage(String title)
	{
		super(title);
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public BasicPreferencesPage(String title, ImageDescriptor image)
	{
		super(title, image);
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

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 * =========================================================================
	 */
	@Override
	public void init(IWorkbench workbench)
	{
		m_conf = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		m_conf.addPropertyChangeListener(this);
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 * =========================================================================
	 */
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

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performCancel()
	 * =========================================================================
	 */
	@Override
	public boolean performCancel()
	{
		m_conf.removePropertyChangeListener(this);
		return super.performCancel();
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 * =========================================================================
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		String property = event.getProperty();
		if (property.equals(GUIPreferencesLoader.REFRESH_PROPERTY))
		{
			refreshPage();
		}
	}

	/***************************************************************************
	 * Refresh page widgets with the values stored at the preferences node
	 **************************************************************************/
	public abstract void refreshPage();
}
