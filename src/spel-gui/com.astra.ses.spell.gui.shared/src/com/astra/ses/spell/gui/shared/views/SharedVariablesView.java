///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.shared.views
// 
// FILE      : SharedVariablesView.java
//
// DATE      : Sep 19, 2013
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
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.shared.views;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.part.ViewPart;

import com.astra.ses.spell.gui.core.comm.messages.MessageException;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageRequest;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageResponse;
import com.astra.ses.spell.gui.core.interfaces.ICommListener;
import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.IMessageField;
import com.astra.ses.spell.gui.core.interfaces.IServerProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreContextOperationListener;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo.ServerRole;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.extensions.GuiNotifications;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.shared.commands.ClearSharedVariablesJob;
import com.astra.ses.spell.gui.shared.commands.RefreshSharedVariablesJob;
import com.astra.ses.spell.gui.shared.messages.ISDMessageField;
import com.astra.ses.spell.gui.shared.messages.ISDMessageId;
import com.astra.ses.spell.gui.shared.messages.SharedDataOperation;
import com.astra.ses.spell.gui.shared.services.ISharedDataService;
import com.astra.ses.spell.gui.shared.services.ISharedScope;
import com.astra.ses.spell.gui.shared.views.controls.ScopeTab;
import com.astra.ses.spell.gui.shared.views.controls.SharedVariableDialog;

/*******************************************************************************
 * 
 ******************************************************************************/
public class SharedVariablesView extends ViewPart implements ICommListener, ICoreContextOperationListener
{
	/** Holds the view identifier */
	public static final String ID = "com.astra.ses.spell.gui.shared.views.SharedVariablesView";

	private ISharedDataService m_service;
	private TabFolder m_tabs;
	private Map<String,ScopeTab> m_scopes;
	private Button m_updateAll;
	private Button m_clearAll;
	private Button m_addScope;
	private IContextProxy m_ctxProxy;
	private IServerProxy m_srvProxy;
	private boolean m_monitoringMode;

	/***************************************************************************
	 * Constructor.
	 **************************************************************************/
	public SharedVariablesView()
	{
		super();
		Logger.debug("Created", Level.INIT, this);
		m_service = (ISharedDataService) ServiceManager.get(ISharedDataService.class);
		m_scopes = new TreeMap<String,ScopeTab>();
		m_ctxProxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		m_srvProxy = (IServerProxy) ServiceManager.get(IServerProxy.class);
		if (m_srvProxy.isConnected())
		{
			m_monitoringMode = m_srvProxy.getCurrentServer().getRole().equals(ServerRole.MONITORING);
		}
		else
		{
			m_monitoringMode = true;
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void dispose()
	{
		super.dispose();
		IContextProxy proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		proxy.removeCommListener(this);
		GuiNotifications.get().removeListener(this);
	}

	/***************************************************************************
	 * Complete the creation of the view
	 **************************************************************************/
	@Override
	public void createPartControl( Composite parent )
	{
		setPartName("Shared Variables");
		
		Composite top = new Composite(parent,SWT.NONE);
		top.setLayout( new GridLayout(3,true) );
		
		m_updateAll = new Button(top, SWT.PUSH );
		m_updateAll.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));
		m_updateAll.setText("Update all");
		m_updateAll.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected( SelectionEvent ev )
			{
				RefreshSharedVariablesJob job = new RefreshSharedVariablesJob();
				CommandHelper.executeInProgress(job, true, true);
				update();
			}
		});

		m_clearAll = new Button(top, SWT.PUSH );
		m_clearAll.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));
		m_clearAll.setText("Clear all");
		m_clearAll.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected( SelectionEvent ev )
			{
				String text = "This action will remove all scopes and variables.\n\nDo you want to proceed?";
				if (MessageDialog.openConfirm(getSite().getShell(), "Delete shared variables", text))
				{
					ClearSharedVariablesJob job = new ClearSharedVariablesJob();
					CommandHelper.executeInProgress(job, true, true);
					update();
				}
			}
		});

		m_addScope = new Button(top, SWT.PUSH);
		m_addScope.setText("Add new scope");
		m_addScope.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		m_addScope.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected( SelectionEvent ev )
			{
				SharedVariableDialog dialog = new SharedVariableDialog(getSite().getShell());
				if (dialog.open() == IDialogConstants.OK_ID)
				{
					ISharedDataService svc = (ISharedDataService) ServiceManager.get(ISharedDataService.class);
					svc.addSharedScope(dialog.getScope());
					createScope(dialog.getScope());
				}
			}
		});


		m_tabs = new TabFolder(top, SWT.BORDER);
		GridData tdata = new GridData( GridData.FILL_BOTH );
		tdata.horizontalSpan = 3;
		m_tabs.setLayoutData( tdata );
		m_tabs.setLayout( new GridLayout(1,true) );
		
		IContextProxy proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		if (proxy.isConnected())
		{
			RefreshSharedVariablesJob job = new RefreshSharedVariablesJob();
			CommandHelper.executeInProgress(job, true, true);
			update();
		}
		
		proxy.addCommListener(this);
		GuiNotifications.get().addListener(this, ICoreContextOperationListener.class);
		
		boolean ready = (m_ctxProxy.isConnected() && !m_monitoringMode);
		m_updateAll.setEnabled(m_ctxProxy.isConnected());
		m_addScope.setEnabled(ready);
		m_clearAll.setEnabled(ready);
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void createScope( String name )
	{
    	Logger.debug("Create new scope tab: " + name, Level.GUI, this);
		ISharedScope table = m_service.getSharedScope(name);
		ScopeTab tab = new ScopeTab(m_tabs, table, this, m_monitoringMode);
		m_scopes.put(name,tab);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void removeScope( String name )
	{
    	Logger.debug("Remove scope tab: " + name, Level.GUI, this);
		ScopeTab tab = m_scopes.remove(name);
		tab.dispose();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void removeAllScopes()
	{
		for(String scope : m_scopes.keySet())
		{
			removeScope(scope);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void updateScope( String name )
	{
		ScopeTab tab = m_scopes.get(name);
		tab.refresh();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void update()
	{
		// Add scopes if needed
		for(String scope : m_service.getSharedScopes())
		{
			Logger.debug("Scope from service: " + scope, Level.GUI, this);
			if (!m_scopes.containsKey(scope))
			{
				Logger.debug("Need to create: " + scope, Level.GUI, this);
				createScope(scope);
			}
			else
			{
				Logger.debug("Need to update: " + scope, Level.GUI, this);
				updateScope(scope);
			}
		}
		
		// Remove scopes if needed
		List<String> toRemove = new LinkedList<String>();
		for(String scope : m_scopes.keySet())
		{
			if (!m_service.getSharedScopes().contains(scope))
			{
				Logger.debug("Scope not in service: " + scope, Level.GUI, this);
				toRemove.add(scope);
			}
		}
		for(String scope : toRemove)
		{
			Logger.debug("Need to remove: " + scope, Level.GUI, this);
			removeScope(scope);
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void setFocus()
    {
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public String getListenerId()
    {
	    return "com.engineering.ses.gui.sharedVariablesView";
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void notifyContextAttached(ContextInfo ctx) 
    {
		try
		{
			RefreshSharedVariablesJob job = new RefreshSharedVariablesJob();
			CommandHelper.executeInProgress(job, true, true);
			update();
		    enableControls(true);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void notifyContextDetached()
    {
		try
		{
			removeAllScopes();
			enableControls(false);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void notifyContextError(ErrorData error)
    {
		try
		{
			removeAllScopes();
			enableControls(false);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void enableControls( boolean enable )
	{
		m_monitoringMode = m_srvProxy.getCurrentServer().getRole().equals(ServerRole.MONITORING);
		m_tabs.setEnabled(enable);
		for(ScopeTab tab : m_scopes.values())
		{
			tab.update(enable, m_monitoringMode);
		}
		m_addScope.setEnabled(enable && !m_monitoringMode);
		m_updateAll.setEnabled(enable);
		m_clearAll.setEnabled(enable && !m_monitoringMode);
	}

	@Override
    public SPELLmessageResponse receiveRequest(SPELLmessageRequest msg)
    {
	    return null;
    }

	@Override
    public void receiveMessage( final SPELLmessage msg )
    {
		// Process only the messages we are interested on
		if (!msg.getId().equals(ISDMessageId.MSG_SHARED_DATA_OP)) return;
		
		// Ignore messages of operations originated by this GUI
		if (msg.hasKey(IMessageField.FIELD_GUI_KEY))
		{
			try
            {
	            String guiKey = msg.get(IMessageField.FIELD_GUI_KEY);
	            if (guiKey.equals(m_ctxProxy.getClientKey())) return;
            }
            catch (MessageException e)
            {
	            e.printStackTrace();
	            return;
            }
		}

		try
        {
	        final SharedDataOperation op = SharedDataOperation.valueOf(msg.get(ISDMessageField.FIELD_SHARED_OP));
	        final String scope = msg.get(ISDMessageField.FIELD_SHARED_SCOPE);
	        
	        Display.getDefault().syncExec( new Runnable()
	        {
	        	public void run()
	        	{
	        		switch(op)
			        {
			        case ADD_SHARED_SCOPE:
			        case DEL_SHARED_SCOPE:
			        	m_service.update( new NullProgressMonitor() );
			        	update();
			        	break;
			        case CLEAR_SHARED_SCOPE:
			        case DEL_SHARED_DATA:
			        case SET_SHARED_DATA:
			        	if (m_scopes.containsKey(scope))
			        	{
			        		try
			        		{
						        String[] varList = msg.get(ISDMessageField.FIELD_SHARED_VARIABLE).split("\3");
						        String[] valueList = msg.get(ISDMessageField.FIELD_SHARED_VALUE).split("\3");
				        		m_scopes.get(scope).forceUpdate( op, varList, valueList );
				        		m_tabs.setSelection(m_scopes.get(scope));
			        		}
			        		catch(Exception ex){};
			        	}
			        	break;
			        }
	        	}
	        });
        }
        catch (MessageException e)
        {
	        e.printStackTrace();
        }
	    
    }

	@Override
    public void connectionLost(ErrorData data)
    {
		try
		{
			removeAllScopes();
			enableControls(false);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
    }

	@Override
    public void connectionFailed(ErrorData data)
    {
		try
		{
			removeAllScopes();
			enableControls(false);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
    }

	@Override
    public void connectionClosed()
    {
		try
		{
			removeAllScopes();
			enableControls(false);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
    }
}
