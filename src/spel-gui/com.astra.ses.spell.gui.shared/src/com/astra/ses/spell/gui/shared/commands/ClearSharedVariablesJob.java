///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.shared.commands
// 
// FILE      : ClearSharedVariablesJob.java
//
// DATE      : Oct 30, 2013
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
package com.astra.ses.spell.gui.shared.commands;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.shared.services.ISharedDataService;
import com.astra.ses.spell.gui.shared.services.ISharedScope;
import com.astra.ses.spell.gui.shared.views.controls.SharedVariable;

public class ClearSharedVariablesJob implements IRunnableWithProgress
{
	public CommandResult result; 
	public String message;
	private ISharedScope m_scope;
	private List<SharedVariable> m_variables;

	public ClearSharedVariablesJob()
	{
		result = CommandResult.NO_EFFECT;
		message = "";
		m_scope = null;
		m_variables = null;
	}

	public ClearSharedVariablesJob( ISharedScope scope )
	{
		result = CommandResult.NO_EFFECT;
		message = "";
		m_scope = scope;
		m_variables = null;
	}

	public ClearSharedVariablesJob( ISharedScope scope, List<SharedVariable> vars )
	{
		result = CommandResult.NO_EFFECT;
		message = "";
		m_scope = scope;
		m_variables = vars;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
	{
		try
		{
			result = CommandResult.SUCCESS;
			if (m_scope == null && m_variables == null)
			{
				monitor.setTaskName("Clearing all variables and scopes");
				ISharedDataService svc = (ISharedDataService) ServiceManager.get(ISharedDataService.class);
				for(String scope : svc.getSharedScopes())
				{
					svc.removeSharedScope(scope);
				}
				svc.getSharedScope(ISharedDataService.GLOBAL_SCOPE).clear();
			}
			else if (m_variables != null)
			{
				monitor.beginTask("Clearing variables in scope " + m_scope.getScopeName(), m_variables.size());
				for(SharedVariable var : m_variables)
				{
					monitor.subTask("Removing variable " + var.name);
					m_scope.clear(var.name);
					monitor.worked(1);
					if (monitor.isCanceled()) return;
				}
			}
			else
			{
				monitor.setTaskName("Clearing variables in scope " + m_scope.getScopeName());
				m_scope.clear();
			}
			monitor.done();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			message = ex.getLocalizedMessage();
			result = CommandResult.FAILED;
		}
	}
}
