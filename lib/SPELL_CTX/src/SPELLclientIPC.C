// ################################################################################
// FILE       : SPELLclientIPC.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the client IPC interface
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
#include "SPELL_CTX/SPELLclientIPC.H"
#include "SPELL_CTX/SPELLcontext.H"
#include "SPELL_CTX/SPELLclientManager.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_IPC/SPELLipcHelper.H"
#include "SPELL_IPC/SPELLipc_Executor.H"
#include "SPELL_IPC/SPELLipc_Context.H"
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////


//=============================================================================
// CONSTRUCTOR: SPELLclientIPC::SPELLclientIPC()
//=============================================================================
SPELLclientIPC::SPELLclientIPC()
: SPELLipcInterfaceListener(),
  m_ipc("CTX-TO-GUI", 888, 0)

{
	SPELLclientManager::instance().setIPC(this);
}

//=============================================================================
// DESTRUCTOR: SPELLclientIPC::~SPELLclientIPC()
//=============================================================================
SPELLclientIPC::~SPELLclientIPC()
{
}

//=============================================================================
// METHOD: SPELLclientIPC::
//=============================================================================
void SPELLclientIPC::setup()
{
	m_ipc.initialize(&*this);
	m_ipc.connect();
	m_ipc.start();
}

//=============================================================================
// METHOD: SPELLclientIPC::
//=============================================================================
void SPELLclientIPC::cleanup()
{
	m_ipc.disconnect();
	removeAllInterest();
}

//=============================================================================
// METHOD: SPELLclientIPC::
//=============================================================================
int SPELLclientIPC::getPort()
{
	return m_ipc.getPort();
}

//=============================================================================
// METHOD: SPELLclientIPC::removeAllInterest()
//=============================================================================
void SPELLclientIPC::removeAllInterest()
{
	LOG_INFO("Clear interest list");
	InterestMap::iterator it;
	for( it = m_interestMap.begin(); it != m_interestMap.end(); it++ )
	{
		delete it->second;
	}
	m_interestMap.clear();
}

//=============================================================================
// METHOD: SPELLclientIPC::
//=============================================================================
void SPELLclientIPC::registerInterest( int clientKey, SPELLclientListener* listener )
{
	LOG_INFO("Register interest on client " + ISTR(clientKey));
	SPELLclientInterestList* list = getClientInterestList(clientKey);
	if (list == NULL)
	{
		list = createClientInterestList(clientKey);
	}
	list->setClientListener( listener );
}

//=============================================================================
// METHOD: SPELLclientIPC::
//=============================================================================
void SPELLclientIPC::unregisterInterest( int clientKey, SPELLclientListener* listener )
{
	LOG_INFO("Unregister interest on client " + ISTR(clientKey));
	SPELLclientInterestList* list = getClientInterestList(clientKey);
	if (list != NULL)
	{
		list->removeClientListener();
	}
}

//=============================================================================
// METHOD: SPELLclientIPC::
//=============================================================================
void SPELLclientIPC::processMessage( const SPELLipcMessage& msg )
{
	// Get the peer key
	int clientKey = msg.getKey();

	SPELLclientInterestList* list = getClientInterestList(clientKey);
	if (list != NULL)
	{
		list->distributeMessage(msg);
	}
}

//=============================================================================
// METHOD: SPELLclientIPC::
//=============================================================================
SPELLipcMessage SPELLclientIPC::processRequest( const SPELLipcMessage& msg )
{
	int clientKey = msg.getKey();
	DEBUG("[CLTRCV] Received request from client: " + msg.getId());
	SPELLipcMessage resp = VOID_MESSAGE;
	SPELLclientInterestList* list = getClientInterestList(clientKey);
	// If the message is a login message, redirect it to the client manager
	if (msg.getId() == ContextMessages::REQ_GUI_LOGIN)
	{
		SPELLclientManager::instance().clientLogin(clientKey, msg.get( MessageField::FIELD_HOST ));
		resp = SPELLipcHelper::createResponse(ContextMessages::RSP_GUI_LOGIN, msg);
	}
	// If the message is a logout message, redirect it to the client manager
	else if (msg.getId() == ContextMessages::REQ_GUI_LOGOUT )
	{
		// No need to close IPC here. When the GUI receives the response,
		// it will close the channel by sending EOC. There, IPC layer will
		// automatically close the corresponding channel.
		SPELLclientManager::instance().clientLogout(clientKey);
		resp = SPELLipcHelper::createResponse(ContextMessages::RSP_GUI_LOGOUT, msg);
	}
	else if (list != NULL)
	{
		//DEBUG("[CLTRCV] Distribute client request: " + msg.getId());
		resp = list->distributeRequest(msg);
		//DEBUG("[CLTRCV] Got response for client request: " + msg.getId());

		// Executor request additional processing (request attended by executor that need to be processed in context also)
		// But only if the response is not an error
		if (resp.getType() != MSG_TYPE_ERROR)
		{
			if (msg.getId() == ExecutorMessages::REQ_SET_CONFIG)
			{
				SPELLipcMessage cfgChange( msg );
				cfgChange.setId( ContextMessages::MSG_EXEC_CONFIG );
				cfgChange.setType( MSG_TYPE_ONEWAY );
				SPELLclientManager::instance().notifyMonitoringClients(&cfgChange);
			}
		}

		if (resp.isVoid())
		{
			LOG_ERROR("Unable to get response for client request " + msg.getId());
		}
	}
	else
	{
		LOG_ERROR("No listeners for client " + ISTR(clientKey) + " to distribute request: " + msg.getId());
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLclientIPC::processConnectionError
//=============================================================================
void SPELLclientIPC::processConnectionError( int peerKey, std::string error, std::string reason )
{
	// Avoid duplicated processing
	InterestMap::iterator it = m_interestMap.find(peerKey);
	if (it != m_interestMap.end())
	{
		LOG_ERROR( "Client " + ISTR(peerKey) + " IPC error: " + error + "," + reason );
		destroyClientInterestList(peerKey);
		SPELLcontext::instance().clientLost( peerKey );
	}
}

//=============================================================================
// METHOD: SPELLclientIPC::processConnectionClosed
//=============================================================================
void SPELLclientIPC::processConnectionClosed( int peerKey )
{
	// Avoid duplicated processing
	InterestMap::iterator it = m_interestMap.find(peerKey);
	if (it != m_interestMap.end())
	{
		LOG_INFO( "Client " + ISTR(peerKey) + " removed" );
		destroyClientInterestList(peerKey);
	}
}

//=============================================================================
// METHOD: SPELLclientIPC::
//=============================================================================
SPELLclientInterestList* SPELLclientIPC::createClientInterestList( int clientKey )
{
	SPELLmonitor m(m_interestLock);
	SPELLclientInterestList* list = new SPELLclientInterestList();
	m_interestMap.insert( std::make_pair( clientKey, list ));
	return list;
}

//=============================================================================
// METHOD: SPELLclientIPC::
//=============================================================================
void SPELLclientIPC::destroyClientInterestList( int clientKey )
{
	SPELLmonitor m(m_interestLock);
	InterestMap::iterator it = m_interestMap.find(clientKey);
	if (it != m_interestMap.end())
	{
		delete it->second;
		m_interestMap.erase(it);
	}
}

//=============================================================================
// METHOD: SPELLclientIPC::
//=============================================================================
SPELLclientInterestList* SPELLclientIPC::getClientInterestList( int clientKey )
{
	SPELLmonitor m(m_interestLock);
	InterestMap::iterator it = m_interestMap.find(clientKey);
	if (it != m_interestMap.end())
	{
		return it->second;
	}
	return NULL;
}

//=============================================================================
// METHOD: SPELLclientIPC::
//=============================================================================
void SPELLclientIPC::sendMessage( int clientKey, const SPELLipcMessage& msg )
{
	SPELLipcMessage toSend(msg);
	m_ipc.sendMessage(clientKey,toSend);
}

//=============================================================================
// METHOD: SPELLclientIPC::
//=============================================================================
SPELLipcMessage SPELLclientIPC::sendRequest( int clientKey, const SPELLipcMessage& msg, unsigned long timeoutMsec )
{
	SPELLipcMessage toSend(msg);
	return m_ipc.sendRequest(clientKey,toSend,timeoutMsec);
}

//=============================================================================
// METHOD: SPELLclientIPC::
//=============================================================================
void SPELLclientIPC::cancelRequestsToClient( int clientKey )
{
	m_ipc.cancelOutgoingRequests(clientKey);
}
