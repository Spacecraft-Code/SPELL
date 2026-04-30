// ################################################################################
// FILE       : SPELLmutexPOSIX.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the mutex mechanism for POSIX
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
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_SYN/SPELLmutexPOSIX.H"
// Project includes --------------------------------------------------------

//=============================================================================
// CONSTRUCTOR : SPELLmutexPOSIX::SPELLmutexPOSIX
//=============================================================================
SPELLmutexPOSIX::SPELLmutexPOSIX()
: SPELLmutexIF()
{
	m_threadId = 0;
    pthread_mutexattr_t attr;
    pthread_mutexattr_init(&attr);
    pthread_mutexattr_settype(&attr, PTHREAD_MUTEX_RECURSIVE);
    pthread_mutex_init(&m_mutex, &attr);
}

//=============================================================================
// DESTRUCTOR : SPELLmutexPOSIX::~SPELLmutexPOSIX
//=============================================================================
SPELLmutexPOSIX::~SPELLmutexPOSIX()
{
    errno = 0;
    pthread_mutex_destroy(&m_mutex);
}

//=============================================================================
// METHOD    : SPELLmutexPOSIX::lock
//=============================================================================
void SPELLmutexPOSIX::lock()
{
    errno = 0;
    pthread_mutex_lock(&m_mutex);
}

//=============================================================================
// METHOD    : SPELLmutexPOSIX::trylock
//=============================================================================
bool SPELLmutexPOSIX::trylock()
{
    errno = 0;
    int result = pthread_mutex_trylock(&m_mutex);
    if (result == EBUSY)
    {
    	return false;
    }
    else if (result == 0)
    {
    	return true;
    }
    return false;
}

//=============================================================================
// METHOD    : SPELLmutexPOSIX::unlock
//=============================================================================
void SPELLmutexPOSIX::unlock()
{
    errno = 0;
    pthread_mutex_unlock(&m_mutex);
}
