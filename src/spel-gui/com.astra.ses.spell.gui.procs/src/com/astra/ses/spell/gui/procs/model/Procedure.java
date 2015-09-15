////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : Procedure.java
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

import java.util.Map;

import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ProcProperties;
import com.astra.ses.spell.gui.procs.interfaces.model.IDependenciesManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformation;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformationHandler;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionStatusManager;
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
public class Procedure extends ProcedureBase 
{
	/** Execution information */
	private IExecutionInformationHandler m_executionInformation;
	/** Execution controller */
	private IProcedureController m_procedureController;
	/** Procedure runtime manager */
	private IExecutionStatusManager m_executionStatusManager;
	/** Source provider */
	private ISourceCodeProvider m_sourceCodeProvider;
	/** Holds the runtime processor */
	private IProcedureRuntimeProcessor m_runtimeProcessor;
	/** Holds the dependencies manager */
	private IDependenciesManager m_dependencies;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public Procedure(String instanceId, Map<ProcProperties, String> properties, ClientMode mode)
	{
		super(instanceId,properties);
		m_sourceCodeProvider = createSourceCodeProvider();
		m_procedureController = createProcedureController();
		m_executionInformation = createExecutionInformation(mode);
		m_executionStatusManager = createExecutionStatusManager();
		m_runtimeProcessor = createRuntimeProcessor();
		m_dependencies = createDependenciesManager();
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	protected ISourceCodeProvider createSourceCodeProvider()
	{
		return new SourceCodeProvider();
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	protected IProcedureController createProcedureController()
	{
		return new ProcedureController(this);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	protected IExecutionStatusManager createExecutionStatusManager()
	{
		return new ExecutionStatusManager(getProcId(),this);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	protected IExecutionInformationHandler createExecutionInformation( ClientMode mode )
	{
		return new ExecutionInformationHandler(mode,this);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	protected IProcedureRuntimeProcessor createRuntimeProcessor()
	{
		return new ProcedureRuntimeProcessor(this);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	protected IDependenciesManager createDependenciesManager()
	{
		return new DependenciesManager(this);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IProcedureController getController()
	{
		return m_procedureController;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IExecutionStatusManager getExecutionManager()
	{
		return m_executionStatusManager;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IProcedureRuntimeProcessor getRuntimeProcessor()
	{
		return m_runtimeProcessor;
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
	public IDependenciesManager getDependenciesManager()
	{
		return m_dependencies;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setReplayMode(boolean doingReplay)
	{
		m_executionStatusManager.setReplay(doingReplay);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean isInReplayMode()
	{
		return m_executionStatusManager.isInReplay();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void onClose()
	{
		m_executionStatusManager.dispose();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void reset()
	{
		m_executionStatusManager.reset();
		m_sourceCodeProvider.reset();
		m_executionInformation.reset();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public ISourceCodeProvider getSourceCodeProvider()
    {
	    return m_sourceCodeProvider;
    }
}
