// ################################################################################
// FILE       : SPELLconditionPOSIX.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of condition mechanism for POSIX
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
#include "SPELL_SYN/SPELLconditionPOSIX.H"
#include "SPELL_UTIL/SPELLlog.H"
// Project includes --------------------------------------------------------

//=============================================================================
// CONSTRUCTOR : SPELLconditionPOSIX::SPELLconditionPOSIX
//=============================================================================
SPELLconditionPOSIX::SPELLconditionPOSIX()
{
    int result = pthread_cond_init(&m_condition, NULL);
    if (result == EAGAIN){
    	LOG_ERROR("[SPELLconditionPOSIX.create] Error creating condition! The system lacked the necessary resources (other than memory) to initialise another condition variable.");
    } else if (result == ENOMEM) {
    	LOG_ERROR("[SPELLconditionPOSIX.create] Error creating condition! Insufficient memory exists to initialise the condition variable.");
    } else if (result == EBUSY) {
    	LOG_ERROR("[SPELLconditionPOSIX.create] Error creating condition! The implementation has detected an attempt to re-initialise the object referenced by cond, a previously initialised, but not yet destroyed, condition variable.");
    } else if (result == EINVAL) {
    	LOG_ERROR("[SPELLconditionPOSIX.create] Error creating condition! The value specified by attr is invalid.");
    }
//#ifdef WITH_DEBUG
//    std::stringstream buf;
//    buf << "SPELLconditionPOSIX create " << &m_condition;
//    DEBUG( buf.str());
//#endif
}

//=============================================================================
// DESTRUCTOR : SPELLconditionPOSIX::~SPELLconditionPOSIX
//=============================================================================
SPELLconditionPOSIX::~SPELLconditionPOSIX()
{
//#ifdef WITH_DEBUG
//    std::stringstream buf;
//    buf << "SPELLconditionPOSIX destroy " << &m_condition;
//    DEBUG( buf.str());
//#endif
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
    if (abstime.tv_nsec >= 1000000000){
    	DEBUG("[SPELLconditionPOSIX.wait] abstime.tv_nsec is >= 1000000000. This may cause Condition error EINVAL. Value will be corrected.");
    	abstime.tv_sec += abstime.tv_nsec / 1000000000;
    	abstime.tv_nsec = abstime.tv_nsec % 1000000000;
    }
    if (abstime.tv_nsec < 0) {
    	DEBUG("[SPELLconditionPOSIX.wait] abstime.tv_nsec is < 0. This may cause Condition error EINVAL. Value will be corrected.");
    	abstime.tv_sec -= 1;
    	abstime.tv_nsec = (abstime.tv_nsec + 1000000000) % 1000000000;
    }
    SPELLmutexPOSIX* pmutex = dynamic_cast<SPELLmutexPOSIX*>(m->getImpl());
    assert(pmutex->get() != 0);
//#ifdef WITH_DEBUG
//    std::stringstream buf;
//    buf << "SPELLconditionPOSIX pthread_cond_timedwait " << &m_condition;
//    DEBUG( buf.str());
//#endif
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
