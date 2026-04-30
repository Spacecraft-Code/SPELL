// ################################################################################
// FILE       : SPELLlistenerIPC.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the listener IPC interface
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
#include "SPELL_CTX/SPELLcontext.H"
#include "SPELL_CTX/SPELLlistenerIPC.H"
#include "SPELL_CTX/SPELLexecutorManager.H"
// Project includes --------------------------------------------------------
#include "SPELL_IPC/SPELLipcHelper.H"
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_IPC/SPELLipc_Context.H"
#include "SPELL_IPC/SPELLipc_Listener.H"
#include "SPELL_IPC/SPELLipc_Executor.H"
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////


//=============================================================================
// CONSTRUCTOR: SPELLlistenerIPC::SPELLlistenerIPC()
//=============================================================================
SPELLlistenerIPC::SPELLlistenerIPC()
{
	m_ipc = NULL;
}

//=============================================================================
// DESTRUCTOR: SPELLlistenerIPC::~SPELLlistenerIPC()
//=============================================================================
SPELLlistenerIPC::~SPELLlistenerIPC()
{
	delete m_ipc;
	m_ipc = NULL;
}

//=============================================================================
// METHOD: SPELLlistenerIPC::
//=============================================================================
void SPELLlistenerIPC::setup()
{
	LOG_INFO("Setting up connection with listener");

	m_ipc = new SPELLipcClientInterface("CTX-TO-LST", SPELLcontext::instance().getListenerHost(), SPELLcontext::instance().getListenerPort() );
	m_ipc->initialize(&*this);
	m_ipc->connect();

	// Login into listener
	SPELLipcMessage login( ListenerMessages::MSG_CONTEXT_OPEN );
	login.set( MessageField::FIELD_CTX_NAME, SPELLcontext::instance().getContextName() );
	login.set( MessageField::FIELD_CTX_PORT, ISTR(SPELLcontext::instance().getClientPort()) );
	login.setType( MSG_TYPE_ONEWAY );
	login.setSender("CTX");
	login.setReceiver("LST");
	m_ipc->sendMessage( login );
	LOG_INFO("Context logged into listener");
}

//=============================================================================
// METHOD: SPELLlistenerIPC::
//=============================================================================
void SPELLlistenerIPC::cleanup()
{
	DEBUG("Cleaning up context connection to listener");
	// Logout from listener
	SPELLipcMessage logout( ListenerMessages::MSG_CONTEXT_CLOSED );
	logout.set( MessageField::FIELD_CTX_NAME, SPELLcontext::instance().getContextName() );
	logout.setType( MSG_TYPE_ONEWAY );
	logout.setSender("CTX");
	logout.setReceiver("LST");
	m_ipc->sendMessage( logout );
	LOG_INFO("Context logged out from listener");
	DEBUG("Disconnecting IPC client interface");
	m_ipc->disconnect();
	DEBUG("Disconnecting IPC client interface done");
}

//=============================================================================
// METHOD: SPELLlistenerIPC::
//=============================================================================
void SPELLlistenerIPC::processMessage( const SPELLipcMessage& msg )
{
	if (msg.getId() == ContextMessages::MSG_CLOSE_CTX)
	{
		DEBUG("Raising event to close context");
		SPELLcontext::instance().readyToFinish();
	}
	else
	{
		LOG_ERROR("Listener message not handled: " + msg.getId());
	}
}

//=============================================================================
// METHOD: SPELLlistenerIPC::
//=============================================================================
SPELLipcMessage SPELLlistenerIPC::processRequest( const SPELLipcMessage& msg )
{
	SPELLipcMessage resp = VOID_MESSAGE;
    DEBUG("Received IPC request: " + msg.getId());
	if (msg.getId() == ContextMessages::REQ_CAN_CLOSE)
	{
		resp = request_CanClose(msg);
	}
	else
	{
		LOG_ERROR("Listener request not handled: " + msg.getId());
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLlistenerIPC::processConnectionError
//=============================================================================
void SPELLlistenerIPC::processConnectionError( int peerKey, std::string error, std::string reason )
{
	LOG_ERROR("Listener connection error:" + error + ": " + reason );
}

//=============================================================================
// METHOD: SPELLlistenerIPC::processConnectionClosed
//=============================================================================
void SPELLlistenerIPC::processConnectionClosed( int peerKey )
{
	LOG_WARN("Listener connection closed");
}

//=============================================================================
// METHOD: SPELLlistenerIPC::
//=============================================================================
SPELLipcMessage SPELLlistenerIPC::request_CanClose( const SPELLipcMessage& msg )
{
	SPELLipcMessage resp = SPELLipcHelper::createResponse( ContextMessages::RSP_CAN_CLOSE, msg );
	unsigned int numActiveProcs = SPELLcontext::instance().getNumActiveProcedures();
	if (numActiveProcs == 0)
	{
		resp.set( MessageField::FIELD_BOOL, "True" );
	}
	else
	{
		resp.set( MessageField::FIELD_BOOL, "False" );
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLlistenerIPC::
//=============================================================================
void SPELLlistenerIPC::sendMessage( const SPELLipcMessage& msg )
{
	SPELLipcMessage toSend(msg);
	m_ipc->sendMessage(toSend);
}

//=============================================================================
// METHOD: SPELLlistenerIPC::
//=============================================================================
SPELLipcMessage SPELLlistenerIPC::sendRequest( const SPELLipcMessage& msg, unsigned long timeoutMsec )
{
	SPELLipcMessage toSend(msg);
	return m_ipc->sendRequest(toSend,timeoutMsec);
}
