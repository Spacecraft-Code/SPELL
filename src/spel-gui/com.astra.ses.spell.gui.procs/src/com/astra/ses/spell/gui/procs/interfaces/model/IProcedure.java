////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.interfaces.model
// 
// FILE      : IProcedure.java
//
// DATE      : 2010-08-03
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
package com.astra.ses.spell.gui.procs.interfaces.model;

import org.eclipse.core.runtime.IAdaptable;

import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreProcedureRuntimeListener;
import com.astra.ses.spell.gui.core.model.types.ProcProperties;

/*******************************************************************************
 * 
 * IProcedure interface defines what a IProcedure implementing class can do.
 * Basically it defines methods for accessing procedure information and
 * controller
 * 
 ******************************************************************************/
public interface IProcedure extends IAdaptable
{

	/***************************************************************************
	 * Get this IProcedure object's identifier
	 * 
	 * @return the IProcedure identifier
	 **************************************************************************/
	public String getProcId();

	/***************************************************************************
	 * Get this IProcedure object's name
	 * 
	 * @return the IProcedure name
	 **************************************************************************/
	public String getProcName();

	/***************************************************************************
	 * Get the procedure's parent, if it has any
	 * 
	 * @return the parent, or null if this IProcedure does not have
	 **************************************************************************/
	public String getParent();

	/***************************************************************************
	 * Check if the procedure is a main one (no parent)
	 * 
	 * @return true if there is no parent
	 **************************************************************************/
	public boolean isMain();

	/***************************************************************************
	 * Get an IProcedure property whose key is the given one
	 * 
	 * @param propertyKey
	 *            the property key
	 * @return the property value if the given key exists. Otherwise null is
	 *         returned
	 **************************************************************************/
	public String getProperty(ProcProperties property);

	/***************************************************************************
	 * Get this procedure controller
	 * 
	 * @return the controller
	 **************************************************************************/
	public IProcedureController getController();

	/***************************************************************************
	 * Get the {@link ICoreProcedureRuntimeListener} object that will process
	 * runtime notifications
	 * 
	 * @return
	 **************************************************************************/
	public IProcedureRuntimeProcessor getRuntimeProcessor();

	/***************************************************************************
	 * Return a class which grants access to some of the procedure's properties
	 * 
	 * @return an {@link IExecutionInformation} instance giving information
	 *         about the current procedure status
	 **************************************************************************/
	public IExecutionInformation getRuntimeInformation();

	/***************************************************************************
	 * Return the manager of inter-procedure dependencies
	 **************************************************************************/
	public IDependenciesManager getDependenciesManager();

	/***************************************************************************
	 * Return this object's execution status manager
	 **************************************************************************/
	public IExecutionStatusManager getExecutionManager();

	/***************************************************************************
	 * Return this object's source code provider
	 **************************************************************************/
	public ISourceCodeProvider getSourceCodeProvider();

	/***************************************************************************
	 * Reset this procedure
	 **************************************************************************/
	public void reset();

	/***************************************************************************
	 * Enable or disable the replay mode
	 **************************************************************************/
	public void setReplayMode(boolean doingReplay);

	/***************************************************************************
	 * Check if it is in replay mode
	 **************************************************************************/
	public boolean isInReplayMode();

	/***************************************************************************
	 * Notify close of model
	 **************************************************************************/
	public void onClose();
}
