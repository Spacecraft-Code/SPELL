// ################################################################################
// FILE       : SPELLipcServerInterface.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the interface for servers
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
#include "SPELL_IPC/SPELLipcServerInterface.H"
#include "SPELL_IPC/SPELLipcHelper.H"
#include "SPELL_IPC/SPELLtimeoutValues.H"
// Project includes --------------------------------------------------------
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
// System includes ---------------------------------------------------------

// DEFINES /////////////////////////////////////////////////////////////////
#define NAME std::string("[ IPC-SRV-") + ISTR(m_serverPort) + "-" + m_ifcName + " ] "

//=============================================================================
// CONSTRUCTOR: SPELLipcServerInterface::SPELLipcServerInterface
//=============================================================================
SPELLipcServerInterface::SPELLipcServerInterface( const std::string& name, int key, int port )
    : SPELLipcInterface(name),
      m_connected(false),
      m_trash(name),
      m_ipcSequence(0),
      m_mailbox(name)
      //m_msgPool(name + "-POOL", 30, 100)
{
    m_serverKey = key;
    m_serverPort = port;

    m_clients.clear();

    m_serverSocket = NULL;

    m_ifcName = name;

    m_listener = NULL;
    m_trash.start();

    m_timeoutIpcRetry = SPELLconfiguration::instance().commonOrDefault("RetryTimeout", IPC_REQUEST_RETRY_DEFAULT_DELAY_MSEC );

    DEBUG(NAME + "Server interface created " + PSTR(this) );
}

//=============================================================================
// DESTRUCTOR: SPELLipcServerInterface:~SPELLipcServerInterface
//=============================================================================
SPELLipcServerInterface::~SPELLipcServerInterface()
{
    DEBUG(NAME + "Destroying server interface");
	removeAllClients();
    DEBUG(NAME + "Server interface destroyed");
}

//=============================================================================
// METHOD: SPELLipcServerInterface:initialize
//=============================================================================
void SPELLipcServerInterface::initialize( SPELLipcInterfaceListener* listener )
{
	m_listener = listener;
}

//=============================================================================
// METHOD: SPELLipcServerInterface:removeListener
//=============================================================================
void SPELLipcServerInterface::removeListener()
{
	m_listener = NULL;
}

//=============================================================================
// METHOD: SPELLipcServerInterface:connect
//=============================================================================
void SPELLipcServerInterface::connect( bool reconnect )
{
    DEBUG(NAME + "Connecting server interface");

    m_serverSocket = SPELLsocket::listen( &m_serverPort );

    m_connected = true;

    DEBUG(NAME + "Server interface ready");
}

//=============================================================================
// METHOD: SPELLipcServerInterface:run
//=============================================================================
void SPELLipcServerInterface::run()
{
    DEBUG(NAME + "Server interface start");

    DEBUG("  - Entering select loop");

    DEBUG(NAME + "Accepting connections");

    while(isConnected())
    {
        bool disconnected = false;
        SPELLsocket* clientSocket = m_serverSocket->acceptClient( &disconnected );

        if ((!disconnected)&&clientSocket != NULL)
        {
            LOG_INFO(NAME + "Accepted new client connection");

            try
            {
				int key = -1;
				// Try to read a key from the client. If it is zero, assign a new key
			    DEBUG(NAME + "Reading proposed client key");
				key = readKey(clientSocket);
			    DEBUG(NAME + "Key read: " + ISTR(key));

				if (key == 0)
				{
					key = getFreeClientKey();
					LOG_INFO(NAME + "New client, assigned key: " + ISTR(key));
				}
				else
				{
					LOG_INFO(NAME + "#### Client reconnected, reusing key: " + ISTR(key));
					m_usedClientKeys.push_back(key);
				}
				// Send back the key to the client
				writeKey(key,clientSocket);
				// Add the client model with the given key
				addClient(key, clientSocket);
				DEBUG(NAME + "Waiting interface to be ready");
				waitReady();
				LOG_INFO(NAME + "New connection ready");
            }
            catch(SPELLipcError& err)
            {
            	LOG_ERROR("Failed to accept client: " + err.what());
            }
        }

        if (disconnected) return;
    }
    DEBUG(NAME + "Server interface stop");

    DEBUG(NAME + "Interface stopped");
}

//=============================================================================
// METHOD: SPELLipcServerInterface:getFreeClientKey()
//=============================================================================
int SPELLipcServerInterface::getFreeClientKey()
{
	if (m_usedClientKeys.empty())
	{
		m_usedClientKeys.push_back(1);
		return 1;
	}
	std::vector<int>::const_iterator it;
	int biggestKey = -1;
	for(it = m_usedClientKeys.begin(); it != m_usedClientKeys.end(); it++)
	{
		if (biggestKey < (*it)) biggestKey = (*it);
	}
	int freeKey = -1;
	for(int index=1; index<biggestKey; index++)
	{
		int freeKey = index+1;
		// If the key not being used already
		it = std::find(m_usedClientKeys.begin(), m_usedClientKeys.end(), freeKey);
		if (it == m_usedClientKeys.end())
		{
			m_usedClientKeys.push_back(freeKey);
			break;
		}
	}
	// If we did not find any free key in between
	if (freeKey == -1)
	{
		m_usedClientKeys.push_back(biggestKey+1);
		freeKey = biggestKey+1;
	}
	return freeKey;
}

//=============================================================================
// METHOD: SPELLipcServerInterface:hasClient
//=============================================================================
bool SPELLipcServerInterface::hasClient( int key )
{
    DEBUG(NAME + "Has client TRY-IN");
    SPELLmonitor m(m_clientLock);
    DEBUG(NAME + "Has client OUT");
    return ( m_clients.find(key) != m_clients.end() );
}

//=============================================================================
// METHOD: SPELLipcServerInterface:addClient
//=============================================================================
void SPELLipcServerInterface::addClient( int key, SPELLsocket* skt )
{
    DEBUG( NAME + "add Client TRY-IN");

    SPELLmonitor m(m_clientLock);

    SPELLipcChannel* channel = new SPELLipcChannel( std::string(m_ifcName), skt, key, *this );
    channel->connect();

    m_clients.insert( std::make_pair( key, channel ));
    LOG_INFO(NAME + "Added client " + ISTR(key));
    m_clientConnectionEvent.set();
}

//=============================================================================
// METHOD: SPELLipcServerInterface:removeClient
//=============================================================================
void SPELLipcServerInterface::removeClient( int key )
{
    if (!hasClient(key))
    {
        return;
    }
    DEBUG( NAME + "Remove client TRY-IN" );

    SPELLmonitor m(m_clientLock);

    SPELLipcChannelMap::iterator cit = m_clients.find(key);
    cit->second->disconnect();
    delete cit->second;
    m_clients.erase(cit);
    std::vector<int>::iterator kit = std::find(m_usedClientKeys.begin(), m_usedClientKeys.end(), key);
    if (kit != m_usedClientKeys.end())
    {
    	m_usedClientKeys.erase(kit);
    }
    LOG_INFO(NAME + "Removed client " + ISTR(key));
    DEBUG( NAME + "Remove client OUT" );
}

//=============================================================================
// METHOD: SPELLipcServerInterface:removeAllClients
//=============================================================================
void SPELLipcServerInterface::removeAllClients()
{
	DEBUG( NAME + "Remove all clients IN");

    SPELLipcChannelMap::iterator cit;
    for( cit = m_clients.begin(); cit != m_clients.end(); cit++)
    {
        DEBUG(NAME + "Removing client " + ISTR(cit->first));
        cit->second->disconnect();
        delete cit->second;
        DEBUG(NAME + "Removing client " + ISTR(cit->first) + " done" );
    }
    m_usedClientKeys.clear();
    m_clients.clear();
	DEBUG( NAME + "Remove all clients OUT");
}

//=============================================================================
// METHOD: SPELLipcServerInterface:disconnect
//=============================================================================
void SPELLipcServerInterface::disconnect()
{
    LOG_INFO(NAME + "Server interface disconnect all");
    if (!isConnected()) return;
    DEBUG(NAME + "Server interface disconnect ALL IN");

    m_connected = false;
    DEBUG(NAME + "Shutting down mailbox");
    m_mailbox.shutdown();
    DEBUG(NAME + "Shutting down trash");
    m_trash.shutdown();
    DEBUG(NAME + "Shutting down socket");
    m_serverSocket->shutdown();
    try
    {
    	m_trash.join();
    }
    catch(SPELLipcError& ex){;};
    //DEBUG(NAME + "Shutdown thread pool");
    //m_msgPool.shutdown();
    DEBUG(NAME + "Removing clients");
    removeAllClients();
    DEBUG(NAME + "Server interface wait to disconnect...");
    join();
    LOG_INFO(NAME + "Server interface disconnect all, done");
}

//=============================================================================
// METHOD: SPELLipcServerInterface:disconnectClient
//=============================================================================
void SPELLipcServerInterface::disconnectClient( int peerKey )
{
    if (!isConnected()) return;
    LOG_INFO( NAME + "Disconnect peer " + ISTR(peerKey));
    cancelOutgoingRequests(peerKey);
    removeClient( peerKey );
}

//=============================================================================
// METHOD: SPELLipcServerInterface:connectionLost
//=============================================================================
void SPELLipcServerInterface::connectionLost( int peerKey, int errNo, const std::string& reason )
{
    LOG_ERROR(NAME + "#### Connection lost with peer " + ISTR(peerKey) + ", ERRNO=" + ISTR(errNo));
    removeClient(peerKey);
    SPELLipcChannelMap::iterator cit = m_clients.end();
    if (errNo == 104)
    {
    	m_clientConnectionEvent.clear();
		// Wait to see if the client reconnects
		LOG_ERROR(NAME + "#### Waiting 2 secs for reconnection...");
		bool timedout = m_clientConnectionEvent.wait(2000);
		if (!timedout)
		{
			cit = m_clients.find(peerKey);
		}
    }
    if (cit != m_clients.end())
    {
        LOG_INFO(NAME + "#### Peer " + ISTR(peerKey) + " reconnected!");
    }
    else
    {
    	if (errNo == 104) LOG_ERROR(NAME + "#### Client did not reconnect");
        if (m_listener != NULL)
        {
            m_listener->processConnectionError( peerKey, "Connection lost by peer", reason );
        }
    }
}

//=============================================================================
// METHOD: SPELLipcServerInterface:connectionClosed
//=============================================================================
void SPELLipcServerInterface::connectionClosed( int peerKey )
{
    LOG_INFO(NAME + "The client " + ISTR(peerKey) + " has closed the connection (EOC)");
    removeClient(peerKey);
    if (m_listener != NULL)
    {
        m_listener->processConnectionClosed( peerKey );
    }
}

//=============================================================================
// METHOD: SPELLipcServerInterface:sendMessage
//=============================================================================
void SPELLipcServerInterface::sendMessage( int peerKey, SPELLipcMessage& msg )
{
    DEBUG(NAME + "Send message to peer " + ISTR(peerKey));
    try
    {
		// Must set the appropriate key to the message beforehand. The key info is used
		// to identify requests and their responses
		msg.setKey(peerKey);
    	getChannel(peerKey).getWriter().send(msg);
    }
    catch(SPELLipcError& err)
    {
    	LOG_ERROR(NAME + "Cannot send message to peer " + ISTR(peerKey) + ": " + err.what());
    }
}

//=============================================================================
// METHOD: SPELLipcServerInterface:sendRequest
//=============================================================================
SPELLipcMessage SPELLipcServerInterface::sendRequest( int peerKey, SPELLipcMessage& msg, unsigned long timeoutMsec )
{
    DEBUG(NAME + "Send request to peer " + ISTR(peerKey) + " " + msg.dataStr());
    SPELLipcMessage resp = VOID_MESSAGE;
    try
    {
		DEBUG(NAME + "Get writer for peer " + ISTR(peerKey));
		// Must set the appropriate key to the message beforehand. The key info is used
		// to identify requests and their responses
		msg.setKey(peerKey);
		DEBUG(NAME + "Sending now request to peer " + ISTR(peerKey));
		resp = performRequest( getChannel(peerKey), msg, timeoutMsec );
    }
    catch( SPELLipcError& err )
    {
    	LOG_ERROR(NAME + "Cannot send request to peer " + ISTR(peerKey) + ": " + err.what());
    	std::string id = msg.getId();
    	SPELLutils::replace( id, "REQ", "RSP");
    	resp = SPELLipcHelper::createErrorResponse( id, msg );
    	resp.set(MessageField::FIELD_ERROR, "Cannot send request to peer " + ISTR(peerKey) + ": " + msg.getId());
    	resp.set(MessageField::FIELD_REASON, err.getError());
    	resp.set(MessageField::FIELD_FATAL, PythonConstants::False);
    }
    DEBUG(NAME + "Response for request to peer " + ISTR(peerKey) + " " + resp.dataStr());
    return resp;
}

//=============================================================================
// METHOD: SPELLipcServerInterface:getChannel
//=============================================================================
SPELLipcChannel& SPELLipcServerInterface::getChannel( int peerKey )
{
    DEBUG( NAME + "Get channel TRY-IN" );

    SPELLmonitor m(m_clientLock);
    SPELLipcChannelMap::iterator cit = m_clients.find(peerKey);
    if (cit != m_clients.end())
    {
        DEBUG( NAME + "Get channel OUT" );
        return *cit->second;
    }
    DEBUG( NAME + "Get channel FAIL" );
    throw SPELLipcError("Cannot get writer, no such key: " + ISTR(peerKey));
}

//=============================================================================
// METHOD: SPELLipcServerInterface::writeKey
//=============================================================================
void SPELLipcServerInterface::writeKey( int key, SPELLsocket* skt )
{
    unsigned char bytes[1];
    bytes[0] = (unsigned char)((key >> 8) & 0xFF);
    int sent = skt->send( bytes, 1 );
    if (sent != 1)
    {
        throw SPELLipcError("Could not send first key byte");
    }
    bytes[0] = (unsigned char)(key & 0xFF);
    sent = skt->send( bytes, 1 );
    if (sent != 1)
    {
        throw SPELLipcError("Could not send second key byte");
    }
    DEBUG(NAME + "Key sent: " + ISTR(key));
}

//=============================================================================
// METHOD: SPELLipcServerInterface::readKey
//=============================================================================
int SPELLipcServerInterface::readKey( SPELLsocket* skt )
{
	int key = 0;
	try
	{
		unsigned char byte1[1];
		unsigned char byte2[1];
		int received = skt->receive( byte1, 1 );
		if (received != 1)
		{
			throw SPELLipcError("Could not receive first key byte");
		}
		received = skt->receive( byte2, 1 );
		if (received != 1)
		{
			throw SPELLipcError("Could not receive second key byte");
		}
		key = (byte1[0] << 8) | (byte2[0]);
		DEBUG(NAME + "Key received: " + ISTR(key));
	}
	catch(SPELLipcError& err)
	{
		key = 0;
	}
    return key;
}

//=============================================================================
// METHOD: SPELLipcServerInterface:setReady
//=============================================================================
void SPELLipcServerInterface::setReady()
{
    while(!m_readyEvent.isClear())
    {
        usleep(100);
    }
    m_readyEvent.set();
};

//=============================================================================
// METHOD: SPELLipcServerInterface:waitReady
//=============================================================================
void SPELLipcServerInterface::waitReady()
{
    m_readyEvent.clear();
    m_readyEvent.wait();
};

//=============================================================================
// METHOD: SPELLipcServerInterface:cancelOutgoingRequests()
//=============================================================================
void SPELLipcServerInterface::cancelOutgoingRequests( int peerKey )
{
	DEBUG(NAME + "Cancel outgoing requests");
    DEBUG(NAME + "Cancel outgoing requests for peer " + ISTR(peerKey));

	// Obtain the channel corresponding to the client
	SPELLipcChannel& channel = getChannel(peerKey);

	// Iterate over the list of ongoing requests, and cancel the requests in the mailbox
	std::vector<std::string>::iterator it;
		std::vector<std::string> requestList = channel.getOngoingRequests();
	for( it = requestList.begin(); it != requestList.end(); it++)
	{
				DEBUG(NAME + "   - cancel " + *it);
				m_mailbox.cancel(*it);
	}

    DEBUG(NAME + "Cancel outgoing requests done");
}

//=============================================================================
// METHOD: SPELLipcServerInterface:cancelIncomingRequests
//=============================================================================
void SPELLipcServerInterface::cancelIncomingRequests( int peerKey )
{
	DEBUG(NAME + "Cancel incoming requests");
	m_trash.cancelAndCleanRequests(peerKey);
	DEBUG(NAME + "Cancel incoming requests done");
}

//=============================================================================
// METHOD: SPELLipcServerInterface:incomingRequest
//=============================================================================
void SPELLipcServerInterface::incomingRequest( const SPELLipcMessage& msg )
{
    DEBUG(NAME + "IPC interface incoming request TRY IN (" + msg.getSequenceStr() + ")");

    {
        SPELLmonitor m(m_ipcLock);
		if (!m_connected)
		{
			LOG_WARN("!! Request discarded: not connected: " + msg.getId());
			return;
		}
		if (m_listener == NULL)
		{
			LOG_WARN("!! Request discarded: no listener: " + msg.getId());
			return;
		}
    }

    DEBUG(NAME + "IPC interface incoming request IN (" + msg.getSequenceStr() + ")");

    try
    {
        DEBUG(NAME + "Creating request processor  (" + msg.getSequenceStr() + ")");
    	SPELLipcIncomingRequest* ireq = new SPELLipcIncomingRequest( msg.requestId(), m_ifcName, msg, getChannel(msg.getKey()).getWriter(), *m_listener );
    	ireq->start();
        DEBUG(NAME + "Wait request processor to start (" + msg.getSequenceStr() + ")");
    	ireq->waitStarted();
    	{
    		DEBUG(NAME + "Place in trash");
            SPELLmonitor m(m_ipcLock);
        	m_trash.place(ireq);
    		DEBUG(NAME + "Place in trash DONE");
    	}

        DEBUG(NAME + "Creating request processor DONE (" + msg.getSequenceStr() + ")");
    }
    catch(SPELLipcError& err)
    {
    	LOG_ERROR(err.what());
    }

    DEBUG(NAME + "IPC interface incoming request OUT (" + msg.getSequenceStr() + ")");
}

//=============================================================================
// METHOD: SPELLipcServerInterface:incomingMessage
//=============================================================================
void SPELLipcServerInterface::incomingMessage( const SPELLipcMessage& msg )
{
    DEBUG(NAME + "IPC interface incoming message IN " + msg.getId());
    {
    	SPELLmonitor m(m_ipcLock);
    	if (!m_connected) return;
    	if (m_listener == NULL) return;
    }

    DEBUG(NAME + "Creating message processor");
    SPELLipcIncomingMessage* imsg = new SPELLipcIncomingMessage( msg.requestId(), m_ifcName, msg, *m_listener);
    imsg->start();
    DEBUG(NAME + "Wait processor to start");
	imsg->waitStarted();
	{
		DEBUG(NAME + "Place in trash");
    	SPELLmonitor m(m_ipcLock);
		m_trash.place(imsg);
		DEBUG(NAME + "Place in trash DONE");
	}

    DEBUG(NAME + "IPC interface incoming message OUT");
}

//=============================================================================
// METHOD: SPELLipcServerInterface:incomingResponse
//=============================================================================
void SPELLipcServerInterface::incomingResponse( const SPELLipcMessage& msg )
{
    DEBUG(NAME + "Incoming response " + msg.getId() + " SEQ " + msg.getSequenceStr() + " -- " + msg.responseId());

    bool responseTaken = false;
    {
        DEBUG(NAME + "Incoming response TRY IN");
    	SPELLmonitor m(m_ipcLock);
        DEBUG(NAME + "Incoming response IN");
    	responseTaken = m_mailbox.place( msg.responseId(), msg );
        DEBUG(NAME + "Incoming response " + msg.dataStr() + " placed, taken=" + BSTR(responseTaken));
    }
	if (!responseTaken)
	{
		if ( msg.getType() == MSG_TYPE_ERROR )
		{
			incomingMessage( msg );
		}
		else
		{
			LOG_ERROR(NAME + "Request response not expected: " + msg.dataStr());
		}
	}
}

//=============================================================================
// METHOD: SPELLipcServerInterface::startRequest()
//=============================================================================
std::string SPELLipcServerInterface::startRequest( SPELLipcMessage& msg )
{
    DEBUG(NAME + "IPC interface start request TRY-IN " + msg.getId());
    SPELLmonitor m(m_ipcLock);

    if (m_ipcSequence == LONG_MAX) m_ipcSequence = 0;

    msg.setSequence(m_ipcSequence++);
    std::string reqId = msg.requestId();

    DEBUG(NAME + "Request ID is " + reqId);
    DEBUG(NAME + "Request SEQ is " + msg.getSequenceStr());

    DEBUG(NAME + "IPC interface start request OUT " + msg.getId());
    return reqId;
}

//=============================================================================
// METHOD: SPELLipcServerInterface:waitResponse
//=============================================================================
SPELLipcMessage SPELLipcServerInterface::waitResponse( std::string reqId, unsigned long timeoutMsec )
{
    DEBUG(NAME + "IPC interface wait response");
    // May raise an exception if there is a timeout
    return m_mailbox.retrieve(reqId, timeoutMsec);
}

//=============================================================================
// METHOD: SPELLipcServerInterface:waitResponse
//=============================================================================
SPELLipcMessage SPELLipcServerInterface::waitResponse( std::string reqId )
{
    DEBUG(NAME + "IPC interface wait response");
    // May raise an exception if there is a timeout
    return m_mailbox.retrieve(reqId);
}

//=============================================================================
// METHOD: SPELLipcServerInterface:performRequest
//=============================================================================
SPELLipcMessage SPELLipcServerInterface::performRequest( SPELLipcChannel& channel, SPELLipcMessage& msg )
{
	return performRequest( channel, msg, 0 );
}

//=============================================================================
// METHOD: SPELLipcServerInterface:performRequest
//=============================================================================
SPELLipcMessage SPELLipcServerInterface::performRequest( SPELLipcChannel& channel, SPELLipcMessage& msg, unsigned long timeoutMsec )
{
    SPELLipcMessage response = VOID_MESSAGE;
    if (m_connected)
    {
        std::string reqId = startRequest(msg);

        channel.requestStarted(reqId);

        int retries = 0;
        bool retryWait = true;
        while(retryWait)
        {
            try
            {
            	// Do this on every retry: the queue in the mailbox disappears in case of timeout
                m_mailbox.prepare( reqId );

                // Only the first time
                if (retries == 0)
                {
					// IMPORTANT: the message IPC key must be appropriately set at this point, to
					// well identify the request and the corresponding response
					DEBUG(NAME + "Sending request now to peer, timeout " + ISTR(timeoutMsec));
					DEBUG(NAME + "OUTGOING: " + msg.dataStr());
					channel.getWriter().send(msg);
                }

                DEBUG(NAME + "Waiting response from peer for " + reqId);
                if (timeoutMsec == 0)
                {
                	// With no timeout, wait forever
                	response = waitResponse(reqId);
                }
                else
                {
                	response = waitResponse(reqId, timeoutMsec);
                }
                DEBUG(NAME + "Response obtained");

                channel.requestFinished(reqId);

                retries = 2;
                retryWait = false;
            }
            catch(SPELLipcError& ex)
            {
                LOG_ERROR(NAME + "Failed to perform request " + msg.getId() + " due to timeout");
                LOG_ERROR("#######################################################################");
                LOG_ERROR("RESPONSE TIMED OUT, RETRYING (" + ISTR(retries) + "/" + ISTR(IPC_REQUEST_MAX_RETRIES) + ")");
                LOG_ERROR("TIMEOUT: " + ISTR(timeoutMsec) + " ms." );
                LOG_ERROR("ID: " + msg.getId() + " IPC KEY: " + ISTR(msg.getKey()) + " SEQ: " + ISTR(msg.getSequence()));
                LOG_ERROR("#######################################################################");
                // If it is a notification, just give back the error response right away
                if (msg.getType() == MSG_TYPE_NOTIFY)
                {
                    LOG_ERROR(NAME + "Notification: no more retries");

                    response = SPELLipcHelper::createErrorResponse(msg.getId(),msg);
                    response.set(MessageField::FIELD_ERROR,"Could not send notification");
                    response.set(MessageField::FIELD_REASON,"Response timed out");
                    response.set(MessageField::FIELD_FATAL,PythonConstants::False);
                    channel.requestFinished(reqId);
                    retryWait = false;
                }
                else
                {
					if (retries==IPC_REQUEST_MAX_RETRIES)
					{
						LOG_ERROR(NAME + "Request: no more retries");
						response = SPELLipcHelper::createErrorResponse(msg.getId(),msg);
						response.set(MessageField::FIELD_ERROR,"Could not send request");
						response.set(MessageField::FIELD_REASON,"Response timed out");
						response.set(MessageField::FIELD_FATAL,PythonConstants::True);
	                    channel.requestFinished(reqId);
		                retryWait = false;
					}
					else
					{
						LOG_ERROR(NAME + "Wait before retrying...");
						usleep(m_timeoutIpcRetry * 1000);
						LOG_ERROR(NAME + "Retrying request");
		                retryWait = true;
					}
	                retries++;
                }
            }
        }
        DEBUG(NAME + " Perform request response: " + response.dataStr());
    }
    return response;
}
