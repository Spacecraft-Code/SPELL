///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.preferences.initializer
// 
// FILE      : GUIPreferencesInitializer.java
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
package com.astra.ses.spell.gui.preferences.initializer;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.xml.sax.SAXException;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.preferences.Activator;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;

/******************************************************************************
 * 
 * PreferencesInitializer will load the configuration file and store its
 * contents into the eclipse preferences system
 * 
 *****************************************************************************/
public class GUIPreferencesInitializer extends AbstractPreferenceInitializer implements IPreferenceSetter
{

	/** Profile document DOM model */
	private static final String	PATH_TO_CONFIG;
	/** Preferences store */
	private IPreferenceStore	m_store;

	/***************************************************************************
	 * Get config file location
	 **************************************************************************/
	static
	{
		IConfigurationManager mgr = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		PATH_TO_CONFIG = mgr.getConfigurationFile();
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public GUIPreferencesInitializer()
	{
		// Get default scope
		m_store = Activator.getDefault().getPreferenceStore();
	}

	@Override
	public void initializeDefaultPreferences()
	{
		ConfigFileLoader loader = new ConfigFileLoader(PATH_TO_CONFIG, this);
		try
		{
			loader.loadPreferences();
		}
		catch (FileNotFoundException e)
		{
			Logger.error("Cannot read configuration" + " file: '" + PATH_TO_CONFIG + "'", Level.CONFIG, this);
			System.exit(1);
		}
		catch (ParserConfigurationException e)
		{
			Logger.error("Parse (CFG) error: " + e.getLocalizedMessage(), Level.CONFIG, this);
			System.exit(1);
		}
		catch (SAXException e)
		{
			Logger.error("Parse (SAX) error: " + e.getLocalizedMessage(), Level.CONFIG, this);
			System.exit(1);
		}
		catch (IOException e)
		{
			Logger.error("Parse (UKN) error: " + e.getLocalizedMessage(), Level.CONFIG, this);
			System.exit(1);
		}
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
		m_store.setDefault(name, value);
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
		PreferenceConverter.setDefault(m_store, name, rgb);
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
		PreferenceConverter.setDefault(m_store, name, font);
	}
}
