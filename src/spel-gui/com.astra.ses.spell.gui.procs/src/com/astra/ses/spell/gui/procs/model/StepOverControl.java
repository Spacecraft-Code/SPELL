///////////////////////////////////////////////////////////////////////////////
//

// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : StepOverControl.java
//
// DATE      : Nov 8, 2012
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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformation.StepOverMode;
import com.astra.ses.spell.gui.procs.interfaces.model.IStepOverControl;

class StepOverControl implements IStepOverControl
{
	private class StackElement
	{
		String code;
		String function;
		int lineNo;
		
		public String toString()
		{
			return code + "::" + function + " [" + lineNo + "]";
		}
	}
	
	private StepOverModeState m_state;
	private List<StackElement> m_stack;
	private String m_instanceId;

	/**************************************************************************
	 * 
	 *************************************************************************/
	public StepOverControl( String instanceId )
	{
		m_instanceId = instanceId;
		m_state = new StepOverModeState();
		//TODO this needs to come from the procedure
		m_state.setMode(StepOverMode.STEP_INTO_ALWAYS);
		m_stack = new LinkedList<StackElement>();
		reset();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public StepOverMode getMode()
    {
	    return m_state.getMode();
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void setMode(StepOverMode mode)
    {
		m_state.setMode(mode);
    }
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public List<String> getStackCodeNames()
	{
		List<String> desc = new LinkedList<String>();
		for(StackElement element : m_stack)
		{
			desc.add(element.code);
		}
		return desc;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public List<Integer> getStackLines()
	{
		List<Integer> desc = new LinkedList<Integer>();
		for(StackElement element : m_stack)
		{
			desc.add(element.lineNo);
		}
		return desc;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public List<String> getStackFunctions()
	{
		List<String> desc = new LinkedList<String>();
		for(StackElement element : m_stack)
		{
			desc.add(element.function);
		}
		return desc;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public int getStackDepth()
	{
		return m_stack.size()-1;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public int getViewDepth()
	{
		return m_state.getVisibleDepth();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public boolean stackUp()
	{
		return m_state.increaseReferenceDepth();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public boolean  stackDown()
	{
		return m_state.decreaseReferenceDepth();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public boolean stackTop()
	{
		return m_state.removeReferenceDepth();
	}
	

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public boolean stackTo(int pos)
	{
		return m_state.goToReferenceDepth(pos);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void preInitialize( List<String> stack, boolean fullStack )
    {
		Logger.debug("Preinitialize SO control", Level.PROC, this);
		int limit = stack.size();
		if (!fullStack) limit -= 2;
		for(int idx=0; idx<limit; idx=idx+2)
		{
			Logger.debug("   - add to stack " + stack.get(idx) + ":" + stack.get(idx+1), Level.PROC, this);
			onExecutionCall( stack.get(idx), "<no info>", Integer.parseInt(stack.get(idx+1)));
		}
		Logger.debug("After preinitialization stack is " + Arrays.toString(m_stack.toArray()), Level.PROC, this);
		Logger.debug("Stepping over: " + isSteppingOver(), Level.PROC, this);
		Logger.debug("Current code : " + getCodeId(), Level.PROC, this);
		Logger.debug("Current depth: " + getViewDepth(), Level.PROC, this);
		Logger.debug("Current line : " + getLineNo(), Level.PROC, this);
		Logger.debug("Stack depth  : " + m_stack.size(), Level.PROC, this);
		Logger.debug("", Level.PROC, this);
    }
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void onExecutionCall( String code, String function, int lineNo )
    {
		StackElement se = new StackElement();
		se.code = code;
		se.lineNo = lineNo;
		se.function = function;
		m_stack.add(se);
		m_state.onExecutionCall();
//		Logger.debug("After call event stack is " + Arrays.toString(m_stack.toArray()), Level.PROC, this);
//		Logger.debug("Stepping over: " + isSteppingOver(), Level.PROC, this);
//		Logger.debug("Current code: " + getCodeId(), Level.PROC, this);
//		Logger.debug("Current depth: " + getViewDepth(), Level.PROC, this);
//		Logger.debug("Current line : " + getLineNo(), Level.PROC, this);
//		Logger.debug("", Level.PROC, this);
//		Logger.debug("", Level.PROC, this);
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void onExecutionLine()
    {
		m_state.onExecutionLine();
//		Logger.debug("After line event stack is " + Arrays.toString(m_stack.toArray()), Level.PROC, this);
//		Logger.debug("Stepping over: " + isSteppingOver(), Level.PROC, this);
//		Logger.debug("Current code: " + getCodeId(), Level.PROC, this);
//		Logger.debug("Current depth: " + getViewDepth(), Level.PROC, this);
//		Logger.debug("Current line : " + getLineNo(), Level.PROC, this);
//		Logger.debug("", Level.PROC, this);
//		Logger.debug("", Level.PROC, this);
    }
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void onExecutionReturn()
    {
		m_stack.remove( m_stack.size()-1 );
		m_state.onExecutionReturn();
//		Logger.debug("After return event stack is " + Arrays.toString(m_stack.toArray()), Level.PROC, this);
//		Logger.debug("Stepping over: " + isSteppingOver(), Level.PROC, this);
//		Logger.debug("Current code : " + getCodeId(), Level.PROC, this);
//		Logger.debug("Current depth: " + getViewDepth(), Level.PROC, this);
//		Logger.debug("Current line : " + getLineNo(), Level.PROC, this);
//		Logger.debug("", Level.PROC, this);
//		Logger.debug("", Level.PROC, this);
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void onProcedureReady()
    {
		m_state.initialState();
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void updateCurrentLine( int stackElement, int lineNo )
	{
		int stackIndex = m_stack.size()-stackElement-1;
		if (stackIndex>=0 && m_stack.size()>stackIndex)
		{
			m_stack.get(stackIndex).lineNo = lineNo;
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public boolean isSteppingOver()
    {
		return m_state.isSteppingOver();
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public String getCodeId()
    {
		// For the initialization phase we do not really care about the line
		if (m_stack.isEmpty()) return m_instanceId;
		int depth = m_state.getVisibleDepth();
		if (depth>=m_stack.size()) depth = m_stack.size()-1;
		if (depth<0) depth = 0;
		return m_stack.get(depth).code;
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public String getFunctionName()
    {
		// For the initialization phase we do not really care about the line
		if (m_stack.isEmpty()) return "";
		int depth = m_state.getVisibleDepth();
		if (depth>=m_stack.size()) depth = m_stack.size()-1;
		return m_stack.get(depth).function;
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public int getLineNo()
    {
		// For the initialization phase we do not really care about the line
		if (m_stack.isEmpty()) return 0;
		int depth = m_state.getVisibleDepth();
		if (depth>=m_stack.size()) depth = m_stack.size()-1;
		if (depth==-1) return 0;
		return m_stack.get(depth).lineNo;
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void reset()
	{
		m_stack.clear();
		m_state.reset();
	}
}
