// ################################################################################
// FILE       : SPELLexecutorIPC.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the executor IPC interface
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
#include "SPELL_CTX/SPELLexecutorIPC.H"
#include "SPELL_CTX/SPELLexecutorManager.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_UTIL/SPELLtime.H"
#include "SPELL_IPC/SPELLipcHelper.H"
#include "SPELL_IPC/SPELLipc_Executor.H"
#include "SPELL_IPC/SPELLipc_Context.H"
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////


//=============================================================================
// CONSTRUCTOR: SPELLexecutorIPC::SPELLexecutorIPC()
//=============================================================================
SPELLexecutorIPC::SPELLexecutorIPC( SPELLexecutor& controller, const SPELLexecutorStartupParams& config )
: SPELLipcInterfaceListener(),
  m_ipc("CTX-TO-EXECUTOR",999,config.getIpcPort()), // If port is zero, let the interface get a free port
  m_controller(controller)
{
	m_connected = false;
}

//=============================================================================
// DESTRUCTOR: SPELLexecutorIPC::~SPELLexecutorIPC()
//=============================================================================
SPELLexecutorIPC::~SPELLexecutorIPC()
{
	DEBUG("[EXCIPC] Destroying");
	m_ipc.removeListener();
	m_ipc.join();
	DEBUG("[EXCIPC] Destroyed");
}

//=============================================================================
// METHOD: SPELLexecutorIPC::
//=============================================================================
void SPELLexecutorIPC::setup()
{
	DEBUG("[EXCIPC] Setup");
	m_ipc.initialize(&*this);
	m_ipc.connect();
	m_ipc.start();
	m_connected = true;
}

//=============================================================================
// METHOD: SPELLexecutorIPC::
//=============================================================================
void SPELLexecutorIPC::cleanup()
{
	SPELLmonitor m(m_notifyLock);
	DEBUG("[EXCIPC] Cleanup");
	m_connected = false;
	DEBUG("[EXCIPC] Close executor connection");
	m_ipc.removeListener();
	DEBUG("[EXCIPC] Perform disconnection");
	m_ipc.disconnect();
	DEBUG("[EXCIPC] Close executor connection done");
}

//=============================================================================
// METHOD: SPELLexecutorIPC::
//=============================================================================
int SPELLexecutorIPC::getPort()
{
	return m_ipc.getPort();
}

//=============================================================================
// METHOD: SPELLexecutorIPC::
//=============================================================================
void SPELLexecutorIPC::sendMessage( const std::string& executorId, const SPELLipcMessage& msg )
{
	if (m_connected)
	{
		SPELLipcMessage toSend(msg);
		m_ipc.sendMessage(toSend);
	}
}

//=============================================================================
// METHOD: SPELLexecutorIPC::
//=============================================================================
SPELLipcMessage SPELLexecutorIPC::sendRequest( const std::string& executorId, const SPELLipcMessage& msg, unsigned long timeoutMsec )
{
	if (m_connected)
	{
		SPELLipcMessage toSend(msg);
		return m_ipc.sendRequest(toSend,timeoutMsec);
	}
	else
	{
		SPELLipcMessage resp = SPELLipcHelper::createErrorResponse("IpcErrorResponse", msg);
		resp.set( MessageField::FIELD_ERROR, "Cannot send request" );
		resp.set( MessageField::FIELD_REASON, "IPC not connected");
		resp.set( MessageField::FIELD_FATAL, PythonConstants::True );
		return resp;
	}
}

//=============================================================================
// METHOD: SPELLexecutorIPC::
//=============================================================================
void SPELLexecutorIPC::processMessage( const SPELLipcMessage& msg )
{
	TICK_IN;
	DEBUG("[EXCIPC] Received message from executor: " + msg.dataStr());

	// Certain messages are for monitoring clients only
	if ((msg.getId() != MessageId::MSG_ID_PROMPT_START)&&(msg.getId() != MessageId::MSG_ID_PROMPT_END))
	{
		DEBUG("[EXCIPC] Forward message to controller");
		m_controller.processMessageFromExecutor(msg);
	}

	if (msg.getId() != ExecutorMessages::MSG_NOTIF_EXEC_CLOSE)
	{
		DEBUG("[EXCIPC] Forward message to monitoring clients");
		notifyMessage(msg);
	}
	DEBUG("[EXCIPC] Message processed");
	TICK_OUT;
}

//=============================================================================
// METHOD: SPELLexecutorIPC::
//=============================================================================
SPELLipcMessage SPELLexecutorIPC::processRequest( const SPELLipcMessage& msg )
{
	TICK_IN;

	DEBUG("[EXCIPC] Received request from executor: " + msg.dataStr());
	SPELLipcMessage resp = VOID_MESSAGE;

	DEBUG("[EXCIPC] Forward request to controller");
	resp = m_controller.processRequestFromExecutor(msg);

	// Certain requests are to be sent to controlling clients only
	if ( shouldForwardToMonitoring(msg) )
	{
		DEBUG("[EXCIPC] Forward to monitoring clients");
		// Notify request
		notifyRequest(msg);
	}

	DEBUG("[EXCIPC] Request processed");
	TICK_OUT;
	return resp;
}

//=============================================================================
// METHOD: SPELLexecutorIPC::processConnectionError
//=============================================================================
bool SPELLexecutorIPC::shouldForwardToMonitoring( const SPELLipcMessage& msg )
{
	std::string requestId = msg.getId();

	if (msg.getType() == MSG_TYPE_PROMPT)
	{
		return false;
	}
	if ( requestId == ContextMessages::REQ_OPEN_EXEC )
    {
        return false;
    }
    else if ( requestId == ContextMessages::REQ_INSTANCE_ID )
    {
        return false;
    }
    else if ( requestId == ContextMessages::REQ_EXEC_INFO )
    {
        return false;
    }
    else if ( requestId == ContextMessages::REQ_DEL_SHARED_DATA )
    {
        return false;
    }
    else if ( requestId == ContextMessages::REQ_SET_SHARED_DATA )
    {
        return false;
    }
    else if ( requestId == ContextMessages::REQ_GET_SHARED_DATA )
    {
        return false;
    }
    else if ( requestId == ContextMessages::REQ_GET_SHARED_DATA_KEYS )
    {
        return false;
    }
	return true;
}

//=============================================================================
// METHOD: SPELLexecutorIPC::processConnectionError
//=============================================================================
void SPELLexecutorIPC::processConnectionError( int peerKey, std::string error, std::string reason )
{
	if (m_connected)
	{
		LOG_ERROR("Executor IPC error: " + error + ": " + reason);
		cleanup();
		SPELLexecutorManager::instance().executorLost( m_controller );
	}
}

//=============================================================================
// METHOD: SPELLexecutorIPC::processConnectionClosed
//=============================================================================
void SPELLexecutorIPC::processConnectionClosed( int peerKey )
{
	if (m_connected)
	{
		LOG_WARN("Executor closed connection");
		m_connected = false;
		SPELLexecutorManager::instance().executorLost( m_controller );
	}
}

//=============================================================================
// METHOD: SPELLexecutorIPC::
//=============================================================================
void SPELLexecutorIPC::notifyMessage( const SPELLipcMessage& msg )
{
	if (m_connected)
	{
		for( NotifierMap::iterator it = m_notifiers.begin(); it != m_notifiers.end(); it++)
		{
			SPELLmonitor m(m_notifyLock);
			if (it->second!=NULL) it->second->processMessageFromExecutor(msg);
		}
	}
}

//=============================================================================
// METHOD: SPELLexecutorIPC::
//=============================================================================
void SPELLexecutorIPC::notifyRequest( const SPELLipcMessage& msg )
{
	if (m_connected)
	{
		for( NotifierMap::iterator it = m_notifiers.begin(); it != m_notifiers.end(); it++)
		{
			SPELLmonitor m(m_notifyLock);
			if (it->second!=NULL) it->second->processRequestFromExecutor(msg);
		}
	}
}

//=============================================================================
// METHOD: SPELLexecutorIPC::
//=============================================================================
void SPELLexecutorIPC::registerExecutorNotifier( std::string id, SPELLexecutorListener* notifier )
{
	SPELLmonitor m(m_notifyLock);
	LOG_INFO("[EXCIPC] #### Register notifier listener: " + id);
	NotifierMap::iterator it = m_notifiers.find(id);
	if (it == m_notifiers.end())
	{
		m_notifiers[id] = notifier;
	}
}

//=============================================================================
// METHOD: SPELLexecutorIPC::
//=============================================================================
void SPELLexecutorIPC::deregisterExecutorNotifier( std::string id )
{
	SPELLmonitor m(m_notifyLock);
	LOG_INFO("[EXCIPC] #### Remove notifier listener: " + id);
	NotifierMap::iterator it = m_notifiers.find(id);
	if (it != m_notifiers.end())
	{
		m_notifiers.erase(it);
	}
}
