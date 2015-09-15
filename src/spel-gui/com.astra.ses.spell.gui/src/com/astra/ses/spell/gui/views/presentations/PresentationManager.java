///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.presentations
// 
// FILE      : PresentationManager.java
//
// DATE      : 2008-11-24 08:34
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
package com.astra.ses.spell.gui.views.presentations;

import java.util.Vector;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.interfaces.IProcedurePresentation;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;

/*******************************************************************************
 * @brief Manages procedure presentations for a view
 * @date 09/10/07
 ******************************************************************************/
public class PresentationManager
{
	private static IConfigurationManager	   s_cfg	= null;
	/** Top composites for presentations */
	private Vector<IProcedurePresentation>	m_presentations;


	/***************************************************************************
	 * Constructor.
	 **************************************************************************/
	public PresentationManager()
	{
		if (s_cfg == null)
		{
			s_cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		}
		m_presentations = new Vector<IProcedurePresentation>();
	}
	
	/***************************************************************************
	 * Dispose all resources
	 **************************************************************************/
	public void disposeAll()
	{
		for (IProcedurePresentation p : m_presentations)
		{
			p.dispose();
		}
	}

	/***************************************************************************
	 * Enable or disable the presentations
	 **************************************************************************/
	public void enablePresentations(boolean enable)
	{
		for (IProcedurePresentation p : m_presentations)
		{
			p.setEnabled(enable);
		}
	}

	/***************************************************************************
	 * Enable or disable the autoscroll
	 **************************************************************************/
	public void setPresentationsAutoScroll(boolean enable)
	{
		for (IProcedurePresentation p : m_presentations)
		{
			p.setAutoScroll(enable);
		}
	}

	/***************************************************************************
	 * Set the current presentation
	 **************************************************************************/
	public void setPresentationSelected(int index)
	{
		for (int count = 0; count < m_presentations.size(); count++)
		{
			m_presentations.get(count).setSelected(count == index);
		}
	}

	/***************************************************************************
	 * Get the procedure presentation for the given key
	 * 
	 * @return
	 **************************************************************************/
	public IProcedurePresentation getPresentation(String extensionId)
	{
		for (IProcedurePresentation presentation : m_presentations)
		{
			String extId = presentation.getExtensionId();
			if (extId.equalsIgnoreCase(extensionId)) { return presentation; }
		}
		return null;
	}

	/***************************************************************************
	 * Get the procedure presentation for the given index
	 * 
	 * @return
	 **************************************************************************/
	public IProcedurePresentation getPresentation(int index)
	{
		return m_presentations.get(index);
	}

	/***************************************************************************
	 * Change font size
	 * 
	 * @param increase
	 *            If true, increase the font size. Otherwise decrease it.
	 **************************************************************************/
	public void zoomPresentations(boolean increase)
	{
		for (IProcedurePresentation p : m_presentations)
		{
			p.zoom(increase);
		}
	}

	/***************************************************************************
	 * Show line
	 **************************************************************************/
	public void showLine(int lineNo)
	{
		for (IProcedurePresentation p : m_presentations)
		{
			p.showLine(lineNo);
		}
	}

	/***************************************************************************
	 * Show line
	 **************************************************************************/
	public void showCurrentLine()
	{
		for (IProcedurePresentation p : m_presentations)
		{
			p.showCurrentLine();
		}
	}

	// =========================================================================
	// NON-ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Obtain the number of defined presentations
	 **************************************************************************/
	public int getNumAvailablePresentations()
	{
		return s_cfg.getPresentations().size();
	}

	/***************************************************************************
	 * Obtain the number of loaded presentations
	 **************************************************************************/
	public int getNumLoadedPresentations()
	{
		return m_presentations.size();
	}

	/***************************************************************************
	 * Load all defined procedure presentations
	 **************************************************************************/
	public void loadPresentations()
	{
		Logger.debug(this + ": Loading presentation extensions", Level.GUI,
		        this);
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint ep = registry
		        .getExtensionPoint(IProcedurePresentation.EXTENSION_ID);
		IExtension[] extensions = ep.getExtensions();
		Logger.debug(this + ": Defined extensions: " + extensions.length,
		        Level.GUI, this);
		// The same collections exists twice because for checking order
		// and the fact that every expected extension has been loaded.
		@SuppressWarnings("unchecked")
		Vector<String> requiredPresentations = (Vector<String>) s_cfg
		        .getPresentations().clone();
		@SuppressWarnings("unchecked")
		Vector<String> requiredPresentationsClone = (Vector<String>) requiredPresentations
		        .clone();
		IProcedurePresentation[] orderedPresentations = new IProcedurePresentation[requiredPresentations
		        .size()];
		for (IExtension extension : extensions)
		{
			// Obtain the configuration element for this extension point
			IConfigurationElement cfgElem = extension
			        .getConfigurationElements()[0];
			String elementName = cfgElem
			        .getAttribute(IProcedurePresentation.ELEMENT_NAME);
			String elementDesc = cfgElem
			        .getAttribute(IProcedurePresentation.ELEMENT_DESC);
			String elementClass = cfgElem
			        .getAttribute(IProcedurePresentation.ELEMENT_CLASS);
			Logger.debug(this + ": Extension name : " + elementName, Level.GUI,
			        this);
			Logger.debug(this + ": Extension desc : " + elementDesc, Level.GUI,
			        this);
			Logger.debug(this + ": Extension class: " + elementClass,
			        Level.GUI, this);
			try
			{
				IProcedurePresentation presentation = (IProcedurePresentation) IProcedurePresentation.class
				        .cast(cfgElem
				                .createExecutableExtension(IProcedurePresentation.ELEMENT_CLASS));

				int presentationPosition = requiredPresentations
				        .indexOf(presentation.getTitle());
				if (presentationPosition != -1)
				{
					orderedPresentations[presentationPosition] = presentation;
					requiredPresentationsClone.remove(presentation.getTitle());
				}
				Logger.debug(
				        this + ": Extension loaded: "
				                + presentation.getExtensionId(), Level.GUI,
				        this);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		// Once we have the presentations in the correct order, we should
		// check if there are any null object in the array
		for (IProcedurePresentation presentation : orderedPresentations)
		{
			if (presentation != null)
			{
				m_presentations.add(presentation);
			}
		}
		// TODO review this check and the error reporting
		// If we expected to load a presentation and it failed,
		// a warning message is raised
		if (requiredPresentationsClone.size() > 0)
		{
			Logger.error("Could not find the following presentation plugins: ",
			        Level.GUI, this);
			for (String pname : requiredPresentationsClone)
			{
				Logger.error("\t- " + pname, Level.GUI, this);
			}
		}
	}
}
