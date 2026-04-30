// ################################################################################
// FILE       : SPELLevent.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the event mechanism
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
#include "SPELL_SYN/SPELLevent.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"


//=============================================================================
// CONSTRUCTOR : SPELLevent::SPELLevent
//=============================================================================
SPELLevent::SPELLevent( bool log )
    : m_condition(),
      m_mutex()
{
	m_log = log;
    m_isClear = false;
    //DEBUG("[EVE] Event " + PSTR(this) + " created with mutex " + PSTR(&m_mutex))
	if (m_log) std::cerr << "   CREATE EVENT " << (void*) this << std::endl;
}

//=============================================================================
// DESTRUCTOR : SPELLevent::~SPELLevent
//=============================================================================
SPELLevent::~SPELLevent()
{
    //DEBUG("[EVE] Event " + PSTR(this) + " destroyed")
	if (m_log) std::cerr << "   DESTROY EVENT " << (void*) this << std::endl;
}

//=============================================================================
// METHOD    : SPELLevent::set
//=============================================================================
void SPELLevent::set()
{
	if (m_log) std::cerr << "SET EVENT TRY IN " << (void*) this << std::endl;
	//DEBUG("[EVE] Event " + PSTR(this) + " set")
	m_mutex.lock();
	if (m_isClear)
	{
		if (m_log) std::cerr << "   SET EVENT SIGNAL IN " << (void*) this << std::endl;
		m_isClear = false;
		m_condition.signal();
		if (m_log) std::cerr << "   SET EVENT SIGNAL OUT " << (void*) this << std::endl;
	}
	m_mutex.unlock();
	if (m_log) std::cerr << "SET EVENT OUT " << (void*) this << std::endl;
}

//=============================================================================
// METHOD    : SPELLevent::clear
//=============================================================================
void SPELLevent::clear()
{
	if (m_log) std::cerr << "CHECK CLEAR EVENT IN " << (void*) this << std::endl;
    //DEBUG("[EVE] Event " + PSTR(this) + " clear")
    m_mutex.lock();
    m_isClear = true;
    m_mutex.unlock();
	if (m_log) std::cerr << "CHECK CLEAR EVENT OUT" << (void*) this << std::endl;
}

//=============================================================================
// METHOD    : SPELLevent::wait
//=============================================================================
void SPELLevent::wait()
{
	if (m_log) std::cerr << "WAIT EVENT TRY IN " << (void*) this << std::endl;
	//DEBUG("[EVE] Event " + PSTR(this) + " wait in with mutex " + PSTR(&m_mutex))
	m_mutex.lock();
	while (m_isClear)
	{
		m_condition.wait(&m_mutex);
	}
	m_mutex.unlock();
	//DEBUG("[EVE] Event " + PSTR(this) + " wait out")
	if (m_log) std::cerr << "WAIT EVENT FINISH " << (void*) this << std::endl;
}

//=============================================================================
// METHOD    : SPELLevent::wait
//=============================================================================
bool SPELLevent::wait( unsigned long timeoutMsec )
{
        bool timedout = false;
	//DEBUG("[EVE] Event " + PSTR(this) + " wait in with mutex " + PSTR(&m_mutex))
	if (m_log) std::cerr << "   WAIT EVENT T.O. TRY IN " << (void*) this << std::endl;
	m_mutex.lock();
	while (m_isClear && !timedout)
	{
		if (m_log) std::cerr << "   WAIT EVENT T.O. COND IN " << (void*) this << std::endl;
		timedout = m_condition.wait(&m_mutex,timeoutMsec);
		if (m_log) std::cerr << "   WAIT EVENT T.O. COND OUT, TIMEOUT " << timedout << " " << (void*) this << std::endl;
	}
	m_mutex.unlock();
	if (m_log) std::cerr << "   WAIT EVENT T.O. FINISH" << (void*) this << std::endl;
	//DEBUG("[EVE] Event " + PSTR(this) + " wait out")
    return timedout;
}

//=============================================================================
// METHOD    : SPELLevent::isClear
//=============================================================================
bool SPELLevent::isClear()
{
	if (m_log) std::cerr << "   IS CLEAR EVENT IN " << (void*) this << std::endl;
    //DEBUG("[EVE] Event " + PSTR(this) + " isclear with mutex " + PSTR(&m_mutex))
    bool isClear = false;
    m_mutex.lock();
    isClear = m_isClear;
    m_mutex.unlock();
	if (m_log) std::cerr << "   IS CLEAR EVENT OUT" << (void*) this << std::endl;
    return isClear;
}
