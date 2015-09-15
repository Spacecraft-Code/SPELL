///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : CodeLine.java
//
// DATE      : Nov 6, 2012
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
package com.astra.ses.spell.gui.procs.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.types.BreakpointType;
import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.PropertyKey;
import com.astra.ses.spell.gui.procs.interfaces.model.ICodeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.SummaryMode;

public class CodeLine implements ICodeLine
{
	private String m_source;
	private String m_partentCodeId;
	private int m_lineNo;
	private int m_numExecuted;
	private BreakpointType m_breakpoint;
	private String m_summaryName;
	private String m_summaryValue;
	private String m_summaryStatus;
	private ItemStatus m_status;
	private int m_biggestNotificationExecution;
	private boolean m_extendedTimeLine;
	private int m_maxNotifications;
	

	class NotificationsPerLine
	{
		List<ItemNotification> notifications = new LinkedList<ItemNotification>();
		List<ItemNotification> all = new LinkedList<ItemNotification>();
	}
	
	private SortedMap<Integer,NotificationsPerLine> m_notifications;
	
	public CodeLine( String parentCodeId, int index, String source )
	{
		m_partentCodeId = parentCodeId;
		m_source = source;
		m_lineNo = index;
		m_notifications = new TreeMap<Integer,NotificationsPerLine>();
		reset();
	}
	
	@Override
    public int getLineNo()
    {
	    return m_lineNo;
    }

	@Override
    public String getParentCodeId()
    {
	    return m_partentCodeId;
    }

	@Override
    public String getSource()
    {
	    return m_source;
    }

	@Override
    public int getNumExecutions()
    {
	    return m_numExecuted;
    }

	@Override
    public boolean isExecutable()
    {
		if (m_source.trim().isEmpty()) return false;
		if (m_source.trim().startsWith("#") || 
			m_source.trim().startsWith("\"\"\"") || 
			m_source.trim().startsWith("'''")) return false;
	    return true;
    }

	@Override
    public BreakpointType getBreakpoint()
    {
	    return m_breakpoint;
    }

	@Override
    public String getSummaryName()
    {
	    return m_summaryName;
    }

	@Override
    public String getSummaryValue()
    {
	    return m_summaryValue;
    }

	@Override
    public String getSummaryStatus()
    {
	    return m_summaryStatus;
    }

	@Override
    public ItemStatus getStatus()
    {
	    return m_status;
    }

	@Override
    public List<ItemNotification> getNotifications( SummaryMode mode )
    {
		List<ItemNotification> list = new LinkedList<ItemNotification>();
		switch(mode)
		{
		case FULL:
			for(NotificationsPerLine not : m_notifications.values())
			{
				list.addAll(not.all);
			}
			break;
		case HISTORY:
			for(NotificationsPerLine not : m_notifications.values())
			{
				list.addAll(not.notifications);
			}
			break;
		case LATEST:
			list.addAll( m_notifications.get(m_biggestNotificationExecution).notifications );
			break;
		}
	    Collections.sort(list);
	    return list;
    }
	
	@Override
	public boolean hasNotifications()
	{
		return !m_notifications.isEmpty();
	}
	
	

	@Override
    public void onItemNotification(ItemNotification data)
    {
		// For a given line, groups of notifications ItemNotification can come with
		// different lists of items inside. All these are grouped in NotificationsPerLine.
		// Moreover, the notifications per line are separated on a line-execution-number basis,
		// since a line can be executed several times.
		//
		// Every time a new group of items comes we need to see if we need to substitute a 
		// previous one to update the data, or to add it as a new group of items.
		int executionNumber = data.getNumExecutions();
		if (m_notifications.containsKey(executionNumber))
		{
			NotificationsPerLine npl = m_notifications.get(executionNumber);
			int index = 0;
			int placeAtIndex = -1;
			boolean someMatched = false;
			for(ItemNotification not : npl.notifications)
			{
				// Merge only if it refers to the same item, AND it is a notification
				// later in time than the present one. Note that some messages
				// can come misordered.
				
				// Check if the notification matches any of the existing ones. It may happen that it matches one
				// of these, but we do not want to add it since it is an obsolete notification.
				someMatched = someMatched | not.referToSame(data); 
				if (not.referToSame(data) && data.getSequence() > not.getSequence())
				{
					placeAtIndex = index;
					break;
				}
				index++;
			}
			// Register all notifications in any case
			npl.all.add(data);
			
			// For the summary
			if (placeAtIndex>=0)
			{
				npl.notifications.remove(placeAtIndex);
				npl.notifications.add(placeAtIndex, data);
			}
			// Do not append it to the notifications if it was matching to one, but it was in the past.
			else if (!someMatched)
			{
				npl.notifications.add(data);
			}
		}
		else
		{
			if (m_biggestNotificationExecution < executionNumber)
			{
				m_biggestNotificationExecution = executionNumber;
			}
			NotificationsPerLine npl = new NotificationsPerLine();
			npl.notifications.add(data);
			npl.all.add(data);
			m_notifications.put(executionNumber,npl);
		}

		// Avoid the map growing without bounds
		if (m_maxNotifications >= 0)
		{
			while (m_notifications.firstKey() < m_biggestNotificationExecution - m_maxNotifications)
			{
				m_notifications.remove(m_notifications.firstKey());
			}
		}
				
	    // Build the summary element for this notification
    	calculateSummary();
    }

	@Override
    public void onExecuted()
    {
	    m_numExecuted++;
    }

	@Override
    public void resetExecuted()
    {
	    m_numExecuted = 0;
    }

	@Override
    public void setBreakpoint(BreakpointType type)
    {
	    m_breakpoint = type;
    }

	@Override
    public void removeBreakpoint()
    {
	    m_breakpoint = null;
    }
	
	@Override
    public void removeTemporaryBreakpoint()
    {
	    if(m_breakpoint==BreakpointType.TEMPORARY)
	    	m_breakpoint = null;
    }

	@Override
    public void reset()
    {
	    m_breakpoint = null;
	    m_numExecuted = 0;
	    clearNotifications();
	    // Check line configuration
	    IConfigurationManager cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
	    m_extendedTimeLine = cfg.getBooleanProperty(PropertyKey.SHOW_DATE_LINE); 
	    m_maxNotifications = Integer.parseInt(cfg.getProperty(PropertyKey.LINE_HISTORY_ITEMS));
    }
	
	@Override
	public void clearNotifications()
	{
	    m_summaryName = "";
	    m_summaryStatus = "";
	    m_summaryValue = "";
		m_biggestNotificationExecution = 0;
	    m_notifications.clear();
	    m_status = ItemStatus.UNKNOWN;
	}

	@Override
	public void clearFullHistory()
	{
	    for(NotificationsPerLine n : m_notifications.values())
	    {
	    	n.all.clear();
	    }
	}

	public String toString()
	{
		return "[LINE " + m_lineNo + ": executed=" + m_numExecuted + " source='" + m_source + "' notifications=" + m_notifications.size() + "]";
	}

	@Override
    public void calculateSummary()
    {
	    m_summaryName = "";
	    m_summaryValue = "";
	    m_summaryStatus = "";
	    m_status = ItemStatus.UNKNOWN;

		NotificationsPerLine npl = m_notifications.get(m_biggestNotificationExecution);
		if (npl != null && !npl.notifications.isEmpty()) 
		{
			int totalCount = 0;
		    int successCount = 0;
		    String notificationTime = "";
		    
		    // Calculate the success count and the overall status
			for(ItemNotification data : npl.notifications)
			{
				totalCount += data.getTotalItems();
			    for(int index = 0; index < data.getTotalItems(); index++)
			    {
			    	ItemStatus itemStatus = ItemStatus.UNKNOWN;
		    		try
		    		{
		    			itemStatus = ItemStatus.fromName(data.getItemStatus().get(index));
		    		}
		    		catch(Exception ex)
		    		{
		    			// Reset to unknown when we don't understand the message
		    			m_status = ItemStatus.UNKNOWN;
		    			m_summaryName = "";
		    			m_summaryValue = "";
		    			notificationTime = data.getTime();
		    		}
			    	if (itemStatus.equals(ItemStatus.SUCCESS))
			    	{
			    		successCount++;
			    	}
	    			if (itemStatus.ordinal() > m_status.ordinal() || 
	    					(itemStatus.ordinal() == m_status.ordinal()	&&
	    					 data.getTime().compareTo(notificationTime) >= 0))
	    			{
	    				// Get all the data from the highest priority record
	    				m_status = itemStatus;
	    				m_summaryName = data.getItemName().get(index);
	    				if (m_summaryName.indexOf("@") != -1)
	    				{
	    					m_summaryName = m_summaryName.split("@")[1];
	    				}
	    				m_summaryValue = data.getItemValue().get(index);
	    				notificationTime = data.getTime();
	    			}
			    }
			}

			if (m_extendedTimeLine)
			{
				if (m_status.getName().isEmpty())
				{
					m_summaryStatus = notificationTime;
				}
				else
				{
					m_summaryStatus = notificationTime + " " + m_status.getName().charAt(0);  
				}
			}
			else 
			{
				if (totalCount > 1)
				{
					m_summaryStatus = m_status.getName() + " (" + successCount + "/" + totalCount + ")";
				}
				else
				{
					m_summaryStatus = m_status.getName();
				}
			}
		}
    }
}
