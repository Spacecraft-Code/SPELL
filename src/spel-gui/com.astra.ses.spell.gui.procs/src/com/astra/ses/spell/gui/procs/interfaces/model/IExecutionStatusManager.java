package com.astra.ses.spell.gui.procs.interfaces.model;

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.types.BreakpointType;
import com.astra.ses.spell.gui.procs.interfaces.listeners.IExecutionListener;

public interface IExecutionStatusManager extends IStackMovements
{
	public String getCurrentCode();
	public int getCurrentLineNo();
	public String getCurrentFunction();
	public ICodeModel getCodeModel( String id );

	public void initialize( IProgressMonitor monitor );
	
	public void onProcedureReady();
	public void onItemNotification( ItemNotification data );
	public void onStackNotification( StackNotification data );
	public void onStatusNotification( StatusNotification data );

	public void reset();
	public void dispose();
	public void clearNotifications();
	
	public void beforeGoto();
	public void setBreakpoint( int lineNo, BreakpointType type );
	public void removeBreakpoint();
	public void clearBreakpoints();
	
	public void setReplay( boolean replay );
	public boolean isInReplay();
	
	public void setRunInto( boolean runInto );
	public boolean isRunInto();
	public void doSkip();
	public void doStepInto();
	public void doStepOver();
	
	
	public void addListener(IExecutionListener listener);
	public void removeListener(IExecutionListener listener);

}
