// ################################################################################
// FILE       : SPELLexecutorManager.C
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
#include "SPELL_CTX/SPELLexecutorManager.H"
#include "SPELL_CTX/SPELLclientManager.H"
#include "SPELL_CTX/SPELLdataHelper.H"
#include "SPELL_CTX/SPELLcontext.H"
// Project includes --------------------------------------------------------
#include "SPELL_PRD/SPELLprocedureManager.H"
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_IPC/SPELLipc_Context.H"
#include "SPELL_IPC/SPELLipc_Executor.H"
#include "SPELL_WRP/SPELLconstants.H"
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////
SPELLexecutorManager* SPELLexecutorManager::s_instance = NULL;

//=============================================================================
// CONSTRUCTOR: SPELLexecutorManager::SPELLexecutorManager()
//=============================================================================
SPELLexecutorManager::SPELLexecutorManager()
{
	m_persisTable = NULL;
}

//=============================================================================
// DESTRUCTOR: SPELLexecutorManager::~SPELLexecutorManager()
//=============================================================================
SPELLexecutorManager::~SPELLexecutorManager()
{
}

//=============================================================================
// STATIC: SPELLexecutorManager::instance()
//=============================================================================
SPELLexecutorManager& SPELLexecutorManager::instance()
{
	if (s_instance == NULL)
	{
		s_instance = new SPELLexecutorManager();
	}
	return *s_instance;
}

//=============================================================================
// METHOD: SPELLexecutorManager::setup()
//=============================================================================
void SPELLexecutorManager::setup( const std::string& ctxName )
{
	m_contextName = ctxName;
	m_persisTable = new SPELLpersistencyTable(ctxName);
	if (m_persisTable->load())
	{
		reconnectExecutors();
	}
}

//=============================================================================
// METHOD: SPELLexecutorManager::setup()
//=============================================================================
void SPELLexecutorManager::cleanup()
{
	if (m_persisTable) delete m_persisTable;
	for( ExecutorMap::iterator it = m_pendingLogin.begin(); it != m_pendingLogin.end(); it++ )
	{
		delete it->second;
	}
	m_pendingLogin.clear();
}

//=============================================================================
// METHOD: SPELLexecutorManager::startExecutor()
//=============================================================================
void SPELLexecutorManager::startExecutor( SPELLexecutorStartupParams& config, SPELLclient* controllingClient )
{
	DEBUG( "[EMGR] Start executor TRY-IN");
	//TODO check maximum number of executors
	SPELLmonitor m(m_lock);
	DEBUG( "[EMGR] Start executor IN");
	config.setContextName(m_contextName);
	config.setConfigFile(SPELLconfiguration::instance().getFile());

	// Create and start the executor with the given configuration

	// If the open mode does not include VISIBLE, do not pass the controlling client
	// This way we force the GUI to attach to a proc when it is started in non-visible
	SPELLexecutor* exec = NULL;
	if (config.getClientMode() == CLIENT_MODE_BACKGROUND )
	{
		DEBUG( "[EMGR] Start executor in background");
		exec = new SPELLexecutor(config, NULL);
	}
	else if ((config.getOpenMode() & OPEN_MODE_VISIBLE)>0)
	{
		DEBUG( "[EMGR] Start executor controlled, visible mode");
		exec = new SPELLexecutor(config, controllingClient);
	}
	else
	{
		DEBUG( "[EMGR] Start executor controlled, not visible");
		exec = new SPELLexecutor(config, NULL);
	}
	try
	{
		LOG_INFO("Starting executor " + exec->getModel().getInstanceId() );
		exec->start();

		addInstanceNumber( exec->getModel().getProcId(), exec->getModel().getInstanceNum() );
		addExecutorModel( exec );

		LOG_INFO("Executor ready");
	}
	catch(SPELLcoreException& ex)
	{
		// Delete recovery files just created for the new executor file
		deleteExecutorFiles(config);
		DEBUG( "[EMGR] Start executor OUT");
		THROW_EXCEPTION("Failed to start executor", ex.what(), SPELL_ERROR_PROCESS);
	}
	DEBUG( "[EMGR] Start executor OUT");
}

//=============================================================================
// METHOD: SPELLexecutorManager::reconnectExecutors()
//=============================================================================
void SPELLexecutorManager::reconnectExecutors()
{
	DEBUG( "[EMGR] Reconnecting orphan executors");
	SPELLmonitor m(m_lock);
	std::vector<std::string> ids = m_persisTable->getRegisteredExecutors();
	m_pendingLogin.clear();
	for(std::vector<std::string>::iterator it = ids.begin(); it != ids.end(); it++)
	{
		std::string instanceId = *it;
		SPELLexecutorPersistency pers = m_persisTable->getExecutorPersistency(instanceId);
		SPELLexecutorStartupParams config( instanceId, pers.timeId, true );
		config.setParentInstanceId(pers.parentId);
		config.setIpcPort(pers.ipcPort);
		config.setPID(pers.pid);

		SPELLexecutor* exec = new SPELLexecutor(config,NULL);
		try
		{
			LOG_WARN("Reconnecting executor " + instanceId + " listening on port " + ISTR(pers.ipcPort) );
			exec->start();
			m_pendingLogin.insert( std::make_pair( instanceId, exec ) );
		}
		catch(SPELLcoreException& ex)
		{
			deleteExecutorFiles(config);
		}
	}
	DEBUG( "[EMGR] Reconnect executors done");
}

//=============================================================================
// METHOD: SPELLexecutorManager::callback_executorReconnected()
//=============================================================================
void SPELLexecutorManager::callback_executorReconnected( const std::string& instanceId )
{
	LOG_WARN("#######################################");
	LOG_WARN("Executor " + instanceId + " reconnected");
	LOG_WARN("#######################################");
	SPELLmonitor m(m_lock);
	ExecutorMap::iterator it = m_pendingLogin.find(instanceId);
	if (it != m_pendingLogin.end())
	{
		SPELLexecutor* exec = it->second;
		addInstanceNumber( exec->getModel().getProcId(), exec->getModel().getInstanceNum() );
		addExecutorModel( exec );
		m_pendingLogin.erase(it);

		SPELLexecutorOperation op;
		op.instanceId = exec->getModel().getInstanceId();
		op.parentId = exec->getModel().getParentInstanceId();
		op.groupId = exec->getModel().getGroupId();
		op.originId = exec->getModel().getOriginId();
		op.status = exec->getModel().getStatus();
		op.type = SPELLexecutorOperation::EXEC_OP_OPEN;

		SPELLcontext::instance().notifyExecutorOperation( op );
	}
}

//=============================================================================
// METHOD: SPELLexecutorManager::callback_executorNotReconnected()
//=============================================================================
void SPELLexecutorManager::callback_executorNotReconnected( const std::string& instanceId )
{
	LOG_ERROR("#######################################");
	LOG_ERROR("Executor " + instanceId + " failed to reconnect");
	LOG_ERROR("#######################################");
	SPELLmonitor m(m_lock);
	ExecutorMap::iterator it = m_pendingLogin.find(instanceId);
	if (it != m_pendingLogin.end())
	{
		m_persisTable->deregisterExecutor(it->second->getModel().getInstanceId());
		delete it->second;
		m_pendingLogin.erase(it);
	}
}

//=============================================================================
// METHOD: SPELLexecutorManager::recoverExecutor()
//=============================================================================
void SPELLexecutorManager::recoverExecutor( SPELLexecutorStartupParams& config, SPELLclient* controllingClient )
{
	//TODO check maximum number of executors
	SPELLmonitor m(m_lock);
	config.setContextName(m_contextName);
	config.setConfigFile(SPELLconfiguration::instance().getFile());

	// Create and start the executor with the given configuration

	// If the open mode does not include VISIBLE, do not pass the controlling client
	// This way we force the GUI to attach to a proc when it is started in non-visible
	SPELLexecutor* exec = new SPELLexecutor(config, controllingClient);
	try
	{
		LOG_INFO("Recovering executor " + exec->getModel().getInstanceId() );
		exec->start();

		addInstanceNumber( exec->getModel().getProcId(), exec->getModel().getInstanceNum() );
		addExecutorModel( exec );

		LOG_INFO("Executor ready");
	}
	catch(SPELLcoreException& ex)
	{
		// Delete recovery files just created for the new executor file
		deleteExecutorFiles(config);
		THROW_EXCEPTION("Failed to recover executor", ex.what(), SPELL_ERROR_PROCESS);
	}
}

//=============================================================================
// METHOD: SPELLexecutorManager::deleteExecutorFiles()
//=============================================================================
void SPELLexecutorManager::deleteExecutorFiles( const SPELLexecutorStartupParams& config )
{
	SPELLcontextConfig& ctxConfig = SPELLconfiguration::instance().getContext(m_contextName);

	std::string fileName = config.getTimeId() + "_Executor_" + config.getInstanceId();

	LOG_INFO("Deleting executor files: " + fileName );

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
				LOG_INFO("Delete " + path );
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
				LOG_INFO("Delete " + path );
			}
		}
	}
}

//=============================================================================
// METHOD: SPELLexecutorManager::addExecutorModel()
//=============================================================================
void SPELLexecutorManager::addExecutorModel( SPELLexecutor* exec )
{
	m_executors.insert( std::make_pair( exec->getModel().getInstanceId(), exec ));
	DEBUG("[EMGR] Added executor: '" + exec->getModel().getInstanceId() + "'");
	m_persisTable->registerExecutor(exec->getModel().getInstanceId(),
									exec->getModel().getTimeId(),
									exec->getModel().getParentInstanceId(),
									exec->getModel().getIpcPort(),
									exec->getModel().getPID() );
}

//=============================================================================
// METHOD: SPELLexecutorManager::removeExecutorModel()
//=============================================================================
void SPELLexecutorManager::removeExecutorModel( const std::string& instanceId )
{
	ExecutorMap::iterator it = m_executors.find(instanceId);
	if (it != m_executors.end())
	{
		DEBUG("[EMGR] Removing executor model: '" + instanceId + "'");
		m_persisTable->deregisterExecutor(it->second->getModel().getInstanceId());
		delete it->second;
		m_executors.erase(it);
		DEBUG("[EMGR] Removed executor: '" + instanceId + "'");
	}
	else
	{
		LOG_ERROR("[EMGR]: No executor to remove: '" + instanceId + "'");
	}
}

//=============================================================================
// METHOD: SPELLexecutorManager::getInstanceId()
//=============================================================================
std::string SPELLexecutorManager::getInstanceId( const std::string& procId )
{
	SPELLmonitor m(m_lock);
	std::string instanceId = "";
	InstanceMap::iterator it = m_instances.find(procId);
	if ( it == m_instances.end() )
	{
		instanceId = procId + "#0";
	}
	else
	{
		int instanceNum = 0;
		while(hasInstanceNumber(procId, instanceNum)) instanceNum++;
		instanceId = procId + "#" + ISTR(instanceNum);
	}
	DEBUG("[EMGR] Found instance id: '" + instanceId + "'");
	return instanceId;
}

//=============================================================================
// METHOD: SPELLexecutorManager::addInstanceNumber()
//=============================================================================
void SPELLexecutorManager::addInstanceNumber( const std::string& procId, int instanceNum )
{
	InstanceMap::iterator it = m_instances.find(procId);
	if (it == m_instances.end())
	{
		InstanceList list;
		list.push_back(instanceNum);
		m_instances.insert( std::make_pair( procId, list ) );
	}
	else
	{
		it->second.push_back(instanceNum);
	}
}

//=============================================================================
// METHOD: SPELLexecutorManager::hasInstanceNumber()
//=============================================================================
bool SPELLexecutorManager::hasInstanceNumber( const std::string& procId, int instanceNum )
{
	InstanceMap::iterator it = m_instances.find(procId);
	InstanceList::iterator lit;
	for( lit = it->second.begin(); lit != it->second.end(); lit++ )
	{
		if (*lit == instanceNum) return true;
	}
	return false;
}

//=============================================================================
// METHOD: SPELLexecutorManager::removeInstanceNumber()
//=============================================================================
void SPELLexecutorManager::removeInstanceNumber( const std::string& procId, int instanceNum )
{
	InstanceMap::iterator it = m_instances.find(procId);
	if (it != m_instances.end())
	{
		InstanceList::iterator toRemove = it->second.end();
		InstanceList::iterator lit;
		for( lit = it->second.begin(); lit != it->second.end(); lit++ )
		{
			if (*lit == instanceNum)
			{
				toRemove = lit;
				break;
			}
		}
		if (toRemove != it->second.end())
		{
			it->second.erase(toRemove);
		}
	}
}

//=============================================================================
// METHOD: SPELLexecutorManager::closeExecutor()
//=============================================================================
void SPELLexecutorManager::closeExecutor( const std::string& instanceId )
{
	SPELLmonitor m(m_lock);
	try
	{
		// Will raise exception if not found
		SPELLexecutor* exec = getExecutor(instanceId);
		exec->close();
		removeInstanceNumber( exec->getModel().getProcId(), exec->getModel().getInstanceNum() );
		removeExecutorModel( exec->getModel().getInstanceId() );
		LOG_INFO("Executor successfully closed: " + instanceId);
	}
	catch(SPELLcoreException& ex)
	{
		LOG_ERROR("Unable to find executor '" + instanceId + "', cannot close");
	}
}

//=============================================================================
// METHOD: SPELLexecutorManager::executorLost()
//=============================================================================
void SPELLexecutorManager::executorLost( SPELLexecutor& executor )
{
	LOG_WARN( "[EMGR] Executor lost: " + executor.getModel().getInstanceId());
}

//=============================================================================
// METHOD: SPELLexecutorManager::killExecutor()
//=============================================================================
void SPELLexecutorManager::killExecutor( const std::string& instanceId )
{
	try
	{
		SPELLmonitor m(m_lock);
		// Will raise exception if not found
		SPELLexecutor* exec = getExecutor(instanceId);
		exec->kill();
		removeInstanceNumber( exec->getModel().getProcId(), exec->getModel().getInstanceNum() );
		removeExecutorModel( exec->getModel().getInstanceId() );
	}
	catch(SPELLcoreException& ex)
	{
		THROW_EXCEPTION("Failed to kill executor '" + instanceId + "' ", ex.what(), SPELL_ERROR_PROCESS);
	}
}

//=============================================================================
// METHOD: SPELLexecutorManager::clearExecutor()
//=============================================================================
void SPELLexecutorManager::clearExecutor( const std::string& instanceId )
{
	m_toClear.push_back(instanceId);
}

//=============================================================================
// METHOD: SPELLexecutorManager::clearModels
//=============================================================================
void SPELLexecutorManager::clearModels()
{
	if (m_toClear.size()>0)
	{
		IdentifierList::iterator it;
		for( it = m_toClear.begin(); it != m_toClear.end(); it++)
		{
			ExecutorMap::iterator eit = m_executors.find(*it);
			if (eit != m_executors.end())
			{
				try
				{
					SPELLexecutor* exec = getExecutor(*it);
					removeInstanceNumber( exec->getModel().getProcId(), exec->getModel().getInstanceNum() );
					removeExecutorModel( exec->getModel().getInstanceId() );
				}
				catch(SPELLcoreException& ex)
				{
					LOG_ERROR("Failed to remove executor model");
				};
			}
		}
		m_toClear.clear();
	}
}

//=============================================================================
// METHOD: SPELLexecutorManager::getExecutor()
//=============================================================================
SPELLexecutor* SPELLexecutorManager::getExecutor( const std::string& instanceId )
{
	//DEBUG("[EMGR] Search for executor: '" + instanceId + "'");
	ExecutorMap::iterator it = m_executors.find(instanceId);
	if (it != m_executors.end()) return it->second;
	THROW_EXCEPTION("Cannot get executor", "No such id: " + instanceId, SPELL_ERROR_EXECUTION );
	return NULL;
}

//=============================================================================
// METHOD: SPELLexecutorManager::killAll()
//=============================================================================
void SPELLexecutorManager::killAll()
{
	DEBUG( "[EMGR] Kill all executors");
	ExecList list = getExecutorList();
	ExecList::iterator lit;
	for( lit = list.begin(); lit != list.end(); lit++)
	{
		try
		{
			killExecutor(*lit);
		}
		catch(SPELLcoreException& ex)
		{
			LOG_ERROR("Could not kill executor: " + ex.what());
		}
	}
	DEBUG( "[EMGR] Kill all executors done");
}

//=============================================================================
// METHOD: SPELLexecutorManager::getNumActiveExecutors()
//=============================================================================
unsigned int SPELLexecutorManager::getNumActiveExecutors()
{
	DEBUG( "[EMGR] Get number of active executors TRY-IN");
	SPELLmonitor m(m_lock);
	DEBUG( "[EMGR] Get number of active executors IN");
	clearModels();
	ExecutorMap::iterator it;
	unsigned int numActive = 0;
	for( it = m_executors.begin(); it != m_executors.end(); it++)
	{
		if (it->second->isActive()) numActive++;
	}
	DEBUG( "[EMGR] Get number of active executors OUT");
	return numActive;
}

//=============================================================================
// METHOD: SPELLexecutorManager::getExecutorList()
//=============================================================================
SPELLexecutorManager::ExecList SPELLexecutorManager::getExecutorList()
{
	DEBUG( "[EMGR] Get executors list TRY-IN");
	SPELLmonitor m(m_lock);
	DEBUG( "[EMGR] Get executors list IN");
	clearModels();
	ExecList ids;
	ExecutorMap::iterator it;
	for( it = m_executors.begin(); it != m_executors.end(); it++)
	{
		ids.push_back(it->first);
	}
	DEBUG( "[EMGR] Get executors list OUT");
	return ids;
}

//=============================================================================
// METHOD: SPELLexecutorManager::buildExecutorInfo()
//=============================================================================
void SPELLexecutorManager::buildExecutorInfo( const std::string& procId, SPELLipcMessage& msg )
{
	LOG_INFO("Building executor information for " + procId );

	std::string procName = 	SPELLprocedureManager::instance().getProcName(procId);

	try
	{
		// Will raise exception if not found
		SPELLexecutor* exec = getExecutor(procId);

		SPELLclient* client = exec->getControllingClient();
		msg.set( MessageField::FIELD_PROC_ID, procId );
		msg.set( MessageField::FIELD_PARENT_PROC, exec->getModel().getParentInstanceId() );
		msg.set( MessageField::FIELD_PARENT_PROC_LINE, ISTR(exec->getModel().getParentCallingLine()) );

		// The group id of a main (without parent) procedure is the same as proc id.
		std::string groupId = exec->getModel().getGroupId();
		if (groupId == "") groupId = procId;
		msg.set( MessageField::FIELD_GROUP_ID, groupId );
		// The origin id is optional
		msg.set( MessageField::FIELD_ORIGIN_ID, exec->getModel().getOriginId() );
		msg.set( MessageField::FIELD_PROC_NAME, procName);
		msg.set( MessageField::FIELD_ASRUN_NAME, exec->getModel().getAsRunFilename() );
		SPELLexecutorStatus st = exec->getStatus();
		msg.set( MessageField::FIELD_EXEC_STATUS, SPELLdataHelper::executorStatusToString(st) );
		msg.set( MessageField::FIELD_CONDITION, exec->getModel().getCondition() );
		msg.set( MessageField::FIELD_STAGE_ID, exec->getModel().getStageId() );
		msg.set( MessageField::FIELD_STAGE_TL, exec->getModel().getStageTitle() );
		msg.set( MessageField::FIELD_CSP, exec->getModel().getStack() );
		msg.set( MessageField::FIELD_CODE_NAME, exec->getModel().getCode() );

		if (exec->getModel().getUserAction().isSet())
		{
			std::string action = exec->getModel().getUserAction().getLabel();
			LOG_INFO("User action: " + action);
			msg.set( MessageField::FIELD_ACTION_LABEL, action );
			LOG_INFO("User action enabled: " + BSTR(exec->getModel().getUserAction().isEnabled()));
			msg.set( MessageField::FIELD_ACTION_ENABLED, exec->getModel().getUserAction().isEnabled() ? "true" : "false" );
		    switch(exec->getModel().getUserAction().getSeverity())
		    {
		    case LanguageConstants::INFORMATION:
				msg.set( MessageField::FIELD_ACTION_SEVERITY, MessageValue::DATA_SEVERITY_INFO );
				break;
		    case LanguageConstants::WARNING:
				msg.set( MessageField::FIELD_ACTION_SEVERITY, MessageValue::DATA_SEVERITY_WARN );
				break;
		    case LanguageConstants::ERROR:
				msg.set( MessageField::FIELD_ACTION_SEVERITY, MessageValue::DATA_SEVERITY_ERROR );
				break;
		    default:
				msg.set( MessageField::FIELD_ACTION_SEVERITY, MessageValue::DATA_SEVERITY_INFO );
				break;
		    }
		}

		LOG_INFO("[EMGR] Parent procedure  : " + exec->getModel().getParentInstanceId());
		LOG_INFO("[EMGR] Status            : " + SPELLdataHelper::executorStatusToString(st));
		LOG_INFO("[EMGR] ASRUN file        : " + exec->getModel().getAsRunFilename());
		if (client)
		{
			int controlKey = client->getClientKey();
			std::string controlStr = "";
			if (controlKey >= 0) controlStr = ISTR(controlKey);
			msg.set( MessageField::FIELD_GUI_CONTROL, controlStr );
			msg.set( MessageField::FIELD_GUI_CONTROL_HOST, client->getClientHost() );
			LOG_INFO("[EMGR] Controlling client: " + controlStr + ":" + client->getClientHost());
		}
		else
		{
			if (exec->isBackground())
			{
				LOG_INFO("[EMGR] Procedure in background");
				msg.set( MessageField::FIELD_GUI_CONTROL, "<BACKGROUND>" );
				msg.set( MessageField::FIELD_GUI_CONTROL_HOST, "" );
			}
			else
			{
				LOG_INFO("[EMGR] NO controlling client");
				msg.set( MessageField::FIELD_GUI_CONTROL, "" );
				msg.set( MessageField::FIELD_GUI_CONTROL_HOST, "" );
			}
		}
		msg.set( MessageField::FIELD_OPEN_MODE, SPELLdataHelper::openModeToString(exec->getModel().getOpenMode()) );
		LOG_INFO("[EMGR] Open mode       : " + SPELLdataHelper::openModeToString(exec->getModel().getOpenMode()));
	}
	catch(SPELLcoreException& ex)
	{
		LOG_ERROR("No executor found to complete information");
		msg.set( MessageField::FIELD_PROC_ID, procId );
		msg.set( MessageField::FIELD_GROUP_ID, procId );
		msg.set( MessageField::FIELD_ORIGIN_ID, "???" );
		msg.set( MessageField::FIELD_PARENT_PROC, " " );
		msg.set( MessageField::FIELD_PARENT_PROC_LINE, " " );
		msg.set( MessageField::FIELD_PROC_NAME, procName );
		msg.set( MessageField::FIELD_ASRUN_NAME, " " );
		msg.set( MessageField::FIELD_EXEC_STATUS, SPELLdataHelper::executorStatusToString(STATUS_UNINIT) );
		msg.set( MessageField::FIELD_CONDITION, " " );
		msg.set( MessageField::FIELD_GUI_LIST, " " );
		msg.set( MessageField::FIELD_GUI_CONTROL, " " );
		msg.set( MessageField::FIELD_GUI_CONTROL_HOST, " " );
		msg.set( MessageField::FIELD_OPEN_MODE, " " );
		msg.set( MessageField::FIELD_STAGE_ID, " " );
		msg.set( MessageField::FIELD_STAGE_TL, " " );
		msg.set( MessageField::FIELD_CSP, " " );
		msg.set( MessageField::FIELD_CODE_NAME, " " );
	}
}
