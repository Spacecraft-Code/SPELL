///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.text.model
// 
// FILE      : TextParagraph.java
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
package com.astra.ses.spell.gui.presentation.text.model;

import com.astra.ses.spell.gui.core.model.types.Scope;

/*******************************************************************************
 * Model for a text paragraph of the display or textual view.
 ******************************************************************************/
public class TextParagraph
{
	/** Icon size */
	public static final int	ICON_SIZE	= 16;
	/** Holds the lines of the paragraph */
	private String[]	    m_text;
	/** Holds the parahraph type */
	private ParagraphType	m_type;
	/** Holds the paragraph sequence number */
	private long	        m_sequence;
	/** Holds the message scope */
	private Scope	        m_scope;
	/** Holds the message timestamp */
	private String          m_timestamp;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param model
	 *            Handle to the text model
	 * @param type
	 *            Paragraph type
	 **************************************************************************/
	public TextParagraph(ParagraphType type, Scope scope, String text, String timestamp, long sequence)
	{
		m_type = type;
		m_text = text.split("\n");
		m_sequence = sequence;
		m_scope = scope;
		m_timestamp = timestamp;
	}

	/***************************************************************************
	 * Get number of lines
	 **************************************************************************/
	int getNumLines()
	{
		return m_text.length;
	}

	/***************************************************************************
	 * Obtain the total paragraph length
	 * 
	 * @return The length
	 **************************************************************************/
	public int length()
	{
		int len = 0;
		for (String line : m_text)
			len += line.length();
		return len;
	}

	/***************************************************************************
	 * 
	 * @return The sequence number
	 **************************************************************************/
	public long getSequence()
	{
		return m_sequence;
	}

	/***************************************************************************
	 * Obtain the paragraph text
	 * 
	 * @return The entire paragraph text
	 **************************************************************************/
	public String[] getText()
	{
		return m_text;
	}

	/***************************************************************************
	 * Obtain the paragraph timestamp
	 * 
	 * @return The paragraph timestamp
	 **************************************************************************/
	public String getTimestamp()
	{
		return m_timestamp;
	}

	/***************************************************************************
	 * Obtain the corresponding paragraph type
	 * 
	 * @return The icon image
	 **************************************************************************/
	public ParagraphType getType()
	{
		return m_type;
	}

	/***************************************************************************
	 * Return this message scope
	 * 
	 * @return
	 **************************************************************************/
	public Scope getScope()
	{
		return m_scope;
	}
}
