package com.astra.ses.spell.gui.procs.interfaces.model;

import java.util.List;

import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.types.BreakpointType;
import com.astra.ses.spell.gui.core.model.types.ItemStatus;

public interface ICodeLine
{
	public int getLineNo();
	public String getParentCodeId();
	public String getSource();
	public int getNumExecutions();
	public boolean isExecutable();
	public BreakpointType getBreakpoint();

	public void calculateSummary();
	
	public String getSummaryName();
	public String getSummaryValue();
	public String getSummaryStatus();
	public ItemStatus getStatus();
	
	public List<ItemNotification> getNotifications( SummaryMode mode );
	public boolean hasNotifications();
	public void clearNotifications();
	public void clearFullHistory();
	
	public void onItemNotification( ItemNotification data );
	public void onExecuted();
	public void resetExecuted();
	public void setBreakpoint( BreakpointType type );
	public void removeBreakpoint();
	public void removeTemporaryBreakpoint();
	
	public void reset();
}
