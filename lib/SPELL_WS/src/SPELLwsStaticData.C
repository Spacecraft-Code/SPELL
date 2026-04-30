// ################################################################################
// FILE       : SPELLwsStaticData.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the static data manager
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2018 SES ENGINEERING, Luxembourg S.A.R.L.
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
#include "SPELL_WS/SPELLwsStaticData.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// System includes ---------------------------------------------------------


//=============================================================================
// CONSTRUCTOR: SPELLwsStaticData::SPELLwsStaticData
//=============================================================================
SPELLwsStaticData::SPELLwsStaticData( const SPELLwsStartupInfo& info, unsigned int depth, PyFrameObject* frame )
: m_startup(info),
  m_depth(depth)
{

	if (m_startup.persistentFile != "")
	{
		m_persistentFile = m_startup.persistentFile + "_" + ISTR(depth) + ".wss";
		DEBUG("STC Using persistent file: '" + m_persistentFile + "'");
	}
	else
	{
		m_persistentFile = "";
	}

	if (m_startup.recoveryFile != "")
	{
		m_recoveryFile = m_startup.recoveryFile + "_" + ISTR(depth) + ".wss";
		DEBUG("STC Using recovery file: '" + m_recoveryFile + "'");
	}

	// In recovery mode this is the previous frame in the stack, it shall be updated later.
	m_frame = frame;

	// Do not use static data for the top level frame
	DEBUG("STC working mode: " + WorkingModeToString(m_startup.workingMode));
	DEBUG("STC perform recovery: " + BSTR(m_startup.performRecovery));

	if (m_startup.performRecovery && m_recoveryFile != "")
	{
		DEBUG("[STC] Created static data manager");
		m_storage = new SPELLwsStorage( m_recoveryFile, SPELLwsStorage::MODE_READ );
	}
	else if (m_persistentFile != "")
	{
		DEBUG("[STC] Created static data manager for frame " + PYCREPR(m_frame));
		m_storage = new SPELLwsStorage( m_persistentFile, SPELLwsStorage::MODE_WRITE );
		// Save the status right away
		save();
	}
}

//=============================================================================
// DESTRUCTOR: SPELLwsStaticData::~SPELLwsStaticData
//=============================================================================
SPELLwsStaticData::~SPELLwsStaticData()
{
	DEBUG("[STC] Destroyed static data manager for frame " + PYCREPR(m_frame));
}

//=============================================================================
// METHOD    : SPELLwsStaticData::cleanup()
//=============================================================================
void SPELLwsStaticData::cleanup()
{
	DEBUG("[STC] Remove static data file for frame " + PYCREPR(m_frame));
	if (m_persistentFile != "")
	{
		SPELLutils::deleteFile( m_persistentFile );
	}
}

//=============================================================================
// METHOD    : SPELLwsStaticData::restore()
//=============================================================================
PyFrameObject* SPELLwsStaticData::restore()
{
	if (!m_storage->isReady())
	{
		THROW_EXCEPTION("Failed to restore static data", "Storage not ready", SPELL_ERROR_WSTART);
	}

	DEBUG("[STC] Restoring frame information");
	PyCodeObject* code = (PyCodeObject*) m_storage->loadObject();
	DEBUG("[STC] Code loaded");

	// If we are in depth zero, just re-create the initial frame
	if (m_depth == 0)
	{
		DEBUG("[STC] Create head frame");
		m_frame = SPELLpythonHelper::instance().createFrame( m_startup.procedureFile, code );
		m_frame->f_back = NULL;
	}
	else
	{
		DEBUG("[STC] Create new stack frame");
		PyFrameObject* prevFrame = m_frame;
		// Copy the globals dictionary from the previous frame
		m_frame = PyFrame_New( PyThreadState_GET(), code, PyDict_Copy(prevFrame->f_globals), NULL );
		m_frame->f_back = prevFrame;
	}

	DEBUG("[STC] Restoring frame done");

	// Sanity check
	if (m_frame->f_globals == NULL)
	{
		THROW_EXCEPTION("Failed to restore static data", "Globals dictionary is missing", SPELL_ERROR_WSTART);
	}

	// Reset the storage to write mode and re-save the sate
	DEBUG("[STC] Switching to save state mode");
	delete m_storage;

	if (m_persistentFile != "")
	{
		m_storage = new SPELLwsStorage( m_persistentFile, SPELLwsStorage::MODE_WRITE );
		save();
	}

	return m_frame;
}

//=============================================================================
// METHOD    : SPELLwsStaticData::save()
//=============================================================================
void SPELLwsStaticData::save()
{
	DEBUG("[STC] Storing static data for frame " + PYCREPR(m_frame));
	m_storage->storeObject( (PyObject*) m_frame->f_code );
}

