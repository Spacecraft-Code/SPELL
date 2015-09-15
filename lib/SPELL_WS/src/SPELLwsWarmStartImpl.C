// ################################################################################
// FILE       : SPELLwsWarmStartImpl.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the warm start mechanism controller
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
#include "SPELL_WS/SPELLwsWarmStartImpl.H"
#include "SPELL_WS/SPELLwsDictDataHandler.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_WRP/SPELLconstants.H"
// System includes ---------------------------------------------------------


PyObject* SPELLwsWarmStartImpl::s_filterKeys = 0;

//=============================================================================
// CONSTRUCTOR: SPELLwsWarmStartImpl::SPELLwsWarmStartImpl
//=============================================================================
SPELLwsWarmStartImpl::SPELLwsWarmStartImpl()
: SPELLwarmStart()
{
	DEBUG("[WS] WarmStart created");
	m_recursionDepth = 0;
	m_topFrame = NULL;
	m_storage = NULL;
}

//=============================================================================
// DESTRUCTOR: SPELLwsWarmStartImpl::~SPELLwsWarmStartImpl
//=============================================================================
SPELLwsWarmStartImpl::~SPELLwsWarmStartImpl()
{
	DEBUG("[WS] WarmStart destroyed");
}

//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::initialize()
//=============================================================================
void SPELLwsWarmStartImpl::initialize( const SPELLwsStartupInfo& info )
{
	m_startup = info;

	m_persistentFile = m_startup.persistentFile + ".wsp";
	m_recoveryFile = m_startup.recoveryFile + ".wsp";

	if (m_startup.performRecovery)
	{
		LOG_INFO("Warm start using recovery file: " + m_recoveryFile);
		DEBUG("[WS] Initialize in recovery mode");
		m_storage = new SPELLwsStorage( m_recoveryFile, SPELLwsStorage::MODE_READ );
	}
	else
	{
		LOG_INFO("Warm start using persistent file: " + m_persistentFile);
		DEBUG("[WS] Initialize in save mode");
		m_storage = new SPELLwsStorage( m_persistentFile, SPELLwsStorage::MODE_WRITE );
	}
	DEBUG("[WS] WarmStart initialized");
}
//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::reset()
//=============================================================================
void SPELLwsWarmStartImpl::reset()
{
	DEBUG("[WS] Reset mechanism");
	while(m_frames.size()>1) removeTopFrame();
	m_recursionDepth = 0;
	m_topFrame = NULL;
	DEBUG("[WS] Reset mechanism done");
}

//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::reset()
//=============================================================================
void SPELLwsWarmStartImpl::cleanup()
{
	DEBUG("[WS] WarmStart cleanup");
	// Perform state save
	unsigned int frameCount = m_frames.size();
	for( unsigned int index = 0; index < frameCount; index++)
	{
		m_frames[index]->cleanup();
	}
	SPELLutils::deleteFile( m_persistentFile );
}
//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::notifyCall()
//=============================================================================
void SPELLwsWarmStartImpl::notifyCall( const std::string& id, PyFrameObject* newFrame )
{
	DEBUG("[WS] Notify call on " + PYCREPR(newFrame) + ", recursion depth " + ISTR(m_recursionDepth) +", id=" + id);
	// Add the frame to the list of frames
	addFrame( id, newFrame );
}

//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::notifyReturn()
//=============================================================================
void SPELLwsWarmStartImpl::notifyReturn()
{
	DEBUG("[WS] Notify return");
	// Remove the frame at the top of the tree, we dont need it anymore
	// But do not remove the very first frame
	while(m_frames.size()>1) removeTopFrame();
}

//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::notifyLine()
//=============================================================================
void SPELLwsWarmStartImpl::notifyLine()
{
	DEBUG("[WS] Notify line, top frame " + PYCREPR(m_topFrame->getFrameObject()));
	// Notify the top frame to keep updated the recovery information
	m_topFrame->eventLine();
	// Perform state save if working mode is ON_LINE
	if (getWorkingMode()==MODE_ON_LINE)
	{
		saveState();
	}
}

//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::notifyStage()
//=============================================================================
void SPELLwsWarmStartImpl::notifyStage()
{
	DEBUG("[WS] Notify stage");
	// Perform state save if working mode is ON_STAGE
	if (getWorkingMode()==MODE_ON_STEP)
	{
		saveState();
	}
}

//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::saveState()
//=============================================================================
void SPELLwsWarmStartImpl::saveState()
{
	DEBUG("[WS] Saving state ============================================");

	std::cerr << "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" << std::endl;
	std::cerr << "SAVE STATE START" << std::endl;
	std::cerr << "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" << std::endl;

	// Synchronize so that nothing can be done while saving
	SPELLmonitor m(m_lock);

	SPELLsafePythonOperations ops("SPELLwsWarmstartImpl::saveState()");

	//TODO check, possibly there are several states that need to be saved
	PyThreadState* state = m_topFrame->getFrameObject()->f_tstate;
	DEBUG("Current thread state: " + PSTR(state));

	assert(state != NULL);
	// Reset the storage. Remove this if we want the full history, but
	// the load algorith would need to be modified.
	m_storage->reset();
	DEBUG("    - Recursion depth : " + ISTR(state->recursion_depth));
	m_storage->storeLong( state->recursion_depth );
	DEBUG("    - Tick counter    : " + ISTR(state->tick_counter));
	m_storage->storeLong( state->tick_counter );
	DEBUG("    - GIL counter     : " + ISTR(state->gilstate_counter));
	m_storage->storeLong( state->gilstate_counter );
	DEBUG("    - Number of frames: " + ISTR(m_frames.size()));
	m_storage->storeLong( m_frames.size() );

	DEBUG("[WS] Saving frames");
	unsigned int frameCount = m_frames.size();
	for( unsigned int index = 0; index < frameCount; index++)
	{
		std::string id = m_frames[index]->getCodeId();
		DEBUG("   - Saving frame '" + id + "'");
		m_storage->storeObject(SSTRPY(id));
		m_frames[index]->saveState();
	}

	std::cerr << "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" << std::endl;
	std::cerr << "SAVE STATE END" << std::endl;
	std::cerr << "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" << std::endl;

	DEBUG("[WS] Saving state done =======================================");
}

//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::restoreState()
//=============================================================================
PyFrameObject* SPELLwsWarmStartImpl::restoreState()
{
	DEBUG("[WS] Restoring state ========================================");

	// We need a separate scope so that we can invoke saveState at the end without deadlock
	{
		std::cerr << "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" << std::endl;
		std::cerr << "RESTORE STATE START" << std::endl;
		std::cerr << "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" << std::endl;

		// Synchronize so that nothing can be done while saving
		SPELLmonitor m(m_lock);

		SPELLsafePythonOperations ops("SPELLwsWarmStartImpl::restoreState()");

		// Create a fresh thread state
		PyThreadState* tstate = PyThreadState_Get();

		DEBUG("[WS] Restoring interpreter parameters");

		tstate->recursion_depth = m_storage->loadLong();
		DEBUG("    - Recursion depth : " + ISTR(tstate->recursion_depth));
		tstate->tick_counter = m_storage->loadLong();
		DEBUG("    - Tcik counter    : " + ISTR(tstate->tick_counter));
		tstate->gilstate_counter = m_storage->loadLong();
		DEBUG("    - GIL counter     : " + ISTR(tstate->gilstate_counter));
		int numFrames = m_storage->loadLong();
		DEBUG("    - Number of frames: " + ISTR(numFrames));

		if (numFrames < 0)
		{
			THROW_EXCEPTION("Unable to restore state", "Failed to restore interpreter parameters", SPELL_ERROR_WSTART);
		}

		DEBUG("[WS] Restoring frames");
		m_frames.clear();
		m_recursionDepth = 0;

		// Restore the frames now. Consider that the top frame is already there

		// Use the originally created frame to copy globals
		PyFrameObject* prevFrame = NULL;

		for( int count = 0; count < numFrames; count++ )
		{
			DEBUG("[WS] Restoring frame " + ISTR(count));
			// In recursion depth zero, use the original frame
			// to copy the globals from
			DEBUG("[WS] X00");
			PyObject* id = m_storage->loadObject();
			DEBUG("[WS] X01");
			std::string frameId = PYSSTR(id);
			DEBUG("[WS] Frame identifier: " + frameId);
			m_topFrame = new SPELLwsFrame( frameId, m_startup, m_frames.size(), prevFrame );
			DEBUG("[WS] X1");
			m_frames.push_back(m_topFrame);
			// For the head frame, store its address in the interpreter thread state
			if (m_recursionDepth == 0)
			{
				if (tstate->frame) delete tstate->frame;
				tstate->frame = m_topFrame->getFrameObject();
			}
			DEBUG("[WS] X2");
			m_recursionDepth++;
			prevFrame = m_topFrame->getFrameObject();
			DEBUG("[WS] X3");
		}
		// Update the recursion depth
		tstate->recursion_depth = m_recursionDepth;

		DEBUG("[WS] Checking errors");
		SPELLpythonHelper::instance().checkError();

		DEBUG("[WS] Switching to save state mode");
		// Reset the storage now, to mode write
		delete m_storage;
		m_storage = new SPELLwsStorage( m_persistentFile, SPELLwsStorage::MODE_WRITE );

		std::cerr << "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" << std::endl;
		std::cerr << "RESTORE STATE END" << std::endl;
		std::cerr << "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" << std::endl;

	}

	// Re-save the current state, so that the new persistent files are ok
	saveState();

	DEBUG("[WS] Checking errors");
	SPELLpythonHelper::instance().checkError();

	// Reset values
	m_startup.recoveryFile = "";
	m_startup.performRecovery = false;

	DEBUG("[WS] State recovered ===========================================");

	return m_topFrame->getFrameObject();
}

//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::fixState()
//=============================================================================
PyFrameObject* SPELLwsWarmStartImpl::fixState()
{
	DEBUG("[WS] Fixing state ==============================================");

	// Synchronize so that nothing can be done while saving
	SPELLmonitor m(m_lock);

	// Get the head interpreter state
	PyInterpreterState* istate = PyInterpreterState_Head();

	// Get the current thread state
	PyThreadState* oldState = PyThreadState_GET();
	DEBUG("[WS] Old state: " + PSTR(oldState));
	DEBUG("[WS] Interpreter head: " + PSTR(istate->tstate_head));
	DEBUG("[WS] Interpreter next: " + PSTR(istate->next));
	DEBUG("[WS] State recursion depth: " + ISTR(oldState->recursion_depth));
	DEBUG("[WS] State next: " + PSTR(oldState->next));

	// Create a fresh thread state
	PyThreadState* newState = PyThreadState_New(istate);
	istate->tstate_head = newState;

	newState->recursion_depth = oldState->recursion_depth;
	newState->tracing = oldState->tracing;
	newState->use_tracing = oldState->use_tracing;
	newState->tick_counter = oldState->tick_counter;
	newState->gilstate_counter = oldState->gilstate_counter;
	newState->dict = PyDict_Copy(oldState->dict);

	FrameList::iterator it;
	unsigned int frameCount = m_frames.size();
	DEBUG("[WS] Total frames to fix " + ISTR(frameCount));
	m_topFrame = NULL;
	for( unsigned int index = 0; index < frameCount; index++)
	{
		bool isHead = (index == (frameCount-1));
		DEBUG("[WS] Fix state on frame index " + ISTR(index) + " frame=" + PYCREPR(m_frames[index]));
		m_topFrame = m_frames[index];
		m_topFrame->fixState(newState, isHead);
	}
	DEBUG("[WS] State fixed ===============================================");
	PyErr_Clear();
	return m_topFrame->getFrameObject();
}

//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::addFrame()
//=============================================================================
void SPELLwsWarmStartImpl::addFrame( const std::string& id, PyFrameObject* frame )
{
	DEBUG("[WS] Adding new frame");
	// Dont actually add it if it is the head (this happens after fixing the state)
	if ( m_topFrame == NULL || frame != m_topFrame->getFrameObject() )
	{
		DEBUG("[WS] Adding frame manager for " + PYCREPR(frame));
		m_topFrame = new SPELLwsFrame( id, m_startup, m_frames.size(), frame );
		m_frames.push_back(m_topFrame);
		m_recursionDepth++;
	}
}

//=============================================================================
// METHOD    : SPELLwsWarmStartImpl::removeTopFrame()
//=============================================================================
void SPELLwsWarmStartImpl::removeTopFrame()
{
	DEBUG("[WS] Removing top frame manager");
	// Delete the top frame
	m_topFrame->cleanup();
	delete m_topFrame;
	// Remove the list element
	FrameList::iterator it = m_frames.end();
	it--; //Point to the last frame
	m_frames.pop_back();
	// Get the new top frame now
	it = m_frames.end();
	it--; //Point to the last frame
	m_topFrame = (*it);
	m_recursionDepth--;
}

//=============================================================================
// STATIC : SPELLwsWarmStartImpl::setGlobalsFilter()
//=============================================================================
void SPELLwsWarmStartImpl::setGlobalsFilter( PyObject* filterKeys )
{
	s_filterKeys = PySet_New( filterKeys );
}

//=============================================================================
// STATIC : SPELLwsWarmStartImpl::shouldFilter()
//=============================================================================
bool SPELLwsWarmStartImpl::shouldFilter( PyObject* key, PyObject* item )
{
	bool doFilter = false;
	// Do not consider modules
	if (PyModule_Check(item))
	{
		doFilter = true;
	}
	// Do not consider callables
	else if (PyCallable_Check(item))
	{
		doFilter = true;
	}
	// Do not consider functions
	else if (PyFunction_Check(item))
	{
		doFilter = true;
	}
	else if ( PYREPR(key) == DatabaseConstants::SCDB )
	{
		doFilter = true;
	}
	else if ( PYREPR(key) == DatabaseConstants::GDB )
	{
		doFilter = true;
	}
	else if ( PYREPR(key) == DatabaseConstants::PROC )
	{
		doFilter = true;
	}
	if ((!doFilter) && (s_filterKeys != NULL))
	{
		// If the set contains the key, must filter it
		int contains = PySet_Contains( s_filterKeys, key );
		doFilter = (contains!=0);
	}
	return doFilter;
}
