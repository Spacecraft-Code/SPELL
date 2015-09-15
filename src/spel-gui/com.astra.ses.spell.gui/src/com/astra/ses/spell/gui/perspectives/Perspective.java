///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.perspectives
// 
// FILE      : Perspective.java
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
package com.astra.ses.spell.gui.perspectives;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.views.MasterView;
import com.astra.ses.spell.gui.views.NavigationView;

/*******************************************************************************
 * @brief Unique perspective of the RCP application. Sets up all the views and
 *        folders used by the GUI.
 * @date 09/10/07
 ******************************************************************************/
public class Perspective implements IPerspectiveFactory
{
	public static final String FOLDER_ADDITIONS = "com.astra.ses.spell.gui.views.additions";

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	public void createInitialLayout(IPageLayout layout)
	{
		Logger.debug("Creating initial layout", Level.INIT, this);

		layout.setEditorAreaVisible(false);

		layout.addView(NavigationView.ID, IPageLayout.LEFT, 0.22f, layout.getEditorArea());
		layout.addView(MasterView.ID, IPageLayout.TOP, 0.95f, layout.getEditorArea());

		IFolderLayout folder = layout.createFolder(FOLDER_ADDITIONS, IPageLayout.BOTTOM, 0.4f, NavigationView.ID);

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint ep = registry.getExtensionPoint("org.eclipse.ui.views");
		IExtension[] extensions = ep.getExtensions();

		for(IExtension ext : extensions)
		{
			for(IConfigurationElement cfgElem : ext.getConfigurationElements())
			{
				String elementId = cfgElem.getAttribute("id");
				if (elementId.startsWith("com.astra.ses.spell.gui.views.tools"))
				{
					Logger.debug("Adding tool view: " + elementId, Level.INIT, this);
					folder.addView(elementId);
					
					IViewLayout vLayout = layout.getViewLayout(elementId);
					vLayout.setCloseable(false);
					vLayout.setMoveable(false);
				}
			}

		}

		IViewLayout vLayout = layout.getViewLayout(NavigationView.ID);
		vLayout.setCloseable(false);
		vLayout.setMoveable(false);
		
		vLayout = layout.getViewLayout(MasterView.ID);
		vLayout.setCloseable(false);
		vLayout.setMoveable(false);
		
		Logger.debug("Layout created", Level.INIT, this);
	}
	

}
