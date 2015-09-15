// ################################################################################
// FILE       : SPELLcontext.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the context main class
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
#include "SPELL_CTX/SPELLcontext.H"
#include "SPELL_CTX/SPELLclientManager.H"
#include "SPELL_CTX/SPELLexecutorManager.H"
#include "SPELL_CTX/SPELLdataHelper.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_WRP/SPELLdriverManager.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_PRD/SPELLprocedureManager.H"
#include "SPELL_IPC/SPELLipcHelper.H"
#include "SPELL_IPC/SPELLipc_Executor.H"
#include "SPELL_IPC/SPELLipc_Context.H"
#include "SPELL_IPC/SPELLipc_Listener.H"
#include "SPELL_SDB/SPELLdatabaseFactory.H"
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////
#define NAME std::string("[CTX] ")

SPELLcontext* SPELLcontext::s_instance = NULL;

const std::string USE_DRIVER_TIME = "UseDriverTime";


//=============================================================================
// CONSTRUCTOR: SPELLcontext::SPELLcontext()
//=============================================================================
SPELLcontext::SPELLcontext()
: m_maxProcs(10),
  m_clientIPC(),
  m_listenerIPC(),
  m_executorDefaults(),
  m_sharedData()
{

}

//=============================================================================
// DESTRUCTOR: SPELLcontext::~SPELLcontext()
//=============================================================================
SPELLcontext::~SPELLcontext()
{
}

//=============================================================================
// STATIC: SPELLcontext::instance()
//=============================================================================
SPELLcontext& SPELLcontext::instance()
{
	if (s_instance == NULL)
	{
		s_instance = new SPELLcontext();
	}
	return *s_instance;
}

//=============================================================================
// METHOD: SPELLcontext::start()
//=============================================================================
void SPELLcontext::start( const SPELLcontextConfiguration& config )
{
	// Store the configuration
	m_config = config;

	// Setup the configuration
	SPELLconfiguration::instance().loadConfig(m_config.configFile);

    // Configure the time format if defined in configuration
    std::string format = SPELLconfiguration::instance().getCommonParameter("TdsTimeFormat");
    if (format.length()!=0)
    {
    	if (format.compare ("1") == 0)
    	{
    		SPELLutils::setTimeFormat(TIME_FORMAT_SLASH);
    	}
    	else if (format.compare("0")==0)
    	{
    		SPELLutils::setTimeFormat(TIME_FORMAT_DOT);
    	}
    }

	LOG_INFO("Starting context " + getContextName() );

	// Setting Context Executor Defaults
	LOG_INFO(" Setting Context Executor Defaults ");
	SPELLconfiguration& srvConfig = SPELLconfiguration::instance();

	//Set Server defaults
	m_executorDefaults.setRunInto( srvConfig.getExecutorParameter(ExecutorConstants::RunInto) == ExecutorConstants::TRUE_VALUE );
	m_executorDefaults.setExecDelay( STRI(srvConfig.getExecutorParameter(ExecutorConstants::ExecDelay)) );
	m_executorDefaults.setPromptWarningDelay( STRI(srvConfig.getExecutorParameter(ExecutorConstants::PromptDelay)) );
	m_executorDefaults.setByStep( srvConfig.getExecutorParameter(ExecutorConstants::ByStep) == ExecutorConstants::TRUE_VALUE );
	m_executorDefaults.setForceTcConfirm( srvConfig.getExecutorParameter(ExecutorConstants::ForceTcConfirm) == ExecutorConstants::TRUE_VALUE );
	m_executorDefaults.setSaveStateMode( srvConfig.getExecutorParameter(ExecutorConstants::SaveStateMode) );
	m_executorDefaults.setWatchVariables( srvConfig.getExecutorParameter(ExecutorConstants::WatchVariables) == ExecutorConstants::ENABLED );
	m_executorDefaults.setMaxVerbosity( STRI(srvConfig.getExecutorParameter(ExecutorConstants::MaxVerbosity)) );
	m_executorDefaults.setBrowsableLibStr( srvConfig.getExecutorParameter(ExecutorConstants::BrowsableLib) );

	//Override executor defaults with context parameters
	if( srvConfig.getContext(getContextName()).getExecutorParameter(ExecutorConstants::RunInto) != "" )
		m_executorDefaults.setRunInto( srvConfig.getContext(getContextName()).getExecutorParameter(ExecutorConstants::RunInto) == ExecutorConstants::TRUE_VALUE );
	if( srvConfig.getContext(getContextName()).getExecutorParameter(ExecutorConstants::ExecDelay) != "" )
		m_executorDefaults.setExecDelay( STRI(srvConfig.getContext(getContextName()).getExecutorParameter(ExecutorConstants::ExecDelay)) );
	if( srvConfig.getContext(getContextName()).getExecutorParameter(ExecutorConstants::PromptDelay) != "" )
		m_executorDefaults.setPromptWarningDelay( STRI(srvConfig.getContext(getContextName()).getExecutorParameter(ExecutorConstants::PromptDelay)) );
	if( srvConfig.getContext(getContextName()).getExecutorParameter(ExecutorConstants::ByStep) != "" )
		m_executorDefaults.setByStep( srvConfig.getContext(getContextName()).getExecutorParameter(ExecutorConstants::ByStep) == ExecutorConstants::TRUE_VALUE );
	if( srvConfig.getContext(getContextName()).getExecutorParameter(ExecutorConstants::ForceTcConfirm) != "" )
		m_executorDefaults.setForceTcConfirm( srvConfig.getContext(getContextName()).getExecutorParameter(ExecutorConstants::ForceTcConfirm) == ExecutorConstants::TRUE_VALUE );
	if( srvConfig.getContext(getContextName()).getExecutorParameter(ExecutorConstants::SaveStateMode) != "" )
		m_executorDefaults.setSaveStateMode( srvConfig.getContext(getContextName()).getExecutorParameter(ExecutorConstants::SaveStateMode) );
	if( srvConfig.getContext(getContextName()).getExecutorParameter(ExecutorConstants::SaveStateMode) != "" )
		m_executorDefaults.setWatchVariables( srvConfig.getContext(getContextName()).getExecutorParameter(ExecutorConstants::WatchVariables) == ExecutorConstants::ENABLED );
	if( srvConfig.getContext(getContextName()).getExecutorParameter(ExecutorConstants::MaxVerbosity) != "" )
		m_executorDefaults.setMaxVerbosity( STRI(srvConfig.getContext(getContextName()).getExecutorParameter(ExecutorConstants::MaxVerbosity)) );
	if( srvConfig.getContext(getContextName()).getExecutorParameter(ExecutorConstants::BrowsableLib) != "" )
		m_executorDefaults.setBrowsableLibStr( srvConfig.getContext(getContextName()).getExecutorParameter(ExecutorConstants::BrowsableLib) );

	LOG_INFO( "Execution Defaults: " + m_executorDefaults.toString() );

	// Preload the max. amount of active procedures
	std::string drvName = SPELLconfiguration::instance().getContext(m_config.contextName).getDriverName();
	SPELLdriverConfig& driverConfig = SPELLconfiguration::instance().getDriver(drvName);
	m_maxProcs = driverConfig.getMaxProcs();

	// Initialize Python support (some client requests require Python API)
    SPELLpythonHelper::instance().initialize();

	// Setup the procedure manager
	SPELLprocedureManager::instance().setup( getContextName() );

	// Setup the executor manager. This may take time to come back, if
	// there are executors to reconnect to. The listener login message will be sent after this stage.
	SPELLexecutorManager::instance().setup( getContextName() );

	if (SPELLconfiguration::instance().getContextParameter(USE_DRIVER_TIME) == "true")
	{
		// Setup the TIME driver for getCurrentTime (need to load Python config first)
		SPELLconfiguration::instance().loadPythonConfig(m_config.configFile);
		SPELLdriverManager::instance().setup(getContextName(), "TIME");
	}

	// Setup the IPC interfaces to clients and listener. The listener one logs in.
	m_clientIPC.setup();
	m_listenerIPC.setup();

	LOG_INFO("Context " + getContextName() + " ready" );

	// SPELLpythonHelper::initialize has acquired the GIL. Release it so
	// that operations in other threads (messages) can use it too.
	PyEval_ReleaseLock();
}

//=============================================================================
// METHOD: SPELLcontext::stop()
//=============================================================================
void SPELLcontext::stop()
{
	LOG_INFO("Stopping context " + getContextName() );
	SPELLclientManager::instance().removeAllClients();
	if (SPELLconfiguration::instance().getContextParameter(USE_DRIVER_TIME) == "true")
	{
		SPELLdriverManager::instance().cleanup(true);
	}
	SPELLexecutorManager::instance().cleanup();
	m_clientIPC.cleanup();
	SPELLexecutorManager::instance().killAll();
	m_listenerIPC.cleanup();
	// Cleanup Python API
	SPELLpythonHelper::instance().acquireGIL();
	SPELLpythonHelper::instance().finalize();
	LOG_INFO("Context " + getContextName() + " stopped" );
}

//=============================================================================
// METHOD: SPELLcontext::getNumActiveProcedures()
//=============================================================================
unsigned int SPELLcontext::getNumActiveProcedures()
{
	return SPELLexecutorManager::instance().getNumActiveExecutors();
}

//=============================================================================
// METHOD: SPELLcontext::readyToFinish()
//=============================================================================
void SPELLcontext::readyToFinish()
{
	LOG_INFO("Context ready to finish " + getContextName() );
	m_eventFinish.set();
}

//=============================================================================
// METHOD: SPELLcontext::waitFinish()
//=============================================================================
void SPELLcontext::waitFinish()
{
	m_eventFinish.clear();
	m_eventFinish.wait();
}

//=============================================================================
// METHOD: SPELLcontext::openExecutor
//=============================================================================
SPELLipcMessage SPELLcontext::openExecutor( const SPELLipcMessage& msg, SPELLclient* controllingClient )
{
	SPELLipcMessage resp = VOID_MESSAGE;

	LOG_INFO( "Requested opening new executor" );

	// Check maximum amount of procs per context (driver specific)
	if (m_maxProcs != 0 && m_maxProcs == getNumActiveProcedures())
	{
		LOG_ERROR(NAME + "Cannot open: maximum number of procedures reached");
	    resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_OPEN_EXEC, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot open executor");
		resp.set( MessageField::FIELD_REASON, "Maximum amount of active procedures reached (" + ISTR(m_maxProcs) + ")" );
		return resp;
	}

	// Get the information about procedure instance identifiers. Note that if the procedure
	// has been started by another one, there will be a parent instance identifier in the request message.
	//
	//   - The identifier of the child (subprocedure) will be in SPROC_ID in this case,
	//   - The identifier of the parent will be in the PROC_ID.
	std::string theInstanceId = "";
	std::string theParentInstanceId = "";
	// Note that the group id matches the instance id for standalone procedures, but it is
	// the id of the original main procedure for all the children down a dependency tree.
	std::string theGroupId = "";
	
	// The following condition is for the case when we have a subprocedure being started.
	int callingLine = 0;
	if (msg.hasField( MessageField::FIELD_SPROC_ID ))
	{
		theInstanceId = msg.get( MessageField::FIELD_SPROC_ID );
		theParentInstanceId = msg.get( MessageField::FIELD_PROC_ID );
	}
	else
	{
		theInstanceId = msg.get( MessageField::FIELD_PROC_ID );
	}

	// The group id will be copied from the request if exists
	if (msg.hasField( MessageField::FIELD_GROUP_ID ))
	{
		theGroupId = msg.get( MessageField::FIELD_GROUP_ID );
	}

	// For subprocedures, there will be a calling procedure line number.
	if (msg.hasField( MessageField::FIELD_PARENT_PROC_LINE ))
	{
		callingLine = STRI( msg.get(MessageField::FIELD_PARENT_PROC_LINE ));
	}

	// Origin ID is just informative for SPELL and it is optional
	std::string theOriginId = "GUI";
	if (msg.hasField( MessageField::FIELD_ORIGIN_ID ))
	{
		theOriginId = msg.get( MessageField::FIELD_ORIGIN_ID );
	}

	SPELLexecutorStartupParams config(theInstanceId, SPELLutils::fileTimestamp() );

	config.setArguments(msg.get( MessageField::FIELD_ARGS ));
	config.setCondition(msg.get( MessageField::FIELD_CONDITION ));
	std::string cMode = msg.get( MessageField::FIELD_GUI_MODE );
	config.setClientMode(SPELLdataHelper::clientModeFromString(cMode));
	std::string oMode = msg.get( MessageField::FIELD_OPEN_MODE );
	config.setOpenMode(SPELLdataHelper::openModeFromString(oMode));
	config.setParentInstanceId(theParentInstanceId);
	config.setParentCallingLine(callingLine);
	config.setGroupId( theGroupId );
	config.setOriginId( theOriginId );

	LOG_INFO("		- Instance Id : " + config.getInstanceId());
	LOG_INFO("		- Client mode : " + cMode);
	LOG_INFO("		- Group Id    : " + config.getGroupId());
	LOG_INFO("		- Origin Id   : " + config.getOriginId());
	LOG_INFO("		- Calling line: " + ISTR(config.getParentCallingLine()) );
	LOG_INFO("		- Time Id     : " + config.getTimeId());
	LOG_INFO("		- Proc Id     : " + config.getProcId());
	LOG_INFO("		- Instance num: " + ISTR(config.getInstanceNum()));
	LOG_INFO("		- Arguments   : " + config.getArguments());
	LOG_INFO("		- Condition   : " + config.getCondition());
	LOG_INFO("		- Open mode   : " + oMode);
	LOG_INFO("		- Parent      : " + config.getParentInstanceId());

	try
	{
		SPELLexecutorManager::instance().startExecutor( config, controllingClient );
		resp = SPELLipcHelper::createResponse( ContextMessages::RSP_OPEN_EXEC, msg );

		SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor(config.getInstanceId());
		SPELLexecutorStatus initialStatus = exec->getStatus();

		int clientKey = -1;
		if (controllingClient && ( config.getClientMode() != CLIENT_MODE_BACKGROUND ))
		{
			DEBUG("Set executor controller: " + ISTR(controllingClient->getClientKey()));
			SPELLclientManager::instance().setExecutorController( controllingClient, exec );
			clientKey = controllingClient->getClientKey();
		}
		else if ( config.getClientMode() != CLIENT_MODE_BACKGROUND )
		{
			DEBUG("Set unknown client mode");
			config.setClientMode(CLIENT_MODE_UNKNOWN);
		}
		else
		{
			DEBUG("Set background client mode");
		}
		// Notify other clients
		SPELLexecutorOperation op;
		op.instanceId = config.getInstanceId();
		op.parentId = theParentInstanceId;
		op.groupId = config.getGroupId();
		op.originId = config.getOriginId();
		op.clientKey = clientKey;
		op.clientMode = config.getClientMode();
		op.status = initialStatus;
		op.condition = config.getCondition();
		op.type = SPELLexecutorOperation::EXEC_OP_OPEN;
		notifyExecutorOperation( op );

	}
	catch( SPELLcoreException& err )
	{
	    resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_OPEN_EXEC, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot open executor");
		resp.set( MessageField::FIELD_REASON, err.what() );
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::closeExecutor( const SPELLipcMessage& msg )
{
	std::string instanceId = msg.get( MessageField::FIELD_PROC_ID );
	LOG_INFO( NAME + "Requested closing executor " + instanceId );
	SPELLipcMessage resp = VOID_MESSAGE;
	try
	{
		// Check if there is a parent to notify
		SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor( instanceId );
		std::string parentInstanceId = exec->getParentInstanceId();
		std::string groupId = exec->getModel().getGroupId();
		std::string originId = exec->getModel().getOriginId();

		DEBUG( NAME + "Removing monitoring clients");
		// Unsubscribe any monitoring client
		std::list<int> mclients = SPELLclientManager::instance().getMonitoringClientsKeys( instanceId );
		std::list<int>::iterator it;
		for( it = mclients.begin(); it != mclients.end(); it++ )
		{
			SPELLclient* client = SPELLclientManager::instance().getClient(*it);
			SPELLclientManager::instance().stopMonitorExecutor( client, exec );
		}

		DEBUG( NAME + "Removing controlling client");
		SPELLclient* client = exec->getControllingClient();
		if (client)
		{
			SPELLclientManager::instance().removeExecutorController( client, exec, true, false );
		}

		DEBUG( NAME + "Closing executor");
		SPELLexecutorManager::instance().closeExecutor( instanceId );

		resp = SPELLipcHelper::createResponse( ContextMessages::RSP_CLOSE_EXEC, msg );

		DEBUG( NAME + "Notifying operation");
		// Notify other clients
		SPELLexecutorOperation op;
		op.instanceId = instanceId;
		op.parentId = parentInstanceId;
		op.groupId = groupId;
		op.originId = originId;
		op.clientKey = msg.getKey();
		op.type = SPELLexecutorOperation::EXEC_OP_CLOSE;
		notifyExecutorOperation( op );
	}
	catch( SPELLcoreException& err )
	{
	  	resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_CLOSE_EXEC, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot close executor");
		resp.set( MessageField::FIELD_REASON, err.what() );
	}
	DEBUG( NAME + "Request to close executor finished");
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::killExecutor( const SPELLipcMessage& msg )
{
	std::string instanceId = msg.get( MessageField::FIELD_PROC_ID );
	DEBUG( NAME + "Requested killing executor " + instanceId );
	SPELLipcMessage resp = VOID_MESSAGE;
	try
	{
		// Check if there is a parent to notify first
		SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor( instanceId );
		std::string parentInstanceId = exec->getParentInstanceId();
		std::string groupId = exec->getModel().getGroupId();
		std::string originId = exec->getModel().getOriginId();

		// Unsubscribe any monitoring client
		std::list<int> mclients = SPELLclientManager::instance().getMonitoringClientsKeys( instanceId );
		std::list<int>::iterator it;
		for( it = mclients.begin(); it != mclients.end(); it++ )
		{
			SPELLclient* client = SPELLclientManager::instance().getClient(*it);
			SPELLclientManager::instance().stopMonitorExecutor( client, exec );
		}

		SPELLclient* client = exec->getControllingClient();
		if (client)
		{
			SPELLclientManager::instance().removeExecutorController( client, exec, false, false );
		}

		SPELLexecutorManager::instance().killExecutor( instanceId );

		resp = SPELLipcHelper::createResponse( ContextMessages::RSP_KILL_EXEC, msg );

		// Notify other clients
		SPELLexecutorOperation op;
		op.instanceId = instanceId;
		op.parentId = parentInstanceId;
		op.groupId = groupId;
		op.originId = originId;
		op.clientKey = msg.getKey();
		op.type = SPELLexecutorOperation::EXEC_OP_KILL;
		notifyExecutorOperation(op);
	}
	catch( SPELLcoreException& err )
	{
	  	resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_KILL_EXEC, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot kill to executor");
		resp.set( MessageField::FIELD_REASON, "Cannot find executor " + instanceId );
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::recoverExecutor
//=============================================================================
SPELLipcMessage SPELLcontext::recoverExecutor( const SPELLipcMessage& msg, SPELLclient* controllingClient )
{
	SPELLipcMessage resp = VOID_MESSAGE;

	DEBUG( NAME + "Requested recovering executor");

	// Check maximum amount of procs per context (driver specific)
	if (m_maxProcs != 0 && m_maxProcs == getNumActiveProcedures())
	{
	    resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_RECOVER_EXEC, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot recover executor");
		resp.set( MessageField::FIELD_REASON, "Maximum amount of active procedures reached (" + ISTR(m_maxProcs) + ")" );
		return resp;
	}

	SPELLexecutorStartupParams config(msg.get( MessageField::FIELD_PROC_ID ), SPELLutils::fileTimestamp());

	config.setClientMode(CLIENT_MODE_CONTROL);
	config.setOpenMode((SPELLopenMode)(OPEN_MODE_VISIBLE | OPEN_MODE_BLOCKING));
	config.setRecoveryFile(msg.get( MessageField::FIELD_FILE_NAME ));

	LOG_INFO("RECOVER FILE " + config.getRecoveryFile() );

	DEBUG("		- Instance Id : " + config.getInstanceId());
	DEBUG("		- Time Id     : " + config.getTimeId());
	DEBUG("		- Proc Id     : " + config.getProcId());
	DEBUG("		- Instance num: " + ISTR(config.getInstanceNum()));
	DEBUG("		- Arguments   : " + config.getArguments());
	DEBUG("		- Condition   : " + config.getCondition());
	DEBUG("		- Client mode : " + SPELLdataHelper::clientModeToString(config.getClientMode()));
	DEBUG("		- Open mode   : " + SPELLdataHelper::openModeToString(config.getOpenMode()));
	DEBUG("		- Parent      : " + config.getParentInstanceId());

	try
	{
		checkRecoveryFiles( config.getRecoveryFile() );

		SPELLexecutorManager::instance().recoverExecutor( config, controllingClient );
		resp = SPELLipcHelper::createResponse( ContextMessages::RSP_RECOVER_EXEC, msg );

		SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor(config.getInstanceId());
		SPELLexecutorStatus initialStatus = exec->getStatus();

		int clientKey = controllingClient->getClientKey();
		SPELLclientManager::instance().setExecutorController( controllingClient, exec );

		// Notify other clients
		SPELLexecutorOperation op;
		op.instanceId = config.getInstanceId();
		op.parentId = config.getParentInstanceId();
		op.groupId = config.getGroupId();
		op.originId = config.getOriginId();
		op.clientKey = clientKey;
		op.clientMode = config.getClientMode();
		op.status = initialStatus;
		op.condition = config.getCondition();
		op.type = SPELLexecutorOperation::EXEC_OP_OPEN;
		notifyExecutorOperation( op );
	}
	catch( SPELLcoreException& err )
	{
	    resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_RECOVER_EXEC, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot recover executor");
		resp.set( MessageField::FIELD_REASON, err.what() );
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
void SPELLcontext::checkRecoveryFiles( const std::string& filename )
{
	SPELLcontextConfig& ctxConfig = SPELLconfiguration::instance().getContext(m_config.contextName);

	std::string wsDataDir = SPELLutils::getSPELL_DATA() + PATH_SEPARATOR + ctxConfig.getLocationPath("ws");
	std::string arDataDir = SPELLutils::getSPELL_DATA() + PATH_SEPARATOR + ctxConfig.getLocationPath("ar");

	bool wsdFound = false;
	bool wssFound = false;
	bool wspFound = false;
	bool arfFound = false;

	std::list<std::string> files = SPELLutils::getFilesInDir(wsDataDir);
	for(std::list<std::string>::const_iterator it = files.begin(); it != files.end(); it++)
	{
		std::string completeFilename = *it;
		std::string path = wsDataDir + PATH_SEPARATOR + completeFilename;
		if (completeFilename.find(filename) != std::string::npos)
		{
			if (!wssFound && + (completeFilename.find(".wss") != std::string::npos))
			{
				wssFound = true;
				if (SPELLutils::fileSize(path)==0)
				{
					THROW_EXCEPTION("Recovery file check failed", "WSS data file has zero bytes", SPELL_ERROR_WSTART);
				}
			}
			else if (!wsdFound && + (completeFilename.find(".wsd") != std::string::npos))
			{
				wsdFound = true;
				if (SPELLutils::fileSize(path)==0)
				{
					THROW_EXCEPTION("Recovery file check failed", "WSD data file has zero bytes", SPELL_ERROR_WSTART);
				}
			}
			if (!wspFound && + (completeFilename.find(".wsp") != std::string::npos))
			{
				wspFound = true;
				if (SPELLutils::fileSize(path)==0)
				{
					THROW_EXCEPTION("Recovery file check failed", "WSP data file has zero bytes", SPELL_ERROR_WSTART);
				}
			}
		}
	}
	if (!wssFound || !wsdFound || !wspFound)
	{
		THROW_EXCEPTION("Recovery file check failed", "Warmstart files missing", SPELL_ERROR_WSTART);
	}

	files = SPELLutils::getFilesInDir(arDataDir);
	for(std::list<std::string>::const_iterator it = files.begin(); it != files.end(); it++)
	{
		std::string completeFilename = *it;
		std::string path = arDataDir + PATH_SEPARATOR + completeFilename;
		if (completeFilename == filename + ".ASRUN")
		{
			arfFound = true;
			if (SPELLutils::fileSize(path)==0)
			{
				THROW_EXCEPTION("Recovery file check failed", "ASRUN file has zero bytes: '" + completeFilename + "'", SPELL_ERROR_WSTART);
			}
			break;
		}
	}
	if (!arfFound)
	{
		THROW_EXCEPTION("Recovery file check failed", "ASRUN file missing: '" + arDataDir + PATH_SEPARATOR + filename + ".ASRUN'", SPELL_ERROR_WSTART);
	}
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getProcedureList( const SPELLipcMessage& msg )
{
	DEBUG( NAME + "Requested list of procedures");
	SPELLipcMessage resp = VOID_MESSAGE;

	SPELLsafePythonOperations ops("getProcedureList");

	if (msg.hasField(MessageField::FIELD_REFRESH))
	{
		std::string refresh = msg.get(MessageField::FIELD_REFRESH);
		if (refresh == MessageValue::DATA_TRUE)
		{
			SPELLprocedureManager::instance().refresh();
		}
	}
	SPELLprocedureManager::ProcList list = SPELLprocedureManager::instance().getProcList();

	std::string listStr = "";
	SPELLprocedureManager::ProcList::const_iterator it;
	for( it = list.begin(); it != list.end(); it++)
	{
		if (listStr != "") listStr += LIST_SEPARATOR;
		listStr += *it;
	}
	resp = SPELLipcHelper::createResponse( ContextMessages::RSP_PROC_LIST, msg );
	resp.set( MessageField::FIELD_PROC_LIST, listStr );
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getProcedureRecoveryList( const SPELLipcMessage& msg )
{
	DEBUG( NAME + "Requested list of procedure recovery files");
	SPELLipcMessage resp = VOID_MESSAGE;

	SPELLcontextConfig& ctxConfig = SPELLconfiguration::instance().getContext(m_config.contextName);
	std::string dataDir = SPELLutils::getSPELL_DATA() + PATH_SEPARATOR + ctxConfig.getLocationPath("ws");

	std::list<std::string> files = SPELLutils::getFilesInDir(dataDir);
	std::list<std::string> wsfiles;
	for(std::list<std::string>::const_iterator it = files.begin(); it != files.end(); it++)
	{
		std::string filename = *it;
		if (filename.find(".wsp") == (filename.length()-4) )
		{
			wsfiles.push_back(filename);
		}
	}

	std::string listStr = "";
	for(std::list<std::string>::const_iterator it = wsfiles.begin(); it != wsfiles.end(); it++)
	{
		if (listStr != "") listStr += LIST_SEPARATOR;
		listStr += *it;
	}
	resp = SPELLipcHelper::createResponse( ContextMessages::RSP_RECOVERY_LIST, msg );
	resp.set( MessageField::FIELD_FILE_LIST, listStr );
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getProcedureAsRunList( const SPELLipcMessage& msg )
{
	DEBUG( NAME + "Requested list of procedure ASRUN files");
	SPELLipcMessage resp = VOID_MESSAGE;

	SPELLcontextConfig& ctxConfig = SPELLconfiguration::instance().getContext(m_config.contextName);
	std::string dataDir = SPELLutils::getSPELL_DATA() + PATH_SEPARATOR + ctxConfig.getLocationPath("ar");

	std::list<std::string> files = SPELLutils::getFilesInDir(dataDir);
	std::list<std::string> wsfiles;
	for(std::list<std::string>::const_iterator it = files.begin(); it != files.end(); it++)
	{
		std::string filename = *it;
		if (filename.find(".ASRUN") != std::string::npos )
		{
			wsfiles.push_back(filename);
		}
	}

	std::string listStr = "";
	for(std::list<std::string>::const_iterator it = wsfiles.begin(); it != wsfiles.end(); it++)
	{
		if (listStr != "") listStr += LIST_SEPARATOR;
		listStr += *it;
	}
	resp = SPELLipcHelper::createResponse( ContextMessages::RSP_ASRUN_LIST, msg );
	resp.set( MessageField::FIELD_FILE_LIST, listStr );
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::listFiles( const SPELLipcMessage& msg )
{
	SPELLipcMessage resp = VOID_MESSAGE;

	std::string dir = msg.get(MessageField::FIELD_DIR_NAME);

	DEBUG( NAME + "Requested list of files in directory " + dir);

	std::string fileList = "";
	if (SPELLutils::pathExists(dir))
	{
		std::list<std::string> files = SPELLutils::getFilesInDir(dir);
		std::list<std::string>::iterator it;
		for( it = files.begin(); it != files.end(); it++ )
		{
			if (fileList.size()>0) fileList += LIST_SEPARATOR;
			fileList += *it;
		}
		files = SPELLutils::getSubdirs(dir);
		for( it = files.begin(); it != files.end(); it++ )
		{
			if (fileList.size()>0) fileList += LIST_SEPARATOR;
			fileList += "+" + *it;
		}
	}
	else
	{
		LOG_ERROR("Path does not exist, cannot list files: '" + dir + "'");
	}
	resp = SPELLipcHelper::createResponse( ContextMessages::RSP_LIST_FILES, msg );
	resp.set( MessageField::FIELD_FILE_LIST, fileList );
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::listDataDirectories( const SPELLipcMessage& msg )
{
	DEBUG( NAME + "Requested list of data directories");
	SPELLipcMessage resp = VOID_MESSAGE;

	SPELLcontextConfig& ctxConfig = SPELLconfiguration::instance().getContext(m_config.contextName);
	std::list<std::string> dataLocations = ctxConfig.getLocations();

	std::list<std::string>::iterator it;
	std::string dirList = "";
	// To avoid duplicated location paths (may happen in config)
	std::list<std::string> alreadyProcessed;
	for(it = dataLocations.begin(); it != dataLocations.end(); it++)
	{
		std::string locationPath = ctxConfig.getLocationPath(*it);
		if ( std::find( alreadyProcessed.begin(), alreadyProcessed.end(), locationPath ) == alreadyProcessed.end() )
		{
			if (dirList.size()>0) dirList += LIST_SEPARATOR;
			dirList += SPELLutils::getSPELL_DATA() + PATH_SEPARATOR + locationPath;
			alreadyProcessed.push_back(locationPath);
			DEBUG( "    - " + SPELLutils::getSPELL_DATA() + PATH_SEPARATOR + locationPath);
		}
	}

	resp = SPELLipcHelper::createResponse( ContextMessages::RSP_LIST_DATADIRS, msg );
	resp.set( MessageField::FIELD_FILE_LIST, dirList );
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::deleteRecoveryFiles()
//=============================================================================
SPELLipcMessage SPELLcontext::deleteRecoveryFiles( const SPELLipcMessage& msg )
{
	DEBUG( NAME + "Requested delete recovery files");
	SPELLipcMessage resp = VOID_MESSAGE;

	SPELLcontextConfig& ctxConfig = SPELLconfiguration::instance().getContext(m_config.contextName);

	std::string fileName = msg.get(MessageField::FIELD_FILE_NAME);
	fileName = fileName.substr(0,fileName.length()-4);

	// Delete warmstart files
	std::string wsDataDir = SPELLutils::getSPELL_DATA() + PATH_SEPARATOR + ctxConfig.getLocationPath("ws");
	std::list<std::string> files = SPELLutils::getFilesInDir(wsDataDir);

	for(std::list<std::string>::const_iterator it = files.begin(); it != files.end(); it++)
	{
		std::string completeFilename = *it;
		if (completeFilename.find(fileName) != std::string::npos)
		{
			std::string path = wsDataDir + PATH_SEPARATOR + completeFilename;
			if (SPELLutils::pathExists(path))
			{
				SPELLutils::deleteFile(path);
			}
		}
	}

	// Delete Asrun files
	std::string arDataDir = SPELLutils::getSPELL_DATA() + PATH_SEPARATOR + ctxConfig.getLocationPath("ar");

	files = SPELLutils::getFilesInDir(arDataDir);

	for(std::list<std::string>::const_iterator it = files.begin(); it != files.end(); it++)
	{
		std::string completeFilename = *it;
		if (completeFilename.find(fileName) != std::string::npos)
		{
			std::string path = arDataDir + PATH_SEPARATOR + completeFilename;
			if (SPELLutils::pathExists(path))
			{
				SPELLutils::deleteFile(path);
			}
		}
	}

	resp = SPELLipcHelper::createResponse( ContextMessages::RSP_DELETE_RECOVERY, msg );
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getExecutorList( const SPELLipcMessage& msg )
{
	DEBUG( NAME + "Requested list of executors");
	SPELLipcMessage resp = VOID_MESSAGE;
	SPELLexecutorManager::ExecList list = SPELLexecutorManager::instance().getExecutorList();
	std::string listStr = "";
	SPELLexecutorManager::ExecList::const_iterator it;
	for( it = list.begin(); it != list.end(); it++)
	{
		if (listStr != "") listStr += LIST_SEPARATOR;
		DEBUG("    found " + *it);
		listStr += *it;
	}
	resp = SPELLipcHelper::createResponse( ContextMessages::RSP_EXEC_LIST, msg );
	resp.set( MessageField::FIELD_EXEC_LIST, listStr );
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getProcedureProperties( const SPELLipcMessage& msg )
{
	std::string procId = msg.get( MessageField::FIELD_PROC_ID );
	DEBUG( NAME + "Requested procedure properties for " + procId);
	SPELLipcMessage resp = VOID_MESSAGE;
	resp = SPELLipcHelper::createResponse( ContextMessages::RSP_PROC_PROP, msg );
	SPELLprocedure::PropertyKeys keys = SPELLprocedureManager::instance().getPropertyKeys( procId );
	SPELLprocedure::PropertyKeys::iterator it;
	for( it = keys.begin(); it != keys.end(); it++)
	{
		std::string key = *it;
		const std::string value = SPELLprocedureManager::instance().getProperty( procId, key );
		key = SPELLutils::toLower(key);
		resp.set( key, value );
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getExecutorInfo( const SPELLipcMessage& msg )
{
	std::string instanceId = msg.get( MessageField::FIELD_PROC_ID );
	SPELLipcMessage resp = SPELLipcHelper::createResponse( ContextMessages::RSP_EXEC_INFO, msg);
	DEBUG( NAME + "Requested executor information: " + instanceId);
	SPELLexecutorManager::instance().buildExecutorInfo( instanceId, resp );
	SPELLclientManager::instance().completeMonitoringInfo( instanceId, resp );
	return resp;
}


//=============================================================================
// METHOD: SPELLcontext::getExecutorDefaults
//=============================================================================
SPELLipcMessage SPELLcontext::getExecutorDefaults( const SPELLipcMessage& msg )
{
	SPELLipcMessage resp = SPELLipcHelper::createResponse( ContextMessages::RSP_GET_CTX_EXEC_DFLT, msg);
	DEBUG( NAME + "Requested executor defaults ");

	fillExecutorDefaults(resp);

	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::setExecutorDefaults
//=============================================================================
SPELLipcMessage SPELLcontext::setExecutorDefaults( const SPELLipcMessage& msg )
{
	//std::string instanceId = msg.get( MessageField::FIELD_PROC_ID );
	SPELLipcMessage resp = SPELLipcHelper::createResponse( ContextMessages::RSP_SET_CTX_EXEC_DFLT, msg);
	DEBUG( NAME + "Updating context executor defaults from GUI");

	LOG_INFO("Update executor defaults");
	//Set only set fields that come within the message
	if( msg.hasField(MessageField::FIELD_RUN_INTO) )
	{
		LOG_INFO("   - Run into     : " + msg.get(MessageField::FIELD_RUN_INTO) );
		m_executorDefaults.setRunInto( msg.get(MessageField::FIELD_RUN_INTO) == ExecutorConstants::TRUE_VALUE );
	}

	if( msg.hasField(MessageField::FIELD_EXEC_DELAY) )
	{
		LOG_INFO("   - Exec delay   : " + msg.get(MessageField::FIELD_EXEC_DELAY) );
		m_executorDefaults.setExecDelay( STRI(msg.get(MessageField::FIELD_EXEC_DELAY)) );
	}

	if( msg.hasField(MessageField::FIELD_PROMPT_DELAY) )
	{
		LOG_INFO("   - Prompt delay : " + msg.get(MessageField::FIELD_PROMPT_DELAY) );
		m_executorDefaults.setPromptWarningDelay( STRI(msg.get(MessageField::FIELD_PROMPT_DELAY)) );
	}

	if( msg.hasField(MessageField::FIELD_BY_STEP) )
	{
		LOG_INFO("   - By step      : " + msg.get(MessageField::FIELD_BY_STEP) );
		m_executorDefaults.setByStep( msg.get(MessageField::FIELD_BY_STEP) == ExecutorConstants::TRUE_VALUE );
	}

	if( msg.hasField(MessageField::FIELD_FORCE_TC_CONFIRM) )
	{
		LOG_INFO("   - TC confirm   : " + msg.get(MessageField::FIELD_FORCE_TC_CONFIRM) );
		m_executorDefaults.setForceTcConfirm( msg.get(MessageField::FIELD_FORCE_TC_CONFIRM) == ExecutorConstants::TRUE_VALUE );
	}

	if( msg.hasField(MessageField::FIELD_SAVE_STATE_MODE) )
	{
		LOG_INFO("   - Save state   : " + msg.get(MessageField::FIELD_SAVE_STATE_MODE) );
		m_executorDefaults.setSaveStateMode( msg.get(MessageField::FIELD_SAVE_STATE_MODE) );
	}

	if( msg.hasField(MessageField::FIELD_WATCH_VARIABLES) )
	{
		LOG_INFO("   - Watch vars   : " + msg.get(MessageField::FIELD_WATCH_VARIABLES) );
		m_executorDefaults.setWatchVariables( msg.get(MessageField::FIELD_WATCH_VARIABLES) == ExecutorConstants::TRUE_VALUE );
	}

	if( msg.hasField(MessageField::FIELD_MAX_VERBOSITY) )
	{
		LOG_INFO("   - Verbosity    : " + msg.get(MessageField::FIELD_MAX_VERBOSITY) );
		m_executorDefaults.setMaxVerbosity( STRI(msg.get(MessageField::FIELD_MAX_VERBOSITY)) );
	}

	if( msg.hasField(MessageField::FIELD_BROWSABLE_LIB) )
	{
		LOG_INFO("   - Browsable lib: " + msg.get(MessageField::FIELD_BROWSABLE_LIB) );
		m_executorDefaults.setBrowsableLibStr( msg.get(MessageField::FIELD_BROWSABLE_LIB) );
	}

	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
void SPELLcontext::fillExecutorDefaults( SPELLipcMessage& msg )
{
	DEBUG( NAME + "Filling SPELLipcMessage with executor Defaults ");

	msg.set( MessageField::FIELD_RUN_INTO, (m_executorDefaults.isRunInto() ? ExecutorConstants::TRUE_VALUE : ExecutorConstants::FALSE_VALUE) );
	msg.set( MessageField::FIELD_EXEC_DELAY, ISTR( m_executorDefaults.getExecDelay() ) );
	msg.set( MessageField::FIELD_PROMPT_DELAY, ISTR(m_executorDefaults.getPromptWarningDelay()) );
	msg.set( MessageField::FIELD_BY_STEP, (m_executorDefaults.isByStep() ? ExecutorConstants::TRUE_VALUE : ExecutorConstants::FALSE_VALUE) );
	msg.set( MessageField::FIELD_FORCE_TC_CONFIRM, (m_executorDefaults.isForceTcConfirm() ? ExecutorConstants::TRUE_VALUE : ExecutorConstants::FALSE_VALUE) );
	msg.set( MessageField::FIELD_SAVE_STATE_MODE, m_executorDefaults.getSaveStateMode() );
	msg.set( MessageField::FIELD_WATCH_VARIABLES, (m_executorDefaults.isWatchVariables() ? ExecutorConstants::TRUE_VALUE : ExecutorConstants::FALSE_VALUE) );
	msg.set( MessageField::FIELD_MAX_VERBOSITY, ISTR(m_executorDefaults.getMaxVerbosity()) );
	msg.set( MessageField::FIELD_BROWSABLE_LIB, m_executorDefaults.getBrowsableLibStr() );

} //fillExecutorDefaults

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::attachExecutor( const SPELLipcMessage& msg, SPELLclient* client )
{
	SPELLipcMessage resp = VOID_MESSAGE;

	std::string instanceId = msg.get( MessageField::FIELD_PROC_ID );
	std::string clientModeStr = msg.get( MessageField::FIELD_GUI_MODE );
	DEBUG( NAME + "Requested attaching to executor: " + instanceId + " in mode " + clientModeStr );

	SPELLclientMode mode = SPELLdataHelper::clientModeFromString( clientModeStr );

	try
	{
		SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor(instanceId);
		std::string groupId = exec->getModel().getGroupId();
		std::string originId = exec->getModel().getOriginId();

		if (mode == CLIENT_MODE_CONTROL)
		{
			if (exec->hasControllingClient())
			{
				LOG_ERROR("Cannot attach, already controlled: " + instanceId );
				resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_ATTACH_EXEC, msg );
				resp.set( MessageField::FIELD_ERROR, "Cannot attach to executor in controlling mode");
				resp.set( MessageField::FIELD_REASON, "Executor is already controlled" );
				resp.set( MessageField::FIELD_FATAL, PythonConstants::True );
			}
			else
			{
				// Set the procedure controlling client on the client model
				client->addProcedure(instanceId, CLIENT_MODE_CONTROL);
				// Set the procedure controlling client on the executor model
				LOG_INFO("Client " + ISTR(client->getClientKey()) + " controlling executor " + instanceId);
				exec->setControllingClient( client );
				resp = SPELLipcHelper::createResponse( ContextMessages::RSP_ATTACH_EXEC, msg );
				// Add the executor information
				SPELLexecutorManager::instance().buildExecutorInfo( instanceId, resp );
				// Add the GUI list
				SPELLclientManager::instance().completeMonitoringInfo( instanceId, resp );
				// Notify other clients
				SPELLexecutorOperation op;
				op.instanceId = instanceId;
				op.parentId = exec->getParentInstanceId();
				op.groupId = groupId;
				op.originId = originId;
				op.clientKey = msg.getKey();
				op.clientMode = CLIENT_MODE_CONTROL;
				op.status = exec->getStatus();
				op.type = SPELLexecutorOperation::EXEC_OP_ATTACH;
				notifyExecutorOperation( op );
			}
		}
		else
		{
			// Set the procedure controlling client on the client model
			client->addProcedure(instanceId, CLIENT_MODE_MONITOR);

			LOG_INFO("Client " + ISTR(client->getClientKey()) + " monitoring executor " + instanceId);

			resp = SPELLipcHelper::createResponse( ContextMessages::RSP_ATTACH_EXEC, msg );
			// Add the executor information
			SPELLexecutorManager::instance().buildExecutorInfo( instanceId, resp );
			// Add the GUI list
			SPELLclientManager::instance().completeMonitoringInfo( instanceId, resp );
			// Monitor the executor
			SPELLclientManager::instance().startMonitorExecutor( client, exec );
			// Notify other clients
			SPELLexecutorOperation op;
			op.instanceId = instanceId;
			op.parentId = exec->getParentInstanceId();
			op.groupId = groupId;
			op.originId = originId;
			op.clientKey = msg.getKey();
			op.clientMode = CLIENT_MODE_MONITOR;
			op.status = exec->getStatus();
			op.type = SPELLexecutorOperation::EXEC_OP_ATTACH;
			notifyExecutorOperation( op );
		}
	}
	catch( SPELLcoreException& ex )
	{
		LOG_ERROR("Cannot attach: " + std::string(ex.what()) );
		resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_ATTACH_EXEC, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot attach to executor");
		resp.set( MessageField::FIELD_REASON, ex.what());
		resp.set( MessageField::FIELD_FATAL, PythonConstants::True );
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
void SPELLcontext::clientLost( int clientKey )
{
	DEBUG("Client " + ISTR(clientKey) + " lost, stopping procedures");
	SPELLclient* client = SPELLclientManager::instance().getClient( clientKey );
	if (client)
	{
		// Remove the client from all procedures associated to it
		SPELLclient::ProcedureList procs = client->getProcedures();
		SPELLclient::ProcedureList::iterator it;
		for ( it = procs.begin(); it != procs.end(); it++ )
		{
			try
			{
				SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor( it->first );
				if (it->second == CLIENT_MODE_CONTROL )
				{
					SPELLclientManager::instance().removeExecutorController(client, exec, true, true);
					LOG_WARN("Client " + ISTR(clientKey) + " stop controlling executor " + exec->getModel().getInstanceId());
				}
				else
				{
					SPELLclientManager::instance().stopMonitorExecutor(client, exec);
					LOG_INFO("Client " + ISTR(clientKey) + " stop monitoring executor " + exec->getModel().getInstanceId());
				}
			}
			catch(SPELLcoreException& ex){};
		}
		DEBUG("Client " + ISTR(clientKey) + " lost, stopping procedures done");
	}
	else
	{
		LOG_ERROR("Unable to stop procedures for client " + ISTR(clientKey) + " client not found");
	}
	// Now remove the client from the client manager
	SPELLclientManager::instance().clientLost(clientKey);
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
void SPELLcontext::executorLost( const std::string& instanceId )
{
	try
	{
		LOG_ERROR("Executor lost: " + instanceId);
		SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor( instanceId );
		std::string groupId = exec->getModel().getGroupId();
		std::string originId = exec->getModel().getOriginId();
		std::string parentId = exec->getModel().getParentInstanceId();
		LOG_ERROR("   - Group ID : " + groupId);
		LOG_ERROR("   - Origin ID: " + originId);
		LOG_ERROR("   - Parent ID: " + parentId);

		// Remove the controlling client about the loss
		SPELLclient* client = exec->getControllingClient();
		if (client)
		{
			SPELLclientManager::instance().removeExecutorController( client, exec, false, false );
		}

		// Remove all monitoring clients
		std::list<int> mkeys = SPELLclientManager::instance().getMonitoringClientsKeys( instanceId );
		std::list<int>::iterator it;
		for( it = mkeys.begin(); it != mkeys.end(); it++ )
		{
			SPELLclient* mclient = SPELLclientManager::instance().getClient(*it);
			SPELLclientManager::instance().stopMonitorExecutor(mclient, exec);
		}

		LOG_ERROR("Notify clients about executor lost");

		// Notify all GUIs in the system
		SPELLexecutorOperation op;
		op.instanceId = instanceId;
		op.status = STATUS_ERROR;
		op.groupId = groupId;
		op.originId = originId;
		op.parentId = parentId;
		op.type = SPELLexecutorOperation::EXEC_OP_CRASH;
		notifyExecutorOperation( op );

		// Mark the model to be removed (cannot delete now, since this operation is
		// triggered from the process model itself
		SPELLexecutorManager::instance().clearExecutor( instanceId );
	}
	catch(SPELLcoreException& ex){};
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::detachExecutor( const SPELLipcMessage& msg, SPELLclient* client )
{
	SPELLipcMessage resp = VOID_MESSAGE;

	std::string instanceId = msg.get( MessageField::FIELD_PROC_ID );
	DEBUG( NAME + "Requested detaching from executor: " + instanceId );

	try
	{
		SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor(instanceId);
		std::string groupId = exec->getModel().getGroupId();
		std::string originId = exec->getModel().getOriginId();

		LOG_INFO("Client " + ISTR(client->getClientKey()) + " detached from executor " + instanceId);

		// To notify to other clients
		SPELLexecutorOperation op;
		op.instanceId = instanceId;
		op.clientKey = msg.getKey();
		op.groupId = groupId;
		op.originId = originId;
		op.status = exec->getStatus();
		op.type = SPELLexecutorOperation::EXEC_OP_DETACH;

		resp = SPELLipcHelper::createResponse( ContextMessages::RSP_DETACH_EXEC, msg);

		if (exec->getControllingClient() == client)
		{
			SPELLclientManager::instance().removeExecutorController( client, exec, true, false );
			op.clientMode = CLIENT_MODE_CONTROL;
		}
		else
		{
			SPELLclientManager::instance().stopMonitorExecutor( client, exec );
			op.clientMode = CLIENT_MODE_MONITOR;
		}


		notifyExecutorOperation( op );
	}
	catch( SPELLcoreException& ex )
	{
		LOG_ERROR("Cannot detach: " + std::string(ex.what()) );
		resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_DETACH_EXEC, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot detach from executor");
		resp.set( MessageField::FIELD_REASON, ex.what() );
		resp.set( MessageField::FIELD_FATAL, PythonConstants::True );
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::removeControl( const SPELLipcMessage& msg )
{
	SPELLipcMessage resp = VOID_MESSAGE;

	std::string instanceId = msg.get( MessageField::FIELD_PROC_ID );
	DEBUG( NAME + "Requested remove control from executor: " + instanceId );


	try
	{
		SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor(instanceId);
		std::string groupId = exec->getModel().getGroupId();
		std::string originId = exec->getModel().getOriginId();

		resp = SPELLipcHelper::createResponse( ContextMessages::RSP_REMOVE_CONTROL, msg);
		SPELLclient* client = exec->getControllingClient();

		if (client)
		{
			LOG_INFO("Client " + ISTR(client->getClientKey()) + " removed from executor " + instanceId);
			SPELLclientManager::instance().removeExecutorController( client, exec, true, false );
			// Notify other clients
			SPELLexecutorOperation op;
			op.instanceId = instanceId;
			op.clientKey = msg.getKey();
			op.groupId = groupId;
			op.originId = originId;
			op.clientMode = CLIENT_MODE_CONTROL;
			op.status = exec->getStatus();
			op.type = SPELLexecutorOperation::EXEC_OP_DETACH;
			notifyExecutorOperation( op );
		}

	}
	catch( SPELLcoreException& ex )
	{
		LOG_ERROR("Cannot remove control: " + std::string(ex.what()) );
		resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_REMOVE_CONTROL, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot remove control");
		resp.set( MessageField::FIELD_REASON, ex.what() );
		resp.set( MessageField::FIELD_FATAL, PythonConstants::True );
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::setExecutorInBackground( const SPELLipcMessage& msg )
{
	SPELLipcMessage resp = VOID_MESSAGE;

	std::string instanceId = msg.get( MessageField::FIELD_PROC_ID );
	DEBUG( NAME + "Requested put executor " + instanceId + " in background");

	try
	{
		SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor(instanceId);
		std::string groupId = exec->getModel().getGroupId();
		std::string originId = exec->getModel().getOriginId();

		resp = SPELLipcHelper::createResponse( ContextMessages::RSP_SET_BACKGROUND, msg);
		SPELLclient* client = exec->getControllingClient();

		if (client)
		{
			LOG_INFO("Client " + ISTR(client->getClientKey()) + " removed from executor " + instanceId);
			SPELLclientManager::instance().setExecutorInBackground( client, exec );
			// Notify other clients
			SPELLexecutorOperation op;
			op.instanceId = instanceId;
			op.clientKey = msg.getKey();
			op.groupId = groupId;
			op.originId = originId;
			op.clientMode = CLIENT_MODE_CONTROL;
			op.status = exec->getStatus();
			op.type = SPELLexecutorOperation::EXEC_OP_DETACH;
			notifyExecutorOperation( op );
		}

	}
	catch( SPELLcoreException& ex )
	{
		LOG_ERROR("Cannot set to background: " + std::string(ex.what()) );
		resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_REMOVE_CONTROL, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot set to background");
		resp.set( MessageField::FIELD_REASON, ex.what() );
		resp.set( MessageField::FIELD_FATAL, PythonConstants::True );
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getClientInfo( const SPELLipcMessage& msg )
{
	SPELLipcMessage resp = VOID_MESSAGE;
	std::string clientKeyStr = msg.get( MessageField::FIELD_GUI_KEY );

	if (clientKeyStr != "")
	{
		std::vector<std::string> tok = SPELLutils::tokenize( clientKeyStr, ":" );
		int clientKey = -1;
		if (tok.size()==2) clientKey = STRI( tok[1] );
		SPELLclient* client = SPELLclientManager::instance().getClient(clientKey);
		if (client)
		{
			resp = SPELLipcHelper::createResponse( ContextMessages::RSP_CLIENT_INFO, msg);
			resp.set( MessageField::FIELD_GUI_KEY, ISTR(clientKey) );
			resp.set( MessageField::FIELD_GUI_MODE, "" ); // to be removed
			resp.set( MessageField::FIELD_HOST, client->getClientHost() );
		}
		else
		{
			resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_CLIENT_INFO, msg );
			resp.set( MessageField::FIELD_ERROR, "Cannot get client information");
			resp.set( MessageField::FIELD_REASON, "No such client: " + clientKeyStr );
			resp.set( MessageField::FIELD_FATAL, PythonConstants::True );
		}
	}
	else
	{
		resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_CLIENT_INFO, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot get client information");
		resp.set( MessageField::FIELD_REASON, "No client key given" );
		resp.set( MessageField::FIELD_FATAL, PythonConstants::True );
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getInstanceId( const SPELLipcMessage& msg )
{
    // Create the login message
    std::string requestId = msg.getId();
    SPELLipcMessage resp = VOID_MESSAGE;

	DEBUG( NAME + "Requested new instance id");
	std::string procId = msg.get( MessageField::FIELD_PROC_ID );
	try
	{
		// Ensure that the procedure id exists
		SPELLprocedureManager::instance().getProcName(procId);
		std::string instanceId = SPELLexecutorManager::instance().getInstanceId( procId );
		resp = SPELLipcHelper::createResponse( ContextMessages::RSP_INSTANCE_ID, msg );
		resp.set( MessageField::FIELD_INSTANCE_ID, instanceId );
	}
	catch(SPELLcoreException& ex)
	{
		resp = SPELLipcHelper::createErrorResponse(ContextMessages::RSP_INSTANCE_ID, msg);
		resp.set(MessageField::FIELD_ERROR, "Cannot get instance number");
		resp.set(MessageField::FIELD_REASON, "Procedure does not exist: '" + procId + "'");
		resp.set(MessageField::FIELD_FATAL, "true");
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::getCurrentTime
//=============================================================================
SPELLipcMessage SPELLcontext::getCurrentTime(const SPELLipcMessage& msg)
{
	SPELLipcMessage resp = VOID_MESSAGE;

	SPELLsafePythonOperations ops("getCurrentTime");

	std::string time = SPELLpythonHelper::instance().evalTime("NOW").toString();
	resp = SPELLipcHelper::createResponse(ContextMessages::RSP_CURRENT_TIME, msg);
	resp.set(MessageField::FIELD_DRIVER_TIME, time);
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getProcedureCode( const SPELLipcMessage& msg )
{
	SPELLipcMessage resp = VOID_MESSAGE;
	DEBUG( NAME + "Requested procedure source code");
	resp = SPELLipcHelper::createResponse( ContextMessages::RSP_PROC_CODE, msg);
	std::string procId = msg.get( MessageField::FIELD_PROC_ID );
	SPELLipcDataChunk::DataList data;

	if (msg.get( MessageField::FIELD_CHUNK ) != "")
	{
		int chunkNo = STRI( msg.get( MessageField::FIELD_CHUNK ) );
		DEBUG( NAME + "Get code chunk " + ISTR(chunkNo));
		data = getChunker(msg).getChunk( procId, chunkNo );
		int totalChunks = getChunker(msg).getSize( procId );
		if (chunkNo == (totalChunks-1))
		{
			getChunker(msg).endChunks(procId);
			clearChunker(msg);
		}
		resp.set( MessageField::FIELD_CHUNK, ISTR(chunkNo) );
		resp.set( MessageField::FIELD_TOTAL_CHUNKS, ISTR(totalChunks));
	}
	else
	{
		SPELLprocedureSourceCode source = SPELLprocedureManager::instance().getSourceCode( procId );
		std::vector<std::string> lines = source.getSourceCodeLines();
		int totalChunks = getChunker(msg).startChunks( procId, lines );
		resp.set( MessageField::FIELD_CHUNK, "0" );

		DEBUG( NAME + "Start chunks " + ISTR(totalChunks));

		if (totalChunks == 0)
		{
			resp.set( MessageField::FIELD_TOTAL_CHUNKS, "0");
			data = source.getSourceCodeLines();
		}
		else
		{
			resp.set( MessageField::FIELD_TOTAL_CHUNKS, ISTR(totalChunks));
			data = getChunker(msg).getChunk( procId, 0 );
		}
	}
	std::string dataStr = "";
	dataStr = SPELLdataHelper::sourceToString(data);
	resp.set( MessageField::FIELD_PROC_CODE, dataStr );
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getServerFilePath( const SPELLipcMessage& msg )
{
	SPELLipcMessage resp = VOID_MESSAGE;

	if (msg.hasField(MessageField::FIELD_PROC_ID))
	{
		std::string procId = msg.get( MessageField::FIELD_PROC_ID );
		DEBUG( NAME + "Requested server file for " + procId );

		if (msg.hasField( MessageField::FIELD_SERVER_FILE_ID))
		{
			std::string fileTypeStr = msg.get( MessageField::FIELD_SERVER_FILE_ID );
			std::string path = "";

			try
			{
				SPELLexecutor* exec = SPELLexecutorManager::instance().getExecutor(procId);

				SPELLserverFile file = SPELLdataHelper::serverFileFromString( fileTypeStr );
				if (file == FILE_ASRUN)
				{
					path = exec->getModel().getAsRunFilename();
				}
				else if (file == FILE_LOG)
				{
					path = exec->getModel().getLogFilename();
				}

				resp = SPELLipcHelper::createResponse( ContextMessages::RSP_SERVER_FILE_PATH, msg );
				resp.set( MessageField::FIELD_FILE_PATH, path );
			}
			catch(SPELLcoreException& ex)
			{
				LOG_ERROR("Cannot provide file path: cannot find executor '" + procId + "'");
				resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_SERVER_FILE_PATH, msg );
				resp.set( MessageField::FIELD_ERROR, "Cannot provide file path");
				resp.set( MessageField::FIELD_REASON, "Cannot find executor '" + procId + "'");
			}
		}
		else
		{
			LOG_ERROR("Cannot provide file path: missing server file type identifier");
			resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_SERVER_FILE_PATH, msg );
			resp.set( MessageField::FIELD_ERROR, "Cannot provide file path");
			resp.set( MessageField::FIELD_REASON, "Missing file type");
		}
	}
	else if (msg.hasField( MessageField::FIELD_SERVER_FILE_ID))
	{
		std::string fileTypeStr = msg.get( MessageField::FIELD_SERVER_FILE_ID );
		std::string path = "";
		SPELLserverFile file = SPELLdataHelper::serverFileFromString( fileTypeStr );
		SPELLcontextConfig& ctxConfig = SPELLconfiguration::instance().getContext(m_config.contextName);

		if (file == FILE_ASRUN)
		{
			path = SPELLutils::getSPELL_DATA() + PATH_SEPARATOR + ctxConfig.getLocationPath("ar");
		}
		else if (file == FILE_LOG)
		{
			path = SPELLutils::getSPELL_LOG();
		}

		resp = SPELLipcHelper::createResponse( ContextMessages::RSP_SERVER_FILE_PATH, msg );
		resp.set( MessageField::FIELD_FILE_PATH, path );
	}
	else
	{
		LOG_ERROR("Cannot provide file path: missing procedure identifier and no file type given");
		resp = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_SERVER_FILE_PATH, msg );
		resp.set( MessageField::FIELD_ERROR, "Cannot provide file path");
		resp.set( MessageField::FIELD_REASON, "Missing procedure identifier");
	}
	return resp;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcDataChunk::DataList SPELLcontext::getServerFileData( const std::string& filename )
{
	SPELLipcDataChunk::DataList data;
	std::ifstream fileis;
	if (!SPELLutils::pathExists(filename))
	{
		THROW_EXCEPTION("Cannot obtain file '" + filename + "'", "File not found", SPELL_ERROR_FILESYSTEM);
	}
    fileis.open( filename.c_str() );
    if (!fileis.is_open())
    {
        THROW_EXCEPTION("Cannot obtain file '" + filename + "'", "Unable to open", SPELL_ERROR_FILESYSTEM);
    }
    do
    {
        std::string line = "";
        std::getline(fileis,line);
        if (line != "") data.push_back(line);
    }
    while(!fileis.eof());
    fileis.close();
    return data;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcDataChunk::DataList SPELLcontext::getInputFileData( SPELLdatabase* db )
{
	SPELLipcDataChunk::DataList data;
	std::vector<std::string> keys = db->keysStr();
	std::vector<std::string>::iterator it;
	for( it = keys.begin(); it != keys.end(); it++ )
	{
		std::string value = db->getStr(*it);
		SPELLutils::trim(value);
		std::string line = *it + VARIABLE_PROPERTY_SEPARATOR + value;
		if (data.size()>0)
		{
			line = VARIABLE_SEPARATOR + line;
		}
		data.push_back(line);
	}
    return data;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcDataChunk& SPELLcontext::getChunker( const SPELLipcMessage& msg )
{
	SPELLmonitor m(m_chunkLock);
	std::string chunkerId = ISTR(msg.getKey()) + ":" + msg.getId();

	if (m_dataChunkers.find(chunkerId) == m_dataChunkers.end())
	{
		m_dataChunkers.insert( std::make_pair( chunkerId, new SPELLipcDataChunk(100) ));
	}
	return *m_dataChunkers[chunkerId];
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
void SPELLcontext::clearChunker( const SPELLipcMessage& msg )
{
	SPELLmonitor m(m_chunkLock);
	std::string chunkerId = ISTR(msg.getKey()) + ":" + msg.getId();

	ChunkerMap::iterator it = m_dataChunkers.find(chunkerId);
	if (it != m_dataChunkers.end())
	{
		delete it->second;
		m_dataChunkers.erase(it);
	}
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getInputFile( const SPELLipcMessage& msg )
{
	SPELLsafePythonOperations ops("getInputFile");

	SPELLipcMessage response = SPELLipcHelper::createResponse( ContextMessages::RSP_INPUT_FILE, msg );

	std::string filename = msg.get(MessageField::FIELD_FILE_PATH);

	DEBUG( NAME + "Request to get input file '" + filename + "'");

	SPELLipcDataChunk::DataList data;

	if (msg.get( MessageField::FIELD_CHUNK ) != "")
	{
		int chunkNo = STRI( msg.get( MessageField::FIELD_CHUNK ) );
		DEBUG("Get dictionary contents chunk " + ISTR(chunkNo));
		data = getChunker(msg).getChunk( filename, chunkNo );
		int totalChunks = getChunker(msg).getSize( filename );
		if (chunkNo == (totalChunks-1))
		{
			DEBUG( NAME + "End chunks" );
			getChunker(msg).endChunks(filename);
			clearChunker(msg);
		}
		response.set( MessageField::FIELD_CHUNK, ISTR(chunkNo) );
		response.set( MessageField::FIELD_TOTAL_CHUNKS, ISTR(totalChunks));
		response.set( MessageField::FIELD_DICT_CONTENTS, SPELLdataHelper::linesToString(data) );
		DEBUG("Given chunk " +ISTR(chunkNo));
	}
	else
	{
		if (!SPELLutils::pathExists(filename))
		{
			THROW_EXCEPTION("Cannot obtain file '" + filename + "'", "File not found", SPELL_ERROR_FILESYSTEM);
		}

		// Find out the file location
		SPELLcontextConfig& ctxConfig = SPELLconfiguration::instance().getContext(m_config.contextName);

		std::list<std::string> locations = ctxConfig.getLocations();

		std::string correspondingLocation = "";
		for(std::list<std::string>::iterator it = locations.begin(); it != locations.end(); it++ )
		{
			std::string lpath = ctxConfig.getLocationPath(*it);
			// Append the data dir
			lpath = SPELLutils::getSPELL_DATA() + PATH_SEPARATOR + lpath;

			if (filename.find(lpath) == 0)
			{
				correspondingLocation = *it;
				break;
			}
		}
		if (correspondingLocation == "")
		{
			THROW_EXCEPTION("Cannot find appropriate location path for '" + filename + "'", "Unknown location", SPELL_ERROR_FILESYSTEM);
		}

		DEBUG( NAME + "Corresponding location is " + correspondingLocation);

		int idx = filename.rfind(".")+1;
		std::string ext = filename.substr(idx,filename.size()-idx);
		std::string type = ctxConfig.getLocationType(correspondingLocation);

		DEBUG( NAME + "Input file type is " + type + ", extension " + ext);

		// Once the location path is obtained, use the appropriate parser for the file
		SPELLdatabase* db = SPELLdatabaseFactory::instance().createDatabase( type, filename, filename, ext );

		if (db == NULL)
		{
			THROW_EXCEPTION("Cannot load input file '" + filename + "'", "Unable to create database parser (" + type + ")", SPELL_ERROR_FILESYSTEM);
		}

		DEBUG( NAME + "Loading database");

		// May throw exceptions in parsing or setup errors
		db->load();

		data = getInputFileData( db );

		int totalChunks = getChunker(msg).startChunks( filename, data );
		response.set( MessageField::FIELD_CHUNK, "0" );

		// Chunk if needed
		if (totalChunks != 0)
		{
			DEBUG("Input file contents needs chunk: " + ISTR(totalChunks));

			response.set( MessageField::FIELD_CHUNK, "0" );

			data = getChunker(msg).getChunk( filename, 0 );

			response.set( MessageField::FIELD_DICT_CONTENTS, SPELLdataHelper::linesToString(data) );
			response.set( MessageField::FIELD_TOTAL_CHUNKS, ISTR(totalChunks));
		}
		else
		{
			DEBUG("No need for chunk");
			response.set( MessageField::FIELD_TOTAL_CHUNKS, "0");
			response.set( MessageField::FIELD_DICT_CONTENTS, SPELLdataHelper::linesToString(data) );
		}
	}

	DEBUG( NAME + "Input file '" + filename + "' obtained");

    return response;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
void SPELLcontext::notifyExecutorOperation( const SPELLexecutorOperation& op )
{
	LOG_INFO( NAME + "Notify executor operation " + op.toString() );
	SPELLipcMessage notification( ContextMessages::MSG_EXEC_OP );
	notification.setSender("CTX");
	notification.setReceiver("CLT");
	notification.setType(MSG_TYPE_ONEWAY);

	op.completeMessage(notification);

	// Notify to clients
	SPELLclientManager::instance().notifyClients( notification );

	// Notify to parent proc if needed
	LOG_INFO( NAME + "Notify executor operation to parent: " + op.parentId );
	if (op.parentId != "")
	{
		DEBUG( NAME + "Notify to parent executor if present");
		try
		{
			SPELLexecutor* parentExec = SPELLexecutorManager::instance().getExecutor( op.parentId );
			LOG_INFO("Notify parent executor " + op.parentId);
			parentExec->sendMessageToExecutor( notification );
		}
		catch( SPELLcoreException& ex )
		{
			LOG_ERROR("Cannot notify parent executor: " + std::string(ex.what()));
		}
	}
	DEBUG( NAME + "Notify executor operation done");
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
void SPELLcontext::notifySharedDataOperation( const std::string& procId,
											  const SPELLsharedDataOperation& operation,
											  const std::string& variables, const std::string& values,
											  const std::string& scope )
{
	DEBUG( NAME + "Notify context shared data operation by procedure");
	SPELLipcMessage notification( MessageId::MSG_ID_SHARED_DATA_OP );
	notification.setSender("CTX");
	notification.setReceiver("CLT");
	notification.setType(MSG_TYPE_ONEWAY);
	notification.set( MessageField::FIELD_PROC_ID, procId );
	notification.set( MessageField::FIELD_SHARED_OP, SPELLdataHelper::sharedDataOperationToString(operation) );
	notification.set( MessageField::FIELD_SHARED_SCOPE, scope );
	notification.set( MessageField::FIELD_SHARED_VARIABLE, variables );
	notification.set( MessageField::FIELD_SHARED_VALUE, values );

	// Notify to clients
	SPELLclientManager::instance().notifyClients( notification );

	DEBUG( NAME + "Notify context operation done");
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
void SPELLcontext::notifySharedDataOperation( const SPELLipcMessage& msg,
											  const SPELLsharedDataOperation& operation,
											  const std::string& variables, const std::string& values,
											  const std::string& scope )
{
	// Determine the originator
	std::string procId = msg.get( MessageField::FIELD_PROC_ID );
	std::string client = msg.get( MessageField::FIELD_GUI_KEY );
	if (procId != "")
	{
		notifySharedDataOperation( procId, operation, variables, values , scope );
	}
	else if (client != "")
	{
		notifySharedDataOperation( STRI(client), operation, variables, values , scope );
	}
	else
	{
		LOG_ERROR("Cannot notify context operation: unknown originator: " + msg.dataStr());
	}
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
void SPELLcontext::notifySharedDataOperation( int clientKey,
											  const SPELLsharedDataOperation& operation,
											  const std::string& variables, const std::string& values,
											  const std::string& scope )
{
	DEBUG( NAME + "Notify context shared data operation by client");
	SPELLipcMessage notification( MessageId::MSG_ID_SHARED_DATA_OP );
	notification.setSender("CTX");
	notification.setReceiver("CLT");
	notification.setType(MSG_TYPE_ONEWAY);
	notification.set( MessageField::FIELD_GUI_KEY, ISTR(clientKey) );
	notification.set( MessageField::FIELD_SHARED_OP, SPELLdataHelper::sharedDataOperationToString(operation) );
	notification.set( MessageField::FIELD_SHARED_SCOPE, scope );
	notification.set( MessageField::FIELD_SHARED_VARIABLE, variables );
	notification.set( MessageField::FIELD_SHARED_VALUE, values );

	// Notify to clients
	SPELLclientManager::instance().notifyClients( notification );

	DEBUG( NAME + "Notify context operation done");
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::setSharedData( const SPELLipcMessage& msg )
{
	std::string varName = msg.get(MessageField::FIELD_SHARED_VARIABLE);
	std::string varValue = msg.get(MessageField::FIELD_SHARED_VALUE);
	std::string varExpected = msg.get(MessageField::FIELD_SHARED_EXPECTED);
	std::string scope = msg.get(MessageField::FIELD_SHARED_SCOPE);

	std::string result = "";
	std::string errors = "";

	// If a set of variables is given
	if (varName.find(LIST_SEPARATOR) != std::string::npos)
	{
		std::vector<std::string> varNames = SPELLutils::tokenize(varName, LIST_SEPARATOR_STR);
		std::vector<std::string> varValues = SPELLutils::tokenize(varValue, LIST_SEPARATOR_STR);
		std::vector<std::string> varExpecteds = SPELLutils::tokenize(varExpected, LIST_SEPARATOR_STR);
		std::vector<std::string>::iterator it;
		int idx = 0;
		for( it = varNames.begin(); it != varNames.end(); it++ )
		{
			std::string itemName = varNames[idx];
			std::string itemValue = varValues[idx];
			std::string itemExpected = varExpecteds[idx];
			if (result != "") result += LIST_SEPARATOR;
			try
			{
				if (m_sharedData.set(itemName,itemValue,itemExpected,scope))
				{
					result += "True";
				}
				else
				{
					result += "False";
				}
			}
			catch(SPELLcoreException& ex)
			{
				LOG_ERROR("Failed to set shared variable data: " + ex.what());
				result += "False";
				errors += "Item " + itemName + ": " + ex.what() + "\n";
			}
			idx++;
		}
	}
	else
	{
		try
		{
			if (m_sharedData.set(varName,varValue,varExpected,scope))
			{
				result = "True";
			}
			else
			{
				result = "False";
			}
		}
		catch(SPELLcoreException& ex)
		{
			result = "False";
			errors += "Variable " + varName + ": " + ex.what() + "\n";
			LOG_ERROR("Failed to set shared variable data: " + ex.what());
		}
	}

	SPELLipcMessage response = VOID_MESSAGE;

	if (errors != "")
	{
		response = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_SET_SHARED_DATA, msg );
		response.set(MessageField::FIELD_ERROR, "Failed to set shared variable data");
		response.set(MessageField::FIELD_REASON, errors);
		response.set(MessageField::FIELD_SHARED_SUCCESS, result);
	}
	else
	{
		response = SPELLipcHelper::createResponse( ContextMessages::RSP_SET_SHARED_DATA, msg );
		response.set(MessageField::FIELD_SHARED_SUCCESS, result);
		notifySharedDataOperation(msg, SET_SHARED_DATA, varName, varValue, scope);
	}

	return response;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getSharedData( const SPELLipcMessage& msg )
{
	std::string varName = msg.get(MessageField::FIELD_SHARED_VARIABLE);
	std::string scope = msg.get(MessageField::FIELD_SHARED_SCOPE);

	std::string result = "";
	std::string errors = "";

	// If a set of variables is given
	if (varName.find(LIST_SEPARATOR) != std::string::npos)
	{
		std::vector<std::string> varNames = SPELLutils::tokenize(varName, LIST_SEPARATOR_STR);
		std::vector<std::string>::iterator it;
		int idx = 0;
		for( it = varNames.begin(); it != varNames.end(); it++ )
		{
			std::string itemName = varNames[idx];
			std::string itemValue;
			if (result != "") result += LIST_SEPARATOR;
			try
			{
				result += m_sharedData.get(itemName,scope);
			}
			catch(SPELLcoreException& ex)
			{
				LOG_ERROR("Failed to get shared variable data: " + ex.what());
				result += "None";
				errors += "Item " + itemName + ": " + ex.what() + "\n";
			}
			idx++;
		}
	}
	else
	{
		try
		{
			result = m_sharedData.get(varName,scope);
		}
		catch(SPELLcoreException& ex)
		{
			errors = ex.what() + "\n";
		}
	}

	SPELLipcMessage response = VOID_MESSAGE;

	if (errors != "")
	{
		response = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_GET_SHARED_DATA, msg );
		response.set(MessageField::FIELD_ERROR, "Failed to get shared variable data");
		response.set(MessageField::FIELD_REASON, errors);
		response.set(MessageField::FIELD_SHARED_VARIABLE, varName);
		response.set(MessageField::FIELD_SHARED_VALUE, result);
		response.set(MessageField::FIELD_SHARED_SCOPE, scope);
	}
	else
	{
		response = SPELLipcHelper::createResponse( ContextMessages::RSP_GET_SHARED_DATA, msg );
		response.set(MessageField::FIELD_SHARED_VARIABLE, varName);
		response.set(MessageField::FIELD_SHARED_VALUE, result);
		response.set(MessageField::FIELD_SHARED_SCOPE, scope);
	}

	return response;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::clearSharedData( const SPELLipcMessage& msg )
{
	std::string varName = msg.get(MessageField::FIELD_SHARED_VARIABLE);
	std::string scope = msg.get(MessageField::FIELD_SHARED_SCOPE);

	std::string errors = "";

	std::string result = "";

	// If no variable given
	if (varName == "")
	{
		m_sharedData.clearScope(scope);
		result = "True";
		notifySharedDataOperation( msg, CLEAR_SHARED_SCOPE, "", "", scope );
	}
	// If a set of variables is given
	else if (varName.find(LIST_SEPARATOR) != std::string::npos)
	{
		std::vector<std::string> varNames = SPELLutils::tokenize(varName, LIST_SEPARATOR_STR);
		std::vector<std::string>::iterator it;
		int idx = 0;
		for( it = varNames.begin(); it != varNames.end(); it++ )
		{
			std::string itemName = varNames[idx];
			try
			{
				if (result != "") result += LIST_SEPARATOR;
				if (m_sharedData.clear(itemName,scope))
				{
					result += "True";
				}
				else
				{
					result += "False";
				}
			}
			catch(SPELLcoreException& ex)
			{
				LOG_ERROR("Failed to clear shared variable data: " + ex.what());
				errors += "Item " + itemName + ": " + ex.what() + "\n";
				result += "False";
			}
			idx++;
		}
		notifySharedDataOperation( msg, DEL_SHARED_DATA, varName, "", scope );
	}
	else
	{
		try
		{
			if (m_sharedData.clear(varName,scope))
			{
				result = "True";
			}
			else
			{
				result = "False";
			}
		}
		catch(SPELLcoreException& ex)
		{
			errors = ex.what() + "\n";
			result = "False";
		}
		notifySharedDataOperation( msg, DEL_SHARED_DATA, varName, "", scope );
	}

	SPELLipcMessage response = VOID_MESSAGE;

	if (errors != "")
	{
		response = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_DEL_SHARED_DATA, msg );
		response.set(MessageField::FIELD_ERROR, "Failed to clear shared variable data");
		response.set(MessageField::FIELD_REASON, errors);
		response.set(MessageField::FIELD_SHARED_VARIABLE, varName);
		response.set(MessageField::FIELD_SHARED_SUCCESS, result);
		response.set(MessageField::FIELD_SHARED_SCOPE, scope);
	}
	else
	{
		response = SPELLipcHelper::createResponse( ContextMessages::RSP_DEL_SHARED_DATA, msg );
		response.set(MessageField::FIELD_SHARED_VARIABLE, varName);
		response.set(MessageField::FIELD_SHARED_SUCCESS, result);
		response.set(MessageField::FIELD_SHARED_SCOPE, scope);
	}

	return response;

}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getSharedDataKeys( const SPELLipcMessage& msg )
{
	std::string scope = msg.get(MessageField::FIELD_SHARED_SCOPE);

	std::string keysStr = "";
	std::string errors = "";

	try
	{
		SPELLdataTable::KeyList keys = m_sharedData.getVariables(scope);
		SPELLdataTable::KeyList::iterator it;
		for( it = keys.begin(); it != keys.end(); it++ )
		{
			if (keysStr != "") keysStr += LIST_SEPARATOR;
			keysStr += *it;
		}
	}
	catch(SPELLcoreException& ex)
	{
		errors = ex.what() + "\n";
	}

	SPELLipcMessage response = VOID_MESSAGE;

	if (errors != "")
	{
		response = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_GET_SHARED_DATA_KEYS, msg );
		response.set(MessageField::FIELD_ERROR, "Failed to get shared variable names");
		response.set(MessageField::FIELD_REASON, errors);
		response.set(MessageField::FIELD_SHARED_SCOPE, scope);
	}
	else
	{
		response = SPELLipcHelper::createResponse( ContextMessages::RSP_GET_SHARED_DATA_KEYS, msg );
		response.set(MessageField::FIELD_SHARED_VARIABLE, keysStr);
		response.set(MessageField::FIELD_SHARED_SCOPE, scope);
	}

	return response;

}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::getSharedDataScopes( const SPELLipcMessage& msg )
{
	std::string scopeStr = "";
	std::string errors = "";

	try
	{
		SPELLdataTable::KeyList scopes = m_sharedData.getScopes();
		SPELLdataTable::KeyList::iterator it;
		for( it = scopes.begin(); it != scopes.end(); it++ )
		{
			std::string scope = *it;
			if (scope == GLOBAL_SCOPE) continue;
			if (scopeStr != "") scopeStr += LIST_SEPARATOR;
			scopeStr += scope;
		}
	}
	catch(SPELLcoreException& ex)
	{
		errors = ex.what() + "\n";
	}

	SPELLipcMessage response = VOID_MESSAGE;

	if (errors != "")
	{
		response = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_GET_SHARED_DATA_SCOPES, msg );
		response.set(MessageField::FIELD_ERROR, "Failed to get shared variable scopes");
		response.set(MessageField::FIELD_REASON, errors);
	}
	else
	{
		response = SPELLipcHelper::createResponse( ContextMessages::RSP_GET_SHARED_DATA_SCOPES, msg );
		response.set(MessageField::FIELD_SHARED_SCOPE, scopeStr);
	}

	return response;

}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::addSharedDataScope( const SPELLipcMessage& msg )
{
	std::string scope = msg.get(MessageField::FIELD_SHARED_SCOPE);
	std::string errors = "";

	try
	{
		m_sharedData.addScope(scope);
	}
	catch(SPELLcoreException& ex)
	{
		errors = ex.what() + "\n";
	}

	SPELLipcMessage response = VOID_MESSAGE;

	if (errors != "")
	{
		response = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_ADD_SHARED_DATA_SCOPE, msg );
		response.set(MessageField::FIELD_ERROR, "Failed to add shared variable scope");
		response.set(MessageField::FIELD_REASON, errors);
	}
	else
	{
		response = SPELLipcHelper::createResponse( ContextMessages::RSP_ADD_SHARED_DATA_SCOPE, msg );
		response.set(MessageField::FIELD_SHARED_SUCCESS, "True");
		notifySharedDataOperation( msg, ADD_SHARED_SCOPE, "", "", scope );
	}

	return response;
}

//=============================================================================
// METHOD: SPELLcontext::
//=============================================================================
SPELLipcMessage SPELLcontext::removeSharedDataScope( const SPELLipcMessage& msg )
{
	std::string scope = msg.get(MessageField::FIELD_SHARED_SCOPE);
	std::string errors = "";
	// Used to determine the originator
	std::string procId = msg.get( MessageField::FIELD_PROC_ID );
	std::string client = msg.get( MessageField::FIELD_GUI_KEY );


	SPELLdataTable::KeyList allScopes;

	try
	{
		if (scope == "")
		{
			// Remove all
			allScopes = m_sharedData.getScopes();
			for(SPELLdataTable::KeyList::iterator it = allScopes.begin(); it != allScopes.end(); it++)
			{
				if (*it == GLOBAL_SCOPE) continue;
				m_sharedData.removeScope(*it);
			}
		}
		else
		{
			m_sharedData.removeScope(scope);
		}
	}
	catch(SPELLcoreException& ex)
	{
		errors = ex.what() + "\n";
	}

	SPELLipcMessage response = VOID_MESSAGE;

	if (errors != "")
	{
		response = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_DEL_SHARED_DATA_SCOPE, msg );
		if (scope == "")
		{
			response.set(MessageField::FIELD_ERROR, "Failed to remove ALL shared variable scopes");
			response.set(MessageField::FIELD_REASON, errors);
		}
		else
		{
			response.set(MessageField::FIELD_ERROR, "Failed to remove shared variable scope");
			response.set(MessageField::FIELD_REASON, errors);
		}
	}
	else
	{
		response = SPELLipcHelper::createResponse( ContextMessages::RSP_DEL_SHARED_DATA_SCOPE, msg );
		response.set(MessageField::FIELD_SHARED_SUCCESS, "True");
		if (scope != "")
		{
			notifySharedDataOperation( msg, DEL_SHARED_SCOPE, "", "", scope );
		}
		else
		{
			for(SPELLdataTable::KeyList::iterator it = allScopes.begin(); it != allScopes.end(); it++)
			{
				notifySharedDataOperation( msg, DEL_SHARED_SCOPE, "", "", *it );
			}
		}
	}

	return response;
}
