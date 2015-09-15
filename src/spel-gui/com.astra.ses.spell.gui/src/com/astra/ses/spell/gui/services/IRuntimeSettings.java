///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.services
// 
// FILE      : IRuntimeSettings.java
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
package com.astra.ses.spell.gui.services;

import com.astra.ses.spell.gui.core.interfaces.IService;

/*******************************************************************************
 * 
 * RuntimeSettingsService will handle propeties which are only valid during
 * application lifecycle. That means that they won't be stored as preferences
 * are being made.
 ******************************************************************************/
public interface IRuntimeSettings extends IService 
{
	public interface IRuntimeSettingsListener
	{
		public void runtimePropertyChanged(RuntimeProperty property,
		        Object newValue);
	}

	/***************************************************************************
	 * 
	 * Runtime property keys
	 * 
	 **************************************************************************/
	public enum RuntimeProperty
	{

		ID_SERVER_SELECTION("ServerSelection"), ID_CONTEXT_SELECTION(
		        "ContextSelection"), ID_PROCEDURE_SELECTION(
		        "ProcedureSelection"), ID_NAVIGATION_VIEW_SELECTION(
		        "NavigationViewSelection");

		/** tag */
		public String	tag;

		/***************************************************************************
		 * Constructor
		 * 
		 * @param tag
		 *            the tag which identifies this enum in the preferences
		 *            system
		 **************************************************************************/
		private RuntimeProperty(String tag)
		{
			this.tag = tag;
		}
	}

	/***************************************************************************
	 * Add listener to settings changes
	 * 
	 * @param listener
	 **************************************************************************/
	public void addRuntimeSettingsListener(IRuntimeSettingsListener listener);

	/***************************************************************************
	 * Remove listener to settings changes
	 * 
	 * @param listener
	 **************************************************************************/
	public void removeRuntimeSettingsListener(IRuntimeSettingsListener listener);

	/***************************************************************************
	 * Store selected object using the given key as index
	 * 
	 * @param key
	 *            the runtime property key
	 * @param selected
	 *            the object to store for the given jey
	 **************************************************************************/
	public void setRuntimeProperty(RuntimeProperty key, Object element);

	/***************************************************************************
	 * Retrieved objected stored with the given key
	 * 
	 * @param key
	 *            the key used to store the required object
	 * @return the objected stored with the given key
	 **************************************************************************/
	public Object getRuntimeProperty(RuntimeProperty key);
}
