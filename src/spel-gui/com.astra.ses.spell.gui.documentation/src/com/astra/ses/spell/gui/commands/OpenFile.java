////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.commands
// 
// FILE      : OpenFile.java
//
// DATE      : Nov 19, 2010 10:13:18 AM
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
package com.astra.ses.spell.gui.commands;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Shell;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.Environment;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;

/*******************************************************************************
 * 
 * OpenFile handler opens the file whose absolute path is given as a command
 * parameter
 ******************************************************************************/
public class OpenFile extends AbstractHandler
{

	/** File path parameter id */
	private static final String	PARAM_PATH	= "filepath";

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 * =========================================================================
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		String filePath = event.getParameter(PARAM_PATH);

		/*
		 * Replace possible environment variables
		 */
		for (Environment var : Environment.values())
		{
			String pattern = "$" + var.toString() + "$";
			if (filePath.contains(pattern))
			{
				IConfigurationManager cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
				String value = cfg.getEnvironmentVariable(var);
				filePath = filePath.replace(pattern, value);
			}
		}

		CommandResult cmdRes = CommandResult.FAILED;
		if (new File(filePath).exists())
		{
			boolean result = Program.launch(filePath);
			cmdRes = result ? CommandResult.SUCCESS : CommandResult.FAILED;
			if (!result)
			{
				MessageDialog.openError(new Shell(), "Error while opening the file", "Cannot open file " + filePath
				        + ".\n" + "Check there is a tool for viewing the manual installed in the workstation");
			}
		}
		else
		{
			MessageDialog.openError(new Shell(), "File does not exist", "Cannot open file " + filePath);
		}

		return cmdRes;
	}

}
