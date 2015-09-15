///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.watchvariables.notification
// 
// FILE      : VariableNotification.java
//
// DATE      : Nov 28, 2011
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
package com.astra.ses.spell.gui.watchvariables.notification;

import java.util.ArrayList;

import com.astra.ses.spell.gui.core.model.notification.NotificationData;

/*******************************************************************************
 * @brief Data structure used for scope change notifications.
 * @date 25/10/07
 ******************************************************************************/
public class VariableNotification extends NotificationData
{
	private ArrayList<VariableData>	m_addedVariables	= new ArrayList<VariableData>();
	private ArrayList<VariableData>	m_changedVariables	= new ArrayList<VariableData>();
	private ArrayList<VariableData>	m_deletedVariables	= new ArrayList<VariableData>();
	private String m_scopeName = "";
	
	/***************************************************************************
	 * Constructor
	 * 
	 * @param type
	 *            The type of the notification
	 * @param stack
	 *            The stack position
	 ***************************************************************************/
	public VariableNotification(String procId)
	{
		super(procId, "");
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public void addChangedVariable(VariableData data)
	{
		m_changedVariables.add(data);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public void addNewVariable(VariableData data)
	{
		m_addedVariables.add(data);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public void addDeletedVariable(VariableData data)
	{
		m_deletedVariables.add(data);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public VariableData[] getChangedVariables()
	{
		return m_changedVariables.toArray(new VariableData[0]);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public VariableData[] getAddedVariables()
	{
		return m_addedVariables.toArray(new VariableData[0]);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public VariableData[] getDeletedVariables()
	{
		return m_deletedVariables.toArray(new VariableData[0]);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public String getScopeName()
	{
		return m_scopeName;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public void setScopeName( String name )
	{
		m_scopeName = name;
	}
}
