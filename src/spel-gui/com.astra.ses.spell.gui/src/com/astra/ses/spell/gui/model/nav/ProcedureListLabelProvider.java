///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.nav
// 
// FILE      : ProcedureListLabelProvider.java
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
package com.astra.ses.spell.gui.model.nav;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.model.nav.content.BaseProcedureSystemElement;

/***************************************************************************
 * Provider for the item labels and images of the tree view
 **************************************************************************/
public class ProcedureListLabelProvider extends LabelProvider
{
	private Image m_procImg;
	
	/***********************************************************************
	 * 
	 **********************************************************************/
	public ProcedureListLabelProvider()
	{
		super();
		m_procImg = Activator.imageDescriptorFromPlugin( Activator.PLUGIN_ID, "icons/cog.png" ).createImage();
	}

	/***********************************************************************
	 * Obtain the item label
	 * 
	 * @param obj
	 *            The item text data
	 * @return The label string
	 **********************************************************************/
	public String getText(Object obj)
	{
		BaseProcedureSystemElement element = (BaseProcedureSystemElement) obj;
		return element.getName();
	}

	/***********************************************************************
	 * Obtain the item image
	 * 
	 * @param obj
	 *            The item data (unused)
	 * @return The item image
	 **********************************************************************/
	public Image getImage(Object obj)
	{
		BaseProcedureSystemElement element = (BaseProcedureSystemElement) obj;
		if (!element.isLeaf()) return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		return m_procImg;
	}

	/***********************************************************************
	 * 
	 **********************************************************************/
	public void dispose()
	{
		super.dispose();
		m_procImg.dispose();
	}
}
