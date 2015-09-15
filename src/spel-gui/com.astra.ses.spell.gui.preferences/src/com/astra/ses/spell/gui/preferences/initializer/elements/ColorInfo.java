///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.preferences.initializer.elements
// 
// FILE      : ColorInfo.java
//
// DATE      : 2010-05-27
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
package com.astra.ses.spell.gui.preferences.initializer.elements;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;

/********************************************************************************
 * 
 * Color info provides information about colors
 * 
 *******************************************************************************/
public class ColorInfo
{
	public String	            id;
	public int	                red;
	public int	                green;
	public int	                blue;
	private static final String	SEPARATOR	= ",";

	/***************************************************************************
	 * Create a ColorInfo instance from a serialized object
	 * 
	 * @return
	 * @see toString to know how a ColorInfo is serialized
	 **************************************************************************/
	public static ColorInfo valueOf(String stringifiedColor)
	{
		String[] s = stringifiedColor.split(SEPARATOR);
		return new ColorInfo(Integer.valueOf(s[0]), Integer.valueOf(s[1]),
		        Integer.valueOf(s[2]));
	}

	/***************************************************************************
	 * Default constructor
	 * 
	 * @param name
	 * @param r
	 * @param g
	 * @param b
	 **************************************************************************/
	public ColorInfo(int r, int g, int b)
	{
		red = r;
		green = g;
		blue = b;
	}

	/***************************************************************************
	 * Constructor
	 * 
	 * @param xmlElement
	 *            The XML config file element for a color definition
	 * 
	 **************************************************************************/
	public ColorInfo(Element xmlElement)
	{
		if (xmlElement.getNodeType() == Node.ELEMENT_NODE)
		{
			id = xmlElement.getAttribute("id");
			String colorDef = xmlElement.getTextContent();
			if (colorDef == null)
			{
				Logger.error("Bad color definition: " + id, Level.CONFIG, this);
				return;
			}
			String[] defs = colorDef.split(":");
			if (defs.length != 3)
			{
				Logger.error("Bad color definition: " + id, Level.CONFIG, this);
				return;
			}
			red = Integer.parseInt(defs[0]);
			green = Integer.parseInt(defs[1]);
			blue = Integer.parseInt(defs[2]);
		}
	}

	/***************************************************************************
	 * Serialize this instance into a String
	 **************************************************************************/
	public String toString()
	{
		return red + SEPARATOR + green + SEPARATOR + blue;
	}

}
