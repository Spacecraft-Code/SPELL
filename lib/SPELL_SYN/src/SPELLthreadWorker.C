// ################################################################################
// FILE       : SPELLthreadWorker.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the thread worker mechanism
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
#include "SPELL_SYN/SPELLthreadWorker.H"
#include "SPELL_SYN/SPELLsyncError.H"
#include "SPELL_SYN/SPELLmonitor.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLlog.H"
// System includes ---------------------------------------------------------


//=============================================================================
// CONSTRUCTOR : SPELLthreadWorker::SPELLthreadWorker
//=============================================================================
SPELLthreadWorker::SPELLthreadWorker( const std::string& id )
: SPELLthread(id)
{
    m_work = NULL;
    m_shutdown = false;
    m_newWork.clear();
    m_busy = false;
}

//=============================================================================
// CONSTRUCTOR : SPELLthreadWorker::SPELLthreadWorker
//=============================================================================
SPELLthreadWorker::~SPELLthreadWorker()
{
	if (m_work)
	{
		delete m_work;
	}
}

//=============================================================================
// METHOD: SPELLthreadWorker::isFree
//=============================================================================
bool SPELLthreadWorker::isFree()
{
    SPELLmonitor m(m_lock);
    return (m_work == NULL);
}

//=============================================================================
// METHOD: SPELLthreadWorker::isShutdown
//=============================================================================
bool SPELLthreadWorker::isShutdown()
{
    SPELLmonitor m(m_lock);
    return m_shutdown;
}

//=============================================================================
// METHOD: SPELLthreadWorker::shutdown()
//=============================================================================
void SPELLthreadWorker::shutdown()
{
    SPELLmonitor m(m_lock);
    m_shutdown = true;
    if (m_work != NULL)
    {
    	delete m_work;
    	m_work = NULL;
    }
    m_newWork.set();
}

//=============================================================================
// METHOD: SPELLthreadWorker::assignWork()
//=============================================================================
void SPELLthreadWorker::assignWork( SPELLthreadWork* work )
{
    SPELLmonitor m(m_lock);
    m_work = work;
    m_newWork.set();
}

//=============================================================================
// METHOD: SPELLthreadWorker::getWork()
//=============================================================================
SPELLthreadWork* SPELLthreadWorker::getWork()
{
    SPELLmonitor m(m_lock);
    return m_work;
}

//=============================================================================
// METHOD: SPELLthreadWorker::deleteWork()
//=============================================================================
void SPELLthreadWorker::deleteWork()
{
    SPELLmonitor m(m_lock);
    if (m_work != NULL)
    {
    	delete m_work;
    	m_work = NULL;
    }
}

//=============================================================================
// METHOD: SPELLthreadWorker::run()
//=============================================================================
void SPELLthreadWorker::run()
{
	while(true)
	{
		m_newWork.wait();
		m_newWork.clear();
		if (!isShutdown())
		{
			getWork()->doWork();
		}
		deleteWork();
	}
}
