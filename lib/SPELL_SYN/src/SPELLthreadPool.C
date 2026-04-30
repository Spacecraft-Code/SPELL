// ################################################################################
// FILE       : SPELLthreadPool.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the thread pool mechanism
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
#include "SPELL_SYN/SPELLthreadPool.H"
#include "SPELL_SYN/SPELLsyncError.H"
#include "SPELL_SYN/SPELLmonitor.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLlog.H"
// System includes ---------------------------------------------------------


//=============================================================================
// CONSTRUCTOR : SPELLthreadPool::SPELLthreadPool
//=============================================================================
SPELLthreadPool::SPELLthreadPool( const std::string& id, unsigned int initialSize, unsigned int maxSize )
: m_id(id),
  m_initialSize(initialSize),
  m_maxSize(maxSize)
{
    DEBUG("[THR] Thread pool " + m_id + " created");
    // Create the worker threads
    for(unsigned int count = 0; count < initialSize; count++)
    {
    	SPELLthreadWorker* worker = new SPELLthreadWorker(id + "-" + ISTR(count));
    	worker->start();
    	m_threads.push_back(worker);
    }
}

//=============================================================================
// CONSTRUCTOR : SPELLthreadPool::SPELLthreadPool
//=============================================================================
SPELLthreadPool::~SPELLthreadPool()
{
    DEBUG("[THR] Thread pool " + m_id + " destroyed");
}

//=============================================================================
// METHOD : SPELLthreadPool::workToDo
//=============================================================================
void SPELLthreadPool::workToDo( SPELLthreadWork* work )
{
	SPELLmonitor m(m_lock);
	SPELLthreadWorker* thread = findFreeThread();
	while(thread == NULL)
	{
		::usleep(1000);
		thread = findFreeThread();
	}
	thread->assignWork(work);
}

//=============================================================================
// METHOD : SPELLthreadPool::shutdown()
//=============================================================================
void SPELLthreadPool::shutdown()
{
    DEBUG("[THR] Shutting down thread pool " + m_id);
	SPELLmonitor m(m_lock);
	std::list<SPELLthreadWorker*>::iterator it;
	for(it = m_threads.begin(); it != m_threads.end(); it++)
	{
		(*it)->shutdown();
		try
		{
			(*it)->join();
		}
		catch(...){;};
	}
	for(it = m_threads.begin(); it != m_threads.end(); it++)
	{
		delete *it;
	}
	m_threads.clear();
    DEBUG("[THR] Thread pool shutdown done");
}

//=============================================================================
// METHOD : SPELLthreadPool::findFreeThread()
//=============================================================================
SPELLthreadWorker* SPELLthreadPool::findFreeThread()
{
	std::list<SPELLthreadWorker*>::iterator it;
	SPELLthreadWorker* thread = NULL;
	for(it = m_threads.begin(); it != m_threads.end(); it++)
	{
		if ((*it)->isFree())
		{
			thread = *it;
			break;
		}
	}
	if (thread == NULL && m_threads.size() < m_maxSize)
	{
		thread = new SPELLthreadWorker(m_id + "-" + ISTR(m_threads.size()));
		m_threads.push_back(thread);
	}
	return thread;
}
