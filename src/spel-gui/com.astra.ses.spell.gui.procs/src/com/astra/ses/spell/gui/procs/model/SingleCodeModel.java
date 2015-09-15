///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : SingleCodeModel.java
//
// DATE      : May 24, 2013
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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.types.BreakpointType;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.interfaces.model.ICodeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.ICodeModel;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

public class SingleCodeModel implements ICodeModel 
{
	/** Holds the current code identifier */
	private String m_codeId;
	/** Holds code lines */
	private List<ICodeLine> m_lines;
	/** True if in replay mode */
	private boolean m_replay;
	/** Reference to the model */
	private IProcedure m_model;
	
	
	/**************************************************************************
	 * Constructor
	 * @param instanceId
	 * @param model
	 *************************************************************************/
	public SingleCodeModel( String codeId, IProcedure model )
	{
		Logger.debug("Created: " + codeId, Level.PROC, this);
		int idx = codeId.indexOf("#");
		if (idx != -1)
		{
			codeId = codeId.substring(0, idx);
		}
		m_codeId = codeId;
		m_model = model;
		m_lines = new LinkedList<ICodeLine>();
		m_replay = false;
	}
	
	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.procs.model.ICodeModel#getCodeId()
     */
    @Override
    public String getCodeId()
    {
	    return m_codeId;
    }

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.procs.model.ICodeModel#getLine(int)
     */
    @Override
    public ICodeLine getLine(int lineNo)
    {
    	if (m_lines.size()>lineNo)
    	{
    		return m_lines.get(lineNo);
    	}
    	return null;
    }

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.procs.model.ICodeModel#getLines()
     */
    @Override
    public List<ICodeLine> getLines()
    {
	    return m_lines;
    }

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.procs.model.ICodeModel#initialize(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void initialize(IProgressMonitor monitor)
    {
		Logger.debug("Initializing " + m_codeId, Level.PROC, this);
	    String[] lines = m_model.getSourceCodeProvider().getSource(m_codeId, monitor);
	    if (lines != null)
	    {
		    int index = 1;
		    for(String source : lines)
		    {
		    	ICodeLine codeLine = new CodeLine(m_codeId, index, source);
		    	getLines().add(codeLine);
		    	index++;
		    }
			Logger.debug("Code " + m_codeId + " initialized, lines " + getLines().size(), Level.PROC, this);
	    }
	    else
	    {
			Logger.error("Code " + m_codeId + " NOT initialized: " + m_codeId, Level.PROC, this);
	    }
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
    public boolean isInReplay()
    {
	    return m_replay;
    }
	
	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.procs.model.ICodeModel#clearNotifications()
     */
	@Override
    public void clearNotifications()
	{
		for(ICodeLine line : getLines())
		{
			line.clearNotifications();
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
    void onItemNotification( int lineNo, ItemNotification data , List<ICodeLine> updatedLines )
    {
		ICodeLine line = getLine(lineNo);
		if (line == null)
		{
			Logger.error("Cannot place item notification, no such line (" + m_codeId + ":" + lineNo + ")", Level.PROC, this);
			return;
		}
		line.onItemNotification(data);
		//notifyItemChanged(line);
		updatedLines.add(line);
    }
	
	/**************************************************************************
	 * 
	 *************************************************************************/
    void onStackNotification( int lineNo )
    {
		ICodeLine line = getLine(lineNo);
		if (line == null)
		{
			Logger.error("Cannot place stack notification, no such line (" + m_codeId + ":" + lineNo + ")", Level.PROC, this);
			return;
		}
		line.onExecuted();
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.procs.model.ICodeModel#reset()
     */
    @Override
    public void reset()
    {
		Logger.debug("Reset model", Level.PROC, this);
	    for(ICodeLine line : getLines())
	    {
	    	line.reset();
	    }
		m_replay = false;
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
    public void dispose()
    {
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
    public String toString()
    {
    	return "[CODE " + m_codeId + "]";
    }

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.procs.model.ICodeModel#clearBreakpoints()
     */
    @Override
    public void clearBreakpoints()
    {
		Logger.debug("Clear breakpoints", Level.PROC, this);
	    for(ICodeLine line : getLines())
	    {
	    	line.removeBreakpoint();
	    }
    }

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.procs.model.ICodeModel#setBreakpoint(int, com.astra.ses.spell.gui.core.model.types.BreakpointType)
     */
    @Override
    public void setBreakpoint(int lineNo, BreakpointType type)
    {
		Logger.debug("Set breakpoint on line " + lineNo, Level.PROC, this);
	    getLine(lineNo).setBreakpoint(type);
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
    public void setReplay(boolean replay)
    {
		Logger.debug("Set replay mode: " + replay, Level.PROC, this);
	    m_replay = replay;
    }

}
