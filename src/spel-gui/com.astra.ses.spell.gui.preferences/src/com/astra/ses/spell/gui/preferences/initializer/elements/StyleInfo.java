///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.preferences.initializer.elements
// 
// FILE      : StyleInfo.java
//
// DATE      : Oct 13, 2010
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

import org.eclipse.swt.SWT;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;

public class StyleInfo
{
	private static final String	SEPARATOR	= "|";
	public String	            id;
	public String	            font;
	public int	                color_r;
	public int	                color_g;
	public int	                color_b;
	public int	                style;

	/***************************************************************************
	 * Return a FontInfo object from a serialized instance
	 * 
	 * @param stringifiedFont
	 * @return
	 **************************************************************************/
	public static StyleInfo valueOf(String stringifiedStyle)
	{
		String[] splitted = stringifiedStyle.split("\\|");
		return new StyleInfo(splitted[0], splitted[1],
		        Integer.parseInt(splitted[2]), Integer.parseInt(splitted[3]),
		        Integer.parseInt(splitted[4]), Integer.valueOf(splitted[5]));
	}

	/***************************************************************************
	 * Default constructor
	 * 
	 * @param face
	 * @param style
	 * @param size
	 **************************************************************************/
	public StyleInfo(String id, String fontFace, int color_r, int color_g,
	        int color_b, int style)
	{
		this.id = id;
		this.font = fontFace;
		this.color_r = color_r;
		this.color_g = color_g;
		this.color_b = color_b;
		this.style = style;
	}

	/***************************************************************************
	 * Constructor
	 * 
	 * @param xmlElement
	 *            The XML config file element for a style definition
	 * 
	 **************************************************************************/
	public StyleInfo(Element xmlElement)
	{
		if (xmlElement.getNodeType() == Node.ELEMENT_NODE)
		{
			id = xmlElement.getAttribute("id");
			font = xmlElement.getAttribute("font");
			String fontStyle = xmlElement.getAttribute("style");
			/*
			 * Different font styles are norm bold italic bold-italic
			 */
			if (fontStyle.equals("bold"))
			{
				style = SWT.BOLD;
			}
			else if (fontStyle.equals("italic"))
			{
				style = SWT.ITALIC;
			}
			else if (fontStyle.equals("bold-italic"))
			{
				style = SWT.BOLD | SWT.ITALIC;
			}
			else if (fontStyle.equals("norm"))
			{
				style = SWT.NORMAL;
			}
			else
			{
				Logger.error("Bad font style definition: " + fontStyle
				        + ". Using norm", Level.CONFIG, this);
				style = SWT.NORMAL;
			}

			String colorDef = xmlElement.getAttribute("color");
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
			color_r = Integer.parseInt(defs[0]);
			color_g = Integer.parseInt(defs[1]);
			color_b = Integer.parseInt(defs[2]);
		}
	}

	@Override
	public String toString()
	{
		return id + SEPARATOR + font + SEPARATOR + color_r + SEPARATOR
		        + color_g + SEPARATOR + color_b + SEPARATOR + style;
	}
}
