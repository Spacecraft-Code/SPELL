// ################################################################################
// FILE       : SPELLtimer.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the timer mechanism
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
#include "SPELL_SYN/SPELLtimer.H"
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_SYN/SPELLsyncError.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"

//=============================================================================
// CONSTRUCTOR : SPELLtimer::SPELLtimer
//=============================================================================
SPELLtimer::SPELLtimer( unsigned long period, SPELLtimerListener& listener )
    : SPELLthread("timer"),
      m_listener(listener),
      m_stopEvent()
{
    m_counting = false;
    m_period   = period;
    m_elapsed  = 0;
    m_timeout  = 0;
    //DEBUG("[TMR] Timer created (1) " + PSTR(this))
}

//=============================================================================
// CONSTRUCTOR : SPELLtimer::SPELLtimer
//=============================================================================
SPELLtimer::SPELLtimer( unsigned long period, SPELLtimerListener& listener, unsigned long timeout )
    : SPELLthread("timer"),
      m_listener(listener)
{
    m_counting = false;
    m_listener = listener;
    m_period   = period;
    m_elapsed  = 0;
    m_timeout  = timeout;
    //DEBUG("[TMR] Timer created (2) " + PSTR(this))
}

//=============================================================================
// DESTRUCTOR : SPELLtimer::~SPELLtimer
//=============================================================================
SPELLtimer::~SPELLtimer()
{
    // Ensure we finish the thread
    m_counting = false;
    m_stopEvent.set();
}

//=============================================================================
// METHOD    : SPELLtimer::run
//=============================================================================
void SPELLtimer::run()
{
    //DEBUG("[TMR] Timer thread started: " + PSTR(this))
    setCounting(true);
    while(isCounting())
    {
        usleep(m_period*1000);// Period is given in msec
        if (callTimerCallback())
        {
            //DEBUG("[TMR] Ending timer thread (callback) for " + PSTR(this))
            return;
        }
        if (checkTimeout())
        {
            //DEBUG("[TMR] Ending timer thread (timeout) for " + PSTR(this))
            return;
        }
        //DEBUG("[TMR] Stop event wait in " + PSTR(this))
        wait();
        //DEBUG("[TMR] Stop event wait out " + PSTR(this))
    }
    //DEBUG("[TMR] Ending timer thread for " + PSTR(this))
}

//=============================================================================
// METHOD    : SPELLtimer::callTimerCallback
//=============================================================================
bool SPELLtimer::callTimerCallback()
{
    m_elapsed += m_period;
    bool result = m_listener.timerCallback(m_elapsed);
    if (result)
    {
        SPELLmonitor monitor(m_mutex);
        m_counting = false;
        if (m_stopEvent.isClear())
        {
            m_stopEvent.set();
        }
    }
    return result;
}

//=============================================================================
// METHOD    : SPELLtimer::checkTimeout
//=============================================================================
bool SPELLtimer::checkTimeout()
{
    if ((m_timeout>0)&& (m_elapsed>m_timeout))
    {
        return true;
    }
    return false;
}

//=============================================================================
// METHOD    : SPELLtimer::cancel
//=============================================================================
void SPELLtimer::cancel()
{
	//DEBUG("[TMR] Cancelling timer thread IN" + PSTR(this))
	setCounting(false);
	cont();
    //DEBUG("[TMR] Cancelling timer thread " + PSTR(this) + " OUT ")
}

//=============================================================================
// METHOD    : SPELLtimer::counting
//=============================================================================
bool SPELLtimer::isCounting()
{
    SPELLmonitor monitor(m_mutex);
    return m_counting;
}

//=============================================================================
// METHOD    : SPELLtimer::setCounting
//=============================================================================
void SPELLtimer::setCounting( bool counting )
{
    SPELLmonitor monitor(m_mutex);
    m_counting = counting;
}

//=============================================================================
// METHOD    : SPELLtimer::stop
//=============================================================================
void SPELLtimer::stop()
{
    //DEBUG("[TMR] Stopping timer IN" + PSTR(this))
    SPELLmonitor monitor(m_mutex);
    m_stopEvent.clear();
}

//=============================================================================
// METHOD    : SPELLtimer::cont
//=============================================================================
void SPELLtimer::cont()
{
    //DEBUG("[TMR] Continue timer IN" + PSTR(this))
    SPELLmonitor monitor(m_mutex);
    m_stopEvent.set();
}

//=============================================================================
// METHOD    : SPELLtimer::wait
//=============================================================================
void SPELLtimer::wait()
{
    //DEBUG("[TMR] Wait timer IN" + PSTR(this))
    m_stopEvent.wait();
}
