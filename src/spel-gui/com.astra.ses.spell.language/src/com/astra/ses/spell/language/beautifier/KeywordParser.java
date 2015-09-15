///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.language.beautifier
// 
// FILE      : KeywordParser.java
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

import com.astra.ses.spell.language.model.SimpleNode;
import com.astra.ses.spell.language.model.ast.Dict;
import com.astra.ses.spell.language.model.ast.List;
import com.astra.ses.spell.language.model.ast.Name;
import com.astra.ses.spell.language.model.ast.NameTok;
import com.astra.ses.spell.language.model.ast.Num;
import com.astra.ses.spell.language.model.ast.Str;
import com.astra.ses.spell.language.model.ast.keywordType;

public class KeywordParser
{
	public static void parse(keywordType keywd, ArrayList<Paragraph> pars,
	        int indent, int maxLength)
	{
		SimpleNode kname = keywd.arg;
		SimpleNode kvalue = keywd.value;

		Paragraph p = new Paragraph();
		String line = "";
		if (kname instanceof NameTok)
		{
			line = ((NameTok) kname).id + " = ";
		}
		else
		{
			System.err.println("[KEYWORD] Unable to parse keyword name "
			        + kname);
			line = "<?> = ";
		}

		if (kvalue instanceof Name)
		{
			line += ((Name) kvalue).id;
			p.addLine(line);
		}
		else if (kvalue instanceof Str)
		{
			line += "'" + ((Str) kvalue).s + "'";
			p.addLine(line);
		}
		else if (kvalue instanceof Num)
		{
			line += ((Num) kvalue).num;
			p.addLine(line);
		}
		else if (kvalue instanceof List)
		{
			ArrayList<Paragraph> subpars = new ArrayList<Paragraph>();
			ListParser.parse((List) kvalue, subpars, indent + line.length(),
			        maxLength - line.length());
			int count = 0;
			for (String sline : subpars.get(0).getLines())
			{
				if (count == 0) sline = line + sline;
				p.addLine(sline);
				count++;
			}
		}
		else if (kvalue instanceof Dict)
		{
			ArrayList<Paragraph> subpars = new ArrayList<Paragraph>();
			DictParser.parse((Dict) kvalue, subpars, indent + line.length(),
			        maxLength - line.length());
			int count = 0;
			for (String sline : subpars.get(0).getLines())
			{
				if (count == 0) sline = line + sline;
				p.addLine(sline);
				count++;
			}
		}
		else
		{
			System.err.println("[KEYWORD] Unable to parse keyword value "
			        + kvalue);
			line += "<?>";
			p.addLine(line);
		}
		pars.add(p);
	}
}
