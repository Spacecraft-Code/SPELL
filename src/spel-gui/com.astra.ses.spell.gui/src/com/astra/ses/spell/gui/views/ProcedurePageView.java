///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views
// 
// FILE      : ProcedurePageView.java
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
package com.astra.ses.spell.gui.views;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.exceptions.NoSuchViewException;
import com.astra.ses.spell.gui.interfaces.IProcedureView;
import com.astra.ses.spell.gui.services.IRuntimeSettings;
import com.astra.ses.spell.gui.services.IViewManager;

/**************************************************************************
 * Base class for procedure page-based views
 * 
 *************************************************************************/
public abstract class ProcedurePageView extends PageBookView
{
	/** Holds the default empty string */
	private String m_defaultMsg;
	/** Holds the default title */
	private String m_defaultTitle;

	/***************************************************************************
	 * Constructor.
	 * 
	 * @param defaultMessage
	 **************************************************************************/
	public ProcedurePageView(String defaultMessage, String defaultTitle)
	{
		super();
		m_defaultMsg = defaultMessage;
		m_defaultTitle = defaultTitle;
	}

	@Override
	protected IPage createDefaultPage(PageBook book)
	{
		MessagePage defaultPage = new MessagePage();
		initPage(defaultPage);
		defaultPage.setMessage(m_defaultMsg);
		defaultPage.createControl(book);
		setPartName(m_defaultTitle);
		return defaultPage;
	}

	protected abstract Page createMyPage(String procedureId, String name);

	@Override
	protected PageRec doCreatePage(IWorkbenchPart part)
	{
		// Get the view
		IProcedureView view = (IProcedureView) part;
		// Create the page
		Page page = createMyPage(view.getProcId(), view.getProcName());
		initPage(page);
		page.createControl(getPageBook());
		return new PageRec(part, page);
	}

	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord)
	{
		pageRecord.page.dispose();
		setPartName(m_defaultTitle);
	}

	@Override
	protected IWorkbenchPart getBootstrapPart()
	{
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IWorkbenchPart result = null;
		if (page != null)
		{
			IWorkbenchPart part = page.getActivePart();
			if (isImportant(part))
			{
				result = part;
			}
			else
			{
				IRuntimeSettings mgr = (IRuntimeSettings) ServiceManager.get(IRuntimeSettings.class);
				String procId = (String) mgr.getRuntimeProperty(IRuntimeSettings.RuntimeProperty.ID_PROCEDURE_SELECTION);
				if ((procId != null) && (!procId.isEmpty()))
				{
					try
					{
						IViewManager vmgr = (IViewManager) ServiceManager.get(IViewManager.class);
						result = (IWorkbenchPart) vmgr.getProcedureView(procId);
					}
					catch (NoSuchViewException ex)
					{
						result = null;
					}
				}
			}
		}
		return result;
	}

	@Override
	protected boolean isImportant(IWorkbenchPart part)
	{
		if (part == null)
			return false;
		boolean important = part.getClass().equals(ProcedureView.class);
		return (important);
	}

	@Override
	protected void showPageRec(PageRec pageRec)
	{
		super.showPageRec(pageRec);

		String partName = m_defaultTitle;
		if (!pageRec.page.equals(getDefaultPage()))
		{
			IProcedureView view = (IProcedureView) pageRec.part;
			partName += " - " + view.getProcName();
		}
		setPartName(partName);
	}
}
