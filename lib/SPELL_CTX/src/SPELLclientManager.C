// ################################################################################
// FILE       : SPELLclientManager.C
// DATE       : Apr 26, 2011
// PROJECT    : SPELL
// DESCRIPTION: Client manager implementation
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
#include "SPELL_CTX/SPELLclientManager.H"
#include "SPELL_CTX/SPELLclient.H"
#include "SPELL_CTX/SPELLdataHelper.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_IPC/SPELLipc_Context.H"
#include "SPELL_IPC/SPELLipc_Executor.H"
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////
SPELLclientManager* SPELLclientManager::s_instance = NULL;

//=============================================================================
// CONSTRUCTOR: SPELLclientManager::SPELLclientManager()
//=============================================================================
SPELLclientManager::SPELLclientManager()
{

}

//=============================================================================
// DESTRUCTOR: SPELLclientManager::~SPELLclientManager()
//=============================================================================
SPELLclientManager::~SPELLclientManager()
{
	ClientMap::iterator it;
	for( it = m_clientMap.begin(); it != m_clientMap.end(); it++ )
	{
		delete it->second;
	}
	m_clientMap.clear();

	NotifierMap::iterator mit;
	for( mit = m_notifiers.begin(); mit != m_notifiers.end(); mit++)
	{
		NotifierList::iterator lit;
		for( lit = mit->second.begin(); lit != mit->second.end(); lit++ )
		{
			delete *lit;
			mit->second.erase(lit);
		}
	}
	m_notifiers.clear();
}

//=============================================================================
// STATIC: SPELLclientManager::instance()
//=============================================================================
SPELLclientManager& SPELLclientManager::instance()
{
	if (s_instance == NULL)
	{
		s_instance = new SPELLclientManager();
	}
	return *s_instance;
}

//=============================================================================
// METHOD: SPELLclientManager::clientLogin
//=============================================================================
void SPELLclientManager::clientLogin( int clientKey, const std::string& host )
{
	SPELLmonitor m(m_clientLock);
	ClientMap::iterator it = m_clientMap.find(clientKey);
	if (it == m_clientMap.end())
	{
		SPELLclient* client = new SPELLclient( clientKey, host, *m_ipc );

		m_clientMap.insert( std::make_pair( clientKey, client ));
		DEBUG("[CMGR] Client logged in: " + ISTR(clientKey));

		notifyClientOperation( clientKey, host, CLIENT_OP_LOGIN );
	}
	else
	{
		LOG_ERROR("Client " + ISTR(clientKey) + " already exists!");
	}
}

//=============================================================================
// METHOD: SPELLclientManager::clientLogout
//=============================================================================
void SPELLclientManager::clientLogout( int clientKey )
{
	SPELLmonitor m(m_clientLock);
	ClientMap::iterator it = m_clientMap.find(clientKey);
	if (it != m_clientMap.end())
	{
		SPELLclient* client = it->second;
		std::string host = client->getClientHost();
		LOG_INFO("Client logged out: " + ISTR(clientKey) + ":" + host);
		delete client;
		m_clientMap.erase(it);
		notifyClientOperation( clientKey, host, CLIENT_OP_LOGOUT );
	}
	else
	{
		LOG_ERROR("Client " + ISTR(clientKey) + " does not exist!");
	}
}

//=============================================================================
// METHOD: SPELLclientManager::clientLost
//=============================================================================
void SPELLclientManager::clientLost( int clientKey )
{
	DEBUG("Client " + ISTR(clientKey) + " lost");
	SPELLmonitor m(m_clientLock);
	ClientMap::iterator it = m_clientMap.find(clientKey);
	if (it != m_clientMap.end())
	{
		SPELLclient* client = it->second;
		std::string host = client->getClientHost();
		LOG_WARN("Client lost: " + ISTR(clientKey) + ":" + host);
		delete client;
		m_clientMap.erase(it);
		notifyClientOperation( clientKey, host, CLIENT_OP_CRASH );
	}
	else
	{
		LOG_ERROR("Client " + ISTR(clientKey) + " does not exist!");
	}
}

//=============================================================================
// METHOD: SPELLclientManager::getClient
//=============================================================================
SPELLclient* SPELLclientManager::getClient( int clientKey )
{
	DEBUG("Get client TRY-IN");
	SPELLmonitor m(m_clientLock);
	DEBUG("Get client IN");
	ClientMap::iterator it = m_clientMap.find(clientKey);
	if (it != m_clientMap.end())
	{
		return it->second;
	}
	else
	{
		LOG_ERROR("Client " + ISTR(clientKey) + " does not exist!");
		return NULL;
	}
}

//=============================================================================
// METHOD: SPELLclientManager::getClientKeys
//=============================================================================
std::list<int> SPELLclientManager::getClientKeys()
{
	DEBUG("Get client keys TRY-IN");
	SPELLmonitor m(m_clientLock);
	std::list<int> keys;
	ClientMap::const_iterator it;
	for( it = m_clientMap.begin(); it != m_clientMap.end(); it++ )
	{
		keys.push_back(it->first);
	}
	return keys;
}

//=============================================================================
// METHOD: SPELLclientManager::removeAllClients()
//=============================================================================
void SPELLclientManager::removeAllClients()
{
	std::list<int> keys = getClientKeys();
	std::list<int>::iterator it;
	for( it = keys.begin(); it != keys.end(); it++ )
	{
		clientLogout(*it);
	}
}

//=============================================================================
// METHOD: SPELLclientManager::
//=============================================================================
void SPELLclientManager::startMonitorExecutor( SPELLclient* client, SPELLexecutor* exec )
{
	DEBUG("Start monitoring executor TRY-IN");
	SPELLmonitor m(m_clientLock);
	std::string instanceId = exec->getModel().getInstanceId();
	SPELLclientNotifier* notifier = new SPELLclientNotifier(client,exec);
	client->addProcedure( instanceId, CLIENT_MODE_MONITOR );
	notifier->start();
	m_notifiers[instanceId].push_back( notifier );
	LOG_INFO("Client " + ISTR(client->getClientKey()) + " start monitoring executor " + instanceId);
}

//=============================================================================
// METHOD: SPELLclientManager::
//=============================================================================
void SPELLclientManager::stopMonitorExecutor( SPELLclient* client, SPELLexecutor* exec )
{
	DEBUG("Stop monitoring executor TRY-IN");
	SPELLmonitor m(m_clientLock);
	NotifierMap::iterator mit = m_notifiers.find(exec->getModel().getInstanceId());
	if (mit != m_notifiers.end())
	{
		NotifierList::iterator lit;
		for( lit = mit->second.begin(); lit != mit->second.end(); lit++ )
		{
			SPELLclientNotifier* notifier = (*lit);
			if (notifier->getClientKey() == client->getClientKey())
			{
				client->removeProcedure( exec->getModel().getInstanceId() );
				notifier->stop();
				delete notifier;
				mit->second.erase(lit);
				LOG_INFO("Client " + ISTR(client->getClientKey()) + " stop monitoring executor " + exec->getModel().getInstanceId());
				return;
			}
		}
	}
}

//=============================================================================
// METHOD: SPELLclientManager::
//=============================================================================
std::list<std::string> SPELLclientManager::getMonitoringClients( const std::string& procId )
{
	SPELLmonitor m(m_clientLock);
	std::list<std::string> monitoring;
	NotifierMap::iterator mit = m_notifiers.find(procId);
	if (mit != m_notifiers.end())
	{
		NotifierList::iterator lit;
		for( lit = mit->second.begin(); lit != mit->second.end(); lit++ )
		{
			SPELLclientNotifier* notifier = (*lit);
			std::string id = notifier->getClientHost() + ":" + ISTR(notifier->getClientKey());
			monitoring.push_back(id);
		}
	}
	return monitoring;
}

//=============================================================================
// METHOD: SPELLclientManager::
//=============================================================================
std::list<int> SPELLclientManager::getMonitoringClientsKeys( const std::string& procId )
{
	SPELLmonitor m(m_clientLock);
	std::list<int> keys;
	NotifierMap::iterator mit = m_notifiers.find(procId);
	if (mit != m_notifiers.end())
	{
		NotifierList::iterator lit;
		for( lit = mit->second.begin(); lit != mit->second.end(); lit++ )
		{
			SPELLclientNotifier* notifier = (*lit);
			keys.push_back(notifier->getClientKey());
		}
	}
	return keys;
}

//=============================================================================
// METHOD: SPELLclientManager::
//=============================================================================
void SPELLclientManager::completeMonitoringInfo( const std::string& procId, SPELLipcMessage& msg )
{
	std::list<std::string> monitoring = getMonitoringClients(procId);
	std::list<std::string>::iterator it;
	std::string monitoringList = "";
	for( it = monitoring.begin(); it != monitoring.end(); it++)
	{
		if (monitoringList != "") monitoringList += ",";
		monitoringList += (*it);
	}
	msg.set( MessageField::FIELD_GUI_LIST, monitoringList );
}

//=============================================================================
// METHOD: SPELLclientManager::
//=============================================================================
void SPELLclientManager::notifyClientOperation( int clientKey,
		                    const std::string& host,
		                    const SPELLclientOperation& operation )
{
	DEBUG("Notify client operation start");
	SPELLipcMessage notification( ContextMessages::MSG_CLIENT_OP );
	notification.setSender("CTX");
	notification.setReceiver("CLT");
	notification.setType(MSG_TYPE_ONEWAY);
	notification.set( MessageField::FIELD_GUI_KEY, ISTR(clientKey));
	notification.set( MessageField::FIELD_CLOP, SPELLdataHelper::clientOperationToString(operation) );
	notification.set( MessageField::FIELD_HOST, host );

	ClientMap::const_iterator it;
	for( it = m_clientMap.begin(); it != m_clientMap.end(); it++ )
	{
		SPELLclient* client = it->second;
		if (client->getClientKey() == clientKey) continue;
		client->sendMessageToClient(notification);
	}
	DEBUG("Notify client operation done");
}

//=============================================================================
// METHOD: SPELLclientManager::
//=============================================================================
void SPELLclientManager::notifyClients( const SPELLipcMessage& msg )
{
	DEBUG("Notify clients TRY-IN");
	SPELLmonitor m(m_clientLock);
	DEBUG("Notify clients IN");
	ClientMap::const_iterator it;
	for( it = m_clientMap.begin(); it != m_clientMap.end(); it++ )
	{
		SPELLclient* client = it->second;
		DEBUG("    - client " + ISTR(it->first));
		client->sendMessageToClient(msg);
	}
	DEBUG("Notify clients done");
}

//=============================================================================
// METHOD: SPELLclientManager::
//=============================================================================
void SPELLclientManager::notifyMonitoringClients( const SPELLipcMessage& msg )
{
	DEBUG("Notify monitoring clients TRY-IN");
	SPELLmonitor m(m_clientLock);
	DEBUG("Notify monitoring clients IN");
	ClientMap::const_iterator it;
	int controllingKey = msg.getKey();
	for( it = m_clientMap.begin(); it != m_clientMap.end(); it++ )
	{
		SPELLclient* client = it->second;
		if (it->first == controllingKey) continue;
		DEBUG("    - client " + ISTR(it->first));
		client->sendMessageToClient(msg);
	}
	DEBUG("Notify monitoring clients done");
}

//=============================================================================
// METHOD: SPELLclientManager::
//=============================================================================
void SPELLclientManager::setExecutorController( SPELLclient* client, SPELLexecutor* exec )
{
	client->addProcedure( exec->getModel().getInstanceId(), CLIENT_MODE_CONTROL );
	exec->setControllingClient( client );
}

//=============================================================================
// METHOD: SPELLclientManager::
//=============================================================================
void SPELLclientManager::removeExecutorController( SPELLclient* client, SPELLexecutor* exec, bool notifyExecutor, bool error )
{
	client->removeProcedure( exec->getModel().getInstanceId() );
	if (notifyExecutor)
	{
		exec->removeControllingClient(error);
	}
}

//=============================================================================
// METHOD: SPELLclientManager::
//=============================================================================
void SPELLclientManager::setExecutorInBackground( SPELLclient* client, SPELLexecutor* exec )
{
	client->removeProcedure( exec->getModel().getInstanceId() );
	exec->setBackground();
}
