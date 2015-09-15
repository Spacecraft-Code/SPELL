///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.language.beautifier
// 
// FILE      : CallParser.java
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
import com.astra.ses.spell.language.model.ast.keywordType;

public class CallParser extends MultilineParser
{
	public static void parse(Call call, ArrayList<Paragraph> pars, int indent,
	        int maxLength)
	{
		String funcName = ((Name) call.func).id;
		String first = funcName + "( ";
		String indentstr = "";
		for (int i = 0; i < first.length(); i++)
			indentstr += " ";

		ArrayList<Paragraph> ksubpars = new ArrayList<Paragraph>();
		ArrayList<Paragraph> asubpars = new ArrayList<Paragraph>();

		for (keywordType kwd : call.keywords)
		{
			KeywordParser.parse(kwd, ksubpars, indent, maxLength);
		}

		for (exprType arg : call.args)
		{
			if (arg instanceof Str)
			{
				Paragraph p = new Paragraph();
				p.addLine("'" + ((Str) arg).s + "'");
				asubpars.add(p);
			}
			else if (arg instanceof Name)
			{
				Paragraph p = new Paragraph();
				p.addLine(((Name) arg).id);
				asubpars.add(p);
			}
			else if (arg instanceof Num)
			{
				Paragraph p = new Paragraph();
				p.addLine(((Num) arg).num);
				asubpars.add(p);
			}
			else if (arg instanceof Dict)
			{
				DictParser.parse((Dict) arg, asubpars, indent, maxLength);
			}
			else if (arg instanceof List)
			{
				ListParser.parse((List) arg, asubpars, indent, maxLength);
			}
			else
			{
				System.err.println("[CALL] Unable to parse argument " + arg);
			}
		}

		// Put all paragraphs together
		Paragraph main = new Paragraph();
		for (Paragraph p : asubpars)
		{
			for (String line : p.getLines())
			{
				if (main.size() > 0)
				{
					main.addLine(indentstr + line);
				}
				else
				{
					main.addLine(first + line);
				}
			}
			main.addAfter(",");
		}
		for (Paragraph p : ksubpars)
		{
			for (String line : p.getLines())
			{
				if (main.size() > 0)
				{
					main.addLine(indentstr + line);
				}
				else
				{
					main.addLine(first + line);
				}
			}
			main.addAfter(",");
		}
		asubpars.clear();
		ksubpars.clear();
		String lastLine = main.get(main.size() - 1);
		lastLine = lastLine.substring(0, lastLine.length() - 1) + " )";
		main.set(main.size() - 1, lastLine);
		// alignAt(main, "=", 0);
		pars.add(main);
	}
}
