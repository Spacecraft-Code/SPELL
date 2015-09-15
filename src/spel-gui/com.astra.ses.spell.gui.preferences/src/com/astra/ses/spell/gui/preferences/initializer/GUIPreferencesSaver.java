///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.preferences.initializer
// 
// FILE      : GUIPreferencesSaver.java
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Vector;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

import com.astra.ses.spell.gui.core.model.server.AuthenticationData;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.types.Scope;
import com.astra.ses.spell.gui.preferences.Activator;
import com.astra.ses.spell.gui.preferences.initializer.elements.CommandsInfo;
import com.astra.ses.spell.gui.preferences.initializer.elements.StatusInfo;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.preferences.keys.GuiColorKey;
import com.astra.ses.spell.gui.preferences.keys.PreferenceCategory;
import com.astra.ses.spell.gui.preferences.keys.ProcColorKey;
import com.astra.ses.spell.gui.preferences.keys.PropertyKey;
import com.astra.ses.spell.gui.preferences.keys.StatusColorKey;
import com.astra.ses.spell.gui.preferences.model.PresentationsManager;
import com.astra.ses.spell.gui.preferences.model.ServersManager;
import com.astra.ses.spell.gui.types.ExecutorStatus;

/******************************************************************************
 * 
 * PreferencesInitializer will load the configuration file and store its
 * contents into the eclipse preferences system
 * 
 *****************************************************************************/
public class GUIPreferencesSaver
{
	private String	m_configFile;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public GUIPreferencesSaver(String configFile)
	{
		m_configFile = configFile;
	}

	/***************************************************************************
	 * Read the settings file and store its values in the preferences node
	 * 
	 * @param node
	 **************************************************************************/
	public void savePreferences()
	{
		FileOutputStream stream;
		try
		{
			stream = new FileOutputStream(m_configFile);
			OutputStreamWriter swriter = new OutputStreamWriter(stream);
			PrintWriter writer = new PrintWriter(swriter);
			IPreferenceStore prefStore = Activator.getDefault()
			        .getPreferenceStore();

			fileHeader(writer);
			dumpProperties(prefStore, writer);
			dumpConnectivity(prefStore, writer);
			dumpPresentations(prefStore, writer);
			dumpAppearance(prefStore, writer);
			dumpProcPanel(prefStore, writer);
			dumpServers(prefStore, writer);
			fileFooter(writer);

			writer.close();
			stream.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void dumpProperties(IPreferenceStore store, PrintWriter writer)
	{
		for (PropertyKey key : PropertyKey.values())
		{
			String propValue = store.getString(key.getPreferenceName());
			writer.println("    <property name=\"" + key.getPropertyName()
			        + "\">" + propValue + "</property>");
		}
		writer.println();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void dumpPresentations(IPreferenceStore store, PrintWriter writer)
	{
		String representation = store
		        .getString(PreferenceCategory.PRESENTATIONS.tag);
		// Tabular:presentation:Text:presentation:Shell:presentation::enabled:
		PresentationsManager mgr = PresentationsManager.valueOf(representation);
		Vector<String> enabled = mgr.getEnabledPresentations();
		Vector<String> disabled = mgr.getDisabledPresentations();
		writer.println("    <presentations>");
		int count = 0;
		for (String presentation : enabled)
		{
			writer.print("        <presentation name=\"" + presentation + "\"");
			if (count == 0) writer.print(" default=\"yes\"");
			writer.println(" enabled=\"yes\"/>");
			count++;

		}
		for (String presentation : disabled)
		{
			writer.println("        <presentation name=\"" + presentation
			        + "\" enabled=\"no\"/>");
		}
		writer.println("    </presentations>");
		writer.println();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void dumpConnectivity(IPreferenceStore store, PrintWriter writer)
	{
		String authRepr = store.getString(PreferenceCategory.CONNECTIVITY.tag);
		if (authRepr != null && !authRepr.trim().isEmpty())
		{
			AuthenticationData auth = AuthenticationData.valueOf(authRepr);
			if (auth == null) return;
			writer.println("    <connectivity>");
			writer.println("        <user>" + auth.getUsername() + "</user>");
			if (auth.getPassword()!=null) writer.println("        <pwd>" + auth.getPassword() + "</pwd>");
			if (auth.getKeyFile()!=null) writer.println("        <key>" + auth.getKeyFile() + "</key>");
			writer.println("    </connectivity>");
			writer.println(" ");
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void dumpAppearance(IPreferenceStore store, PrintWriter writer)
	{
		writer.println("    <appearance>");
		dumpFonts(store, writer);
		dumpStyles(store, writer);
		dumpColors(store, writer);
		writer.println("    </appearance>");
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void dumpFonts(IPreferenceStore store, PrintWriter writer)
	{
		writer.println("        <fonts>");
		for (FontKey key : FontKey.values())
		{
			try
			{
				FontData data = PreferenceConverter.getFontData(store,
				        key.getPreferenceName());
				String styleStr = "norm";
				if (data.getStyle() == SWT.BOLD) styleStr = "bold";
				if (data.getStyle() == SWT.ITALIC) styleStr = "italic";
				if (data.getStyle() == (SWT.BOLD | SWT.ITALIC)) styleStr = "bold-italic";
				dumpFont(key.tag, data.getName(),
				        Integer.toString((int) data.height), styleStr, writer);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		writer.println("        </fonts>");
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void dumpStyles(IPreferenceStore store, PrintWriter writer)
	{
		writer.println("        <styles>");
		for (Scope scope : Scope.values())
		{
			try
			{
				String styleRepr = store
				        .getString(PreferenceCategory.STYLES.tag + "."
				                + scope.tag);
				String[] elements = styleRepr.split("\\|");
				String font = elements[1];
				String color = elements[2] + ":" + elements[3] + ":"
				        + elements[4];
				int style = Integer.parseInt(elements[5]);
				String styleStr = "norm";
				if (style == SWT.BOLD) styleStr = "bold";
				if (style == SWT.ITALIC) styleStr = "italic";
				if (style == (SWT.BOLD | SWT.ITALIC)) styleStr = "bold-italic";
				dumpStyle(scope.tag, font, color, styleStr, writer);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		writer.println("        </styles>");
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void dumpColors(IPreferenceStore store, PrintWriter writer)
	{
		writer.println("        <colors>");
		dumpStatusColors(store, writer);
		dumpGuiColors(store, writer);
		dumpProcColors(store, writer);
		writer.println("        </colors>");
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void dumpStatusColors(IPreferenceStore store, PrintWriter writer)
	{
		writer.println("            <statuscolors>");
		for (StatusColorKey key : StatusColorKey.values())
		{
			try
			{
				RGB rgb = PreferenceConverter.getColor(store,
				        key.getPreferenceName());
				String repr = rgb.red + ":" + rgb.green + ":" + rgb.blue;
				dumpColor(key.tag, repr, writer);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		writer.println("            </statuscolors>");
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void dumpGuiColors(IPreferenceStore store, PrintWriter writer)
	{
		writer.println("            <guicolors>");
		for (GuiColorKey key : GuiColorKey.values())
		{
			try
			{
				RGB rgb = PreferenceConverter.getColor(store,
				        key.getPreferenceName());
				String repr = rgb.red + ":" + rgb.green + ":" + rgb.blue;
				dumpColor(key.tag, repr, writer);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		writer.println("            </guicolors>");
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void dumpProcColors(IPreferenceStore store, PrintWriter writer)
	{
		writer.println("            <proccolors>");
		for (ExecutorStatus st : ExecutorStatus.values())
		{
			try
			{
				String tag = ProcColorKey.getPreferenceName(st);
				RGB rgb = PreferenceConverter.getColor(store, tag);
				String repr = rgb.red + ":" + rgb.green + ":" + rgb.blue;
				dumpColor(st.toString(), repr, writer);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		writer.println("            </proccolors>");
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void dumpProcPanel(IPreferenceStore store, PrintWriter writer)
	{
		writer.println("    <procpanel>");
		
		String label = store.getString(PreferenceCategory.COMMANDS.tag);
		CommandsInfo commandsInfo = new CommandsInfo(label);
		
		String status = store.getString(PreferenceCategory.STATUS.tag);
		StatusInfo statusInfo = new StatusInfo(status);
		int pos = statusInfo.getLocation();
		
		// Writing components (i.e. commands and status)
		int index=0;
		for (String command : commandsInfo.getMap().keySet())
		{
			if ( index == pos )
			{
				writer.println("            <component id=" + StatusInfo.STATUS_ID + ">" + statusInfo.getLabel() + "</component>");
			}			
			writer.println("            <component id" + command + ">" + commandsInfo.getMap().get(command) + "</component>");			
			index++;
		}
		// Writing status (if applies)
		if ( index<=pos || pos==-1 ) {
			writer.println("            <component>" + StatusInfo.STATUS_ID + "</component>");			
		}
		
		writer.println("    </procpanel>");
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private void dumpServers(IPreferenceStore store, PrintWriter writer)
	{
		writer.println("    <servers>");
		String managerRepr = store.getString(PreferenceCategory.SERVER.tag);
		ServersManager manager = ServersManager.fromString(managerRepr);
		String[] serverIds = manager.getServerIds();
		for (String id : serverIds)
		{
			ServerInfo info = manager.getServer(id);
			dumpServer(info.getName(), info.getHost(), Integer.toString(info.getPort()), info.getRole().toString(), info.getAuthentication(),writer);
		}
		writer.println("    </servers>");
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void dumpColor(String id, String repr, PrintWriter writer)
	{
		writer.println("                <color id=\"" + id + "\">" + repr
		        + "</color>");
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void dumpFont(String id, String face, String size, String style,
	        PrintWriter writer)
	{
		writer.println("            <font id=\"" + id + "\" face=\"" + face
		        + "\" size=\"" + size + "\" style=\"" + style + "\"/>");
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void dumpStyle(String id, String font, String color, String style,
	        PrintWriter writer)
	{
		writer.println("            <style id=\"" + id + "\" font=\"" + font
		        + "\" color=\"" + color + "\" style=\"" + style + "\"/>");
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void dumpServer(String name, String host, String port, String role, AuthenticationData auth, PrintWriter writer)
	{
		writer.println("        <server>");
		writer.println("            <name>" + name + "</name>");
		writer.println("            <host>" + host + "</host>");
		writer.println("            <port>" + port + "</port>");
		writer.println("            <role>" + role + "</role>");
		if (auth != null)
		{
			writer.println("            <connectivity>");
			writer.println("                <user>" + auth.getUsername() + "</user>");
			if (auth.getPassword()!=null) writer.println("                <pwd>" + auth.getPassword() + "</pwd>");
			if (auth.getKeyFile()!=null) writer.println("                <key>" + auth.getKeyFile() + "</key>");
			writer.println("            </connectivity>");
		}
		writer.println("        </server>");
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void fileHeader(PrintWriter writer)
	{
		writer.println("<?xml version=\"1.0\"?>");
		writer.println();
		writer.println("<configuration>");
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void fileFooter(PrintWriter writer)
	{
		writer.println("</configuration>");
	}
}
