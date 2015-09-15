// ################################################################################
// FILE       : SPELLipcIncoming.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the incoming message handler
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
#include "SPELL_IPC/SPELLipcIncoming.H"
#include "SPELL_IPC/SPELLipcOutput.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_SYN/SPELLmonitor.H"
// System includes ---------------------------------------------------------

// DEFINES /////////////////////////////////////////////////////////////////
#define NAME "(IR-" + getIfcName() + "/ ID:" + getThreadId() + "/" + getProcessingId() + " KEY:" + ISTR(getKey()) + ") "

//=============================================================================
// CONSTRUCTOR: SPELLipcIncomingBase::SPELLipcIncomingBase
//=============================================================================
SPELLipcIncomingBase::SPELLipcIncomingBase( const std::string& id, const std::string& ifcName, SPELLipcMessage msg,
        SPELLipcInterfaceListener& listener )
    : SPELLthread(id),
      m_ifcName(ifcName),
	  m_processingId(id),
      m_listener(listener),
      m_message(msg),
      m_processingFinishEvent(false),
      m_processingStartEvent(false),
      m_key(msg.getKey()),
      m_finished(false),
      m_started(false)
{
    DEBUG( NAME + "Created");
    DEBUG( NAME + "Using message " + m_message.getId());
	m_processingFinishEvent.clear();
	m_processingStartEvent.clear();
}

//=============================================================================
// DESTRUCTOR: SPELLipcIncomingBase::~SPELLipcIncomingBase
//=============================================================================
SPELLipcIncomingBase::~SPELLipcIncomingBase()
{
    DEBUG("Destroyed");
    m_finished = true;
}

//=============================================================================
// METHOD: SPELLipcIncomingBase:getProcessingId()
//=============================================================================
std::string SPELLipcIncomingBase::getProcessingId()
{
	SPELLmonitor m(m_dataLock);
	return m_processingId;
};

//=============================================================================
// METHOD: SPELLipcIncomingBase::getSequence()
//=============================================================================
std::string SPELLipcIncomingBase::getSequence()
{
	SPELLmonitor m(m_dataLock);
	return m_message.getSequenceStr();
};

//=============================================================================
// METHOD: SPELLipcIncomingBase:setStarted()
//=============================================================================
void SPELLipcIncomingBase::setStarted()
{
	SPELLmonitor m(m_dataLock);
	m_started = true;
	m_processingStartEvent.set();
}

//=============================================================================
// METHOD: SPELLipcIncomingBase:isStarted()
//=============================================================================
bool SPELLipcIncomingBase::isStarted()
{
	SPELLmonitor m(m_dataLock);
	return m_started;
}

//=============================================================================
// METHOD: SPELLipcIncomingBase:waitStarted()
//=============================================================================
void SPELLipcIncomingBase::waitStarted()
{
	if (isFinished()) return;
	if (isStarted()) return;
	m_processingStartEvent.wait(500);
}

//=============================================================================
// METHOD: SPELLipcIncomingBase:setFinished()
//=============================================================================
void SPELLipcIncomingBase::setFinished()
{
	SPELLmonitor m(m_dataLock);
	m_finished = true;
}

//=============================================================================
// METHOD: SPELLipcIncomingBase:isFinished()
//=============================================================================
bool SPELLipcIncomingBase::isFinished()
{
	SPELLmonitor m(m_dataLock);
	return m_finished;
}

//=============================================================================
// METHOD: SPELLipcIncomingBase:getMessage()
//=============================================================================
const SPELLipcMessage& SPELLipcIncomingBase::getMessage()
{
	SPELLmonitor m(m_dataLock);
	return m_message;
}

//=============================================================================
// METHOD: SPELLipcIncomingBase:getMessage()
//=============================================================================
SPELLipcInterfaceListener& SPELLipcIncomingBase::getListener()
{
	SPELLmonitor m(m_dataLock);
	return m_listener;
}

//=============================================================================
// METHOD: SPELLipcIncomingBase:getKey()
//=============================================================================
int SPELLipcIncomingBase::getKey()
{
	SPELLmonitor m(m_dataLock);
	return m_key;
}

//=============================================================================
// METHOD: SPELLipcIncomingBase:wait
//=============================================================================
bool SPELLipcIncomingBase::wait( long timeoutMsec )
{
	DEBUG( NAME + "Wait finish " + getMessage().getSequenceStr());
	bool timedout = m_processingFinishEvent.wait( timeoutMsec );
	if (timedout)
	{
		DEBUG( NAME + "Finish timeout (" + ISTR(timeoutMsec) + ") " + getMessage().getSequenceStr());
		DEBUG( "MESSAGE " + getMessage().dataStr());
	}
	else
	{
		DEBUG( NAME + "Wait finish done " + getMessage().getSequenceStr());
	}
    return timedout;
}

//=============================================================================
// METHOD: SPELLipcIncomingBase:finish
//=============================================================================
void SPELLipcIncomingBase::finish()
{
	SPELLmonitor m(m_dataLock);
	if (m_finished) return;
	m_processingFinishEvent.set();
	m_finished = true;
}

//=============================================================================
// METHOD: SPELLipcIncomingBase:cancel()
//=============================================================================
void SPELLipcIncomingBase::cancel()
{
	SPELLmonitor m(m_dataLock);
	m_processingFinishEvent.set();
	m_finished = true;
}

//=============================================================================
// CONSTRUCTOR: SPELLipcIncomingMessage::SPELLipcIncomingMessage
//=============================================================================
SPELLipcIncomingMessage::SPELLipcIncomingMessage( const std::string& msgId,
												  const std::string& ifcName,
												  SPELLipcMessage msg,
												  SPELLipcInterfaceListener& listener)
: SPELLipcIncomingBase(msgId,ifcName,msg,listener)
{
}

//=============================================================================
// DESTRUCTOR: SPELLipcIncomingMessage:~SPELLipcIncomingMessage
//=============================================================================
SPELLipcIncomingMessage::~SPELLipcIncomingMessage()
{
}

//=============================================================================
// METHOD: SPELLipcIncomingMessage:run
//=============================================================================
void SPELLipcIncomingMessage::run()
{
	DEBUG( NAME + "Incoming message starting");
	setStarted();
	getListener().processMessage(getMessage());
	finish();
	DEBUG( NAME + "Incoming message finished");
}

//=============================================================================
// CONSTRUCTOR: SPELLipcIncomingRequest::SPELLipcIncomingRequest
//=============================================================================
SPELLipcIncomingRequest::SPELLipcIncomingRequest( const std::string& requestId,
											      const std::string& ifcName,
											      SPELLipcMessage msg,
											      SPELLipcOutput& writer,
											      SPELLipcInterfaceListener& listener)

: SPELLipcIncomingBase(requestId,ifcName,msg,listener),
  m_writer(writer)
{
}

//=============================================================================
// DESTRUCTOR: SPELLipcIncomingRequest:~SPELLipcIncomingRequest
//=============================================================================
SPELLipcIncomingRequest::~SPELLipcIncomingRequest()
{
	DEBUG( NAME + "Incoming request destroyed");
}

//=============================================================================
// METHOD: SPELLipcIncomingRequest:run
//=============================================================================
void SPELLipcIncomingRequest::run()
{
    DEBUG( NAME + "Request processing started (" + getMessage().getSequenceStr() + ")");

    SPELLipcMessage copy(getMessage());

    // Mark as processing started
    setStarted();

	DEBUG("----------------------------------------------------------------------");
    DEBUG( NAME + "Processing " + copy.getId() + " (" + copy.getSequenceStr() + ")");

    std::string senderId = copy.getSender();
    std::string receiverId = copy.getReceiver();

    SPELLipcMessage response = getListener().processRequest(copy);

    DEBUG( NAME + "Got listener response for: " + copy.getId() + " (" + copy.getSequenceStr() + ")" );
	DEBUG("----------------------------------------------------------------------");

	if (!response.isVoid())
    {
        response.setSender(receiverId);
        response.setReceiver(senderId);
        response.setSequence( copy.getSequence() );
        try
        {
			DEBUG( NAME + "Sending response (" + copy.getSequenceStr() + ")");
			m_writer.send(response);
			DEBUG( NAME + "Response sent (" + copy.getSequenceStr() + ")");
        }
        catch(...)
        {
			return;
        };
    }
    else
    {
    	DEBUG( NAME + "Request " + copy.getId() + " was cancelled");
    }
    finish();
	DEBUG( NAME + "Processing finished (" + copy.getSequenceStr() + ")");
}
