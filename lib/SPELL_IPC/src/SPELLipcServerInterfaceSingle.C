// ################################################################################
// FILE       : SPELLipcServerInterfaceSingle.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the interface for single client servers
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
#include "SPELL_IPC/SPELLipcServerInterfaceSingle.H"
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
#define NAME std::string("[ IPC-SSRV-") + ISTR(m_serverPort) + "-" + m_ifcName + " ] "

//=============================================================================
// CONSTRUCTOR: SPELLipcServerInterfaceSingle::SPELLipcServerInterfaceSingle
//=============================================================================
SPELLipcServerInterfaceSingle::SPELLipcServerInterfaceSingle( const std::string& name, int key, int port )
    : SPELLipcInterface(name),
      m_connected(false),
      m_trash(name),
      m_ipcSequence(0),
      m_mailbox(name)
{
    m_serverKey = key;
    m_serverPort = port;
    m_serverSocket = NULL;

    m_ifcName = name;

    m_channel = NULL;
    m_listener = NULL;
    m_trash.start();

    m_timeoutIpcRetry = SPELLconfiguration::instance().commonOrDefault("RetryTimeout", IPC_REQUEST_RETRY_DEFAULT_DELAY_MSEC );

    DEBUG(NAME + "Server interface created " + PSTR(this) );
}

//=============================================================================
// DESTRUCTOR: SPELLipcServerInterfaceSingle:~SPELLipcServerInterfaceSingle
//=============================================================================
SPELLipcServerInterfaceSingle::~SPELLipcServerInterfaceSingle()
{
    DEBUG(NAME + "Destroying server interface");
    DEBUG(NAME + "Server interface destroyed");
}

//=============================================================================
// METHOD: SPELLipcServerInterfaceSingle:initialize
//=============================================================================
void SPELLipcServerInterfaceSingle::initialize( SPELLipcInterfaceListener* listener )
{
	m_listener = listener;
}

//=============================================================================
// METHOD: SPELLipcServerInterfaceSingle:removeListener
//=============================================================================
void SPELLipcServerInterfaceSingle::removeListener()
{
	m_listener = NULL;
}

//=============================================================================
// METHOD: SPELLipcServerInterfaceSingle:connect
//=============================================================================
void SPELLipcServerInterfaceSingle::connect( bool reconnect )
{
    DEBUG(NAME + "Connecting server interface");

    m_serverSocket = SPELLsocket::listen( &m_serverPort );

    m_connected = true;

    DEBUG(NAME + "Server interface ready");
}

//=============================================================================
// METHOD: SPELLipcServerInterfaceSingle:run
//=============================================================================
void SPELLipcServerInterfaceSingle::run()
{
    DEBUG(NAME + "Server interface start");
	m_connected = true;

    DEBUG("  - Entering select loop");

    DEBUG(NAME + "Accepting connections");

    while(isConnected())
    {
        bool disconnected = false;
        SPELLsocket* clientSocket = m_serverSocket->acceptClient( &disconnected );

        if ( (!disconnected) && (clientSocket != NULL) && (isConnected()) )
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
					key = 1;
					LOG_INFO(NAME + "Got client, assigned key: " + ISTR(key));
				}
				else
				{
					LOG_INFO(NAME + "#### Client reconnected, reusing key: " + ISTR(key));
				}
				// Send back the key to the client
				writeKey(key,clientSocket);
				// Add the client model with the given key
				addClient(clientSocket);
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
// METHOD: SPELLipcServerInterfaceSingle:addClient
//=============================================================================
void SPELLipcServerInterfaceSingle::addClient( SPELLsocket* skt )
{
    DEBUG( NAME + "add Client TRY-IN");

    SPELLmonitor m(m_clientLock);

    m_channel = new SPELLipcChannel( std::string(m_ifcName), skt, 1, *this );
    m_channel->connect();

    LOG_INFO(NAME + "Added client");
    m_clientConnectionEvent.set();
}

//=============================================================================
// METHOD: SPELLipcServerInterfaceSingle:removeClient
//=============================================================================
void SPELLipcServerInterfaceSingle::removeClient()
{
    DEBUG( NAME + "Remove client TRY-IN" );
    SPELLmonitor m(m_clientLock);
    if (m_channel)
    {
    	m_channel->disconnect();
    	delete m_channel;
    	m_channel = NULL;
    }
    DEBUG( NAME + "Remove client OUT" );
}

//=============================================================================
// METHOD: SPELLipcServerInterfaceSingle:disconnect
//=============================================================================
void SPELLipcServerInterfaceSingle::disconnect()
{
    DEBUG(NAME + "Server interface disconnect TRY-IN");
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
    removeClient();
    LOG_INFO(NAME + "Wait to end...");
    join();
    LOG_INFO(NAME + "Server interface disconnected");
}

//=============================================================================
// METHOD: SPELLipcServerInterfaceSingle:disconnectClient
//=============================================================================
void SPELLipcServerInterfaceSingle::disconnectClient()
{
    if (!isConnected()) return;
    LOG_INFO( NAME + "Disconnect peer");
    cancelOutgoingRequests();
    removeClient();
}

//=============================================================================
// METHOD: SPELLipcServerInterfaceSingle:connectionLost
//=============================================================================
void SPELLipcServerInterfaceSingle::connectionLost( int peerKey, int errNo, const std::string& reason )
{
    LOG_ERROR(NAME + "#### Connection lost with peer, ERRNO=" + ISTR(errNo));
    removeClient();
	if (m_listener != NULL)
	{
		m_listener->processConnectionError( peerKey, "Connection lost by peer", reason );
	}
}

//=============================================================================
// METHOD: SPELLipcServerInterfaceSingle:connectionClosed
//=============================================================================
void SPELLipcServerInterfaceSingle::connectionClosed( int peerKey )
{
    DEBUG(NAME + "The peer has closed the connection (EOC)");
    removeClient();
    if (m_listener != NULL)
    {
        m_listener->processConnectionClosed( peerKey );
    }
}

//=============================================================================
// METHOD: SPELLipcServerInterfaceSingle:sendMessage
//=============================================================================
void SPELLipcServerInterfaceSingle::sendMessage( SPELLipcMessage& msg )
{
    SPELLmonitor m(m_clientLock);
    DEBUG(NAME + "Send message to peer");
    try
    {
    	SPELLipcOutput& writer = m_channel->getWriter();
		// Must set the appropriate key to the message beforehand. The key info is used
		// to identify requests and their responses
		msg.setKey(1);
    	writer.send(msg);
    }
    catch(SPELLipcError& err)
    {
    	LOG_ERROR(NAME + "Cannot send message to peer: " + err.what());
    }
}

//=============================================================================
// METHOD: SPELLipcServerInterfaceSingle:sendRequest
//=============================================================================
SPELLipcMessage SPELLipcServerInterfaceSingle::sendRequest( SPELLipcMessage& msg, unsigned long timeoutMsec )
{
    SPELLmonitor m(m_clientLock);
    DEBUG(NAME + "Send request to peer " + msg.dataStr());
    SPELLipcMessage resp = VOID_MESSAGE;
    try
    {
		// Must set the appropriate key to the message beforehand. The key info is used
		// to identify requests and their responses
		msg.setKey(1);
		resp = performRequest( msg, timeoutMsec );
    }
    catch( SPELLipcError& err )
    {
    	LOG_ERROR(NAME + "Cannot send request to peer: " + err.what());
    	std::string id = msg.getId();
    	SPELLutils::replace( id, "REQ", "RSP");
    	resp = SPELLipcHelper::createErrorResponse( id, msg );
    	resp.set(MessageField::FIELD_ERROR, "Cannot send request to peer: " + msg.getId());
    	resp.set(MessageField::FIELD_REASON, err.getError());
    	resp.set(MessageField::FIELD_FATAL, PythonConstants::False);
    }
    DEBUG(NAME + "Response for request to peer " + resp.dataStr());
    return resp;
}

//=============================================================================
// METHOD: SPELLipcServerInterfaceSingle::writeKey
//=============================================================================
void SPELLipcServerInterfaceSingle::writeKey( int key, SPELLsocket* skt )
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
// METHOD: SPELLipcServerInterfaceSingle::readKey
//=============================================================================
int SPELLipcServerInterfaceSingle::readKey( SPELLsocket* skt )
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
// METHOD: SPELLipcServerInterfaceSingle:setReady
//=============================================================================
void SPELLipcServerInterfaceSingle::setReady()
{
    while(!m_readyEvent.isClear())
    {
        usleep(100);
    }
    m_readyEvent.set();
};

//=============================================================================
// METHOD: SPELLipcServerInterfaceSingle:waitReady
//=============================================================================
void SPELLipcServerInterfaceSingle::waitReady()
{
    m_readyEvent.clear();
    m_readyEvent.wait();
};

//=============================================================================
// METHOD: SPELLipcServerInterfaceSingle:cancelOutgoingRequests()
//=============================================================================
void SPELLipcServerInterfaceSingle::cancelOutgoingRequests()
{
	DEBUG(NAME + "Cancel outgoing requests");

	if (m_channel)
	{
		// Iterate over the list of ongoing requests, and cancel the requests in the mailbox
		std::vector<std::string>::iterator it;
		std::vector<std::string> requestList = m_channel->getOngoingRequests();
		for( it = requestList.begin(); it != requestList.end(); it++)
		{
			DEBUG(NAME + "   - cancel " + *it);
			m_mailbox.cancel(*it);
		}
	}
	DEBUG(NAME + "Cancel outgoing requests done");
}

//=============================================================================
// METHOD: SPELLipcServerInterfaceSingle:incomingRequest
//=============================================================================
void SPELLipcServerInterfaceSingle::incomingRequest( const SPELLipcMessage& msg )
{
    DEBUG(NAME + "IPC interface incoming request TRY IN");
	SPELLmonitor m(m_incomingLock);
    DEBUG(NAME + "IPC interface incoming request IN (" + msg.getSequenceStr() + ")");
    {
        SPELLmonitor m(m_ipcLock);
		if (!isConnected())
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
    	SPELLipcIncomingRequest* ireq = new SPELLipcIncomingRequest( msg.requestId(), m_ifcName, msg, m_channel->getWriter(), *m_listener );
    	ireq->start();
        DEBUG(NAME + "Wait request processor to start (" + msg.getSequenceStr() + ")");
    	ireq->waitStarted();
		DEBUG(NAME + "Place in trash");
		m_trash.place(ireq);
		DEBUG(NAME + "Place in trash DONE");

        DEBUG(NAME + "Creating request processor DONE (" + msg.getSequenceStr() + ")");
    }
    catch(SPELLipcError& err)
    {
    	LOG_ERROR(err.what());
    }

    DEBUG(NAME + "IPC interface incoming request OUT (" + msg.getSequenceStr() + ")");
}

//=============================================================================
// METHOD: SPELLipcServerInterfaceSingle:incomingMessage
//=============================================================================
void SPELLipcServerInterfaceSingle::incomingMessage( const SPELLipcMessage& msg )
{
    DEBUG(NAME + "IPC interface incoming message TRY IN");
	SPELLmonitor m(m_incomingLock);
    DEBUG(NAME + "IPC interface incoming message IN " + msg.getId());
    {
    	SPELLmonitor m(m_ipcLock);
    	if (!isConnected()) return;
    	if (m_listener == NULL) return;
    }

    DEBUG(NAME + "Creating message processor");
    while(m_trash.size()>50)
    {
        //LOG_WARN(NAME + "Waiting for free space in queue");
    	usleep(2000);
    }
    SPELLipcIncomingMessage* imsg = new SPELLipcIncomingMessage( msg.requestId(), m_ifcName, msg, *m_listener);
    imsg->start();
    DEBUG(NAME + "Wait processor to start");
	imsg->waitStarted();
	DEBUG(NAME + "Place in trash");
	m_trash.place(imsg);
	DEBUG(NAME + "Place in trash DONE");

    DEBUG(NAME + "IPC interface incoming message OUT");
}

//=============================================================================
// METHOD: SPELLipcServerInterfaceSingle:incomingResponse
//=============================================================================
void SPELLipcServerInterfaceSingle::incomingResponse( const SPELLipcMessage& msg )
{
    DEBUG(NAME + "Incoming response " + msg.getId() + " SEQ " + msg.getSequenceStr() + " -- " + msg.responseId());

    bool responseTaken = false;
    {
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
// METHOD: SPELLipcServerInterfaceSingle::startRequest()
//=============================================================================
std::string SPELLipcServerInterfaceSingle::startRequest( SPELLipcMessage& msg )
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
// METHOD: SPELLipcServerInterfaceSingle:waitResponse
//=============================================================================
SPELLipcMessage SPELLipcServerInterfaceSingle::waitResponse( std::string reqId, unsigned long timeoutMsec )
{
    DEBUG(NAME + "IPC interface wait response");
    // May raise an exception if there is a timeout
    return m_mailbox.retrieve(reqId, timeoutMsec);
}

//=============================================================================
// METHOD: SPELLipcServerInterfaceSingle:waitResponse
//=============================================================================
SPELLipcMessage SPELLipcServerInterfaceSingle::waitResponse( std::string reqId )
{
    DEBUG(NAME + "IPC interface wait response");
    // May raise an exception if there is a timeout
    return m_mailbox.retrieve(reqId);
}

//=============================================================================
// METHOD: SPELLipcServerInterfaceSingle:performRequest
//=============================================================================
SPELLipcMessage SPELLipcServerInterfaceSingle::performRequest( SPELLipcMessage& msg )
{
	return performRequest( msg, 0 );
}

//=============================================================================
// METHOD: SPELLipcServerInterfaceSingle:performRequest
//=============================================================================
SPELLipcMessage SPELLipcServerInterfaceSingle::performRequest( SPELLipcMessage& msg, unsigned long timeoutMsec )
{
    SPELLipcMessage response = VOID_MESSAGE;
    if (isConnected() && m_channel->isConnected())
    {
        std::string reqId = startRequest(msg);
    	m_channel->requestStarted(reqId);

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
					m_channel->getWriter().send(msg);
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

            	m_channel->requestFinished(reqId);

                retries = 2;
                retryWait = false;
            }
            catch(SPELLipcError& ex)
            {
                LOG_ERROR(NAME + "Failed to perform request " + msg.getId() + " due to timeout");
                LOG_ERROR("#######################################################################");
                LOG_ERROR("MESSAGE " + msg.dataStr());
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
                    response.setSequence(msg.getSequence());
                    response.setKey(msg.getKey());
                    retryWait = false;
                	m_channel->requestFinished(reqId);
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
				        response.setSequence(msg.getSequence());
				        response.setKey(msg.getKey());
		                retryWait = false;
	                	m_channel->requestFinished(reqId);
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
