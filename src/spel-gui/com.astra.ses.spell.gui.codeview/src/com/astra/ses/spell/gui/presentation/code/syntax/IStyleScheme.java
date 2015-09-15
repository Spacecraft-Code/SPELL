///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.syntax
// 
// FILE      : IStyleScheme.java
//
// DATE      : 2008-11-21 08:55
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
package com.astra.ses.spell.gui.presentation.code.syntax;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextStyle;

/*******************************************************************************
 * @brief Definition of a style scheme interface
 * @date 27/03/08
 ******************************************************************************/
public interface IStyleScheme
{
	/***************************************************************************
	 * Assign the current scheme type
	 * 
	 * @param s
	 *            The scheme type
	 **************************************************************************/
	public void setScheme(SchemeType s);

	/***************************************************************************
	 * Change the font to use
	 * 
	 * @param font
	 **************************************************************************/
	public void setFont(Font font);

	/***************************************************************************
	 * Obtain the currently defined code-style font
	 * 
	 * @return The font
	 **************************************************************************/
	public Font getCodeFont();

	/***************************************************************************
	 * Obtain the default text style
	 * 
	 * @return The style
	 **************************************************************************/
	public TextStyle getDefaultStyle();

	/***************************************************************************
	 * Obtain the default text style
	 * 
	 * @return The style
	 **************************************************************************/
	public TextStyle getSelectedStyle();

	/***************************************************************************
	 * Obtain the default text style
	 * 
	 * @return The style
	 **************************************************************************/
	public TextStyle getHighlightedStyle();

	/***************************************************************************
	 * Obtain the style corresponding to the given token type
	 * 
	 * @param type
	 *            The token type
	 * @return The corresponding style
	 **************************************************************************/
	public TextStyle getStyle(TokenTypes type);

	/***************************************************************************
	 * Dispose resources
	 **************************************************************************/
	public void dispose();
}
