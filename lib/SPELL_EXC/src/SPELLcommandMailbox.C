// ################################################################################
// FILE       : SPELLcommandMailbox.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the command mailbox
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
#include "SPELL_EXC/SPELLcommandMailbox.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_WRP/SPELLconstants.H"


#define QUEUE_MAX_SIZE 10
#define HQUEUE_MAX_SIZE 3

//=============================================================================
// CONSTRUCTOR    : SPELLcommandMailbox::SPELLcommandMailbox
//=============================================================================
SPELLcommandMailbox::SPELLcommandMailbox()
    : m_queue(10),
      m_hqueue(3)
{
    m_lock.clear();
    m_lastCommand = "";
}

//=============================================================================
// DESTRUCTOR    : SPELLcommandMailbox::~SPELLcommandMailbox
//=============================================================================
SPELLcommandMailbox::~SPELLcommandMailbox()
{
}

//=============================================================================
// METHOD     : SPELLcommandMailbox::reset()
//=============================================================================
void SPELLcommandMailbox::reset()
{
	m_lastCommand = "";
	m_hqueue.clear();
	m_queue.clear();
}

//=============================================================================
// METHOD     : SPELLcommandMailbox::push
//=============================================================================
void SPELLcommandMailbox::push( const ExecutorCommand cmd, const bool hp )
{
	if ((cmd.id == m_lastCommand)&&!isCommandRepeatable(cmd.id))
	{
		DEBUG("[CMD] Discarding repeated command: " + cmd.id);
		return;
	}
    if (hp)
    {
    	if (m_hqueue.size()==HQUEUE_MAX_SIZE)
    	{
    		DEBUG("[CM] Discarding command, hqueue full");
    		return;
    	}
    	else
    	{
            DEBUG("[CM] Pushing high priority command: " + cmd.id);
            m_hqueue.push(cmd);
    	}
    }
    else
    {
    	if (m_queue.size()==QUEUE_MAX_SIZE)
    	{
    		DEBUG("[CM] Discarding command, queue full");
    		return;
    	}
    	else
    	{
    		DEBUG("[CM] Pushing normal command: " + cmd.id);
    		m_queue.push(cmd);
    	}
    }
    m_lock.set();
}

//=============================================================================
// METHOD     : SPELLcommandMailbox::isCommandRepeatable()
//=============================================================================
bool SPELLcommandMailbox::isCommandRepeatable( const std::string& id )
{
	if (id == CMD_PAUSE) return false;
	if (id == CMD_RUN) return false;
	if (id == CMD_ABORT) return false;
	if (id == CMD_RELOAD) return false;
	return true;
}

//=============================================================================
// METHOD     : SPELLcommandMailbox::pull
//=============================================================================
ExecutorCommand SPELLcommandMailbox::pull()
{
    ExecutorCommand cmd;
    bool gotten = false;
    while(!gotten)
    {
        DEBUG("[CM] Awaiting commands");
        m_lock.wait();
        if (!m_hqueue.empty())
        {
            cmd = m_hqueue.pull();
            if ((m_lastCommand != cmd.id)||isCommandRepeatable(cmd.id))
            {
                DEBUG("[CM] Got high priority command: " + cmd.id);
            	gotten = true;
            }
            else
            {
                DEBUG("[CM] Discarding high priority command: " + cmd.id);
            }
        }
        else if (!m_queue.empty())
        {
            cmd = m_queue.pull();
            if ((m_lastCommand != cmd.id)||isCommandRepeatable(cmd.id))
            {
                DEBUG("[CM] Got command: " + cmd.id);
            	gotten = true;
            }
            else
            {
                DEBUG("[CM] Discarding command: " + cmd.id);
            }
        }
        if (!gotten) m_lock.clear();
    }
    DEBUG("[CM] Pulled command: " + cmd.id);
    m_lastCommand = cmd.id;
    return cmd;
}
