///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.language.beautifier
// 
// FILE      : CodeBeautifier.java
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

import com.astra.ses.spell.language.Parser;
import com.astra.ses.spell.language.model.SimpleNode;
import com.astra.ses.spell.language.model.ast.Module;

public class CodeBeautifier
{
	private static Parser	s_parser	= new Parser();

	/****************************************************************************
	 * Beautify the given piece of code
	 * 
	 * @param code
	 * @param maxWidth
	 * @return
	 ***************************************************************************/
	public String beautifyCode(String code, int maxWidth)
	{
		String newCode = "";
		try
		{
			SimpleNode root = s_parser.parseCodeGetTree(code);

			ArrayList<Paragraph> pars = parseBlock(root, maxWidth);
			int i = 1;

			for (Paragraph p : pars)
			{
				newCode += p.getText();
				if (i != pars.size())
				{
					newCode += "\n";
				}
				i++;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return newCode;
	}

	/****************************************************************************
	 * Parse the given code block
	 * 
	 * @param code
	 * @param maxWidth
	 * @return
	 ***************************************************************************/
	protected ArrayList<Paragraph> parseBlock(SimpleNode node, int maxWidth)
	{
		ArrayList<Paragraph> result = new ArrayList<Paragraph>();
		if (node instanceof Module)
		{
			ModuleParser.parse((Module) node, result, 0, maxWidth);
		}
		return result;
	}

	public static void main(String[] args)
	{
		String code2 = "[ [325235235235325,235235235325,23523523555], 153534543534534535,3453453455345345342,[1463463463464363,243634634634646346,334634634634646],345345345353]";
		String code3 = "{ Adffgdf:5645654654654655641, Bdfg:2546456456546565, Cf:54645645645645643 }";
		String code4 = "[ [325235235235325,235235235325,23523523555], { A:153534543534534535, BBb:435435664456, Ceeeee:56756767} ,3453453455345345342,[1463463463464363,243634634634646346,334634634634646],345345345353]";
		String code5 = "{ A:1, B:2, C:3}";
		String code6 = "{ Adffgdf:5645654654654655641, Bdfg:[5345435353453453455,4534534534534543535,34534534543534534534], Cf:54645645645645643 }";
		String code7 = "GetTM( 'TMPOINT', Value, 45, [1,2,3], Wait=True, ValueFormat=RAW, config = {Config:X})";
		String code8 = "Send( command = 'TC', args=[['ARG1',56],['ARG2',54,{ValueFormat:RAW}]], verify = [['TM1 rtert', eq, 466],['TM2 egrtgretreg ert ert trt', lt, 34, {Modifier5:0.1}]], Modifier1=1, Modifier2=4, config = {Modifier3:True})";
		CodeBeautifier beautifier = new CodeBeautifier();

		int maxWidth = 80;

		beautifier.beautifyCode(code2, maxWidth);
		beautifier.beautifyCode(code3, maxWidth);
		beautifier.beautifyCode(code4, maxWidth);
		beautifier.beautifyCode(code5, maxWidth);
		beautifier.beautifyCode(code6, maxWidth);
		beautifier.beautifyCode(code7, maxWidth);
		beautifier.beautifyCode(code8, maxWidth);
	}
}
