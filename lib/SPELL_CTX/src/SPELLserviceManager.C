// ################################################################################
// FILE       : SPELLserviceManager.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the executor manager
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
#include "SPELL_CTX/SPELLserviceManager.H"
#include "SPELL_CTX/SPELLservice.H"
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
SPELLserviceManager* SPELLserviceManager::s_instance = NULL;

//=============================================================================
// CONSTRUCTOR: SPELLexecutorManager::SPELLexecutorManager()
//=============================================================================
SPELLserviceManager::SPELLserviceManager()
{
	m_persisTable = NULL;
}

//=============================================================================
// DESTRUCTOR: SPELLexecutorManager::~SPELLexecutorManager()
//=============================================================================
SPELLserviceManager::~SPELLserviceManager()
{
}

//=============================================================================
// STATIC: SPELLexecutorManager::instance()
//=============================================================================
SPELLserviceManager& SPELLserviceManager::instance()
{
	if (s_instance == NULL)
	{
		s_instance = new SPELLserviceManager();
	}
	return *s_instance;
}

//=============================================================================
// METHOD: SPELLexecutorManager::setup()
//=============================================================================
void SPELLserviceManager::setup( const std::string& ctxName )
{
	m_contextName = ctxName;
	m_persisTable = new SPELLpersistencyTable(ctxName + "_services");
	if (m_persisTable->load())
	{
		reconnectServices();
	}
}

//=============================================================================
// METHOD: SPELLexecutorManager::setup()
//=============================================================================
void SPELLserviceManager::cleanup()
{
	if (m_persisTable) delete m_persisTable;
	for( ServiceMap::iterator it = m_pendingLogin.begin(); it != m_pendingLogin.end(); it++ )
	{
		delete it->second;
	}
	m_pendingLogin.clear();
}

//=============================================================================
// METHOD: SPELLexecutorManager::startExecutor()
//=============================================================================
void SPELLserviceManager::startService( SPELLserviceStartupParams& config)
{
	DEBUG( "[SMGR] Start service TRY-IN");

	SPELLmonitor m(m_lock);
	DEBUG( "[SMGR] Start executor IN");
	config.setContextName(m_contextName);
	config.setConfigFile(SPELLconfiguration::instance().getFile());

	// Create and start the executor with the given configuration

	// If the open mode does not include VISIBLE, do not pass the controlling client
	// This way we force the GUI to attach to a proc when it is started in non-visible
	SPELLservice* exec = NULL;

    DEBUG( "[SMGR] Start executor in background");
    exec = new SPELLservice(config);

	try
	{
		LOG_INFO("Starting service " + exec->getModel().getInstanceId() );
		exec->start();

		addInstanceNumber( exec->getModel().getProcId(), exec->getModel().getInstanceNum() );
		addServiceModel( exec );

		LOG_INFO("Service ready");
	}
	catch(SPELLcoreException& ex)
	{
		// Delete recovery files just created for the new executor file
		deleteServiceFiles(config);
		DEBUG( "[SMGR] Start service OUT");
		THROW_EXCEPTION("Failed to start service", ex.what(), SPELL_ERROR_PROCESS);
	}
	DEBUG( "[SMGR] Start executor OUT");
}

//=============================================================================
// METHOD: SPELLexecutorManager::reconnectExecutors()
//=============================================================================
void SPELLserviceManager::reconnectServices()
{
	DEBUG( "[SMGR] Reconnecting orphan services");
	SPELLmonitor m(m_lock);
	std::vector<std::string> ids = m_persisTable->getRegisteredExecutors();
	m_pendingLogin.clear();
	for(std::vector<std::string>::iterator it = ids.begin(); it != ids.end(); it++)
	{
		std::string instanceId = *it;
		SPELLexecutorPersistency pers = m_persisTable->getExecutorPersistency(instanceId);

		if (getpgid(pers.pid) < 0)
		{
		    DEBUG( "[SMGR] PID " + ISTR(pers.pid) + " for service "+instanceId+" not found");
		    continue;
		}

		SPELLserviceStartupParams config( instanceId, pers.timeId, true );
		config.setParentInstanceId(pers.parentId);
		config.setIpcPort(pers.ipcPort);
		config.setPID(pers.pid);

		SPELLservice* exec = new SPELLservice(config);
		try
		{
			LOG_WARN("Reconnecting service " + instanceId + " listening on port " + ISTR(pers.ipcPort) );
			exec->start();
			m_services.insert( std::make_pair( exec->getModel().getInstanceId(), exec ));
			m_pendingLogin.insert( std::make_pair( instanceId, exec ) );
		}
		catch(SPELLcoreException& ex)
		{
			deleteServiceFiles(config);
		}
	}
	DEBUG( "[SMGR] Reconnect services done");
}

//=============================================================================
// METHOD: SPELLexecutorManager::callback_executorReconnected()
//=============================================================================
void SPELLserviceManager::callback_serviceReconnected( const std::string& instanceId )
{
	LOG_WARN("#######################################");
	LOG_WARN("Service " + instanceId + " reconnected");
	LOG_WARN("#######################################");
	SPELLmonitor m(m_lock);
	ServiceMap::iterator it = m_pendingLogin.find(instanceId);
	if (it != m_pendingLogin.end())
	{
		SPELLservice* exec = it->second;
		addInstanceNumber( exec->getModel().getProcId(), exec->getModel().getInstanceNum() );
		addServiceModel( exec );
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
void SPELLserviceManager::callback_serviceNotReconnected( const std::string& instanceId )
{
	LOG_ERROR("#######################################");
	LOG_ERROR("Service " + instanceId + " failed to reconnect");
	LOG_ERROR("#######################################");
	SPELLmonitor m(m_lock);
	ServiceMap::iterator it = m_pendingLogin.find(instanceId);
	if (it != m_pendingLogin.end())
	{
		m_persisTable->deregisterExecutor(it->second->getModel().getInstanceId());
		delete it->second;
		m_pendingLogin.erase(it);
	}
}

////=============================================================================
//// METHOD: SPELLexecutorManager::recoverExecutor()
////=============================================================================
//void SPELLserviceManager::recoverExecutor( SPELLexecutorStartupParams& config, SPELLclient* controllingClient )
//{
//	//TODO check maximum number of executors
//	SPELLmonitor m(m_lock);
//	config.setContextName(m_contextName);
//	config.setConfigFile(SPELLconfiguration::instance().getFile());
//
//	// Create and start the executor with the given configuration
//
//	// If the open mode does not include VISIBLE, do not pass the controlling client
//	// This way we force the GUI to attach to a proc when it is started in non-visible
//	SPELLexecutor* exec = new SPELLexecutor(config, controllingClient);
//	try
//	{
//		LOG_INFO("Recovering executor " + exec->getModel().getInstanceId() );
//		exec->start();
//
//		addInstanceNumber( exec->getModel().getProcId(), exec->getModel().getInstanceNum() );
//		addExecutorModel( exec );
//
//		LOG_INFO("Executor ready");
//	}
//	catch(SPELLcoreException& ex)
//	{
//		// Delete recovery files just created for the new executor file
//		deleteExecutorFiles(config);
//		THROW_EXCEPTION("Failed to recover executor", ex.what(), SPELL_ERROR_PROCESS);
//	}
//}

//=============================================================================
// METHOD: SPELLexecutorManager::deleteExecutorFiles()
//=============================================================================
void SPELLserviceManager::deleteServiceFiles( const SPELLserviceStartupParams& config )
{
	SPELLcontextConfig& ctxConfig = SPELLconfiguration::instance().getContext(m_contextName);

	std::string fileName = config.getTimeId() + "_Service_" + config.getInstanceId();

	LOG_INFO("Deleting service files: " + fileName );

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
void SPELLserviceManager::addServiceModel( SPELLservice* exec )
{
    const std::string id = exec->getModel().getInstanceId();
    if (m_services.count(id) > 0)
    {
        delete m_services[id];
        m_services.erase(id);
	}
    m_services.insert( std::make_pair( exec->getModel().getInstanceId(), exec ));
	DEBUG("[SMGR] Added executor: '" + exec->getModel().getInstanceId() + "'");
	m_persisTable->registerExecutor(exec->getModel().getInstanceId(),
									exec->getModel().getTimeId(),
									exec->getModel().getParentInstanceId(),
									exec->getModel().getIpcPort(),
									exec->getModel().getPID() );
}

//=============================================================================
// METHOD: SPELLexecutorManager::removeExecutorModel()
//=============================================================================
void SPELLserviceManager::removeServiceModel( const std::string& instanceId )
{
	ServiceMap::iterator it = m_services.find(instanceId);
	if (it != m_services.end())
	{
		DEBUG("[SMGR] Removing service model: '" + instanceId + "'");
		m_persisTable->deregisterExecutor(it->second->getModel().getInstanceId());
		delete it->second;
		m_services.erase(it);
		DEBUG("[SMGR] Removed service: '" + instanceId + "'");
	}
	else
	{
		LOG_ERROR("[SMGR]: No service to remove: '" + instanceId + "'");
	}
}

//=============================================================================
// METHOD: SPELLexecutorManager::getInstanceId()
//=============================================================================
std::string SPELLserviceManager::getInstanceId( const std::string& procId )
{
	SPELLmonitor m(m_lock);

	// We never remove instance numbers but we can live with it - memory consumption
	// is not big and do not need to be concerned with race conditions between add and get
	// due to simultaneous requests from *different* clients (e.g .GUI and Parent Exec).

	// Nevertheless attempt to clean now, protected by the lock, before getting an instance id.
	// Just clean if no executors running.
	if (m_services.size()==0) m_instances.clear();

	std::string instanceId = "";
	InstanceMap::iterator it = m_instances.find(procId);
	if ( it == m_instances.end() )
	{
		instanceId = procId;
	}
	else
	{
		int instanceNum = 0;
		while(hasInstanceNumber(procId, instanceNum)) instanceNum++;
		addInstanceNumber(procId, instanceNum);
		instanceId = procId;
	}
	LOG_INFO("Assigned instance id: '" + instanceId + "'");
	return instanceId;
}

//=============================================================================
// METHOD: SPELLexecutorManager::addInstanceNumber()
//=============================================================================
void SPELLserviceManager::addInstanceNumber( const std::string& procId, int instanceNum )
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
bool SPELLserviceManager::hasInstanceNumber( const std::string& procId, int instanceNum )
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
// METHOD: SPELLexecutorManager::closeExecutor()
//=============================================================================
void SPELLserviceManager::closeService( const std::string& instanceId )
{
	SPELLmonitor m(m_lock);
	try
	{
		// Will raise exception if not found
		SPELLservice* exec = getService(instanceId);
		exec->close();
		removeServiceModel( exec->getModel().getInstanceId() );
		LOG_INFO("Service successfully closed: " + instanceId);
	}
	catch(SPELLcoreException& ex)
	{
		LOG_ERROR("Unable to find service '" + instanceId + "', cannot close");
	}
}

//=============================================================================
// METHOD: SPELLexecutorManager::executorLost()
//=============================================================================
void SPELLserviceManager::serviceLost( SPELLservice& service )
{
	LOG_WARN( "[SMGR] Service lost: " + service.getModel().getInstanceId());
	clearService(service.getModel().getInstanceId());
}

//=============================================================================
// METHOD: SPELLexecutorManager::killExecutor()
//=============================================================================
void SPELLserviceManager::killService( const std::string& instanceId )
{
	try
	{
		SPELLmonitor m(m_lock);
		// Will raise exception if not found
		SPELLservice* exec = getService(instanceId);
		exec->kill();
		removeServiceModel( exec->getModel().getInstanceId() );
	}
	catch(SPELLcoreException& ex)
	{
		THROW_EXCEPTION("Failed to kill Service '" + instanceId + "' ", ex.what(), SPELL_ERROR_PROCESS);
	}
}

//=============================================================================
// METHOD: SPELLexecutorManager::clearExecutor()
//=============================================================================
void SPELLserviceManager::clearService( const std::string& instanceId )
{
    SPELLmonitor m(m_lock);
	m_toClear.push_back(instanceId);
}

//=============================================================================
// METHOD: SPELLexecutorManager::clearModels
//=============================================================================
void SPELLserviceManager::clearModels()
{
	if (m_toClear.size()>0)
	{
		IdentifierList::iterator it;
		for( it = m_toClear.begin(); it != m_toClear.end(); it++)
		{
			ServiceMap::iterator eit = m_services.find(*it);
			if (eit != m_services.end())
			{
				try
				{
					SPELLservice* exec = getService(*it);
					removeServiceModel( exec->getModel().getInstanceId() );
				}
				catch(SPELLcoreException& ex)
				{
					LOG_ERROR("Failed to remove executor model");
				};
			}
		}
		m_toClear.clear();
		if (m_services.empty()){
			m_instances.clear();
		}
	}
}

//=============================================================================
// METHOD: SPELLexecutorManager::getExecutor()
//=============================================================================
SPELLservice* SPELLserviceManager::getService( const std::string& instanceId )
{
	DEBUG("[SMGR] Search for service: '" + instanceId + "'");
	int idx = instanceId.find("#");
	std::string id = instanceId;
	if (idx >= 0)
	    id = id.substr(0,idx);
	ServiceMap::iterator it = m_services.find(id);
	if (it != m_services.end()) return it->second;
	THROW_EXCEPTION("Cannot get service", "No such id: " + instanceId, SPELL_ERROR_EXECUTION );
}

//=============================================================================
// METHOD: SPELLexecutorManager::killAll()
//=============================================================================
void SPELLserviceManager::killAll()
{
	DEBUG( "[SMGR] Kill all Services");
	ServiceList list = getServiceList();
	ServiceList::iterator lit;
	for( lit = list.begin(); lit != list.end(); lit++)
	{
		try
		{
			killService(*lit);
		}
		catch(SPELLcoreException& ex)
		{
			LOG_ERROR("Could not kill service: " + ex.what());
		}
	}
	DEBUG( "[SMGR] Kill all services done");
}

//=============================================================================
// METHOD: SPELLexecutorManager::getNumActiveExecutors()
//=============================================================================
unsigned int SPELLserviceManager::getNumActiveServices()
{
	DEBUG( "[SMGR] Get number of active services TRY-IN");
	SPELLmonitor m(m_lock);
	DEBUG( "[SMGR] Get number of active services IN");
	clearModels();
	ServiceMap::iterator it;
	unsigned int numActive = 0;
	for( it = m_services.begin(); it != m_services.end(); it++)
	{
		if (it->second->isActive()) numActive++;
	}
	DEBUG( "[SMGR] Get number of active services OUT");
	return numActive;
}

//=============================================================================
// METHOD: SPELLexecutorManager::getExecutorList()
//=============================================================================
SPELLserviceManager::ServiceList SPELLserviceManager::getServiceList()
{
	SPELLmonitor m(m_lock);
	clearModels();
	ServiceList ids;
	ServiceMap::iterator it;
	for( it = m_services.begin(); it != m_services.end(); it++)
	{
		ids.push_back(it->first);
	}
	//DEBUG( "[SMGR] Get service list OUT");
	return ids;
}

//=============================================================================
// METHOD: SPELLexecutorManager::buildExecutorInfo()
//=============================================================================
void SPELLserviceManager::buildServiceInfo( const std::string& serviceId, SPELLipcMessage& msg )
{
	LOG_INFO("Building service information for " + serviceId );

	try
	{
		// Will raise exception if not found
	    std::string procId;
	    std::string::size_type idx = serviceId.find("#");
	    if (idx != std::string::npos)
	        procId = serviceId.substr(0,idx);
	    else
	        procId = serviceId;
		SPELLservice* exec = getService(procId);
		SPELLpyService& service = SPELLprocedureManager::instance().getService(procId);

		msg.set( MessageField::FIELD_PROC_ID, procId);
		msg.set( MessageField::FIELD_PARENT_PROC, exec->getModel().getParentInstanceId() );
		msg.set( MessageField::FIELD_PARENT_PROC_LINE, ISTR(exec->getModel().getParentCallingLine()) );

		// The group id of a main (without parent) procedure is the same as proc id.
		std::string groupId = exec->getModel().getGroupId();
		if (groupId == "") groupId = procId;
		msg.set( MessageField::FIELD_GROUP_ID, groupId );
		// The origin id is optional
		msg.set( MessageField::FIELD_ORIGIN_ID, exec->getModel().getOriginId() );
		msg.set( MessageField::FIELD_PROC_NAME, service.getName() + "#0");
		msg.set( MessageField::FIELD_ASRUN_NAME, exec->getModel().getAsRunFilename() );
		msg.set( MessageField::FIELD_LOG_NAME, exec->getModel().getLogFilename() );
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

		LOG_INFO("[SMGR] Parent procedure  : " + exec->getModel().getParentInstanceId());
		LOG_INFO("[SMGR] Status            : " + SPELLdataHelper::executorStatusToString(st));
		LOG_INFO("[SMGR] ASRUN file        : " + exec->getModel().getAsRunFilename());


        LOG_INFO("[SMGR] Procedure in background");
        msg.set( MessageField::FIELD_GUI_CONTROL, "<BACKGROUND>" );
        msg.set( MessageField::FIELD_GUI_CONTROL_HOST, "" );

		msg.set( MessageField::FIELD_OPEN_MODE, SPELLdataHelper::openModeToString(exec->getModel().getOpenMode()) );
		LOG_INFO("[SMGR] Open mode       : " + SPELLdataHelper::openModeToString(exec->getModel().getOpenMode()));
	}
	catch(SPELLcoreException& ex)
	{
		LOG_ERROR("No service found to complete information");
		msg.set( MessageField::FIELD_PROC_ID, serviceId );
		msg.set( MessageField::FIELD_GROUP_ID, serviceId );
		msg.set( MessageField::FIELD_ORIGIN_ID, "???" );
		msg.set( MessageField::FIELD_PARENT_PROC, " " );
		msg.set( MessageField::FIELD_PARENT_PROC_LINE, " " );
		msg.set( MessageField::FIELD_PROC_NAME, "" );
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
