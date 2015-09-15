// ################################################################################
// FILE       : SPELLipcClientInterface.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the interface for clients
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
#include "SPELL_IPC/SPELLipcClientInterface.H"
#include "SPELL_IPC/SPELLipcHelper.H"
#include "SPELL_IPC/SPELLipcIncoming.H"
#include "SPELL_IPC/SPELLipcChannel.H"
#include "SPELL_IPC/SPELLipc_Executor.H"
#include "SPELL_IPC/SPELLtimeoutValues.H"
// Project includes --------------------------------------------------------
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_WRP/SPELLconstants.H"
// System includes ---------------------------------------------------------

// DEFINES /////////////////////////////////////////////////////////////////
#define NAME std::string("[ IPC-CLI-") + m_serverHost + ":" + ISTR(m_serverPort) + "-" + m_ifcName + " ] "

//=============================================================================
// CONSTRUCTOR: SPELLipcClientInterface::SPELLipcClientInterface
//=============================================================================
SPELLipcClientInterface::SPELLipcClientInterface( const std::string& name, const std::string& host, int port )
    : SPELLipcInterface(name),
      m_ifcName(name),
      m_clientKey(0),
      m_serverHost(host),
      m_serverPort(port),
	  m_connected(false),
	  m_trash(name),
	  m_ipcSequence(0),
	  m_mailbox(name)
{
    m_channel = NULL;
    m_listener = NULL;
    m_trash.start();

    m_timeoutIpcRetry = SPELLconfiguration::instance().commonOrDefault("RetryTimeout", IPC_REQUEST_RETRY_DEFAULT_DELAY_MSEC );

    DEBUG(NAME + "Client interface created");
}

//=============================================================================
// DESTRUCTOR: SPELLipcClientInterface:~SPELLipcClientInterface
//=============================================================================
SPELLipcClientInterface::~SPELLipcClientInterface()
{
    if (m_channel != NULL)
    {
        delete m_channel;
    }
    if (m_socket != NULL)
    {
        delete m_socket;
    }
    DEBUG(NAME + "Client interface destroyed");
}

//=============================================================================
// METHOD: SPELLipcClientInterface:connect
//=============================================================================
void SPELLipcClientInterface::connect( bool reconnect )
{
    DEBUG(NAME + "Connecting client interface");

    m_socket = SPELLsocket::connect( m_serverHost, m_serverPort );

    if (reconnect)
    {
    	writeMyKey(m_clientKey);
    }
    else
    {
    	writeMyKey(0);
    }
	m_clientKey = readMyKey();

    m_connected = true;

    if (m_channel != NULL) delete m_channel;
    m_channel = createChannel();
    m_channel->connect();

    waitReady();
    DEBUG(NAME +"Ready");
    LOG_INFO(NAME + "Connected to " + m_serverHost + ":" + ISTR(m_serverPort));
}

//=============================================================================
// METHOD: SPELLipcClientInterface:createChannel
//=============================================================================
SPELLipcChannel* SPELLipcClientInterface::createChannel()
{
    return new SPELLipcChannel(m_ifcName, m_socket, m_clientKey, *this);
}

//=============================================================================
// METHOD: SPELLipcClientInterface:disconnect
//=============================================================================
void SPELLipcClientInterface::disconnect()
{
    DEBUG(NAME + "Disconnect TRY-IN");
    if (!isConnected()) return;
    DEBUG(NAME + "Disconnect ALL IN");
    m_connected = false;
    DEBUG(NAME + "Clear outgoing requests");
    cancelOutgoingRequests();
    cancelIncomingRequests();
    DEBUG(NAME + "Shutting down mailbox");
    m_mailbox.shutdown();
    DEBUG(NAME + "Shutting down trash");
    m_trash.shutdown();
    DEBUG(NAME + "Shutting down channel");
    m_channel->disconnect();
    DEBUG(NAME + "Shutting down socket");
    m_socket->shutdownRead();
    m_socket->shutdownWrite();
	m_channel->disconnect();
	delete m_channel;
	m_channel = NULL;
	m_trash.join();
    LOG_INFO(NAME + "Disconnected");
}

//=============================================================================
// METHOD: SPELLipcClientInterface:connectionLost
//=============================================================================
void SPELLipcClientInterface::connectionLost( int peerKey, int errNo, const std::string& reason )
{
    LOG_ERROR(NAME + "### CONNECTION LOST with server: " + reason + ", ERRNO=" + ISTR(errNo));
    // Socket is closed at this moment
    m_connected = false;
    if (m_listener != NULL)
    {
        m_listener->processConnectionError( peerKey, "Connection lost", reason );
    }
}

//=============================================================================
// METHOD: SPELLipcClientInterface:connectionClosed
//=============================================================================
void SPELLipcClientInterface::connectionClosed( int peerKey )
{
    DEBUG(NAME + "The server has closed the connection (EOC)");
    if (m_listener != NULL)
    {
        m_listener->processConnectionClosed( peerKey );
    }
}

//=============================================================================
// METHOD: SPELLipcClientInterface:sendMessage
//=============================================================================
void SPELLipcClientInterface::sendMessage( SPELLipcMessage& msg )
{
    DEBUG(NAME + "Send message to server");
    try
    {
		// Must set the appropriate key to the message beforehand. The key info is used
		// to identify requests and their responses
		msg.setKey(m_clientKey);
		// Must set the IPC sequence
		SPELLmonitor m(m_lock);
		msg.setSequence(m_ipcSequence++);
	    m_channel->getWriter().send(msg);
    }
    catch(SPELLipcError& err)
    {
    	LOG_ERROR(NAME + "Cannot send message to server: " + err.what());
    }
    DEBUG(NAME + "Send message to server done");
}

//=============================================================================
// METHOD: SPELLipcClientInterface::sendRequest
//=============================================================================
SPELLipcMessage SPELLipcClientInterface::sendRequest( SPELLipcMessage& msg, unsigned long timeoutMsec )
{
    DEBUG(NAME + "Send request to server");
    SPELLipcMessage resp = VOID_MESSAGE;
    try
    {
		// Must set the appropriate key to the message beforehand. The key info is used
		// to identify requests and their responses
		msg.setKey(m_clientKey);
		resp = performRequest( msg, timeoutMsec );
    }
    catch( SPELLipcError& err )
    {
    	LOG_ERROR(NAME + "Cannot send request to server: " + err.what());
    	std::string id = msg.getId();
    	SPELLutils::replace( id, "REQ", "RSP");
    	resp = SPELLipcHelper::createErrorResponse( id, msg );
    	resp.set(MessageField::FIELD_ERROR, "Cannot send request to server: " + msg.getId());
    	resp.set(MessageField::FIELD_REASON, err.getError());
    	resp.set(MessageField::FIELD_FATAL, PythonConstants::False);
    }
    DEBUG(NAME + "Send request to server finished");
    return resp;
}

//=============================================================================
// METHOD: SPELLipcClientInterface::readMyKey
//=============================================================================
int SPELLipcClientInterface::readMyKey()
{
	int key = -1;
    unsigned char byte1[1];
    unsigned char byte2[1];
    int read = m_socket->receiveAll(byte1,1);
    if (read != 1)
    {
        throw SPELLipcError("Could not read first key byte");
    }
    read = m_socket->receiveAll(byte2,1);
    if (read != 1)
    {
        throw SPELLipcError("Could not read second key byte");
    }
    key = (byte1[0] << 8) | (byte2[0]);
    DEBUG(NAME + "Read key: " + ISTR(key));
    return key;
}

//=============================================================================
// METHOD: SPELLipcClientInterface::writeMyKey
//=============================================================================
void SPELLipcClientInterface::writeMyKey( int key )
{
    unsigned char bytes[1];
    bytes[0] = (unsigned char)((key >> 8) & 0xFF);
    int sent = m_socket->send( bytes, 1 );
    if (sent != 1)
    {
        throw SPELLipcError("Could not send first key byte");
    }
    bytes[0] = (unsigned char)(key & 0xFF);
    sent = m_socket->send( bytes, 1 );
    if (sent != 1)
    {
        throw SPELLipcError("Could not send second key byte");
    }
    DEBUG(NAME + "Key sent: " + ISTR(key));
}

//=============================================================================
// METHOD: SPELLipcClientInterface:setReady
//=============================================================================
void SPELLipcClientInterface::setReady()
{
    while(!m_readyEvent.isClear())
    {
        usleep(100);
    }
    m_readyEvent.set();
};

//=============================================================================
// METHOD: SPELLipcClientInterface:waitReady
//=============================================================================
void SPELLipcClientInterface::waitReady()
{
    m_readyEvent.clear();
    m_readyEvent.wait();
};

//=============================================================================
// METHOD: SPELLipcClientInterface:cancelOutgoingRequests()
//=============================================================================
void SPELLipcClientInterface::cancelOutgoingRequests()
{
	DEBUG(NAME + "Cancel outgoing requests");
    std::list<std::string>::iterator it;
    std::list<std::string>::const_iterator end = m_outgoingRequests.end();

    for( it = m_outgoingRequests.begin(); it != end; it++)
    {
		DEBUG(NAME + "   - cancel " + *it);
		m_mailbox.cancel(*it);
    }
    DEBUG(NAME + "Deleting cancelled requests");
    m_outgoingRequests.clear();
	DEBUG(NAME + "Cancel outgoing requests done");
}

//=============================================================================
// METHOD: SPELLipcClientInterface:cancelIncomingRequests
//=============================================================================
void SPELLipcClientInterface::cancelIncomingRequests()
{
	DEBUG(NAME + "Cancel incoming requests");
	m_trash.cancelAndCleanRequests(m_clientKey);
	DEBUG(NAME + "Cancel incoming requests done");
}

//=============================================================================
// METHOD: SPELLipcClientInterface:incomingRequest
//=============================================================================
void SPELLipcClientInterface::incomingRequest( const SPELLipcMessage& msg )
{
    DEBUG(NAME + "IPC interface incoming request IN (" + msg.getSequenceStr() + ")");

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

    SPELLmonitor m(m_lock);

    DEBUG(NAME + "Incoming request (" + msg.getSequenceStr() + ")");

    try
    {
        DEBUG(NAME + "Creating request processor (" + msg.getSequenceStr() + ")");
    	SPELLipcIncomingRequest* ireq = new SPELLipcIncomingRequest( msg.requestId(), m_ifcName, msg, m_channel->getWriter(), *m_listener );
    	ireq->start();
        DEBUG(NAME + "Wait request processor to start (" + msg.getSequenceStr() + ")");
    	ireq->waitStarted();
        DEBUG(NAME + "Place in trash");
    	m_trash.place(ireq);
        DEBUG(NAME + "Creating request processor DONE (" + msg.getSequenceStr() + ")");
    }
    catch(SPELLipcError& err)
    {
    	LOG_ERROR(err.what());
    }

    DEBUG(NAME + "IPC interface incoming request OUT (" + msg.getSequenceStr() + ")");
}

//=============================================================================
// METHOD: SPELLipcClientInterface:incomingMessage
//=============================================================================
void SPELLipcClientInterface::incomingMessage( const SPELLipcMessage& msg )
{
    DEBUG(NAME + "IPC interface incoming message " + msg.getId());

    if (!m_connected) return;
    if (m_listener == NULL) return;

    std::string msgId = msg.requestId();

    SPELLipcIncomingMessage* imsg = new SPELLipcIncomingMessage( msgId, m_ifcName, msg, *m_listener);
    imsg->start();
    DEBUG(NAME + "Wait processor to start");
	imsg->waitStarted();
    DEBUG(NAME + "Place in trash");
    m_trash.place(imsg);
    DEBUG(NAME + "IPC interface incoming message OUT");
}

//=============================================================================
// METHOD: SPELLipcClientInterface:incomingResponse
//=============================================================================
void SPELLipcClientInterface::incomingResponse( const SPELLipcMessage& msg )
{
    DEBUG(NAME + "Incoming response " + msg.getId() + " SEQ " + msg.getSequenceStr() + " -- " + msg.responseId());

    bool responseTaken = false;
    {
    	SPELLmonitor m(m_lock);
    	responseTaken = m_mailbox.place( msg.responseId(), msg );
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
// METHOD: SPELLipcClientInterface::startRequest()
//=============================================================================
std::string SPELLipcClientInterface::startRequest( SPELLipcMessage& msg )
{
    DEBUG(NAME + "IPC interface start request TRY-IN " + msg.getId());
    SPELLmonitor m(m_lock);

    msg.setSequence(m_ipcSequence++);
    std::string reqId = msg.requestId();

    DEBUG(NAME + "Request ID is " + reqId);
    DEBUG(NAME + "Request SEQ is " + msg.getSequenceStr());

    DEBUG(NAME + "IPC interface start request OUT " + msg.getId());
    return reqId;
}

//=============================================================================
// METHOD: SPELLipcClientInterface:waitResponse
//=============================================================================
SPELLipcMessage SPELLipcClientInterface::waitResponse( std::string reqId, unsigned long timeoutMsec )
{
    DEBUG(NAME + "IPC interface wait response");
    // May raise an exception if there is a timeout
    return m_mailbox.retrieve(reqId, timeoutMsec);
}

//=============================================================================
// METHOD: SPELLipcClientInterface:waitResponse
//=============================================================================
SPELLipcMessage SPELLipcClientInterface::waitResponse( std::string reqId )
{
    DEBUG(NAME + "IPC interface wait response");
    // May raise an exception if there is a timeout
    return m_mailbox.retrieve(reqId);
}

//=============================================================================
// METHOD: SPELLipcClientInterface:performRequest
//=============================================================================
SPELLipcMessage SPELLipcClientInterface::performRequest( SPELLipcMessage& msg )
{
	return performRequest( msg, 0 );
}

//=============================================================================
// METHOD: SPELLipcClientInterface:performRequest
//=============================================================================
SPELLipcMessage SPELLipcClientInterface::performRequest( SPELLipcMessage& msg, unsigned long timeoutMsec )
{
    SPELLipcMessage response = VOID_MESSAGE;
    if (m_connected)
    {
        std::string reqId = startRequest(msg);

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
                    m_outgoingRequests.push_back( reqId );
					// IMPORTANT: the message IPC key must be appropriately set at this point, to
					// well identify the request and the corresponding response
					DEBUG(NAME + "Sending request now to peer, timeout " + ISTR(timeoutMsec));
					DEBUG(NAME + "OUTGOING: " + msg.dataStr());
					m_channel->getWriter().send(msg);
                }

                DEBUG(NAME + "Waiting response from peer");
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

                std::list<std::string>::iterator it = std::find( m_outgoingRequests.begin(), m_outgoingRequests.end(), reqId );
                if (it != m_outgoingRequests.end())
                {
                	m_outgoingRequests.erase(it);
                }

                retries = 2;
                retryWait = false;
            }
            catch(SPELLipcError& ex)
            {
                // If it is a notification, just give back the error response right away
                if (msg.getType() == MSG_TYPE_NOTIFY || msg.getId() == MessageId::MSG_ID_NOTIFICATION)
                {
                    LOG_ERROR(NAME + "Notification timeout: will not retry");
                    LOG_ERROR(NAME + msg.dataStr());
                    response = SPELLipcHelper::createResponse(msg.getId(),msg);
                    response.setSequence(msg.getSequence());
                    response.setKey(msg.getKey());
                    retryWait = false;
                }
                else
                {
                    LOG_ERROR(NAME + "Failed to perform request " + msg.getId() + " due to timeout");
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
					}
					else
					{
	                    LOG_ERROR("#######################################################################");
	                    LOG_ERROR("Request " + msg.dataStr());
	                    LOG_ERROR("RESPONSE TIMED OUT, RETRYING (" + ISTR(retries) + "/" + ISTR(IPC_REQUEST_MAX_RETRIES) + ")");
	                    LOG_ERROR("TIMEOUT: " + ISTR(timeoutMsec) + " ms." );
	                    LOG_ERROR("ID: " + msg.getId() + " IPC KEY: " + ISTR(msg.getKey()) + " SEQ: " + ISTR(msg.getSequence()));
	                    LOG_ERROR("#######################################################################");
						LOG_ERROR(NAME + "Wait before retrying...");
						usleep(m_timeoutIpcRetry);
						LOG_ERROR(NAME + "Retrying request");
		                retryWait = true;
					}
	                retries++;
                }
            }
        }
    }

    return response;
}

//=============================================================================
// METHOD: SPELLipcClientInterface:initialize
//=============================================================================
void SPELLipcClientInterface::initialize( SPELLipcInterfaceListener* listener )
{
	m_listener = listener;
}

//=============================================================================
// METHOD: SPELLipcClientInterface:removeListener
//=============================================================================
void SPELLipcClientInterface::removeListener()
{
	m_listener = NULL;
}

