// ################################################################################
// FILE       : SPELLipcTrash.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the request cleaner
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
#include "SPELL_IPC/SPELLipcError.H"
#include "SPELL_IPC/SPELLipcInput.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
// System includes ---------------------------------------------------------
#include <unistd.h>


// DEFINES /////////////////////////////////////////////////////////////////
#define NAME "[TRASH-" + m_ifcName + "] "

//=============================================================================
// CONSTRUCTOR: SPELLipcTrash::SPELLipcTrash
//=============================================================================
SPELLipcTrash::SPELLipcTrash( const std::string& name )
    : SPELLthread("trash-" + name)
{
    m_ifcName = name;
    m_trashWorking = true;
    m_trashFinishEvent.clear();
    DEBUG( NAME + "Created" );
}

//=============================================================================
// DESTRUCTOR: SPELLipcTrash::~SPELLipcTrash
//=============================================================================
SPELLipcTrash::~SPELLipcTrash()
{
    clearData();
    DEBUG( NAME + "Destroyed" );
}

//=============================================================================
// METHOD: SPELLipcTrash::shutdown
//=============================================================================
void SPELLipcTrash::shutdown()
{
    DEBUG( NAME + "Shutdown" );
    m_trashWorking = false;
    m_trashFinishEvent.wait();
    while(haveData())
    {
    	clearData();
    }
    DEBUG( NAME + "Shutdown done" );
}

//=============================================================================
// METHOD: SPELLipcTrash::cancelAndCleanRequests
//=============================================================================
void SPELLipcTrash::cancelAndCleanRequests( int peerKey )
{
    SPELLmonitor m(m_dataLock);
    DEBUG( NAME + "Cancel all requests from peer " + ISTR(peerKey) );
    SPELLipcTrashList::iterator it;
    for( it = m_messages.begin(); it != m_messages.end(); it++)
    {
    	if (peerKey == (*it)->getKey())
    	{
    		DEBUG("    - cancel request " + (*it)->getProcessingId());
			(*it)->cancel();
    	}
    	else
    	{
    		DEBUG("    - do not cancel " + (*it)->getProcessingId());
    	}
    }
    DEBUG( NAME + "All requests from peer " + ISTR(peerKey) + " cancelled" );
}

//=============================================================================
// METHOD: SPELLipcTrash::getNumRequests
//=============================================================================
int SPELLipcTrash::getNumRequests( int peerKey )
{
    SPELLmonitor m(m_dataLock);
    DEBUG( NAME + "Get ongoing requests from peer " + ISTR(peerKey) );
    SPELLipcTrashList::iterator it;
    int count = 0;
    for( it = m_messages.begin(); it != m_messages.end(); it++)
    {
    	if (peerKey == (*it)->getKey())
    	{
    		count++;
    	}
    }
    return count;
}

//=============================================================================
// METHOD: SPELLipcTrash::place
//=============================================================================
void SPELLipcTrash::place( SPELLipcIncomingBase* msg )
{
    SPELLmonitor m(m_dataLock);
    if (m_trashWorking)
    {
        m_messages.push_back(msg);
    }
}

//=============================================================================
// METHOD: SPELLipcTrash::run
//=============================================================================
void SPELLipcTrash::run()
{
    DEBUG( NAME + "Thread started" );
    while(isWorking())
    {
        if (haveData()) clearData();
        usleep(350000);
    }
    if (haveData()) clearData();
    m_trashFinishEvent.set();
    DEBUG( NAME + "Thread finished" );
}

//=============================================================================
// METHOD: SPELLipcTrash::isWorking
//=============================================================================
bool SPELLipcTrash::isWorking()
{
    SPELLmonitor m(m_dataLock);
    return m_trashWorking;
}

//=============================================================================
// METHOD: SPELLipcTrash::haveData
//=============================================================================
bool SPELLipcTrash::haveData()
{
    SPELLmonitor m(m_dataLock);
    return (m_messages.size()>0);
}

//=============================================================================
// METHOD: SPELLipcTrash::size()
//=============================================================================
unsigned int SPELLipcTrash::size()
{
    SPELLmonitor m(m_dataLock);
    return m_messages.size();
}

//=============================================================================
// METHOD: SPELLipcTrash::haveData
//=============================================================================
void SPELLipcTrash::clearData()
{
    SPELLmonitor m(m_dataLock);
    SPELLipcTrashList::iterator it;

    for( it = m_messages.begin(); it != m_messages.end(); it++)
    {
        DEBUG( NAME + "Waiting for " + (*it)->getSequence() );
        if ((*it)->isStarted())
		{
			bool timedout = (*it)->wait(500); // milliseconds
			if (!timedout)
			{
				DEBUG( NAME + "Joining " + (*it)->getSequence() );
				(*it)->join();
				DEBUG( NAME + "************ Deleting " + (*it)->getSequence() );
				delete (*it);
				it = m_messages.erase(it);
				DEBUG( NAME + "Deleted");
			}
			else
			{
				DEBUG( NAME + "Did not delete " + (*it)->getSequence() );
			}
		}
    }
}
