///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.commands
//
// FILE      : CmdGoto.java
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
// EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, THE PROGRAM IS PROVclassED
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
// CONTRIBUTORS SHALL HAVE ANY LIABILITY FOR ANY DIRECT, INDIRECT, INCclassENTAL,
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

import java.util.HashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.dialogs.GotoDialog;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;

public class CmdGoto extends AbstractHandler
{

	/** Command id */
	public static final String	ID	       = "com.astra.ses.spell.gui.commands.Goto";

	/** Proc id command argument */
	public static final String	ARG_PROCID	= "procId";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		GotoDialog dialog = new GotoDialog(Display.getCurrent()
		        .getActiveShell());
		Object result = CommandResult.NO_EFFECT;
		// Process the returned value
		if (dialog.open() == Window.OK)
		{
			String target = dialog.getTarget();
			if (target != null && (!target.isEmpty()))
			{
				if (dialog.isLabel())
				{
					HashMap<String, String> args = new HashMap<String, String>();
					args.put(CmdGotoLabel.ARG_PROCID,
					        event.getParameter(ARG_PROCID));
					args.put(CmdGotoLabel.ARG_LABEL, target);
					result = CommandHelper.execute(CmdGotoLabel.ID, args);
				}
				else
				{
					HashMap<String, String> args = new HashMap<String, String>();
					args.put(CmdGotoLine.ARG_PROCID,
					        event.getParameter(ARG_PROCID));
					args.put(CmdGotoLine.ARG_LINENO, target);
					result = CommandHelper.execute(CmdGotoLine.ID, args);
				}
			}
		}
		return result;
	}

}
