// ################################################################################
// FILE       : SPELLconditionPOSIX.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of condition mechanism for POSIX
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
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_SYN/SPELLconditionPOSIX.H"
#include "SPELL_UTIL/SPELLlog.H"
// Project includes --------------------------------------------------------

//=============================================================================
// CONSTRUCTOR : SPELLconditionPOSIX::SPELLconditionPOSIX
//=============================================================================
SPELLconditionPOSIX::SPELLconditionPOSIX()
{
    pthread_cond_init(&m_condition, NULL);
}

//=============================================================================
// DESTRUCTOR : SPELLconditionPOSIX::~SPELLconditionPOSIX
//=============================================================================
SPELLconditionPOSIX::~SPELLconditionPOSIX()
{
    pthread_cond_destroy(&m_condition);
}

//=============================================================================
// METHOD    : SPELLconditionPOSIX::signla
//=============================================================================
void SPELLconditionPOSIX::signal()
{
    pthread_cond_broadcast(&m_condition);
}

//=============================================================================
// METHOD    : SPELLconditionPOSIX::unlock
//=============================================================================
void SPELLconditionPOSIX::wait( SPELLmutex* m )
{
	SPELLmutexPOSIX* pmutex = dynamic_cast<SPELLmutexPOSIX*>(m->getImpl());
    pthread_cond_wait(&m_condition, pmutex->get());
}

//=============================================================================
// METHOD    : SPELLconditionPOSIX::unlock
//=============================================================================
bool SPELLconditionPOSIX::wait( SPELLmutex* m, unsigned long timeoutMsec )
{
    struct timespec abstime;
    clock_gettime(CLOCK_REALTIME, &abstime);
    abstime.tv_sec += timeoutMsec / 1000;
    abstime.tv_nsec += ( timeoutMsec  - (timeoutMsec / 1000)*1000 ) * 1000;
    SPELLmutexPOSIX* pmutex = dynamic_cast<SPELLmutexPOSIX*>(m->getImpl());
    assert(pmutex->get() != 0);
    int ret = pthread_cond_timedwait(&m_condition, pmutex->get(), &abstime);
    if (ret == ETIMEDOUT)
    {
        // Do nothing: return true
    }
    else if (ret == 0)
    {
        return false;
    }
    else if (ret == EINVAL)
    {
            LOG_ERROR("[SPELLconditionPOSIX.wait] Condition error EINVAL");
    }
    else if (ret == EPERM)
    {
        LOG_ERROR("[SPELLconditionPOSIX.wait] Condition error EPERM");
    } 
    return true;
}
