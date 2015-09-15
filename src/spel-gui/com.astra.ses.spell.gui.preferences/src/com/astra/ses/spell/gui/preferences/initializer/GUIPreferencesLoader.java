////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.preferences.initializer
// 
// FILE      : GUIPreferencesLoader.java
//
// DATE      : 2010-11-08
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
package com.astra.ses.spell.gui.preferences.initializer;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.xml.sax.SAXException;

/*******************************************************************************
 * 
 * {@link GUIPreferencesLoader} gets an exernal file and overwrites all the
 * preferences attributes with values defined in the given file
 * 
 ******************************************************************************/
public class GUIPreferencesLoader implements IPreferenceSetter
{
	/** Property to catch which makes this page to be reloaded */
	public static final String	REFRESH_PROPERTY	= "shall_refresh_page";

	/** Path to config file */
	private String	           m_pathToFile;
	/** Target preferences store */
	private IPreferenceStore	m_store;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param externalFile
	 * @param targetStore
	 *            the target store where values will be overwritten
	 **************************************************************************/
	public GUIPreferencesLoader(String externalFile,
	        IPreferenceStore targetStore)
	{
		m_pathToFile = externalFile;
		m_store = targetStore;
	}

	/***************************************************************************
	 * Overwrite the workspace preferences values with the ones defined in the
	 * m_externalFile attribute
	 **************************************************************************/
	public boolean overwrite()
	{
		ConfigFileLoader loader = new ConfigFileLoader(m_pathToFile, this);
		try
		{
			loader.loadPreferences();
			m_store.firePropertyChangeEvent(REFRESH_PROPERTY, false, true);
			return true;
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IPreferenceSetter#setValue(java.lang.String, java.lang.String)
	 * ======
	 * ====================================================================
	 */
	@Override
	public void setValue(String name, String value)
	{
		m_store.setValue(name, value);
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IPreferenceSetter#rgbToValue(String, RGB)
	 * ============================
	 * ==============================================
	 */
	public void setRGBValue(String name, RGB rgb)
	{
		PreferenceConverter.setValue(m_store, name, rgb);
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IPreferenceSetter#fontToValue(String, FontData[])
	 * ====================
	 * ======================================================
	 */
	public void setFontValue(String name, FontData[] font)
	{
		PreferenceConverter.setValue(m_store, name, font);
	}
}
