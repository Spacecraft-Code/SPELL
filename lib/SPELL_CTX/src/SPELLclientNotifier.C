// ################################################################################
// FILE       : SPELLclientNotifier.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the client notifier
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
#include "SPELL_CTX/SPELLclientNotifier.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLtime.H"
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////
#define MSG_QUEUE_SIZE 400

//=============================================================================
// CONSTRUCTOR: SPELLclientNotifier::SPELLclientNotifier()
//=============================================================================
SPELLclientNotifier::SPELLclientNotifier( SPELLclient* client, SPELLexecutor* exec )
: SPELLexecutorListener(),
  SPELLthread("notifier-" + ISTR(client->getClientKey()) + "-" + exec->getModel().getInstanceId()),
  m_client(client),
  m_exec(exec),
  m_requests(300),
  m_notifierLock()
{
	m_id = "notifier-" + ISTR(client->getClientKey()) + "-" + exec->getModel().getInstanceId();
	DEBUG("[NOTIF] Created notifier");
	m_exec->registerNotifier( m_id, this );
	m_currentRequest = VOID_MESSAGE;
	m_notifierWorking = true;
}

//=============================================================================
// DESTRUCTOR: SPELLclientNotifier::~SPELLclientNotifier()
//=============================================================================
SPELLclientNotifier::~SPELLclientNotifier()
{
	DEBUG("[NOTIF] De-register notifier");
	m_exec->deregisterNotifier( m_id );
	m_requests.clear();
}

//=============================================================================
// METHOD: SPELLclientNotifier::run
//=============================================================================
void SPELLclientNotifier::run()
{
	DEBUG("[NOTIF] Notification thread started");
	while(m_notifierWorking)
	{
		if (!m_notifierWorking) return;
		// Take the next request from the queue
		m_currentRequest = m_requests.pull();
		if (m_currentRequest.isVoid()) continue;
		if (!m_notifierWorking) return;
		TICK_IN;
		m_client->sendRequestToClient(m_currentRequest, 5000);
		TICK_OUT;

		m_currentRequest = VOID_MESSAGE;
	}
	DEBUG("[NOTIF] Notification thread finished");
}

//=============================================================================
// METHOD: SPELLclientNotifier::run
//=============================================================================
void SPELLclientNotifier::stop()
{
	SPELLmonitor m(m_notifierLock);
	DEBUG("[NOTIF] Stopping notification thread");
	m_notifierWorking = false;
	if (m_requests.empty())
	{
		m_requests.push( VOID_MESSAGE );
	}
	DEBUG("[NOTIF] Joining notification thread");
	join();
	DEBUG("[NOTIF] Notification thread stopped");
}

//=============================================================================
// METHOD: SPELLclientNotifier::processMessageFromExecutor
//=============================================================================
void SPELLclientNotifier::processMessageFromExecutor( SPELLipcMessage msg )
{
	if (!m_notifierWorking) return;
	DEBUG("[NOTIF] Forward message to client " + ISTR(m_client->getClientKey()) + ": " + msg.dataStr());
	m_client->sendMessageToClient(msg);
}

//=============================================================================
// METHOD: SPELLclientNotifier::processRequestFromExecutor
//=============================================================================
SPELLipcMessage SPELLclientNotifier::processRequestFromExecutor( SPELLipcMessage msg )
{
	if (!m_notifierWorking) return VOID_MESSAGE;
	SPELLmonitor m(m_notifierLock);
	TICK_IN;
	m_requests.push(msg);
	TICK_OUT;
	return VOID_MESSAGE;
}
