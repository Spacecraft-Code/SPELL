///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : ExecutionStatusManager.java
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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.astra.ses.spell.gui.core.CoreNotifications;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreApplicationStatusListener;
import com.astra.ses.spell.gui.core.model.notification.ApplicationStatus;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification.StackType;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.types.BreakpointType;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.procs.interfaces.listeners.IExecutionListener;
import com.astra.ses.spell.gui.procs.interfaces.model.ICodeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.ICodeModel;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformation.StepOverMode;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionStatusManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.procs.interfaces.model.IStepOverControl;

public class ExecutionStatusManager implements IExecutionStatusManager, ICoreApplicationStatusListener
{
	private static DateFormat s_df;
	
	/** Holds the base code identifier */
	private String m_instanceId;
	/** Holds code lines per code identifier */
	private Map<String,SingleCodeModel> m_codeModels;
	/** True if in replay mode */
	private boolean m_replay;
	/** Holds the listeners */
	private List<IExecutionListener> m_listeners;
	/** Reference to the model */
	private IProcedure m_model;
	
	/** Used to order the notifications */
	private long m_lastLineNotificationSequence;
	
	/** USed to detect processing delays */
	private long m_processingDelaySec;
	
	/** Holds the step over control */
	private IStepOverControl m_so;
	
	/** Holds the stack update buffer */
	private StackUpdateBuffer m_buffer;
	
	/**************************************************************************
	 * Constructor
	 * @param instanceId
	 * @param model
	 *************************************************************************/
	public ExecutionStatusManager( String instanceId, IProcedure model )
	{
		Logger.debug("Created", Level.PROC, this);
		int idx = instanceId.indexOf("#");
		if (idx != -1)
		{
			instanceId = instanceId.substring(0, idx);
		}
		m_instanceId = instanceId;
		m_model = model;
		m_codeModels = new TreeMap<String,SingleCodeModel>();
		m_codeModels.put( instanceId, new SingleCodeModel(instanceId, model) );
		m_replay = false;
		m_listeners = new LinkedList<IExecutionListener>();
		m_lastLineNotificationSequence = 0;
		m_processingDelaySec = 0;
		m_so = new StepOverControl( m_instanceId );

		CoreNotifications.get().addListener(this, ICoreApplicationStatusListener.class);
		
		// Time configuration
		IConfigurationManager cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		s_df = cfg.getTimeFormat();
		m_buffer = new StackUpdateBuffer(this);
		m_buffer.start();
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public String getCurrentCode()
    {
		return m_so.getCodeId(); 
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public String getCurrentFunction()
    {
		return m_so.getFunctionName(); 
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public int getCurrentLineNo()
    {
		return m_so.getLineNo();
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void initialize(IProgressMonitor monitor)
    {
		Logger.debug("Initializing", Level.PROC, this);
		m_codeModels.get(m_instanceId).initialize(monitor);
	    notifyCodeChanged( m_codeModels.get(m_instanceId) );
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public List<String> getStackCodeNames()
	{
		return m_so.getStackCodeNames();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public List<Integer> getStackLines()
	{
		return m_so.getStackLines();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public List<String> getStackFunctions()
    {
		return m_so.getStackFunctions(); 
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public boolean stackUp()
	{
		return m_so.stackUp();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public boolean stackTo(int pos)
	{
		return m_so.stackTo(pos);
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public boolean stackDown()
	{
		return m_so.stackDown();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public boolean stackTop()
	{
		if (m_so.stackTop())
		{
			m_model.getController().moveStack(m_so.getStackDepth());
			return true;
		}
		return false;	
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public int getStackDepth()
	{
		return m_so.getStackDepth();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public int getViewDepth()
	{
		return m_so.getViewDepth();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void onProcedureReady()
    {
		if (!isInReplay())
		{
			m_so.onProcedureReady();
		}
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public boolean isInReplay()
    {
	    return m_replay;
    }
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setRunInto(boolean runInto)
	{
		// Internal information model
		if (runInto)
		{
			m_so.setMode(StepOverMode.STEP_INTO_ALWAYS);
		}
		else
		{
			m_so.setMode(StepOverMode.STEP_OVER_ALWAYS);
		}
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public boolean isRunInto()
	{
		return m_so.getMode().equals(StepOverMode.STEP_INTO_ALWAYS);
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void clearNotifications()
	{
		for(String codeId : m_codeModels.keySet())
		{
			m_codeModels.get(codeId).clearNotifications();
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void onItemNotification(ItemNotification data)
    {
	    List<String> stack = data.getStackPosition();
		//Logger.debug("Notified item: " + Arrays.toString(stack.toArray()), Level.PROC, this);
		
		List<ICodeLine> updatedLines = new LinkedList<ICodeLine>();
	    placeNotification( stack, data, updatedLines );
	    notifyItemsChanged(updatedLines);
    }
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	private void placeNotification( List<String> stack, ItemNotification data, List<ICodeLine> updatedLines )
	{
		//Logger.debug("Place notification: " + Arrays.toString(stack.toArray()), Level.PROC, this);
		if (stack.size()>2)
		{
			String codeId = stack.get(0);
			int lineNo = Integer.parseInt(stack.get(1))-1;
			SingleCodeModel model = m_codeModels.get(codeId);
			if (model == null)
			{
				Logger.error("Cannot place item notification, no such code: " + codeId, Level.PROC, this);
				return;
			}
			model.onItemNotification(lineNo, data, updatedLines);
			// Iterate
			placeNotification( stack.subList(2, stack.size()), data, updatedLines);
		}
		else
		{
			String codeId = stack.get(0);
			int lineNo = Integer.parseInt(stack.get(1))-1;
			SingleCodeModel model = m_codeModels.get(codeId);
			if (model == null)
			{
				Logger.error("Cannot place item notification, no such code: " + codeId, Level.PROC, this);
				return;
			}
			model.onItemNotification(lineNo, data, updatedLines);
		}
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void onStackNotification(StackNotification data)
    {
	    List<String> stack = data.getStackPosition();
	    
	    // Do not process notifications with no stakc unless they are of type RETURN 
	    if ( (!data.getStackType().equals(StackType.RETURN)) && (stack.isEmpty() || stack.size()==1) ) return;
	    
	    // Do not process stage notifications
		if (data.getStackType().equals(StackNotification.StackType.STAGE)) return;

		// Some pre-checks
		String prevCode = getCurrentCode();
		boolean refreshCode = false; // May be needed upon initialization from ASRUN
		boolean preInitialize = (getStackDepth() == -1);
		boolean realign = (!preInitialize) && (!m_replay) && (data.getStackType().equals(StackType.LINE)); 

		////////////////////////////////////////////////////////////////////////////////////////////
		// REALIGN AND PREINITIALIZE: after processing ASRUN data
		////////////////////////////////////////////////////////////////////////////////////////////
		if (preInitialize)
		{
			preinitializeStack(data);
			
			// Also notify a code change for whenever we start with several stack levels
			// on the very first notification -- we may need to change to a different source
			// code
			if (stack.size()>2) refreshCode = true;
		}
		// After processing ASRUN, having missed some interim CALL/RETURN notifications, we may have
		// to realign. The current stack depth is not zero but does not match with the notification.
		else if (realign)
		{
			if (realignStack(data)) return;
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////
		// MAIN EVENT PROCESSING
		////////////////////////////////////////////////////////////////////////////////////////////
		switch(data.getStackType())
		{
		case CALL:
			//Logger.info("Notified CALL: " + Arrays.toString(data.getStackPosition().toArray()), Level.PROC, this);
			String code = data.getStackPosition().get(data.getStackPosition().size()-2);
			int lineNo = Integer.parseInt(data.getStackPosition().get(data.getStackPosition().size()-1));
			m_so.onExecutionCall(code,data.getCodeName(),lineNo);
			break;
		case LINE:
			//Logger.info("Notified LINE: " + Arrays.toString(data.getStackPosition().toArray()), Level.PROC, this);
			m_so.onExecutionLine();
			break;
		}

		// On RETURN events: do not invoke place execution.
		if (!data.getStackType().equals(StackType.RETURN))
		{
			processAndPlaceExecution(data);
		}
		
		// Only on LINE events: if the sequence of the notification is lower than the last
		// one processed, do not NOTIFY IT to GUI components. 
		// We need to do this is messages come in bad order from the server, to avoid 
		// weird line jumping.
		boolean notifyGUI = shouldNotifyGUI(data);

	    // At this point process return events	    
	    if (data.getStackType().equals(StackType.RETURN))
	    {
	    	m_so.onExecutionReturn();
	    }
	    
	    // Check if a code change should be notified
		refreshCode |= !prevCode.equals(getCurrentCode());
		if (refreshCode)
		{
			ICodeModel currentCode = getCodeModel(getCurrentCode());
			notifyCodeChanged( currentCode );
		}

		////////////////////////////////////////////////////////////////////////////////////////////
		// OTHERS 
		////////////////////////////////////////////////////////////////////////////////////////////

	    // Update processing delay
	    if (!m_replay && notifyGUI) notifyProcessingDelay(data);

    }
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	private void preinitializeStack( StackNotification data )
	{
		Logger.warning("###############################", Level.PROC, this);
		Logger.warning("Initialization of the stack", Level.PROC, this);
		Logger.warning("###############################", Level.PROC, this);
		m_so.preInitialize(data.getStackPosition(), data.getStackType().equals(StackType.LINE));

		int modelCount = getStackCodeNames().size();
		Logger.warning("###############################", Level.PROC, this);
		Logger.warning("After initialization of the stack: " + modelCount, Level.PROC, this);
		Logger.warning("###############################", Level.PROC, this);
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	private boolean realignStack( StackNotification data )
	{
		List<String> stack = data.getStackPosition();
		int modelCount = getStackCodeNames().size();
		int notifiedCount = stack.size()/2; // Note that includes line numbers

		if (modelCount != notifiedCount)
		{
			Logger.warning("###############################", Level.PROC, this);
			Logger.warning("Realign the stack", Level.PROC, this);
			Logger.warning("   - Current  (" + modelCount + "): " + Arrays.toString(getStackCodeNames().toArray()), Level.PROC, this);
			Logger.warning("   - Notified (" + notifiedCount + "): " + Arrays.toString(stack.toArray()), Level.PROC, this);
			Logger.warning("###############################", Level.PROC, this);

			// If the model has more elements than the notified stack, we missed some RETURN notifications
			if (notifiedCount < modelCount)
			{
				int diff = modelCount - notifiedCount;
				Logger.warning("Return " + diff + " times", Level.PROC, this);
				for(int count = 0; count<diff; count++) m_so.onExecutionReturn();
			}
			// If the stack has more elements than the notified stack, we missed some CALL notifications
			else if (notifiedCount > modelCount)
			{
				int diff = notifiedCount - modelCount;
				Logger.warning("Call " + diff + " times", Level.PROC, this);
				for(int count = 1; count<diff; count++) 
				{
					m_so.onExecutionCall( stack.get(modelCount+count), "<no info>", Integer.parseInt(stack.get(modelCount+count+1)));
				}
			}

			modelCount = getStackCodeNames().size();
			Logger.warning("###############################", Level.PROC, this);
			Logger.warning("After realign the stack", Level.PROC, this);
			Logger.warning("   - Current  (" + modelCount + "): " + Arrays.toString(getStackCodeNames().toArray()), Level.PROC, this);
			Logger.warning("   - Notified (" + notifiedCount + "): " + Arrays.toString(stack.toArray()), Level.PROC, this);
			Logger.warning("###############################", Level.PROC, this);
			
			stackTop();
			
			return true;
		}
		return false;
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	private void processAndPlaceExecution( StackNotification data )
	{
		try
		{
			List<ICodeLine> updatedLines = new LinkedList<ICodeLine>();
			placeExecution( data.getStackPosition(), data.getStackType(), updatedLines );
			if (m_buffer != null)
			{
				m_buffer.scheduleUpdate(updatedLines);
			}
			else
			{
				notifyLinesChanged(updatedLines);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	private boolean shouldNotifyGUI( StackNotification data )
	{
		if (data.getStackType().equals(StackType.LINE))
		{
			long seq = data.getSequence();
			if (seq < m_lastLineNotificationSequence)
			{
				return false;
			}
			else
			{
				// If we accept the notification store it so that we avoid further
				// notifications that are actually in the past
				m_lastLineNotificationSequence = seq;
			}
		}
		return true;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void notifyProcessingDelay( StackNotification data )
	{
	    try
        {
	    	Date currentTime = Calendar.getInstance().getTime();
	    	Date notificationTime = s_df.parse(data.getTime());
	    	long curSec = currentTime.getTime()/1000;
	    	long notSec = notificationTime.getTime()/1000;
	        long diffSec = Math.abs(curSec-notSec);
	        if (m_processingDelaySec != diffSec)
	        {
	        	m_processingDelaySec = diffSec;
	        	notifyDelayChanged(diffSec);
	        }
        }
        catch (ParseException e)
        {
	        e.printStackTrace();
        }
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public ICodeModel getCodeModel( String id )
	{
		return m_codeModels.get(id);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private SingleCodeModel getModel( String codeId )
	{
		SingleCodeModel code = null;
		if (!m_codeModels.containsKey(codeId))
		{
			code = new SingleCodeModel(codeId,m_model);
			code.initialize( new NullProgressMonitor() );
			m_codeModels.put(codeId, code);
		}
		else
		{
			code = m_codeModels.get(codeId);
		}
		return code;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void placeExecution( List<String> stack, StackNotification.StackType type, List<ICodeLine> updatedLines )
	{
		//Logger.debug("Place execution: " + Arrays.toString(stack.toArray()), Level.PROC, this);
		int stackElement = stack.size()/2-1;
		int lineNo = -1;
		String codeId = stack.get(0);
		//Logger.debug("   - stack element: " + stackElement, Level.PROC, this);
		//Logger.debug("   - code id      : " + codeId, Level.PROC, this);
		SingleCodeModel code = getModel(codeId);
		try
		{
			lineNo = Integer.parseInt(stack.get(1))-1;
			//Logger.debug("   - line number  : " + lineNo, Level.PROC, this);
			if (code.getLines().size()>lineNo)
			{
				//Logger.debug("Place notification for line " + lineNo + " on code " + codeId, Level.PROC, this);
				code.onStackNotification(lineNo);
				m_so.updateCurrentLine(stackElement, lineNo);
			}
			else
			{
				Logger.error("Cannot place notification for line " + lineNo + " on code " + codeId, Level.PROC, this);
				return;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		if (stack.size()>2)
		{
			//Logger.debug("Subsequent call", Level.PROC, this);
			placeExecution( stack.subList(2, stack.size()), type, updatedLines);
		}
		else
		{
			ICodeLine line = code.getLine(lineNo);
			if (!updatedLines.contains(line)) updatedLines.add(line);
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void reset()
    {
		Logger.debug("Reset model", Level.PROC, this);
		for(String codeId : m_codeModels.keySet())
		{
	    	if (!codeId.equals(m_instanceId))
	    	{
	    		m_codeModels.remove(codeId);
	    	}
	    	else
	    	{
			    for(ICodeModel model : m_codeModels.values())
			    {
			    	model.reset();
			    }
	    	}
		}
		m_lastLineNotificationSequence = 0;
		m_replay = false;
		m_processingDelaySec = 0;
		m_so.reset();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void doSkip()
	{
		ICodeModel currentCode = getCodeModel(getCurrentCode());
		currentCode.getLine(getCurrentLineNo()).resetExecuted();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void doStepInto()
	{
		m_so.setMode(StepOverMode.STEP_INTO_ONCE);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void doStepOver()
	{
		m_so.setMode(StepOverMode.STEP_OVER_ONCE);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void dispose()
    {
		CoreNotifications.get().removeListener(this);
		m_buffer.stopUpdate();
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void beforeGoto()
    {
		getCodeModel(getCurrentCode()).getLine(getCurrentLineNo()).resetExecuted();
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void setBreakpoint( int lineNo, BreakpointType type )
    {
		Logger.debug("Set breakpoint " + lineNo, Level.PROC, this);
		getCodeModel(getCurrentCode()).setBreakpoint(lineNo-1, type);
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void removeBreakpoint()
    {
		Logger.debug("Unset temporary breakpoint", Level.PROC, this);
		// Unset only temporary breakpoint
		getCodeModel(getCurrentCode()).getLine(getCurrentLineNo()).removeTemporaryBreakpoint();
		//getCodeModel(getCurrentCode()).getLine(getCurrentLineNo()).removeBreakpoint();
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void clearBreakpoints()
    {
		Logger.debug("Clear breakpoints", Level.PROC, this);
		for(ICodeModel code : m_codeModels.values())
	    {
	    	code.clearBreakpoints();
	    }
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void setReplay(boolean replay)
    {
		Logger.debug("Set replay mode: " + replay, Level.PROC, this);
	    m_replay = replay;
	    m_lastLineNotificationSequence = 0;
	    m_processingDelaySec = 0;
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void notifyDelayChanged( final long delaySec )
	{
		try
		{
			for(IExecutionListener listener: m_listeners)
			{
				listener.onProcessingDelayChanged(delaySec);
			}
		}
		catch(ConcurrentModificationException ignore){};
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	void notifyLinesChanged( List<ICodeLine> lines )
	{
		try
		{
			for(IExecutionListener listener: m_listeners)
			{
				listener.onLinesChanged( lines );
			}
		}
		catch(ConcurrentModificationException ignore){};
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void notifyCodeChanged( ICodeModel code )
	{
		try
		{
			for(IExecutionListener listener: m_listeners)
			{
				listener.onCodeChanged( code );
			}
		}
		catch(ConcurrentModificationException ignore){};
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void notifyItemsChanged( List<ICodeLine> lines )
	{
		try
		{
			for(IExecutionListener listener: m_listeners)
			{
				listener.onItemsChanged(lines);
			}
		}
		catch(ConcurrentModificationException ignore){};
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void addListener(IExecutionListener listener)
    {
	    if (!m_listeners.contains(listener))
	    {
	    	m_listeners.add(listener);
	    }
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void removeListener(IExecutionListener listener)
    {
	    if (m_listeners.contains(listener))
	    {
	    	m_listeners.remove(listener);
	    }
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	public void onStatusNotification( StatusNotification status )
	{
		switch(status.getStatus())
		{
		case ERROR:
		case INTERRUPTED:
			if (stackTop())
			{
				notifyCodeChanged(getCodeModel(getCurrentCode()));
			}
			break;
		default:
			break;
		}

		switch(status.getStatus())
		{
		case FINISHED:
		case ERROR:
		case ABORTED:
			m_buffer.stopUpdate();
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void onApplicationStatus(ApplicationStatus status)
    {
	    if (status.freeMemoryPC<10.0)
	    {
	    	Logger.warning("Running out of memory, removing notification history", Level.PROC, this);
	    	for(SingleCodeModel model : m_codeModels.values())
	    	{
	    		model.clearNotifications();
	    	}
	    	System.gc();
	    }
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public String getListenerId()
    {
	    return "Execution status manager for " + m_model.getProcId();
    }
}
