////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : RemoteProcedure.java
//
// DATE      : 2010-07-30
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
////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.procs.model;

import java.util.HashMap;

import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.IExecutorInfo;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.server.ExecutorConfig;
import com.astra.ses.spell.gui.core.model.server.ExecutorInfo;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ProcProperties;
import com.astra.ses.spell.gui.procs.interfaces.model.IDependenciesManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformation;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformationHandler;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionStatusManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedureController;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedureRuntimeProcessor;
import com.astra.ses.spell.gui.procs.interfaces.model.ISourceCodeProvider;

/*******************************************************************************
 * 
 * Procedure holds the static and interactive procedure information: - Holds the
 * source code blocks to execute - Holds the runtime information that can be
 * handled by the user * Breakpoints, run into mode, step by step execution
 * 
 ******************************************************************************/
public class RemoteProcedure extends ProcedureBase
{
	/** Context proxy */
	private static IContextProxy s_proxy = null;

	/*
	 * Static block to retrieve the context proxy
	 */
	static
	{
		s_proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
	}

	/** Executor information */
	private IExecutionInformationHandler m_executionInformation;
	/** Fake controller for remote updates */
	private IProcedureController         m_controller;
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public RemoteProcedure(String instanceId)
	{
		super(instanceId,new HashMap<ProcProperties,String>());
		m_executionInformation = new ExecutionInformationHandler(ClientMode.UNKNOWN,this);
		m_controller = new RemoteController(this);
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public RemoteProcedure( IProcedure wasLocalProcedure )
	{
		super(wasLocalProcedure.getProcId(),new HashMap<ProcProperties,String>());
		for( ProcProperties prop : ProcProperties.values())
		{
			setProperty(prop, wasLocalProcedure.getProperty(prop));
		}
		m_controller = new RemoteController(this);
		ClientMode cmode = wasLocalProcedure.getRuntimeInformation().getClientMode();
		m_executionInformation = new ExecutionInformationHandler(cmode, this);
		IExecutorInfo info = (IExecutorInfo) wasLocalProcedure.getAdapter(ExecutorInfo.class);
		m_executionInformation.copyFrom(info);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	void updateInfoFromRemote() throws Exception
	{
		try
		{
			IExecutorInfo info =  s_proxy.getExecutorInfo(getProcId());
			m_executionInformation.copyFrom(info);
			ExecutorConfig cfg = new ExecutorConfig(getProcId());
			s_proxy.updateExecutorConfig(getProcId(), cfg);
			m_executionInformation.copyFrom(cfg);
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
	public void onClose()
	{
		// Nothing to do
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IProcedureController getController()
	{
		return m_controller;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IDependenciesManager getDependenciesManager()
	{
		return null;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IProcedureRuntimeProcessor getRuntimeProcessor()
	{
		return null;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IExecutionInformation getRuntimeInformation()
	{
		return m_executionInformation;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setReplayMode(boolean doingReplay) {};

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean isInReplayMode() { return false; };

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void reset() {}

	@Override
    public IExecutionStatusManager getExecutionManager()
    {
	    return null;
    }

	@Override
    public ISourceCodeProvider getSourceCodeProvider()
    {
	    return null;
    };
}
