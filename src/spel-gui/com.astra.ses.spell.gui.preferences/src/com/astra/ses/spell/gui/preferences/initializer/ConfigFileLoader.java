////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.preferences.initializer
// 
// FILE      : ConfigFileLoader.java
//
// DATE      : 2010-11-09
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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.astra.ses.spell.gui.core.model.server.AuthenticationData;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.preferences.initializer.elements.ColorInfo;
import com.astra.ses.spell.gui.preferences.initializer.elements.CommandsInfo;
import com.astra.ses.spell.gui.preferences.initializer.elements.FontInfo;
import com.astra.ses.spell.gui.preferences.initializer.elements.StatusInfo;
import com.astra.ses.spell.gui.preferences.initializer.elements.StyleInfo;
import com.astra.ses.spell.gui.preferences.keys.PreferenceCategory;
import com.astra.ses.spell.gui.preferences.keys.PropertyKey;
import com.astra.ses.spell.gui.preferences.model.BooleanValue;
import com.astra.ses.spell.gui.preferences.model.PresentationsManager;
import com.astra.ses.spell.gui.preferences.model.ServersManager;

/*******************************************************************************
 * 
 * {@link ConfigFileLoader} parses the config xml file and notifies the given
 * {@link IPreferenceSetter} object about parsed values for storing them
 * 
 ******************************************************************************/
public class ConfigFileLoader
{

	/** Profile document DOM model */
	private String m_pathToFile;
	/** Preferences setter */
	private IPreferenceSetter m_setter;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param pathToFile
	 *            the absolute path to the configuration file to load
	 **************************************************************************/
	public ConfigFileLoader(String pathToFile, IPreferenceSetter setter)
	{
		m_pathToFile = pathToFile;
		m_setter = setter;
	}

	/***************************************************************************
	 * Determine the default preferences file to read
	 * 
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 **************************************************************************/
	private Document resolveConfigurationFile(String path) throws ParserConfigurationException, SAXException, IOException
	{
		// Create the XML Document object
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		FileInputStream stream = new FileInputStream(path);
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(stream);
	}

	/***************************************************************************
	 * Read the settings file and store its values in the preferences node
	 * 
	 * @param node
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 **************************************************************************/
	public void loadPreferences() throws ParserConfigurationException, SAXException, IOException
	{
		Document doc = resolveConfigurationFile(m_pathToFile);
		Element docElement = doc.getDocumentElement();
		/* Read general properties */
		loadProperties(docElement);
		/* Read connectivity info */
		AuthenticationData defaultConnectivity = loadConnectivity(docElement);
		/* Read presentations info */
		loadPresentations(docElement);
		/* Read look and fell properties */
		loadLookAndFeel(docElement);
		/* Read procedure buttons */
		loadProcPanel(docElement);
		/* read server properties */
		loadServers(docElement,defaultConnectivity);
		Logger.info("All properties loaded", Level.CONFIG, this);
	}

	/***************************************************************************
	 * Load and Store general preferences
	 * 
	 * @param node
	 * @param nl
	 **************************************************************************/
	private void loadProperties(Element doc)
	{
		NodeList nl = doc.getElementsByTagName("property");

		// Predefined values, in case the properties are missing in the file
		for (PropertyKey key : PropertyKey.values())
		{
			m_setter.setValue(key.getPreferenceName(), key.getHardcodedDefault());
		}

		/*
		 * Load default properties from file
		 */
		for (int count = 0; count < nl.getLength(); count++)
		{
			Element element = (Element) nl.item(count);
			String name = element.getAttribute("name");
			String value = element.getTextContent();
			String id = PreferenceCategory.GENERAL.tag + "." + name;
			m_setter.setValue(id, value);
			Logger.info("Loaded property: " + id + "->" + value, Level.CONFIG, this);
		}

		/*
		 * Set initial empty values for last connection properties
		 */
		m_setter.setValue(PropertyKey.LAST_SERVER_CONNECTED.getPreferenceName(), "");
		m_setter.setValue(PropertyKey.LAST_HOST_CONNECTED.getPreferenceName(), "");
		m_setter.setValue(PropertyKey.LAST_PORT_CONNECTED.getPreferenceName(), "");
		m_setter.setValue(PropertyKey.LAST_CONNECTION_MANUAL.getPreferenceName(), BooleanValue.NO.toString());
	}

	/***************************************************************************
	 * Load procedure presentation settings
	 * 
	 * @param node
	 * @param doc
	 **************************************************************************/
	private void loadPresentations(Element doc)
	{
		/*
		 * Presentations list. the first one will be the default presentation
		 */
		Vector<String> enabledPresentations = new Vector<String>();
		Vector<String> disabledPresentations = new Vector<String>();

		/*
		 * Read the first "presentations" tag found
		 */
		NodeList nl = doc.getElementsByTagName("presentations");

		if (nl == null || nl.getLength() == 0)
		{
			Logger.error("No presentation configuration found!", Level.CONFIG, this);
			return;
		}
		Element presentations = (Element) nl.item(0);

		NodeList pDefinitions = presentations.getElementsByTagName("presentation");

		if (pDefinitions == null || pDefinitions.getLength() == 0)
		{
			Logger.error("No presentations found!", Level.CONFIG, this);
		}
		else
		{
			for (int count = 0; count < pDefinitions.getLength(); count++)
			{
				Node node = pDefinitions.item(count);
				NamedNodeMap attrs = node.getAttributes();
				String pName = attrs.getNamedItem("name").getNodeValue();
				boolean defaultPresentation = false;
				boolean enabledPresentation = true;

				Node defaultNode = attrs.getNamedItem("default");
				if (defaultNode != null)
				{
					defaultPresentation = defaultNode.getNodeValue().toLowerCase().equals("yes");
				}

				Node enabledNode = attrs.getNamedItem("enabled");
				if (enabledNode != null)
				{
					enabledPresentation = enabledNode.getNodeValue().toLowerCase().equals("yes");
				}

				if (defaultPresentation && !enabledPresentation)
				{
					enabledPresentation = true;
					Logger.error("Default presentation " + pName + " can't be disabled. Force enabled", Level.CONFIG, this);
				}

				Vector<String> target = enabledPresentation ? enabledPresentations : disabledPresentations;
				if (defaultPresentation)
				{
					Logger.debug("Added DEFAULT presentation: " + pName, Level.CONFIG, this);
					target.add(0, pName);
				}
				else
				{
					Logger.debug("Added presentation: " + pName, Level.CONFIG, this);
					target.add(pName);
				}
			}
		}
		/*
		 * Create the string representations to store
		 */
		PresentationsManager mgr = new PresentationsManager(enabledPresentations, disabledPresentations);
		m_setter.setValue(PreferenceCategory.PRESENTATIONS.tag, mgr.toString());
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private AuthenticationData loadConnectivity(Element doc)
	{
		/*
		 * Read the main "connectivity" tag if any
		 */
		
		Node connectivity = null;
		NodeList nl = doc.getChildNodes();
		for(int index = 0; index< nl.getLength(); index++)
		{
			Node node = nl.item(index);
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("connectivity"))
			{
				connectivity = node;
				break;
			}
		}

		if (connectivity == null)
		{
			Logger.info("No general connectivity configuration found", Level.CONFIG, this);
			return null;
		}

		AuthenticationData auth = new AuthenticationData( connectivity );

		Logger.info("Default connectivity settings loaded", Level.CONFIG, this);

		m_setter.setValue(PreferenceCategory.CONNECTIVITY.tag, auth.toString());
		
		return auth;
	}

	/***************************************************************************
	 * Store look and feel preferences
	 * 
	 * @param node
	 * @param nl
	 **************************************************************************/
	private void loadLookAndFeel(Element doc)
	{
		NodeList nl = doc.getElementsByTagName("appearance");

		if (nl == null || nl.getLength() == 0)
		{
			Logger.error("No appearance configuration found!", Level.CONFIG, this);
			return;
		}
		Element appearance = (Element) nl.item(0);

		NodeList colorDefinitions = appearance.getElementsByTagName("colors");
		Element colorDefs = (Element) colorDefinitions.item(0);

		/*
		 * Status colors
		 */
		NodeList statusColors = colorDefs.getElementsByTagName("statuscolors");
		if (statusColors == null || statusColors.getLength() == 0)
		{
			Logger.error("No status color definitions found!", Level.CONFIG, this);
		}
		else
		{
			NodeList colorList = statusColors.item(0).getChildNodes();
			for (int count = 0; count < colorList.getLength(); count++)
			{
				Node node = colorList.item(count);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element color = (Element) node;
					ColorInfo info = new ColorInfo(color);
					String key = PreferenceCategory.STATUS_COLOR.tag + "." + info.id;
					RGB rgb = new RGB(info.red, info.green, info.blue);
					m_setter.setRGBValue(key, rgb);
				}
			}
		}

		/*
		 * GUI Colors
		 */
		NodeList guiColors = appearance.getElementsByTagName("guicolors");
		if (guiColors == null || guiColors.getLength() == 0)
		{
			Logger.error("No gui color definitions found!", Level.CONFIG, this);
		}
		else
		{
			NodeList colorList = guiColors.item(0).getChildNodes();
			for (int count = 0; count < colorList.getLength(); count++)
			{
				Node node = colorList.item(count);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element color = (Element) node;
					ColorInfo info = new ColorInfo(color);
					String key = PreferenceCategory.GUI_COLOR.tag + "." + info.id;
					RGB rgb = new RGB(info.red, info.green, info.blue);
					m_setter.setRGBValue(key, rgb);
				}
			}
		}

		/*
		 * Load procedure colors
		 */
		NodeList procColors = appearance.getElementsByTagName("proccolors");
		if (procColors == null || procColors.getLength() == 0)
		{
			Logger.error("No proc color definitions found!", Level.CONFIG, this);
		}
		else
		{
			NodeList colorList = procColors.item(0).getChildNodes();
			for (int count = 0; count < colorList.getLength(); count++)
			{
				Node node = colorList.item(count);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element color = (Element) node;
					ColorInfo info = new ColorInfo(color);
					String key = PreferenceCategory.PROC_COLOR.tag + "." + info.id;
					RGB rgb = new RGB(info.red, info.green, info.blue);
					m_setter.setRGBValue(key, rgb);
				}
			}
		}

		/*
		 * Load fonts from the preferences file
		 */
		NodeList fontDefinitions = appearance.getElementsByTagName("fonts");
		Element fontDefs = (Element) fontDefinitions.item(0);

		NodeList fontList = fontDefs.getElementsByTagName("font");
		if (fontList == null || fontList.getLength() == 0)
		{
			Logger.error("No font definitions found!", Level.CONFIG, this);
		}
		else
		{
			for (int count = 0; count < fontList.getLength(); count++)
			{
				Node node = fontList.item(count);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element fontDef = (Element) node;
					FontInfo info = new FontInfo(fontDef);
					String key = PreferenceCategory.FONT.tag + "." + info.id;
					Font font = new Font(Display.getDefault(), info.face, (int) info.size, info.style);
					m_setter.setFontValue(key, font.getFontData());
				}
			}
		}

		/*
		 * Load styles from the preferences file
		 */
		NodeList styleDefinitions = appearance.getElementsByTagName("styles");
		Element styleDefs = (Element) styleDefinitions.item(0);

		NodeList styleList = styleDefs.getElementsByTagName("style");
		if (styleList == null || styleList.getLength() == 0)
		{
			Logger.error("No style definitions found!", Level.CONFIG, this);
		}
		else
		{
			for (int count = 0; count < styleList.getLength(); count++)
			{
				Node node = styleList.item(count);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element styleDef = (Element) node;
					StyleInfo info = new StyleInfo(styleDef);
					String key = PreferenceCategory.STYLES.tag + "." + info.id;
					m_setter.setValue(key, info.toString());
				}
			}
		}
	}
	
	/***************************************************************************
	 * Load and Store procedure panel components
	 * 
	 * @param node
	 * @param nl
	 **************************************************************************/
	private void loadProcPanel(Element doc)
	{
		NodeList nl = doc.getElementsByTagName("procpanel");
		
		if (nl == null || nl.getLength() == 0 )
		{
			Logger.error("No proc panel configuration found!", Level.CONFIG, this);
			return;
		}
		
		Element presentations = (Element) nl.item(0);
		NodeList pDefinitions = presentations.getElementsByTagName("component");
		if (pDefinitions == null || pDefinitions.getLength() == 0) 
        {
			Logger.error("No proc panel components found!", Level.CONFIG, this);
		}
		else
		{
			LinkedHashMap<String,String> components = new LinkedHashMap<String,String>();
			for (int count = 0; count < pDefinitions.getLength(); count++)
			{
				Node node = pDefinitions.item(count);				
				String id = node.getAttributes().getNamedItem("id").getTextContent();
				String descr = node.getTextContent();
				if(components.keySet().contains(id))
				{
					Logger.error("Component repeated! Check configuration file", Level.CONFIG, this);
				}
				else if ( id!=null && !id.isEmpty())
				{
					components.put(id,descr);
					Logger.debug("Added component: " + id, Level.CONFIG, this);
				}
			}
			CommandsInfo commands = new CommandsInfo(components);
			String key = PreferenceCategory.COMMANDS.tag;
			m_setter.setValue(key,commands.getText());
			
			StatusInfo status = new StatusInfo(components);
			m_setter.setValue(PreferenceCategory.STATUS.tag,status.getText());
		} 
	}	

	/***************************************************************************
	 * Load default servers as stored in preferences
	 * 
	 * @param node
	 * @param nl
	 *************************************************************************/
	private void loadServers(Element doc, AuthenticationData data)
	{
		// Read the first "servers" tag found
		NodeList nl = doc.getElementsByTagName("servers");

		if (nl == null || nl.getLength() == 0)
		{
			Logger.error("No SPELL server names found!", Level.CONFIG, this);
			return;
		}
		Element servers = (Element) nl.item(0);

		NodeList serverList = servers.getElementsByTagName("server");
		if (serverList == null || serverList.getLength() == 0)
		{
			Logger.error("No SPELL server names found!", Level.CONFIG, this);
			return;
		}
		ArrayList<ServerInfo> serversList = new ArrayList<ServerInfo>();
		/*
		 * Iterate over the found server nodes
		 */
		for (int count = 0; count < serverList.getLength(); count++)
		{
			Element server = (Element) serverList.item(count);

			ServerInfo info = new ServerInfo(server,data);

			serversList.add(info);
		}
		/*
		 * Create a ServersManager instance
		 */
		ServerInfo[] serversArray = new ServerInfo[serversList.size()];
		serversList.toArray(serversArray);
		ServersManager manager = new ServersManager(serversArray);
		String repr = manager.toString();
		/*
		 * Store the preferences
		 */
		m_setter.setValue(PreferenceCategory.SERVER.tag, repr);
	}
}
