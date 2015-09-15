///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.language.beautifier
// 
// FILE      : MultilineParser.java
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

public class MultilineParser
{
	protected static int alignAt(Paragraph p, String elem, int start, int indent)
	{
		int latestPos = 0;
		String ind = "";
		for (int i = 0; i < indent; i++)
			ind += " ";
		for (String line : p.getLines())
		{
			int pos = line.indexOf(elem, start);
			if (line.indexOf(ind) == 0) pos -= indent;
			if (pos > latestPos) latestPos = pos;
		}
		if (latestPos == 0) return 0;
		for (int index = 0; index < p.size(); index++)
		{
			String line = p.get(index);
			int pos = line.indexOf(elem, start);
			if (pos != -1)
			{
				ind = "";
				for (int i = 0; i < (latestPos - pos); i++)
					ind += " ";
				p.set(index,
				        line.substring(0, pos) + ind
				                + line.substring(pos, line.length()));
			}
		}
		return latestPos + 1;
	}

	protected static int countChar(String line, char c)
	{
		char[] ca = line.toCharArray();
		int count = 0;
		for (int index = 0; index < ca.length; index++)
		{
			if (ca[index] == c) count++;
		}
		return count;
	}

	protected static void mergeLines(ArrayList<String> lines, Paragraph p,
	        String start, String end)
	{
		String merged = start;
		for (String line : lines)
			merged += line;
		merged += end;
		p.addLine(merged);
	}

	protected static void indentLines(ArrayList<String> lines, int indent,
	        Paragraph p)
	{
		int count = 0;
		String ind = "";
		for (int i = 0; i < indent; i++)
			ind += " ";
		ind += "  ";
		for (String line : lines)
		{
			if (count > 0) line = ind + line;
			p.addLine(line);
			count++;
		}
	}

	protected static int alignByCommas(Paragraph p, int indent)
	{
		int repeat = 0;
		for (String line : p.getLines())
		{
			int lcount = countChar(line, ',');
			if (lcount > repeat) repeat = lcount;
		}
		int pos = 0;
		for (int i = 0; i < repeat - 1; i++)
		{
			pos = alignAt(p, ",", pos, indent);
		}
		return pos;
	}
}
