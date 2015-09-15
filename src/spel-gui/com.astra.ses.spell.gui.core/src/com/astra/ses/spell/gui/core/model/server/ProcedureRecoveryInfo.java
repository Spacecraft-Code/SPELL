///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.server
// 
// FILE      : ProcedureRecoveryInfo.java
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
package com.astra.ses.spell.gui.core.model.server;


/**
 * @author Rafael Chinchilla
 *
 */
public class ProcedureRecoveryInfo
{
	private String m_procedureFile;
	private String m_procedureName;
	private String m_procId;
	
	private String m_newInstanceId;
	private String m_originalInstanceId;
	
	private String m_timeId;
	private String m_recoveryDate;

	public ProcedureRecoveryInfo( String procFile )
	{
		this(procFile,null);
	}

	public ProcedureRecoveryInfo( String procFile, String procName )
	{
		int idx1 = procFile.indexOf("_Executor_");
		int idx2 = procFile.indexOf("#");
		int idx3 = procFile.indexOf(".", idx2);

		// Remove the extension and file index
		if (idx3 == -1)
		{
			idx3 = procFile.length();
		}

		m_procedureFile = procFile.substring(0,idx3);


		// Remove the time id, extension and file index
		m_originalInstanceId = procFile.substring(idx1+"_Executor_".length(), idx3);
		// Replace the double-underscore by slash for the ids
		m_originalInstanceId = m_originalInstanceId.replace("__","/");
		
		// Remove the time id, extension, instance number and file index
		m_procId = procFile.substring(idx1+"_Executor_".length(), idx2);
		// Replace the double-underscore by slash for the ids
		m_procId = m_procId.replace("__","/");
		
		m_procedureName = procName;
		m_timeId = procFile.substring(0, idx1);
    	m_recoveryDate = m_timeId.replace("_", " ");
    	m_recoveryDate = m_recoveryDate.substring(0,13) + ":" + m_recoveryDate.substring(13,15) + ":" + m_recoveryDate.substring(15,17);

		m_newInstanceId = null;
	}
	
	public String getFile()
	{
		return m_procedureFile;
	}

	public String getName()
	{
		return m_procedureName;
	}

	public void setName( String name )
	{
		m_procedureName = name;
	}

	public String getProcId()
	{
		return m_procId;
	}

	public String getTimeId()
	{
		return m_timeId;
	}

	public String getOriginalInstanceId()
	{
		return m_originalInstanceId;
	}

	public String getNewInstanceId()
	{
		return m_newInstanceId;
	}

	public void setNewInstanceId( String id )
	{
		m_newInstanceId = id;
	}
	
	public String getRecoveryDate()
	{
		return m_recoveryDate;
	}
}
