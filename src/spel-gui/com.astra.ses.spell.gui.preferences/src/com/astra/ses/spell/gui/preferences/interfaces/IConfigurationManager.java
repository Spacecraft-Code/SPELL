///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.preferences.interfaces
// 
// FILE      : IConfigurationManager.java
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
package com.astra.ses.spell.gui.preferences.interfaces;

import java.text.DateFormat;
import java.util.Vector;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;

import com.astra.ses.spell.gui.core.interfaces.IService;
import com.astra.ses.spell.gui.core.model.server.AuthenticationData;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.types.Environment;
import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.core.model.types.Scope;
import com.astra.ses.spell.gui.preferences.initializer.elements.CommandsInfo;
import com.astra.ses.spell.gui.preferences.initializer.elements.StatusInfo;
import com.astra.ses.spell.gui.preferences.initializer.elements.StyleInfo;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.preferences.keys.GuiColorKey;
import com.astra.ses.spell.gui.preferences.keys.ProcColorKey;
import com.astra.ses.spell.gui.preferences.keys.PropertyKey;
import com.astra.ses.spell.gui.preferences.keys.StatusColorKey;
import com.astra.ses.spell.gui.types.ExecutorStatus;

/*******************************************************************************
 * Configuration manager provides attributes stored in the preferences system
 ******************************************************************************/
public interface IConfigurationManager extends IService
{
	/***************************************************************************
	 * Get the configuration file
	 **************************************************************************/
	public String getConfigurationFile();

	/***************************************************************************
	 * Set the configuration file
	 **************************************************************************/
	public void setConfigurationFile( String path );

	/***************************************************************************
	 * Get the value of a environment variable
	 * 
	 * @param var
	 *            the {@link Environment} variable
	 * @return the variable value as it is defined in the os
	 **************************************************************************/
	public String getEnvironmentVariable(Environment var);

	/***************************************************************************
	 * Obtain the list of available servers. This list is defined in the XML
	 * configuration file.
	 * 
	 * @return The list of available servers
	 **************************************************************************/
	public String[] getAvailableServers();

	/***************************************************************************
	 * Update available servers info
	 * 
	 * @param servers
	 **************************************************************************/
	public void updateServers(ServerInfo[] servers);

	/***************************************************************************
	 * Obtain the server information
	 * 
	 * @return Server data structure
	 **************************************************************************/
	public ServerInfo getServerData(String serverID);

	/***************************************************************************
	 * Restore servers configuration to its default value
	 **************************************************************************/
	public void restoreServers();

	/***************************************************************************
	 * Get the required procedure presentations
	 **************************************************************************/
	public Vector<String> getPresentations();

	/***************************************************************************
	 * Get user disabled presentations
	 * 
	 * @return
	 **************************************************************************/
	public Vector<String> getDisabledPresentations();

	/***************************************************************************
	 * Update presentations property
	 **************************************************************************/
	public void updatePresentations(String[] enabled, String[] disabled);

	/***************************************************************************
	 * Restore presentations to its default value
	 **************************************************************************/
	public void restorePresentations();

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void updateConnectivityDefaults( AuthenticationData auth );

	/***************************************************************************
	 * 
	 **************************************************************************/
	public AuthenticationData getConnectivityDefaults();

	/***************************************************************************
	 * Return a preferences values as stored in the eclipse preferences system
	 * 
	 * @param id
	 *            the id of the property to look for
	 * @return the stringified value
	 **************************************************************************/
	public String getProperty(PropertyKey pref);

	/***************************************************************************
	 * Store a value in the preferences node
	 * 
	 * @param id
	 *            the id of the property to look for
	 * @return the stringified value
	 **************************************************************************/
	public void setProperty(PropertyKey pref, String value);

	/***************************************************************************
	 * Reset this property to its defautl value
	 * 
	 * @param pref
	 **************************************************************************/
	public void resetProperty(PropertyKey pref);

	/***************************************************************************
	 * Return a preferences values as stored in the eclipse preferences system,
	 * but as a boolean value
	 * 
	 * @param id
	 *            the id of the property to look for
	 * @return the stringified value
	 **************************************************************************/
	public boolean getBooleanProperty(PropertyKey pref);

	/***************************************************************************
	 * Store a boolean value
	 * 
	 * @param pref
	 * @param value
	 **************************************************************************/
	public void setBooleanProperty(PropertyKey pref, boolean value);

	/***************************************************************************
	 * Obtain a predefined color
	 * 
	 * @param code
	 *            Color code
	 * @return The color object
	 **************************************************************************/
	public Color getStatusColor(ItemStatus status);

	/***************************************************************************
	 * Store a color model in preferences
	 * 
	 * @param key
	 * @param color
	 **************************************************************************/
	public void setStatusColor(ItemStatus status, RGB color);

	/***************************************************************************
	 * Set the preference related to the given {@link StatusColorKey} to its
	 * default value
	 * 
	 * @param key
	 **************************************************************************/
	public void resetStatusColor(StatusColorKey key);

	/***************************************************************************
	 * Obtain a predefined color
	 * 
	 * @param code
	 *            Color code
	 * @return The color object
	 **************************************************************************/
	public Color getGuiColor(GuiColorKey key);

	/***************************************************************************
	 * Store a color model in preferences
	 * 
	 * @param key
	 * @param color
	 **************************************************************************/
	public void setGuiColor(GuiColorKey key, RGB color);

	/***************************************************************************
	 * Set the preference related to the given {@link GuiColorKey} to its
	 * default value
	 * 
	 * @param key
	 **************************************************************************/
	public void resetGuiColor(GuiColorKey key);

	/***************************************************************************
	 * Obtain a predefined color
	 * 
	 * @param code
	 *            Color code
	 * @return The color object
	 **************************************************************************/
	public Color getProcedureColor(ExecutorStatus status);

	/***************************************************************************
	 * Obtain a predefined color
	 * 
	 * @param code
	 *            Color code
	 * @return The color object
	 **************************************************************************/
	public Color getProcedureColorDark(ExecutorStatus status);

	/***************************************************************************
	 * Store a color model in preferences
	 * 
	 * @param key
	 * @param color
	 **************************************************************************/
	public void setProcedureColor(ExecutorStatus status, RGB color);

	/***************************************************************************
	 * Set the preference related to the given {@link ProcColorKey} to its
	 * default value
	 * 
	 * @param key
	 **************************************************************************/
	public void resetProcedureColor(ExecutorStatus st);

	/***************************************************************************
	 * Obtain a predefined style
	 **************************************************************************/
	public Color getScopeColor(Scope scope);

	/***************************************************************************
	 * Obtain a predefined style
	 **************************************************************************/
	public String getScopeFont(Scope scope);

	/***************************************************************************
	 * Obtain a predefined style
	 **************************************************************************/
	public int getScopeStyle(Scope scope);

	/***************************************************************************
	 * Update the style info associated to the given scope
	 **************************************************************************/
	public void setScopeStyleInfo(Scope scope, StyleInfo style);

	/***************************************************************************
	 * Restore scope styles to its default values
	 **************************************************************************/
	public void restoreScopeStyleInfo();

	/***************************************************************************
	 * Get the color stored in the given preference name
	 * 
	 * @param preferenceName
	 **************************************************************************/
	public Color getColor(String preferenceName);

	/***************************************************************************
	 * Store the given color in the given preference key
	 * 
	 * @param preferenceName
	 * @param color
	 **************************************************************************/
	public void setColor(String preferenceName, RGB color);

	/***************************************************************************
	 * Obtain predefined font
	 * 
	 * @return The font
	 **************************************************************************/
	public Font getFont(FontKey code);

	/***************************************************************************
	 * Obtain predefined font
	 * 
	 * @return The font
	 **************************************************************************/
	public Font getFont(FontKey code, int size);

	/***************************************************************************
	 * Set a font for the given key preference
	 * 
	 * @param key
	 * @param font
	 **************************************************************************/
	public void setFont(FontKey key, Font font);
	
	/***************************************************************************
	 * Get commands info
	 * 
	 **************************************************************************/
	public CommandsInfo getCommands();
	
	/***************************************************************************
	 * Set commands info
	 * 
	 **************************************************************************/
	public void setCommands(CommandsInfo commands);
	
	/***************************************************************************
	 * Restore commands info
	 * 
	 **************************************************************************/
	public void restoreCommands();
	
	/***************************************************************************
	 * Get status location
	 * 
	 **************************************************************************/
	public StatusInfo getStatus();
	
	/***************************************************************************
	 * Set status location
	 * 
	 **************************************************************************/
	public void setStatus(StatusInfo status);
	
	/***************************************************************************
	 * Restore status location
	 * 
	 **************************************************************************/
	public void restoreStatus();

	/***************************************************************************
	 * Add a preference change listener
	 **************************************************************************/
	public void addPropertyChangeListener(IPropertyChangeListener listener);

	/***************************************************************************
	 * Add a preference change listener
	 **************************************************************************/
	public void removePropertyChangeListener(IPropertyChangeListener listener);
	
	/***************************************************************************
	 * Add a preference change listener
	 **************************************************************************/
	public DateFormat getTimeFormat ();
}
