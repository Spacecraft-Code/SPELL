///////////////////////////////////////////////////////////////////////////////
//
// (C) SES Engineering 2008
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.commands
// 
// FILE      : ExecutorCommand.java
//
// DATE      : 2008-11-21 13:54
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
 * {@link ExecutorCommand} specifies the actions that can be performed on a
 * procedure
 * 
 ******************************************************************************/
public enum ExecutorCommand
{
	ABORT("CMD_ABORT")
	{
		@Override
		public boolean validate( ExecutorStatus previousStatus, ExecutorStatus currentStatus )
		{
			boolean result = false;
			switch (currentStatus)
			{
			case RUNNING:
			case PAUSED:
			case PROMPT:
			case INTERRUPTED:
			case WAITING:
				result = true;
				break;
			}
			return result;
		}
	},
	ACTION("CMD_ACTION")
	{
		@Override
		public boolean validate( ExecutorStatus previousStatus, ExecutorStatus currentStatus )
		{
			boolean result = true;
			switch (currentStatus)
			{
			case ERROR:
			case ABORTED:
			case PROMPT:
			case FINISHED:
				result = false;
				break;
			default:
				break;
			}
			return result;
		}
	},
	GOTO("CMD_GOTO")
	{
		@Override
		public boolean validate( ExecutorStatus previousStatus, ExecutorStatus currentStatus )
		{
			return (currentStatus.equals(ExecutorStatus.PAUSED));
		}
	},
	PAUSE("CMD_PAUSE")
	{
		@Override
		public boolean validate( ExecutorStatus previousStatus, ExecutorStatus currentStatus )
		{
			boolean result = false;
			switch (currentStatus)
			{
			case RUNNING:
				result = true;
				break;
			case PROMPT:
			case WAITING:
				result = true;
				if (previousStatus != null)
				{
					if (previousStatus.equals(ExecutorStatus.PAUSED)) result = false;
					if (previousStatus.equals(ExecutorStatus.INTERRUPTED)) result = false;
				}
				break;
			}
			return result;
		}
	},
	INTERRUPT("CMD_INTERRUPT")
	{
		@Override
		public boolean validate( ExecutorStatus previousStatus, ExecutorStatus currentStatus )
		{
			boolean result = false;
			switch (currentStatus)
			{
			case RUNNING:
			// We allow the paused state to be able
			// to interrupt operations when stepping
			case PAUSED:
			case WAITING:
				result = true;
				break;
			}
			return result;
		}
	},
	RELOAD("CMD_RELOAD")
	{
		@Override
		public boolean validate( ExecutorStatus previousStatus, ExecutorStatus currentStatus )
		{
			boolean result = false;
			switch (currentStatus)
			{
			case ABORTED:
			case FINISHED:
			case ERROR:
				result = true;
				break;
			}
			return result;
		}
	},
	RECOVER("CMD_RECOVER")
	{
		@Override
		public boolean validate( ExecutorStatus previousStatus, ExecutorStatus currentStatus )
		{
			return (currentStatus.equals(ExecutorStatus.ERROR));
		}
	},
	RUN("CMD_RUN")
	{
		@Override
		public boolean validate( ExecutorStatus previousStatus, ExecutorStatus currentStatus )
		{
			return (currentStatus.equals(ExecutorStatus.PAUSED));
		}
	},
	SCRIPT("CMD_SCRIPT")
	{
		@Override
		public boolean validate( ExecutorStatus previousStatus, ExecutorStatus currentStatus )
		{
			boolean result = false;
			switch (currentStatus)
			{
			case INTERRUPTED:
			case PAUSED:
			case PROMPT:
			case WAITING:
				result = true;
				break;
			}
			return result;
		}
	},
	SKIP("CMD_SKIP")
	{
		@Override
		public boolean validate( ExecutorStatus previousStatus, ExecutorStatus currentStatus )
		{
			boolean result = false;
			switch (currentStatus)
			{
			case PAUSED:
			case INTERRUPTED:
				result = true;
				break;
			}
			return result;
		}
	},
	STEP("CMD_STEP")
	{
		@Override
		public boolean validate( ExecutorStatus previousStatus, ExecutorStatus currentStatus )
		{
			boolean result = false;
			switch (currentStatus)
			{
			case PAUSED:
			case INTERRUPTED:
				result = true;
				break;
			}
			return result;
		}
	},
	STEP_OVER("CMD_STEP_OVER")
	{
		@Override
		public boolean validate( ExecutorStatus previousStatus, ExecutorStatus currentStatus )
		{
			boolean result = false;
			switch (currentStatus)
			{
			case PAUSED:
			case INTERRUPTED:
				result = true;
				break;
			}
			return result;
		}
	};

	/** Message type */
	private String	m_msgId;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param msgId
	 **************************************************************************/
	private ExecutorCommand(String msgId)
	{
		m_msgId = msgId;
	}

	/***************************************************************************
	 * Get command id to release
	 * 
	 * @return
	 **************************************************************************/
	public String getId()
	{
		return m_msgId;
	}

	/***************************************************************************
	 * Determine if a command is valid for the given Executor status
	 * 
	 * @param st
	 *            the {@link ExecutorStatus} to validate over
	 * @return true if the command can be applied. false otherwise
	 **************************************************************************/
	public abstract boolean validate( ExecutorStatus previousStatus, ExecutorStatus currentStatus );
}
