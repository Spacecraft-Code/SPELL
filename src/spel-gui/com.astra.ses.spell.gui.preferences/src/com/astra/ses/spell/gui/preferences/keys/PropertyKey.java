///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.preferences.keys
// 
// FILE      : PropertyKey.java
//
// DATE      : 2010-05-27
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
package com.astra.ses.spell.gui.preferences.keys;


/***************************************************************************
 * 
 * General preferences keys
 * 
 **************************************************************************/
public enum PropertyKey
{
	APPLICATION_NAME("AppName", "SPELL GUI"), 
	STARTUP_CONNECT("ConnectAtStartup", "NO"), 
	INITIAL_SERVER("InitialServer", ""), 
	INITIAL_CONTEXT("InitialContext", ""), 
	RESPONSE_TIMEOUT("ResponseTimeout", "8000"), 
	OPEN_TIMEOUT("OpenTimeout", "10000"), 
	PROCS_EDITOR("ProceduresEditor", ""),
	ALTERNATE_PROCS_EDITOR("AlternateProceduresEditor",""),
	LAST_SERVER_CONNECTED("LastServerConnected", ""), 
	LAST_HOST_CONNECTED("LastHostConnected", ""), 
	LAST_PORT_CONNECTED("LastPortConnected", ""), 
	LAST_CONNECTION_MANUAL("LastConnectionManual", ""), 
	PROMPT_SOUND_FILE("PromptSoundFile", "120"), 
	PREFERENCES_ENABLED("PreferencesEnabled", "YES"),
	TDS_TIME_FORMAT("TdsTimeFormat", "-1"),
	GUI_ANCHOR_WINDOW("AnchorWindow", "NO"),
	/** ALL, NO, CHILDREN */
	AUTOMATIC_CLOSE("AutomaticClose", "NO"),
	/** YES, NO */
	SHOW_DATE_LINE("ShowDateLine","NO"),
	/** YES, NO */
	TEXT_TIMESTAMP("ShowTextTimestamp","NO"),
	/** NAME, VALUE, BOTH */
	DISPLAY_DATA("DisplayData", "BOTH"),
	LINE_HISTORY_ITEMS("LineHistoryItems", "-1"),
	TEXT_HISTORY_ITEMS("TextHistoryItems", "-1"),
	/** YES, NO */
	CONFIRM_ABORT("ConfirmAbort", "NO"),
	/** YES, NO, PROMPT */
	MULTIPLE_ATTACH("MultipleAttach", "PROMPT"),
	/** Use ASRUN when controlling: YES, NO, PROMPT */
	ASRUN_CONTROL("AsRunControl", "PROMPT"),
	/** Use ASRUN when monitoring: YES, NO, PROMPT */
	ASRUN_MONITOR("AsRunMonitor", "NO");

	/** Qualifier */
	private String	m_qualifier;
	/** Default hardcoded value */
	private String	m_hardcodedDefault;

	/***********************************************************************
	 * Constructor
	 * 
	 * @param qualifier
	 **********************************************************************/
	private PropertyKey(String qualifier, String defaultValue)
	{
		m_qualifier = qualifier;
		m_hardcodedDefault = defaultValue;
	}

	/***********************************************************************
	 * Return the qualified name for this property
	 ***********************************************************************/
	public String getPreferenceName()
	{
		return PreferenceCategory.GENERAL.tag + "." + m_qualifier;
	}

	/***********************************************************************
	 * Return the qualified name for this property
	 ***********************************************************************/
	public String getPropertyName()
	{
		return m_qualifier;
	}

	/***********************************************************************
	 * Return the hardcoded default value for this property
	 ***********************************************************************/
	public String getHardcodedDefault()
	{
		return m_hardcodedDefault;
	}
}
