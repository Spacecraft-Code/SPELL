///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs
// 
// FILE      : ConnectionDialog.java
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
package com.astra.ses.spell.gui.dialogs;

import java.util.Arrays;
import java.util.Vector;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.IServerProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreContextOperationListener;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreServerOperationListener;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.server.AuthenticationData;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.types.ContextStatus;
import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.dialogs.controls.ContextTable;
import com.astra.ses.spell.gui.dialogs.controls.ContextTableColumn;
import com.astra.ses.spell.gui.extensions.GuiNotifications;
import com.astra.ses.spell.gui.model.commands.AttachContext;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.model.commands.ConnectServer;
import com.astra.ses.spell.gui.model.commands.DestroyContext;
import com.astra.ses.spell.gui.model.commands.DetachContext;
import com.astra.ses.spell.gui.model.commands.DisconnectServer;
import com.astra.ses.spell.gui.model.commands.StartContext;
import com.astra.ses.spell.gui.model.commands.StopContext;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.preferences.keys.PropertyKey;
import com.astra.ses.spell.gui.services.IRuntimeSettings;
import com.astra.ses.spell.gui.services.IRuntimeSettings.RuntimeProperty;

/*******************************************************************************
 * @brief Dialog for selecting the SPELL server connection to be used.
 * @date 18/09/07
 ******************************************************************************/
public class ConnectionDialog extends TitleAreaDialog implements SelectionListener, VerifyListener, ICoreServerOperationListener, ICoreContextOperationListener
{
	/** Holds the configuration manager handle */
	private static IConfigurationManager s_cfg = null;
	/** Holds the listener proxy handle */
	private static IServerProxy s_lstProxy = null;
	/** Holds the context proxy handle */
	private static IContextProxy s_ctxProxy = null;
	/** Button labels */
	private static final String BTN_CONNECT = "Connect";
	private static final String BTN_DISCONNECT = "Disconnect";
	private static final String BTN_START = "Start context";
	private static final String BTN_STOP = "Stop context";
	private static final String BTN_ATTACH = "Attach to context";
	private static final String BTN_DESTROY = "Destroy context";
	private static final String BTN_DETACH = "Detach from context";
	private static final String SRV_CONNECTED = "LISTENER CONNECTED: ";
	private static final String SRV_DISCONNECTED = "LISTENER DISCONNECTED";
	private static final String SRV_DISC_LOST = "LISTENER CONNECTION FAILURE";
	private static final String CTX_NONE = "NONE";
	private static final String CTX_NONE_LOST = "CONTEXT CONNECTION FAILURE";

	public static final String ID = "com.astra.ses.spell.gui.dialogs.ConnectionDialog";

	/** Runtime settings service */
	private IRuntimeSettings m_runtimeService = null;
	/** Holds the dialog image icon */
	private Image m_image;
	/** Holds the currently selected server */
	private int m_currentServer;
	/** Holds the list of server IDs */
	private Vector<ServerInfo> m_servers;
	/** Holds the combo data for server IDs */
	private Combo m_cmbServers;
	/** Holds the manual server host */
	private Text m_txtHost;
	/** hols the manual server port */
	private Text m_txtPort;
	/** Holds the table of contexts */
	private ContextTable m_contexts;
	/** Holds the connection status label */
	private Label m_lblConnection;
	/** Holds the connect button */
	private Button m_btnConnect;
	/** Holds the disconnect button */
	private Button m_btnDisconnect;
	/** Holds the start context button */
	private Button m_btnStartCtx;
	/** Holds the stop context button */
	private Button m_btnStopCtx;
	/** Holds the attach ctx button */
	private Button m_btnAttachCtx;
	/** Holds the detach ctx button */
	private Button m_btnDetachCtx;
	/** Holds the destroy ctx button */
	private Button m_btnDestroyCtx;
	/** Holds the current ctx label */
	private Label m_lblContext;
	/** Holds the spacecraft filter combo */
	private Combo m_cmbSpacecrafts;
	/** Holds the driver filter combo */
	private Combo m_cmbDrivers;
	/** Holds the filter controls */
	private Composite m_filterGroup;
	/** Predefined/manual connection tab folder */
	private TabFolder m_serversFolder;
	/** Tunneling button */
	private Button m_withAuthentication;
	/** Server user */
	private Text m_authUser;
	/** Server password */
	private Text m_authPwd;
	/** Server key */
	private Text m_authKey;
	/** Browse button for key */
	private Button m_browseKey;


	/***************************************************************************
	 * Constructor
	 * 
	 * @param shell
	 *            The parent shell
	 **************************************************************************/
	public ConnectionDialog(Shell shell)
	{
		super(shell);
		// Obtain the image for the dialog icon
		ImageDescriptor descr = Activator.getImageDescriptor("icons/network.gif");
		m_image = descr.createImage();
		m_currentServer = -1;
		m_runtimeService = (IRuntimeSettings) ServiceManager.get(IRuntimeSettings.class);
		// Obtain the manager handlers
		if (s_cfg == null)
		{
			s_cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		}
		if (s_lstProxy == null)
		{
			s_lstProxy = (IServerProxy) ServiceManager.get(IServerProxy.class);
		}
		if (s_ctxProxy == null)
		{
			s_ctxProxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		}
		GuiNotifications.get().addListener(this, ICoreServerOperationListener.class);
		GuiNotifications.get().addListener(this, ICoreContextOperationListener.class);
	}

	/***************************************************************************
	 * Called when the dialog is about to close.
	 * 
	 * @return The superclass return value.
	 **************************************************************************/
	public boolean close()
	{
		GuiNotifications.get().removeListener(this);
		m_image.dispose();
		return super.close();
	}

	/***************************************************************************
	 * Button callback
	 **************************************************************************/
	public void widgetDefaultSelected(SelectionEvent e)
	{
		widgetSelected(e);
	}

	/***************************************************************************
	 * Button callback
	 **************************************************************************/
	public void widgetSelected(SelectionEvent e)
	{
		if (e.widget instanceof Combo)
		{
			if (e.widget == m_cmbServers)
			{
				int fidx = m_cmbServers.getSelectionIndex();
				m_btnConnect.setEnabled(fidx != -1);
				if (fidx != -1)
				{
	            	Display.getCurrent().getActiveShell().setDefaultButton(m_btnConnect);
	            	m_btnConnect.setFocus();
				}
				else
				{
	            	Display.getCurrent().getActiveShell().setDefaultButton(getButton(IDialogConstants.CLOSE_ID));
	            	getButton(IDialogConstants.CLOSE_ID).setFocus();
				}
			}
			else if (e.widget == m_cmbSpacecrafts)
			{
				int fidx = m_cmbSpacecrafts.getSelectionIndex();
				if (fidx != -1)
				{
					String sc = m_cmbSpacecrafts.getItem(fidx);
					if (!sc.equals("(none)"))
					{
						m_contexts.addFilter(ContextTableColumn.SATELLITE.ordinal(), sc);
					}
					else
					{
						m_contexts.removeFilter(ContextTableColumn.SATELLITE.ordinal());
					}
				}
				else
				{
					m_contexts.removeFilter(ContextTableColumn.SATELLITE.ordinal());
				}
			}
			else if (e.widget == m_cmbDrivers)
			{
				int fidx = m_cmbDrivers.getSelectionIndex();
				if (fidx != -1)
				{
					String drv = m_cmbDrivers.getItem(fidx);
					if (!drv.equals("(none)"))
					{
						m_contexts.addFilter(ContextTableColumn.DRIVER.ordinal(), drv);
					}
					else
					{
						m_contexts.removeFilter(ContextTableColumn.DRIVER.ordinal());
					}
				}
				else
				{
					m_contexts.removeFilter(ContextTableColumn.DRIVER.ordinal());
				}
			}
			m_contexts.getTable().setFocus();
			m_contexts.getTable().deselectAll();
			updateAttachDetach(null);
			updateStartStop(null);
		}
		else if (e.widget instanceof Button)
		{
			Button b = (Button) e.widget;
			if (b == m_withAuthentication)
			{
				boolean selected = b.getSelection();
				m_authUser.setEnabled(selected);
				m_authPwd.setEnabled(selected);
				m_authKey.setEnabled(selected);
				m_browseKey.setEnabled(selected);
				updateServer(false);
			}
			else
			{
				String label = b.getText();
				if (label.equals(BTN_CONNECT))
				{
					connectToListener();
				}
				else if (label.equals(BTN_DISCONNECT))
				{
					disconnectFromListener();
				}
				else if (label.equals(BTN_START))
				{
					startSelectedContext();
				}
				else if (label.equals(BTN_STOP))
				{
					stopSelectedContext();
				}
				else if (label.equals(BTN_ATTACH))
				{
					attachSelectedContext();
				}
				else if (label.equals(BTN_DETACH))
				{
					detachCurrentContext();
				}
				else if (label.equals(BTN_DESTROY))
				{
					destroySelectedContext();
				}
				m_contexts.getTable().setFocus();
				m_contexts.getTable().deselectAll();
				updateAttachDetach(null);
				updateStartStop(null);
			}

		}
		else if (e.widget instanceof Table)
		{
			String selectedContext = m_contexts.getSelectedContext();
			if (selectedContext != null)
			{
				ContextInfo info = s_lstProxy.getContextInfo(selectedContext);
				if (info != null)
				{
					updateStartStop(info);
					updateAttachDetach(info);
				}
			}
		}
		else if (e.widget instanceof TabFolder)
		{
			updateServer(false);
		}
	}


	/***************************************************************************
	 * Creates the dialog contents.
	 * 
	 * @param parent
	 *            The base composite of the dialog
	 * @return The resulting contents
	 **************************************************************************/
	protected Control createContents(Composite parent)
	{
		Control contents = super.createContents(parent);
		setMessage("Setup server connection");
		setTitle("Connect to server");
		setTitleImage(m_image);
		getShell().setText("Connection Dialog");
		return contents;
	}

	/***************************************************************************
	 * Create the dialog area contents.
	 * 
	 * @param parent
	 *            The base composite of the dialog
	 * @return The resulting contents
	 **************************************************************************/
	protected Control createDialogArea(Composite parent)
	{
		// Main composite of the dialog area -----------------------------------
		Composite top = new Composite(parent, SWT.NONE);
		GridData areaData = new GridData(GridData.FILL_BOTH);
		top.setLayoutData(areaData);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		layout.numColumns = 1;
		top.setLayout(layout);

		createServerGroup(top);

		createContextGroup(top);

		return parent;
	}

	/***************************************************************************
	 * Create the button bar buttons.
	 * 
	 * @param parent
	 *            The Button Bar.
	 **************************************************************************/
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, true);
	}

	/***************************************************************************
	 * Called when one of the buttons of the button bar is pressed.
	 * 
	 * @param buttonId
	 *            The button identifier.
	 **************************************************************************/
	protected void buttonPressed(int buttonId)
	{
		switch (buttonId)
		{
		case IDialogConstants.CLOSE_ID:
			close();
		}
	}

	/***************************************************************************
	 * Control changes of host and port fields
	 **************************************************************************/
	public void verifyText(VerifyEvent e)
	{
		boolean ok = true;
		String id = (String) e.widget.getData("ID");
		if (id.equals("PORT"))
		{
			if ((e.keyCode != SWT.BS) && (e.keyCode != SWT.DEL))
			{
				try
				{
					Integer.parseInt(e.text);
				}
				catch (NumberFormatException ex)
				{
					ok = false;
				}
			}
		}
		e.doit = ok;
	}

	/***************************************************************************
	 * Callback called when a context is started
	 **************************************************************************/
	@Override
	public void notifyContextStarted(ContextInfo info)
	{
		Logger.debug("Context started: " + info.getName(), Level.GUI, this);
		m_contexts.updateContext(info);
	}

	/***************************************************************************
	 * Callback called when a context is stopped
	 **************************************************************************/
	@Override
	public void notifyContextStopped(ContextInfo info)
	{
		Logger.debug("Context stopped: " + info.getName(), Level.GUI, this);
		m_contexts.updateContext(info);
	}

	/***************************************************************************
	 * Callback called when the listener is connected
	 **************************************************************************/
	@Override
	public void notifyListenerConnected(ServerInfo info)
	{
		// Once executed, update the context table
		Logger.debug("Listener connected: " + info.getName(), Level.GUI, this);
		m_contexts.updateContexts(getCurrentContexts());
		m_contexts.selectContext(null);

		// Update the button status
		updateStartStop(null);
		updateAttachDetach(null);
		updateServer(false);
	}

	/***************************************************************************
	 * Callback called when the listener is connected
	 **************************************************************************/
	@Override
	public void notifyListenerDisconnected()
	{
		Logger.debug("Listener disconnected", Level.GUI, this);
		setNoListenerConnection(false);
	}

	/***************************************************************************
	 * Callback called when the listener connection is lost
	 **************************************************************************/
	@Override
	public void notifyListenerError(ErrorData error)
	{
		Logger.debug("Listener error", Level.GUI, this);
		setNoListenerConnection(true);
	}

	/***************************************************************************
	 * Callback called when a context is attached
	 **************************************************************************/
	@Override
	public void notifyContextAttached(ContextInfo ctx)
	{
		Logger.debug("Context attached", Level.GUI, this);
		m_lblContext.setText(ctx.getName());
		m_lblContext.setBackground(s_cfg.getStatusColor(ItemStatus.SUCCESS));
	}

	/***************************************************************************
	 * Callback called when a context is detached
	 **************************************************************************/
	@Override
	public void notifyContextDetached()
	{
		Logger.debug("Context detached", Level.GUI, this);
		m_lblContext.setText(CTX_NONE);
		m_lblContext.setBackground(s_cfg.getStatusColor(ItemStatus.WARNING));
	}

	/***************************************************************************
	 * Callback called when context connection is lost
	 **************************************************************************/
	@Override
	public void notifyContextError(ErrorData error)
	{
		Logger.debug("Context error", Level.GUI, this);
		if (!s_ctxProxy.isConnected())
		{
			m_lblContext.setText(CTX_NONE_LOST);
			m_lblContext.setBackground(s_cfg.getStatusColor(ItemStatus.ERROR));
		}
		ContextInfo info = s_lstProxy.getContextInfo(error.getOrigin());
		m_contexts.updateContext(info);
	}

	/***************************************************************************
	 * Dialog listener id
	 **************************************************************************/
	@Override
	public String getListenerId()
	{
		return ID;
	}

	/***************************************************************************
	 * Obtain the list of context info items
	 **************************************************************************/
	private Vector<ContextInfo> getCurrentContexts()
	{
		Vector<String> contexts = s_lstProxy.getAvailableContexts();
		Vector<ContextInfo> cinfo = new Vector<ContextInfo>();
		if (contexts != null)
		{
			Vector<String> sc = new Vector<String>();
			Vector<String> drv = new Vector<String>();
			for (String ctx : contexts)
			{
				ContextInfo info = s_lstProxy.getContextInfo(ctx);
				cinfo.addElement(info);
				if (!sc.contains(info.getSC()))
				{
					sc.add(info.getSC());
				}
				if (!drv.contains(info.getDriver()))
				{
					drv.add(info.getDriver());
				}
			}
			updateSpacecrafts(sc);
			updateDrivers(drv);
			m_filterGroup.pack();
		}
		return cinfo;
	}

	/***************************************************************************
	 * Update controls depending on listener connection
	 **************************************************************************/
	private void setNoListenerConnection(boolean connectionLost)
	{
		ContextInfo ctx = s_ctxProxy.getInfo();
		if (ctx != null)
		{
			Vector<ContextInfo> list = new Vector<ContextInfo>();
			list.add(ctx);
			m_contexts.updateContexts(list);
		}
		else
		{
			m_contexts.updateContexts(null);
		}
		updateStartStop(null);
		updateAttachDetach(null);
		updateServer(connectionLost);
		updateSpacecrafts(null);
		updateDrivers(null);
		m_filterGroup.pack();
	}

	/***************************************************************************
	 * Create the controls for the server management part
	 * 
	 * @param parent
	 **************************************************************************/
	private void createServerGroup(Composite parent)
	{
		/*
		 * Read last connection settings
		 */
		String lastAutoServer = s_cfg.getProperty(PropertyKey.LAST_SERVER_CONNECTED);
		String lastManualServer = s_cfg.getProperty(PropertyKey.LAST_HOST_CONNECTED);
		String lastManualPort = s_cfg.getProperty(PropertyKey.LAST_PORT_CONNECTED);

		// ----------------------------------------------------------------------
		// Base group
		// ----------------------------------------------------------------------
		Group serverGroup = new Group(parent, SWT.BORDER);
		serverGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout slayout = new GridLayout(1, true);
		serverGroup.setLayout(slayout);
		serverGroup.setText("Server selection");

		// ----------------------------------------------------------------------
		// Tab folder
		// ----------------------------------------------------------------------
		m_serversFolder = new TabFolder(serverGroup, SWT.TOP);
		m_serversFolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// ----------------------------------------------------------------------
		// Predefined servers tab
		// ----------------------------------------------------------------------
		TabItem predefinedTab = new TabItem(m_serversFolder, SWT.NONE);
		predefinedTab.setData("MODE", "predefined");
		predefinedTab.setText("Predefined servers");

		// Widgets
		Composite predefinedWidgets = new Composite(m_serversFolder, SWT.NONE);
		predefinedWidgets.setLayout(new GridLayout(2, false));
		predefinedWidgets.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label serversLabel = new Label(predefinedWidgets, SWT.NONE);
		serversLabel.setText("Predefined Servers:");

		m_cmbServers = new Combo(predefinedWidgets, SWT.NONE);
		m_cmbServers.addSelectionListener(this);
		GridData d2 = new GridData();
		d2.horizontalAlignment = GridData.BEGINNING;
		d2.widthHint = 150;
		m_cmbServers.setLayoutData(d2);

		Label descLabel = new Label(predefinedWidgets, SWT.NONE);
		descLabel.setText("Servers information can be modified at " + "Servers page in the preferences dialog");
		GridData descData = new GridData(GridData.FILL_BOTH);
		descData.horizontalSpan = 2;
		descData.verticalAlignment = SWT.BOTTOM;
		descLabel.setLayoutData(descData);

		// set control
		predefinedTab.setControl(predefinedWidgets);

		// ----------------------------------------------------------------------
		// Automatic connection tab
		// ----------------------------------------------------------------------
		TabItem manualTab = new TabItem(m_serversFolder, SWT.NONE);
		manualTab.setData("MODE", "manual");
		manualTab.setText("Manual connection");

		// widgets
		Composite manualWidgets = new Composite(m_serversFolder, SWT.NONE);
		manualWidgets.setLayout(new GridLayout(4, false));
		manualWidgets.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label hostLabel = new Label(manualWidgets, SWT.NONE);
		hostLabel.setText("Host:");

		m_txtHost = new Text(manualWidgets, SWT.BORDER);
		GridData st1D = new GridData();
		st1D.widthHint = 180;
		st1D.horizontalAlignment = GridData.FILL;
		m_txtHost.setLayoutData(st1D);
		m_txtHost.addVerifyListener(this);
		m_txtHost.setData("ID", "HOST");
		m_txtHost.setText(lastManualServer);
		m_txtHost.addModifyListener( new ModifyListener(){

			@Override
            public void modifyText(ModifyEvent e)
            {
	            if (!m_txtHost.getText().isEmpty() || !m_txtPort.getText().isEmpty())
	            {
	            	Display.getCurrent().getActiveShell().setDefaultButton(m_btnConnect);
	            }
	            else
	            {
	            	Display.getCurrent().getActiveShell().setDefaultButton(getButton(IDialogConstants.CLOSE_ID));
	            }
            }
			
			
		});

		Label portLabel = new Label(manualWidgets, SWT.NONE);
		portLabel.setText("Port:");

		m_txtPort = new Text(manualWidgets, SWT.BORDER);
		GridData st2D = new GridData();
		st2D.widthHint = 50;
		m_txtPort.setLayoutData(st2D);
		m_txtPort.addVerifyListener(this);
		m_txtPort.setData("ID", "PORT");
		m_txtPort.setText(lastManualPort);
		m_txtPort.addModifyListener( new ModifyListener(){

			@Override
            public void modifyText(ModifyEvent e)
            {
	            if (!m_txtHost.getText().isEmpty() || !m_txtPort.getText().isEmpty())
	            {
	            	Display.getCurrent().getActiveShell().setDefaultButton(m_btnConnect);
	            }
	            else
	            {
	            	Display.getCurrent().getActiveShell().setDefaultButton(getButton(IDialogConstants.CLOSE_ID));
	            }
            }
			
			
		});

		m_withAuthentication = new Button(manualWidgets, SWT.CHECK);
		m_withAuthentication.setText("Requires user authentication");
		GridData tunnelData = new GridData();
		tunnelData.horizontalSpan = 4;
		m_withAuthentication.setLayoutData(tunnelData);

		Label authUsrLabel = new Label(manualWidgets, SWT.NONE);
		authUsrLabel.setText("Username");

		m_authUser = new Text(manualWidgets, SWT.BORDER);
		m_authUser.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		m_authUser.setEnabled(m_withAuthentication.getSelection());

		Label authPwdLabel = new Label(manualWidgets, SWT.NONE);
		authPwdLabel.setText("Password");

		m_authPwd = new Text(manualWidgets, SWT.BORDER | SWT.PASSWORD);
		m_authPwd.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		m_authPwd.setEnabled(m_withAuthentication.getSelection());

		Label authKeyLabel = new Label(manualWidgets, SWT.NONE);
		authKeyLabel.setText("Key file");

		Composite two = new Composite(manualWidgets, SWT.NONE);
		two.setLayout( new GridLayout(2,false));
		GridData tData = new GridData( GridData.FILL_HORIZONTAL );
		tData.horizontalSpan = 3;
		two.setLayoutData( tData );
		
		m_authKey = new Text(two, SWT.BORDER);
		m_authKey.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		m_authKey.setEnabled(m_withAuthentication.getSelection());

		// Browse
		m_browseKey = new Button(two,SWT.PUSH);
		m_browseKey.setText("Browse");
		m_browseKey.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected( SelectionEvent event )
			{
				FileDialog dialog = new FileDialog(ConnectionDialog.this.getShell(),SWT.OPEN);
				dialog.setText("Select a SSH private key file");
				String file = dialog.open();
				if (file != null && !file.trim().isEmpty())
				{
					m_authKey.setText(file);
				}
			}
		});
		m_browseKey.setEnabled(m_withAuthentication.getSelection());

		m_withAuthentication.addSelectionListener(this);

		// set control
		manualTab.setControl(manualWidgets);

		// ----------------------------------------------------------------------
		// default tab
		// ----------------------------------------------------------------------
		m_serversFolder.setSelection(0);

		// ----------------------------------------------------------------------
		// Listener for updating connect/disconnect buttons when switchign tabs
		// ----------------------------------------------------------------------
		m_serversFolder.addSelectionListener(this);

		// ----------------------------------------------------------------------
		// Manual connection widgets changes listener
		// ----------------------------------------------------------------------
		ModifyListener manualModificationListener = new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				boolean ready = isManualConnectionReady();
				m_btnConnect.setEnabled(ready);
			}
		};
		m_txtHost.addModifyListener(manualModificationListener);
		m_txtPort.addModifyListener(manualModificationListener);
		m_authUser.addModifyListener(manualModificationListener);
		m_authPwd.addModifyListener(manualModificationListener);
		m_authKey.addModifyListener(manualModificationListener);

		// ----------------------------------------------------------------------
		// Composite for combo and manual selection
		// ----------------------------------------------------------------------
		Composite c1 = new Composite(serverGroup, SWT.NONE);
		GridLayout glayout1 = new GridLayout();
		glayout1.marginHeight = 0;
		glayout1.marginWidth = 0;
		glayout1.marginTop = 0;
		glayout1.marginBottom = 0;
		glayout1.marginLeft = 0;
		glayout1.marginRight = 0;
		glayout1.numColumns = 2;
		c1.setLayout(glayout1);
		GridData c1d = new GridData(GridData.FILL_HORIZONTAL);
		c1.setLayoutData(c1d);

		// ----------------------------------------------------------------------
		// Manual server controls
		// ----------------------------------------------------------------------
		Composite tc = new Composite(c1, SWT.NONE);
		GridLayout tcLayout = new GridLayout();
		tcLayout.numColumns = 3;
		tcLayout.marginHeight = 1;
		tcLayout.marginWidth = 1;
		tcLayout.marginTop = 1;
		tcLayout.marginBottom = 1;
		tcLayout.marginLeft = 0;
		tcLayout.marginRight = 0;
		tc.setLayout(tcLayout);
		tc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// ----------------------------------------------------------------------
		// Connection status label
		// ----------------------------------------------------------------------
		m_lblConnection = new Label(c1, SWT.BORDER);
		m_lblConnection.setText(SRV_DISCONNECTED);
		m_lblConnection.setAlignment(SWT.CENTER);
		m_lblConnection.setFont(s_cfg.getFont(FontKey.GUI_BOLD));
		GridData std = new GridData();
		std.horizontalAlignment = GridData.FILL;
		std.horizontalSpan = 2;
		std.heightHint = 15;
		std.verticalIndent = 10;
		m_lblConnection.setLayoutData(std);

		// ----------------------------------------------------------------------
		// Connection buttons
		// ----------------------------------------------------------------------
		Composite c2 = new Composite(serverGroup, SWT.NONE);
		GridLayout glayout2 = new GridLayout();
		glayout2.marginHeight = 0;
		glayout2.marginWidth = 0;
		glayout2.marginTop = 10;
		glayout2.marginBottom = 0;
		glayout2.marginLeft = 0;
		glayout2.marginRight = 0;
		glayout2.numColumns = 2;
		c2.setLayout(glayout2);
		GridData c2d = new GridData(GridData.FILL_HORIZONTAL);
		c2.setLayoutData(c2d);

		m_btnConnect = new Button(c2, SWT.PUSH);
		m_btnConnect.setText(BTN_CONNECT);
		m_btnConnect.addSelectionListener(this);
		GridData b1d = new GridData(GridData.FILL_HORIZONTAL);
		m_btnConnect.setLayoutData(b1d);

		m_btnDisconnect = new Button(c2, SWT.PUSH);
		m_btnDisconnect.setText(BTN_DISCONNECT);
		m_btnDisconnect.addSelectionListener(this);
		GridData b2d = new GridData(GridData.FILL_HORIZONTAL);
		m_btnDisconnect.setLayoutData(b2d);

		// ----------------------------------------------------------------------
		// Get the available servers
		// ----------------------------------------------------------------------
		String[] serverIDs = s_cfg.getAvailableServers();
		Logger.debug("Available servers: " + serverIDs.length, Level.GUI, this);
		m_servers = new Vector<ServerInfo>();
		Arrays.sort(serverIDs);
		for (String server : serverIDs)
		{
			ServerInfo info = s_cfg.getServerData(server);
			m_servers.add(info);
			m_cmbServers.add(info.getName());
		}
		String initial = s_cfg.getProperty(PropertyKey.INITIAL_SERVER);
		if (!lastAutoServer.isEmpty())
		{
			initial = lastAutoServer;
		}
		if ((initial != null) && (initial.length() > 0))
		{
			// Look for the initial server in the combo
			int idx = -1;
			int current = 0;
			for (String serverIf : serverIDs)
			{
				if (serverIf.equals(initial))
				{
					idx = current;
					break;
				}
				current++;
			}
			m_cmbServers.select(idx);
			m_currentServer = idx;
		}
		updateServer(false);
	}

	/***************************************************************************
	 * Create the context information group
	 **************************************************************************/
	private void createContextGroup(Composite parent)
	{
		Group contextGroup = new Group(parent, SWT.NONE);
		GridData cld = new GridData(GridData.FILL_HORIZONTAL);
		cld.heightHint = 220;
		contextGroup.setLayoutData(cld);
		contextGroup.setText("Available Contexts");
		GridLayout clayout = new GridLayout();
		clayout.marginHeight = 2;
		clayout.marginWidth = 2;
		clayout.marginTop = 2;
		clayout.marginBottom = 2;
		clayout.marginLeft = 2;
		clayout.marginRight = 2;
		clayout.numColumns = 1;
		contextGroup.setLayout(clayout);

		m_filterGroup = new Composite(contextGroup, SWT.NONE);
		GridData fdata = new GridData();
		fdata.grabExcessHorizontalSpace = true;
		fdata.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
		m_filterGroup.setLayoutData(fdata);
		m_filterGroup.setLayout(new RowLayout());
		Label flabel = new Label(m_filterGroup, SWT.NONE);
		flabel.setText("Filter by spacecraft:");
		m_cmbSpacecrafts = new Combo(m_filterGroup, SWT.SINGLE | SWT.READ_ONLY);
		m_cmbSpacecrafts.addSelectionListener(this);
		m_cmbSpacecrafts.setEnabled(false);
		m_cmbSpacecrafts.setData("ID", "SC");
		m_cmbSpacecrafts.add("(none)");
		m_cmbSpacecrafts.select(0);
		Label f2label = new Label(m_filterGroup, SWT.NONE);
		f2label.setText("Filter by driver:");
		m_cmbDrivers = new Combo(m_filterGroup, SWT.SINGLE | SWT.READ_ONLY);
		m_cmbDrivers.addSelectionListener(this);
		m_cmbDrivers.setEnabled(false);
		m_cmbDrivers.setData("ID", "DRV");
		m_cmbDrivers.add("(none)");
		m_cmbDrivers.select(0);

		m_contexts = new ContextTable(contextGroup, this);
		GridData tableLayoutData = new GridData(GridData.FILL_BOTH);
		tableLayoutData.grabExcessHorizontalSpace = true;
		tableLayoutData.widthHint = m_contexts.getTableWidthHint();
		tableLayoutData.heightHint = m_contexts.getTableHeightHint();
		m_contexts.getTable().setLayoutData(tableLayoutData);

		Composite buttonBar = new Composite(contextGroup, SWT.BORDER);
		buttonBar.setLayout(new FillLayout(SWT.HORIZONTAL));
		buttonBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		m_btnStartCtx = new Button(buttonBar, SWT.PUSH);
		m_btnStartCtx.setText(BTN_START);
		m_btnStartCtx.addSelectionListener(this);
		m_btnStopCtx = new Button(buttonBar, SWT.PUSH);
		m_btnStopCtx.setText(BTN_STOP);
		m_btnStopCtx.addSelectionListener(this);
		m_btnAttachCtx = new Button(buttonBar, SWT.PUSH);
		m_btnAttachCtx.setText(BTN_ATTACH);
		m_btnAttachCtx.addSelectionListener(this);

		Composite contextBar = new Composite(contextGroup, SWT.BORDER);
		GridLayout cbl = new GridLayout(4, false);
		contextBar.setLayout(cbl);
		GridData cbd = new GridData(GridData.FILL_HORIZONTAL);
		// cbd.heightHint = 25;
		contextBar.setLayoutData(cbd);

		Label l = new Label(contextBar, SWT.NONE);
		l.setText("Current context: ");
		l.setFont(s_cfg.getFont(FontKey.GUI_BOLD));

		m_lblContext = new Label(contextBar, SWT.BORDER);
		GridData ldata = new GridData(GridData.FILL_HORIZONTAL);
		ldata.grabExcessHorizontalSpace = true;
		ldata.heightHint = 15;
		m_lblContext.setLayoutData(ldata);
		m_lblContext.setAlignment(SWT.CENTER);

		m_btnDetachCtx = new Button(contextBar, SWT.PUSH);
		m_btnDetachCtx.setText(BTN_DETACH);
		m_btnDetachCtx.addSelectionListener(this);
		GridData bdata = new GridData(GridData.END);
		m_btnDetachCtx.setLayoutData(bdata);

		m_btnDestroyCtx = new Button(contextBar, SWT.PUSH);
		m_btnDestroyCtx.setText(BTN_DESTROY);
		m_btnDestroyCtx.addSelectionListener(this);
		GridData b2data = new GridData(GridData.END);
		m_btnDestroyCtx.setLayoutData(b2data);
		m_btnDestroyCtx.setEnabled(false);

		updateContexts();
		m_contexts.getTable().setFocus();
	}

	/***************************************************************************
	 * Update the attach/detach control buttons
	 **************************************************************************/
	private void updateAttachDetach(ContextInfo info)
	{
		boolean listenerConnected = s_lstProxy.isConnected();
		boolean contextConnected = s_ctxProxy.isConnected();
		boolean contextRunning = false;
		if (info != null)
			contextRunning = info.isRunning();

		// We can attach when:
		// 1. Listener is connected
		// 2. There is a context selected
		// 3. There is no current context connected
		// 4. The context is started
		m_btnAttachCtx.setEnabled(listenerConnected && (!contextConnected) && (info != null) && info.isRunning());

		// We can detach when:
		// 1. Listener is connected
		// 2. There is a context connected
		m_btnDetachCtx.setEnabled(listenerConnected && contextConnected);
		String selected = m_contexts.getSelectedContext();
		m_btnDestroyCtx.setEnabled(contextRunning && listenerConnected && (selected != null));
	}

	/***************************************************************************
	 * Update the start/stop context buttons
	 **************************************************************************/
	private void updateStartStop(ContextInfo info)
	{
		// We can stop the context if:
		// 1 - Context information is available (there is a context selected)
		// 2 - Listener is connected
		// 3 - We can stop/destroy if context is running AND we are not attached
		// to it
		// 4 - We can start if context is not running
		if ((info != null) && (s_lstProxy.isConnected()))
		{
			boolean running = info.isRunning();
			boolean error = info.getStatus().equals(ContextStatus.ERROR);
			m_btnStartCtx.setEnabled(!running && !error);
			m_btnDestroyCtx.setEnabled(running);
			if (s_ctxProxy.isConnected())
			{
				boolean myContext = s_ctxProxy.getCurrentContext().equals(info.getName());
				m_btnStopCtx.setEnabled(running && (!myContext));
			}
			else
			{
				m_btnStopCtx.setEnabled(running);
			}
		}
		else
		{
			m_btnStartCtx.setEnabled(false);
			m_btnStopCtx.setEnabled(false);
			m_btnDestroyCtx.setEnabled(false);
		}
	}

	/***************************************************************************
	 * Update the server controls
	 **************************************************************************/
	private void updateServer(boolean connectionLost)
	{
		if (s_lstProxy.isConnected())
		{
			String serverID = s_lstProxy.getCurrentServerID();
			ServerInfo info = null;
			if (serverID != null)
			{
				info = s_cfg.getServerData(serverID);
			}
			String name = "MANUAL";
			if (info != null)
			{
				name = info.getName();
			}
			m_lblConnection.setText(SRV_CONNECTED + name);
			m_lblConnection.setBackground(s_cfg.getStatusColor(ItemStatus.SUCCESS));
			// We cannot disconnect if there is a context connected
			m_btnDisconnect.setEnabled(!s_ctxProxy.isConnected());
			m_btnConnect.setEnabled(false);
			m_serversFolder.setEnabled(false);
		}
		else
		{
			m_serversFolder.setEnabled(true);
			boolean manual = m_serversFolder.getSelectionIndex() == 1;
			boolean connectEnabled = m_cmbServers.getSelectionIndex() != -1;
			if (manual)
			{
				connectEnabled = isManualConnectionReady();
			}
			m_btnConnect.setEnabled(connectEnabled);
			m_btnDisconnect.setEnabled(false);

			if (connectionLost)
			{
				m_lblConnection.setText(SRV_DISC_LOST);
				m_lblConnection.setBackground(s_cfg.getStatusColor(ItemStatus.ERROR));
			}
			else
			{
				m_lblConnection.setText(SRV_DISCONNECTED);
				m_lblConnection.setBackground(s_cfg.getStatusColor(ItemStatus.WARNING));
			}
			m_cmbServers.select(m_currentServer);
		}
	}

	/***************************************************************************
	 * Update the context table
	 **************************************************************************/
	private void updateContexts()
	{
		Logger.debug("Updating context table", Level.GUI, this);
		String currentCtx = s_ctxProxy.getCurrentContext();
		String server = s_lstProxy.getCurrentServerID();
		if (server != null)
		{
			Logger.debug("Connected to a server, getting context list", Level.GUI, this);
			m_contexts.updateContexts(getCurrentContexts());
			if (currentCtx != null)
			{
				Logger.debug("Current context is " + currentCtx, Level.GUI, this);
				m_contexts.selectContext(currentCtx);
			}
		}
		else
		{
			Logger.debug("Not connected", Level.GUI, this);
		}
		m_contexts.selectContext(null);
		updateStartStop(null);
		updateAttachDetach(null);
		if (currentCtx != null)
		{
			m_lblContext.setText(currentCtx);
			m_lblContext.setBackground(s_cfg.getStatusColor(ItemStatus.SUCCESS));
		}
		else
		{
			m_lblContext.setText(CTX_NONE);
			m_lblContext.setBackground(s_cfg.getStatusColor(ItemStatus.WARNING));
		}
	}

	/***************************************************************************
	 * Update the S/C filter
	 **************************************************************************/
	private void updateSpacecrafts(Vector<String> scnames)
	{
		m_cmbSpacecrafts.removeAll();
		m_cmbSpacecrafts.add("(none)");
		if (scnames == null || scnames.size() == 0)
		{
			m_cmbSpacecrafts.setEnabled(false);
		}
		else
		{
			for (String scname : scnames)
			{
				m_cmbSpacecrafts.add(scname);
			}
			m_cmbSpacecrafts.setEnabled(true);
		}
		m_cmbSpacecrafts.select(0);
	}

	/***************************************************************************
	 * Update the driver filter
	 **************************************************************************/
	private void updateDrivers(Vector<String> drnames)
	{
		m_cmbDrivers.removeAll();
		m_cmbDrivers.add("(none)");
		if (drnames == null || drnames.size() == 0)
		{
			m_cmbDrivers.setEnabled(false);
		}
		else
		{
			for (String dname : drnames)
			{
				m_cmbDrivers.add(dname);
			}
			m_cmbDrivers.setEnabled(true);
		}
		m_cmbDrivers.select(0);
	}

	/***************************************************************************
	 * Connect listener proxy
	 **************************************************************************/
	private void connectToListener()
	{
		Logger.debug("Connecting to listener", Level.GUI, this);
		ServerInfo server = null;
		int idx = m_cmbServers.getSelectionIndex();
		String autoServer = "";
		String manualServer = m_txtHost.getText();
		String manualPort = m_txtPort.getText();
		boolean manual = m_serversFolder.getSelectionIndex() == 1;
		// Connection selecting a predefined server
		if ((idx != -1) && !manual)
		{
			server = m_servers.get(idx);
			autoServer = server.getName();
			Logger.debug("Using server " + server.getName(), Level.GUI, this);
		}
		// Connection with manual settings
		else if (manual)
		{
			server = new ServerInfo();
			server.setHost(manualServer);
			server.setPort(Integer.parseInt(manualPort));
			server.setName(manualServer + ":" + manualPort);
			if (m_withAuthentication.getSelection())
			{
				AuthenticationData auth = new AuthenticationData(m_authUser.getText(),m_authPwd.getText(),m_authKey.getText());
				server.setAuthentication(auth);
			}
			Logger.debug("Using manual server", Level.GUI, this);
		}

		// Set the current server selection
		m_runtimeService.setRuntimeProperty(RuntimeProperty.ID_SERVER_SELECTION, server);
		CommandResult result = CommandHelper.execute(ConnectServer.ID);
		m_runtimeService.setRuntimeProperty(RuntimeProperty.ID_SERVER_SELECTION, null);
		/*
		 * If command execution was successful, then store the values in the
		 * preferences store
		 */
		if (result.equals(CommandResult.SUCCESS))
		{
			s_cfg.setProperty(PropertyKey.LAST_SERVER_CONNECTED, autoServer);
			s_cfg.setProperty(PropertyKey.LAST_HOST_CONNECTED, manualServer);
			s_cfg.setProperty(PropertyKey.LAST_PORT_CONNECTED, manualPort);
			s_cfg.setBooleanProperty(PropertyKey.LAST_CONNECTION_MANUAL, manual);
		}
	}

	/***************************************************************************
	 * Disconnect from listener process
	 **************************************************************************/
	private void disconnectFromListener()
	{
		Logger.debug("Disconnecting from listener", Level.GUI, this);
		CommandHelper.execute(DisconnectServer.ID);
	}

	/***************************************************************************
	 * Start the given context
	 **************************************************************************/
	private void startSelectedContext()
	{
		String ctxName = m_contexts.getSelectedContext();
		ContextInfo info = s_lstProxy.getContextInfo(ctxName);
		m_runtimeService.setRuntimeProperty(RuntimeProperty.ID_CONTEXT_SELECTION, info);
		CommandHelper.execute(StartContext.ID);
	}

	/***************************************************************************
	 * Stop the given context
	 **************************************************************************/
	private void stopSelectedContext()
	{
		String ctxName = m_contexts.getSelectedContext();
		ContextInfo info = s_lstProxy.getContextInfo(ctxName);
		m_runtimeService.setRuntimeProperty(RuntimeProperty.ID_CONTEXT_SELECTION, info);
		CommandHelper.execute(StopContext.ID);
	}

	/***************************************************************************
	 * Attach to the given context
	 **************************************************************************/
	private void attachSelectedContext()
	{
		String ctxName = m_contexts.getSelectedContext();
		ContextInfo info = s_lstProxy.getContextInfo(ctxName);
		m_runtimeService.setRuntimeProperty(RuntimeProperty.ID_CONTEXT_SELECTION, info);
		CommandHelper.execute(AttachContext.ID);
		updateServer(false);
	}

	/***************************************************************************
	 * Detach from the current context
	 **************************************************************************/
	private void detachCurrentContext()
	{
		String ctxName = s_ctxProxy.getCurrentContext();
		ContextInfo info = s_lstProxy.getContextInfo(ctxName);
		m_runtimeService.setRuntimeProperty(RuntimeProperty.ID_CONTEXT_SELECTION, info);
		CommandHelper.execute(DetachContext.ID);
		updateServer(false);
	}

	/***************************************************************************
	 * Destroy the selected context
	 **************************************************************************/
	private void destroySelectedContext()
	{
		String ctxName = m_contexts.getSelectedContext();
		if (ctxName != null)
		{
			boolean proceed = MessageDialog.openConfirm(this.getShell(), "Destroy context",
			        "WARNING: destroying a context will kill ALL its associated executors. Proceed?");
			if (proceed)
			{
				ContextInfo info = s_lstProxy.getContextInfo(ctxName);
				m_runtimeService.setRuntimeProperty(RuntimeProperty.ID_CONTEXT_SELECTION, info);
				CommandHelper.execute(DestroyContext.ID);
				updateServer(false);
			}
		}
	}

	/****************************************************************************
	 * Check if all the information for establishing a manual connection is
	 * entered in the dialog
	 * 
	 * @return
	 ***************************************************************************/
	private boolean isManualConnectionReady()
	{
		boolean result = true;

		if (m_txtHost.getText().isEmpty() || m_txtPort.getText().isEmpty())
		{
			result = false;
		}
		else
		{
			if (m_withAuthentication.getSelection())
			{
				if (m_authUser.getText().trim().isEmpty() || (m_authPwd.getText().trim().isEmpty() && m_authKey.getText().trim().isEmpty()))
				{
					result = false;
				}
			}
		}

		return result;
	}
}
