///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.language.beautifier
// 
// FILE      : ModuleParser.java
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

import com.astra.ses.spell.language.model.ast.Assign;
import com.astra.ses.spell.language.model.ast.Call;
import com.astra.ses.spell.language.model.ast.Dict;
import com.astra.ses.spell.language.model.ast.Expr;
import com.astra.ses.spell.language.model.ast.List;
import com.astra.ses.spell.language.model.ast.Module;
import com.astra.ses.spell.language.model.ast.exprType;
import com.astra.ses.spell.language.model.ast.stmtType;

public class ModuleParser
{
	public static void parse(Module module, ArrayList<Paragraph> pars,
	        int indent, int maxLength)
	{
		for (stmtType stmt : module.body)
		{
			if (stmt instanceof Expr)
			{
				exprType exValue = ((Expr) stmt).value;
				if (exValue instanceof Call)
				{
					CallParser.parse((Call) exValue, pars, indent, maxLength);
				}
				else if (exValue instanceof List)
				{
					ListParser.parse((List) exValue, pars, indent, maxLength);
				}
				else if (exValue instanceof Dict)
				{
					DictParser.parse((Dict) exValue, pars, indent, maxLength);
				}
				else
				{
					System.err.println("[MODULE] Unable to parse expression "
					        + exValue);
				}
			}
			else if (stmt instanceof Assign)
			{
				AsignParser.parse((Assign) stmt, pars, indent, maxLength);
			}
			else
			{
				System.err
				        .println("[MODULE] Unable to parse statement " + stmt);
			}
		}
	}
}
