///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.watchvariables.notification
// 
// FILE      : VariableData.java
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

/*******************************************************************************
 * @brief Data structure used for variables.
 * @date 25/10/07
 ******************************************************************************/
public class VariableData implements Comparable<VariableData>
{
	private String	m_name;
	private String	m_type;
	private String	m_value;
	private boolean	m_isGlobal;
	private boolean	m_isChanged;
	private boolean m_isDeleted;
	private boolean m_isAdded;

	/**************************************************************************
	 * Constructor.
	 * 
	 * @param m_name
	 * @param m_type
	 * @param m_value
	 * @param global
	 *************************************************************************/
	public VariableData(String name, String type, String value, boolean global)
	{
		this.m_name = name;
		this.m_type = type;
		this.m_value = value;
		this.m_isGlobal = global;
		this.m_isDeleted = false;
		this.m_isAdded = false;
		this.m_isChanged = false;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public VariableData(String name, String type, String value, boolean global, boolean deleted, boolean added )
	{
		this.m_name = name;
		this.m_type = type;
		this.m_value = value;
		this.m_isGlobal = global;
		this.m_isDeleted = deleted;
		this.m_isAdded = added;
		this.m_isChanged = false;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public void updateFrom( VariableData other )
	{
		this.m_name = other.m_name;
		this.m_type = other.m_type;
		this.m_value = other.m_value;
		this.m_isGlobal = other.m_isGlobal;
		this.m_isDeleted = other.m_isDeleted;
		this.m_isAdded = other.m_isAdded;
		this.m_isChanged = other.m_isChanged;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public String getName()
	{
		return m_name;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public String getValue()
	{
		return m_value;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public String getType()
	{
		return m_type;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public void setValue( String value )
	{
		m_value = value;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public boolean isGlobal()
	{
		return m_isGlobal;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public boolean isDeleted()
	{
		return m_isDeleted;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public boolean isAdded()
	{
		return m_isAdded;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public boolean isChanged()
	{
		return m_isChanged;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public String toString()
	{
		return "(" + m_name + " = " + m_value + ": " + m_type + ", " + m_isGlobal + ", " + m_isDeleted + ", " + m_isAdded + ")";
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public int compareTo(VariableData o)
	{
		int result = 1;
		if ((o.m_name.equals(m_name)) && (o.m_isGlobal == m_isGlobal))
		{
			result = 0;
		}
		return result;
	}
}
