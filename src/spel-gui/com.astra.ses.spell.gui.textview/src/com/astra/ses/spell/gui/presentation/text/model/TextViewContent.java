///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.text.model
// 
// FILE      : TextViewContent.java
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

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.custom.TextChangedEvent;

import com.astra.ses.spell.gui.core.model.types.Scope;

public class TextViewContent implements StyledTextContent, TextBufferListener
{
	private final static String LINE_DELIMITER = System.getProperty("line.separator");

	/** Holds the subscribed text change listeners */
	private ArrayList<TextChangeListener> m_listeners;
	/** Holds the text contents */
	private TextViewLine[] m_text;
	/** Holds the full text buffer */
	private TextBuffer m_buffer;
	/** Holds the line start and end positions */
	private Range[] m_ranges;
	/** Holds the maximum amount of lines in the model */
	private int m_viewWindowLength;
	/** Holds the index of the view window */
	private int m_viewWindowStart;
	/** Status of show timestamp flag */
	private boolean m_showTimestamp;

	/** Utility class for storing line start offset and length */
	private class Range
	{
		public int start;
		public int length;
	}

	/**************************************************************************
	 * Constructor
	 *************************************************************************/
	public TextViewContent( int capacity )
	{
		m_listeners = new ArrayList<TextChangeListener>();
		m_text = new TextViewLine[1];
		m_showTimestamp = false;
		m_text[0] = new TextViewLine("", null, Scope.OTHER, ParagraphType.NORMAL, 0, m_showTimestamp);
		m_ranges = null;
		m_viewWindowLength = 0;
		m_viewWindowStart = 0;
		m_buffer = new TextBuffer(capacity);
		m_buffer.addBufferListener(this);
		makeLineIndex();
	}

	@Override
	/**************************************************************************
	 * Register a text change listener
	 *************************************************************************/
	public void addTextChangeListener(TextChangeListener listener)
	{
		m_listeners.add(listener);
	}

	@Override
	/**************************************************************************
	 * De-register a text change listener
	 *************************************************************************/
	public void removeTextChangeListener(TextChangeListener listener)
	{
		m_listeners.remove(listener);
	}

	/**************************************************************************
	 * Toggle set show timestamp
	 *************************************************************************/
	public void setShowTimestamp(boolean show)
	{
		m_showTimestamp = show;
		for (TextViewLine line : m_text)
		{
			line.setShowTimestamp(show);
		}
		m_buffer.setShowTimestamp(show);
		updateContents();
	}

	/**************************************************************************
	 * Set buffer capacity
	 *************************************************************************/
	public void setCapacity( int lines )
	{
		m_buffer.setCapacity(lines);
		updateContents();
	}

	/**************************************************************************
	 * Fire a text-set event
	 *************************************************************************/
	private void fireTextSet()
	{
		for (TextChangeListener lst : m_listeners)
		{
			lst.textSet(new TextChangedEvent(this));
		}
	}

	/**************************************************************************
	 * Fire a text-moved event
	 *************************************************************************/
	private void fireTextMoved()
	{
		for (TextChangeListener lst : m_listeners)
		{
			lst.textSet(new TextChangedEvent(this));
		}
	}

	/**************************************************************************
	 * Recreate the start/end position representation of lines. This method
	 * needs to be called when the text content is changed.
	 *************************************************************************/
	private void makeLineIndex()
	{
		int offset = 0;
		// There are as many ranges as lines
		m_ranges = new Range[m_text.length];
		for (int index = 0; index < m_text.length; index++)
		{
			// Determine the length of the string
			int length = m_text[index].length();
			Range r = new Range();
			r.start = offset;
			r.length = length;
			// Store the range
			m_ranges[index] = r;
			// Add the length to keep the current offset value updated
			offset += length;
		}
	}

	/**************************************************************************
	 * Get the data to be shown from the buffer
	 *************************************************************************/
	private void getDataFromBuffer()
	{
		m_text = m_buffer.getWindowData();
		// If we have no data to show, put a blank line (required by algorithm)
		if (m_text.length == 0)
		{
			m_text = new TextViewLine[1];
			m_text[0] = new TextViewLine("", null, Scope.OTHER, ParagraphType.NORMAL, 0, m_showTimestamp);
		}
	}

	/**************************************************************************
	 * Update the data being shown
	 *************************************************************************/
	private void updateContents()
	{
		// Get the data to be shown from the buffer
		getDataFromBuffer();
		// Regenerate the ranges
		makeLineIndex();
	}

	/**************************************************************************
	 * Append a line to the buffer
	 *************************************************************************/
	public void append(TextViewLine p)
	{
		m_buffer.append(p);
	}

	/**************************************************************************
	 * Change the position in the buffer
	 *************************************************************************/
	public void setViewWindow(int start, int maxLines, boolean autoscroll)
	{
		m_viewWindowLength = maxLines;
		m_viewWindowStart = start;
		// Inform the buffer about the window change
		m_buffer.setWindowPosition(m_viewWindowStart, m_viewWindowLength, autoscroll);
	}

	@Override
	/**************************************************************************
	 * Obtain the total amount of characters.
	 * 
	 * @return The amount of characters
	 *************************************************************************/
	public int getCharCount()
	{
		int count = 0;
		for (Range r : m_ranges)
		{
			count += r.length;
		}
		if (m_showTimestamp)
		{
			count += (13*m_text.length);
		}
		return count;
	}

	@Override
	/**************************************************************************
	 * Obtain the line text corresponding to the given index
	 * 
	 * @return The line text
	 *************************************************************************/
	public String getLine(int lineIndex)
	{
		if ((lineIndex >= m_text.length) || (lineIndex < 0))
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		return m_text[lineIndex].getText();
	}

	/**************************************************************************
	 * Obtain the line text corresponding to the given index
	 * 
	 * @return The line text
	 *************************************************************************/
	public TextViewLine getLineObject(int lineIndex)
	{
		if ((lineIndex >= m_text.length) || (lineIndex < 0))
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		return m_text[lineIndex];
	}

	@Override
	/**************************************************************************
	 * Obtain the line text corresponding to the given offset
	 * 
	 * @return The line index
	 *************************************************************************/
	public int getLineAtOffset(int offset)
	{
		if ((offset > getCharCount()) || (offset < 0))
		{
			System.err.println("getLineAtOffset " + offset + ": data size " + m_buffer.getDataSize() + " cc " + getCharCount());
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}

		int theIndex = m_ranges.length - 1;
		int idxLowBox = 0;
		int idxHiBox = m_ranges.length - 1;
		while (idxLowBox != idxHiBox)
		{
			int idxMiddleBox = (idxHiBox + idxLowBox) / 2;
			int mbStartOffset = m_ranges[idxMiddleBox].start;
			int pivot = mbStartOffset + m_ranges[idxMiddleBox].length;

			if (offset >= mbStartOffset && offset < pivot)
			{
				theIndex = idxMiddleBox;
				break;
			}

			if (pivot < offset)
			{
				idxLowBox = idxMiddleBox + 1;
			}
			else if (pivot > offset)
			{
				idxHiBox = idxMiddleBox;
			}
			else
			{
				theIndex = idxMiddleBox + 1;
				break;
			}
		}
		return theIndex;
	}

	@Override
	/**************************************************************************
	 * Obtain the amount of lines
	 * 
	 * @return The amount of lines
	 *************************************************************************/
	public int getLineCount()
	{
		return m_text.length;
	}

	@Override
	/**************************************************************************
	 * Obtain the line delimiter
	 * 
	 * @return LINE_DELIMITER
	 *************************************************************************/
	public String getLineDelimiter()
	{
		return LINE_DELIMITER;
	}

	@Override
	/**************************************************************************
	 * Obtain the offset corresponding to the given line index
	 * 
	 * @return The offset for that line
	 *************************************************************************/
	public int getOffsetAtLine(int lineIndex)
	{
		if ((lineIndex > m_text.length) || (lineIndex < 0))
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		return m_ranges[lineIndex].start;
	}

	@Override
	/**************************************************************************
	 * Obtain the text inside the given range
	 * 
	 * @return The corresponding text
	 *************************************************************************/
	public String getTextRange(int start, int length)
	{
		return "";
	}

	/**************************************************************************
	 * Get the total amount of data stored
	 *************************************************************************/
	public int getTotalDataSize()
	{
		return m_buffer.getDataSize();
	}

	/**************************************************************************
	 * Get all data stored as strings
	 *************************************************************************/
	public String[] getAllLines()
	{
		TextViewLine[] lines = m_buffer.getData();
		String[] data = new String[lines.length];
		int index = 0;
		for (TextViewLine line : lines)
		{
			data[index] = line.getText();
			index++;
		}
		return data;
	}

	/**************************************************************************
	 * Get the total amount of data viewed
	 *************************************************************************/
	public int getViewWindowLength()
	{
		return m_viewWindowLength;
	}

	/**************************************************************************
	 * Get the position of the view window
	 *************************************************************************/
	public int getViewWindowPosition()
	{
		return m_viewWindowStart;
	}

	@Override
	/**************************************************************************
	 * This method has no effect. We do not allow modifying the text contents
	 * via this method.
	 *************************************************************************/
	public void replaceTextRange(int start, int replaceLength, String text)
	{
		// Not implemented, we do not allow API modification via this method
	}

	@Override
	/**************************************************************************
	 * Set the text contents.
	 *************************************************************************/
	public void setText(String text)
	{
		// Not implemented
	}

	/**************************************************************************
	 * Clear the text contents.
	 *************************************************************************/
	public void clear()
	{
		// Set the entire set of data on the buffer
		m_buffer.clear();
		// Get the data to be shown from the buffer
		updateContents();
		// Notify the listeners
		fireTextSet();
	}

	@Override
	/**************************************************************************
	 * Called when the buffer receives data.
	 *************************************************************************/
	public void dataChanged(TextBuffer buffer)
	{
		updateContents();
		// Notify the listeners
		fireTextSet();
	}

	@Override
	/**************************************************************************
	 * Called when the buffer is cleared
	 *************************************************************************/
	public void dataCleared(TextBuffer buffer)
	{
	}

	@Override
	/**************************************************************************
	 * Called when the data window of the buffer is cleared
	 *************************************************************************/
	public void dataWindowMoved(TextBuffer buffer)
	{
		// Get the data to be shown from the buffer
		updateContents();
		// Update the window position
		m_viewWindowStart = m_buffer.getDataWindowStart();
		// Notify that the window has moved
		fireTextMoved();
	}
}
