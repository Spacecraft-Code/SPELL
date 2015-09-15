///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.language.beautifier
// 
// FILE      : ListParser.java
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

import com.astra.ses.spell.language.model.ast.Call;
import com.astra.ses.spell.language.model.ast.Dict;
import com.astra.ses.spell.language.model.ast.List;
import com.astra.ses.spell.language.model.ast.Name;
import com.astra.ses.spell.language.model.ast.Num;
import com.astra.ses.spell.language.model.ast.Str;
import com.astra.ses.spell.language.model.ast.exprType;

public class ListParser extends MultilineParser
{
	public static void parse(List list, ArrayList<Paragraph> pars, int indent,
	        int maxLength)
	{
		Paragraph p = new Paragraph();
		ArrayList<String> lines = new ArrayList<String>();
		int totalLength = 0;
		for (int index = 0; index < list.elts.length; index++)
		{
			exprType element = list.elts[index];
			if (element instanceof Num)
			{
				totalLength += addElement(lines, ((Num) element).num,
				        (index < list.elts.length - 1));
			}
			else if (element instanceof Str)
			{
				totalLength += addElement(lines, "'" + ((Str) element).s + "'",
				        (index < list.elts.length - 1));
			}
			else if (element instanceof Name)
			{
				totalLength += addElement(lines, ((Name) element).id,
				        (index < list.elts.length - 1));
			}
			else if (element instanceof List)
			{
				totalLength += addList(lines, (List) element, indent - 2,
				        maxLength, (index < list.elts.length - 1));
			}
			else if (element instanceof Dict)
			{
				totalLength += addDict(lines, (Dict) element, indent,
				        maxLength, (index < list.elts.length - 1));
			}
			else if (element instanceof Call)
			{
				totalLength += addCall(lines, (Call) element, indent,
				        maxLength, (index < list.elts.length - 1));
			}
			else
			{
				System.err.println("[LIST] Unable to parse element " + element);
			}
		}
		alignOrMerge(lines, p, indent, totalLength, maxLength);
		pars.add(p);
	}

	protected static int addElement(ArrayList<String> lines, String element,
	        boolean addComma)
	{
		if (addComma) element += ", ";
		lines.add(element);
		return element.length();
	}

	protected static int addList(ArrayList<String> lines, List list,
	        int indent, int maxLength, boolean addComma)
	{
		ArrayList<Paragraph> sublist = new ArrayList<Paragraph>();
		parse(list, sublist, indent + 2, maxLength);
		int listLength = 0;
		for (String subline : sublist.get(0).getLines())
		{
			lines.add(subline);
			listLength += subline.length();
		}
		if (addComma)
		{
			lines.set(lines.size() - 1, lines.get(lines.size() - 1) + ", ");
		}
		sublist.clear();
		return listLength;
	}

	protected static int addDict(ArrayList<String> lines, Dict dict,
	        int indent, int maxLength, boolean addComma)
	{
		ArrayList<Paragraph> sublist = new ArrayList<Paragraph>();
		DictParser.parse(dict, sublist, indent, maxLength);
		int dictLength = 0;
		for (String subline : sublist.get(0).getLines())
		{
			lines.add(subline);
			dictLength += subline.length();
		}
		if (addComma)
		{
			lines.set(lines.size() - 1, lines.get(lines.size() - 1) + ", ");
		}
		sublist.clear();
		return dictLength;
	}

	protected static int addCall(ArrayList<String> lines, Call call,
	        int indent, int maxLength, boolean addComma)
	{
		ArrayList<Paragraph> sublist = new ArrayList<Paragraph>();
		CallParser.parse(call, sublist, indent, maxLength);
		int callLength = 0;
		for (String line : sublist.get(0).getLines())
		{
			lines.add(line);
			callLength += line.length();
		}
		if (addComma)
		{
			lines.set(lines.size() - 1, lines.get(lines.size() - 1) + ", ");
		}
		return callLength;
	}

	protected static void alignOrMerge(ArrayList<String> lines, Paragraph p,
	        int indent, int totalLength, int maxLength)
	{
		if (totalLength < maxLength)
		{
			mergeLines(lines, p, "[ ", " ]");
		}
		else
		{
			indentLines(lines, indent, p);
			p.addBefore("[ ");
			p.addAfter(" ]");
			int pos = alignByCommas(p, indent);
			alignAt(p, "]", pos, indent);
		}
	}
}
