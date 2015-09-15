///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.master
// 
// FILE      : RecoveryTable.java
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
package com.astra.ses.spell.gui.views.controls.master.recovery;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;

import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.server.ProcedureRecoveryInfo;

/*******************************************************************************
 * @brief
 * @date 09/10/07
 ******************************************************************************/
public class RecoveryTable extends TableViewer 
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Procedure manager handle */
	private static IContextProxy	s_proxy	= null;
	
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private RecoveryFilesContentProvider m_contentProvider;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor.
	 * 
	 * @param procId
	 *            The procedure identifier
	 * @param parent
	 *            The parent composite
	 **************************************************************************/
	public RecoveryTable(RecoveryComposite parent)
	{
		super(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER
		        | SWT.V_SCROLL);
		if (s_proxy == null)
		{
			s_proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		}
		getTable().addSelectionListener(parent);
		getTable().setHeaderVisible(true);
		getTable().setLinesVisible(true);
		createColumns();
		m_contentProvider = new RecoveryFilesContentProvider();
		setContentProvider(m_contentProvider);
		setLabelProvider(new RecoveryFilesLabelProvider());
		setInput(s_proxy);
	}

	/***************************************************************************
	 * Get the selected procedure
	 **************************************************************************/
	public ProcedureRecoveryInfo[] getSelectedProcedures()
	{
		ArrayList<ProcedureRecoveryInfo> ids = new ArrayList<ProcedureRecoveryInfo>();
		IStructuredSelection sel = (IStructuredSelection) getSelection();
		if (!sel.isEmpty())
		{
			@SuppressWarnings("unchecked")
			Iterator<ProcedureRecoveryInfo> it = sel.iterator();
			while (it.hasNext())
			{
				Object proc = it.next();
				ids.add((ProcedureRecoveryInfo) proc);
			}
		}
		return ids.toArray(new ProcedureRecoveryInfo[0]);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void createColumns()
	{
		for (int index = 0; index < RecoveryFilesTableItems.values().length; index++)
		{
			RecoveryFilesTableItems item = RecoveryFilesTableItems.index(index);
			TableViewerColumn col = new TableViewerColumn(this, SWT.NONE);
			col.getColumn().setText(item.title);
			if (item.width > 0) col.getColumn().setWidth(item.width);
			col.getColumn().setResizable(true);
			col.getColumn().setMoveable(false);
			if (item.center) col.getColumn().setAlignment(SWT.CENTER);
		}
	}
}
