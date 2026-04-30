// ################################################################################
// FILE       : SPELLresourceManager.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the procedure resource manager
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
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLresourceManager.H"
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLexecutorUtils.H"
#include "SPELL_EXC/SPELLcommand.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"

// GLOBALS ////////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR : SPELLresourceManager::SPELLresourceManager()
//=============================================================================
SPELLresourceManager::SPELLresourceManager( SPELLframeManager& frame )
: m_frameManager(frame),
m_changeResourceQueue(5)
{
	DEBUG("[RMGR] Created");
}

//=============================================================================
// DESTRUCTOR: SPELLresourceManager::~SPELLresourceManager()
//=============================================================================
SPELLresourceManager::~SPELLresourceManager()
{
	DEBUG("[RMGR] Destroyed");
}

//=============================================================================
// METHOD    : SPELLresourceManager::setEnabled()
//=============================================================================
void SPELLresourceManager::setEnabled( bool enabled )
{
	SPELLresourceMonitor::s_enabled = enabled;
	LOG_INFO("[RMGR] Display of resources enabled: " + BSTR(enabled) );
}

//=============================================================================
// METHOD    : SPELLresourceManager::setEnabled()
//=============================================================================
bool SPELLresourceManager::isEnabled()
{
	return SPELLresourceMonitor::s_enabled;
}

//=============================================================================
// METHOD    : SPELLresourceManager::getResources()
//=============================================================================
void SPELLresourceManager::getResources()
{
	DEBUG("[RMGR] Retrieve all resources");

	if(isStatusValid())
	{
		ExecutorCommand cmd;
		cmd.id = CMD_RESOURCES;
		cmd.arg = RESOURCE_MONITOR_GET_RESOURCES;
		
		// Call through controller
		SPELLexecutor::instance().command(cmd, false);
	}
	else
	{
		SPELLexecutorStatus st = SPELLexecutor::instance().getStatus();
		std::string status = SPELLexecutorUtils::statusToString(st);
		LOG_WARN("[RMGR] Status is not valid for resource analysis: " + status);
	}
}

//=============================================================================
// METHOD    : SPELLresourceManager::analyze()
//=============================================================================
void SPELLresourceManager::analyze()
{
	m_frameManager.getResourceMonitor().analyze();
}


//=============================================================================
// METHOD    : SPELLresourceManager::changeResource()
//=============================================================================
void SPELLresourceManager::changeResource( SPELLresourceInfo& resource )
{
	DEBUG("[RMGR] Change resource " + resource.resourceName);

	if(isStatusValid())
	{
		try
		{
			ExecutorCommand cmd;
			cmd.id = CMD_RESOURCES;
			cmd.arg = RESOURCE_MONITOR_CHANGE_RESOURCE;
			
			m_changeResourceQueue.push(resource);
			// Call through controller
			SPELLexecutor::instance().command(cmd, false);

			LOG_INFO("[RMGR] Resource modification submitted: " 
						+ resource.resourceName + ": globalValue = " 
						+ resource.resourceGlobalValue 
						+ ", localValue = " + resource.resourceLocalValue);
		}
		catch( SPELLcoreException& ex )
		{
			LOG_ERROR("[RMGR] Unable to assign resource value: " + ex.what());
		}
	}
	else
	{
		SPELLexecutorStatus st = SPELLexecutor::instance().getStatus();
		std::string status = SPELLexecutorUtils::statusToString(st);
		LOG_WARN("[RMGR] Status is not valid for resource analysis: " + status);
	}
}

void SPELLresourceManager::callResourceMonitorFromClient(std::string monitorTask)
{
	if (monitorTask == RESOURCE_MONITOR_CHANGE_RESOURCE)
	{
		SPELLresourceInfo resource = m_changeResourceQueue.pull();
		m_frameManager.getResourceMonitor().changeResource(resource);
	}
	else if (monitorTask == RESOURCE_MONITOR_GET_RESOURCES)
	{
		m_frameManager.getResourceMonitor().getResources();
	}
	else
	{
		LOG_ERROR("[RMGR] Invalid monitorTask in resource manager");
	}
}

//=============================================================================
// METHOD    : SPELLresourceManager::isStatusValid()
//=============================================================================
bool SPELLresourceManager::isStatusValid()
{
	bool valid = false;
	if (m_frameManager.isReady())
	{
		SPELLexecutorStatus st = SPELLexecutor::instance().getStatus();
		switch(st)
		{
		case STATUS_PAUSED:
		case STATUS_WAITING:
		case STATUS_PROMPT:
		case STATUS_FINISHED:
		case STATUS_INTERRUPTED:
		case STATUS_ABORTED:
			valid = true;
			break;
		default:
			break;
		}
	}
	else
	{
		LOG_WARN("[RMGR] Frame manager is not ready for resource analysis");
	}
	return valid;
}
