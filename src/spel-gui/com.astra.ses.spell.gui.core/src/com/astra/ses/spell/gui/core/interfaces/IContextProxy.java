///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.interfaces
// 
// FILE      : IContextProxy.java
//
// DATE      : 2008-11-21 08:58
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
package com.astra.ses.spell.gui.core.interfaces;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.gui.core.comm.messages.SPELLlistenerLost;
import com.astra.ses.spell.gui.core.exceptions.ContextError;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.InputData;
import com.astra.ses.spell.gui.core.model.server.ClientInfo;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ExecutorConfig;
import com.astra.ses.spell.gui.core.model.server.ExecutorDefaults;
import com.astra.ses.spell.gui.core.model.server.ProcedureRecoveryInfo;
import com.astra.ses.spell.gui.core.model.types.BreakpointType;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.DataContainer;
import com.astra.ses.spell.gui.core.model.types.ProcProperties;
import com.astra.ses.spell.gui.types.ExecutorCommand;
import com.astra.ses.spell.gui.types.ExecutorStatus;



public interface IContextProxy extends IBaseProxy
{
	/***************************************************************************
	 * Attach to context
	 **************************************************************************/
	public void attach(ContextInfo ctxInfo) throws Exception;

	/***************************************************************************
	 * Close a context on the server
	 * 
	 * @param ctxName
	 *            The context name
	 **************************************************************************/
	public void close();

	/***************************************************************************
	 * Set the timeout for opening executors in msecs
	 **************************************************************************/
	public void setOpenTimeout(long timeout);

	/***************************************************************************
	 * Obtain the currently selected context
	 * 
	 * @return The current context
	 **************************************************************************/
	public String getCurrentContext();

	/***************************************************************************
	 * Obtain GUI key
	 **************************************************************************/
	public String getClientKey();

	/***************************************************************************
	 * Obtain the current context information
	 **************************************************************************/
	public ContextInfo getInfo();

	/***************************************************************************
	 * Obtain the current time as seen by the context
	 **************************************************************************/
	public Date getCurrentTime();
	
	/***************************************************************************
	 * Launch a new executor in the context
	 * 
	 * @param procedureId
	 *            The procedure identifier
	 * @return Executor status information
	 **************************************************************************/
	public void openExecutor(String procedureId, String condition, Map<String, String> arguments, boolean background ) throws ContextError;

	/***************************************************************************
	 * Launch a new executor in the context
	 * 
	 * @param procedureId
	 *            The procedure identifier
	 * @return Executor status information
	 **************************************************************************/
	public void recoverExecutor( ProcedureRecoveryInfo procedure ) throws ContextError;
	
	/***************************************************************************
	 * Obtain an available ID for a procedure
	 **************************************************************************/
	public String getProcedureInstanceId(String procedureId);

	/***************************************************************************
	 * Close the given executor process
	 * 
	 * @param procedureId
	 *            The procedure (executor) identifier
	 * @return True if success
	 **************************************************************************/
	public boolean closeExecutor(String procedureId);

	/***************************************************************************
	 * Kill the given executor process
	 * 
	 * @param procedureId
	 *            The procedure (executor) identifier
	 * @return True if success
	 **************************************************************************/
	public boolean killExecutor(String procedureId);

	/***************************************************************************
	 * Attach to the given executor
	 * 
	 * @param procId
	 *            Procedure (executor) identifier
	 * @return True if success
	 **************************************************************************/
	public IExecutorInfo attachToExecutor(String procId, ClientMode mode);

	/***************************************************************************
	 * Detach from the given executor
	 * 
	 * @param procId
	 *            Procedure (executor) identifier
	 * @return True if success
	 **************************************************************************/
	public boolean detachFromExecutor(String procId, boolean background);

	/***************************************************************************
	 * Remove the controlling client of a given executor
	 * 
	 * @param procId
	 *            Procedure (executor) identifier
	 * @return True if success
	 **************************************************************************/
	public boolean removeControl(String procId);

	/***************************************************************************
	 * Obtain the list of running executors
	 * 
	 * @return Executor name list
	 **************************************************************************/
	public List<String> getAvailableExecutors();

	/***************************************************************************
	 * Obtain executor information
	 * 
	 * @param procId
	 *            Procedure (executor) identifier
	 * @return Executor details
	 **************************************************************************/
	public IExecutorInfo getExecutorInfo(String procId) throws Exception;

	/***************************************************************************
	 * Obtain executor status
	 * 
	 * @param procId
	 *            Procedure (executor) identifier
	 * @return Executor details
	 **************************************************************************/
	public ExecutorStatus getExecutorStatus(String procId) throws Exception;

	/***************************************************************************
	 * Obtain executor prompt information
	 * 
	 * @param procId
	 *            Procedure (executor) identifier
	 * @return Prompt details
	 **************************************************************************/
	public InputData getExecutorPromptData(String procId) throws Exception;

	/***************************************************************************
	 * Obtain executor call stack information
	 * 
	 * @param procId
	 *            Procedure (executor) identifier
	 * @return CSP
	 **************************************************************************/
	public String getExecutorCallstack(String procId) throws Exception;

	/***************************************************************************
	 * Obtain other client information
	 * 
	 * @param client
	 *            key Client identifier
	 * @return Client details
	 **************************************************************************/
	public ClientInfo getClientInfo(String clientKey);

	/***************************************************************************
	 * Obtain the list of available procedures for a given profile
	 * 
	 * @return A list of procedure identifiers
	 **************************************************************************/
	public Map<String, String> getAvailableProcedures( boolean refresh );

	/***************************************************************************
	 * Obtain the procedure code
	 * 
	 * @param procedureId
	 *            The procedure identifier
	 * @return The procedure source lines
	 **************************************************************************/
	public List<String> getProcedureCode(String procedureId, IProgressMonitor monitor );

	/***************************************************************************
	 * Obtain the given procedure properties
	 * 
	 * @param procedureId
	 *            The procedure identifier
	 * @return A map with the procedure properties
	 **************************************************************************/
	public TreeMap<ProcProperties, String> getProcedureProperties( String procedureId);

	/***************************************************************************
	 * Send a predefined command
	 * 
	 * @param cmd
	 *            ExecutorCommand identifier
	 **************************************************************************/
	public void command(ExecutorCommand cmd, String[] args);

	/***************************************************************************
	 * Answer an open prompt (or cancel it)
	 **************************************************************************/
	public void answerPrompt( InputData promptData );

	/***************************************************************************
	 * Change executor configuration
	 * 
	 * @param procId
	 *            Procedure identifier
	 * @param config
	 *            Configuration map
	 **************************************************************************/
	public void setExecutorConfiguration(String procId, ExecutorConfig config);

	/***************************************************************************
	 * Obtain executor configuration
	 * 
	 * @param procId
	 *            Procedure (executor) identifier
	 **************************************************************************/
	public void updateExecutorConfig(String procId, ExecutorConfig config) throws Exception;

	
	/***************************************************************************
	 * Obtain context executor defaults
	 * 
	 * @param defaults
	 *            Executor defaults object to be returned
	 * @return Executor defauls
	 * @throws Exception 
	 **************************************************************************/	
	public void getExecutorDefaults(ExecutorDefaults defaults) throws Exception;	

	/***************************************************************************
	 * Update executor defaults on context
	 * 
	 * @param procId
	 *            Procedure identifier
	 * @param config
	 *            Configuration map
	 **************************************************************************/	
	void setExecutorDefaults(Map<String, String> defaults);	
	
	/***************************************************************************
	 * Toggle a breakpoint at the given line for the given procedure (its id)
	 * 
	 * @param procId
	 * @param lineNo
	 * @throws Exception
	 **************************************************************************/
	public void toggleBreakpoint(String procId, String codeId, int lineNo,
	        BreakpointType type) throws Exception;

	/***************************************************************************
	 * A request for removing all the breakpoints for the given proc is sent
	 * 
	 * @param procId
	 *            the procedure's id
	 * @throws Exception
	 **************************************************************************/
	public void clearBreakpoints(String procId) throws Exception;

	/***************************************************************************
	 * Obtain the contents of an input file
	 * @return the data container with keys and values
	 **************************************************************************/
	public DataContainer getInputFile( String path, IProgressMonitor monitor );

	/***************************************************************************
	 * Save a data container from the procedure to a file
	 * 
	 * @param procId
	 *            the procedure's id
	 * @param name 
	 * 			  name of the container
	 * @param path 
	 * 			  path of the target file
	 **************************************************************************/
	public void saveDataContainer(String procId, String name, String path );

	/***************************************************************************
	 * Retrieve the given data container from a procedure
	 * 
	 * @param procId
	 *            the procedure's id
	 * @param name 
	 * 			  name of the container
	 **************************************************************************/
	public DataContainer getDataContainer(String procId, String name, IProgressMonitor monitor );

	/***************************************************************************
	 * Update the given data container on a procedure
	 * 
	 * @param procId
	 *            the procedure's id
	 * @param container
	 * 			  the container
	 **************************************************************************/
	public void updateDataContainer(String procId, DataContainer container, boolean mergeNew, IProgressMonitor monitor ) throws Exception;
	
	/***************************************************************************
	 * Indicate the server that the visible on-execution node has been
	 * explicitly changed by the user
	 * 
	 * @param procId
	 *            the procId
	 * @param depth
	 *            the depth relative to the root node
	 **************************************************************************/
	public void viewNodeAtDepth(String procId, int depth);

	/***************************************************************************
	 * Detect connection lost
	 **************************************************************************/
	public boolean listenerConnectionLost(SPELLlistenerLost error);

    /***************************************************************************
     * Force the implementation of the comm listener interface
     **************************************************************************/
    public void connectionFailed(ErrorData data);

    /***************************************************************************
     * Force the implementation of the comm listener interface
     **************************************************************************/
    public void connectionLost(ErrorData data);
}
