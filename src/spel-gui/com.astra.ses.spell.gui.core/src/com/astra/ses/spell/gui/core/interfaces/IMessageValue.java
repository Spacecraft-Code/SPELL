///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.interfaces
// 
// FILE      : IMessageValue.java
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

public interface IMessageValue
{
	// Generic sender ID (system operations)
	public static final String	GENERIC_SENDER	      = "GEN";
	public static final String	CLIENT_SENDER	      = "CLT";
	public static final String	CONTEXT_RECEIVER	  = "CTX";
	public static final String	LISTENER_RECEIVER	  = "LST";
	public static final String	GENERIC_RECEIVER	  = "GEN";

	// Basic data
	public static final String  DATA_TRUE             = "True";
	public static final String  DATA_FALSE            = "False";
	
	// ItemType messages / Field Data Type
	public static final String	ITEM_DATA_TYPE_LINE	  = "CURRENT_LINE";
	public static final String	ITEM_DATA_TYPE_ITEM	  = "ITEM";
	public static final String	ITEM_DATA_TYPE_STATUS	= "STATUS";
	public static final String	ITEM_DATA_TYPE_CODE	  = "CODE";
	// Item data types
	public static final String	NOTIF_TYPE_VERIF	  = "VERIFICATION";
	public static final String	NOTIF_TYPE_EXEC	      = "EXECUTION";
	public static final String	NOTIF_TYPE_SYS	      = "SYSTEM";
	public static final String	NOTIF_TYPE_VAL	      = "VALUE";

	// Special sources
	public static final String	MSG_SRC_UKN	          = "UKN";
	public static final String	MSG_SRC_SRV	          = "SPELL";
	public static final String	MSG_SRC_GUI	          = "GUI";

	// Context status
	public static final String	CTX_STATUS_AVAILABLE	= "AVAILABLE";
	public static final String	CTX_STATUS_RUNNING	  = "RUNNING";

	// Executor open mode
	public static final String	OPEN_MODE_AUTOMATIC	  = "Automatic";
	public static final String	OPEN_MODE_VISIBLE	  = "Visible";
	public static final String	OPEN_MODE_BLOCKING	  = "Blocking";

	public static final String	DATA_TYPE_NUM	      = "16";
	public static final String	DATA_TYPE_COMBO	      = "2048";

	public static final String	SEVERITY_INFO	      = "INFO";
	public static final String	SEVERITY_WARN	      = "WARN";
	public static final String	SEVERITY_ERROR	      = "ERROR";

	public static final String	WRITE_TYPE_DISPLAY	  = "DISPLAY";
	public static final String	WRITE_TYPE_LOG	      = "LOG";
	public static final String	WRITE_TYPE_DIALOG	  = "DIALOG";
	public static final String	WRITE_TYPE_SCRIPT	  = "SCRIPT";
}
