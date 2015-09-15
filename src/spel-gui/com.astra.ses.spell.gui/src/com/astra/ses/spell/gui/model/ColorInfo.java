///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model
// 
// FILE      : ColorInfo.java
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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;

public class ColorInfo
{
	private String	m_id;
	private Color	m_rgb;

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
			m_id = xmlElement.getAttribute("id");
			String colorDef = xmlElement.getTextContent();
			if (colorDef == null)
			{
				Logger.error("Bad color definition: " + m_id, Level.CONFIG,
				        this);
				m_rgb = null;
				return;
			}
			String[] defs = colorDef.split(":");
			if (defs.length != 3)
			{
				Logger.error("Bad color definition: " + m_id, Level.CONFIG,
				        this);
				m_rgb = null;
				return;
			}
			int colorR = Integer.parseInt(defs[0]);
			int colorG = Integer.parseInt(defs[1]);
			int colorB = Integer.parseInt(defs[2]);
			m_rgb = new Color(Display.getCurrent(), colorR, colorG, colorB);
		}
	}

	/***************************************************************************
	 * Obtain the color identifier
	 **************************************************************************/
	public String getId()
	{
		return m_id;
	}

	/***************************************************************************
	 * Obtain the color object
	 **************************************************************************/
	public Color getColor()
	{
		return m_rgb;
	}
}
