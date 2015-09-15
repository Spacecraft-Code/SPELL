///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.types
// 
// FILE      : ItemStatus.java
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
package com.astra.ses.spell.gui.core.model.types;

public enum ItemStatus
{
	/**
	 * It is very important to add new elements in the correct order, As the
	 * order is used to determine which color will be used for painting the
	 * status column in the code view. another option is to implement
	 * IComparable interface, but enum provides the method ordinal() which can
	 * help us to determine the most important status
	 */
	// GROUP 1 : LOWEST PRIORITY
	UNKNOWN("UNKNOWN"),
	// GROUP 2
	SUCCESS("SUCCESS"),
	// GROUP 3
	WAITING("WAITING"), PROGRESS("IN PROGRESS"),
	// GROUP 4
	SKIPPED("SKIPPED"), WARNING("WARNING"),
	// GROUP 5 : HIGHEST PRIORITY
	ERROR("ERROR"), TIMEOUT("TIMEOUT"), CANCELLED("CANCELLED"), FAILED("FAILED"), SUPERSEDED(
	        "SUPERSEDED");

	/** Status name */
	private String	m_name;

	/***************************************************************************
	 * Private constructor
	 * 
	 * @param name
	 **************************************************************************/
	private ItemStatus(String name)
	{
		m_name = name;
	}

	/***************************************************************************
	 * Get Status name
	 * 
	 * @return
	 **************************************************************************/
	public String getName()
	{
		return m_name;
	}

	/***************************************************************************
	 * Return ItemStatus from the given string, by comparing their names
	 * 
	 * @return
	 **************************************************************************/
	public static ItemStatus fromName(String name)
	{
		for (ItemStatus status : ItemStatus.values())
		{
			if (status.getName().equals(name)) { return status; }
		}
		return ItemStatus.UNKNOWN;
	}
}
