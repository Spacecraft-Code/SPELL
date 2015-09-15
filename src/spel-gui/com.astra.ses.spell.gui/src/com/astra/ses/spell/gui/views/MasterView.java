///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views
// 
// FILE      : MasterView.java
//
// DATE      : 2014
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
package com.astra.ses.spell.gui.views;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.part.ViewPart;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreContextOperationListener;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.extensions.GuiNotifications;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.services.IRuntimeSettings;
import com.astra.ses.spell.gui.services.IRuntimeSettings.RuntimeProperty;
import com.astra.ses.spell.gui.services.IViewManager;
import com.astra.ses.spell.gui.views.controls.master.NotConnectedPanel;
import com.astra.ses.spell.gui.views.controls.master.executors.ExecutorComposite;
import com.astra.ses.spell.gui.views.controls.master.recovery.RecoveryComposite;

/*******************************************************************************
 * 
 ******************************************************************************/
public class MasterView extends ViewPart implements ICoreContextOperationListener, IPropertyChangeListener
{
	/** The view identifier */
	public static final String ID = "com.astra.ses.spell.gui.views.MasterView";

	/** Holds the stacked dictionary for the condition definition widgets */
	private Composite			    m_stackContainer;
	/** Holds the stack layout */
	private StackLayout	            m_stack;
	/** Holds the "not connected" panel */
	private NotConnectedPanel	    m_notConnectedPanel;
	/** Holds the executors composite */
	private Composite               m_executorsPanel;

	/** Holds the tab folder for executors */
	private TabFolder	   		 m_tabs;
	/** Holds the table of executors */
	private ExecutorComposite 	 m_executorsTab;
	/** Holds the table of recovery files */
	private RecoveryComposite 	 m_recoveryTab;
	
	/** Holds the executors area label */
	private Label                m_executorsLabel;


	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public MasterView()
	{
		super();
		Logger.debug("Created", Level.INIT, this);
		GuiNotifications.get().addListener(this, ICoreContextOperationListener.class);
	}

	/***************************************************************************
	 * Create the view contents.
	 * 
	 * @param parent
	 *            The view top composite
	 **************************************************************************/
	public void createPartControl(Composite parent)
	{
		// Obtain the required resources
		IConfigurationManager cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		cfg.addPropertyChangeListener(this);
		
		parent.setLayout( new GridLayout(1,true) );
		
		m_stack = new StackLayout();
		m_stackContainer = new Composite(parent, SWT.NONE);
		GridData std = new GridData(GridData.FILL_BOTH);
		m_stackContainer.setLayoutData(std);
		m_stackContainer.setLayout(m_stack);

		// STACK / EXECUTORS =================================================================

		m_executorsPanel = new Composite(m_stackContainer, SWT.NONE);
		m_executorsPanel.setLayout( new GridLayout(1,true) );
		m_executorsPanel.setLayoutData( new GridData( GridData.FILL_BOTH ));
		
		m_executorsLabel = new Label(m_executorsPanel, SWT.CENTER);
		m_executorsLabel.setText("???" );
		m_executorsLabel.setFont( cfg.getFont( FontKey.GUI_BOLD, 18 ) );
		m_executorsLabel.setLayoutData( new GridData( GridData.FILL_HORIZONTAL));

		m_tabs = new TabFolder(m_executorsPanel, SWT.NONE);
		GridData cld = new GridData(GridData.FILL_BOTH);
		m_tabs.setLayoutData(cld);
		
		// TAB - Currently open procedures
		TabItem executorsItem = new TabItem(m_tabs,SWT.NONE);
		executorsItem.setText("Open procedures");
		m_executorsTab = new ExecutorComposite(m_tabs, SWT.NONE );
		m_executorsTab.setLayoutData( new GridData( GridData.FILL_BOTH ));
		executorsItem.setControl(m_executorsTab);

		// TAB - Recovery
		TabItem recoveryItem = new TabItem(m_tabs,SWT.NONE);
		recoveryItem.setText("Recover executions");
		m_recoveryTab = new RecoveryComposite( m_tabs, SWT.NONE );
		m_recoveryTab.setLayoutData( new GridData( GridData.FILL_BOTH ));
		recoveryItem.setControl(m_recoveryTab);

		// STACK / NOT CONNECTED ==============================================================
		m_notConnectedPanel = new NotConnectedPanel(m_stackContainer);
		m_stack.topControl = m_notConnectedPanel;
		m_stackContainer.layout();

		// SEPARATOR ==========================================================================
		
		Label sep = new Label( parent, SWT.SEPARATOR | SWT.HORIZONTAL );
		sep.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));

		// SUMMARY PANEL ======================================================================

		
		
		// Register in the view manager to make this view available
		IViewManager mgr = (IViewManager) ServiceManager.get(IViewManager.class);
		mgr.registerView(ID, this);
	}

	/***************************************************************************
	 * Destroy the view.
	 **************************************************************************/
	public void dispose()
	{
		GuiNotifications.get().removeListener(this);
		IConfigurationManager cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		cfg.removePropertyChangeListener(this);
		super.dispose();
	}

	/***************************************************************************
	 * Receive the input focus.
	 **************************************************************************/
	public void setFocus()
	{
		IRuntimeSettings runtime = (IRuntimeSettings) ServiceManager.get(IRuntimeSettings.class);
		runtime.setRuntimeProperty(RuntimeProperty.ID_PROCEDURE_SELECTION, "");
		m_executorsPanel.setFocus();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public String getListenerId()
    {
	    return ID;
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void notifyContextAttached( final ContextInfo ctx)
    {
		Display.getDefault().syncExec( new Runnable()
		{
			public void run()
			{
				m_executorsLabel.setText("Procedures for spacecraft " + ctx.getName() );
				m_stack.topControl = m_executorsPanel;
				m_stackContainer.layout();
				m_executorsTab.refresh();
			}
		});
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void notifyContextDetached()
    {
		if (!m_stackContainer.isDisposed())
		{
			m_stack.topControl = m_notConnectedPanel;
			m_stackContainer.layout();
		}
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void notifyContextError(ErrorData error)
    {
		if (!m_stackContainer.isDisposed())
		{
			m_stack.topControl = m_notConnectedPanel;
			m_stackContainer.layout();
		}
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void propertyChange(PropertyChangeEvent event)
    {
		String property = event.getProperty();
		if (property.equals(FontKey.GUI_NOM.getPreferenceName()) || 
			property.equals(FontKey.GUI_BIG.getPreferenceName()))
		{
			m_executorsLabel.redraw();
			m_executorsTab.applyFonts();
		}
    }
}
