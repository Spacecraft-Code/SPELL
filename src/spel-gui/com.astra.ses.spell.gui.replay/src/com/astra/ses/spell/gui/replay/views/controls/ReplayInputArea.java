///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.input
// 
// FILE      : ReplayInputArea.java
//
// DATE      : 2008-11-21 13:54
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
package com.astra.ses.spell.gui.replay.views.controls;

import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.InputData;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.PromptDisplayType;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.interfaces.IControlArea;
import com.astra.ses.spell.gui.interfaces.ISashListener;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;

/*******************************************************************************
 * @brief Special control for showing user prompts in the tabular view
 * @date 20/10/07
 ******************************************************************************/
public class ReplayInputArea extends Composite implements ISashListener
{
	private static final String KEY_SEPARATOR = ":";
	private static IConfigurationManager s_cfg = null;

	/** Maximum font size */
	private static final int MAX_FONT_SIZE = 16;
	/** Minimum font size */
	private static final int MIN_FONT_SIZE = 7;
	/** Currently selected font size */
	private int m_fontSize;
	/** Created font for internal use */
	private Font m_myFont = null;
	/** Text field for text prompts */
	private Text m_promptText;
	/** Future storing the prompt answers */
	private InputData m_promptData;
	/** Parent view of the input area */
	private IControlArea m_parent;
	/** Holds the radio buttons for options */
	private ArrayList<Button> m_optionsRadio;
	/** Holds the combo widget for options */
	private Combo m_optionsCombo;
	/** Holds the type of widget for options */
	private PromptDisplayType m_promptDisplayType;
	/** Provides the scrolling of option buttons */
	private ScrolledComposite m_optionScroll;
	/** Holds the set of radio buttons */
	private Composite m_optionContainer;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param view
	 *            The parent procedure view
	 * @param top
	 *            The dictionary composite
	 **************************************************************************/
	public ReplayInputArea(IControlArea area)
	{
		super(area.getControl(), SWT.NONE);
		m_parent = area;
		m_promptData = null;

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginBottom = 0;
		layout.numColumns = 1;
		setLayout(layout);

		setLayoutData(new GridData(GridData.FILL_BOTH));

		m_promptText = new Text(this, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		m_promptText.setText("(no prompt active)");
		m_promptText.getVerticalBar().setVisible(false);
		GridData tpData = new GridData(GridData.FILL_HORIZONTAL);
		m_promptText.setLayoutData(tpData);
		m_promptText.setBackground( Display.getCurrent().getSystemColor(SWT.COLOR_WHITE) );

		if (s_cfg == null)
		{
			s_cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		}

		Composite inputGroup = new Composite(this, SWT.NONE);
		GridLayout ig_layout = new GridLayout(3,false);
		ig_layout.marginHeight = 0;
		ig_layout.marginTop = 0;
		ig_layout.marginBottom = 0;
		ig_layout.marginWidth = 0;
		ig_layout.marginLeft = 3;
		ig_layout.marginRight = 3;
		inputGroup.setLayout(ig_layout);
		inputGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		m_optionsRadio = new ArrayList<Button>();
		m_optionsCombo = null;
		m_optionScroll = null;

		m_fontSize = s_cfg.getFont(FontKey.TEXT).getFontData()[0].getHeight();
		updateFontFromSize();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setEnabled(boolean enabled)
	{
		if (enabled) return;
		m_promptText.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void zoom(boolean increase)
	{
		boolean changed = true;
		if (increase)
		{
			m_fontSize++;
			if (m_fontSize > MAX_FONT_SIZE)
			{
				m_fontSize = MAX_FONT_SIZE;
				changed = false;
			}
		}
		else
		{
			m_fontSize--;
			if (m_fontSize < MIN_FONT_SIZE)
			{
				m_fontSize = MIN_FONT_SIZE;
				changed = false;
			}
		}
		if (changed)
		{
			updateFontFromSize();
		}
	}

	/***************************************************************************
	 * Set the table font
	 **************************************************************************/
	private void updateFontFromSize()
	{
		if (m_myFont != null)
		{
			m_myFont.dispose();
		}
		setRedraw(false);
		m_myFont = new Font(Display.getDefault(),"Courier New", m_fontSize, SWT.NORMAL);
		m_promptText.setFont(m_myFont);
		
		try
		{
			if (m_optionContainer != null && !m_optionContainer.isDisposed())
			{
				for (Button opt : m_optionsRadio)
				{
					opt.setFont(m_myFont);
				}
				m_optionContainer.pack();
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		setRedraw(true);
		m_parent.getControl().layout();
	}

	/***************************************************************************
	 * Set the table font
	 **************************************************************************/
	public void setFont(Font font)
	{
		m_fontSize = font.getFontData()[0].getHeight();
		updateFontFromSize();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean setFocus()
	{
		return true;
	}

	/***************************************************************************
	 * Issue a textual prompt
	 * 
	 * @param prompt
	 *            Prompt text
	 * @param value
	 *            Future for storing the answer
	 * @param isNumeric
	 *            Numeric prompt flag
	 **************************************************************************/
	public void prompt(InputData promptData)
	{
		Logger.debug("Start prompt", Level.PROC, this);
		reset();
		m_promptData = promptData;
		setupPrompt();
	}

	/***************************************************************************
	 * Shows a prompt 
	 **************************************************************************/
	private void setupPrompt()
	{
		if (m_promptData.isList())
		{
			// Set the prompt text
			m_promptText.setText(m_promptData.getText());

			// Adjust the height and visibility of scroll bar depending on
			// the control doing text wrapping or not
			int areaWidth = m_promptText.getClientArea().width;
			GC gc = new GC(m_promptText);
			int textWidth = gc.getFontMetrics().getAverageCharWidth() * m_promptText.getText().length();
			boolean wrapping = textWidth >= areaWidth; 
			gc.dispose();
			m_promptText.getVerticalBar().setVisible(wrapping);
			if (wrapping)
			{
				GridData gd = (GridData) m_promptText.getLayoutData();
				gd.heightHint = 45;
				layout();
			}

			m_promptDisplayType = m_promptData.getPromptDisplayType();
			// Build the option list and show the option composite
			updateOptions(m_promptData.getOptions(), m_promptData.getExpected());
		}
		else
		{
			m_promptText.setText(m_promptData.getText());

		}
		if (m_promptDisplayType == PromptDisplayType.RADIO)
		{
			for (Button opt : m_optionsRadio)
			{
				opt.setEnabled(false);
			}
		}
		else
		{
			m_optionsCombo.setEnabled(false);
		}
	}

	/***************************************************************************
	 * Reset the input area and show the no input page
	 * 
	 * @param resetAll
	 *            If true, reset automatically all the rest of input handlers
	 **************************************************************************/
	public void reset()
	{
		Logger.debug("Reset input area", Level.PROC, this);
		m_promptText.setText("");
		GridData gd = (GridData) m_promptText.getLayoutData();
		gd.heightHint = SWT.DEFAULT;
		m_promptText.getVerticalBar().setVisible(false);
		clearOptions();
		m_promptData = null;
	}

	/***************************************************************************
	 * Clear the option buttons
	 **************************************************************************/
	private void clearOptions()
	{
		if (m_optionsRadio.size() > 0)
		{
			for (Button opt : m_optionsRadio)
			{
				opt.dispose();
			}
			m_optionsRadio.clear();
		}
		if (m_optionScroll != null)
		{
			m_optionContainer.dispose();
			m_optionScroll.dispose();
			m_optionScroll = null;
		}
		if (m_optionsCombo != null)
		{
			m_optionsCombo.dispose();
			m_optionsCombo = null;
		}
	}

	/***************************************************************************
	 * Update the available options
	 * 
	 * @param options
	 * @param expectedValues
	 **************************************************************************/
	private void updateOptions(Vector<String> options, Vector<String> expectedValues)
	{
		if (options == null || options.size() == 0)
			return;

		m_optionScroll = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
		// DO NOT PUT LAYOUT DATA FOR THE SCROLLED: IT IS ADJUSTED WITH THE SASH EVENTS!
		
		m_optionContainer = new Composite(m_optionScroll, SWT.NONE);
		m_optionContainer.setLayout( new GridLayout(1,true) );
		
		int count = 0;
		if (m_promptDisplayType == PromptDisplayType.RADIO)
		{
			for (String option : options)
			{
				String expected = expectedValues.elementAt(count);

				Button b = new Button(m_optionContainer, SWT.RADIO);
				b.setFont(m_myFont);
				String value = option.substring(option.indexOf(KEY_SEPARATOR) + 1, option.length());
				// Take into account the LIST|ALPHA case. When keys are the same
				// as values,
				// we do not want to display it twice.
				if (!expected.equals(value))
				{
					b.setText(expected + " : " + value);
				}
				else
				{
					b.setText(value);
				}
				b.setSelection(false);
				b.setData("ID", count);
				m_optionsRadio.add(b);
				count++;
			}
		}
		else
		{
			m_optionsCombo = new Combo(m_optionContainer, SWT.DROP_DOWN | SWT.READ_ONLY);
			m_optionsCombo.setData("IDs", expectedValues);
			for (String option : options)
			{
				m_optionsCombo.add(option.substring(option.indexOf(KEY_SEPARATOR) + 1, option.length()));
			}

		}
		m_optionContainer.pack();
		m_optionScroll.setContent(m_optionContainer);
	}

	@Override
    public void onSashMoved( int height )
    {
		if (m_optionScroll != null)
		{
			int substract = m_promptText.getBounds().height + 25;
			int finalHeight = height - substract;;
			if (finalHeight<40) finalHeight=40;
			m_optionScroll.setSize( getClientArea().width, finalHeight );
		}
    }
}
