///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.preferences.ui.pages
// 
// FILE      : ServersPreferencePage.java
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
package com.astra.ses.spell.gui.preferences.ui.pages;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;

import com.astra.ses.spell.gui.core.model.server.AuthenticationData;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo.ServerRole;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.PropertyKey;

public class ServersPreferencePage extends BasicPreferencesPage
{
	/** Servers map <serverName, ServerInfo> */
	private Map<String, ServerInfo> m_serversInfo;

	/** Servers list */
	private Combo m_serversList;
	/** Default server button */
	private Combo m_defaultServer;
	/** Default context */
	private Text m_defaultContext;
	/** Server role combo widget */
	private Combo m_serverRole;
	/** Server name text widget */
	private Text m_serverName;
	/** Server host text widget */
	private Text m_serverHost;
	/** Server port text widget */
	private Text m_serverPort;
	/** Server user text widget */
	private Text m_serverUser;
	/** Server password text widget */
	private Text m_serverPassword;
	/** Server password text widget */
	private Text m_serverKeyFile;

	@Override
	protected Control createContents(Composite parent)
	{
		// CONTAINER
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		container.setLayout(layout);
		container.setLayoutData(layoutData);

		// This gridata object will be reused by every form widget
		GridData expand = new GridData(GridData.FILL_HORIZONTAL);

		// ////////////////////////////
		// Default servers group
		// ////////////////////////////
		Group defServer = new Group(container, SWT.BORDER);
		defServer.setText("Default Server");
		GridLayout defServerLayout = new GridLayout(2, false);
		GridData defServerData = new GridData(GridData.FILL_HORIZONTAL);
		defServer.setLayout(defServerLayout);
		defServer.setLayoutData(defServerData);

		Label defServerLabel = new Label(defServer, SWT.NONE);
		defServerLabel.setText("Server to connect at startup");

		m_defaultServer = new Combo(defServer, SWT.READ_ONLY);

		Label defContextLabel = new Label(defServer, SWT.NONE);
		defContextLabel.setText("Default context");

		m_defaultContext = new Text(defServer, SWT.BORDER);
		m_defaultContext.setLayoutData(GridDataFactory.copyData(expand));

		// ////////////////////////////
		// Servers group
		// ////////////////////////////
		GridLayout serverLayout = new GridLayout(2, false);
		GridData serverData = new GridData(GridData.FILL_HORIZONTAL);
		Group serverGroup = new Group(container, SWT.BORDER);
		serverGroup.setLayout(serverLayout);
		serverGroup.setLayoutData(serverData);
		serverGroup.setText("Servers information");
		// Server combo
		Label serverLabel = new Label(serverGroup, SWT.NONE);
		serverLabel.setText("Server");
		m_serversList = new Combo(serverGroup, SWT.READ_ONLY);

		// Server name
		Label serverNameLabel = new Label(serverGroup, SWT.NONE);
		serverNameLabel.setText("Name");
		m_serverName = new Text(serverGroup, SWT.BORDER);
		m_serverName.setLayoutData(GridDataFactory.copyData(expand));
		// Server host
		Label serverHostLabel = new Label(serverGroup, SWT.NONE);
		serverHostLabel.setText("Host");
		m_serverHost = new Text(serverGroup, SWT.BORDER);
		m_serverHost.setLayoutData(GridDataFactory.copyData(expand));
		// Server port
		Label serverPortLabel = new Label(serverGroup, SWT.NONE);
		serverPortLabel.setText("Port");
		m_serverPort = new Text(serverGroup, SWT.BORDER);
		m_serverPort.setLayoutData(GridDataFactory.copyData(expand));
		// Server User
		Label serverUserLabel = new Label(serverGroup, SWT.NONE);
		serverUserLabel.setText("User");
		m_serverUser = new Text(serverGroup, SWT.BORDER);
		m_serverUser.setLayoutData(GridDataFactory.copyData(expand));
		// Server password
		Label serverPasswordLabel = new Label(serverGroup, SWT.NONE);
		serverPasswordLabel.setText("Password");
		m_serverPassword = new Text(serverGroup, SWT.PASSWORD | SWT.BORDER);
		m_serverPassword.setLayoutData(GridDataFactory.copyData(expand));
		// Server keyFile
		Label serverKeyLabel = new Label(serverGroup, SWT.NONE);
		serverKeyLabel.setText("Key file");
		m_serverKeyFile = new Text(serverGroup, SWT.BORDER);
		m_serverKeyFile.setLayoutData(GridDataFactory.copyData(expand));
		// Label explaining what does setting a password mean
		new Label(serverGroup, SWT.NONE);
		Label pwdExplanation = new Label(serverGroup, SWT.WRAP);
		pwdExplanation.setText("Setting a password or key file for a server avoids user to be asked"
		        + " to enter a password while connecting to it.");
		GridData explanationData = new GridData(GridData.FILL_HORIZONTAL);
		explanationData.horizontalSpan = serverLayout.numColumns - 1;
		explanationData.widthHint = 250;
		pwdExplanation.setLayoutData(explanationData);

		// Server role
		Label serverRoleLabel = new Label(serverGroup, SWT.NONE);
		serverRoleLabel.setText("Role");
		m_serverRole = new Combo(serverGroup, SWT.READ_ONLY);
		for (ServerRole role : ServerRole.values())
		{
			m_serverRole.add(role.toString());
		}

		// Buttons group
		Composite buttons = new Composite(container, SWT.NONE);
		GridLayout buttonsLayout = new GridLayout(3, false);
		buttons.setLayout(buttonsLayout);
		// Add
		final Button addServer = new Button(buttons, SWT.PUSH);
		addServer.setText("Add");
		// Update
		final Button updateServer = new Button(buttons, SWT.PUSH);
		updateServer.setText("Update");
		// Delete
		final Button deleteServer = new Button(buttons, SWT.PUSH);
		deleteServer.setText("Delete");

		/*
		 * Event hooks
		 */
		m_serverName.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				Text text = (Text) e.widget;
				String content = text.getText();
				boolean addEnabled = !m_serversInfo.containsKey(content);
				addServer.setEnabled(addEnabled);
				updateServer.setEnabled(addEnabled);
				deleteServer.setEnabled(!addEnabled && (m_serversInfo.size() > 1));
			}
		});

		/*
		 * When modifying any of the widgets but the server name, then allow the
		 * user to update it
		 */
		ModifyListener widgetListener = new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				updateServer.setEnabled(true);
			}
		};
		m_serverHost.addModifyListener(widgetListener);
		m_serverPort.addModifyListener(widgetListener);
		m_serverUser.addModifyListener(widgetListener);
		m_serverPassword.addModifyListener(widgetListener);
		m_serverKeyFile.addModifyListener(widgetListener);
		m_serverRole.addModifyListener(widgetListener);

		/*
		 * When user clicks the add button, then a server is added
		 */
		addServer.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				String username = m_serverUser.getText();
				String password = m_serverPassword.getText();
				String keyFile = m_serverKeyFile.getText();

				AuthenticationData auth = null;
				if (username != null)
				{
					auth = new AuthenticationData(username, password, keyFile);
				}

				ServerRole role = ServerRole.valueOf(m_serverRole.getItem(m_serverRole.getSelectionIndex()));
				ServerInfo info = new ServerInfo(m_serverName.getText(), m_serverHost.getText(), Integer.valueOf(m_serverPort.getText()),
				        role, auth);
				m_serversInfo.put(info.getName(), info);

				// Get default server selection to update the widget later
				String defaultServer = m_defaultServer.getItem(m_defaultServer.getSelectionIndex());

				updateServersList();

				// Update default server widget position
				int defPos = Arrays.binarySearch(m_defaultServer.getItems(), defaultServer);
				m_defaultServer.select(defPos);
				// Update servers widget contents
				int serverPos = Arrays.binarySearch(m_serversList.getItems(), info.getName());
				m_serversList.select(serverPos);

				refreshForm(info.getName());
			}
		});

		/*
		 * When user clicks delete button, then the on-edition server is
		 * removed.
		 */
		deleteServer.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				// default server
				String defaultServer = m_defaultServer.getItem(m_defaultServer.getSelectionIndex());

				int selected = m_serversList.getSelectionIndex();
				String id = m_serversList.getItem(selected);
				m_serversInfo.remove(id);

				updateServersList();

				int newIndex = Math.max(0, selected - 1);
				String newServer = m_serversList.getItem(newIndex);
				m_serversList.select(newIndex);
				if (id.equals(defaultServer))
				{
					m_defaultServer.select(newIndex);
				}

				refreshForm(newServer);
			}
		});

		/*
		 * When user clicks update button, then the on-edition server is removed
		 */
		updateServer.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				// We have to remove current server
				String currentServer = m_serversList.getItem(m_serversList.getSelectionIndex());
				// default server
				String defaultServer = m_defaultServer.getItem(m_defaultServer.getSelectionIndex());

				ServerRole role = ServerRole.valueOf(m_serverRole.getItem(m_serverRole.getSelectionIndex()));
				
				String username = m_serverUser.getText();
				String password = m_serverPassword.getText();
				String keyFile = m_serverKeyFile.getText();

				AuthenticationData auth = null;
				if (username != null)
				{
					auth = new AuthenticationData(username, password, keyFile);
				}
				
				ServerInfo info = new ServerInfo(m_serverName.getText(), m_serverHost.getText(), Integer.valueOf(m_serverPort.getText()), role, auth);
				m_serversInfo.remove(currentServer);
				m_serversInfo.put(info.getName(), info);

				updateServersList();
				refreshForm(info.getName());

				// select the new server and update default server
				for (int i = 0; i < m_serversList.getItemCount(); i++)
				{
					String s = m_serversList.getItem(i);
					if (s.equals(info.getName()))
					{
						m_serversList.select(i);
						if (currentServer.equals(defaultServer))
						{
							m_defaultServer.select(i);
						}
						break;
					}
				}
			}
		});

		/*
		 * Update contents whenever a new server is selected in the combo widget
		 */
		m_serversList.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				int selected = m_serversList.getSelectionIndex();
				String server = m_serversList.getItem(selected);
				// Update the form widgets with relevant information
				refreshForm(server);
			}
		});

		/*
		 * Initialize the form
		 */
		updateServersList();

		String[] servers = m_serversList.getItems();

		IConfigurationManager conf = getConfigurationManager();

		String initialServer = conf.getProperty(PropertyKey.INITIAL_SERVER);
		int position = Arrays.binarySearch(servers, initialServer);
		if (position >= 0)
		{
			m_serversList.select(position);
			m_defaultServer.select(position);
			// Update the form widgets with relevant information
			refreshForm(initialServer);
		}

		String initialContext = conf.getProperty(PropertyKey.INITIAL_CONTEXT);
		m_defaultContext.setText(initialContext);

		return container;
	}

	@Override
	public void init(IWorkbench workbench)
	{
		super.init(workbench);
		IConfigurationManager conf = getConfigurationManager();
		// Load servers information from preferences
		m_serversInfo = new HashMap<String, ServerInfo>();
		for (String server : conf.getAvailableServers())
		{
			m_serversInfo.put(server, conf.getServerData(server));
		}
	}

	/***************************************************************************
	 * Fill the Servers list widget with the available servers
	 **************************************************************************/
	private void updateServersList()
	{
		m_serversList.removeAll();
		String[] items = new String[m_serversInfo.size()];
		m_serversInfo.keySet().toArray(items);
		Arrays.sort(items);
		m_serversList.setItems(items);
		m_defaultServer.setItems(items);
	}

	/***************************************************************************
	 * Update contents with the new server selection
	 * 
	 * @param newServer
	 **************************************************************************/
	private void refreshForm(String server)
	{
		ServerInfo info = m_serversInfo.get(server);
		// Update the form widgets with relevant information
		m_serverName.setText(info.getName());
		m_serverHost.setText(info.getHost());
		m_serverPort.setText(String.valueOf(info.getPort()));
		String user = "";
		String pwd = "";
		String key = "";
		if (info.getAuthentication()!= null)
		{
			user = info.getAuthentication().getUsername();
			pwd = info.getAuthentication().getPassword();
			key  = info.getAuthentication().getKeyFile();
		}
		m_serverUser.setText(user);
		m_serverPassword.setText(pwd);
		m_serverKeyFile.setText(key);
		m_serverRole.select(info.getRole().ordinal());
	}

	@Override
	public void performApply()
	{
		IConfigurationManager conf = getConfigurationManager();
		/* Servers info */
		ServerInfo[] servers = new ServerInfo[m_serversInfo.size()];
		m_serversInfo.values().toArray(servers);
		conf.updateServers(servers);
		/* Default server */
		if (m_defaultServer.getSelectionIndex() > 0)
		{
			String defServer = m_defaultServer.getItem(m_defaultServer.getSelectionIndex());
			conf.setProperty(PropertyKey.INITIAL_SERVER, defServer);
		}
		/* Default context */
		String defContext = m_defaultContext.getText();
		if ((defContext != null) && (!defContext.isEmpty()))
		{
			conf.setProperty(PropertyKey.INITIAL_CONTEXT, defContext);
		}
	}

	@Override
	public void performDefaults()
	{
		IConfigurationManager conf = getConfigurationManager();
		// restore servers configuration
		conf.restoreServers();
		// Refresh the page
		refreshPage();
	}

	@Override
	public void refreshPage()
	{
		IConfigurationManager conf = getConfigurationManager();
		// Load servers information from preferences
		m_serversInfo = new HashMap<String, ServerInfo>();
		for (String server : conf.getAvailableServers())
		{
			m_serversInfo.put(server, conf.getServerData(server));
		}
		updateServersList();

		String initialServer = conf.getProperty(PropertyKey.INITIAL_SERVER);
		int position = Arrays.binarySearch(m_defaultServer.getItems(), initialServer);
		if (position >= 0)
		{
			m_serversList.select(position);
			m_defaultServer.select(position);
			// Update the form widgets with relevant information
			refreshForm(initialServer);
		}
	}
}
