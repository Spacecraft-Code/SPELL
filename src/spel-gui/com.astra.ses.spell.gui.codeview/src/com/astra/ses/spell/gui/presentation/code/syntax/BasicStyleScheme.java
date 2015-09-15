///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.syntax
// 
// FILE      : BasicStyleScheme.java
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
package com.astra.ses.spell.gui.presentation.code.syntax;

import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;

/*******************************************************************************
 * @brief Implementation of a basic style scheme for syntax highlighting
 * @date 27/03/08
 ******************************************************************************/
public class BasicStyleScheme implements IStyleScheme
{
	/** Flag for default style usage warning */
	private static boolean s_firstTimeWarningStyle = true;
	/** Holds the colors associated for each token type */
	protected static TreeMap<TokenTypes, Color> s_colors = new TreeMap<TokenTypes, Color>();

	/** Code-style font */
	private Font m_codeFont = null;
	/** Default style */
	private TextStyle m_defaultStyle = null;
	/** Default style */
	private TextStyle m_selectedStyle = null;
	/** Default style */
	private TextStyle m_highlightedStyle = null;
	/** Currently selected scheme */
	protected SchemeType m_scheme = null;
	/** Currently configured font size */
	protected int m_fontSize = 0;


	/***************************************************************************
	 * Constructor
	 * 
	 * @param initialFontSize
	 *            The initial font size
	 **************************************************************************/
	public BasicStyleScheme(Font font)
	{
		// Initialize the scheme parameters
		setScheme(SchemeType.DAY);
		setFont(font);
		Logger.debug("Color scheme ready", Level.GUI, this);
	}

	/***************************************************************************
	 * Set the desired color scheme.
	 * 
	 * @param s
	 *            The scheme identifier, defined in SchemeType enumeration
	 **************************************************************************/
	@Override
	public void setScheme(SchemeType s)
	{
		// Each time the scheme is changed, colors shall be reloaded
		if (m_scheme != s)
		{
			m_scheme = s;
			loadColorScheme();
		}
	}

	/***************************************************************************
	 * Obtain the text style corresponding to the given token type
	 * 
	 * @param type
	 *            Token type id
	 * @return The corresponding style
	 **************************************************************************/
	@Override
	public TextStyle getStyle(TokenTypes type)
	{
		// Obtain the corresponding color first.
		// If no color is found for the given type, use the default (NORMAL)
		Color c = null;
		if (s_colors.containsKey(type))
		{
			c = s_colors.get(type);
		}
		else
		{
			c = s_colors.get(TokenTypes.NORMAL);
		}
		// Find the corresponding style, depending on the token type.
		TextStyle toApply = null;
		switch (type)
		{
		case NORMAL:
		case SYMBOL:
		case CONSTANT:
		case NUMBER:
		case CODE:
		case MODIFIER:
		case ENTITY:
		case SPELL:
		case STRING:
		case COMMENT:
			toApply = new TextStyle(m_codeFont, c, null);
			break;
		case CRITICAL:
			toApply = new TextStyle(m_codeFont, c, Display.getDefault().getSystemColor(SWT.COLOR_RED));
			break;
		default:
			if (s_firstTimeWarningStyle)
			{
				s_firstTimeWarningStyle = false;
				Logger.warning("Using default style (given " + type + ")", Level.GUI, this);
			}
			toApply = new TextStyle(m_codeFont, c, null);
			break;
		}
		return toApply;
	}

	/***************************************************************************
	 * Set the font to use
	 * 
	 * @param font
	 **************************************************************************/
	@Override
	public void setFont(Font font)
	{
		String face = font.getFontData()[0].getName();
		int size = font.getFontData()[0].getHeight();
		int style = font.getFontData()[0].getStyle() | SWT.NORMAL;

		FontData newData = new FontData(face, size, style);

		Font newFont = new Font(Display.getDefault(), newData);
		if (m_codeFont != null)
		{
			m_codeFont.dispose();
		}
		m_codeFont = newFont;
		m_defaultStyle = new TextStyle(m_codeFont, Display.getCurrent().getSystemColor(SWT.COLOR_BLACK), null);
		m_selectedStyle = new TextStyle(m_codeFont, Display.getCurrent().getSystemColor(SWT.COLOR_WHITE), null);
		m_highlightedStyle = new TextStyle(m_codeFont, Display.getCurrent().getSystemColor(SWT.COLOR_BLACK), null);
	}

	/***************************************************************************
	 * Obtain the currently defined code-style font
	 * 
	 * @return The code font
	 **************************************************************************/
	@Override
	public Font getCodeFont()
	{
		return m_codeFont;
	}

	/***************************************************************************
	 * Obtain the default style
	 * 
	 * @return The default text style
	 **************************************************************************/
	@Override
	public TextStyle getDefaultStyle()
	{
		return m_defaultStyle;
	}

	/***************************************************************************
	 * Obtain the default style
	 * 
	 * @return The default text style
	 **************************************************************************/
	@Override
	public TextStyle getSelectedStyle()
	{
		return m_selectedStyle;
	}

	/***************************************************************************
	 * Obtain the default style
	 * 
	 * @return The default text style
	 **************************************************************************/
	@Override
	public TextStyle getHighlightedStyle()
	{
		return m_highlightedStyle;
	}

	/***************************************************************************
	 * Load colors depending on the configured scheme. Each scheme defines a
	 * different set of colors. There is one color defined per each existing
	 * token type.
	 **************************************************************************/
	protected void loadColorScheme()
	{
		Logger.debug("Loading color schemes", Level.GUI, this);
		// TODO: load colors from configuration file
		s_colors.put(TokenTypes.NORMAL, Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		s_colors.put(TokenTypes.STRING, Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
		s_colors.put(TokenTypes.CODE, Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE));
		s_colors.put(TokenTypes.SPELL, Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));
		s_colors.put(TokenTypes.CRITICAL, Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW));
		s_colors.put(TokenTypes.COMMENT, Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
		s_colors.put(TokenTypes.MODIFIER, new Color(Display.getCurrent(), 170, 115, 0));
		s_colors.put(TokenTypes.ENTITY, new Color(Display.getCurrent(), 91, 17, 166));
		s_colors.put(TokenTypes.SYMBOL, Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));
		s_colors.put(TokenTypes.NUMBER, new Color(Display.getCurrent(), 10, 110, 255));
		s_colors.put(TokenTypes.CONSTANT, new Color(Display.getCurrent(), 140, 0, 180));
		s_colors.put(TokenTypes.BACKGROUND, new Color(Display.getCurrent(), 225, 235, 240));
	}

	/***************************************************************************
	 * Dispose colors
	 **************************************************************************/
	@Override
	public void dispose()
	{
		s_colors.get(TokenTypes.MODIFIER).dispose();
		s_colors.get(TokenTypes.ENTITY).dispose();
		s_colors.get(TokenTypes.NUMBER).dispose();
		s_colors.get(TokenTypes.CONSTANT).dispose();
		s_colors.get(TokenTypes.BACKGROUND).dispose();
	}
}
