// ################################################################################
// FILE       : SPELLwsFrame.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the frame data manager
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2015 SES ENGINEERING, Luxembourg S.A.R.L.
//
//  This file is part of SPELL.
//
// SPELL is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// SPELL is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with SPELL. If not, see <http://www.gnu.org/licenses/>.
//
// ################################################################################

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_WS/SPELLwsFrame.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// System includes ---------------------------------------------------------



//=============================================================================
// CONSTRUCTOR: SPELLwsFrame::SPELLwsFrame
//=============================================================================
SPELLwsFrame::SPELLwsFrame( const std::string& id, const SPELLwsStartupInfo& info, unsigned int depth, PyFrameObject* frame )
{
	DEBUG("[FRM] Construct");
	// If mode is recover and depth > 0, 'frame' contains the f_back frame.
	// If depth is zero and mode is recover, we just need to fix the dynamics.
	// If mode is other, 'frame' contains the frame associated to this structure.
	if (info.performRecovery)
	{
		DEBUG("[FRM] Recovering " + ISTR(depth) + " level frame id '" + id + "'");
		m_static = new SPELLwsStaticData(info,depth,frame);
		// Recover this frame
		m_frame = m_static->restore();
		// In recovery mode, the given frame is the previous one in the stack
		m_frame->f_back = frame;
		// Use the recovered frame to restore dynamic data
		m_dynamic = new SPELLwsDynamicData(info,depth,m_frame);
		m_dynamic->restore();
		DEBUG("[FRM] Recovering " + ISTR(depth) + " level frame done: " + PYCREPR(m_frame));
	}
	else
	{
		DEBUG("[FRM] Restoring " + ISTR(depth) + " level frame id '" + id + "'");
		m_frame = frame;
		m_static = new SPELLwsStaticData(info,depth,m_frame);
		m_dynamic = new SPELLwsDynamicData(info,depth,m_frame);
	}
	DEBUG("[FRM] Created manager for frame " + PYCREPR(m_frame) + "with identifier " + id);
	m_codeId = id;
	m_lastInstruction = m_frame->f_lasti;
	m_lastLine = m_frame->f_lineno;
}

//=============================================================================
// DESTRUCTOR: SPELLwsFrame::~SPELLwsFrame
//=============================================================================
SPELLwsFrame::~SPELLwsFrame()
{
	DEBUG("[FRM] Destroyed manager for frame " + PYCREPR(m_frame));
	delete m_dynamic;
	delete m_static;
}

//=============================================================================
// METHOD    : SPELLwsFrame::eventLine()
//=============================================================================
void SPELLwsFrame::eventLine()
{
	// On a line event we need to keep the latest instruction and line number used
	// so that we can reapply them after a recovery.
	m_lastInstruction = m_frame->f_lasti;
	m_lastLine = m_frame->f_lineno;

	DEBUG("[FRM] Frame " + PYCREPR(m_frame) + ": INS(" + ISTR(m_lastInstruction) + "), LIN(" + ISTR(m_lastLine) + ")");

	// Update the tracked dynamic data of the frame
	m_dynamic->update();

	DEBUG("[FRM] Update on line event finished");
}

//=============================================================================
// METHOD    : SPELLwsFrame::fixState()
//=============================================================================
void SPELLwsFrame::cleanup()
{
	m_dynamic->cleanup();
	m_static->cleanup();
}

//=============================================================================
// METHOD    : SPELLwsFrame::fixState()
//=============================================================================
void SPELLwsFrame::fixState( PyThreadState* newState, bool isHead )
{
	DEBUG("[FRM] Fix state on frame " + PYCREPR(m_frame) + ", head=" + BSTR(isHead));
	// This is required due to how the Python evaluation loop works. The
	// instruction interesting for us is the one after the function call, if
	// the frame is no the head of the tree.
	if (isHead)
	{
		DEBUG("[FRM] Set instruction as head");
		m_lastInstruction--;
	}
	else
	{
		DEBUG("[FRM] Set instruction as intermediate");
		DEBUG("[FRM] Original instruction was " + ISTR(m_lastInstruction));
		DEBUG("[FRM] Last line was " + ISTR(m_lastLine));

		int nextLine = SPELLexecutor::instance().getFrameManager().getModel(getCodeId()).lineAfter(m_lastLine);
		DEBUG("[FRM] Next line is " + ISTR(nextLine));
		int nextInstruction = SPELLexecutor::instance().getFrameManager().getModel(getCodeId()).offset(nextLine);
		m_lastInstruction = nextInstruction -1; // Will position it in the lastLine but POP_TOP instr.
		DEBUG("[FRM] Set instruction to: " + ISTR(m_lastInstruction));
	}

	DEBUG("[FRM] Applying: INS(" + ISTR(m_lastInstruction) + "), LIN(" + ISTR(m_lastLine) + ") on frame " + PYCREPR(m_frame));

	// Reset the frame values
	m_frame->f_lasti = m_lastInstruction;
	m_frame->f_lineno = m_lastLine;
	m_frame->f_tstate = newState;
	m_frame->f_stacktop = m_frame->f_valuestack;
	// Reset exception trace
	Py_XDECREF(m_frame->f_exc_traceback);
	Py_XDECREF(m_frame->f_exc_type);
	Py_XDECREF(m_frame->f_exc_value);
	m_frame->f_exc_traceback = NULL;
	m_frame->f_exc_type = NULL;
	m_frame->f_exc_value = NULL;

	// Recover the dynamic data and update the frame
	DEBUG("[FRM] Recovering dynamic data");
	m_dynamic->recover();

	// Connect the head with the thread state (the head is the frame going to
	// be executed after recovery)
	if (isHead)
	{
		newState->frame = m_frame;
	}

	DEBUG("[FRM] State fixed on frame " + PYCREPR(m_frame));
}

//=============================================================================
// METHOD    : SPELLwsFrame::reset()
//=============================================================================
void SPELLwsFrame::reset()
{
	m_dynamic->reset();
}

//=============================================================================
// METHOD    : SPELLwsFrame::saveState()
//=============================================================================
void SPELLwsFrame::saveState()
{
	// Save the status in memory and persistent file
	m_dynamic->save();
	// Static data is not to be saved here
}
