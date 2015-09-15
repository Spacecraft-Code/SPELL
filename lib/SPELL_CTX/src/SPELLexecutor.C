// ################################################################################
// FILE       : SPELLexecutor.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the executor manager
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
#include "SPELL_CTX/SPELLexecutor.H"
#include "SPELL_CTX/SPELLexecutorManager.H"
#include "SPELL_CTX/SPELLdataHelper.H"
#include "SPELL_CTX/SPELLcontext.H"
// Project includes --------------------------------------------------------
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_SYN/SPELLthread.H"
#include "SPELL_IPC/SPELLipcHelper.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLtime.H"
#include "SPELL_IPC/SPELLipc_Executor.H"
#include "SPELL_IPC/SPELLipc_Context.H"
// System includes ---------------------------------------------------------

class LoginMonitor : public SPELLthread
{
public:
	LoginMonitor( SPELLexecutor& exec )
	: SPELLthread("login-" + exec.getModel().getInstanceId() ),
	  m_exec( exec )
	{}

	void run()
	{
		SPELLtime checkStart;
		for(;;)
		{
			SPELLtime now;
			SPELLtime delta = now - checkStart;
			if (delta.getSeconds()>120)
			{
				SPELLexecutorManager::instance().callback_executorNotReconnected( m_exec.getModel().getInstanceId() );
				return;
			}
			else
			{
				if (m_exec.isLoggedIn())
				{
					SPELLexecutorManager::instance().callback_executorReconnected( m_exec.getModel().getInstanceId() );
					return;
				}
				else
				{
					usleep(30000);
				}
			}
		}
	}

private:
	SPELLexecutor& m_exec;
};

// DEFINES /////////////////////////////////////////////////////////////////

#define DEFAULT_START_TIMEOUT 20
#define DEFAULT_LOGIN_TIMEOUT 60


//=============================================================================
// CONSTRUCTOR: SPELLexecutor::SPELLexecutor()
//=============================================================================
SPELLexecutor::SPELLexecutor( const SPELLexecutorStartupParams& config, SPELLclient* controllingClient )
: SPELLexecutorListener(),
  SPELLprocessListener(),
  m_model(config),
  m_ipc(*this,config),
  m_controllingClient(controllingClient)
{
	SPELLprocessManager::instance().addListener( m_model.getInstanceId(), this );
	switch(config.getClientMode())
	{
	case CLIENT_MODE_BACKGROUND:
		m_clientConnection = CLI_BACKGROUND;
		break;
	default:
		m_clientConnection = CLI_CONNECTED;
		break;
	}
	m_reconnecting = config.isReconnecting();
	m_loggedIn = false;
	m_loginMonitor = NULL;
}

//=============================================================================
// DESTRUCTOR: SPELLexecutor::~SPELLexecutor()
//=============================================================================
SPELLexecutor::~SPELLexecutor()
{
	SPELLprocessManager::instance().removeListener( m_model.getInstanceId(), this );
	m_ipc.cleanup();
	if (m_loginMonitor)
	{
		try
		{
			m_loginMonitor->join();
		}
		catch(...){};
	}
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::start()
{
	LOG_INFO("Starting executor " + m_model.getInstanceId());
	m_ipc.setup();
	m_model.setIpcPort( m_ipc.getPort() );
	m_executorLoggedInEvent.clear();

	if (!m_reconnecting)
	{
		m_processStartedEvent.clear();
		std::string command = SPELLconfiguration::instance().getContextParameter( SPELLcontextConfig::ExecutorProgram );
		if (command == "")
		{
			THROW_EXCEPTION("Cannot launch executor", "No executor command defined in configuration", SPELL_ERROR_CONFIG);
		}

		int startTimeout = -1;
		int loginTimeout = -1;
		std::string startTimeoutStr = SPELLconfiguration::instance().getContextParameter( SPELLcontextConfig::ExecutorStartTimeout );
		std::string loginTimeoutStr = SPELLconfiguration::instance().getContextParameter( SPELLcontextConfig::ExecutorLoginTimeout );
		if (startTimeoutStr != "")
		{
			startTimeout = STRI(startTimeoutStr);
		}
		if (loginTimeoutStr != "")
		{
			loginTimeout = STRI(loginTimeoutStr);
		}
		if (startTimeout == -1) startTimeout = DEFAULT_START_TIMEOUT;
		if (loginTimeout == -1) loginTimeout = DEFAULT_LOGIN_TIMEOUT;

		LOG_INFO("Using start timeout: " + ISTR(startTimeout) + " seconds");
		LOG_INFO("Using login timeout: " + ISTR(loginTimeout) + " seconds");

		command += " -c " + m_model.getConfigFile();
		command += " -n " + m_model.getContextName();
		command += " -s " + ISTR(m_ipc.getPort());
		command += " -p " + m_model.getInstanceId(); // Parent id
		command += " -w ";
		if (m_model.getWsFilename() != "")
		{
			command += " -r " + m_model.getWsFilename();
		}

		SPELLprocessManager::instance().clearProcess( m_model.getInstanceId() );
		SPELLprocessManager::instance().startProcess( m_model.getInstanceId(), command );

		DEBUG("Waiting executor process to begin");
		bool timedOut = m_processStartedEvent.wait( startTimeout * 1000 ); // milliseconds
		if (timedOut)
		{
			THROW_EXCEPTION("Cannot launch executor", "Executor process did not begin in time", SPELL_ERROR_PROCESS);
		}
		DEBUG("Start event received");

		m_processStatus = SPELLprocessManager::instance().getProcessStatus( m_model.getInstanceId() );
		if (m_processStatus == PSTATUS_RUNNING)
		{
			if (!m_loggedIn)
			{
				DEBUG("Executor process started, waiting for login");
				bool timeout = m_executorLoggedInEvent.wait( loginTimeout * 1000 ); // milliseconds
				if (timeout)
				{
					THROW_EXCEPTION("Cannot launch executor", "Executor did not login in time", SPELL_ERROR_PROCESS);
				}
			}
			DEBUG("Executor process logged in");
			m_model.setPID( SPELLprocessManager::instance().getProcessId( m_model.getInstanceId() ));
			LOG_INFO("Executor started: " + m_model.getInstanceId() + " with pid " + ISTR(m_model.getPID()));
		}
		else if (m_processStatus == PSTATUS_FINISHED )
		{
			THROW_EXCEPTION("Cannot launch executor", "Executor process finished too quickly", SPELL_ERROR_PROCESS);
		}
		else if (m_processStatus == PSTATUS_FAILED )
		{
			THROW_EXCEPTION("Cannot launch executor", "Executor process failed to start", SPELL_ERROR_PROCESS);
		}
		else if (m_processStatus == PSTATUS_KILLED )
		{
			THROW_EXCEPTION("Cannot launch executor", "Executor process crashed or was killed", SPELL_ERROR_PROCESS);
		}
		else
		{
			THROW_EXCEPTION("Cannot launch executor", "Executor process in unexpected state: " + SPELLprocessUtils::processStatusToString(m_processStatus), SPELL_ERROR_PROCESS);
		}
	}
	else
	{
		m_loginMonitor = new LoginMonitor(*this);
		m_loginMonitor->start();
	}
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
bool SPELLexecutor::waitLogin( unsigned int timeoutMsec )
{
	if (m_loggedIn) return true;
	bool timeout = m_executorLoggedInEvent.wait( timeoutMsec); // milliseconds
	return !timeout;
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
bool SPELLexecutor::processOk()
{
	return (m_processStatus == PSTATUS_RUNNING);
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::close()
{
	bool needToClose = processOk();

	if (needToClose)
	{
		LOG_INFO("Closing executor " + m_model.getInstanceId());
		m_executorLoggedOutEvent.clear();

		// This has effect only in non-child processes (recovered executors)
		SPELLprocessManager::instance().aboutToCloseProcess( m_model.getInstanceId() );

		ExecutorCommand cmd;
		cmd.id = CMD_CLOSE;
		command(cmd);
		bool timeout = m_executorLoggedOutEvent.wait(5 * 1000); // Wait for 5 seconds

		DEBUG("Executor logged out or about to be killed, disconnecting IPC");
		m_ipc.cleanup();
		DEBUG("Executor logged out or about to be killed, disconnecting IPC done");

		if (timeout)
		{
			LOG_ERROR("Failed to close executor, it did not log out in time, it will be killed");
			try
			{
				SPELLprocessManager::instance().removeListener( m_model.getInstanceId(), this );
				SPELLprocessManager::instance().killProcess( m_model.getInstanceId() );
				SPELLprocessManager::instance().waitProcess( m_model.getInstanceId() );
				SPELLprocessManager::instance().clearProcess( m_model.getInstanceId() );
			}
			catch( SPELLcoreException& ex )
			{
				LOG_ERROR("Could not kill process: " + m_model.getInstanceId());
			}
		}
		else
		{
			// Wait only if the process is known to be still alive
			DEBUG("Now waiting process to finish");
			waitFinish();
			DEBUG("Waiting process to finish done");
		}
	}

	LOG_INFO("Executor closed: " + m_model.getInstanceId());
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::kill()
{
	bool needToKill = processOk();

	if (needToKill)
	{
		LOG_INFO("Killing executor " + m_model.getInstanceId());

		// Remove process listener, not to be notified for the kill
    	SPELLprocessManager::instance().removeListener( m_model.getInstanceId(), this );

		// Cancel requests to client
		SPELLclient* ctrl = getControllingClient();
		if (ctrl != NULL)
		{
			ctrl->cancelRequestsToClient();
		}

		// Disconnect IPC
		m_ipc.cleanup();

		try
		{
			SPELLprocessManager::instance().killProcess( m_model.getInstanceId() );
	    	SPELLprocessManager::instance().waitProcess( m_model.getInstanceId() );
	    	SPELLprocessManager::instance().clearProcess( m_model.getInstanceId() );
		}
		catch( SPELLcoreException& ex )
		{
			LOG_ERROR("Could not kill process: " + m_model.getInstanceId());
		}
		LOG_INFO("Executor killed: " + m_model.getInstanceId());
	}
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::sendMessageToExecutor( const SPELLipcMessage& msg )
{
	if (processOk())
	{
		m_ipc.sendMessage( m_model.getInstanceId(), msg );
	}
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
SPELLipcMessage SPELLexecutor::sendRequestToExecutor( const SPELLipcMessage& msg )
{
	SPELLipcMessage response = VOID_MESSAGE;
	if (processOk())
	{
		response =  m_ipc.sendRequest( m_model.getInstanceId(), msg, 5000 );
	}
	else
	{
		LOG_ERROR("Cannot forward request to executor, process is not OK");
		response = SPELLipcHelper::createErrorResponse( MessageId::MSG_ID_ERROR, msg );
		response.set( MessageField::FIELD_PROC_ID, m_model.getInstanceId() );
		response.set( MessageField::FIELD_ERROR, "Cannot send request " + msg.getId() );
		response.set( MessageField::FIELD_REASON, "Executor process crashed" );
		response.set( MessageField::FIELD_FATAL, PythonConstants::True );
	}
	return response;
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::processMessageFromExecutor( SPELLipcMessage msg )
{
	DEBUG("Executor message: " + msg.dataStr());
	std::string id = msg.getId();
	if (id == ExecutorMessages::MSG_NOTIF_EXEC_CLOSE )
	{
		DEBUG("Executor logged out");
		m_executorLoggedOutEvent.set();
		return;
	}
    else if ( id == MessageId::MSG_ID_NOTIFICATION )
    {
    	executorNotification(msg);
    }
    else if ( id == MessageId::MSG_ID_SET_UACTION )
    {
    	LOG_INFO("Executor set user action");
    	std::string label = msg.get(MessageField::FIELD_ACTION_LABEL);
    	std::string sevStr = msg.get(MessageField::FIELD_ACTION_SEVERITY);
    	unsigned int severity = LanguageConstants::INFORMATION;
    	getModel().getUserAction().setLabel(label);
    	if (sevStr == MessageValue::DATA_SEVERITY_WARN ) severity = LanguageConstants::WARNING;
    	if (sevStr == MessageValue::DATA_SEVERITY_ERROR ) severity = LanguageConstants::ERROR;
    	getModel().getUserAction().setSeverity(severity);
    	getModel().getUserAction().enable(true);
    }
    else if ( id == MessageId::MSG_ID_DISMISS_UACTION )
    {
    	LOG_INFO("Executor dismiss user action");
    	getModel().getUserAction().reset();
    }
    else if ( id == MessageId::MSG_ID_ENABLE_UACTION )
    {
    	LOG_INFO("Executor enable user action");
    	getModel().getUserAction().enable(true);
    }
    else if ( id == MessageId::MSG_ID_DISABLE_UACTION )
    {
    	LOG_INFO("Executor disable user action");
    	getModel().getUserAction().enable(false);
    }
	else
	{
		// In case of error, store the information
		if ( msg.getType() == MSG_TYPE_ERROR )
		{
			m_model.setError( msg.get( MessageField::FIELD_ERROR ), msg.get( MessageField::FIELD_REASON) );
			executorStatusChanged( STATUS_ERROR, msg.get( MessageField::FIELD_ERROR ), msg.get( MessageField::FIELD_REASON ) );
		}
	}
	forwardMessageToClient( msg );
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
SPELLipcMessage SPELLexecutor::processRequestFromExecutor( SPELLipcMessage msg )
{
	DEBUG("Executor request start: " + msg.getId());

	// Create the login message
    std::string requestId = msg.getId();

    SPELLipcMessage response = VOID_MESSAGE;

    // Otherwise continue processing, first local, then forward to client
    if (requestId == ExecutorMessages::REQ_NOTIF_EXEC_OPEN )
    {
    	response = executorLogin(msg);
    	return response;
    }

    if ( requestId == ContextMessages::REQ_OPEN_EXEC )
    {
        response = SPELLcontext::instance().openExecutor( msg, m_controllingClient );
    }
    else if ( requestId == ContextMessages::REQ_INSTANCE_ID )
    {
        response = SPELLcontext::instance().getInstanceId( msg );
    }
    else if ( requestId == ContextMessages::REQ_EXEC_INFO )
    {
        response = SPELLcontext::instance().getExecutorInfo( msg );
    }
    else if ( requestId == ContextMessages::REQ_DEL_SHARED_DATA )
    {
        response = SPELLcontext::instance().clearSharedData(msg);
    }
    else if ( requestId == ContextMessages::REQ_SET_SHARED_DATA )
    {
        response = SPELLcontext::instance().setSharedData(msg);
    }
    else if ( requestId == ContextMessages::REQ_GET_SHARED_DATA )
    {
        response = SPELLcontext::instance().getSharedData(msg);
    }
    else if ( requestId == ContextMessages::REQ_GET_SHARED_DATA_KEYS )
    {
        response = SPELLcontext::instance().getSharedDataKeys(msg);
    }
    else if ( requestId == ContextMessages::REQ_ADD_SHARED_DATA_SCOPE )
    {
        response = SPELLcontext::instance().addSharedDataScope(msg);
    }
    else if ( requestId == ContextMessages::REQ_DEL_SHARED_DATA_SCOPE )
    {
        response = SPELLcontext::instance().removeSharedDataScope(msg);
    }
    else if ( requestId == ContextMessages::REQ_GET_SHARED_DATA_SCOPES )
    {
        response = SPELLcontext::instance().getSharedDataScopes(msg);
    }
    else
    {
		response = forwardRequestToClient( msg );
    }

	DEBUG("Executor request end");
    return response;
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::processStarted( const std::string& identifier )
{
	DEBUG("Callback - Executor process started: " + identifier);
	m_processStatus = PSTATUS_RUNNING;
	m_processStartedEvent.set();
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::processFinished( const std::string& identifier, const int& retValue )
{
	LOG_INFO("Executor process finished: " + identifier);
	m_processStatus = PSTATUS_FINISHED;
	m_processStartedEvent.set();
	m_processStoppedEvent.set();
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::processKilled( const std::string& identifier )
{
	LOG_ERROR("Executor process killed: " + identifier);
	m_model.setStatus(STATUS_ERROR);
	m_processStatus = PSTATUS_KILLED;
	m_processStartedEvent.set();
	m_executorStatusEvent.set();
	m_ipc.cleanup();

	SPELLipcMessage error( MessageId::MSG_ID_ERROR );
	error.setType( MSG_TYPE_ERROR );
	error.set( MessageField::FIELD_PROC_ID, identifier );
	error.setSender("CTX");
	error.setReceiver("CLT");
	error.set( MessageField::FIELD_ERROR, "Lost connection wih executor" );
	error.set( MessageField::FIELD_REASON, "Process crashed" );
	error.set( MessageField::FIELD_FATAL, PythonConstants::True );

	LOG_ERROR("Communicate executor process killed: " + identifier);

	forwardMessageToClient( error );

	SPELLcontext::instance().executorLost( identifier );

	m_processStoppedEvent.set();
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::processFailed( const std::string& identifier )
{
	LOG_ERROR("Executor process failed startup: " + identifier);
	m_model.setStatus(STATUS_ABORTED);
	m_processStatus = PSTATUS_FAILED;
	m_processStartedEvent.set();
	m_executorStatusEvent.set();

	SPELLipcMessage error( MessageId::MSG_ID_ERROR );
	error.setType( MSG_TYPE_ERROR );
	error.set( MessageField::FIELD_PROC_ID, identifier );
	error.setSender("CTX");
	error.setReceiver("CLT");
	error.set( MessageField::FIELD_ERROR, "Could not start executor" );
	error.set( MessageField::FIELD_REASON, "Process crashed at startup" );
	error.set( MessageField::FIELD_FATAL, PythonConstants::True );

	forwardMessageToClient( error );

	SPELLcontext::instance().executorLost( identifier );

	m_processStoppedEvent.set();
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::waitFinish()
{
	if (m_processStatus == PSTATUS_RUNNING)
	{
		DEBUG("Waiting executor process to finish");
		m_processStoppedEvent.clear();
		bool timeout = m_processStoppedEvent.wait(2000);
		if (timeout)
		{
			LOG_WARN("Did not see the process close, try kill");
			SPELLprocessManager::instance().removeListener( m_model.getInstanceId(), this );
			try
			{
				SPELLprocessManager::instance().killProcess( m_model.getInstanceId() );
		    	SPELLprocessManager::instance().waitProcess( m_model.getInstanceId() );
			}
			catch( SPELLcoreException& ex )
			{
				LOG_ERROR("Could not kill process: " + m_model.getInstanceId());
			}
		}
		else
		{
			DEBUG("Executor process finished, stop waiting");
		}
	}
	SPELLprocessManager::instance().clearProcess( m_model.getInstanceId() );
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
bool SPELLexecutor::waitStatus( const SPELLexecutorStatus& status, unsigned int timeoutMSec )
{
	if (m_model.getStatus() != status)
	{
		DEBUG("Waiting for executor status " + SPELLdataHelper::executorStatusToString(status));
		while(true)
		{
			m_executorStatusEvent.clear();
			bool timeout = m_executorStatusEvent.wait(timeoutMSec);
			if (timeout)
			{
				DEBUG("Status did not change in time");
				return true;
			}
			DEBUG("Status changed to " + SPELLdataHelper::executorStatusToString(m_model.getStatus()));
			if (m_model.getStatus() == status) return false;
		}
	}
	return false;
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
SPELLexecutorStatus SPELLexecutor::getStatus()
{
	return m_model.getStatus();
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
bool SPELLexecutor::isActive()
{
	switch(getStatus())
	{
	case STATUS_ABORTED:
	case STATUS_FINISHED:
	case STATUS_ERROR:
		return false;
	default:
		break;
	}
	if (processOk())
	{
		DEBUG("Executor " + m_model.getInstanceId() + " active: " + SPELLdataHelper::executorStatusToString(getStatus()));
		return true;
	}
	return false;
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::command( const ExecutorCommand& command )
{
	if (processOk())
	{
		std::string commandId = command.id;
		LOG_INFO("Send command to executor: " + commandId );
		SPELLipcMessage cmd( commandId );
		cmd.setType( MSG_TYPE_ONEWAY );
		cmd.set( MessageField::FIELD_PROC_ID, m_model.getInstanceId() );
		//TODO command arguments
		sendMessageToExecutor( cmd );
	}
	else
	{
		LOG_ERROR("Cannot send command: executor process crashed");
	}
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::setControllingClient( SPELLclient* client )
{
	SPELLmonitor m(m_clientLock);
	LOG_INFO("Set controlling client: " + ISTR(client->getClientKey()));

	SPELLipcMessage addClient( MessageId::MSG_ID_ADD_CLIENT );
	addClient.setType( MSG_TYPE_ONEWAY );
	addClient.setSender( "CTX" );
	addClient.setReceiver( m_model.getInstanceId() );
	addClient.set( MessageField::FIELD_PROC_ID, m_model.getInstanceId() );
	addClient.set( MessageField::FIELD_GUI_CONTROL, ISTR(client->getClientKey()));
	addClient.set( MessageField::FIELD_GUI_CONTROL_HOST, client->getClientHost());
	sendMessageToExecutor(addClient);

	m_controllingClient = client;
	m_notAckNotifications = 0;
	m_clientConnection = CLI_CONNECTED;
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::removeControllingClient( bool clientLost )
{
	SPELLmonitor m(m_clientLock);
	LOG_INFO("Remove controlling client (lost: " + BSTR(clientLost) + ")");

	if (m_controllingClient)
	{
		m_clientConnection = clientLost ? CLI_ERROR : CLI_NO_CLIENT;

		int cKey = m_controllingClient->getClientKey();
		std::string cHost = m_controllingClient->getClientHost();

		DEBUG("Remove client from executor");
		SPELLipcMessage removeClient( MessageId::MSG_ID_REMOVE_CLIENT );
		removeClient.setType( MSG_TYPE_ONEWAY );
		removeClient.setSender( "CTX" );
		removeClient.setReceiver( m_model.getInstanceId() );
		removeClient.set( MessageField::FIELD_PROC_ID, m_model.getInstanceId() );
		removeClient.set( MessageField::FIELD_GUI_CONTROL, ISTR(cKey));
		removeClient.set( MessageField::FIELD_GUI_CONTROL_HOST, cHost);
		sendMessageToExecutor(removeClient);

		LOG_INFO("Controlling client removed");
		m_controllingClient = NULL;
		m_notAckNotifications = 0;
	}
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::forcePause()
{
	ExecutorCommand cmd;
	if (getStatus() == STATUS_WAITING)
	{
		cmd.id = CMD_INTERRUPT;
		LOG_WARN("Interrupting procedure (force stop)");
		command(cmd);
		bool timedout = waitStatus(STATUS_INTERRUPTED, 5*1000);
		if (timedout)
		{
			LOG_ERROR("Unable to interrupt procedure");
			return;
		}
		LOG_WARN("Procedure interrupted");
	}
	else if (getStatus() == STATUS_RUNNING)
	{
		cmd.id = CMD_PAUSE;
		LOG_WARN("Pausing procedure (force stop)");
		command(cmd);
		bool timedout = waitStatus(STATUS_PAUSED, 5*1000);
		if (timedout)
		{
			LOG_ERROR("Unable to pause procedure");
			return;
		}
		LOG_WARN("Procedure paused");
	}
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::setBackground()
{
	SPELLmonitor m(m_clientLock);
	LOG_WARN("Make procedure headless");

	int cKey = m_controllingClient->getClientKey();
	std::string cHost = m_controllingClient->getClientHost();

	SPELLipcMessage setBackground( MessageId::MSG_ID_BACKGROUND );
	setBackground.setType( MSG_TYPE_ONEWAY );
	setBackground.setSender( "CTX" );
	setBackground.setReceiver( m_model.getInstanceId() );
	setBackground.set( MessageField::FIELD_PROC_ID, m_model.getInstanceId() );
	setBackground.set( MessageField::FIELD_GUI_CONTROL, ISTR(cKey));
	setBackground.set( MessageField::FIELD_GUI_CONTROL_HOST, cHost);
	sendMessageToExecutor(setBackground);

	m_clientConnection = CLI_BACKGROUND;
	m_controllingClient = NULL;
	m_notAckNotifications = 0;

	DEBUG("Controlling client removed, procedure in background now");
	ExecutorCommand cmd;
	cmd.id = CMD_RUN;
	DEBUG("Running procedure in background");
	command(cmd);
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
bool SPELLexecutor::hasControllingClient()
{
	SPELLmonitor m(m_clientLock);
	return (m_controllingClient != NULL);
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
SPELLclient* SPELLexecutor::getControllingClient()
{
	SPELLmonitor m(m_clientLock);
	return m_controllingClient;
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::forwardMessageToClient( const SPELLipcMessage& msg )
{
	switch(m_clientConnection)
	{
	case CLI_ERROR:
		LOG_ERROR("Cannot forward message, controlling client error (" + msg.getId() + ")");
		LOG_ERROR("Msg: " + msg.dataStr());
		forcePause();
		break;

	case CLI_NO_CLIENT:
		LOG_WARN("Received message but no client is attached (" + msg.getId() + ")");
		LOG_ERROR("Msg: " + msg.dataStr());
		// Send acknowledge. We may want to do other stuff later on. We cannot just
		// force pause as the executor knows that the client was removed and will pause,
		// but it needs some notifications to be acknowledge either way in order to
		// finish a detach operation if it was done in RUNNING mode.
		if (msg.getType() == MSG_TYPE_NOTIFY)
		{
			m_notAckNotifications++;
			SPELLipcMessage ack = VOID_MESSAGE;
			ack.setSender("CTX");
			ack.setReceiver(m_model.getProcId());
			ack.setType(MSG_TYPE_ONEWAY);
			ack.setId(ExecutorMessages::ACKNOWLEDGE);
			ack.set(MessageField::FIELD_MSG_SEQUENCE,msg.get(MessageField::FIELD_MSG_SEQUENCE));
			sendMessageToExecutor(ack);

			if (m_notAckNotifications == 5)
			{
				forcePause();
				m_notAckNotifications = 0;
			}
		}
		else
		{
			forcePause();
		}
		break;

	case CLI_BACKGROUND:
		if (msg.getType() == MSG_TYPE_NOTIFY)
		{
			SPELLipcMessage ack = VOID_MESSAGE;
			ack.setSender("CTX");
			ack.setReceiver(m_model.getProcId());
			ack.setType(MSG_TYPE_ONEWAY);
			ack.setId(ExecutorMessages::ACKNOWLEDGE);
			ack.set(MessageField::FIELD_MSG_SEQUENCE,msg.get(MessageField::FIELD_MSG_SEQUENCE));
			sendMessageToExecutor(ack);
		}
		break;

	case CLI_CONNECTED:
		if (hasControllingClient())
		{
			m_controllingClient->sendMessageToClient(msg);
		}
		else
		{
			LOG_ERROR("Cannot forward message, no controlling client! " + msg.getId());
			LOG_ERROR("Msg: " + msg.dataStr());
			forcePause();
		}
		break;
	}
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
SPELLipcMessage SPELLexecutor::forwardRequestToClient( const SPELLipcMessage& msg )
{
	SPELLipcMessage resp = VOID_MESSAGE;

	switch(m_clientConnection)
	{
	case CLI_ERROR:
		LOG_ERROR("Cannot forward request, no controlling client! " + msg.getId());
		forcePause();
		resp = SPELLipcHelper::createErrorResponse( MessageId::MSG_PEER_LOST, msg );
		resp.set(MessageField::FIELD_ERROR, "Cannot forward " + msg.getId());
		resp.set(MessageField::FIELD_REASON, "No controlling client available");
		resp.set(MessageField::FIELD_FATAL, PythonConstants::False );
    	LOG_ERROR("Executor request end in peer error");
    	break;
	case CLI_CONNECTED:
		if (hasControllingClient())
		{
			resp = m_controllingClient->sendRequestToClient(msg);
		}
		else
		{
			LOG_ERROR("Cannot forward request, no controlling client! " + msg.getId());
			forcePause();
			resp = SPELLipcHelper::createErrorResponse( MessageId::MSG_PEER_LOST, msg );
			resp.set(MessageField::FIELD_ERROR, "Cannot forward " + msg.getId());
			resp.set(MessageField::FIELD_REASON, "No controlling client available");
			resp.set(MessageField::FIELD_FATAL, PythonConstants::False );
	    	LOG_ERROR("Executor request end in peer error");
		}
		break;
	case CLI_BACKGROUND:
	case CLI_NO_CLIENT:
		break;
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::executorStatusChanged( const SPELLexecutorStatus& status,
										   const std::string& error, const std::string& reason )
{
	LOG_INFO("Executor " + m_model.getInstanceId() + " status changed: " + SPELLdataHelper::executorStatusToString(status) );
	if (error != "") LOG_INFO("Error information: " + error + ":" + reason );
	m_model.setStatus(status);
	m_executorStatusEvent.set();

	SPELLexecutorOperation op;
	op.instanceId = m_model.getInstanceId();
	op.parentId = m_model.getParentInstanceId();
	op.originId = m_model.getOriginId();
	op.groupId = m_model.getGroupId();
	op.status = status;
	op.type = SPELLexecutorOperation::EXEC_OP_STATUS;

	SPELLcontext::instance().notifyExecutorOperation( op );
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::registerNotifier( std::string id, SPELLexecutorListener* listener )
{
	m_ipc.registerExecutorNotifier( id, listener );
	//TODO check if need to add client in exec (ipc msg)
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::deregisterNotifier( std::string id )
{
	m_ipc.deregisterExecutorNotifier( id );
	//TODO check if need to remove client in exec (ipc msg)
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
SPELLipcMessage SPELLexecutor::executorLogin( const SPELLipcMessage& msg )
{
	DEBUG("Received executor login: " + m_model.getInstanceId() );

	m_loggedIn = true;

	// Store the information given by the executor
	m_model.setIpcKey(msg.getKey());
	m_model.setStatus(SPELLdataHelper::stringToExecutorStatus( msg.get( MessageField::FIELD_EXEC_STATUS )));
	m_model.setAsRunFilename( msg.get( MessageField::FIELD_ASRUN_NAME ));
	m_model.setLogFilename( msg.get( MessageField::FIELD_LOG_NAME ));
	//TODO
	//m_model.wsFileName = msg.get( MessageField::FIELD_WS_NAME );

	// Create the response
	SPELLipcMessage response = SPELLipcHelper::createResponse( ExecutorMessages::RSP_NOTIF_EXEC_OPEN, msg );
	LOG_INFO("Executor login options: " + m_model.getInstanceId());
	LOG_INFO("    Connection: " + ISTR(m_clientConnection));
	response.set( MessageField::FIELD_ARGS, m_model.getArguments() );
	LOG_INFO("    Arguments: " + m_model.getArguments());
	std::string oMode = SPELLdataHelper::openModeToString(m_model.getOpenMode());
	response.set( MessageField::FIELD_OPEN_MODE, oMode );
	LOG_INFO("    Open mode: " + oMode);
	response.set( MessageField::FIELD_CONDITION, m_model.getCondition() );
	LOG_INFO("    Condition: " + m_model.getCondition());
	response.set( MessageField::FIELD_PARENT_PROC, m_model.getParentInstanceId() );\
	LOG_INFO("    Parent   : " + m_model.getParentInstanceId());
	response.set( MessageField::FIELD_GROUP_ID, m_model.getGroupId() );
	LOG_INFO("    Group    : " + m_model.getGroupId());
	response.set( MessageField::FIELD_ORIGIN_ID, m_model.getOriginId() );
	LOG_INFO("    Origin   : " + m_model.getOriginId());

	if (m_controllingClient)
	{
		response.set( MessageField::FIELD_GUI_CONTROL, ISTR(m_controllingClient->getClientKey()) );
		LOG_INFO("    Client   : " + ISTR(m_controllingClient->getClientKey()));
		response.set( MessageField::FIELD_GUI_CONTROL_HOST, m_controllingClient->getClientHost() );
	}
	else
	{
		if (m_clientConnection == CLI_BACKGROUND)
		{
			LOG_WARN("    Executor in background!");
			response.set( MessageField::FIELD_GUI_CONTROL, "<BACKGROUND>" );
			response.set( MessageField::FIELD_GUI_CONTROL_HOST, "" );
		}
		else
		{
			LOG_WARN("    No controlling client!");
		}
	}

	// Get the context configuration from the login parameters
	SPELLcontext::instance().fillExecutorDefaults(response);

    // The response will be deleted by IPC layer!
	if (m_reconnecting)
	{
		// Attach the process manager to the executor process
		LOG_INFO("Re-attaching to executor process with PID " + ISTR(m_model.getPID()));
		SPELLprocessManager::instance().attachProcess( m_model.getInstanceId(), m_model.getPID() );

		// Retrieve the procedure status
		SPELLipcMessage msg( ExecutorMessages::REQ_EXEC_STATUS );
		msg.setType( MSG_TYPE_REQUEST );
		msg.setSender("CTX");
		msg.setReceiver( m_model.getInstanceId() );
		SPELLipcMessage resp = sendRequestToExecutor( msg );
		if (!resp.isVoid() && resp.getType() != MSG_TYPE_ERROR )
		{
			std::string statusStr = resp.get( MessageField::FIELD_EXEC_STATUS );
			SPELLexecutorStatus st = SPELLdataHelper::stringToExecutorStatus( statusStr );
			if (st == STATUS_ERROR )
			{
				std::string error = resp.get( MessageField::FIELD_ERROR );
				std::string reason = resp.get( MessageField::FIELD_REASON );
				SPELLutils::trim(error);
				SPELLutils::trim(reason);
				if (error != "")
				{
					LOG_INFO("Executor status error information: " + error + ":" + reason );
				}
				executorStatusChanged( st, error, reason );
			}
			else
			{
				executorStatusChanged( st );
			}
		}
	}

	m_executorLoggedInEvent.set();

	DEBUG("Returning login response for " + m_model.getInstanceId() );
	return response;
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::executorNotification( const SPELLipcMessage& nmsg )
{
	SPELLipcMessage msg(nmsg);
	switch( msg.getType() )
	{
	case MSG_TYPE_NOTIFY_ASYNC:
	case MSG_TYPE_NOTIFY:
	{
		DEBUG("Received notification from " + m_model.getInstanceId() );
		std::string dataType = msg.get( MessageField::FIELD_DATA_TYPE );
		if (dataType == MessageValue::DATA_TYPE_STATUS)
		{
			std::string statusStr = msg.get( MessageField::FIELD_EXEC_STATUS );

			SPELLexecutorStatus st = SPELLdataHelper::stringToExecutorStatus( statusStr );
			if (st == STATUS_ERROR )
			{
				std::string error = msg.get( MessageField::FIELD_ERROR );
				std::string reason = msg.get( MessageField::FIELD_REASON );
				SPELLutils::trim(error);
				SPELLutils::trim(reason);
				if (error != "")
				{
					LOG_INFO("Executor status error information: " + error + ":" + reason );
				}
				executorStatusChanged( st, error, reason );
			}
			else
			{
				executorStatusChanged( st );
			}
		}
		else if (dataType == MessageValue::DATA_TYPE_LINE)
		{
			std::string id = msg.get( MessageField::FIELD_STAGE_ID );
			std::string tl = msg.get( MessageField::FIELD_STAGE_TL );

			getModel().setStack(msg.get( MessageField::FIELD_CSP ), msg.get( MessageField::FIELD_CODE_NAME ) );

			// Force an additional status notification upon line notifications where
			// the stage title changes
			if (getModel().getStageId() != id)
			{
				getModel().setStage(id,tl);
				SPELLexecutorOperation op;
				op.instanceId = m_model.getInstanceId();
				op.originId = m_model.getOriginId();
				op.groupId = m_model.getGroupId();
				op.stageId = id;
				op.stageTitle = tl;
				op.type = SPELLexecutorOperation::EXEC_OP_SUMMARY;
				SPELLcontext::instance().notifyExecutorOperation( op );
			}
		}
		else if (dataType == MessageValue::DATA_TYPE_CALL || dataType == MessageValue::DATA_TYPE_RETURN)
		{
			getModel().setStack(msg.get( MessageField::FIELD_CSP ), msg.get( MessageField::FIELD_CODE_NAME ) );
		}
		break;
	}
	default:
		LOG_ERROR("UNHANDLED EXECUTOR NOTIFICATION" + msg.dataStr());
		break;
	}
}
