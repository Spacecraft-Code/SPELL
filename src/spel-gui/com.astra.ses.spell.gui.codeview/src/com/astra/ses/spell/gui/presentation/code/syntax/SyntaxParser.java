///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.syntax
// 
// FILE      : SyntaxParser.java
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

import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;

import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.language.SpellProgrammingLanguage;

import de.susebox.jtopas.Flags;
import de.susebox.jtopas.StandardTokenizer;
import de.susebox.jtopas.StandardTokenizerProperties;
import de.susebox.jtopas.StringSource;
import de.susebox.jtopas.Token;
import de.susebox.jtopas.TokenizerException;
import de.susebox.jtopas.TokenizerProperties;

/*******************************************************************************
 * @brief Source code parser based on JTOPAS package. Implements a basic token
 *        recognition algorithm for SPELL. Requires JTOPAS runtime package.
 * @date 27/03/08
 ******************************************************************************/
public class SyntaxParser implements ISyntaxParser
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Currently configured scheme */
	private IStyleScheme m_scheme = null;
	// PROTECTED ---------------------------------------------------------------
	/** Holds the list of defined functions */
	protected Vector<String> m_listFunctions;
	/** Holds the list of defined critical functions */
	protected Vector<String> m_listCriticalFunctions;
	/** Holds the list of defined modifiers */
	protected Vector<String> m_listModifiers;
	/** Holds the list of defined constants */
	protected Vector<String> m_listConstants;
	/** Holds the list of defined language keywords */
	protected Vector<String> m_listKeywords;
	/** Holds the list of defined language entities */
	protected Vector<String> m_listEntities;
	/** Holds the JTOPAS tokenizer configuration */
	protected TokenizerProperties m_properties = null;
	/** Holds the map of syntax-relevant areas */
	protected Map<String, ISyntaxAreas> m_syntaxAreas;
	/** Holds the current code id */
	protected String m_currentCodeId;

	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INNER CLASSES
	// =========================================================================

	/***************************************************************************
	 * Holds the applicability range for a text style
	 **************************************************************************/
	private class Range
	{
		/** Start position */
		int start;
		/** End position */
		int end;
	}

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 * 
	 * @param scheme
	 *            Initial color scheme
	 **************************************************************************/
	public SyntaxParser(IStyleScheme scheme)
	{
		Logger.debug("Reading word list", Level.GUI, this);
		m_listFunctions = new Vector<String>();
		m_listCriticalFunctions = new Vector<String>();
		m_listModifiers = new Vector<String>();
		m_listConstants = new Vector<String>();
		m_listKeywords = new Vector<String>();
		m_listEntities = new Vector<String>();

		String[] functions = SpellProgrammingLanguage.getInstance().getSpellFunctions();
		for (String f : functions)
		{
			if (SpellProgrammingLanguage.getInstance().isCriticalFunction(f))
			{
				m_listCriticalFunctions.addElement(f);
			}
			m_listFunctions.addElement(f);
		}


		String[] modifiers = SpellProgrammingLanguage.getInstance().getSpellModifiers();
		for (String m : modifiers)
			m_listModifiers.addElement(m);
		
		String[] keywords = SpellProgrammingLanguage.getInstance().getSpellKeywords();
		for (String l : keywords)
			m_listKeywords.addElement(l);
		
		String[] constants = SpellProgrammingLanguage.getInstance().getSpellConstants();
		for (String c : constants)
			m_listConstants.addElement(c);
		
		String[] entities = SpellProgrammingLanguage.getInstance().getSpellEntities();
		for (String e : entities)
			m_listEntities.addElement(e);
		
		Logger.debug("Word list read", Level.GUI, this);
		m_scheme = scheme;
		m_syntaxAreas = new TreeMap<String, ISyntaxAreas>();
		m_currentCodeId = null;
		// Configure the JTOPAS tokenizer to recognise SPELL
		configureTokenizer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.presentation.code.syntax.ISyntaxParser#parseSyntax
	 * (org.eclipse.swt.graphics.TextLayout, int)
	 */
	@Override
	public void parseSyntax(TextLayout layout, int rowIndex, boolean selected, boolean highlighted )
	{
		// Obtain the text to parse
		String text = layout.getText();
		// Do not process null or blank text
		if (text == null || text.length() == 0)
			return;
		// Default style
		
		if (selected)
		{
			layout.setStyle(m_scheme.getSelectedStyle(), 0, text.length());
			return;
		}
		else if (highlighted)
		{
			layout.setStyle(m_scheme.getHighlightedStyle(), 0, text.length());
			return;
		}
		else
		{
			layout.setStyle(m_scheme.getDefaultStyle(), 0, text.length());
		}

		// Create a source for the tokenizer with the text
		StringSource source = new StringSource(text);
		// Create the tokenizer using the current configuration
		StandardTokenizer tokenizer = new StandardTokenizer(m_properties);
		// Assign the source
		tokenizer.setSource(source);
		try
		{
			// For each token recognised in the text
			while (tokenizer.hasMoreToken())
			{
				Token token = tokenizer.nextToken();
				int type = token.getType();
				// This is the token text
				String word = tokenizer.currentImage();
				// When the image is null, we are processing EOL
				if (word == null)
					break;
				boolean isDocStringTag1 = (type == Token.SPECIAL_SEQUENCE && word.equals("\"\"\""));
				boolean isDocStringTag2 = (type == Token.SPECIAL_SEQUENCE && word.equals("'''"));
				boolean isDocString = false;
				if (m_currentCodeId != null)
				{
					ISyntaxAreas areas = m_syntaxAreas.get(m_currentCodeId);
					if (areas != null)
					{
						isDocString = areas.isInCommentBlock(rowIndex);
					}
				}
				// Find the applicable style, depending on the token type
				TextStyle toApply = null;
				if (isDocString || isDocStringTag1 || isDocStringTag2)
				{
					toApply = m_scheme.getStyle(TokenTypes.COMMENT);
				}
				else
				{
					toApply = getApplicableStyle(word, type);
				}
				// If no style is returned, continue to next token
				if (toApply == null)
					continue;
				// Get the applicable range (find token position)
				Range range = getApplicableRange(text, word, token);
				// Apply the style to the layout
				layout.setStyle(toApply, range.start, range.end);
			}
			// Close tokenizer
			tokenizer.close();
			tokenizer = null;
		}
		catch (TokenizerException e)
		{
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.presentation.code.syntax.ISyntaxParser#parseSource
	 * (String,String)
	 */
	@Override
	public void parseSource(String codeId, String[] source)
	{
		if (!m_syntaxAreas.containsKey(codeId))
		{
			ISyntaxAreas areas = generateAreas(source);
			m_syntaxAreas.put(codeId, areas);
		}
		m_currentCodeId = codeId;
	}

	// =========================================================================
	// # NON-ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Generate relevant areas. We need to this manually because the JTOPAS
	 * parser is not able to detect Python block comments.
	 **************************************************************************/
	private ISyntaxAreas generateAreas(String[] source)
	{
		ISyntaxAreas areas = new SyntaxAreas();
		findCommentBlocks(areas, source, "\"\"\"");
		findCommentBlocks(areas, source, "'''");
		return areas;
	}

	/***************************************************************************
	 * Find comment blocks of the given type.
	 **************************************************************************/
	private void findCommentBlocks(ISyntaxAreas areas, String[] source, String word)
	{
		int blockStart = -1;
		int blockEnd = -1;
		int lineCount = 1;
		for (String sourceLine : source)
		{
			// Find an occurence of the tag
			int pos = sourceLine.indexOf(word);
			if (pos != -1)
			{
				if (blockStart != -1)
				{
					blockEnd = lineCount-1;
					areas.addCommentBlock(blockStart, blockEnd);
					blockStart = -1;
					blockEnd = -1;
				}
				else
				{
					// First try to find the closing tag in the same line. If
					// there is no tag,
					// continue in next lines
					pos = sourceLine.indexOf(word, pos + 1);
					if (pos != -1)
					{
						areas.addCommentBlock(lineCount, lineCount);
						blockStart = -1;
						blockEnd = -1;
					}
					else
					{
						blockStart = lineCount;
					}
				}
			}
			lineCount++;
		}
	}

	/***************************************************************************
	 * Configure the JTOPAS tokenizer to recognise SPELL
	 **************************************************************************/

	/***************************************************************************
	 * Configure the JTOPAS tokenizer to recognise SPELL
	 **************************************************************************/
	private void configureTokenizer()
	{
		Logger.debug("Configuring syntax tokenizer", Level.GUI, this);
		try
		{
			Logger.debug("Setting flags", Level.GUI, this);
			// Use the standard configuration as a base
			m_properties = new StandardTokenizerProperties();
			// Return token positions and line comments
			m_properties.setParseFlags(Flags.F_TOKEN_POS_ONLY | Flags.F_RETURN_LINE_COMMENTS);
			// Python comments
			// Block comments are parsed manually
			m_properties.addLineComment("#");
			// Python strings
			m_properties.addString("\"", "\"", "\"");
			m_properties.addString("'", "'", "'");
			// Normal whitespaces
			m_properties.addWhitespaces(TokenizerProperties.DEFAULT_WHITESPACES);
			// Normal separators
			m_properties.addSeparators(TokenizerProperties.DEFAULT_SEPARATORS);
			// Add our keywords
			Logger.debug("Adding keywords", Level.GUI, this);
			for (String word : m_listFunctions)
			{
				m_properties.addKeyword(word);
			}
			for (String word : m_listModifiers)
			{
				m_properties.addKeyword(word);
			}
			for (String word : m_listKeywords)
			{
				m_properties.addKeyword(word);
			}
			for (String word : m_listConstants)
			{
				m_properties.addKeyword(word);
			}
			for (String word : m_listEntities)
			{
				m_properties.addKeyword(word);
			}
			// Special symbols
			Logger.debug("Adding symbols", Level.GUI, this);
			m_properties.addSpecialSequence("\"\"\"");
			m_properties.addSpecialSequence("'''");
			m_properties.addSpecialSequence("{");
			m_properties.addSpecialSequence("}");
			m_properties.addSpecialSequence("(");
			m_properties.addSpecialSequence(")");
			m_properties.addSpecialSequence("[");
			m_properties.addSpecialSequence("]");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/***************************************************************************
	 * Check if the given word is a SPELL function
	 * 
	 * @return True if the word is a SPELL function
	 **************************************************************************/
	protected boolean isFunction(String word)
	{
		return m_listFunctions.contains(word);
	}

	/***************************************************************************
	 * Check if the given word is a SPELL function
	 * 
	 * @return True if the word is a SPELL function
	 **************************************************************************/
	protected boolean isCriticalFunction(String word)
	{
		return m_listCriticalFunctions.contains(word);
	}

	/***************************************************************************
	 * Check if the given word is a SPELL modifier
	 * 
	 * @return True if the word is a SPELL modifier
	 **************************************************************************/
	protected boolean isModifier(String word)
	{
		return m_listModifiers.contains(word);
	}

	/***************************************************************************
	 * Check if the given word is a language keyword
	 * 
	 * @return True if the word is a language keyword
	 **************************************************************************/
	protected boolean isKeyword(String word)
	{
		return m_listKeywords.contains(word);
	}

	/***************************************************************************
	 * Check if the given word is a SPELL constant
	 * 
	 * @return True if the word is a SPELL constant
	 **************************************************************************/
	protected boolean isConstant(String word)
	{
		return m_listConstants.contains(word);
	}

	/***************************************************************************
	 * Check if the given word is a SPELL constant
	 * 
	 * @return True if the word is a SPELL constant
	 **************************************************************************/
	protected boolean isEntity(String word)
	{
		return m_listEntities.contains(word);
	}

	/***************************************************************************
	 * Check if the given token is a number
	 * 
	 * @param s
	 *            The token
	 * @return True if it is a number
	 **************************************************************************/
	protected boolean isNumber(String s)
	{
		try
		{
			int idx = s.indexOf("E");
			if (idx != -1)
			{
				return isNumber(s.substring(0, idx));
			}
			Double.parseDouble(s);
			return true;
		}
		catch (NumberFormatException ex)
		{
			return false;
		}
	}

	/***************************************************************************
	 * Obtain the applicability range for the given token.
	 * 
	 * @param text
	 *            The whole line
	 * @param word
	 *            The image of the token being processed
	 * @param token
	 *            The token being processed
	 * @return The corresponding range
	 **************************************************************************/
	protected Range getApplicableRange(String text, String word, Token token)
	{
		Range r = new Range();
		switch (token.getType())
		{
		case Token.STRING:
		case Token.UNKNOWN:
		case Token.SPECIAL_SEQUENCE:
		case Token.NORMAL:
		case Token.KEYWORD:
			r.start = token.getStartPosition();
			r.end = r.start + word.length();
			break;
		case Token.SEPARATOR:
			// Separators shall be processed this way
			r.start = token.getStartPosition();
			r.end = r.start + 1;
			break;
		case Token.LINE_COMMENT:
		default:
			// For comments, and by default apply the style to the whole line
			r.start = token.getStartPosition();
			r.end = text.length();
			break;
		}
		return r;
	}

	/***************************************************************************
	 * Find the corresponding style for the given token type
	 * 
	 * @param word
	 *            Token image
	 * @param type
	 *            Token type
	 * @return The corresponding style
	 **************************************************************************/
	protected TextStyle getApplicableStyle(String word, int type)
	{
		TextStyle toApply = null;
		switch (type)
		{
		case Token.KEYWORD:
			if (isFunction(word))
			{
				if (isCriticalFunction(word))
				{
					//toApply = m_scheme.getStyle(TokenTypes.CRITICAL);
					toApply = m_scheme.getStyle(TokenTypes.SPELL);
				}
				else
				{
					toApply = m_scheme.getStyle(TokenTypes.SPELL);
				}
			}
			else if (isKeyword(word))
			{
				toApply = m_scheme.getStyle(TokenTypes.CODE);
			}
			else if (isModifier(word))
			{
				toApply = m_scheme.getStyle(TokenTypes.MODIFIER);
			}
			else if (isConstant(word))
			{
				toApply = m_scheme.getStyle(TokenTypes.CONSTANT);
			}
			else if (isEntity(word))
			{
				toApply = m_scheme.getStyle(TokenTypes.ENTITY);
			}
			else
			{
				toApply = m_scheme.getStyle(TokenTypes.NORMAL);
			}
			break;
		case Token.LINE_COMMENT:
			toApply = m_scheme.getStyle(TokenTypes.COMMENT);
			break;
		case Token.NORMAL:
			// Easier than using a pattern for recognising numbers
			if (isNumber(word))
			{
				toApply = m_scheme.getStyle(TokenTypes.NUMBER);
			}
			else
			{
				toApply = m_scheme.getStyle(TokenTypes.NORMAL);
			}
			break;
		case Token.WHITESPACE:
		case Token.EOF:
			break;
		case Token.SEPARATOR:
		case Token.SPECIAL_SEQUENCE:
			toApply = m_scheme.getStyle(TokenTypes.SYMBOL);
			break;
		case Token.STRING:
			toApply = m_scheme.getStyle(TokenTypes.STRING);
			break;
		case Token.UNKNOWN:
		default:
			toApply = m_scheme.getDefaultStyle();
			break;
		}
		return toApply;
	}
}
