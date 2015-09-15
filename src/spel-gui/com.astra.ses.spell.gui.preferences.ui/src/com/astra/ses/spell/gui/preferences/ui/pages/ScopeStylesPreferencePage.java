///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.preferences.ui.pages
// 
// FILE      : ScopeStylesPreferencePage.java
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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.astra.ses.spell.gui.core.model.types.Scope;
import com.astra.ses.spell.gui.preferences.initializer.elements.StyleInfo;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;

/*******************************************************************************
 * 
 * {@link ScopeStylesPreferencePage} allows the user to configure how incoming
 * messages will be rendered in the textual view
 * 
 ******************************************************************************/
public class ScopeStylesPreferencePage extends BasicPreferencesPage
{

	/** Color selector */
	private RGB[]	      m_scopeColors;
	/** Font selector */
	private String[]	  m_scopeFonts;
	/** Scope styles */
	private Integer[]	  m_scopeStyles;

	/** Scope selection widget */
	private Combo	      m_scopesCombo;
	/** Color selection widget */
	private ColorSelector	m_colorSelector;
	/** Font style combo */
	// private Combo m_fontCombo;
	/** Style buttons */
	private Button	      m_boldButton;
	private Button	      m_italicButton;

	@Override
	protected Control createContents(Composite parent)
	{
		// Container
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.grabExcessHorizontalSpace = true;
		container.setLayout(layout);
		container.setLayoutData(layoutData);

		/*
		 * Scopes group
		 */
		Group scopesGroup = new Group(container, SWT.BORDER);
		GridLayout scopesGroupLayout = new GridLayout(2, false);
		scopesGroup.setLayout(scopesGroupLayout);
		scopesGroup.setLayoutData(GridDataFactory.copyData(layoutData));
		scopesGroup.setText("Scope selection");

		Label scopesLabel = new Label(scopesGroup, SWT.NONE);
		scopesLabel.setText("Scope");

		m_scopesCombo = new Combo(scopesGroup, SWT.READ_ONLY | SWT.SINGLE);
		for (Scope scope : Scope.values())
		{
			m_scopesCombo.add(scope.name());
		}

		/*
		 * Styles group
		 */
		Group stylesGroup = new Group(container, SWT.BORDER);
		GridLayout stylesGroupLayout = new GridLayout(2, true);
		stylesGroup.setLayout(stylesGroupLayout);
		stylesGroup.setLayoutData(GridDataFactory.copyData(layoutData));
		stylesGroup.setText("Scope style properties");

		// Color
		Label styleColor = new Label(stylesGroup, SWT.NONE);
		styleColor.setText("Color");

		m_colorSelector = new ColorSelector(stylesGroup);

		// Font
		/*
		 * Label styleFont = new Label(stylesGroup, SWT.NONE);
		 * styleFont.setText("Font style");
		 * 
		 * m_fontCombo = new Combo(stylesGroup, SWT.READ_ONLY|SWT.SINGLE); for
		 * (FontKey font : FontKey.values()) {
		 * m_fontCombo.add(font.description);
		 * m_fontCombo.setData(font.description, font); }
		 */

		// Style
		Label styleLabel = new Label(stylesGroup, SWT.NONE);
		styleLabel.setText("Style");

		Composite fontStyles = new Composite(stylesGroup, SWT.NONE);
		fontStyles.setLayout(new GridLayout(2, true));

		m_boldButton = new Button(fontStyles, SWT.CHECK);
		m_boldButton.setText("Bold");
		m_boldButton.setData(SWT.BOLD);

		m_italicButton = new Button(fontStyles, SWT.CHECK);
		m_italicButton.setText("Italic");
		m_italicButton.setData(SWT.ITALIC);

		// Initialize listeners
		// Add a listener so when the combo selection changes style widgets must
		// be refreshed
		m_scopesCombo.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Combo combo = (Combo) e.widget;
				updateSelection(combo.getSelectionIndex());
			}
		});

		// when the style color selector changes, then update the color in the
		// model
		m_colorSelector.addListener(new IPropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent event)
			{
				if (event.getProperty().equals(ColorSelector.PROP_COLORCHANGE))
				{
					m_scopeColors[m_scopesCombo.getSelectionIndex()] = (RGB) event
					        .getNewValue();
				}
			}
		});

		/*
		 * m_fontCombo.addSelectionListener(new SelectionAdapter() { public void
		 * widgetSelected(SelectionEvent e) { Combo fontCombo = (Combo)
		 * e.widget; String font =
		 * fontCombo.getItem(fontCombo.getSelectionIndex()); FontKey key =
		 * (FontKey) fontCombo.getData(font);
		 * m_scopeFonts[scopesCombo.getSelectionIndex()] = key.toString(); } });
		 */

		SelectionListener fontStyleListener = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Button styleButton = (Button) e.widget;

				int style = (Integer) styleButton.getData();
				int currentStyle = m_scopeStyles[m_scopesCombo
				        .getSelectionIndex()];

				if (styleButton.getSelection())
				{
					currentStyle = currentStyle | style;
				}
				else
				{
					currentStyle = currentStyle & (~~~style);
				}

				m_scopeStyles[m_scopesCombo.getSelectionIndex()] = currentStyle;
			}
		};
		m_boldButton.addSelectionListener(fontStyleListener);
		m_italicButton.addSelectionListener(fontStyleListener);

		initializeValues();
		// Initialize groups
		m_scopesCombo.select(0);
		updateSelection(0);

		return container;
	}

	@Override
	public void performApply()
	{
		/*
		 * store styles information
		 */
		IConfigurationManager conf = getConfigurationManager();
		for (Scope scope : Scope.values())
		{
			RGB color = m_scopeColors[scope.ordinal()];
			String font = m_scopeFonts[scope.ordinal()];
			int style = m_scopeStyles[scope.ordinal()];
			StyleInfo styleInfo = new StyleInfo(scope.tag, font, color.red,
			        color.green, color.blue, style);
			conf.setScopeStyleInfo(scope, styleInfo);
		}
	}

	@Override
	public void performDefaults()
	{
		IConfigurationManager conf = getConfigurationManager();
		conf.restoreScopeStyleInfo();
		initializeValues();
		updateSelection(m_scopesCombo.getSelectionIndex());
	}

	/***************************************************************************
	 * Get the values from preferences
	 **************************************************************************/
	private void initializeValues()
	{
		IConfigurationManager conf = getConfigurationManager();

		m_scopeColors = new RGB[Scope.values().length];
		m_scopeFonts = new String[Scope.values().length];
		m_scopeStyles = new Integer[Scope.values().length];

		for (Scope scope : Scope.values())
		{
			Color color = conf.getScopeColor(scope);
			String font = conf.getScopeFont(scope);
			int style = conf.getScopeStyle(scope);

			m_scopeColors[scope.ordinal()] = color.getRGB();
			m_scopeFonts[scope.ordinal()] = font;
			m_scopeStyles[scope.ordinal()] = style;
		}
	}

	/***************************************************************************
	 * Update the widgets information with the values related to the scope whose
	 * index is the given one
	 * 
	 * @param index
	 *            the scope index. See {@link Scope}
	 **************************************************************************/
	private void updateSelection(int index)
	{
		m_colorSelector.setColorValue(m_scopeColors[index]);

		// String font = m_scopeFonts[index];
		// FontKey key = FontKey.valueOf(font);
		// m_fontCombo.select(key.ordinal());

		m_boldButton.setSelection((SWT.BOLD & m_scopeStyles[index]) != 0);
		m_italicButton.setSelection((SWT.ITALIC & m_scopeStyles[index]) != 0);
	}

	@Override
	public void refreshPage()
	{
		initializeValues();
		updateSelection(m_scopesCombo.getSelectionIndex());
	}
}
