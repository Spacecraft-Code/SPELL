///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.preferences.model
// 
// FILE      : PresentationsManager.java
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
package com.astra.ses.spell.gui.preferences.model;

import java.util.Arrays;
import java.util.Vector;

/*******************************************************************************
 * 
 * PresentationsManager will handle the information stored in preferences
 * relative to procedure presentations
 * 
 ******************************************************************************/
public class PresentationsManager
{

	/** Presentations separator */
	private static final String	SEPARATOR	      = ":presentation:";
	/** Enabled spearator */
	private static final String	ENABLED_SEPARATOR	= ":enabled:";
	/** Enabled presentation */
	private Vector<String>	    m_enabledPresentations;
	/** Disabled presentation */
	private Vector<String>	    m_disabledPresentations;

	/***************************************************************************
	 * Create a PresentationManager instance from a serialized instance
	 * 
	 * @param serializedManager
	 * @return
	 **************************************************************************/
	public static PresentationsManager valueOf(String serializedManager)
	{
		/*
		 * Process string as stored in the preferences system
		 */
		String[] splitted = serializedManager.split(ENABLED_SEPARATOR, -2);
		/* Enabled presentations */
		String enabledStr = splitted[0];

		/*
		 * Process splitted strings for constructing the PresentationsManager
		 * instance
		 */
		Vector<String> enabled = new Vector<String>();
		if (enabledStr.length() > 0)
		{
			String[] en = enabledStr.split(SEPARATOR, 0);
			enabled.addAll(Arrays.asList(en));
		}

		/* Disabled presentations */
		Vector<String> disabled = new Vector<String>();
		String disabledStr = splitted[1];
		if (disabledStr.length() > 0)
		{
			String[] di = disabledStr.split(SEPARATOR, 0);
			disabled.addAll(Arrays.asList(di));
		}

		/*
		 * Instance object
		 */
		return new PresentationsManager(enabled, disabled);
	}

	/***************************************************************************
	 * Default constructor
	 * 
	 * @param enabled
	 * @param disabled
	 **************************************************************************/
	public PresentationsManager(Vector<String> enabled, Vector<String> disabled)
	{
		m_enabledPresentations = enabled;
		m_disabledPresentations = disabled;
	}

	/**************************************************************************
	 * Get enabled presentations
	 * 
	 * @return
	 *************************************************************************/
	public Vector<String> getEnabledPresentations()
	{
		return m_enabledPresentations;
	}

	/***************************************************************************
	 * Return disabled presentations
	 * 
	 * @return
	 **************************************************************************/
	public Vector<String> getDisabledPresentations()
	{
		return m_disabledPresentations;
	}

	@Override
	public String toString()
	{
		/* Enabled presentations */
		String enabled = "";
		for (String enabledPres : m_enabledPresentations)
		{
			enabled = enabled + enabledPres + SEPARATOR;
		}
		/* Disabled presentations */
		String disabled = "";
		for (String disabledPres : m_disabledPresentations)
		{
			disabled = disabled + disabledPres + SEPARATOR;
		}
		/* result */
		String result = enabled + ENABLED_SEPARATOR + disabled;
		return result;
	}
}
