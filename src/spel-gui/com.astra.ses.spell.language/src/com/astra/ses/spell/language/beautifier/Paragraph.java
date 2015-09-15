///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.language.beautifier
// 
// FILE      : Paragraph.java
//
// DATE      : 2009-11-23
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
// SUBPROJECT: SPELL Development Environment
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.language.beautifier;

import java.util.ArrayList;

public class Paragraph
{
	private ArrayList<String>	m_lines;

	public Paragraph()
	{
		m_lines = new ArrayList<String>();
	}

	public String getText()
	{
		String result = "";
		for (int index = 0; index < m_lines.size(); index++)
		{
			String line = m_lines.get(index);
			result += line;
			if (index < m_lines.size() - 1) result += "\n";
		}
		return result;
	}

	public void addLine(String line)
	{
		m_lines.add(line);
	}

	public void addLine(int index, String line)
	{
		m_lines.add(index, line);
	}

	public void addAfter(String text)
	{
		int sz = m_lines.size() - 1;
		m_lines.set(sz, m_lines.get(sz) + text);
	}

	public void addBefore(String text)
	{
		if (m_lines.size() == 0) return;
		m_lines.set(0, text + m_lines.get(0));
	}

	public void set(int index, String text)
	{
		m_lines.set(index, text);
	}

	public String get(int index)
	{
		return m_lines.get(index);
	}

	public int size()
	{
		return m_lines.size();
	}

	public ArrayList<String> getLines()
	{
		return m_lines;
	}

}
