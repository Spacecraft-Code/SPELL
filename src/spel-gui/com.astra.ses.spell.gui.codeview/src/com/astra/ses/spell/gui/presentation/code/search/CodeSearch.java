///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.search
// 
// FILE      : CodeSearch.java
//
// DATE      : 2008-11-24 08:34
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
package com.astra.ses.spell.gui.presentation.code.search;

import java.util.ArrayList;

import org.eclipse.nebula.widgets.grid.GridItem;

import com.astra.ses.spell.gui.presentation.code.controls.CodeViewer;
import com.astra.ses.spell.gui.presentation.code.controls.CodeViewerColumn;

/*******************************************************************************
 * @brief Used to search for text occurences in the code viewer.
 * @date 09/10/07
 ******************************************************************************/
public class CodeSearch
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	public class SearchMatch
	{
		public int	lineNo;
		public int	startOffset;
		public int	length;
	}

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	/** Holds the search result */
	private SearchMatch[] m_result;
	/** Holds the current index in the result */
	private int	          m_resultIndex;
	/** Holds the reference to the table model */
	private CodeViewer   m_viewer;

	/***************************************************************************
	 * Constructor.
	 **************************************************************************/
	public CodeSearch( CodeViewer viewer )
	{
		m_viewer = viewer;
		m_result = null;
		m_resultIndex = -1;
	}

	/***************************************************************************
	 * Search for a given string in the code
	 **************************************************************************/
	public int searchString(String toSearch)
	{
		ArrayList<SearchMatch> occurrences = new ArrayList<SearchMatch>();
		for (GridItem item : m_viewer.getGrid().getItems())
		{
			String source = item.getText(CodeViewerColumn.CODE.ordinal());
			int idx = source.indexOf(toSearch);
			// If the string is found
			if (idx != -1)
			{
				SearchMatch match = new SearchMatch();
				match.lineNo = item.getParent().indexOf(item) + 1;
				match.startOffset = idx;
				match.length = toSearch.length() - 1;
				occurrences.add(match);
			}
		}
		m_result = occurrences.toArray(new SearchMatch[0]);
		return m_result.length;
	}

	/***************************************************************************
	 * Search for a given string in the code
	 **************************************************************************/
	public SearchMatch[] getMatches()
	{
		return m_result;
	}

	/***************************************************************************
	 * Clear search
	 **************************************************************************/
	public void clear()
	{
		m_result = null;
		m_resultIndex = -1;
	}

	/***************************************************************************
	 * Get next match
	 **************************************************************************/
	public SearchMatch getNext()
	{
		if ((m_result == null) || (m_result.length == 0)) return null;
		m_resultIndex++;
		if (m_resultIndex == m_result.length)
		{
			m_resultIndex = m_result.length - 1;
			return null;
		}
		return m_result[m_resultIndex];
	}

	/***************************************************************************
	 * Get previous match
	 **************************************************************************/
	public SearchMatch getPrevious()
	{
		if ((m_result == null) || (m_result.length == 0)) return null;
		m_resultIndex--;
		if (m_resultIndex < 0)
		{
			m_resultIndex = 0;
			return null;
		}
		return m_result[m_resultIndex];
	}
}
