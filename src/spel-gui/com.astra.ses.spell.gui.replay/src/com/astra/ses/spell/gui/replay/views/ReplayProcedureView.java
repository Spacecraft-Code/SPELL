///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.replay.views
// 
// FILE      : ReplayProcedureView.java
//
// DATE      : Jun 17, 2013
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
package com.astra.ses.spell.gui.replay.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.interfaces.IControlArea;
import com.astra.ses.spell.gui.interfaces.IPresentationPanel;
import com.astra.ses.spell.gui.interfaces.ProcedureViewCloseMode;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.replay.views.controls.ReplayControlArea;
import com.astra.ses.spell.gui.replay.views.controls.ReplayPresentationPanel;
import com.astra.ses.spell.gui.types.ExecutorStatus;
import com.astra.ses.spell.gui.views.ProcedureView;

/*******************************************************************************
 * 
 ******************************************************************************/
public class ReplayProcedureView extends ProcedureView 
{
	private static IConfigurationManager s_cfg = null;
	private static IContextProxy s_proxy = null;
	private static IProcedureManager s_pmgr = null;
	
	/** Holds the view identifier */
	public static final String ID = "com.astra.ses.spell.gui.replay.views.ReplayProcedureView";

	/***************************************************************************
	 * Constructor.
	 **************************************************************************/
	public ReplayProcedureView()
	{
		super();
		if (s_cfg == null)
		{
			s_cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		}
		if (s_proxy == null)
		{
			s_proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		}
		if (s_pmgr == null)
		{
			s_pmgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
		}
		Logger.debug("Created", Level.INIT, this);
	}

	/***************************************************************************
	 * Dispose the view. Called when the view part is closed.
	 **************************************************************************/
	@Override
	public void dispose()
	{
		String instanceId = getProcId();
		IProcedureManager pmgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
		pmgr.removeProcedure(instanceId);
		super.dispose();
		Logger.debug("Disposed", Level.GUI, this);
	}

	/***************************************************************************
	 * Complete the creation of the view
	 **************************************************************************/
	@Override
	public void createPartControl( Composite parent )
	{
		super.createPartControl(parent);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public ProcedureViewCloseMode getCloseMode()
	{
		return ProcedureViewCloseMode.NONE;
	}

	/***************************************************************************
	 * Create the control area
	 **************************************************************************/
	@Override
	protected IControlArea createControlArea()
	{
		return new ReplayControlArea(this, getModel(), getControlSection(), getModel().getProcId());
	}

	/***************************************************************************
	 * Create the presentation panel
	 **************************************************************************/
	@Override
	protected IPresentationPanel createPresentationPanel( Composite parent )
	{
		int numAvailablePresentations = getAvailablePresentationsCount();
		return new ReplayPresentationPanel(this, getModel(), parent, SWT.NONE, numAvailablePresentations);
	}

	/***************************************************************************
	 * Makes an asterisk to appear in the title when the procedure is running
	 **************************************************************************/
	@Override
	public boolean isDirty()
	{
		return false;
	}

	/***************************************************************************
	 * Trigger the "save on close" event if the procedure is runnning
	 **************************************************************************/
	@Override
	public boolean isSaveOnCloseNeeded()
	{
		return false;
	}

	/***************************************************************************
	 * Update part name
	 **************************************************************************/
	@Override
	protected void updatePartName(ExecutorStatus status)
	{
		// Parse the ID. If there are several instances, show the
		// instance number in the part title
		String name = getProcName();
		if (status != ExecutorStatus.UNINIT)
		{
			String instanceNum = getProcId().substring(getProcId().indexOf("#") + 1);
			name += "(" + instanceNum + ") - REPLAY";
		}
		setPartName(name);
	}

	/***************************************************************************
	 * Update dependent command status
	 **************************************************************************/
	@Override
	public void updateDependentCommands()
	{
	}
}
