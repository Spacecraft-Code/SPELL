///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.preferences.ui.pages
// 
// FILE      : StatusColorsPreferencePage.java
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
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.StatusColorKey;

public class StatusColorsPreferencePage extends BasicPreferencesPage
{
	/** Color selector */
	private ColorSelector[]	m_colorSelectors;

	@Override
	protected Control createContents(Composite parent)
	{
		// CONTAINER
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, true);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.grabExcessHorizontalSpace = true;
		container.setLayout(layout);
		container.setLayoutData(layoutData);
		/*
		 * Fill the widget with the color chooser controls
		 */
		IConfigurationManager conf = getConfigurationManager();
		m_colorSelectors = new ColorSelector[StatusColorKey.values().length];
		for (StatusColorKey key : StatusColorKey.values())
		{
			Label label = new Label(container, SWT.NONE);
			label.setText(key.description);
			label.setLayoutData(GridDataFactory.copyData(layoutData));

			ColorSelector selector = new ColorSelector(container);
			selector.setColorValue(conf.getColor(key.getPreferenceName())
			        .getRGB());
			m_colorSelectors[key.ordinal()] = selector;
		}
		return container;
	}

	@Override
	public void performApply()
	{
		IConfigurationManager conf = getConfigurationManager();
		for (StatusColorKey key : StatusColorKey.values())
		{
			ColorSelector selector = m_colorSelectors[key.ordinal()];
			RGB rgb = selector.getColorValue();
			conf.setColor(key.getPreferenceName(), rgb);
		}
	}

	@Override
	public void performDefaults()
	{
		IConfigurationManager conf = getConfigurationManager();
		for (StatusColorKey key : StatusColorKey.values())
		{
			conf.resetStatusColor(key);
		}
		refreshPage();
	}

	@Override
	public void refreshPage()
	{
		IConfigurationManager conf = getConfigurationManager();
		for (StatusColorKey key : StatusColorKey.values())
		{
			m_colorSelectors[key.ordinal()].setColorValue(conf.getColor(
			        key.getPreferenceName()).getRGB());
		}
	}
}
