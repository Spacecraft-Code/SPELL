///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : StepOverModeState.java
//
// DATE      : May 28, 2013
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

import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformation.StepOverMode;

class StepOverModeState
{
	private StepOverMode m_mode;
	private StepOverMode m_prevMode;
	private int m_currentDepth;
	private int m_referenceDepth;
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	StepOverModeState()
	{
		reset();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
    boolean isSteppingOver()
    {
    	boolean isSteppingOver = false;
    	if ((m_referenceDepth!=-1)&&(m_currentDepth>m_referenceDepth))
    	{
    		isSteppingOver = true;
    	}
    	return isSteppingOver;
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	void reset()
	{
		m_mode = null;
		m_prevMode = null;
		m_currentDepth = -1;
		m_referenceDepth = -1;
    	Logger.debug("Reset", Level.PROC, this);
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	StepOverMode getMode()
	{
		return m_mode;
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	int getVisibleDepth()
	{
		int depth = 0;
    	if (m_referenceDepth!=-1)
    	{
    		depth = m_referenceDepth;
    	}
    	else
    	{
    		depth = m_currentDepth;
    	}
    	return depth;
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	boolean goToReferenceDepth(int pos)
	{
		if (m_referenceDepth != -1)
		{
			m_referenceDepth = pos;
			if (m_referenceDepth == m_currentDepth)
			{
				m_referenceDepth = -1;
			}
			return true;
		}
		return false;
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	boolean increaseReferenceDepth()
	{
		if (m_referenceDepth != -1)
		{
			m_referenceDepth++;
			if (m_referenceDepth == m_currentDepth)
			{
				m_referenceDepth = -1;
			}
			return true;
		}
		return false;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	boolean decreaseReferenceDepth()
	{
		if ((m_referenceDepth != -1) && (m_referenceDepth>0))
		{
			m_referenceDepth--;
			if (m_referenceDepth == m_currentDepth)
			{
				m_referenceDepth = -1;
			}
			return true;
		}
		return false;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	boolean removeReferenceDepth()
	{
		if (m_referenceDepth != -1)
		{
			m_referenceDepth = m_currentDepth;
//	    	Logger.debug("--------------------------------------------------", Level.PROC, this);
//	    	Logger.debug("After MoveTop", Level.PROC, this);
//	    	Logger.debug("Current depth " + m_currentDepth, Level.PROC, this);
//	    	Logger.debug("Reference depth " + m_referenceDepth, Level.PROC, this);
//	    	Logger.debug("Mode " + m_mode, Level.PROC, this);
//	    	Logger.debug("--------------------------------------------------", Level.PROC, this);
			return true;
		}
		return false;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	void initialState()
	{
		m_currentDepth = 0;
		m_referenceDepth = -1;
    	Logger.debug("Initial state", Level.PROC, this);
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	void setMode( StepOverMode mode )
	{
		Logger.debug("Set SO mode " + mode, Level.PROC,this);
		switch(mode)
		{
		// The user wants to get in always, so remove any reference depth
		case STEP_INTO_ALWAYS:
			m_referenceDepth = -1;
			m_prevMode = m_mode;
			m_mode = mode;
			break;
		case STEP_INTO_ONCE:
			if (m_mode.equals(StepOverMode.STEP_OVER_ALWAYS))
			{
				m_prevMode = m_mode;
				m_mode = mode;
			}
			break;
		case STEP_OVER_ALWAYS:
			m_referenceDepth = m_currentDepth;
			m_prevMode = m_mode;
			m_mode = mode;
			break;
		case STEP_OVER_ONCE:
			// The user wants to get over only once. Here are two cases:
			// 1. We are already in STEP_OVER_ALWAYS, therefore this setMode
			//    call should have no effect
			// 2. We are in STEP_INTO_ALWAYS, meaning that we want to get
			//    over only once and then continue stepping into. In this
			//    case we need to set the reference depth to the current one 
			//    but after returning to the same level remove the reference
			if (m_mode.equals(StepOverMode.STEP_INTO_ALWAYS))
			{
				m_prevMode = m_mode;
				m_mode = mode;
				m_referenceDepth = m_currentDepth;
			}
			break;
		}
//    	Logger.debug("--------------------------------------------------", Level.PROC, this);
//    	Logger.debug("After set mode " + mode, Level.PROC, this);
//    	Logger.debug("Current depth " + m_currentDepth, Level.PROC, this);
//    	Logger.debug("Reference depth " + m_referenceDepth, Level.PROC, this);
//    	Logger.debug("Prev mode " + m_prevMode, Level.PROC, this);
//    	Logger.debug("SO: " + isSteppingOver(), Level.PROC, this);
//    	Logger.debug("--------------------------------------------------", Level.PROC, this);
//    	Logger.debug("", Level.PROC, this);
//    	Logger.debug("", Level.PROC, this);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
    void onExecutionCall()
    {
		// Increase the depth now
		m_currentDepth++;
		
    	// The first time we get a CALL for the procedure load. Ignore this one.
    	// The ProcedureController will update everything appropriately
    	// once the load process is finished.
    	if (m_mode == null) return;

//    	Logger.debug("--------------------------------------------------", Level.PROC, this);
//    	Logger.debug("Before On Call", Level.PROC, this);
//    	Logger.debug("Current depth " + m_currentDepth, Level.PROC, this);
//    	Logger.debug("Reference depth " + m_referenceDepth, Level.PROC, this);
//    	Logger.debug("Mode " + m_mode, Level.PROC, this);
//    	Logger.debug("SO: " + isSteppingOver(), Level.PROC, this);
//    	Logger.debug("--------------------------------------------------", Level.PROC, this);

		// If we are at step INTO once, increase the reference depth
		if (m_mode.equals(StepOverMode.STEP_INTO_ONCE) && m_prevMode.equals(StepOverMode.STEP_OVER_ALWAYS))
		{
			m_referenceDepth++;
		}
		
    	// Reset the temporary modes
    	if (m_mode.equals(StepOverMode.STEP_INTO_ONCE))
    	{
			m_mode = m_prevMode;
    	}

//    	Logger.debug("--------------------------------------------------", Level.PROC, this);
//    	Logger.debug("After On Call", Level.PROC, this);
//    	Logger.debug("Current depth " + m_currentDepth, Level.PROC, this);
//    	Logger.debug("Reference depth " + m_referenceDepth, Level.PROC, this);
//    	Logger.debug("Mode " + m_mode, Level.PROC, this);
//    	Logger.debug("SO: " + isSteppingOver(), Level.PROC, this);
//    	Logger.debug("--------------------------------------------------", Level.PROC, this);
//    	Logger.debug("", Level.PROC, this);
//    	Logger.debug("", Level.PROC, this);
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
    void onExecutionLine()
    {
    	// Reset temporary modes if they did not have any effect
    	if (m_mode != null)
    	{
//        	Logger.debug("--------------------------------------------------", Level.PROC, this);
//        	Logger.debug("Before On Line", Level.PROC, this);
//        	Logger.debug("Current depth " + m_currentDepth, Level.PROC, this);
//        	Logger.debug("Reference depth " + m_referenceDepth, Level.PROC, this);
//        	Logger.debug("Mode " + m_mode, Level.PROC, this);
//	    	Logger.debug("SO: " + isSteppingOver(), Level.PROC, this);
//        	Logger.debug("--------------------------------------------------", Level.PROC, this);

	    	if (m_mode.equals(StepOverMode.STEP_OVER_ONCE) && (m_currentDepth == m_referenceDepth))
	    	{
				m_mode = m_prevMode;
				// If we were in STEP_INTO_ALWAYS remove the reference that was put by the temporary
				// step over
				if (m_mode.equals(StepOverMode.STEP_INTO_ALWAYS))
				{
					m_referenceDepth = -1;
				}
	    	}
	    	else if (m_mode.equals(StepOverMode.STEP_INTO_ONCE))
	    	{
				m_mode = m_prevMode;
	    	}
	
//	    	Logger.debug("--------------------------------------------------", Level.PROC, this);
//	    	Logger.debug("After On Line", Level.PROC, this);
//	    	Logger.debug("Current depth " + m_currentDepth, Level.PROC, this);
//	    	Logger.debug("Reference depth " + m_referenceDepth, Level.PROC, this);
//	    	Logger.debug("Mode " + m_mode, Level.PROC, this);
//	    	Logger.debug("SO: " + isSteppingOver(), Level.PROC, this);
//	    	Logger.debug("--------------------------------------------------", Level.PROC, this);
//	    	Logger.debug("", Level.PROC, this);
//	    	Logger.debug("", Level.PROC, this);
    	}
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
    void onExecutionReturn()
    {
//    	Logger.debug("--------------------------------------------------", Level.PROC, this);
//    	Logger.debug("Before On Return", Level.PROC, this);
//    	Logger.debug("Current depth " + m_currentDepth, Level.PROC, this);
//    	Logger.debug("Reference depth " + m_referenceDepth, Level.PROC, this);
//    	Logger.debug("Mode " + m_mode, Level.PROC, this);
//    	Logger.debug("SO: " + isSteppingOver(), Level.PROC, this);
//    	Logger.debug("--------------------------------------------------", Level.PROC, this);

    	m_currentDepth--;
		if (m_currentDepth < m_referenceDepth) 
		{
			m_referenceDepth--;
		}
		else if (m_referenceDepth == m_currentDepth)
		{
			Logger.debug("Go back to mode " + m_prevMode, Level.PROC, this);
			m_referenceDepth = -1;
			m_mode = m_prevMode;
		}

//    	Logger.debug("--------------------------------------------------", Level.PROC, this);
//    	Logger.debug("After On Return", Level.PROC, this);
//    	Logger.debug("Current depth " + m_currentDepth, Level.PROC, this);
//    	Logger.debug("Reference depth " + m_referenceDepth, Level.PROC, this);
//    	Logger.debug("Mode " + m_mode, Level.PROC, this);
//    	Logger.debug("SO: " + isSteppingOver(), Level.PROC, this);
//    	Logger.debug("--------------------------------------------------", Level.PROC, this);
//    	Logger.debug("", Level.PROC, this);
//    	Logger.debug("", Level.PROC, this);
    }
}
