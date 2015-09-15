///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.shell.controls
// 
// FILE      : ShellTerminal.java
//
// DATE      : Sep 27, 2010
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
package com.astra.ses.spell.gui.presentation.shell.controls;

import java.util.ArrayList;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.presentation.shell.utils.KeyCodes;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.language.ParseException;
import com.astra.ses.spell.language.Parser;
import com.astra.ses.spell.language.model.TokenMgrError;

/******************************************************************************
 * 
 * 
 *****************************************************************************/
public class ShellTerminal extends StyledText implements KeyListener, VerifyKeyListener, MouseListener,
        IPropertyChangeListener
{
	private static final String	PROMPT_STRING	= ">>> ";
	private static final String	MULTI_STRING	= "... ";
	private static final int	TAB_SIZE	   = 4;
	private static final String	LINE_SEPARATOR	= System.getProperty("line.separator");

	/** Font range */
	private static final int	FONT_RANGE	   = 4;
	/** Maximum and minimum font sizes */
	private int	                m_maxfontSize;
	private int	                m_minFontSize;

	/** Holds the parser */
	private Parser	            m_parser;

	/** Multiline input flag */
	private boolean	            m_multi;
	private int	                m_inputStart;
	/** Holds the current line start position */
	private int	                m_lineStart;
	/** Indentation level */
	private int	                m_indentationLevel;

	/** Holds the history of single-line inputs */
	private ArrayList<String>	m_history;
	/** Holds the current position in the history */
	private int	                m_historyIndex;

	/** Procedure model */
	private IProcedure	        m_model;

	/**************************************************************************
	 * Constructor.
	 * 
	 * @param parent
	 * @param style
	 *************************************************************************/
	public ShellTerminal(Composite parent, int style, IProcedure model)
	{
		super(parent, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);

		m_model = model;

		m_parser = new Parser();

		m_lineStart = 0;
		m_indentationLevel = 0;

		m_inputStart = 0;
		m_multi = false;

		m_history = new ArrayList<String>();
		m_historyIndex = 0;

		// Setup initial font
		IConfigurationManager cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);

		Font font = cfg.getFont(FontKey.CODE);
		int fontSize = font.getFontData()[0].getHeight();
		m_minFontSize = Math.max(1, fontSize - FONT_RANGE);
		m_maxfontSize = fontSize + FONT_RANGE;
		setFont(font);

		// Other settings
		setTabs(4);
		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

		append(PROMPT_STRING);

		addKeyListener(this);
		addVerifyKeyListener(this);
		addMouseListener(this);
		cfg.addPropertyChangeListener(this);

		parent.addDisposeListener(new DisposeListener()
		{
			@Override
			public void widgetDisposed(DisposeEvent e)
			{
				unsubuscribeFromPreferences();
			}
		});
	}

	/**************************************************************************
	 * Receive focus
	 *************************************************************************/
	@Override
	public boolean setFocus()
	{
		int offset = getCaretOffset();
		int limit = m_lineStart + PROMPT_STRING.length();
		if ((offset <= limit) && (m_lineStart <= offset))
		{
			setCaretOffset(getText().length());
		}
		return super.setFocus();
	}

	/**************************************************************************
	 * Enable subclassing
	 *************************************************************************/
	@Override
	protected void checkSubclass()
	{
	};

	/**************************************************************************
	 * Zoom the control
	 *************************************************************************/
	public void zoom(boolean increase)
	{
		Font oldFont = getFont();
		// Get the font size
		int fontSize = oldFont.getFontData()[0].getHeight();

		if (increase && (fontSize == m_maxfontSize)) return;
		if (!increase && (fontSize == m_minFontSize)) return;
		if (increase)
		{
			fontSize = Math.min(fontSize + 1, m_maxfontSize);
		}
		else
		{
			fontSize = Math.max(fontSize - 1, m_minFontSize);
		}
		FontData[] fdata = oldFont.getFontData();
		for (int index = 0; index < fdata.length; index++)
		{
			fdata[index].setHeight(fontSize);
		}
		Font newFont = new Font(Display.getCurrent(), fdata);
		setFont(newFont);
		oldFont.dispose();
		redraw();
	}

	/**************************************************************************
	 * Add procedure output
	 *************************************************************************/
	public void addOutput(String msg)
	{
		String last = getText().substring(m_lineStart);
		setRedraw(false);
		if (last.equals(PROMPT_STRING))
		{
			setText(getText().substring(0, m_lineStart));
		}
		append(msg + LINE_SEPARATOR);
		addPromptString();
		setRedraw(true);
		m_inputStart = getText().length();
	}

	/**************************************************************************
	 * Clear the shell
	 *************************************************************************/
	public void clear()
	{
		m_inputStart = 0;
		m_lineStart = 0;
		m_multi = false;
		setText("");
		addPromptString();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private String getInput()
	{
		int end = getText().length() - LINE_SEPARATOR.length();
		String input = "";
		if (m_inputStart < end)
		{
			input = getText().substring(m_inputStart, end);
			input = input.replace(PROMPT_STRING, "");
			input = input.replace(MULTI_STRING, "");
		}
		return input;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private String getCurrentLine()
	{
		int end = getText().length();
		String input = "";
		if (m_inputStart < end)
		{
			input = (getText().substring(m_lineStart, end));
			input = input.replace(PROMPT_STRING, "");
			input = input.replace(MULTI_STRING, "");
		}
		return input;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private boolean isExpressionStart()
	{
		return getText().trim().endsWith(":");
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void resetInput()
	{
		m_multi = false;
		m_indentationLevel = 0;
		m_inputStart = getText().length();
		m_lineStart = m_inputStart - PROMPT_STRING.length();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void showLastLine()
	{
		setCaretOffset(getText().length());
		showSelection();
	}

	/**************************************************************************
	 * Add the prompt string
	 *************************************************************************/
	private void addPromptString()
	{
		if (m_multi)
		{
			append(MULTI_STRING);
		}
		else
		{
			append(PROMPT_STRING);
		}

		m_lineStart = getText().length() - PROMPT_STRING.length();

		for (int count = 0; count < m_indentationLevel; count++)
		{
			for (int c = 0; c < TAB_SIZE; c++)
				append(" ");
		}
		showLastLine();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void indent()
	{
		m_indentationLevel++;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void dedent()
	{
		if (m_indentationLevel > 0) m_indentationLevel--;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public void paste()
	{
		int initialOffset = getCaretOffset();
		int initialLength = getText().length();
		boolean pasteEnd = getCaretOffset() > m_lineStart;
		setRedraw(false);
		super.paste();
		if (pasteEnd)
		{
			String toChange = getText().substring(m_lineStart, getText().length());
			toChange.replace(PROMPT_STRING, "");
			toChange.replace(MULTI_STRING, "");
			toChange.replace("\r\n", LINE_SEPARATOR + MULTI_STRING);
			toChange.replace("\n", LINE_SEPARATOR + MULTI_STRING);
			toChange.replace("\t", "    ");
			setText(getText().substring(0, m_lineStart) + toChange);
			// If it is multiline
			if (toChange.indexOf(LINE_SEPARATOR) != -1)
			{
				m_multi = true;
				addPromptString();
			}
			setCaretOffset(getText().length());
		}
		else
		// We will need to adjust the markers because the text will be shifted
		{
			int newLength = getText().length();
			if (initialLength < newLength)
			{
				m_lineStart += newLength - initialLength;
				m_inputStart += newLength - initialLength;
			}
			else if (newLength < m_inputStart)
			{
				append(LINE_SEPARATOR);
				addPromptString();
				resetInput();
			}
			setCaretOffset(initialOffset + (newLength - initialLength));
		}
		setRedraw(true);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void executeInput()
	{
		String input = getInput();
		try
		{
			if (!input.isEmpty())
			{
				input = input.replaceAll("\r", "");
				// Check the correctness
				m_parser.parseCode(input);
				// Run the script
				m_model.getController().script(input);
			}
		}
		catch (ParseException ex)
		{
			if (ex.currentToken.toString().trim().isEmpty())
			{
				append("Syntax error at line " + ex.currentToken.beginLine + " of the input" + LINE_SEPARATOR);
			}
			else
			{
				append("Syntax error: '" + ex.currentToken + "' at line " + ex.currentToken.beginLine + " of the input"
				        + LINE_SEPARATOR);
			}
		}
		catch (TokenMgrError err)
		{
			append("Syntax error: " + err.getLocalizedMessage() + LINE_SEPARATOR);
		}
		// Append single-line inputs to the history
		input = input.trim();
		int lastIndex = input.indexOf(LINE_SEPARATOR);
		if (lastIndex == -1)
		{
			m_history.add(input);
			m_historyIndex = m_history.size();
		}
		else
		{
			String[] elements = input.split(LINE_SEPARATOR);
			for (String element : elements)
				m_history.add(element);
			m_historyIndex = m_history.size();
		}
		resetInput();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void processInput()
	{
		if (isExpressionStart())
		{
			m_multi = true;
			indent();
		}
		else if (getCurrentLine().trim().isEmpty())
		{
			if (m_multi)
			{
				dedent();
				setText(getText().substring(0, getText().length() - TAB_SIZE));
				setCaretOffset(getText().length());
			}
			if (m_indentationLevel == 0)
			{
				executeInput();
				append(LINE_SEPARATOR);
			}
		}
		else if (!m_multi)
		{
			executeInput();
		}
		addPromptString();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void upHistory()
	{
		if (m_historyIndex > 0)
		{
			m_historyIndex--;
			String totalText = getText().substring(0, m_inputStart);
			setText(totalText);
			resetInput();
			setCaretOffset(m_inputStart);
			append(m_history.get(m_historyIndex));
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void downHistory()
	{
		if (m_historyIndex < m_history.size() - 1)
		{
			m_historyIndex++;
			String totalText = getText().substring(0, m_inputStart);
			setText(totalText);
			resetInput();
			setCaretOffset(m_inputStart);
			append(m_history.get(m_historyIndex));
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private boolean processBackspace()
	{
		boolean doIt = false;
		if (m_indentationLevel > 0)
		{
			int limit = m_lineStart + PROMPT_STRING.length() + m_indentationLevel * TAB_SIZE;
			if (getCaretOffset() == limit)
			{
				dedent();
				setText(getText().substring(0, getText().length() - TAB_SIZE));
				setCaretOffset(getText().length());
				doIt = false;
			}
			else
			{
				doIt = true;
			}
		}
		else
		{
			doIt = (getCaretOffset() > (m_lineStart + PROMPT_STRING.length()));
		}
		return doIt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.
	 * KeyEvent)
	 */
	@Override
	public void keyPressed(KeyEvent e)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events
	 * .KeyEvent)
	 */
	@Override
	public void keyReleased(KeyEvent event)
	{
		switch (event.keyCode)
		{
		case KeyCodes.ENTER_SIDE:
		case KeyCodes.ENTER:
			// Process the input
			processInput();
			break;
		case KeyCodes.CURSOR_UP:
			if ((event.stateMask & SWT.CTRL) != 0) upHistory();
			break;
		case KeyCodes.CURSOR_DOWN:
			if ((event.stateMask & SWT.CTRL) != 0) downHistory();
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.custom.VerifyKeyListener#verifyKey(org.eclipse.swt.events
	 * .VerifyEvent)
	 */
	@Override
	public void verifyKey(VerifyEvent event)
	{
		switch (event.keyCode)
		{
		case KeyCodes.ENTER_SIDE:
		case KeyCodes.ENTER:
			setCaretOffset(getText().length());
			break;
		case KeyCodes.CURSOR_LEFT:
		case KeyCodes.BACKSPACE:
			event.doit = processBackspace();
			break;
		case KeyCodes.HOME:
			int initialCaretOffset = getCaretOffset();
			int finalCaretOffset = m_lineStart + PROMPT_STRING.length();
			// Take into account selection actions
			if ((event.stateMask & SWT.SHIFT) != 0)
			{
				setSelection(initialCaretOffset, finalCaretOffset);
			}
			else
			{
				setCaretOffset(finalCaretOffset);
			}
			event.doit = false;
			break;
		case KeyCodes.END:
			initialCaretOffset = getCaretOffset();
			finalCaretOffset = getText().indexOf(LINE_SEPARATOR, m_lineStart);
			if (finalCaretOffset <= m_lineStart)
			{
				finalCaretOffset = getText().length();
			}
			if ((event.stateMask & SWT.SHIFT) != 0)
			{
				setSelection(initialCaretOffset, finalCaretOffset);
			}
			else
			{
				setCaretOffset(finalCaretOffset);
			}
			break;
		case KeyCodes.CURSOR_UP:
		case KeyCodes.CURSOR_DOWN:
		case KeyCodes.PAGE_UP:
		case KeyCodes.PAGE_DOWN:
			event.doit = false;
			break;
		default:
			if (isTypingCharacter(event))
			{
				// Prevent the user from writing in the middle of old text
				if (getCaretOffset() < m_lineStart + PROMPT_STRING.length())
				{
					setCaretOffset(getText().length());
				}
			}
		}
	}

	private boolean isTypingCharacter(VerifyEvent event)
	{
		if (Character.isLetterOrDigit(event.character)) return true;
		switch (event.keyCode)
		{
		case 262144: // CTRL
		case 131072: // ALT
		case 65536: // SHIFT
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt
	 * .events.MouseEvent)
	 */
	@Override
	public void mouseDoubleClick(MouseEvent e)
	{
		clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events
	 * .MouseEvent)
	 */
	@Override
	public void mouseDown(MouseEvent e)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.
	 * MouseEvent)
	 */
	@Override
	public void mouseUp(MouseEvent e)
	{
	}

	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		String property = event.getProperty();
		if (property.equals(FontKey.CODE.getPreferenceName()))
		{
			IConfigurationManager cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
			Font font = cfg.getFont(FontKey.CODE);
			Font oldFont = getFont();

			String name = font.getFontData()[0].getName();
			int style = font.getFontData()[0].getStyle();
			int height = oldFont.getFontData()[0].getHeight();
			FontData data = new FontData(name, height, style);
			font = new Font(Display.getDefault(), data);

			setFont(font);
		}
	}

	/***************************************************************************
	 * Stop listening from preferences changes
	 **************************************************************************/
	private void unsubuscribeFromPreferences()
	{
		IConfigurationManager cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		cfg.removePropertyChangeListener(this);
	}
}
