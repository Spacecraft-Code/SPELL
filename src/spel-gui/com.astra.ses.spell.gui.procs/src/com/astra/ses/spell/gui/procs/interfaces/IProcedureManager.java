///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.interfaces
// 
// FILE      : IProcedureManager.java
//
// DATE      : 2008-11-24 08:34
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
package com.astra.ses.spell.gui.procs.interfaces;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.gui.core.exceptions.CommandFailed;
import com.astra.ses.spell.gui.core.interfaces.IService;
import com.astra.ses.spell.gui.core.model.server.ProcedureRecoveryInfo;
import com.astra.ses.spell.gui.core.model.types.ProcProperties;
import com.astra.ses.spell.gui.procs.exceptions.LoadFailed;
import com.astra.ses.spell.gui.procs.exceptions.NoSuchProcedure;
import com.astra.ses.spell.gui.procs.exceptions.UnloadFailed;
import com.astra.ses.spell.gui.procs.interfaces.model.AsRunReplayResult;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.types.ExecutorCommand;


/*******************************************************************************
 * @brief Manages procedure models.
 * @date 09/10/07
 ******************************************************************************/
public interface IProcedureManager extends IService 
{
	/***************************************************************************
	 * Check if we can do procedure operations
	 **************************************************************************/
	public boolean canOperate();

	/***************************************************************************
	 * Open a new procedure instance for the given procedure.
	 * 
	 * @param procedureId
	 *            The procedure identifier, no instance number info
	 * @throws LoadFailed
	 *             if the procedure could not be loaded
	 **************************************************************************/
	public void openProcedure(String procedureId, Map<String, String> arguments, IProgressMonitor monitor) throws LoadFailed;

	/***************************************************************************
	 * Open a new procedure instance for the given procedure. in background (headless)
	 * 
	 * @param procedureId
	 *            The procedure identifier, no instance number info
	 * @throws LoadFailed
	 *             if the procedure could not be loaded
	 **************************************************************************/
	public void backgroundProcedure(String procedureId, Map<String, String> arguments, IProgressMonitor monitor) throws LoadFailed;

	/***************************************************************************
	 * Add a model created by an external party programmatically.
	 **************************************************************************/
	public void addProcedure(String instanceId, IProcedure model);

	/***************************************************************************
	 * Remove a model created by an external party programmatically.
	 **************************************************************************/
	public void removeProcedure(String instanceId);

	/***************************************************************************
	 * Recover a procedure instance.
	 **************************************************************************/
	public void recoverProcedure(ProcedureRecoveryInfo procedure, IProgressMonitor monitor) throws LoadFailed;

	/***************************************************************************
	 * Attach an existing procedure instance in control mode.
	 * 
	 * @param instanceId
	 *            The procedure identifier, no instance number info
	 * @throws LoadFailed
	 *             if the procedure could not be attached
	 **************************************************************************/
	public void controlProcedure(String instanceId, AsRunReplayResult result, IProgressMonitor monitor) throws LoadFailed;

	/***************************************************************************
	 * Attach an existing procedure instance in monitoring mode.
	 * 
	 * @param instanceId
	 *            The procedure identifier, no instance number info
	 * @throws LoadFailed
	 *             if the procedure could not be attached
	 **************************************************************************/
	public void monitorProcedure(String instanceId, AsRunReplayResult result, IProgressMonitor monitor) throws LoadFailed;

	/***************************************************************************
	 * Schedule a new procedure instance for the given procedure.
	 * 
	 * @param procedureId
	 *            The procedure identifier, no instance number info
	 * @throws LoadFailed
	 *             if the procedure could not be loaded
	 **************************************************************************/
	public void scheduleProcedure(String procedureId, String condition, IProgressMonitor monitor) throws LoadFailed;

	/***************************************************************************
	 * Close a given procedure instance.
	 * 
	 * @param instanceId
	 *            The procedure identifier, WITH instance number info
	 * @throws UnloadFailed
	 *             if the procedure could not be unloaded
	 **************************************************************************/
	public void closeProcedure(String instanceId, IProgressMonitor monitor) throws UnloadFailed;

	/***************************************************************************
	 * Release a given procedure instance.
	 * 
	 * @param instanceId
	 *            The procedure identifier, WITH instance number info
	 * @throws UnloadFailed
	 *             if the procedure could not be unloaded
	 **************************************************************************/
	public void releaseProcedure(String instanceId, boolean background, IProgressMonitor monitor) throws UnloadFailed;

	/***************************************************************************
	 * Force removing the control of a procedure
	 * 
	 * @param instanceId
	 *            The procedure identifier, WITH instance number info
	 **************************************************************************/
	public void removeControl(String instanceId, IProgressMonitor monitor) throws UnloadFailed;

	/***************************************************************************
	 * Kill a given procedure instance.
	 * 
	 * @param instanceId
	 *            The procedure identifier, WITH instance number info
	 * @throws UnloadFailed
	 *             if the procedure could not be unloaded
	 **************************************************************************/
	public void killProcedure(String instanceId, IProgressMonitor monitor) throws UnloadFailed;

	/***************************************************************************
	 * Obtain a local procedure model.
	 * 
	 * @param instanceId
	 *            The instance identifier
	 * @return The procedure model
	 **************************************************************************/
	public IProcedure getProcedure(String instanceId) throws NoSuchProcedure;

	/***************************************************************************
	 * Check if a procedure is loaded.
	 * 
	 * @param instanceId
	 *            The instance identifier
	 * @return True if the model is loaded
	 **************************************************************************/
	public boolean isLocallyLoaded(String instanceId);

	/***************************************************************************
	 * Obtain a remote procedure model.
	 * 
	 * @param instanceId
	 *            The instance identifier
	 * @return The procedure reduced model
	 **************************************************************************/
	public IProcedure getRemoteProcedure(String instanceId) throws NoSuchProcedure;

	/***************************************************************************
	 * Obtain the list of available procedures in context
	 * 
	 * @return The procedure list
	 **************************************************************************/
	public Map<String, String> getAvailableProcedures( boolean refresh );

	/***************************************************************************
	 * Refresh the list of available procedures in context
	 * 
	 * @return The procedure list
	 **************************************************************************/
	public void refreshAvailableProcedures();

	/***************************************************************************
	 * 
	 **************************************************************************/
	public Set<String> getOpenLocalProcedures();

	/***************************************************************************
	 * 
	 **************************************************************************/
	public Set<String> getOpenRemoteProcedures( boolean refresh );

	/***************************************************************************
	 * Trigger a SPELL command
	 * 
	 * @param cmd
	 *            ExecutorCommand instance
	 * @param procId
	 *            The procedure identifier
	 **************************************************************************/
	public void issueCommand(ExecutorCommand cmd, String instanceId) throws CommandFailed, NoSuchProcedure;

	/***************************************************************************
	 * Request the procedure properties
	 **************************************************************************/
	public TreeMap<ProcProperties, String> getProcedureProperties(String procedureId);

	/**************************************************************************
	 * Get procedure name given its Id
	 * 
	 * @param procId
	 * @return
	 *************************************************************************/
	public String getProcedureName(String procId);

	/**************************************************************************
	 * Get a complete procedure id including leading folders if it is missing them
	 * 
	 * @param procId
	 * @return
	 *************************************************************************/
	public String getCompleteProcedureId( String procId );

	/**************************************************************************
	 * Check if a procedure identifier is available
	 * 
	 * @param procId
	 * @return
	 *************************************************************************/
	public boolean isProcedureIdAvailable( String procId );
}
