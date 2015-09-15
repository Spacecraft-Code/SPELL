///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.notification
// 
// FILE      : ItemNotification.java
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
package com.astra.ses.spell.gui.core.model.notification;

import java.util.LinkedList;
import java.util.List;

import com.astra.ses.spell.gui.core.model.types.ItemType;

/*******************************************************************************
 * @brief Data structure used for item data notifications.
 * @date 25/10/07
 ******************************************************************************/
public class ItemNotification extends NotificationData
{
	// =========================================================================
	// INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	/** Holds the item name */
	private List<String>	m_itemName	   = new LinkedList<String>();
	/** Holds the item value */
	private List<String>	m_itemValue	   = new LinkedList<String>();
	/** Holds the item status */
	private List<String>	m_itemStatus	= new LinkedList<String>();
	/** Holds the item time */
	private List<String>	m_itemTime	   = new LinkedList<String>();
	/** Holds the item comments */
	private List<String>	m_itemComments	= new LinkedList<String>();
	/** Holds the item type */
	private ItemType	      m_itemType;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param type
	 *            The type of the notification
	 * @param stack
	 *            The stack position
	 ***************************************************************************/
	public ItemNotification(String procId, ItemType type, String stack)
	{
		super(procId, stack);
		m_itemType = type;
	}

	/***************************************************************************
	 * Serialize
	 **************************************************************************/
	public String toString()
	{
		return "[" + m_itemType + "]: " + m_itemName + " = " + m_itemValue
		        + " (" + m_itemStatus + "):" + m_itemComments;
	}

	/***************************************************************************
	 * Obtain item name
	 **************************************************************************/
	public List<String> getItemName()
	{
		return m_itemName;
	}

	/***************************************************************************
	 * Obtain item value
	 **************************************************************************/
	public List<String> getItemValue()
	{
		return m_itemValue;
	}

	/***************************************************************************
	 * Obtain item status
	 **************************************************************************/
	public List<String> getItemStatus()
	{
		return m_itemStatus;
	}

	/***************************************************************************
	 * Obtain comments
	 **************************************************************************/
	public List<String> getComments()
	{
		return m_itemComments;
	}

	/***************************************************************************
	 * Obtain times
	 **************************************************************************/
	public List<String> getTimes()
	{
		return m_itemTime;
	}

	/***************************************************************************
	 * Obtain item type
	 **************************************************************************/
	public ItemType getType()
	{
		return m_itemType;
	}

	/***************************************************************************
	 * Set item name
	 **************************************************************************/
	public void setItems(String names, String values, String status,
	        String comments, String times)
	{
		for (String name : names.split(",,"))
			m_itemName.add(name);
		for (String value : values.split(",,"))
			m_itemValue.add(value);
		for (String st : status.split(",,"))
			m_itemStatus.add(st);
		for (String cmt : comments.split(",,"))
			m_itemComments.add(cmt);
		for (String time : times.split(",,"))
		{
			if (time.trim().isEmpty())
			{
				time = getTime();
			}
			m_itemTime.add(time);
		}
	}

	/***************************************************************************
	 * Set item name
	 **************************************************************************/
	public void setItems(List<String> names, List<String> values, List<String> status, List<String> comments, List<String> times)
	{
		m_itemName = names;
		m_itemValue = values;
		m_itemStatus = status;
		m_itemComments = comments;
		m_itemTime = times;
	}

	/***************************************************************************
	 * Get total items
	 **************************************************************************/
	public int getTotalItems()
	{
		return m_itemName.size();
	}

	/***************************************************************************
	 * Compare contained items
	 **************************************************************************/
	public boolean referToSame( ItemNotification other )
	{
		if (getItemName().size() != other.getItemName().size() ) return false;
		int index = 0;
		for(String oname : other.getItemName())
		{
			String name = getItemName().get(index);
			if (!name.equals(oname)) return false;
			index++;
		}
		return true;
	}

	/**
	 * Debugging
	 */
	public void dump()
	{
		System.err.println("Notification for exec number " + getNumExecutions());
		for (int idx = 0; idx < m_itemName.size(); idx++)
		{
			System.err.println(m_itemName.get(idx) + "=" + m_itemValue.get(idx)
			        + " (" + m_itemStatus.get(idx) + ") : "
			        + m_itemComments.get(idx) + " - " + m_itemTime.get(idx));
		}
	}
}
