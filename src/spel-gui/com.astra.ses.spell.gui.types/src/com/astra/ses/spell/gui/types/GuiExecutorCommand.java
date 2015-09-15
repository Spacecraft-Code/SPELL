///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.actions
// 
// FILE      : GuiExecutorCommand.java
//
// DATE      : 2010-08-26
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
package com.astra.ses.spell.gui.types;


/*******************************************************************************
 * 
 * {@link GuiExecutorCommand} model how {@link ExecutorCommand} instances over
 * the procedure may be presented to the user
 * 
 ******************************************************************************/
public enum GuiExecutorCommand
{

	RUN(ExecutorCommand.RUN, CmdConstants.CMDRUN_HANDLER, "Run", "Run the given procedure","icons/16x16/run.png", 0, true),
	STEP(ExecutorCommand.STEP, CmdConstants.CMDSTEP_HANDLER,"Step", "Step into one statement", "icons/16x16/step.png", 1, true), 
	STEP_OVER(ExecutorCommand.STEP_OVER, CmdConstants.CMDSTEPOVER_HANDLER, "S. Over", "Step over one statement", "icons/16x16/step.png", 2, true),
	SKIP(ExecutorCommand.SKIP, CmdConstants.CMDSKIP_HANDLER, "Skip", "Skip one statement","icons/16x16/skip.png", 3, true),
	PAUSE(ExecutorCommand.PAUSE, CmdConstants.CMDPAUSE_HANDLER,"Pause", "Pause execution", "icons/16x16/pause.png", 4, true),
	INTERRUPT(ExecutorCommand.INTERRUPT, CmdConstants.CMDINTERRUPT_HANDLER,"Interrupt", "Interrupt driver operation", "icons/16x16/pause.png", 5, true),
	GOTO(ExecutorCommand.GOTO, CmdConstants.CMDGOTO_HANDLER, "Goto","Goto a given label in the procedure", "icons/16x16/goto.png", 6, true),
	RELOAD(ExecutorCommand.RELOAD, CmdConstants.CMDRELOAD_HANDLER, "Reload", "Reload procedure","icons/16x16/reload.png", 7, true),
	ABORT(ExecutorCommand.ABORT, CmdConstants.CMDABORT_HANDLER, "Abort", "Abort the execution","icons/16x16/abort.png", 8, true), 
	RECOVER(ExecutorCommand.RECOVER, CmdConstants.CMDRECOVER_HANDLER, "Recover", "Recover from failure", "icons/16x16/reload.png", 9, true);

	/** Command identifier */
	public ExecutorCommand	command;
	/** Eclipse command id */
	public String	       handler;
	/** Action label */
	public String	       label;
	/** Description */
	public String	       description;
	/** Image path */
	public String	       imagePath;
	/** Position */
	public Integer	       pos;
	/** Flag that command is in use */
	public Boolean	       used;

	/***************************************************************************
	 * 
	 * @param command
	 * @param label
	 * @param descritpion
	 * @param path
	 **************************************************************************/
	private GuiExecutorCommand(ExecutorCommand command, String handler,
	        String label, String description, String path, Integer pos, Boolean used)
	{
		this.command = command;
		this.handler = handler;
		this.label = label;
		this.description = description;
		this.imagePath = path;
		this.pos = pos;
		this.used = used;
	}
	
	/**
	 * Extract command by identifier
	 * 
	 * @param id
	 * @return
	 */
	public static GuiExecutorCommand getCommandById(String id)
	{
		for (GuiExecutorCommand command : GuiExecutorCommand.values())
		{
			if ( command.command.getId().equals(id) )
			{
				return command;
			}
		}
		return null;
	}
}
