///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.interfaces.model
// 
// FILE      : Dependency.java
//
// DATE      : Jun 6, 2013
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
package com.astra.ses.spell.gui.procs.interfaces.model;


public class Dependency
{
	private String m_procedureId;
	private String m_procedureName;
	private int m_lineNo;
	private String m_instanceId;
	private ChildStatus m_status;
	
	public enum ChildStatus
	{
		NEVER_EXECUTED,
		ACTIVE,
		FINISHED
	}
	
	public Dependency( String procedureId, String procedureName, int lineNo )
	{
		m_lineNo = lineNo;
		m_procedureId = procedureId;
		m_procedureName = procedureName;
		m_instanceId = null;
		m_status = ChildStatus.NEVER_EXECUTED;
	}
	
	public ChildStatus getStatus()
	{
		return m_status;
	}

	public String getProcedureId()
	{
		return m_procedureId;
	}

	public String getProcedureName()
	{
		return m_procedureName;
	}

	public String getInstanceId()
	{
		return m_instanceId;
	}

	public void setChildStarted( String id )
	{
		m_instanceId = id;
		m_status = ChildStatus.ACTIVE;
	}

	public void setChildFinished( String id )
	{
		m_status = ChildStatus.FINISHED;
	}

	public int getLineNo()
	{
		return m_lineNo;
	}

	public String toString()
	{
		return "[PID:" + m_procedureId + " IID:" + m_instanceId + " LINE:" + m_lineNo + "]";
	} 
}
