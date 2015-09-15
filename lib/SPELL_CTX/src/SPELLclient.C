// ################################################################################
// FILE       : SPELLclient.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the client
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
#include "SPELL_CTX/SPELLclient.H"
#include "SPELL_CTX/SPELLcontext.H"
#include "SPELL_CTX/SPELLexecutorManager.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_IPC/SPELLipcHelper.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_EXC/SPELLcommand.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_UTIL/SPELLtime.H"
#include "SPELL_IPC/SPELLtimeoutValues.H"
#include "SPELL_IPC/SPELLipc_Context.H"
#include "SPELL_IPC/SPELLipc_Executor.H"
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////
#define NAME std::string("[CLIENT ") + ISTR(getClientKey()) + ":" + getClientHost() + "] "

//=============================================================================
// CONSTRUCTOR: SPELLclient::SPELLclient()
//=============================================================================
SPELLclient::SPELLclient( int clientKey, const std::string& host, SPELLclientIPC& ipc )
: SPELLclientListener(),
  m_ipcKey(clientKey),
  m_host(host),
  m_clientIPC(ipc)
{
	DEBUG( NAME + "Created");
	m_ipcTimeoutGuiRequestMsec = SPELLconfiguration::instance().commonOrDefault( "GuiRequestTimeout", IPC_GUIREQUEST_DEFAULT_TIMEOUT_MSEC );
	m_clientIPC.registerInterest( clientKey , this );
}

//=============================================================================
// DESTRUCTOR: SPELLclient::~SPELLclient()
//=============================================================================
SPELLclient::~SPELLclient()
{
	m_clientIPC.unregisterInterest( getClientKey(), this );
	DEBUG( NAME + "Destroyed" );
}

//=============================================================================
// METHOD: SPELLclient::
//=============================================================================
void SPELLclient::sendMessageToClient( const SPELLipcMessage& msg )
{
	TICK_IN;
	m_clientIPC.sendMessage( getClientKey(), msg );
	TICK_OUT;
}

//=============================================================================
// METHOD: SPELLclient::
//=============================================================================
SPELLipcMessage SPELLclient::sendRequestToClient( const SPELLipcMessage& msg )
{
	TICK_IN;
	SPELLipcMessage result = VOID_MESSAGE;
	DEBUG( NAME + "Send request to client: " + msg.dataStr() );
	result =  m_clientIPC.sendRequest( getClientKey(), msg, m_ipcTimeoutGuiRequestMsec );
	DEBUG( NAME + "Response for request to client: " + result.dataStr() );
	TICK_OUT;
	return result;
}

//=============================================================================
// METHOD: SPELLclient::
//=============================================================================
SPELLipcMessage SPELLclient::sendRequestToClient( const SPELLipcMessage& msg, unsigned long timeoutMsec )
{
	TICK_IN;
	SPELLipcMessage result = VOID_MESSAGE;
	result = m_clientIPC.sendRequest( getClientKey(), msg, timeoutMsec );
	TICK_OUT;
	return result;
}

//=============================================================================
// METHOD: SPELLclient::
//=============================================================================
void SPELLclient::processMessageFromClient( const SPELLipcMessage& msg )
{
	std::string procId = msg.get( MessageField::FIELD_PROC_ID );
	try
	{
		SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor(procId);
		if (msg.getId() == ExecutorMessages::ACKNOWLEDGE)
		{
			if (exec->getControllingClient() != this)
			{
				return;
			}
		}
		exec->sendMessageToExecutor(msg);
	}
	catch(SPELLcoreException& ex)
	{
		LOG_ERROR("Cannot send message, no suitable executor found ('" + procId + "')");
	}
}

//=============================================================================
// METHOD: SPELLclient::
//=============================================================================
SPELLipcMessage SPELLclient::processRequestFromClient( const SPELLipcMessage& msg )
{
	//DEBUG("  Client request start");
    // Create the login message
    std::string requestId = msg.getId();
    SPELLipcMessage resp = VOID_MESSAGE;

    try
    {
		if ( requestId == ContextMessages::REQ_INSTANCE_ID )
		{
			resp = SPELLcontext::instance().getInstanceId( msg );
		}
		else if ( requestId == ContextMessages::REQ_OPEN_EXEC )
		{
		    resp = SPELLcontext::instance().openExecutor( msg, this );
		}
		else if ( requestId == ContextMessages::REQ_CLOSE_EXEC )
		{
		    resp = SPELLcontext::instance().closeExecutor( msg );
		}
		else if ( requestId == ContextMessages::REQ_KILL_EXEC )
		{
		    resp = SPELLcontext::instance().killExecutor( msg );
		}
		else if ( requestId == ContextMessages::REQ_ATTACH_EXEC )
		{
			resp = SPELLcontext::instance().attachExecutor( msg, this );
		}
		else if ( requestId == ContextMessages::REQ_DETACH_EXEC )
		{
			resp = SPELLcontext::instance().detachExecutor( msg, this );
		}
		else if (msg.getId() == ContextMessages::REQ_RECOVER_EXEC)
		{
			resp = SPELLcontext::instance().recoverExecutor( msg, this );
		}
		else if (msg.getId() == ContextMessages::REQ_PROC_LIST)
		{
			resp = SPELLcontext::instance().getProcedureList( msg );
		}
		else if (msg.getId() == ContextMessages::REQ_EXEC_LIST)
		{
			resp = SPELLcontext::instance().getExecutorList( msg );
		}
		else if (msg.getId() == ContextMessages::REQ_PROC_PROP)
		{
			resp = SPELLcontext::instance().getProcedureProperties( msg );
		}
		else if (msg.getId() == ContextMessages::REQ_PROC_CODE)
		{
			resp = SPELLcontext::instance().getProcedureCode( msg );
		}
		else if (msg.getId() == ContextMessages::REQ_EXEC_INFO)
		{
			resp = SPELLcontext::instance().getExecutorInfo( msg );
		}
		else if (msg.getId() == ContextMessages::REQ_CLIENT_INFO)
		{
			resp = SPELLcontext::instance().getClientInfo( msg );
		}
		else if (msg.getId() == ContextMessages::REQ_SERVER_FILE_PATH)
		{
			resp = SPELLcontext::instance().getServerFilePath( msg );
		}
		else if (msg.getId() == ContextMessages::REQ_RECOVERY_LIST)
		{
			resp = SPELLcontext::instance().getProcedureRecoveryList( msg );
		}
		else if (msg.getId() == ContextMessages::REQ_ASRUN_LIST)
		{
			resp = SPELLcontext::instance().getProcedureAsRunList( msg );
		}
		else if (msg.getId() == ContextMessages::REQ_DELETE_RECOVERY )
		{
			resp = SPELLcontext::instance().deleteRecoveryFiles(msg);
		}
		else if (msg.getId() == ContextMessages::REQ_REMOVE_CONTROL )
		{
			resp = SPELLcontext::instance().removeControl(msg);
		}
		else if (msg.getId() == ContextMessages::REQ_SET_BACKGROUND )
		{
			resp = SPELLcontext::instance().setExecutorInBackground(msg);
		}
		else if (msg.getId() == ContextMessages::REQ_LIST_FILES )
		{
			resp = SPELLcontext::instance().listFiles(msg);
		}
		else if (msg.getId() == ContextMessages::REQ_LIST_DATADIRS )
		{
			resp = SPELLcontext::instance().listDataDirectories(msg);
		}
		else if (msg.getId() == ContextMessages::REQ_INPUT_FILE )
		{
			resp = SPELLcontext::instance().getInputFile(msg);
		}
		else if (msg.getId() == ContextMessages::REQ_CURRENT_TIME )
		{
			resp = SPELLcontext::instance().getCurrentTime(msg);
		}
	    else if ( requestId == ContextMessages::REQ_DEL_SHARED_DATA )
	    {
	    	resp = SPELLcontext::instance().clearSharedData(msg);
	    }
	    else if ( requestId == ContextMessages::REQ_SET_SHARED_DATA )
	    {
	    	resp = SPELLcontext::instance().setSharedData(msg);
	    }
	    else if ( requestId == ContextMessages::REQ_GET_SHARED_DATA )
	    {
	    	resp = SPELLcontext::instance().getSharedData(msg);
	    }
	    else if ( requestId == ContextMessages::REQ_GET_SHARED_DATA_KEYS )
	    {
	    	resp = SPELLcontext::instance().getSharedDataKeys(msg);
	    }
	    else if ( requestId == ContextMessages::REQ_GET_SHARED_DATA_SCOPES )
	    {
	    	resp = SPELLcontext::instance().getSharedDataScopes(msg);
	    }
	    else if ( requestId == ContextMessages::REQ_DEL_SHARED_DATA_SCOPE )
	    {
	    	resp = SPELLcontext::instance().removeSharedDataScope(msg);
	    }
	    else if ( requestId == ContextMessages::REQ_ADD_SHARED_DATA_SCOPE )
	    {
	    	resp = SPELLcontext::instance().addSharedDataScope(msg);
	    }
	    else if ( requestId == ContextMessages::REQ_GET_CTX_EXEC_DFLT )
		{
			resp = SPELLcontext::instance().getExecutorDefaults(msg);
		}
	    else if ( requestId == ContextMessages::REQ_SET_CTX_EXEC_DFLT )
		{
			resp = SPELLcontext::instance().setExecutorDefaults(msg);
		}

		else
		{
			// Forward the request to the executor
			std::string procId = msg.get( MessageField::FIELD_PROC_ID );
			try
			{
				DEBUG( NAME + "Forward request to executor " + procId +": " + msg.getId());
				SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor(procId);
				resp = exec->sendRequestToExecutor(msg);
				if (resp.isVoid())
				{
					LOG_ERROR("Unable to obtain response for client request " + msg.getId() + " from executor ");
				}
			}
			catch( SPELLcoreException& ex )
			{
				LOG_ERROR( NAME + "Unable to send request to executor '" + procId + "', unable to find");
				std::string id = msg.getId();
				SPELLutils::replace(id, "REQ", "RSP");
				resp = SPELLipcHelper::createErrorResponse(id,msg);
				resp.set(MessageField::FIELD_ERROR, "Unable to send request to executor " + procId);
				resp.set(MessageField::FIELD_REASON, "Cannot find the executor");
				resp.set(MessageField::FIELD_FATAL, PythonConstants::True );
			}
		}
    }
    catch(SPELLexecutorManager::OperationError& err)
    {
    	LOG_ERROR( NAME + "Error in client request processing " + ISTR(err.errorCode));
		std::string id = msg.getId();
		SPELLutils::replace(id, "REQ", "RSP");
		resp = SPELLipcHelper::createErrorResponse(id,msg);
		resp.set(MessageField::FIELD_ERROR, "Error in client processing");
		resp.set(MessageField::FIELD_REASON, err.message);
		resp.set(MessageField::FIELD_FATAL, PythonConstants::True);
    }
    catch(...)
    {
    	LOG_ERROR( NAME + "Unknown error in client request processing");
		std::string id = msg.getId();
		SPELLutils::replace(id, "REQ", "RSP");
		resp = SPELLipcHelper::createErrorResponse(id,msg);
		resp.set(MessageField::FIELD_ERROR, "Error in client processing");
		resp.set(MessageField::FIELD_REASON, "Unknown reason");
		resp.set(MessageField::FIELD_FATAL, PythonConstants::True);
    }
	//DEBUG("  Client request end");
	return resp;
}

//=============================================================================
// METHOD: SPELLclient::
//=============================================================================
void SPELLclient::addProcedure( const std::string& procId, const SPELLclientMode& mode )
{
	m_procedures.insert( std::make_pair( procId, mode ) );
}

//=============================================================================
// METHOD: SPELLclient::
//=============================================================================
void SPELLclient::removeProcedure( const std::string& procId )
{
	ProcedureList::iterator it = m_procedures.find(procId);
	if (it != m_procedures.end())
	{
		m_procedures.erase(it);
	}
}

//=============================================================================
// METHOD: SPELLclient::
//=============================================================================
SPELLclient::ProcedureList SPELLclient::getProcedures()
{
	return m_procedures;
}

//=============================================================================
// METHOD: SPELLclient::
//=============================================================================
void SPELLclient::cancelRequestsToClient()
{
	m_clientIPC.cancelRequestsToClient(m_ipcKey);
}
