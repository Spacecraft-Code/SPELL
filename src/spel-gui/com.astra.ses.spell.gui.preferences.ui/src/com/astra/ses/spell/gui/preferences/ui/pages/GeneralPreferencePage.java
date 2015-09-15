///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.preferences.ui.pages
// 
// FILE      : GeneralPreferencePage.java
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

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.astra.ses.spell.gui.core.model.server.AuthenticationData;
import com.astra.ses.spell.gui.preferences.Activator;
import com.astra.ses.spell.gui.preferences.initializer.GUIPreferencesLoader;
import com.astra.ses.spell.gui.preferences.initializer.GUIPreferencesSaver;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.PropertyKey;
import com.astra.ses.spell.gui.preferences.values.AutoClosePref;
import com.astra.ses.spell.gui.preferences.values.YesNoPromptPref;

public class GeneralPreferencePage extends BasicPreferencesPage
{

	/** Connect at startup */
	private Button	m_startupConnect;
	/** Auto close */
	private Combo   m_autoClose;
	/** Authentication user */
	private Text	m_authUser;
	/** Authentication pwd */
	private Text	m_authPassword;
	/** Authentication key */
	private Text	m_authKey;
	/** Response timeout */
	private Text	m_responseTimeout;
	/** Open timeout */
	private Text	m_openTimeout;
	/** Prompt sound file */
	private Text	m_soundFile;
	/** Confirm abort */
	private Button  m_confirmAbort;
	/** Prompt for multiple attach */
	private Combo  m_multiAttach;
	/** Prompt for ASRUN when controlling */
	private Combo  m_asrunControl;
	/** Prompt for ASRUN when monitoring */
	private Combo  m_asrunMonitor;

	@Override
	protected Control createContents(Composite parent)
	{
		// CONTAINER
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		container.setLayout(layout);
		container.setLayoutData(layoutData);

		/* Page layout */
		GridData expand = new GridData(GridData.FILL_HORIZONTAL
		        | GridData.VERTICAL_ALIGN_BEGINNING);
		expand.grabExcessVerticalSpace = true;
		expand.grabExcessHorizontalSpace = true;

		/*
		 * Startup connection button
		 */
		Composite startupConnection = new Composite(container, SWT.NONE);
		startupConnection.setLayout(new GridLayout(2, false));
		startupConnection.setLayoutData(GridDataFactory.copyData(expand));
		// Label
		Label startupDesc = new Label(startupConnection, SWT.NONE);
		startupDesc.setText("Connect to server at startup");
		// Button
		m_startupConnect = new Button(startupConnection, SWT.CHECK);

		// Label
		Label authUser = new Label(startupConnection, SWT.NONE);
		authUser.setText("User for server authentication");
		// Text
		m_authUser = new Text(startupConnection, SWT.BORDER);
		m_authUser.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));

		// Label
		Label authPwd = new Label(startupConnection, SWT.NONE);
		authPwd.setText("Password for server authentication");
		// Text
		m_authPassword = new Text(startupConnection, SWT.BORDER | SWT.PASSWORD );
		m_authPassword.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));

		// Label
		Label authKey = new Label(startupConnection, SWT.NONE);
		authKey.setText("Key file for server authentication");

		Composite two = new Composite(startupConnection,SWT.NONE);
		two.setLayout( new GridLayout(2,false) );
		// Text
		m_authKey = new Text(two, SWT.BORDER );
		m_authKey.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));
		// Browse
		Button browse = new Button(two,SWT.PUSH);
		browse.setText("Browse");
		browse.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected( SelectionEvent event )
			{
				FileDialog dialog = new FileDialog(GeneralPreferencePage.this.getShell(),SWT.OPEN);
				dialog.setText("Select a SSH private key file");
				String file = dialog.open();
				if (file != null && !file.trim().isEmpty())
				{
					m_authKey.setText(file);
				}
			}
		});
		
		/*
		 * Communication group
		 */
		GridLayout communicationLayout = new GridLayout(3, false);
		Group communicationGroup = new Group(container, SWT.BORDER);
		communicationGroup.setText("Communication");
		communicationGroup.setLayout(communicationLayout);
		communicationGroup.setLayoutData(GridDataFactory.copyData(expand));
		/*
		 * Response timeout
		 */
		// Label
		Label responseTimeout = new Label(communicationGroup, SWT.NONE);
		responseTimeout.setText("Response timeout");
		// Text
		m_responseTimeout = new Text(communicationGroup, SWT.BORDER | SWT.RIGHT);
		m_responseTimeout.setLayoutData(GridDataFactory.copyData(expand));
		// Units label
		Label units = new Label(communicationGroup, SWT.NONE);
		units.setText("milliseconds");
		/*
		 * Procedure opening timeout
		 */
		// Label
		Label procedureOpenLabel = new Label(communicationGroup, SWT.NONE);
		procedureOpenLabel.setText("Procedure opening timeout");
		// Text
		m_openTimeout = new Text(communicationGroup, SWT.BORDER | SWT.RIGHT);
		m_openTimeout.setLayoutData(GridDataFactory.copyData(expand));
		// Label
		Label openUnits = new Label(communicationGroup, SWT.NONE);
		openUnits.setText("milliseconds");

		/*
		 * User group
		 */
		GridLayout userLayout = new GridLayout(3, true);
		Group userGroup = new Group(container, SWT.BORDER);
		userGroup.setText("User settings");
		userGroup.setLayout(userLayout);
		userGroup.setLayoutData(GridDataFactory.copyData(expand));

		/*
		 * Prompt sound file
		 */
		// Label
		Label promptSoundLabel = new Label(userGroup, SWT.NONE);
		promptSoundLabel.setText("Prompt sound file");
		m_soundFile = new Text(userGroup, SWT.BORDER);
		m_soundFile.setLayoutData(GridDataFactory.copyData(expand));
		// Browse button
		final Button browse2 = new Button(userGroup, SWT.PUSH);
		browse2.setText("Browse...");
		browse2.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent arg0)
			{
				FileDialog dialog = new FileDialog(browse2.getShell());
				dialog.setFilterExtensions(new String[] { "*.wav" });
				dialog.setText("Select prompt sound file");
				dialog.setFilterNames(new String[] { "Waveform files (*.wav)" });
				String selected = dialog.open();
				if (selected != null)
				{
					m_soundFile.setText(selected);
				}
			}
		});
		
		/** Automatic close of procedures */
		Label autoCloseLabel = new Label(userGroup, SWT.NONE);
		autoCloseLabel.setText("Close finished procedures");
		// Browse button
		m_autoClose = new Combo(userGroup, SWT.READ_ONLY);
		for(AutoClosePref ac : AutoClosePref.values()) m_autoClose.add(ac.title);
		new Label(userGroup, SWT.NONE);

		/** Confirm abort */
		Label confirmAbortLabel = new Label(userGroup, SWT.NONE);
		confirmAbortLabel.setText("Confirm abort");
		// Browse button
		m_confirmAbort = new Button(userGroup, SWT.CHECK);
		m_confirmAbort.setText("(show confirmation dialog before aborting execution)");
		new Label(userGroup, SWT.NONE);

		/** Multiple attach */
		Label multiAttachLabel = new Label(userGroup, SWT.NONE);
		multiAttachLabel.setText("Auto attach to related procedures");
		m_multiAttach = new Combo(userGroup, SWT.READ_ONLY);
		for(YesNoPromptPref ynp : YesNoPromptPref.values()) m_multiAttach.add(ynp.title);
		new Label(userGroup, SWT.NONE);

		/** AsRun control */
		Label arcLabel = new Label(userGroup, SWT.NONE);
		arcLabel.setText("Use ASRUN when controlling");
		m_asrunControl = new Combo(userGroup, SWT.READ_ONLY);
		for(YesNoPromptPref ynp : YesNoPromptPref.values()) m_asrunControl.add(ynp.title);
		new Label(userGroup, SWT.NONE);

		/** AsRun monitor */
		Label armLabel = new Label(userGroup, SWT.NONE);
		armLabel.setText("Use ASRUN when monitoring");
		m_asrunMonitor = new Combo(userGroup, SWT.READ_ONLY);
		for(YesNoPromptPref ynp : YesNoPromptPref.values()) m_asrunMonitor.add(ynp.title);
		new Label(userGroup, SWT.NONE);

		/*
		 * Save group
		 */
		GridLayout saveLayout = new GridLayout(3, true);
		Group saveGroup = new Group(container, SWT.BORDER);
		saveGroup.setText("Save preferences");
		saveGroup.setLayout(saveLayout);
		saveGroup.setLayoutData(GridDataFactory.copyData(expand));

		/*
		 * Save to current XML file
		 */
		final Button saveBtn = new Button(saveGroup, SWT.PUSH);
		saveBtn.setText("Save to current file");
		saveBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent event)
			{
				IConfigurationManager conf = getConfigurationManager();
				String cfgFile = conf.getConfigurationFile();
				try
				{
					if (!MessageDialog.openConfirm(saveBtn.getShell(),
					        "Overwrite preferences?",
					        "This action will overwrite the current preferences stored in "
					                + "the default configuration file '"
					                + cfgFile + "'. Do you want to continue?")) return;

					GUIPreferencesSaver saver = new GUIPreferencesSaver(cfgFile);
					saver.savePreferences();
					MessageDialog.openInformation(saveBtn.getShell(),
					        "Preferences saved", "Preferences saved to file '"
					                + cfgFile + "'");
				}
				catch (Exception ex)
				{
					MessageDialog.openError(saveBtn.getShell(),
					        "Cannot save preferences",
					        "Unable to save preferences to '" + cfgFile + "'\n"
					                + ex.getLocalizedMessage());
				}
			}
		});

		/*
		 * Save to alternate XML file
		 */
		final Button saveOtherBtn = new Button(saveGroup, SWT.PUSH);
		saveOtherBtn.setText("Save to another file...");
		saveOtherBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent event)
			{
				FileDialog dialog = new FileDialog(saveOtherBtn.getShell(),
				        SWT.SAVE);
				dialog.setFilterExtensions(new String[] { "*.xml" });
				dialog.setText("Select file to save preferences");
				dialog.setFilterNames(new String[] { "XML files (*.xml)" });
				String selected = dialog.open();
				if (selected != null)
				{
					/*
					 * If user has not set the file extension, then add it
					 */
					if (!selected.endsWith(".xml"))
					{
						selected += ".xml";
					}
					try
					{
						File file = new File(selected);
						if (file.exists())
						{
							if (!MessageDialog.openConfirm(
							        saveOtherBtn.getShell(), "Overwrite file?",
							        "File '" + selected
							                + "' already exists. Overwrite?")) return;
						}
						GUIPreferencesSaver saver = new GUIPreferencesSaver(
						        selected);
						saver.savePreferences();
						MessageDialog.openInformation(saveOtherBtn.getShell(),
						        "Preferences saved",
						        "Preferences saved to file '" + selected + "'");
					}
					catch (Exception ex)
					{
						MessageDialog.openError(saveOtherBtn.getShell(),
						        "Cannot save preferences",
						        "Unable to save preferences to '" + selected
						                + "'\n" + ex.getLocalizedMessage());
					}
				}
			}
		});

		/*
		 * Load preferences from an external file
		 */
		final Button loadPrefsBtn = new Button(saveGroup, SWT.PUSH);
		loadPrefsBtn.setText("Load from external file...");
		loadPrefsBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent event)
			{
				FileDialog dialog = new FileDialog(loadPrefsBtn.getShell(),SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.xml", "*.XML" });
				String selected = dialog.open();
				if (selected != null)
				{
					File file = new File(selected);
					if (file.exists())
					{
						if (!MessageDialog.openConfirm(
						        loadPrefsBtn.getShell(),
						        "Overwrite preferences?",
						        "All the current values will be replaced with the ones defined in the selected file.\n"
						                + "Do you wish to continue?")) return;

					}

					/*
					 * Overwrite preferences dialog
					 */
					IPreferenceStore store = Activator.getDefault()
					        .getPreferenceStore();
					GUIPreferencesLoader loader = new GUIPreferencesLoader(
					        selected, store);
					boolean loaded = loader.overwrite();
					if (!loaded)
					{
						MessageDialog.openError(
						        loadPrefsBtn.getShell(),
						        "Error while loading configuration file",
						        "An unexpected error ocurred while loading the configuration file.\n"
						                + "Check the application log for details");
					}
				}
			}
		});

		refreshPage();
		return container;
	}

	@Override
	public void performApply()
	{
		IConfigurationManager conf = getConfigurationManager();

		// startup connection
		boolean startup = m_startupConnect.getSelection();
		conf.setBooleanProperty(PropertyKey.STARTUP_CONNECT, startup);

		conf.setProperty(PropertyKey.AUTOMATIC_CLOSE, AutoClosePref.values()[m_autoClose.getSelectionIndex()].name());
		
		// Confirm abort
		conf.setBooleanProperty(PropertyKey.CONFIRM_ABORT, m_confirmAbort.getSelection());

		// Multi attach
		conf.setProperty(PropertyKey.MULTIPLE_ATTACH, YesNoPromptPref.values()[m_multiAttach.getSelectionIndex()].name());

		// AsRun 
		conf.setProperty(PropertyKey.ASRUN_CONTROL, YesNoPromptPref.values()[m_asrunControl.getSelectionIndex()].name());
		conf.setProperty(PropertyKey.ASRUN_MONITOR, YesNoPromptPref.values()[m_asrunMonitor.getSelectionIndex()].name());

		// Update connectivity settings
		String user = m_authUser.getText();
		String pwd = m_authPassword.getText();
		String key = m_authKey.getText();
		if (user != null && user.trim().isEmpty()) user = null;
		if (pwd != null && pwd.trim().isEmpty()) pwd = null;
		if (key != null && key.trim().isEmpty()) key= null;
		if (user != null)
		{
			AuthenticationData auth = new AuthenticationData(user,pwd,key);
			conf.updateConnectivityDefaults(auth);
		}
		
		// response timeout
		String timeout = m_responseTimeout.getText();
		conf.setProperty(PropertyKey.RESPONSE_TIMEOUT, timeout);
		// open timeout
		String openTimeout = m_openTimeout.getText();
		conf.setProperty(PropertyKey.OPEN_TIMEOUT, openTimeout);
		// prompt file
		String file = m_soundFile.getText();
		conf.setProperty(PropertyKey.PROMPT_SOUND_FILE, file);
	}

	@Override
	public void performDefaults()
	{
		IConfigurationManager conf = getConfigurationManager();
		for(PropertyKey key : PropertyKey.values())
		{
			conf.resetProperty(key);
		}
		refreshPage();
	}

	@Override
	public void refreshPage()
	{
		IConfigurationManager conf = getConfigurationManager();

		boolean value = conf.getBooleanProperty(PropertyKey.STARTUP_CONNECT);
		m_startupConnect.setSelection(value);

		// Auto close
		String svalue = conf.getProperty(PropertyKey.AUTOMATIC_CLOSE);
		try
		{
			m_autoClose.select( AutoClosePref.valueOf(svalue).ordinal() );
		}
		catch(Exception ex) { m_autoClose.select(AutoClosePref.NO.ordinal()); }

		// Multiple attach
		String avalue = conf.getProperty(PropertyKey.MULTIPLE_ATTACH);
		try
		{
			m_multiAttach.select(YesNoPromptPref.valueOf(avalue).ordinal());
		}
		catch(Exception ex) { m_multiAttach.select(YesNoPromptPref.NO.ordinal()); }

		// ASRUN
		String acvalue = conf.getProperty(PropertyKey.ASRUN_CONTROL);
		String amvalue = conf.getProperty(PropertyKey.ASRUN_MONITOR);
		try
		{
			m_asrunControl.select(YesNoPromptPref.valueOf(acvalue).ordinal());
			m_asrunMonitor.select(YesNoPromptPref.valueOf(amvalue).ordinal());
		}
		catch(Exception ex) 
		{ 
			m_asrunControl.select(YesNoPromptPref.YES.ordinal());
			m_asrunMonitor.select(YesNoPromptPref.YES.ordinal());
		}

		
		// Confirm abort
		m_confirmAbort.setSelection(conf.getBooleanProperty(PropertyKey.CONFIRM_ABORT));

		AuthenticationData auth = conf.getConnectivityDefaults();
		if (auth != null)
		{
			m_authUser.setText( auth.getUsername() != null ? auth.getUsername() : "");
			m_authPassword.setText( auth.getPassword() != null ? auth.getPassword() : "");
			m_authKey.setText( auth.getKeyFile() != null ? auth.getKeyFile() : "");
		}
		
		String timeout = conf.getProperty(PropertyKey.RESPONSE_TIMEOUT);
		m_responseTimeout.setText(timeout);
		String openTimeout = conf.getProperty(PropertyKey.OPEN_TIMEOUT);
		m_openTimeout.setText(openTimeout);
		String file = conf.getProperty(PropertyKey.PROMPT_SOUND_FILE);
		m_soundFile.setText(file);
	}
}
