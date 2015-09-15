///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model
// 
// FILE      : FontInfo.java
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
package com.astra.ses.spell.gui.model;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class FontInfo
{
	private String	m_id;
	private Font	m_font;
	private String	m_face;
	private int	   m_style;
	private int	   m_size;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param xmlElement
	 *            The XML config file element for a font definition
	 * 
	 **************************************************************************/
	public FontInfo(Element xmlElement)
	{
		if (xmlElement.getNodeType() == Node.ELEMENT_NODE)
		{
			m_id = xmlElement.getAttribute("id");
			m_face = xmlElement.getAttribute("face");
			String fontSize = xmlElement.getAttribute("size");
			m_size = Integer.parseInt(fontSize);
			String fontStyle = xmlElement.getAttribute("style");
			if (fontStyle.equals("bold"))
			{
				m_style = SWT.BOLD;
			}
			else if (fontStyle.equals("italic"))
			{
				m_style = SWT.ITALIC;
			}
			else
			{
				m_style = SWT.NORMAL;
			}
			if (m_face.equals("header"))
			{
				m_font = JFaceResources.getHeaderFont();
			}
			else if (m_face.equals("dialog"))
			{
				if (m_style == SWT.BOLD)
				{
					m_font = JFaceResources.getFontRegistry().getBold(
					        JFaceResources.DIALOG_FONT);
				}
				else if (m_style == SWT.ITALIC)
				{
					m_font = JFaceResources.getFontRegistry().getItalic(
					        JFaceResources.DIALOG_FONT);
				}
				else
				{
					m_font = JFaceResources.getFontRegistry().get(
					        JFaceResources.DIALOG_FONT);
				}
			}
			else
			{
				m_font = new Font(
				        Display.getCurrent(),
				        new FontData[] { new FontData(m_face, m_size, m_style) });
			}
		}
	}

	/***************************************************************************
	 * Obtain the font identifier
	 **************************************************************************/
	public String getId()
	{
		return m_id;
	}

	/***************************************************************************
	 * Obtain the font object
	 **************************************************************************/
	public Font getFont()
	{
		return m_font;
	}

	/***************************************************************************
	 * Obtain the font object with given size
	 **************************************************************************/
	public Font getFont(int size)
	{
		m_font = new Font(Display.getCurrent(), m_face, size, m_style);
		return m_font;
	}
}
