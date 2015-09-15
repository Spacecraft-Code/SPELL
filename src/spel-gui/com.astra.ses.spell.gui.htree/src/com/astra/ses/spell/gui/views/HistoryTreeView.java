///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views
// 
// FILE      : HistoryTreeView.java
//
// DATE      : Jun 12, 2013
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
package com.astra.ses.spell.gui.views;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.interfaces.IProcedureView;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.procs.exceptions.NoSuchProcedure;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

/**************************************************************************
 * View for the history tree
 *************************************************************************/
public class HistoryTreeView extends ViewPart implements IPartListener2
{
	/** The ID of the view as specified by the extension. */
	public static final String	ID	= "com.astra.ses.spell.gui.views.tools.HistoryTreeView";
	private IProcedureManager m_pmgr;
	private Font m_boldFont;
	private StackLayout m_stack;
	private Composite m_base;
	private Composite m_defaultPage;
	private Map<String,Composite> m_pages;
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public HistoryTreeView()
	{
		super();
		m_pmgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
		m_boldFont = ((IConfigurationManager)ServiceManager.get(IConfigurationManager.class)).getFont(FontKey.GUI_BOLD,10);
		m_pages = new TreeMap<String,Composite>();
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addPartListener(this);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void createPartControl(Composite parent)
	{
		m_base = new Composite(parent,SWT.BORDER);
		m_base.setLayoutData( new GridData( GridData.FILL_BOTH ));
		m_stack = new StackLayout();
		m_base.setLayout(m_stack);
		m_stack.topControl = getDefaultPage();
		m_base.layout();
	}

	/***************************************************************************
	 * Dispose the view. Called when the view part is closed.
	 **************************************************************************/
	public void dispose()
	{
		super.dispose();
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (page != null) page.removePartListener(this);
		if (m_defaultPage != null)
		{
			m_defaultPage.dispose();
		}
		for(Composite c : m_pages.values())
		{
			c.dispose();
		}
		m_pages.clear();
		Logger.debug("Disposed", Level.GUI, this);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void setFocus()
    {
		if (!m_stack.topControl.isDisposed())
		{
			m_stack.topControl.setFocus();
		}
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
	private Composite getDefaultPage()
	{
		if (m_defaultPage == null)
		{
			m_defaultPage = new Composite(m_base, SWT.NONE);
			m_defaultPage.setLayout( new GridLayout(1,true) );
			Label label = new Label(m_defaultPage, SWT.NONE);
			label.setText("No history tree available");
			label.setFont(m_boldFont);
			label.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));
			m_defaultPage.setLayoutData( new GridData( GridData.FILL_BOTH ));
		}
		return m_defaultPage;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private String getMainProcId( IProcedureView view )
	{
		String instanceId = view.getProcId();
		try
		{
			IProcedure procedure = m_pmgr.getProcedure(instanceId);
			if (procedure.getRuntimeInformation().getClientMode().equals(ClientMode.CONTROL) ||
				procedure.getRuntimeInformation().getClientMode().equals(ClientMode.MONITOR))
			{
				while(!procedure.isMain())
				{
					procedure = m_pmgr.getProcedure(procedure.getParent());
				}
				return procedure.getProcId();
			}
			return null;
		}
		catch(NoSuchProcedure ex)
		{
			return null;
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private void showPage( String procId )
	{
		if (m_base.isDisposed()) return;
		if (procId == null)
		{
			m_stack.topControl = m_defaultPage;
			m_base.layout();
		}
		else if (m_pages.containsKey(procId))
		{
			m_stack.topControl = m_pages.get(procId);
			m_base.layout();
		}
		else
		{
			m_stack.topControl = createPage(procId);
			m_base.layout();
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private Composite createPage( String procId )
	{
		Composite page = new HistoryTreePage( procId, m_base, this);
		page.setLayoutData( new GridData( GridData.FILL_BOTH ));
		m_pages.put(procId,page);
		return page;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void partActivated(IWorkbenchPartReference partRef)
    {
	    IWorkbenchPart part = partRef.getPart(false);
	    if (part instanceof IProcedureView)
	    {
	    	IProcedureView view = (IProcedureView) part;
	    	String mainProcId = getMainProcId(view);
	    	showPage(mainProcId);
	    }
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void partBroughtToTop(IWorkbenchPartReference partRef)
    {
	    IWorkbenchPart part = partRef.getPart(false);
	    if (part instanceof IProcedureView)
	    {
	    	IProcedureView view = (IProcedureView) part;
	    	String mainProcId = getMainProcId(view);
	    	showPage(mainProcId);
	    }
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void partClosed(IWorkbenchPartReference partRef)
    {
	    IWorkbenchPart part = partRef.getPart(false);
	    if (part instanceof IProcedureView)
	    {
	    	IProcedureView view = (IProcedureView) part;
	    	if (m_pages.containsKey(view.getProcId()))
	    	{
	    		m_pages.remove(view.getProcId()).dispose();
	    	}
	    }
	    else
	    {
	    	showPage(null);
	    }
		
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void partDeactivated(IWorkbenchPartReference partRef)
    {
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void partOpened(IWorkbenchPartReference partRef)
    {
	    IWorkbenchPart part = partRef.getPart(false);
	    if (part instanceof IProcedureView)
	    {
	    	IProcedureView view = (IProcedureView) part;
	    	String mainProcId = getMainProcId(view);
	    	showPage(mainProcId);
	    }
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void partHidden(IWorkbenchPartReference partRef)
    {
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void partVisible(IWorkbenchPartReference partRef)
    {
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public void partInputChanged(IWorkbenchPartReference partRef)
    {
    }

}
