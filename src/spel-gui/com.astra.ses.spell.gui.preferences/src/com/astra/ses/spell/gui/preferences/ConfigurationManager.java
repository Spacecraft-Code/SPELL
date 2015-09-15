///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.preferences
// 
// FILE      : ConfigurationManager.java
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
package com.astra.ses.spell.gui.preferences;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.core.interfaces.BaseService;
import com.astra.ses.spell.gui.core.model.server.AuthenticationData;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.types.Environment;
import com.astra.ses.spell.gui.core.model.types.ICoreConstants;
import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.Scope;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.preferences.initializer.GUIPreferencesInitializer;
import com.astra.ses.spell.gui.preferences.initializer.elements.CommandsInfo;
import com.astra.ses.spell.gui.preferences.initializer.elements.StatusInfo;
import com.astra.ses.spell.gui.preferences.initializer.elements.StyleInfo;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.preferences.keys.GuiColorKey;
import com.astra.ses.spell.gui.preferences.keys.PreferenceCategory;
import com.astra.ses.spell.gui.preferences.keys.ProcColorKey;
import com.astra.ses.spell.gui.preferences.keys.PropertyKey;
import com.astra.ses.spell.gui.preferences.keys.StatusColorKey;
import com.astra.ses.spell.gui.preferences.model.BooleanValue;
import com.astra.ses.spell.gui.preferences.model.PresentationsManager;
import com.astra.ses.spell.gui.preferences.model.ServersManager;
import com.astra.ses.spell.gui.preferences.utils.PreferencesConverter;
import com.astra.ses.spell.gui.types.ExecutorStatus;
import com.astra.ses.spell.gui.types.GuiExecutorCommand;

/*******************************************************************************
 * Configuration manager provides attributes stored in the preferences system
 ******************************************************************************/
public class ConfigurationManager extends BaseService implements IConfigurationManager
{
	// PRIVATE
	// ------------------------------------------------------------------
	/** Holds the path of the XML configuration file */
	private static final String	DEFAULT_CONFIG_PATH	   = "config";
	private static final String	INTERNAL_CONFIG_PATH	= "data";
	private static final String	DEFAULT_CONFIG_FILE	   = "default-config.xml";
	/** Preferences service */
	private IPreferenceStore	m_preferences;
	// PUBLIC ------------------------------------------------------------------
	/** Holds the service ID */
	public static final String	ID	                   = "com.astra.ses.spell.gui.config.ConfigurationManager";
	/** Presentations separator */
	public static final String	PRESENTATION_SEPARATOR	= "<!>";
	/** Holds the current configuration file */
	private String	            m_configFile;
	
	/** Holds the generated colors */
	private Map<String,Color> m_colors;

	/***************************************************************************
	 * Default constructor
	 **************************************************************************/
	public ConfigurationManager()
	{
		super(ID);
		m_configFile = resolveConfigurationFile();
		m_colors = new TreeMap<String,Color>();
		Logger.debug("Created", Level.CONFIG, this);
	}

	@Override
	public void cleanup()
	{
		Logger.debug("Cleanup", Level.CONFIG, this);
		for(Color c : m_colors.values())
		{
			c.dispose();
		}
	}

	@Override
	public void setup()
	{
		m_preferences = Activator.getDefault().getPreferenceStore();
	}

	@Override
	public void subscribe()
	{
	}

	@Override
	public String getConfigurationFile()
	{
		return m_configFile;
	}

	/***************************************************************************
	 * Set the configuration file
	 **************************************************************************/
	public void setConfigurationFile( String path )
	{
		m_configFile = path;
		GUIPreferencesInitializer initializer = new GUIPreferencesInitializer();
		initializer.initializeDefaultPreferences();
	}

	/***************************************************************************
	 * Get the configuration file
	 **************************************************************************/
	private String resolveConfigurationFile()
	{
		// File separator
		String pathSeparator = File.separator;
		// Home
		String home = System.getenv(ICoreConstants.CLIENT_HOME_ENV);
		if (home == null)
		{
			home = ".";
		}

		String[] args = Platform.getApplicationArgs();
		String path = null;
		if (args.length > 0)
		{
			int count = 0;
			for (String arg : args)
			{
				if (arg.equals("-config") && (args.length - 1) > count)
				{
					path = args[count + 1];
					break;
				}
				count++;
			}
		}
		if (path == null)
		{
			path = home + pathSeparator + DEFAULT_CONFIG_PATH + pathSeparator + DEFAULT_CONFIG_FILE;
			if (!(new File(path).canRead()))
			{
				String loc = Platform.getBundle(Activator.PLUGIN_ID).getLocation();
				loc = loc.substring(loc.lastIndexOf(":") + 1, loc.length());
				path = loc + pathSeparator + INTERNAL_CONFIG_PATH + pathSeparator + DEFAULT_CONFIG_FILE;
			}
		}
		return path;
	}

	@Override
	public String getEnvironmentVariable(Environment var)
	{
		return System.getenv(var.systemName());
	}

	@Override
	public String[] getAvailableServers()
	{
		String managerRepr = m_preferences.getString(PreferenceCategory.SERVER.tag);
		ServersManager manager = ServersManager.fromString(managerRepr);
		return manager.getServerIds();
	}

	@Override
	public void updateServers(ServerInfo[] servers)
	{
		ServersManager mgr = new ServersManager(servers);
		String repr = mgr.toString();
		m_preferences.setValue(PreferenceCategory.SERVER.tag, repr);
	}

	@Override
	public ServerInfo getServerData(String serverID)
	{
		String managerRepr = m_preferences.getString(PreferenceCategory.SERVER.tag);
		ServersManager manager = ServersManager.fromString(managerRepr);
		ServerInfo info = manager.getServer(serverID);
		
		// If the server information exists (may not, when a manual connection is used)
		if (info != null)
		{
			// Put the connectivity defaults if the server has none
			if (info.getAuthentication()==null)
			{
				info.setAuthentication( getConnectivityDefaults() );
			}
		}

		return info;
	}

	@Override
	public void restoreServers()
	{
		m_preferences.setToDefault(PreferenceCategory.SERVER.tag);
		m_preferences.setToDefault(PropertyKey.INITIAL_SERVER.getPreferenceName());
	}

	@Override
	public Vector<String> getPresentations()
	{
		String reprPresentations = m_preferences.getString(PreferenceCategory.PRESENTATIONS.tag);
		PresentationsManager mgr = PresentationsManager.valueOf(reprPresentations);
		return mgr.getEnabledPresentations();
	}

	@Override
	public Vector<String> getDisabledPresentations()
	{
		String reprPresentations = m_preferences.getString(PreferenceCategory.PRESENTATIONS.tag);
		PresentationsManager mgr = PresentationsManager.valueOf(reprPresentations);
		return mgr.getDisabledPresentations();
	}

	@Override
	public void updatePresentations(String[] enabled, String[] disabled)
	{
		Vector<String> enabledPres = new Vector<String>(Arrays.asList(enabled));
		Vector<String> disabledPres = new Vector<String>(Arrays.asList(disabled));
		PresentationsManager mgr = new PresentationsManager(enabledPres, disabledPres);
		String serialized = mgr.toString();
		m_preferences.putValue(PreferenceCategory.PRESENTATIONS.tag, serialized);
	}

	@Override
	public void updateConnectivityDefaults( AuthenticationData auth )
	{
		m_preferences.putValue(PreferenceCategory.CONNECTIVITY.tag, auth.toString());
	}

	@Override
	public AuthenticationData getConnectivityDefaults()
	{
		String repr = m_preferences.getString(PreferenceCategory.CONNECTIVITY.tag);
		if (repr != null && !repr.trim().isEmpty())
		{
			return AuthenticationData.valueOf(repr);
		}
		return null;
	}

	@Override
	public void restorePresentations()
	{
		m_preferences.setToDefault(PreferenceCategory.PRESENTATIONS.tag);
	}

	@Override
	public String getProperty(PropertyKey pref)
	{
		return m_preferences.getString(pref.getPreferenceName());
	}

	@Override
	public void setProperty(PropertyKey pref, String value)
	{
		m_preferences.setValue(pref.getPreferenceName(), value);
	}

	@Override
	public void resetProperty(PropertyKey pref)
	{
		m_preferences.setToDefault(pref.getPreferenceName());
	}

	@Override
	public boolean getBooleanProperty(PropertyKey pref)
	{
		String value = this.getProperty(pref);
		return BooleanValue.valueOf(value).booleanValue();
	}

	@Override
	public void setBooleanProperty(PropertyKey pref, boolean value)
	{
		m_preferences.setValue(pref.getPreferenceName(), BooleanValue.valueOf(value).toString());
	}

	@Override
	public Color getStatusColor(ItemStatus status)
	{
		StatusColorKey key = PreferencesConverter.getStatusColor(status);
		return getColor(key.getPreferenceName());
	}

	@Override
	public void setStatusColor(ItemStatus status, RGB color)
	{
		StatusColorKey key = PreferencesConverter.getStatusColor(status);
		setColor(key.getPreferenceName(), color);
	}

	@Override
	public void resetStatusColor(StatusColorKey key)
	{
		m_preferences.setToDefault(key.getPreferenceName());
	}

	@Override
	public Color getGuiColor(GuiColorKey key)
	{
		return getColor(key.getPreferenceName());
	}

	@Override
	public void setGuiColor(GuiColorKey key, RGB color)
	{
		setColor(key.getPreferenceName(), color);
	}

	@Override
	public void resetGuiColor(GuiColorKey key)
	{
		m_preferences.setToDefault(key.getPreferenceName());
	}

	@Override
	public Color getProcedureColor(ExecutorStatus status)
	{
		return getColor(ProcColorKey.getPreferenceName(status));
	}

	@Override
	public Color getProcedureColorDark(ExecutorStatus status)
	{
		
		return getColor(ProcColorKey.getPreferenceName(status) + "-DARK");
	}

	@Override
	public void setProcedureColor(ExecutorStatus status, RGB color)
	{
		setColor(ProcColorKey.getPreferenceName(status), color);
	}

	@Override
	public void resetProcedureColor(ExecutorStatus st)
	{
		m_preferences.setToDefault(ProcColorKey.getPreferenceName(st));
	}

	@Override
	public Color getScopeColor(Scope scope)
	{
		String prefId = PreferenceCategory.STYLES.tag + "." + scope.tag;
		String styleRepr = m_preferences.getString(prefId);
		if ((styleRepr != null) && (!styleRepr.trim().isEmpty()))
		{
			StyleInfo info = StyleInfo.valueOf(styleRepr);
			RGB rgb = new RGB(info.color_r, info.color_g, info.color_b);
			return new Color(Display.getCurrent(), rgb);
		}
		return null;
	}

	@Override
	public String getScopeFont(Scope scope)
	{
		String styleRepr = m_preferences.getString(PreferenceCategory.STYLES.tag + "." + scope.tag);
		StyleInfo info = StyleInfo.valueOf(styleRepr);
		return info.font;
	}

	@Override
	public int getScopeStyle(Scope scope)
	{
		String styleRepr = m_preferences.getString(PreferenceCategory.STYLES.tag + "." + scope.tag);
		StyleInfo info = StyleInfo.valueOf(styleRepr);
		return info.style;
	}

	@Override
	public void setScopeStyleInfo(Scope scope, StyleInfo style)
	{
		String prefId = PreferenceCategory.STYLES.tag + "." + scope.tag;
		String styleRepr = style.toString();
		m_preferences.setValue(prefId, styleRepr);
	}

	@Override
	public void restoreScopeStyleInfo()
	{
		for (Scope scope : Scope.values())
		{
			String key = PreferenceCategory.STYLES.tag + "." + scope.tag;
			m_preferences.setToDefault(key);
		}
	}

	@Override
	public Color getColor(String preferenceName)
	{
		if (!m_colors.containsKey(preferenceName))
		{
			boolean dark = preferenceName.endsWith("-DARK");
			String prefCode = preferenceName.replace("-DARK","");
			RGB rgb = PreferenceConverter.getColor(m_preferences, prefCode);
			if (dark)
			{
				rgb.red -= 25; 
				rgb.blue -= 25; 
				rgb.green -= 25; 
			}
			m_colors.put(preferenceName, new Color(Display.getDefault(), rgb));
		}
		return m_colors.get(preferenceName);
	}

	@Override
	public void setColor(String preferenceName, RGB color)
	{
		PreferenceConverter.setValue(m_preferences, preferenceName, color);
		// Override color if already buffered
		if (m_colors.containsKey(preferenceName))
		{
			m_colors.get(preferenceName).dispose();
			boolean dark = preferenceName.endsWith("-DARK");
			String prefCode = preferenceName.replace("-DARK","");
			RGB rgb = PreferenceConverter.getColor(m_preferences, prefCode);
			if (dark)
			{
				rgb.red -= 25; 
				rgb.blue -= 25; 
				rgb.green -= 25; 
			}
			m_colors.put(preferenceName, new Color(Display.getDefault(), rgb));
		}
	}

	@Override
	public Font getFont(FontKey code)
	{
		FontData data = PreferenceConverter.getFontData(m_preferences, code.getPreferenceName());
		return new Font(Display.getDefault(), data);
	}

	@Override
	public Font getFont(FontKey code, int size)
	{
		FontData data = PreferenceConverter.getFontData(m_preferences, code.getPreferenceName());
		data.height = (float) size;
		return new Font(Display.getDefault(), data);
	}

	@Override
	public void setFont(FontKey key, Font font)
	{
		PreferenceConverter.setValue(m_preferences, key.getPreferenceName(), font.getFontData());
	}

	@Override
	public void addPropertyChangeListener(IPropertyChangeListener listener)
	{
		m_preferences.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(IPropertyChangeListener listener)
	{
		m_preferences.removePropertyChangeListener(listener);
	}

	@Override
	public DateFormat getTimeFormat() {
		DateFormat ret;
		// Time configuration
		int timeFormat = Integer.parseInt(getProperty(PropertyKey.TDS_TIME_FORMAT));
	    switch (timeFormat)
	    {
	    case 0:
	    	ret = new SimpleDateFormat("yyyy.DDD.HH.mm.ss");
	    	break;
	    case 1:
	    	ret = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    	break;
	    default:
	    	ret = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    }
	    return ret;
	}

	@Override
	public CommandsInfo getCommands() 
	{
		// Extract commands from preferences
		String commands = m_preferences.getString(PreferenceCategory.COMMANDS.tag);
		
		// Create commands info
		CommandsInfo info;
		if ( commands.isEmpty() )
		{
			// If preferences does not contain then use default configuration
			info = getDefaultCommands();
		}
		else
		{
			// Use preferences configuration
			info = new CommandsInfo(commands);
		}
		return info;
	}
	
	@Override
	public void setCommands(CommandsInfo commands)
	{
		m_preferences.setValue(PreferenceCategory.COMMANDS.tag, commands.getText());
	}
	
	@Override
	public void restoreCommands()
	{
		m_preferences.setToDefault(PreferenceCategory.COMMANDS.tag);
	}
	
	@Override
	public StatusInfo getStatus() 
	{
		// Extract status from preferences
		String status = m_preferences.getString(PreferenceCategory.STATUS.tag);
		
		// Create status info
		StatusInfo info;
		if ( status.isEmpty() )
		{
			// If preferences does not contain then use default configuration
			info = getDefaultStatus();
		}
		else
		{
			// Use preferences configuration
			info = new StatusInfo(status);
		}
		
		return info;
	}
	
	@Override
	public void setStatus(StatusInfo status) 
	{
		m_preferences.setValue(PreferenceCategory.STATUS.tag,status.getText());
	}
	
	@Override
	public void restoreStatus()
	{
		m_preferences.setToDefault(PreferenceCategory.STATUS.tag);
	}
	
	/**
	 * Get default commands
	 * 
	 * @return
	 */
	private CommandsInfo getDefaultCommands()
	{
		LinkedHashMap<String,String> commands = new LinkedHashMap<String,String>();
		for (GuiExecutorCommand command : GuiExecutorCommand.values())
		{
			commands.put(command.command.getId(),command.label);
		}
		return new CommandsInfo(commands);
	}
	
	/**
	 * Get default status
	 * 
	 * @return
	 */
	private StatusInfo getDefaultStatus()
	{
		return new StatusInfo(GuiExecutorCommand.values().length+";Status");
	}
}
