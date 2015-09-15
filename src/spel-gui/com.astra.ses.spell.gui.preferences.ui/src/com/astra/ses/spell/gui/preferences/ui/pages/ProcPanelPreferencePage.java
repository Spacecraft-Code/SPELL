///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.preferences.ui.pages
// 
// FILE      : ScopeStylesPreferencePage.java
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.astra.ses.spell.gui.core.exceptions.ServerError;
import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.IServerProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.preferences.initializer.elements.CommandsInfo;
import com.astra.ses.spell.gui.preferences.initializer.elements.StatusInfo;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;

/*******************************************************************************
 * 
 * {@link ProcPanelPreferencePage} allows the user to configure how incoming
 * messages will be rendered in the textual view
 * 
 ******************************************************************************/
public class ProcPanelPreferencePage extends BasicPreferencesPage
{

	/** Scope selection widget */
	private List<Combo>	      m_combos;
	/** Flag that marks if preference page components are enabled or disabled */
	private boolean           m_enable;

	/**
	 * Constructor
	 * 
	 */
	public ProcPanelPreferencePage() 
	{
		// Retrieving server, context and procedure manager
		IServerProxy proxy = (IServerProxy) ServiceManager.get(IServerProxy.class);
		IContextProxy cproxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		IProcedureManager pmgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
				
		// Initializing flag for enabling/disable preference page components
		m_enable = true;
		
		// Assess server/context proxies connections
		if (cproxy.isConnected() && proxy.isConnected())
		{
			try
			{
				// Check if there are open procedures.
				Set<String> openProcs = pmgr.getOpenLocalProcedures();
				if (openProcs.size() > 0)
				{
					// Disabling preferences when there are opened procedures
					m_enable = false;
				}
			}
			catch (ServerError ex)
			{
				// Disabling preferences in case of error
				m_enable = false;
			}
		}
		else
		{
			// Disabling preferences in case of disconnection
			m_enable = false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent)
	{
		// Container
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.grabExcessHorizontalSpace = true;
		container.setLayout(layout);
		container.setLayoutData(layoutData);

		/*
		 * Components group
		 */
		Group componentsGroup = new Group(container, SWT.BORDER);
		GridLayout scopesGroupLayout = new GridLayout(2, false);
		componentsGroup.setLayout(scopesGroupLayout);
		componentsGroup.setLayoutData(GridDataFactory.copyData(layoutData));
		componentsGroup.setText("Components allocation");
		
		// Creating list of combos
		m_combos = new ArrayList<Combo>();
		LinkedHashMap<String,String> components = getComponents();
		for ( int index = 0; index < components.size(); index++ )
		{
			Label label = new Label(componentsGroup, SWT.NONE);
			label.setText("Component " + (index+1) + " : ");
			Combo combo = new Combo(componentsGroup, SWT.READ_ONLY | SWT.SINGLE);
			combo.setItems(components.keySet().toArray(new String[]{}));
			combo.select(index);
			combo.setEnabled(m_enable);
			// Initialize listeners
			combo.addSelectionListener(new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					Combo combo = (Combo) e.widget;
					updateSelection(combo);
				}
			});
			m_combos.add(combo);			
		}
		
		// Disabling apply and restore buttons (if applies)
		if (!m_enable)
		{
			noDefaultAndApplyButton();
		}

		return container;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	@Override
	public void performApply()
	{
		// Get configuration
		IConfigurationManager conf = getConfigurationManager();
		
		// Get status and commands
		StatusInfo status = conf.getStatus();
		CommandsInfo commands = conf.getCommands();
		
		// Setup procedure panel components according user changes
		LinkedHashMap<String,String> components = new LinkedHashMap<String, String>();
		for (Combo combo : m_combos)
		{
			if ( combo.getText().equals(StatusInfo.STATUS_ID) )
			{
				components.put(combo.getText(),status.getLabel());
			}
			else
			{
				components.put(combo.getText(),commands.getMap().get(combo.getText()));
			}
		}
		
		// Update status and commands
		conf.setStatus(new StatusInfo(components));
		conf.setCommands(new CommandsInfo(components));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	public void performDefaults()
	{
		IConfigurationManager conf = getConfigurationManager();
		conf.restoreCommands();
		conf.restoreStatus();
		initializeValues();
	}
	
	/* (non-Javadoc)
	 * @see com.astra.ses.spell.gui.preferences.ui.pages.BasicPreferencesPage#refreshPage()
	 */
	@Override
	public void refreshPage()
	{
	}
	
	/**
	 * Initialize values
	 * 
	 */
	private void initializeValues()
	{
		// Extract components
		LinkedHashMap<String,String> components = getComponents();
		
		// Initialize data
		int  index = 0;
		for ( String component : components.keySet() )
		{
			m_combos.get(index).setText(components.get(component));
			index++;
		}		
	}
	
	/**
	 * Get procedure panel components
	 * 
	 */
	private LinkedHashMap<String,String> getComponents()
	{
		IConfigurationManager conf = getConfigurationManager();
		CommandsInfo commands = conf.getCommands();
		StatusInfo status = conf.getStatus();

		// Creating list of combo possibilities
		int location = 0;
		int pos = status.getLocation();
		LinkedHashMap<String,String> components = new LinkedHashMap<String,String>();
		for ( String component : commands.getMap().keySet() )
		{
			if ( pos == location )
			{
				components.put(StatusInfo.STATUS_ID,status.getLabel());
				location++;
			}
			components.put(component,commands.getMap().get(component));
			location++;
		}
		if ( (pos >= location) || (pos == -1) )
		{
			components.put(StatusInfo.STATUS_ID,status.getLabel());
		}
		return components;
	}

	/**
	 * Update the widgets information
	 * 
	 */
	private void updateSelection(Combo combo)
	{
		List<String> components = new ArrayList<String>();
		components.addAll(Arrays.asList(combo.getItems()));
		for ( Combo widget : m_combos )
		{
			components.remove(widget.getText());
		}

		String value = (components.size() == 0 ? "" : components.get(0));
		if ( !value.isEmpty() )
		{
			String selection = combo.getText();
			List<Combo> widgets = new ArrayList<Combo>(m_combos);
			widgets.remove(combo);
			for ( Combo widget :  widgets)
			{
				if ( widget.getText().equals(selection))
				{
					widget.setText(value);
				}
			}
		}
	}

}
