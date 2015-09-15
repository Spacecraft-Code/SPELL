///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm
// 
// FILE      : CommInterfaceFactory.java
//
// DATE      : 2008-11-21 08:58
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
package com.astra.ses.spell.gui.core.comm;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.astra.ses.spell.gui.core.interfaces.ICommInterface;

/*******************************************************************************
 * @brief Factory for communication interfaces
 * @date 22/04/08
 ******************************************************************************/
public class CommInterfaceFactory
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private static final String	 EXTENSION_ID	= "com.astra.ses.spell.gui.extensions.CommunicationInterface";
	private static final boolean	DEBUG	   = false;
	private static final String	 ELEMENT_NAME	= "name";
	private static final String	 ELEMENT_DESC	= "description";
	private static final String	 ELEMENT_CLASS	= "class";

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	/***************************************************************************
	 * Create a communication interface of the given type
	 **************************************************************************/
	public static ICommInterface createCommInterface()
	{
		ICommInterface ifc = null;
		if (DEBUG) System.out.println("[*] Loading extension for point '"
		        + EXTENSION_ID + "'");
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint ep = registry.getExtensionPoint(EXTENSION_ID);
		if (ep == null)
		{
			System.err
			        .println("[FATAL] Could not find any commmunications interface extension");
			return null;
		}
		IExtension[] extensions = ep.getExtensions();
		if (DEBUG) System.out.println("[*] Defined extensions: "
		        + extensions.length);
		if (extensions.length == 0) return null;
		IExtension extension = extensions[0];
		if (DEBUG) System.out.println("[*] Extension ID: "
		        + extension.getUniqueIdentifier());
		// Obtain the configuration element for this extension point
		IConfigurationElement cfgElem = extension.getConfigurationElements()[0];
		String elementName = cfgElem.getAttribute(ELEMENT_NAME);
		String elementDesc = cfgElem.getAttribute(ELEMENT_DESC);
		String elementClass = cfgElem.getAttribute(ELEMENT_CLASS);
		if (DEBUG)
		{
			System.out.println("[*] Extension properties");
			System.out.println("[*]     - Name : " + elementName);
			System.out.println("[*]     - Desc : " + elementDesc);
			System.out.println("[*]     - Class: " + elementClass);
		}
		try
		{
			ifc = ICommInterface.class.cast(cfgElem
			        .createExecutableExtension(ELEMENT_CLASS));
			if (DEBUG) System.out.println("[*] Extension loaded");
		}
		catch (CoreException ex)
		{
			ex.printStackTrace();
		}
		return ifc;
	}
}
