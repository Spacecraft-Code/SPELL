///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.interfaces
// 
// FILE      : IMessageId.java
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

public interface IMessageId
{
	// /////////////////////////////////////////////////////////////////////////
	// SPELL Context Messages
	// /////////////////////////////////////////////////////////////////////////

	/** Oneway messages */
	public final String	MSG_CLOSE	         = "MSG_CLOSE_CTX";
	public final String	MSG_CLIENT_OP	     = "MSG_CLIENT_OP";
	public final String	MSG_EXEC_OP	         = "MSG_EXEC_OP";
	public final String	MSG_EXEC_CONFIG      = "MSG_EXEC_CONFIG";
	public final String	MSG_CONTEXT_OP	     = "MSG_CONTEXT_OP";
	public final String	MSG_CANCEL	         = "MSG_CANCEL";
	public final String	MSG_UNKNOWN	         = "MSG_UNKNOWN";
	public final String	MSG_PING	         = "MSG_PING";
	public final String	MSG_PROMPT_START	 = "MSG_PROMPT_START";
	public final String	MSG_PROMPT_END	     = "MSG_PROMPT_END";
	public final String	MSG_PROMPT_ANSWER    = "MSG_ANSWER";

	public final String	MSG_SETUACTION	     = "MSG_SET_UACTION";
	public final String	MSG_DISABLEUACTION	 = "MSG_DISABLE_UACTION";
	public final String	MSG_ENABLEUACTION	 = "MSG_ENABLE_UACTION";
	public final String	MSG_DISMISSUACTION	 = "MSG_DISMISS_UACTION";

	public final String	MSG_SHOWNODEDEPTH	 = "MSG_SHOW_NODE_DEPTH";

	// Nasty workarround, there is a bug in python subprocess.Popen and sockets
	public final String	MSG_LISTENER_LOST	 = "MSG_LISTENER_LOST";
	public final String	MSG_CONTEXT_LOST	 = "MSG_CONTEXT_LOST";

	/** Identifier for proc code request */
	public final String	REQ_PROC_CODE	     = "REQ_PROC_CODE";
	/** Identifier for proc list request */
	public final String	REQ_PROC_LIST	     = "REQ_PROC_LIST";
	/** Identifier for procedure properties request */
	public final String	REQ_PROC_PROP	     = "REQ_PROC_PROP";

	public final String	REQ_GUI_LOGIN	     = "REQ_GUI_LOGIN";
	public final String	REQ_GUI_LOGOUT	     = "REQ_GUI_LOGOUT";
	
	/** Identifier for create executor request */
	public final String	REQ_OPEN_EXEC	     = "REQ_OPEN_EXEC";
	/** Identifier for closing executor request */
	public final String	REQ_CLOSE_EXEC	     = "REQ_CLOSE_EXEC";
	/** Identifier for killing executor request */
	public final String	REQ_KILL_EXEC	     = "REQ_KILL_EXEC";
	/** Identifier for attach to executor request */
	public final String	REQ_ATTACH_EXEC	     = "REQ_ATTACH_EXEC";
	/** Identifier for executor list request */
	public final String	REQ_EXEC_LIST	     = "REQ_EXEC_LIST";
	/** Identifier for detach from executor request */
	public final String	REQ_DETACH_EXEC	     = "REQ_DETACH_EXEC";
	/** Identifier for remove control request */
	public final String	REQ_REMOVE_CONTROL   = "REQ_REMOVE_CONTROL";
	/** Identifier for setting a procedure in background mode */
	public final String	REQ_SET_BACKGROUND   = "REQ_SET_BACKGROUND";
	/** Identifier for getting executor info */
	public final String	REQ_EXEC_INFO	     = "REQ_EXEC_INFO";
	/** Identifier for getting executor status */
	public final String	REQ_EXEC_STATUS      = "REQ_EXEC_STATUS";
	/** Identifier for getting client info */
	public final String	REQ_CLIENT_INFO	     = "REQ_CLIENT_INFO";
	/** Identifier for getting server file path */
	public final String	REQ_SERVER_FILE_PATH = "REQ_SERVER_FILE_PATH";
	/** Identifier for getting a procedure id */
	public final String	REQ_INSTANCE_ID	     = "REQ_INSTANCE_ID";
	/** Identifier for setting executor configuration */
	public final String	REQ_SET_CONFIG	     = "REQ_SET_CONFIG";
	/** Identifier for getting executor configuration */
	public final String	REQ_GET_CONFIG	     = "REQ_GET_CONFIG";
	/** Identfiier for toggling a breakpoint */
	public final String	REQ_SET_BREAKPOINT	 = "REQ_SET_BREAKPOINT";
	/** Identifier for removing all breakpoints in the code */
	public final String	REQ_CLEAR_BREAKPOINT	= "REQ_CLEAR_BREAKPOINT";
	public final String	REQ_RECOVERY_LIST	 = "REQ_RECOVERY_LIST";
	public final String	REQ_RECOVER_EXEC 	 = "REQ_RECOVER_EXEC";
	public final String	REQ_DELETE_FILE      = "REQ_DELETE_FILE";
	public final String	REQ_DELETE_RECOVERY  = "REQ_DELETE_RECOVERY";
	public final String	REQ_DUMP_INTERPRETER = "REQ_DUMP_INTERPRETER";
	public final String	REQ_SAVE_STATE       = "REQ_SAVE_STATE";
	public final String	REQ_ASRUN_LIST	     = "REQ_ASRUN_LIST";

	public final String	REQ_GET_DICTIONARY = "REQ_GET_DICTIONARY";
	public final String	REQ_UPD_DICTIONARY = "REQ_UPD_DICTIONARY";
	public final String	REQ_SAV_DICTIONARY = "REQ_SAV_DICTIONARY";

	public final String	REQ_LIST_DATADIRS = "REQ_LIST_DATADIRS";
	public final String	RSP_LIST_DATADIRS = "RSP_LIST_DATADIRS";

	public final String	REQ_LIST_FILES = "REQ_LIST_FILES";
	public final String	RSP_LIST_FILES = "RSP_LIST_FILES";

	public final String	REQ_INPUT_FILE = "REQ_INPUT_FILE";
	public final String	RSP_INPUT_FILE = "RSP_INPUT_FILE";

	public final String REQ_CURRENT_TIME = "REQ_CURRENT_TIME";	
	
	// /////////////////////////////////////////////////////////////////////////
	// SPELL Listener Messages
	// /////////////////////////////////////////////////////////////////////////

	/** Identifier for create context request */
	public final String	REQ_OPEN_CTX	     = "REQ_OPEN_CTX";
	/** Identifier for destroy context request */
	public final String	REQ_CLOSE_CTX	     = "REQ_CLOSE_CTX";
	/** Identifier for attach to ctx request */
	public final String	REQ_ATTACH_CTX	     = "REQ_ATTACH_CTX";
	/** Identifier for ctx list request */
	public final String	REQ_CTX_LIST	     = "REQ_CTX_LIST";
	/** Identifier for detach from ctx request */
	public final String	REQ_DETACH_CTX	     = "REQ_DETACH_CTX";
	/** Identifier for destroy ctx request */
	public final String	REQ_DESTROY_CTX	     = "REQ_DESTROY_CTX";
	/** Identifier for getting ctx info */
	public final String	REQ_CTX_INFO	     = "REQ_CTX_INFO";

	/** Request Context Executor Defaults */
	public final String REQ_GET_CTX_EXEC_DFLT = "REQ_GET_CTX_EXEC_DFLT";
	public final String RSP_GET_CTX_EXEC_DFLT = "RSP_GET_CTX_EXEC_DFLT";
	
	/** Set Context Executor Defaults */
	public final String REQ_SET_CTX_EXEC_DFLT = "REQ_SET_CTX_EXEC_DFLT";
	public final String RSP_SET_CTX_EXEC_DFLT = "RSP_SET_CTX_EXEC_DFLT";
	
}
