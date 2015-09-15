////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.outline
// 
// FILE      : OutlineDecorator.java
//
// DATE      : Sep 22, 2010
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
////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.model.outline;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.model.outline.nodes.OutlineNode;
import com.astra.ses.spell.gui.outline.Fragment;

public class OutlineDecorator implements ILabelDecorator
{

	/** Category decorating image */
	private ImageDescriptor	m_categoryImage;

	public OutlineDecorator()
	{
		String id = Fragment.FRAGMENT_ID;
		String baseLocation = "platform:/plugin/" + id;
		m_categoryImage = Activator.getImageDescriptor(baseLocation
		        + "/icons/10x10/folder.png");
	}

	@Override
	public void addListener(ILabelProviderListener listener)
	{
	}

	@Override
	public void dispose()
	{
	}

	@Override
	public boolean isLabelProperty(Object element, String property)
	{
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener)
	{
	}

	@Override
	public Image decorateImage(Image image, Object element)
	{
		OutlineNode node = (OutlineNode) element;
		Image result = null;
		switch (node.getType())
		{
		case CATEGORY:
			DecorationOverlayIcon icon = new DecorationOverlayIcon(image,
			        m_categoryImage, IDecoration.BOTTOM_RIGHT);
			result = icon.createImage();
			break;
		default:
			break;
		}

		return result;
	}

	@Override
	public String decorateText(String text, Object element)
	{
		OutlineNode node = (OutlineNode) element;
		String result = null;
		switch (node.getType())
		{
		case DECLARATION:
		case ERROR:
		case GOTO:
		case STEP:
			String line = String.valueOf(node.getLineNo());
			result = "Line " + line + " : " + text;
			break;
		case ROOT:
		case CATEGORY:
		default:
			break;
		}
		return result;
	}

}
