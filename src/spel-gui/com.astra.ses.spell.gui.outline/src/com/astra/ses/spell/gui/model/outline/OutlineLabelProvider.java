////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.outline
// 
// FILE      : OutlineLabelProvider.java
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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.model.outline.nodes.OutlineCategoryNode;
import com.astra.ses.spell.gui.model.outline.nodes.OutlineNode;
import com.astra.ses.spell.gui.model.outline.nodes.OutlineNode.OutlineNodeType;
import com.astra.ses.spell.gui.outline.Fragment;

/******************************************************************************
 * Provides the labels and icons for the callstack tree.
 *****************************************************************************/
public class OutlineLabelProvider extends LabelProvider
{

	/** Function image */
	private Image	m_declarationImg;
	/** Step image */
	private Image	m_stepImg;
	/** Goto image */
	private Image	m_gotoImg;
	/** Error image */
	private Image	m_errorImg;

	/**************************************************************************
	 * Constructor.
	 *************************************************************************/
	public OutlineLabelProvider()
	{
		String id = Fragment.FRAGMENT_ID;
		String baseLocation = "platform:/plugin/" + id;
		m_errorImg = Activator.getImageDescriptor(
		        baseLocation + "/icons/16x16/cross.png").createImage();
		m_stepImg = Activator.getImageDescriptor(
		        baseLocation + "/icons/16x16/flag_green.png").createImage();
		m_gotoImg = Activator.getImageDescriptor(
		        baseLocation + "/icons/16x16/arrow_out.png").createImage();
		m_declarationImg = Activator.getImageDescriptor(
		        baseLocation + "/icons/16x16/cog.png").createImage();
	}

	/**************************************************************************
	 * Obtain the text corresponding to the given element.
	 *************************************************************************/
	@Override
	public String getText(Object obj)
	{
		return obj.toString();
	}

	/**************************************************************************
	 * Obtain the image corresponding to the given element.
	 *************************************************************************/
	@Override
	public Image getImage(Object obj)
	{
		OutlineNode node = (OutlineNode) obj;

		Image result = PlatformUI.getWorkbench().getSharedImages()
		        .getImage(ISharedImages.IMG_OBJ_ELEMENT);

		OutlineNodeType type = node.getType();
		if (type.equals(OutlineNodeType.CATEGORY))
		{
			type = ((OutlineCategoryNode) node).getChildrenCategory();
		}
		result = getTypeImage(type);

		return result;
	}

	/**************************************************************************
	 * Disposal.
	 *************************************************************************/
	@Override
	public void dispose()
	{
		m_errorImg.dispose();
		m_stepImg.dispose();
		m_declarationImg.dispose();
	}

	/***************************************************************************
	 * Get type image
	 * 
	 * @param type
	 **************************************************************************/
	public Image getTypeImage(OutlineNodeType type)
	{
		Image result = PlatformUI.getWorkbench().getSharedImages()
		        .getImage(ISharedImages.IMG_OBJ_ELEMENT);
		switch (type)
		{
		case ERROR:
			result = m_errorImg;
			break;
		case GOTO:
			result = m_gotoImg;
			break;
		case STEP:
			result = m_stepImg;
			break;
		case DECLARATION:
			result = m_declarationImg;
			break;
		case CATEGORY:
			break;
		default:
			break;
		}
		return result;
	}
}
