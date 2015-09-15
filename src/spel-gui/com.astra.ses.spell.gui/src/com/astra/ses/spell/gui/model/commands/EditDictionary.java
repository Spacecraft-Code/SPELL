///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.commands
// 
// FILE      : EditDictionary.java
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
package com.astra.ses.spell.gui.model.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.astra.ses.spell.gui.core.exceptions.ContextError;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.dialogs.DictionaryEditorDialog;
import com.astra.ses.spell.gui.dialogs.DictionaryNameDialog;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.services.IRuntimeSettings;
import com.astra.ses.spell.gui.services.IRuntimeSettings.RuntimeProperty;

public class EditDictionary extends AbstractHandler
{
	public static final String	ID	= "com.astra.ses.spell.gui.commands.EditDictionary";

	/** Dictionary name argument */
	public static final String	ARG_DICTNAME	= "com.astra.ses.spell.gui.commands.EditDictionary.dict";
	/** Can mnerge files argument */
	public static final String	ARG_CANMERGE	= "com.astra.ses.spell.gui.commands.EditDictionary.canmerge";

	/***************************************************************************
	 * The constructor.
	 **************************************************************************/
	public EditDictionary()
	{
	}

	/***************************************************************************
	 * The command has been executed, so extract extract the needed information
	 * from the application context.
	 **************************************************************************/
	public CommandResult execute(ExecutionEvent event)
	        throws ExecutionException
	{
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		IRuntimeSettings svc = (IRuntimeSettings) ServiceManager.get(IRuntimeSettings.class);
		
		String procId = (String) svc.getRuntimeProperty(RuntimeProperty.ID_PROCEDURE_SELECTION);
		String dictname = event.getParameter(ARG_DICTNAME);
		String canMergeStr = event.getParameter(ARG_CANMERGE);
		boolean canMerge = false;
		if (canMergeStr != null)
		{
			canMerge = canMergeStr.equals("true");
		}

		if (dictname == null)
		{
			DictionaryNameDialog dialog = new DictionaryNameDialog(window.getShell());
			if (dialog.open() == IDialogConstants.OK_ID )
			{
				dictname = dialog.getSelectedDictionary();
			}
		}
		
		if (dictname != null)
		{
			IProcedureManager pmgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
			IProcedure proc = pmgr.getProcedure(procId);
			try
			{
				DictionaryEditorDialog dialog = new DictionaryEditorDialog(window.getShell(), proc, dictname, canMerge );
				dialog.open();
			}
			catch (ContextError ex)
			{
			    MessageDialog.openWarning(window.getShell(), "Error retrieving dictionary",ex.getMessage());
			    return CommandResult.FAILED;
			}
		}
		else
		{
			return CommandResult.CANCELLED;
		}
		
		return CommandResult.SUCCESS;
	}
}
