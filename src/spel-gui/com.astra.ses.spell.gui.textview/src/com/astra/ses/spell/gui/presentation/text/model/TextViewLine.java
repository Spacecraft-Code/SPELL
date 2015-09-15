///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.text.model
// 
// FILE      : TextViewLine.java
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import com.astra.ses.spell.gui.core.model.types.Scope;

public class TextViewLine implements Comparable<TextViewLine>
{
	private static DateFormat s_format ;
	
	// Time formatter
	static
	{
		s_format = new SimpleDateFormat("HH:mm:ss");
		s_format.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	/** Holds the line text */
	private String m_text;
	/** Paragraph type for displays, warnings, errors, etc */
	private ParagraphType m_type;
	/** Scope of the messages (system, proc...) */
	private Scope m_scope;
	/** Sequence of arrival */
	private long m_sequence;
	/** Timestamp of the message (server generation) */
	private String m_timestamp;
	/** Show timestamp flag */
	private boolean m_showTimestamp;

	/***********************************************************************
	 * Constructor
	 **********************************************************************/
	public TextViewLine(String text, String timestamp, Scope scope, ParagraphType iconId, long sequence, boolean showTimestamp )
	{
		m_text = text;
		m_type = iconId;
		m_sequence = sequence;
		m_scope = scope;
		if (timestamp == null)
		{
			m_timestamp = s_format.format(Calendar.getInstance().getTime());
		}
		else
		{
			m_timestamp = timestamp;
		}
		m_showTimestamp = showTimestamp;
	}

	/***********************************************************************
	 * 
	 **********************************************************************/
	public String getText()
	{
		if (m_showTimestamp)
		{
			return "[ " + m_timestamp + " ] " + m_text;
		}
		return m_text;
	}

	/***********************************************************************
	 * 
	 **********************************************************************/
	public void setShowTimestamp(boolean show)
	{
		m_showTimestamp = show;
	}

	/***********************************************************************
	 * 
	 **********************************************************************/
	public long getSequence()
	{
		return m_sequence;
	}

	/***********************************************************************
	 * 
	 **********************************************************************/
	public ParagraphType getType()
	{
		return m_type;
	}

	/***********************************************************************
	 * 
	 **********************************************************************/
	public Scope getScope()
	{
		return m_scope;
	}

	/***********************************************************************
	 * 
	 **********************************************************************/
	public int length()
	{
		return m_text.length();
	}

	/***********************************************************************
	 * 
	 **********************************************************************/
	public String toString()
	{
		return "[ " + m_timestamp + " ] " + m_text;
	}

	/***********************************************************************
	 * 
	 **********************************************************************/
	@Override
	public int compareTo(TextViewLine that)
	{
		Long n1 = new Long(this.getSequence());
		Long n2 = new Long(that.getSequence());
		return n1.compareTo(n2);
	}
}
